package za.co.neroland.nerolandcore.progression;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.Identifier;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * The canonical Neroland progression arc gates, defined in code as the built-in
 * fallback. The same gates ship as datapack JSON under
 * {@code data/nerolandcore/neroland_gates/} so packs can override or extend them —
 * {@link GateDefinitions} prefers the datapack copy and falls back to these.
 *
 * <p>Downstream mods gate content behind these ids ("is {@link #REACHED_ORBIT}
 * open?"); NeroQuests drives them open as players complete objectives. Default
 * scope is {@link GateScope#PLAYER} (each player advances their own arc); a pack may
 * switch a gate to TEAM or SERVER.
 */
public final class CoreGates {

    public static final Identifier INDUSTRIAL_POWER = id("industrial_power");
    public static final Identifier REACHED_ORBIT = id("reached_orbit");
    public static final Identifier FIRST_COLONY = id("first_colony");
    public static final Identifier DEEP_SPACE = id("deep_space");

    private CoreGates() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, path);
    }

    /** The built-in default definitions (used when no datapack copy is present). */
    public static Map<Identifier, Gate> builtinDefaults() {
        Map<Identifier, Gate> gates = new LinkedHashMap<>();
        put(gates, new Gate(INDUSTRIAL_POWER, GateScope.PLAYER, List.of(), "Industrial Power"));
        put(gates, new Gate(REACHED_ORBIT, GateScope.PLAYER, List.of(INDUSTRIAL_POWER), "Reached Orbit"));
        put(gates, new Gate(FIRST_COLONY, GateScope.PLAYER, List.of(REACHED_ORBIT), "First Colony"));
        put(gates, new Gate(DEEP_SPACE, GateScope.PLAYER, List.of(FIRST_COLONY), "Deep Space"));
        return gates;
    }

    private static void put(Map<Identifier, Gate> gates, Gate gate) {
        gates.put(gate.id(), gate);
    }
}
