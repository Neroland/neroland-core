package za.co.neroland.nerolandcore.protection;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * The land-protection SPI (Core 1.21.0). A claims mod (NeroColonies, NeroFactions, NeroSecurity, or a
 * compatibility module for a third-party claims mod) implements this once and registers it with
 * {@link ClaimChecks#register}; every other Nero mod asks {@link ClaimChecks} before changing the world.
 *
 * <p>Both methods are called on the server thread and must be cheap: a hot path such as a radiation
 * tick may call {@link #isProtected} many times a second. They must not load chunks, log the player or
 * keep any record of the query.</p>
 */
public interface ClaimCheck {

    /**
     * Whether {@code pos} is protected against world effects that have no acting player (radiation,
     * explosions, spreading growth, automated breaking). Return {@code false} for land this check
     * knows nothing about.
     */
    boolean isProtected(ServerLevel level, BlockPos pos);

    /**
     * Whether {@code player} may interact at {@code pos} (break, place, open a menu, use an item).
     * Return {@code true} for land this check knows nothing about.
     */
    boolean mayInteract(ServerPlayer player, ServerLevel level, BlockPos pos);
}
