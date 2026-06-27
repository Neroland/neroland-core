package za.co.neroland.nerolandcore.reputation;

import java.util.UUID;

import net.minecraft.resources.Identifier;

import za.co.neroland.nerolandcore.reputation.ReputationEvents.ReputationChange;

/**
 * The single entry point any mod uses to read or adjust player↔faction reputation.
 * Routes to the registered {@link ReputationProvider} (NeroFactions' store) and
 * fires {@link ReputationEvents} on change. Until a real provider registers, an
 * {@link InMemoryReputationProvider} backs the API so callers work without
 * NeroFactions.
 *
 * <p>Core itself stores nothing: this facade only delegates.
 */
public final class ReputationApi {

    private static volatile ReputationProvider provider = new InMemoryReputationProvider();

    private ReputationApi() {
    }

    /** Register the backing store (NeroFactions calls this at init). Replaces the in-memory default. */
    public static void setProvider(ReputationProvider newProvider) {
        provider = newProvider;
    }

    public static ReputationProvider provider() {
        return provider;
    }

    /** Whether a real provider (not the in-memory default) is registered. */
    public static boolean hasRealProvider() {
        return !(provider instanceof InMemoryReputationProvider);
    }

    public static int getReputation(UUID player, Identifier faction) {
        return provider.getReputation(player, faction);
    }

    public static void setReputation(UUID player, Identifier faction, int value) {
        int before = provider.getReputation(player, faction);
        provider.setReputation(player, faction, value);
        if (before != value) {
            ReputationEvents.fire(new ReputationChange(player, faction, before, value));
        }
    }

    public static int adjust(UUID player, Identifier faction, int delta) {
        int before = provider.getReputation(player, faction);
        int updated = provider.adjust(player, faction, delta);
        if (before != updated) {
            ReputationEvents.fire(new ReputationChange(player, faction, before, updated));
        }
        return updated;
    }
}
