package za.co.neroland.nerolandcore.protection;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * The query side of the land-protection SPI (Core 1.21.0). Combines every registered {@link ClaimCheck}:
 * a position is protected if <em>any</em> check protects it, and a player may interact only if
 * <em>every</em> check allows it. With no checks registered, nothing is protected and everything is
 * allowed, so mods that consult this behave exactly as before on servers without a claims mod.
 *
 * <p>Fails closed: a check that throws counts as "protected" / "denied" for that query and is logged
 * once per check, with its class name only (never the player or the position).</p>
 *
 * <p>Vanilla spawn protection is not included; a mod that wants it keeps its own check. Core stores
 * nothing for these queries.</p>
 */
public final class ClaimChecks {

    private static final List<ClaimCheck> CHECKS = new CopyOnWriteArrayList<>();
    private static final List<ClaimCheck> REPORTED = new CopyOnWriteArrayList<>();

    private ClaimChecks() {
    }

    /** Register a check. Call once at init; registering the same instance twice is a no-op. */
    public static void register(ClaimCheck check) {
        if (check != null && !CHECKS.contains(check)) {
            CHECKS.add(check);
        }
    }

    /** Whether any claims check is registered (a claims mod is present). */
    public static boolean active() {
        return !CHECKS.isEmpty();
    }

    /** Whether world effects with no acting player must leave {@code pos} alone. */
    public static boolean isProtected(ServerLevel level, BlockPos pos) {
        for (ClaimCheck check : CHECKS) {
            try {
                if (check.isProtected(level, pos)) {
                    return true;
                }
            } catch (RuntimeException failure) {
                report(check, failure);
                return true;
            }
        }
        return false;
    }

    /** Whether {@code player} may act at {@code pos}. */
    public static boolean mayInteract(ServerPlayer player, ServerLevel level, BlockPos pos) {
        for (ClaimCheck check : CHECKS) {
            try {
                if (!check.mayInteract(player, level, pos)) {
                    return false;
                }
            } catch (RuntimeException failure) {
                report(check, failure);
                return false;
            }
        }
        return true;
    }

    private static void report(ClaimCheck check, RuntimeException failure) {
        if (!REPORTED.contains(check)) {
            REPORTED.add(check);
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Claim check {} failed ({}); treating it as protected",
                    check.getClass().getName(), failure.getClass().getName());
        }
    }

    /** Test teardown only. */
    @org.jetbrains.annotations.ApiStatus.Internal
    public static void clearForTests() {
        CHECKS.clear();
        REPORTED.clear();
    }
}
