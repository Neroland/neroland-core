package za.co.neroland.nerolandcore.progression;

import java.io.BufferedReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/** Loads typed material-milestone definitions from datapacks. */
public final class MaterialMilestoneDefinitions {

    public static final Identifier MATERIAL_DISCOVERED =
            Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "material_discovered");
    private static final String DIRECTORY = "neroland_material_milestones";

    @Nullable
    private static MinecraftServer loadedFor;
    private static Map<Identifier, MaterialMilestone> definitions = builtins();

    private MaterialMilestoneDefinitions() {
    }

    public static synchronized Map<Identifier, MaterialMilestone> forServer(MinecraftServer server) {
        if (server != loadedFor) {
            definitions = load(server);
            loadedFor = server;
        }
        return definitions;
    }

    private static Map<Identifier, MaterialMilestone> load(MinecraftServer server) {
        Map<Identifier, MaterialMilestone> loaded = new LinkedHashMap<>();
        try {
            ResourceManager resources = server.getResourceManager();
            for (Map.Entry<Identifier, Resource> entry :
                    resources.listResources(DIRECTORY, file -> file.getPath().endsWith(".json")).entrySet()) {
                Identifier id = toDefinitionId(entry.getKey());
                if (id == null) {
                    continue;
                }
                try (BufferedReader reader = entry.getValue().openAsReader()) {
                    JsonElement json = JsonParser.parseReader(reader);
                    MaterialMilestone.DATA_CODEC.parse(JsonOps.INSTANCE, json)
                            .resultOrPartial(error -> NerolandCoreCommon.LOGGER.warn(
                                    "[Neroland Core] Bad material milestone {}: {}", id, error))
                            .ifPresent(data -> loaded.put(id, new MaterialMilestone(id, data)));
                } catch (Exception exception) {
                    NerolandCoreCommon.LOGGER.warn("[Neroland Core] Could not read material milestone {}", id,
                            exception);
                }
            }
        } catch (RuntimeException exception) {
            NerolandCoreCommon.LOGGER.warn(
                    "[Neroland Core] Material milestone load failed; using built-ins.", exception);
        }
        return loaded.isEmpty() ? builtins() : Map.copyOf(loaded);
    }

    private static Map<Identifier, MaterialMilestone> builtins() {
        return Map.of(MATERIAL_DISCOVERED, new MaterialMilestone(MATERIAL_DISCOVERED, GateScope.PLAYER,
                List.of(MaterialObservation.OWNER_MOD, MaterialObservation.PLANET_VISIT,
                        MaterialObservation.PLAYER_PICKUP, MaterialObservation.ADMIN),
                "Material Discovered"));
    }

    @Nullable
    private static Identifier toDefinitionId(Identifier file) {
        String path = file.getPath();
        if (!path.startsWith(DIRECTORY + "/") || !path.endsWith(".json")) {
            return null;
        }
        return Identifier.fromNamespaceAndPath(file.getNamespace(),
                path.substring(DIRECTORY.length() + 1, path.length() - ".json".length()));
    }
}
