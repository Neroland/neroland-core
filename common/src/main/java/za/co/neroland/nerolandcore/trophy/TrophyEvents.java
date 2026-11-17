package za.co.neroland.nerolandcore.trophy;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import net.minecraft.resources.Identifier;

import za.co.neroland.nerolandcore.event.ListenerGuard;

/**
 * Trophy notifications, on the server thread. {@link Awarded} and {@link Revoked} are fired by
 * {@link TrophyApi}; {@link CollectionCompleted} and {@link RecordErased} are fired by the registered
 * trophy provider (they are no-ops while Core's no-op provider is bound).
 *
 * <p><b>Privacy.</b> Events carry a player UUID in-process only. Listeners must never persist or log it,
 * and must not show one player's trophies to another player, a link client or a log.
 *
 * @since 1.20.0
 */
public final class TrophyEvents {

    /** {@code player} newly earned {@code trophy}. */
    public record Awarded(UUID player, Identifier trophy, TrophyTier tier, AwardSource source) {
    }

    /** {@code player} lost {@code trophy} (an operator correction). */
    public record Revoked(UUID player, Identifier trophy) {
    }

    /** {@code player} holds every member of {@code collection}; {@code trim} is now unlocked. */
    public record CollectionCompleted(UUID player, Identifier collection, String trim) {
    }

    /** The provider erased {@code player}'s trophy record. */
    public record RecordErased(UUID player) {
    }

    private static final List<Consumer<Awarded>> AWARDED = new CopyOnWriteArrayList<>();
    private static final List<Consumer<Revoked>> REVOKED = new CopyOnWriteArrayList<>();
    private static final List<Consumer<CollectionCompleted>> COLLECTIONS = new CopyOnWriteArrayList<>();
    private static final List<Consumer<RecordErased>> ERASED = new CopyOnWriteArrayList<>();

    private TrophyEvents() {
    }

    public static void onAwarded(Consumer<Awarded> listener) {
        AWARDED.add(listener);
    }

    public static void onRevoked(Consumer<Revoked> listener) {
        REVOKED.add(listener);
    }

    public static void onCollectionCompleted(Consumer<CollectionCompleted> listener) {
        COLLECTIONS.add(listener);
    }

    public static void onRecordErased(Consumer<RecordErased> listener) {
        ERASED.add(listener);
    }

    static void fireAwarded(Awarded event) {
        ListenerGuard.dispatch("TrophyEvents", AWARDED, event);
    }

    static void fireRevoked(Revoked event) {
        ListenerGuard.dispatch("TrophyEvents", REVOKED, event);
    }

    /** For the registered trophy provider only. Does nothing while no real provider is bound. */
    public static void fireCollectionCompleted(CollectionCompleted event) {
        if (TrophyApi.hasRealProvider()) {
            ListenerGuard.dispatch("TrophyEvents", COLLECTIONS, event);
        }
    }

    /** For the registered trophy provider only. Does nothing while no real provider is bound. */
    public static void fireRecordErased(RecordErased event) {
        if (TrophyApi.hasRealProvider()) {
            ListenerGuard.dispatch("TrophyEvents", ERASED, event);
        }
    }
}
