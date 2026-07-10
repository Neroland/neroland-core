package za.co.neroland.nerolandcore.meteor;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * The server-side aggregate of grindable meteor materials. Built from two sources
 * with a single, well-defined precedence; queried by consumers through
 * {@link MeteorMaterials}.
 *
 * <h2>Sources &amp; precedence (lowest → highest)</h2>
 * <ol>
 *   <li>{@link GrindableMaterial} annotation entries (via {@link MeteorAnnotationScanner});</li>
 *   <li>data files at {@code data/<ns>/neroland/meteor_materials/<id>.json}, folded across the
 *       <b>full datapack stack</b> for each id.</li>
 * </ol>
 * The material id is the file path id ({@code <ns>:<path>}); an annotation entry's id is its item id,
 * so a data file shipped at the matching path overrides the annotation — the datapack-override principle.
 *
 * <h2>Partial-field merge</h2>
 * For a given material id every datapack layer is read low→high and overlaid field by field onto the
 * annotation base. A layer overrides only the keys it names; <b>an absent key inherits</b> the lower
 * layer, while an <b>explicit JSON {@code null}</b> overrides to "none" (clears the field). {@code tier}
 * is mandatory across the merged result (an entry with no tier is dropped with a warning); {@code item}
 * defaults to the material id; {@code enabled} defaults to {@code true}; {@code min_gate} / {@code planet}
 * / {@code weight_override} default to none.
 *
 * <p>Loaded lazily per running server and cached; {@link #reload(MinecraftServer)} re-reads after a
 * datapack {@code /reload}. Falls back to {@link #builtinDefaults()} (Core's four base materials) only
 * when nothing is found at all.
 *
 * <p>Holds <b>item metadata only</b> — no player data (POPIA/GDPR).
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class MeteorMaterialRegistry {

    private static final String DIRECTORY = "neroland/meteor_materials";
    private static final String SUFFIX = ".json";

    @Nullable
    private static MinecraftServer loadedFor;
    private static Map<Identifier, MeteorMaterialEntry> entries = builtinDefaults();

    private MeteorMaterialRegistry() {
    }

    /** The aggregate for this server (loads + caches on first use). */
    public static synchronized Map<Identifier, MeteorMaterialEntry> forServer(MinecraftServer server) {
        if (server != loadedFor) {
            entries = load(server.getResourceManager());
            loadedFor = server;
        }
        return entries;
    }

    /** Force a re-read for {@code server} (call from a datapack reload listener). */
    public static synchronized Map<Identifier, MeteorMaterialEntry> reload(MinecraftServer server) {
        entries = load(server.getResourceManager());
        loadedFor = server;
        return entries;
    }

    /** The currently loaded aggregate (built-ins until a server loads its datapacks). */
    public static Collection<MeteorMaterialEntry> current() {
        return entries.values();
    }

    @Nullable
    public static MeteorMaterialEntry get(Identifier id) {
        return entries.get(id);
    }

    // --- loading ------------------------------------------------------------

    private static Map<Identifier, MeteorMaterialEntry> load(ResourceManager resources) {
        // Start from annotation entries (lowest precedence), keyed by material id.
        Map<Identifier, Accumulator> acc = new LinkedHashMap<>();
        for (MeteorMaterialEntry annotated : scanAnnotations()) {
            acc.put(annotated.id(), Accumulator.fromEntry(annotated));
        }

        try {
            Map<Identifier, Resource> files =
                    resources.listResources(DIRECTORY, file -> file.getPath().endsWith(SUFFIX));
            for (Identifier fileId : files.keySet()) {
                Identifier materialId = toMaterialId(fileId);
                if (materialId == null) {
                    continue;
                }
                Accumulator a = acc.computeIfAbsent(materialId, id -> new Accumulator());
                // Fold the whole datapack stack low→high so higher packs override field-by-field.
                for (Resource layer : resources.getResourceStack(fileId)) {
                    try (BufferedReader reader = layer.openAsReader()) {
                        JsonElement json = JsonParser.parseReader(reader);
                        if (json != null && json.isJsonObject()) {
                            a.overlay(json.getAsJsonObject(), materialId);
                        }
                    } catch (Exception e) {
                        NerolandCoreCommon.LOGGER.warn(
                                "[Neroland Core] Could not read meteor material {}", materialId, e);
                    }
                }
            }
        } catch (RuntimeException e) {
            NerolandCoreCommon.LOGGER.warn(
                    "[Neroland Core] Meteor material load failed; using built-ins.", e);
            return builtinDefaults();
        }

        Map<Identifier, MeteorMaterialEntry> loaded = new LinkedHashMap<>();
        for (Map.Entry<Identifier, Accumulator> e : acc.entrySet()) {
            MeteorMaterialEntry entry = e.getValue().build(e.getKey());
            if (entry != null) {
                loaded.put(e.getKey(), entry);
            }
        }
        return loaded.isEmpty() ? builtinDefaults() : loaded;
    }

    private static List<MeteorMaterialEntry> scanAnnotations() {
        try {
            return MeteorAnnotationScanner.INSTANCE.scan();
        } catch (RuntimeException | LinkageError e) {
            NerolandCoreCommon.LOGGER.warn(
                    "[Neroland Core] Meteor annotation scan failed; data files only.", e);
            return List.of();
        }
    }

    /** {@code <ns>:neroland/meteor_materials/foo.json} -> {@code <ns>:foo}. */
    @Nullable
    private static Identifier toMaterialId(Identifier file) {
        String path = file.getPath();
        if (!path.startsWith(DIRECTORY + "/") || !path.endsWith(SUFFIX)) {
            return null;
        }
        String trimmed = path.substring(DIRECTORY.length() + 1, path.length() - SUFFIX.length());
        return Identifier.fromNamespaceAndPath(file.getNamespace(), trimmed);
    }

    // --- built-in fallback (Core's four base materials) ---------------------

    /** Core's canonical materials, also shipped as data files so a pack can override them. */
    public static Map<Identifier, MeteorMaterialEntry> builtinDefaults() {
        Map<Identifier, MeteorMaterialEntry> map = new LinkedHashMap<>();
        put(map, "nero_alloy", "nerolandcore:nero_alloy_dust", MeteorTier.COMMON, null);
        put(map, "plasma_glass", "nerolandcore:plasma_glass", MeteorTier.UNCOMMON, null);
        put(map, "void_crystal", "nerolandcore:void_crystal_dust", MeteorTier.UNCOMMON, "nerolandcore:reached_orbit");
        put(map, "starsteel", "nerolandcore:starsteel_dust", MeteorTier.RARE, "nerolandcore:reached_orbit");
        return map;
    }

    private static void put(Map<Identifier, MeteorMaterialEntry> map, String id, String item,
            MeteorTier tier, @Nullable String minGate) {
        Identifier mid = Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, id);
        map.put(mid, new MeteorMaterialEntry(
                mid,
                Identifier.parse(item),
                tier,
                minGate == null ? null : Identifier.parse(minGate),
                null,
                null,
                true));
    }

    /**
     * A mutable field-by-field accumulator for one material id, folding annotation base + datapack
     * layers. {@code *Set} flags distinguish "specified" from "default".
     */
    private static final class Accumulator {
        @Nullable Identifier item;
        @Nullable MeteorTier tier;
        @Nullable Identifier minGate;
        @Nullable Identifier planet;
        @Nullable Integer weightOverride;
        boolean enabled = true;
        boolean itemSet;
        boolean tierSet;

        static Accumulator fromEntry(MeteorMaterialEntry e) {
            Accumulator a = new Accumulator();
            a.item = e.item();
            a.itemSet = true;
            a.tier = e.tier();
            a.tierSet = true;
            a.minGate = e.minGate();
            a.planet = e.planet();
            a.weightOverride = e.weightOverride();
            a.enabled = e.enabled();
            return a;
        }

        void overlay(JsonObject obj, Identifier materialId) {
            if (obj.has("item") && !obj.get("item").isJsonNull()) {
                Identifier parsed = parseId(obj.get("item").getAsString(), "item", materialId);
                if (parsed != null) {
                    this.item = parsed;
                    this.itemSet = true;
                }
            }
            if (obj.has("tier") && !obj.get("tier").isJsonNull()) {
                MeteorTier parsed = parseTier(obj.get("tier").getAsString(), materialId);
                if (parsed != null) {
                    this.tier = parsed;
                    this.tierSet = true;
                }
            }
            // Nullable fields: a present key (incl. explicit null) overrides; null clears to "none".
            if (obj.has("min_gate")) {
                this.minGate = obj.get("min_gate").isJsonNull()
                        ? null : parseId(obj.get("min_gate").getAsString(), "min_gate", materialId);
            }
            if (obj.has("planet")) {
                this.planet = obj.get("planet").isJsonNull()
                        ? null : parseId(obj.get("planet").getAsString(), "planet", materialId);
            }
            if (obj.has("weight_override")) {
                this.weightOverride = obj.get("weight_override").isJsonNull()
                        ? null : obj.get("weight_override").getAsInt();
            }
            if (obj.has("enabled") && !obj.get("enabled").isJsonNull()) {
                this.enabled = obj.get("enabled").getAsBoolean();
            }
        }

        @Nullable
        MeteorMaterialEntry build(Identifier materialId) {
            if (!tierSet || tier == null) {
                NerolandCoreCommon.LOGGER.warn(
                        "[Neroland Core] Meteor material {} has no tier; skipping.", materialId);
                return null;
            }
            Identifier outputItem = itemSet && item != null ? item : materialId; // default item = id
            return new MeteorMaterialEntry(
                    materialId, outputItem, tier, minGate, planet, weightOverride, enabled);
        }

        @Nullable
        private static Identifier parseId(String raw, String field, Identifier materialId) {
            try {
                return Identifier.parse(raw);
            } catch (RuntimeException e) {
                NerolandCoreCommon.LOGGER.warn(
                        "[Neroland Core] Meteor material {} has invalid {} '{}'; ignoring.",
                        materialId, field, raw);
                return null;
            }
        }

        @Nullable
        private static MeteorTier parseTier(String raw, Identifier materialId) {
            try {
                return MeteorTier.byName(raw);
            } catch (RuntimeException e) {
                NerolandCoreCommon.LOGGER.warn(
                        "[Neroland Core] Meteor material {} has invalid tier '{}'; ignoring.",
                        materialId, raw);
                return null;
            }
        }
    }
}
