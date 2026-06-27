package za.co.neroland.nerolandcore.reputation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.Identifier;

/**
 * The default reference {@link ReputationProvider}: non-persistent, in-memory
 * standings. Active until NeroFactions registers a real store, so the API works (and
 * is testable) without NeroFactions installed. Holds nothing across a restart.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class InMemoryReputationProvider implements ReputationProvider {

    private final Map<UUID, Map<Identifier, Integer>> standings = new ConcurrentHashMap<>();

    @Override
    public int getReputation(UUID player, Identifier faction) {
        return standings.getOrDefault(player, Map.of()).getOrDefault(faction, 0);
    }

    @Override
    public void setReputation(UUID player, Identifier faction, int value) {
        standings.computeIfAbsent(player, ignored -> new ConcurrentHashMap<>()).put(faction, value);
    }

    @Override
    public void forgetPlayer(UUID player) {
        standings.remove(player);
    }
}
