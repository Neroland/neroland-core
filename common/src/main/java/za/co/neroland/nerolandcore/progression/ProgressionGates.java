package za.co.neroland.nerolandcore.progression;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.network.GateSyncPayload;
import za.co.neroland.nerolandcore.platform.Services;
import za.co.neroland.nerolandcore.progression.GateEvents.GateChange;

/**
 * The public progression-gate API. Reads and mutates {@link ProgressionState},
 * resolving each gate to its declared {@link GateScope} (server / team / player).
 * Server-authoritative: every method here runs on the server; clients only read the
 * synced mirror via {@link ClientGates}.
 *
 * <p>NeroQuests drives gates open ({@link #tryOpen}); Nerospace, Nerotech and the
 * rest read them ({@link #isOpen}); mods react through {@link GateEvents}.
 *
 * <p><b>Offline players (Core 1.15.0).</b> {@link #isOpen(MinecraftServer, UUID, Identifier)},
 * {@link #open(MinecraftServer, UUID, Identifier)} and {@link #close(MinecraftServer, UUID, Identifier)}
 * take a UUID instead of a {@link ServerPlayer}, so a gate can be read or opened while its player is
 * offline (an economy payout, a colony export, a NeroLink action). When the player is online they
 * simply delegate to the {@code ServerPlayer} overloads. Offline, scope resolves the same way:
 * {@code SERVER} gates use the server set, {@code PLAYER} gates the player's row, and {@code TEAM}
 * gates the player's scoreboard team — looked up by the player's last-known profile name in the
 * server's name cache and the (offline-persistent) scoreboard. A player with no team, or whose name
 * the cache no longer holds, falls back to their own row, exactly as a teamless online player does.
 * Offline changes fire the same {@link GateEvents.GateChange} (it carries no {@code ServerPlayer})
 * and re-sync every online player.
 */
public final class ProgressionGates {

    private ProgressionGates() {
    }

    // --- queries ------------------------------------------------------------

    /** Whether {@code gate} is open for {@code player}, resolved by the gate's scope. */
    public static boolean isOpen(ServerPlayer player, Identifier gate) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return false;
        }
        ProgressionState state = ProgressionState.get(server);
        return switch (scopeOf(server, gate)) {
            case SERVER -> state.isServerOpen(gate);
            case TEAM -> {
                String team = teamKey(player);
                yield team != null ? state.isTeamOpen(team, gate) : state.isPlayerOpen(player.getUUID(), gate);
            }
            case PLAYER -> state.isPlayerOpen(player.getUUID(), gate);
        };
    }

    /**
     * Whether {@code gate} is open for the player with {@code playerId}, online or offline, resolved by
     * the gate's scope (see the class notes for offline {@code TEAM} resolution). Added in Core 1.15.0.
     */
    public static boolean isOpen(MinecraftServer server, UUID playerId, Identifier gate) {
        if (server == null || playerId == null || gate == null) {
            return false;
        }
        ServerPlayer online = server.getPlayerList().getPlayer(playerId);
        if (online != null) {
            return isOpen(online, gate);
        }
        ProgressionState state = ProgressionState.get(server);
        return switch (scopeOf(server, gate)) {
            case SERVER -> state.isServerOpen(gate);
            case TEAM -> {
                String team = offlineTeamKey(server, playerId);
                yield team != null ? state.isTeamOpen(team, gate) : state.isPlayerOpen(playerId, gate);
            }
            case PLAYER -> state.isPlayerOpen(playerId, gate);
        };
    }

    /** Whether a server-scope {@code gate} is open. */
    public static boolean isServerOpen(MinecraftServer server, Identifier gate) {
        return ProgressionState.get(server).isServerOpen(gate);
    }

    /** Whether every prerequisite of {@code gate} is open for {@code player}. */
    public static boolean requirementsMet(ServerPlayer player, Identifier gate) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return false;
        }
        Gate def = GateDefinitions.forServer(server).get(gate);
        if (def == null) {
            return true;
        }
        for (Identifier required : def.requires()) {
            if (!isOpen(player, required)) {
                return false;
            }
        }
        return true;
    }

    // --- mutations ----------------------------------------------------------

    /** Open {@code gate} for {@code player} (in the gate's scope). Fires events + re-syncs. */
    public static boolean open(ServerPlayer player, Identifier gate) {
        return setOpen(player, gate, true);
    }

    /** Close {@code gate} for {@code player}. */
    public static boolean close(ServerPlayer player, Identifier gate) {
        return setOpen(player, gate, false);
    }

    /**
     * Open {@code gate} for the player with {@code playerId}, online or offline (in the gate's scope).
     * Fires {@link GateEvents} + re-syncs online players. Added in Core 1.15.0.
     *
     * @return {@code true} if the gate state changed
     */
    public static boolean open(MinecraftServer server, UUID playerId, Identifier gate) {
        return setOpen(server, playerId, gate, true);
    }

    /**
     * Close {@code gate} for the player with {@code playerId}, online or offline. Added in Core 1.15.0.
     *
     * @return {@code true} if the gate state changed
     */
    public static boolean close(MinecraftServer server, UUID playerId, Identifier gate) {
        return setOpen(server, playerId, gate, false);
    }

    /** Open {@code gate} only if its prerequisites are already met. */
    public static boolean tryOpen(ServerPlayer player, Identifier gate) {
        return requirementsMet(player, gate) && open(player, gate);
    }

    /** Open or close a server-scope gate without a player context. */
    public static boolean setServerGate(MinecraftServer server, Identifier gate, boolean open) {
        boolean changed = ProgressionState.get(server).setServer(gate, open);
        if (changed) {
            GateEvents.fire(new GateChange(GateScope.SERVER, "", gate, open));
            syncAll(server);
        }
        return changed;
    }

    private static boolean setOpen(MinecraftServer server, UUID playerId, Identifier gate, boolean open) {
        if (server == null || playerId == null || gate == null) {
            return false;
        }
        ServerPlayer online = server.getPlayerList().getPlayer(playerId);
        if (online != null) {
            return setOpen(online, gate, open);
        }
        return apply(server, playerId, scopeOf(server, gate) == GateScope.TEAM ? offlineTeamKey(server, playerId) : null,
                gate, open);
    }

    private static boolean setOpen(ServerPlayer player, Identifier gate, boolean open) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return false;
        }
        return apply(server, player.getUUID(), teamKey(player), gate, open);
    }

    /** Shared mutation for the online and offline paths; {@code team} is the player's team or null. */
    private static boolean apply(MinecraftServer server, UUID playerId, @Nullable String team, Identifier gate,
            boolean open) {
        ProgressionState state = ProgressionState.get(server);
        GateScope scope = scopeOf(server, gate);
        boolean changed;
        String target;
        switch (scope) {
            case SERVER -> {
                changed = state.setServer(gate, open);
                target = "";
            }
            case TEAM -> {
                if (team != null) {
                    changed = state.setTeam(team, gate, open);
                    target = team;
                } else {
                    changed = state.setPlayer(playerId, gate, open);
                    target = playerId.toString();
                }
            }
            default -> {
                changed = state.setPlayer(playerId, gate, open);
                target = playerId.toString();
            }
        }
        if (changed) {
            GateEvents.fire(new GateChange(scope, target, gate, open));
            syncAll(server);
        }
        return changed;
    }

    // --- sync ---------------------------------------------------------------

    /** The gate ids currently open for {@code player}, across all scopes (for client sync). */
    public static List<String> resolvedOpenGates(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return List.of();
        }
        List<String> open = new ArrayList<>();
        for (Gate def : GateDefinitions.forServer(server).values()) {
            if (isOpen(player, def.id())) {
                open.add(def.id().toString());
            }
        }
        return open;
    }

    /** Push the receiving player's resolved gate set to their client (call on join + change). */
    public static void syncTo(ServerPlayer player) {
        Services.NETWORK.sendToPlayer(player, new GateSyncPayload(resolvedOpenGates(player)));
    }

    /** Re-sync every online player (a gate change can affect server/team scopes broadly). */
    public static void syncAll(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(ProgressionGates::syncTo);
    }

    // --- helpers ------------------------------------------------------------

    private static GateScope scopeOf(MinecraftServer server, Identifier gate) {
        Gate def = GateDefinitions.forServer(server).get(gate);
        return def != null ? def.scope() : GateScope.PLAYER;
    }

    /**
     * An offline player's scoreboard team: their last-known profile name from the server's name cache,
     * looked up in the scoreboard (which persists team membership by name while players are offline).
     * The name is used transiently for the lookup only — never stored or logged. {@code null} if the
     * name is unknown, the player has no team, or the lookup fails.
     */
    @Nullable
    private static String offlineTeamKey(MinecraftServer server, UUID playerId) {
        try {
            String name = server.services().nameToIdCache().get(playerId).map(profile -> profile.name()).orElse(null);
            if (name == null) {
                return null;
            }
            Team team = server.getScoreboard().getPlayersTeam(name);
            return team != null ? team.getName() : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Nullable
    private static String teamKey(ServerPlayer player) {
        Team team = player.getTeam();
        return team != null ? team.getName() : null;
    }
}
