package za.co.neroland.nerolandcore.progression;

import java.util.ArrayList;
import java.util.List;

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

    private static boolean setOpen(ServerPlayer player, Identifier gate, boolean open) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return false;
        }
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
                String team = teamKey(player);
                if (team != null) {
                    changed = state.setTeam(team, gate, open);
                    target = team;
                } else {
                    changed = state.setPlayer(player.getUUID(), gate, open);
                    target = player.getUUID().toString();
                }
            }
            default -> {
                changed = state.setPlayer(player.getUUID(), gate, open);
                target = player.getUUID().toString();
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

    @Nullable
    private static String teamKey(ServerPlayer player) {
        Team team = player.getTeam();
        return team != null ? team.getName() : null;
    }
}
