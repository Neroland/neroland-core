package za.co.neroland.nerolandcore.worldgen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

/**
 * Guards the MC 26.3 heightmap change. From 26.3 the MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES,
 * OCEAN_FLOOR and OCEAN_FLOOR_WG heightmaps only count blocks in
 * {@code #minecraft:blocks_motion_in_heightmap} (which resolves to
 * {@code #minecraft:blocks_motion_no_leaves}) instead of asking {@code BlockState.blocksMotion()}.
 * A collidable modded block missing from that tag is invisible to the heightmap: rain and snow fall
 * through roofs, mobs spawn at the wrong height and heightmap-placed features are skipped.
 *
 * <p>Every Core block must therefore be listed in the 26.3-only overlay
 * {@code common/src/main/resources-26.3/data/minecraft/tags/block/blocks_motion_no_leaves.json},
 * or be named in {@link #EXCLUDED} with the reason it has no collision. The tag does not exist before
 * 26.3, so the entry must never appear in the shared resources.
 */
class HeightmapTagCoverageTest {

    private static final String NAMESPACE = "nerolandcore";
    private static final String TAG = "data/minecraft/tags/block/blocks_motion_no_leaves.json";

    /**
     * Core blocks deliberately left out of the tag (no collision shape, like vanilla's torches,
     * flowers or carpets). Key = block path, value = why. Currently empty: every Core block is solid.
     */
    private static final Map<String, String> EXCLUDED = Map.of();

    private static final Pattern REGISTRATION =
            Pattern.compile("(?:BLOCKS\\.register|\\bblock)\\(\\s*\"([a-z0-9_/.]+)\"");

    private static Path repoRoot() {
        Path dir = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        for (Path p = dir; p != null; p = p.getParent()) {
            if (Files.isDirectory(p.resolve("common/src/main/resources-26.3"))
                    && Files.isDirectory(p.resolve("common/src/main/java"))) {
                return p;
            }
        }
        fail("could not locate the repository root from " + dir);
        return null;
    }

    /** Core block ids, from both the blockstate files and the ModBlocks registrations. */
    private static Set<String> coreBlocks(Path root) throws IOException {
        Set<String> ids = new TreeSet<>();
        Path states = root.resolve("common/src/main/resources/assets/" + NAMESPACE + "/blockstates");
        try (Stream<Path> files = Files.list(states)) {
            files.map(f -> f.getFileName().toString())
                    .filter(n -> n.endsWith(".json"))
                    .forEach(n -> ids.add(n.substring(0, n.length() - ".json".length())));
        }
        Path modBlocks = root.resolve("common/src/main/java/za/co/neroland/" + NAMESPACE + "/registry/ModBlocks.java");
        Matcher m = REGISTRATION.matcher(Files.readString(modBlocks, StandardCharsets.UTF_8));
        while (m.find()) {
            ids.add(m.group(1));
        }
        assertFalse(ids.isEmpty(), "found no Core blocks to check");
        return ids;
    }

    private static JsonObject tag(Path root) throws IOException {
        Path file = root.resolve("common/src/main/resources-26.3/" + TAG);
        assertTrue(Files.isRegularFile(file), "missing 26.3 overlay " + file);
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(r).getAsJsonObject();
        }
    }

    @Test
    void everyCoreBlockIsTaggedOrExcluded() throws IOException {
        Path root = repoRoot();
        JsonObject tag = tag(root);
        assertNotNull(tag.get("replace"), "tag must state replace:false");
        assertFalse(tag.get("replace").getAsBoolean(), "tag must not replace the vanilla list");

        Set<String> tagged = new LinkedHashSet<>();
        for (JsonElement e : tag.getAsJsonArray("values")) {
            assertTrue(tagged.add(e.getAsString()), "duplicate tag entry " + e.getAsString());
        }

        Set<String> blocks = coreBlocks(root);
        Set<String> missing = new TreeSet<>();
        for (String id : blocks) {
            String full = NAMESPACE + ":" + id;
            boolean excluded = EXCLUDED.containsKey(id);
            if (excluded && tagged.contains(full)) {
                fail(full + " is both tagged and excluded");
            }
            if (!excluded && !tagged.contains(full)) {
                missing.add(full);
            }
        }
        assertTrue(missing.isEmpty(), "Core blocks missing from the 26.3 heightmap tag " + TAG
                + " (add them, or exclude them with a reason): " + missing);

        for (String entry : tagged) {
            if (entry.startsWith(NAMESPACE + ":")) {
                assertTrue(blocks.contains(entry.substring(NAMESPACE.length() + 1)), "stale tag entry " + entry);
            }
        }
        for (String id : EXCLUDED.keySet()) {
            assertTrue(blocks.contains(id), "stale exclusion " + id);
        }
    }

    @Test
    void tagIsNotShippedToPre263Versions() {
        // blocks_motion_no_leaves only exists from 26.3; on 26.1.2/26.2 the file would define a new,
        // unused tag. Keep it in the resources-26.3 overlay only.
        Path shared = repoRoot().resolve("common/src/main/resources/" + TAG);
        assertFalse(Files.exists(shared), "heightmap tag must live in resources-26.3 only: " + shared);
    }
}
