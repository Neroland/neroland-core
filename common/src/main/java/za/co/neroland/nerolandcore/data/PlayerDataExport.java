package za.co.neroland.nerolandcore.data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.server.MinecraftServer;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * The shared per-player data <b>export</b> hook (POPIA/GDPR right of access and portability), beside
 * {@link PlayerDataErasure} (Core 1.21.0). Every mod that stores player data registers a
 * {@link PlayerDataExporter} under its mod id, and {@code /neroland data exportme} (or the operator form
 * {@code /neroland data export <uuid>}) returns one JSON document with a section per mod.
 *
 * <p>Document shape ({@value #SCHEMA}):</p>
 * <pre>{@code
 * {
 *   "format": "nerolandcore-data-export", "schema": 1,
 *   "generatedAt": "2026-11-22T14:00:00Z",
 *   "erased": false,                  // whether an erasure tombstone exists for this player (yes/no only)
 *   "mods": { "<modid>": { ... } },   // only mods that store something for the player
 *   "nothingStored": ["<modid>", ...],
 *   "failed": ["<modid>", ...]        // exporters that threw; the request should be retried
 * }
 * }</pre>
 *
 * <p>Core keeps no copy: the document is built on request, handed to the requester and forgotten. Nothing
 * about the player is logged; a failing exporter is logged by mod id and exception class only.</p>
 */
public final class PlayerDataExport {

    /** Current document schema. */
    public static final int SCHEMA = 1;

    /** Largest document the in-game command hands over (characters). */
    public static final int MAX_CHARS = 262_144;

    private static final Map<String, PlayerDataExporter> EXPORTERS = new ConcurrentSkipListMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private PlayerDataExport() {
    }

    /**
     * Register {@code modId}'s exporter. Call once at init. A second registration for the same id
     * replaces the first.
     */
    public static void register(String modId, PlayerDataExporter exporter) {
        if (modId == null || modId.isBlank() || exporter == null) {
            throw new IllegalArgumentException("modId and exporter are required");
        }
        EXPORTERS.put(modId, exporter);
    }

    /** Registered mod ids, sorted (diagnostics only). */
    public static java.util.Set<String> registeredIds() {
        return java.util.Collections.unmodifiableSet(EXPORTERS.keySet());
    }

    /** Build the export document for {@code player}. Server thread only. */
    public static JsonObject export(MinecraftServer server, UUID player) {
        return assemble(server, player, Instant.now(), uuid -> ErasureTombstones.get(server).isOwnerErased(uuid));
    }

    /** The export document as pretty-printed JSON text. */
    public static String exportJson(MinecraftServer server, UUID player) {
        return GSON.toJson(export(server, player));
    }

    /** Assembly with the clock and tombstone lookup injected; package-private for plain-JVM tests. */
    static JsonObject assemble(MinecraftServer server, UUID player, Instant now, Predicate<UUID> erased) {
        JsonObject doc = new JsonObject();
        doc.addProperty("format", "nerolandcore-data-export");
        doc.addProperty("schema", SCHEMA);
        doc.addProperty("generatedAt", now.toString());
        boolean isErased;
        try {
            isErased = erased.test(player);
        } catch (RuntimeException failure) {
            isErased = false;
        }
        doc.addProperty("erased", isErased);
        JsonObject mods = new JsonObject();
        JsonArray nothing = new JsonArray();
        JsonArray failed = new JsonArray();
        for (Map.Entry<String, PlayerDataExporter> entry : EXPORTERS.entrySet()) {
            try {
                JsonElement section = entry.getValue().export(server, player);
                if (section == null || section.isJsonNull()) {
                    nothing.add(entry.getKey());
                } else {
                    mods.add(entry.getKey(), section);
                }
            } catch (RuntimeException failure) {
                failed.add(entry.getKey());
                NerolandCoreCommon.LOGGER.warn("[Neroland Core] Data exporter for {} failed ({})",
                        entry.getKey(), failure.getClass().getName());
            }
        }
        doc.add("mods", mods);
        doc.add("nothingStored", nothing);
        doc.add("failed", failed);
        return doc;
    }

    /** Pretty-print a document (tests and the command). */
    static String toJson(JsonObject doc) {
        return GSON.toJson(doc);
    }

    /** Test teardown only. */
    @org.jetbrains.annotations.ApiStatus.Internal
    public static void clearForTests() {
        EXPORTERS.clear();
    }
}
