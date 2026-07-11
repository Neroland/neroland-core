package za.co.neroland.nerolandcore.progression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.network.MaterialMilestoneSyncPayload;
import za.co.neroland.nerolandcore.platform.Services;
import za.co.neroland.nerolandcore.progression.MaterialMilestoneEvents.Change;

/** Public server-authoritative API for scoped, typed per-material milestones. */
public final class MaterialMilestones {

    private MaterialMilestones() {
    }

    public static boolean isObserved(ServerPlayer player, Identifier milestone, Identifier material) {
        MinecraftServer server = player.level().getServer();
        if (server == null || material == null) {
            return false;
        }
        MaterialMilestoneState state = MaterialMilestoneState.get(server);
        return switch (scopeOf(server, milestone)) {
            case SERVER -> state.containsServer(milestone, material);
            case TEAM -> {
                String team = teamKey(player);
                yield team != null ? state.containsTeam(team, milestone, material)
                        : state.containsPlayer(player.getUUID(), milestone, material);
            }
            case PLAYER -> state.containsPlayer(player.getUUID(), milestone, material);
        };
    }

    /**
     * Records legitimate server-observed evidence. Unknown definitions and disallowed observation types
     * fail closed; callers must never invoke this from an unchecked client packet or automation path.
     */
    public static boolean observe(ServerPlayer player, Identifier milestone, Identifier material,
            MaterialObservation observation) {
        MinecraftServer server = player.level().getServer();
        if (server == null || material == null || observation == null) {
            return false;
        }
        MaterialMilestone definition = MaterialMilestoneDefinitions.forServer(server).get(milestone);
        if (definition == null || !definition.observations().contains(observation)) {
            return false;
        }
        return set(player, definition, material, observation, true);
    }

    public static boolean revoke(ServerPlayer player, Identifier milestone, Identifier material) {
        MinecraftServer server = player.level().getServer();
        if (server == null || material == null) {
            return false;
        }
        MaterialMilestone definition = MaterialMilestoneDefinitions.forServer(server).get(milestone);
        return definition != null && set(player, definition, material, MaterialObservation.ADMIN, false);
    }

    /** Direct UUID-keyed rows for a data-subject export; shared team/server state is intentionally absent. */
    public static Map<Identifier, Set<Identifier>> exportPlayer(MinecraftServer server, UUID player) {
        return MaterialMilestoneState.get(server).playerValues(player);
    }

    public static void syncTo(ServerPlayer player) {
        Services.NETWORK.sendToPlayer(player, new MaterialMilestoneSyncPayload(resolved(player)));
    }

    public static void syncAll(MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(MaterialMilestones::syncTo);
    }

    private static boolean set(ServerPlayer player, MaterialMilestone definition, Identifier material,
            MaterialObservation observation, boolean present) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return false;
        }
        MaterialMilestoneState state = MaterialMilestoneState.get(server);
        boolean changed;
        String target;
        GateScope actualScope = definition.scope();
        switch (definition.scope()) {
            case SERVER -> {
                changed = state.setServer(definition.id(), material, present);
                target = "";
            }
            case TEAM -> {
                String team = teamKey(player);
                if (team == null) {
                    changed = state.setPlayer(player.getUUID(), definition.id(), material, present);
                    target = player.getUUID().toString();
                    actualScope = GateScope.PLAYER;
                } else {
                    changed = state.setTeam(team, definition.id(), material, present);
                    target = team;
                }
            }
            default -> {
                changed = state.setPlayer(player.getUUID(), definition.id(), material, present);
                target = player.getUUID().toString();
            }
        }
        if (changed) {
            MaterialMilestoneEvents.fire(new Change(actualScope, target, definition.id(), material,
                    observation, present));
            syncAll(server);
        }
        return changed;
    }

    private static List<String> resolved(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        MaterialMilestoneState.get(server).resolved(player.getUUID(), teamKey(player)).forEach(
                (milestone, materials) -> materials.forEach(material ->
                        result.add(ClientMaterialMilestones.key(milestone, material))));
        return result;
    }

    private static GateScope scopeOf(MinecraftServer server, Identifier milestone) {
        MaterialMilestone definition = MaterialMilestoneDefinitions.forServer(server).get(milestone);
        return definition == null ? GateScope.PLAYER : definition.scope();
    }

    @Nullable
    private static String teamKey(ServerPlayer player) {
        Team team = player.getTeam();
        return team == null ? null : team.getName();
    }
}
