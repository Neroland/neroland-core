package za.co.neroland.nerolandcore.trophy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

/** An in-memory datapack for the loader tests: no resource manager, no game bootstrap. */
final class TrophyTestData {

    final Map<String, Map<Identifier, Resource>> roots = new HashMap<>();

    TrophyTestData trophy(String id, String json) {
        return put(TrophyDefinitions.TROPHY_DIRECTORY, id, json.getBytes(StandardCharsets.UTF_8));
    }

    TrophyTestData collection(String id, String json) {
        return put(TrophyDefinitions.COLLECTION_DIRECTORY, id, json.getBytes(StandardCharsets.UTF_8));
    }

    TrophyTestData put(String root, String id, byte[] bytes) {
        Identifier parsed = Identifier.parse(id);
        Identifier file = Identifier.fromNamespaceAndPath(parsed.getNamespace(),
                root + "/" + parsed.getPath() + ".json");
        roots.computeIfAbsent(root, ignored -> new HashMap<>())
                .put(file, new Resource(null, () -> new ByteArrayInputStream(bytes)));
        return this;
    }

    TrophyDefinitions.Lister lister() {
        return directory -> roots.getOrDefault(directory, Map.of());
    }

    TrophyDefinitions.Result load(Predicate<String> modLoaded, Predicate<Identifier> itemExists) {
        return TrophyDefinitions.loadFrom(lister(), modLoaded, itemExists, 1L);
    }

    /** Loads with every mod present and every icon registered except the listed ones. */
    TrophyDefinitions.Result load(Set<String> absentMods, Set<String> missingItems) {
        return load(mod -> !absentMods.contains(mod), item -> !missingItems.contains(item.toString()));
    }

    static String trophyJson(String icon, String extra) {
        return "{\"icon\":\"" + icon + "\",\"tier\":\"bronze\","
                + "\"trigger\":{\"type\":\"advancement\",\"advancement\":\"minecraft:story/root\"}"
                + (extra.isEmpty() ? "" : "," + extra) + "}";
    }

    static Identifier id(String id) {
        return Identifier.parse(id);
    }
}
