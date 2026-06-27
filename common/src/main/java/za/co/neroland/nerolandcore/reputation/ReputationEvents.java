package za.co.neroland.nerolandcore.reputation;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import net.minecraft.resources.Identifier;

/**
 * Reputation-change notifications, fired by {@link ReputationApi} when a player's
 * standing with a faction changes. Subscribers react (e.g. unlock a faction perk,
 * trigger a faction event).
 */
public final class ReputationEvents {

    /** A reputation change for one player + faction. */
    public record ReputationChange(UUID player, Identifier faction, int oldValue, int newValue) {
    }

    private static final List<Consumer<ReputationChange>> LISTENERS = new CopyOnWriteArrayList<>();

    private ReputationEvents() {
    }

    public static void onChange(Consumer<ReputationChange> listener) {
        LISTENERS.add(listener);
    }

    static void fire(ReputationChange change) {
        for (Consumer<ReputationChange> listener : LISTENERS) {
            listener.accept(change);
        }
    }
}
