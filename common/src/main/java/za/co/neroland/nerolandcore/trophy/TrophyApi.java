package za.co.neroland.nerolandcore.trophy;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * The single entry point for trophies: read the data-driven definitions and award, query or revoke
 * trophies. Mutations route to the registered {@link TrophyProvider} (NeroTrophies) and fire
 * {@link TrophyEvents} only on change. Until a real provider registers, {@link NoOpTrophyProvider}
 * backs the API: awards return false and nothing is stored.
 *
 * <p>Any mod can award a trophy with only a Core dependency:
 * {@code TrophyApi.award(player, Identifier.fromNamespaceAndPath("mymod", "first_boss"))}. An id that is
 * not a loaded definition (its pack or mod is absent) returns false and does nothing.
 *
 * <p><b>Server thread only.</b> Calls from another thread return false (or nothing) without touching the
 * provider. <b>Privacy:</b> {@link #has} and {@link #earned} are for in-process logic; never surface one
 * player's result to another player, a link client or a log.
 *
 * @since 1.20.0
 */
public final class TrophyApi {

    private static volatile TrophyProvider provider = NoOpTrophyProvider.INSTANCE;

    private TrophyApi() {
    }

    /** Register the trophy store (NeroTrophies calls this at init). {@code null} restores the no-op default. */
    public static void setProvider(TrophyProvider newProvider) {
        TrophyProvider replacement = newProvider == null ? NoOpTrophyProvider.INSTANCE : newProvider;
        TrophyProvider previous = provider;
        if (!(previous instanceof NoOpTrophyProvider) && !(replacement instanceof NoOpTrophyProvider)
                && previous != replacement) {
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Trophy provider {} replaced by {}.",
                    previous.getClass().getName(), replacement.getClass().getName());
        }
        provider = replacement;
    }

    public static TrophyProvider provider() {
        return provider;
    }

    /** Whether a real provider (not the no-op default) is registered. */
    public static boolean hasRealProvider() {
        return !(provider instanceof NoOpTrophyProvider);
    }

    /** Every loaded trophy and collection for {@code server}. */
    public static TrophyDefinitions.Snapshot definitions(MinecraftServer server) {
        return TrophyDefinitions.forServer(server);
    }

    public static Optional<TrophyDefinition> definition(MinecraftServer server, Identifier id) {
        return definitions(server).trophy(id);
    }

    /** Award {@code trophy} to an online player, source {@link AwardSource#API}. */
    public static boolean award(ServerPlayer player, Identifier trophy) {
        MinecraftServer server = player.level().getServer();
        return server != null && award(server, player.getUUID(), trophy, AwardSource.API);
    }

    /** Award {@code trophy} to a player (online or not), source {@link AwardSource#API}. */
    public static boolean award(MinecraftServer server, UUID player, Identifier trophy) {
        return award(server, player, trophy, AwardSource.API);
    }

    /**
     * Award {@code trophy}. Fires {@link TrophyEvents.Awarded} only if the provider records it as new.
     *
     * @return true only if newly earned
     */
    public static boolean award(MinecraftServer server, UUID player, Identifier trophy, AwardSource source) {
        if (!onServerThread(server)) {
            return false;
        }
        return award(definitions(server), server, player, trophy, source);
    }

    /** Remove an earned trophy. Fires {@link TrophyEvents.Revoked} only if the provider removed it. */
    public static boolean revoke(MinecraftServer server, UUID player, Identifier trophy) {
        if (!onServerThread(server) || player == null || trophy == null) {
            return false;
        }
        if (!provider.revoke(server, player, trophy)) {
            return false;
        }
        TrophyEvents.fireRevoked(new TrophyEvents.Revoked(player, trophy));
        return true;
    }

    public static boolean has(MinecraftServer server, UUID player, Identifier trophy) {
        return onServerThread(server) && provider.has(server, player, trophy);
    }

    /** An immutable copy of {@code player}'s earned ids (trophies and completed collections). */
    public static Set<Identifier> earned(MinecraftServer server, UUID player) {
        return onServerThread(server) ? provider.earned(server, player) : Set.of();
    }

    /** The award path against an explicit snapshot (testable without a running server). */
    static boolean award(TrophyDefinitions.Snapshot definitions, MinecraftServer server, UUID player,
            Identifier trophy, AwardSource source) {
        if (player == null || trophy == null) {
            return false;
        }
        TrophyDefinition definition = definitions.trophies().get(trophy);
        if (definition == null) {
            return false;
        }
        AwardSource resolved = source == null ? AwardSource.API : source;
        if (!provider.award(server, player, trophy, resolved)) {
            return false;
        }
        TrophyEvents.fireAwarded(new TrophyEvents.Awarded(player, trophy, definition.tier(), resolved));
        return true;
    }

    private static boolean onServerThread(MinecraftServer server) {
        if (server.isSameThread()) {
            return true;
        }
        NerolandCoreCommon.LOGGER.debug("[Neroland Core] TrophyApi called off the server thread; ignored.");
        return false;
    }
}
