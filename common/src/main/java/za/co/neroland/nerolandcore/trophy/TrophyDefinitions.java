package za.co.neroland.nerolandcore.trophy;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.platform.Services;

/**
 * Loads trophy and collection definitions from datapacks, reload-safely.
 *
 * <ul>
 *   <li>Trophies: {@code data/<namespace>/neroland_trophies/<path>.json} → id {@code <namespace>:<path>}</li>
 *   <li>Collections: {@code data/<namespace>/neroland_trophy_collections/<path>.json} → id
 *       {@code <namespace>:<path>}</li>
 * </ul>
 *
 * <p>The cache is keyed on both the server and its resource manager (held weakly), so a {@code /reload}
 * — which swaps the resource manager — is picked up by the next read, and a stopped integrated server
 * can be collected. Core needs no lifecycle hook for this.
 *
 * <p><b>Skip rules</b>, per file, in sorted id order: over a bound → issue {@code too_large} or
 * {@code limit}; unreadable or failing the codec → issue {@code malformed}; a {@code requires_mod} not
 * loaded → skipped quietly; icon item not registered → skipped quietly. Collections then drop members
 * that were skipped quietly, drop a completion {@code trophy} that did not load, are rejected
 * ({@code unknown_member}) for a member that never had a loadable file, are rejected ({@code id_clash}) if
 * their id is also a trophy id, and are skipped quietly when fewer than two members remain.
 *
 * <p><b>Logging.</b> Per-file lines at DEBUG only (file id and reason token). One summary line per load:
 * DEBUG when clean, WARN with counts only when files were rejected. File content is never logged.
 *
 * @since 1.20.0
 */
public final class TrophyDefinitions {

    public static final String TROPHY_DIRECTORY = "neroland_trophies";
    public static final String COLLECTION_DIRECTORY = "neroland_trophy_collections";

    /** Largest data file read, in bytes. */
    public static final int MAX_FILE_BYTES = 16 * 1024;
    /** Most files examined per root, loadable or not. */
    public static final int MAX_FILES_PER_ROOT = 8192;
    /** Most trophies loaded. */
    public static final int MAX_TROPHIES = 4096;
    /** Most collections loaded. */
    public static final int MAX_COLLECTIONS = 512;
    /** Most members one collection may list. */
    public static final int MAX_MEMBERS = 256;

    /** One immutable load. Readers never see a partial load. */
    public record Snapshot(Map<Identifier, TrophyDefinition> trophies, Map<Identifier, TrophyCollection> collections,
            long generation) {

        public static final Snapshot EMPTY = new Snapshot(Map.of(), Map.of(), 0L);

        public Snapshot {
            trophies = Map.copyOf(trophies);
            collections = Map.copyOf(collections);
        }

        public Optional<TrophyDefinition> trophy(Identifier id) {
            return Optional.ofNullable(trophies.get(id));
        }

        public Optional<TrophyCollection> collection(Identifier id) {
            return Optional.ofNullable(collections.get(id));
        }
    }

    /** Lists one data root: resource path → resource. */
    @FunctionalInterface
    interface Lister {
        Map<Identifier, Resource> list(String directory);
    }

    /** A load's snapshot and report. */
    record Result(Snapshot snapshot, TrophyLoadReport report) {
    }

    private static WeakReference<Object> loadedFor = new WeakReference<>(null);
    private static WeakReference<Object> loadedFrom = new WeakReference<>(null);
    private static Snapshot current = Snapshot.EMPTY;
    private static TrophyLoadReport lastReport = TrophyLoadReport.EMPTY;
    private static long generation;

    private TrophyDefinitions() {
    }

    /**
     * The definitions for {@code server}, reloading first if the server or its resource manager changed
     * since the last load. Server thread.
     */
    public static synchronized Snapshot forServer(MinecraftServer server) {
        ResourceManager resources = server.getResourceManager();
        return cached(server, resources,
                directory -> resources.listResources(directory, file -> file.getPath().endsWith(".json")),
                modId -> Services.PLATFORM.isModLoaded(modId),
                item -> BuiltInRegistries.ITEM.containsKey(item));
    }

    /**
     * Increments on every successful load. Moves only when {@link #forServer} is called after a change;
     * to detect a reload, poll {@code forServer(server).generation()}.
     */
    public static synchronized long generation() {
        return generation;
    }

    /** The last load's counts and issues. */
    public static synchronized TrophyLoadReport report() {
        return lastReport;
    }

    /** Forget the cache (a trophy provider may call this when its server stops). */
    public static synchronized void clear() {
        loadedFor = new WeakReference<>(null);
        loadedFrom = new WeakReference<>(null);
        current = Snapshot.EMPTY;
        lastReport = TrophyLoadReport.EMPTY;
    }

    /** Loads through a resource manager (generation 0, nothing cached). */
    static Snapshot load(ResourceManager resources, Predicate<String> modLoaded, Predicate<Identifier> itemExists) {
        return loadFrom(directory -> resources.listResources(directory, file -> file.getPath().endsWith(".json")),
                modLoaded, itemExists, 0L).snapshot();
    }

    /** The caching core of {@link #forServer}, keyed on object identity so it can be tested without a server. */
    static synchronized Snapshot cached(Object server, Object resources, Lister lister, Predicate<String> modLoaded,
            Predicate<Identifier> itemExists) {
        if (server == loadedFor.get() && resources == loadedFrom.get()) {
            return current;
        }
        boolean sameServer = server == loadedFor.get();
        // Recorded before loading: a load that fails is not retried on every read.
        loadedFor = new WeakReference<>(server);
        loadedFrom = new WeakReference<>(resources);
        Result result;
        try {
            result = loadFrom(lister, modLoaded, itemExists, generation + 1);
        // LinkageError too: a jar built for another Minecraft line fails here with NoSuchMethodError
        // (ResourceManager.listResources changed signature in 26.3; Sentry MC-NEROLAND-CORE-2).
        } catch (RuntimeException | LinkageError failure) {
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Trophy data could not be listed ({}); {}.",
                    failure.getClass().getSimpleName(),
                    sameServer ? "keeping the previous trophy data" : "no trophies are loaded");
            if (!sameServer) {
                current = Snapshot.EMPTY;
                lastReport = TrophyLoadReport.EMPTY;
            }
            return current;
        }
        generation++;
        current = result.snapshot();
        lastReport = result.report();
        logSummary(lastReport);
        return current;
    }

    /** Loads both roots. Throws only if listing a root fails. */
    static Result loadFrom(Lister lister, Predicate<String> modLoaded, Predicate<Identifier> itemExists,
            long newGeneration) {
        Issues issues = new Issues();
        int absentMod = 0;
        int missingIcon = 0;

        Map<Identifier, TrophyDefinition> trophies = new LinkedHashMap<>();
        Set<Identifier> skippedQuietly = new HashSet<>();
        int examined = 0;
        boolean limitReported = false;
        for (Map.Entry<Identifier, Resource> entry : sorted(lister.list(TROPHY_DIRECTORY)).entrySet()) {
            Identifier file = entry.getKey();
            Identifier id = toDefinitionId(TROPHY_DIRECTORY, file);
            if (id == null) {
                continue;
            }
            if (examined >= MAX_FILES_PER_ROOT || trophies.size() >= MAX_TROPHIES) {
                if (!limitReported) {
                    issues.add(file, TrophyLoadReport.LIMIT);
                    limitReported = true;
                }
                continue;
            }
            examined++;
            TrophyDefinition definition;
            try {
                definition = TrophyDefinition.of(id, TrophyDefinition.DATA_CODEC
                        .parse(JsonOps.INSTANCE, read(entry.getValue())).getOrThrow());
            } catch (TooLarge tooLarge) {
                issues.add(file, TrophyLoadReport.TOO_LARGE);
                continue;
            } catch (Exception | StackOverflowError malformed) {
                issues.add(file, TrophyLoadReport.MALFORMED);
                continue;
            }
            if (!allLoaded(definition.requiresMods(), modLoaded)) {
                absentMod++;
                skippedQuietly.add(id);
                NerolandCoreCommon.LOGGER.debug("[Neroland Core] Trophy {} skipped: required mod absent", id);
                continue;
            }
            if (!itemExists.test(definition.icon())) {
                missingIcon++;
                skippedQuietly.add(id);
                NerolandCoreCommon.LOGGER.debug("[Neroland Core] Trophy {} skipped: icon item missing", id);
                continue;
            }
            trophies.put(id, definition);
        }

        Map<Identifier, TrophyCollection> collections = new LinkedHashMap<>();
        examined = 0;
        limitReported = false;
        for (Map.Entry<Identifier, Resource> entry : sorted(lister.list(COLLECTION_DIRECTORY)).entrySet()) {
            Identifier file = entry.getKey();
            Identifier id = toDefinitionId(COLLECTION_DIRECTORY, file);
            if (id == null) {
                continue;
            }
            if (examined >= MAX_FILES_PER_ROOT || collections.size() >= MAX_COLLECTIONS) {
                if (!limitReported) {
                    issues.add(file, TrophyLoadReport.LIMIT);
                    limitReported = true;
                }
                continue;
            }
            examined++;
            TrophyCollection.Data data;
            try {
                data = TrophyCollection.DATA_CODEC.parse(JsonOps.INSTANCE, read(entry.getValue())).getOrThrow();
            } catch (TooLarge tooLarge) {
                issues.add(file, TrophyLoadReport.TOO_LARGE);
                continue;
            } catch (Exception | StackOverflowError malformed) {
                issues.add(file, TrophyLoadReport.MALFORMED);
                continue;
            }
            if (trophies.containsKey(id) || skippedQuietly.contains(id)) {
                issues.add(file, TrophyLoadReport.ID_CLASH);
                continue;
            }
            if (data.members().size() > MAX_MEMBERS) {
                issues.add(file, TrophyLoadReport.LIMIT);
                continue;
            }
            if (!allLoaded(data.requiresMods(), modLoaded)) {
                absentMod++;
                NerolandCoreCommon.LOGGER.debug("[Neroland Core] Collection {} skipped: required mod absent", id);
                continue;
            }
            if (!itemExists.test(data.icon())) {
                missingIcon++;
                NerolandCoreCommon.LOGGER.debug("[Neroland Core] Collection {} skipped: icon item missing", id);
                continue;
            }
            Set<Identifier> members = new LinkedHashSet<>();
            boolean unknown = false;
            for (Identifier member : data.members()) {
                if (trophies.containsKey(member)) {
                    members.add(member);
                } else if (!skippedQuietly.contains(member)) {
                    unknown = true;
                    break;
                }
            }
            if (unknown) {
                issues.add(file, TrophyLoadReport.UNKNOWN_MEMBER);
                continue;
            }
            if (members.size() < 2) {
                NerolandCoreCommon.LOGGER.debug("[Neroland Core] Collection {} skipped: fewer than two members", id);
                continue;
            }
            Optional<Identifier> trophy = data.trophy().filter(trophies::containsKey);
            collections.put(id, TrophyCollection.of(id, data, List.copyOf(members), trophy));
        }

        TrophyLoadReport report = new TrophyLoadReport(trophies.size(), collections.size(), absentMod, missingIcon,
                issues.kept, issues.dropped);
        return new Result(new Snapshot(trophies, collections, newGeneration), report);
    }

    private static boolean allLoaded(List<String> mods, Predicate<String> modLoaded) {
        for (String mod : mods) {
            if (!modLoaded.test(mod)) {
                return false;
            }
        }
        return true;
    }

    private static Map<Identifier, Resource> sorted(Map<Identifier, Resource> listed) {
        return new TreeMap<>(listed);
    }

    private static JsonElement read(Resource resource) throws Exception {
        byte[] bytes;
        try (InputStream in = resource.open()) {
            bytes = in.readNBytes(MAX_FILE_BYTES + 1);
        }
        if (bytes.length > MAX_FILE_BYTES) {
            throw new TooLarge();
        }
        return JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
    }

    @Nullable
    private static Identifier toDefinitionId(String directory, Identifier file) {
        String path = file.getPath();
        if (!path.startsWith(directory + "/") || !path.endsWith(".json")) {
            return null;
        }
        String stripped = path.substring(directory.length() + 1, path.length() - ".json".length());
        if (stripped.isEmpty()) {
            return null;
        }
        return Identifier.tryBuild(file.getNamespace(), stripped);
    }

    private static void logSummary(TrophyLoadReport report) {
        if (report.issueCount() == 0) {
            NerolandCoreCommon.LOGGER.debug("[Neroland Core] Trophy data: {} loaded, {} collections", report.trophies(),
                    report.collections());
        } else {
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Trophy data: {} loaded, {} collections, {} malformed files ignored",
                    report.trophies(), report.collections(), report.issueCount());
        }
    }

    /** Thrown for a file over {@link #MAX_FILE_BYTES}. */
    private static final class TooLarge extends Exception {
        private static final long serialVersionUID = 1L;

        TooLarge() {
            super(null, null, false, false);
        }
    }

    /** The bounded issue list. */
    private static final class Issues {
        final List<TrophyLoadReport.Issue> kept = new ArrayList<>();
        int dropped;

        void add(Identifier file, String reason) {
            NerolandCoreCommon.LOGGER.debug("[Neroland Core] Trophy data file {} rejected: {}", file, reason);
            if (kept.size() < TrophyLoadReport.MAX_ISSUES) {
                kept.add(new TrophyLoadReport.Issue(file, reason));
            } else {
                dropped++;
            }
        }
    }
}
