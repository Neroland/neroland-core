package za.co.neroland.nerolandcore.link;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * The shared publish/subscribe channel between event-producing modules and the bridge.
 * Modules {@linkplain #publish(LinkEvent) publish} {@link LinkEvent}s; the bridge
 * {@linkplain #subscribe(Consumer) subscribes} and turns each event into WebSocket
 * deltas and notification candidates.
 *
 * <p>Thread-safe: the subscriber list is a {@link CopyOnWriteArrayList}, matching the
 * pattern Core uses for its other event dispatchers ({@code CurrencyEvents},
 * {@code ReputationEvents}). A misbehaving subscriber cannot break publication — its
 * exception is caught and logged, and remaining subscribers still receive the event.
 *
 * <p>There is one shared instance, reachable via {@link NeroLinkRegistry#eventBus()}.
 */
public final class LinkEventBus {

    private final List<Consumer<LinkEvent>> subscribers = new CopyOnWriteArrayList<>();

    LinkEventBus() {
    }

    /** Publish an event to every subscriber. */
    public void publish(LinkEvent event) {
        for (Consumer<LinkEvent> subscriber : subscribers) {
            try {
                subscriber.accept(event);
            } catch (RuntimeException e) {
                NerolandCoreCommon.LOGGER.warn("[Neroland Core] A NeroLink event subscriber failed.", e);
            }
        }
    }

    /** Subscribe to all events (typically the bridge). */
    public void subscribe(Consumer<LinkEvent> subscriber) {
        subscribers.add(subscriber);
    }

    /** Remove a previously registered subscriber. */
    public void unsubscribe(Consumer<LinkEvent> subscriber) {
        subscribers.remove(subscriber);
    }
}
