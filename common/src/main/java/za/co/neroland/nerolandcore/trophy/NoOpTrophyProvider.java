package za.co.neroland.nerolandcore.trophy;

import java.util.Set;
import java.util.UUID;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

/**
 * Core's default {@link TrophyProvider}: it has no fields and stores nothing. Without a trophy mod,
 * {@link TrophyApi#award} is a cheap no-op and no player has any trophy.
 *
 * @since 1.20.0
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class NoOpTrophyProvider implements TrophyProvider {

    public static final NoOpTrophyProvider INSTANCE = new NoOpTrophyProvider();

    private NoOpTrophyProvider() {
    }

    @Override
    public boolean award(MinecraftServer server, UUID player, Identifier trophy, AwardSource source) {
        return false;
    }

    @Override
    public boolean has(MinecraftServer server, UUID player, Identifier trophy) {
        return false;
    }

    @Override
    public Set<Identifier> earned(MinecraftServer server, UUID player) {
        return Set.of();
    }

    @Override
    public boolean revoke(MinecraftServer server, UUID player, Identifier trophy) {
        return false;
    }
}
