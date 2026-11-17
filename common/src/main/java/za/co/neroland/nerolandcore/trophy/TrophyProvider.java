package za.co.neroland.nerolandcore.trophy;

import java.util.Set;
import java.util.UUID;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

/**
 * The trophy store behind {@link TrophyApi}. Exactly one mod (NeroTrophies) registers a real provider
 * with {@link TrophyApi#setProvider}; it evaluates triggers, owns the per-player records and handles
 * their erasure through Core's {@code data.PlayerDataErasure} with its own eraser. Core's default,
 * {@link NoOpTrophyProvider}, stores nothing.
 *
 * <p>All methods are called on the server thread.
 *
 * @since 1.20.0
 */
public interface TrophyProvider {

    /**
     * Record {@code trophy} as earned by {@code player}.
     *
     * @return true only if it was newly earned (false if already held or not recorded)
     */
    boolean award(MinecraftServer server, UUID player, Identifier trophy, AwardSource source);

    boolean has(MinecraftServer server, UUID player, Identifier trophy);

    /** An immutable copy of the player's earned ids. */
    Set<Identifier> earned(MinecraftServer server, UUID player);

    /**
     * Remove an earned trophy (an operator correction).
     *
     * @return true only if it was held and is now removed
     */
    default boolean revoke(MinecraftServer server, UUID player, Identifier trophy) {
        return false;
    }
}
