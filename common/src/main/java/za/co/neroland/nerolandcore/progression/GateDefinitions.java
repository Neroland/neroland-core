package za.co.neroland.nerolandcore.progression;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.LinkedHashMap;
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

/**
 * The active set of gate definitions, loaded from datapacks. One JSON per gate
 * under {@code data/<namespace>/neroland_gates/<path>.json} (the id is the file's
 * namespace + path); Core's own canonical gates ship there too, so a pack overrides
 * a gate simply by shipping the same id. Falls back to
 * {@link CoreGates#builtinDefaults()} if the resource manager yields nothing.
 *
 * <p>Loaded lazily per running server and cached; re-reading after a
 * {@code /reload} can be added when a reload-listener seam lands.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class GateDefinitions {

    private static final String DIRECTORY = "neroland_gates";

    @Nullable
    private static MinecraftServer loadedFor;
    private static Map<Identifier, Gate> gates = CoreGates.builtinDefaults();

    private GateDefinitions() {
    }

    /** The definitions for this server (loads + caches on first use, falling back to built-ins). */
    public static synchronized Map<Identifier, Gate> forServer(MinecraftServer server) {
        if (server != loadedFor) {
            gates = load(server);
            loadedFor = server;
        }
        return gates;
    }

    /** The currently loaded definitions (built-ins until a server loads its datapacks). */
    public static Map<Identifier, Gate> current() {
        return gates;
    }

    @Nullable
    public static Gate get(Identifier id) {
        return gates.get(id);
    }

    public static Collection<Gate> all() {
        return gates.values();
    }

    private static Map<Identifier, Gate> load(MinecraftServer server) {
        Map<Identifier, Gate> loaded = new LinkedHashMap<>();
        try {
            ResourceManager resources = server.getResourceManager();
            Map<Identifier, Resource> files =
                    resources.listResources(DIRECTORY, file -> file.getPath().endsWith(".json"));
            for (Map.Entry<Identifier, Resource> entry : files.entrySet()) {
                Identifier gateId = toGateId(entry.getKey());
                if (gateId == null) {
                    continue;
                }
                try (BufferedReader reader = entry.getValue().openAsReader()) {
                    JsonElement json = JsonParser.parseReader(reader);
                    Gate.DATA_CODEC.parse(JsonOps.INSTANCE, json)
                            .resultOrPartial(error -> NerolandCoreCommon.LOGGER.warn(
                                    "[Neroland Core] Bad gate definition {}: {}", gateId, error))
                            .ifPresent(data -> loaded.put(gateId, new Gate(gateId, data)));
                } catch (Exception e) {
                    NerolandCoreCommon.LOGGER.warn("[Neroland Core] Could not read gate {}", gateId, e);
                }
            }
        } catch (RuntimeException e) {
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Gate definition load failed; using built-ins.", e);
        }
        return loaded.isEmpty() ? CoreGates.builtinDefaults() : loaded;
    }

    /** {@code <ns>:neroland_gates/foo/bar.json} -> {@code <ns>:foo/bar}. */
    @Nullable
    private static Identifier toGateId(Identifier file) {
        String path = file.getPath();
        if (!path.startsWith(DIRECTORY + "/") || !path.endsWith(".json")) {
            return null;
        }
        String trimmed = path.substring(DIRECTORY.length() + 1, path.length() - ".json".length());
        return Identifier.fromNamespaceAndPath(file.getNamespace(), trimmed);
    }
}
