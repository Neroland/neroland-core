package za.co.neroland.nerolandcore.event;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * Exception isolation for Core's in-process event buses (added in Core 1.15.0).
 *
 * <p>Every Core bus ({@code CurrencyEvents}, {@code ReputationEvents}, {@code GateEvents},
 * {@code MaterialMilestoneEvents}, {@link ThresholdEvents}, the NeroLink event bus and the
 * {@code ItemSources} registry) dispatches through here, so one misbehaving listener can never
 * break the publisher's call stack (often the server tick) or starve the listeners after it. A
 * {@link RuntimeException} thrown by a listener is caught, logged once per (bus, listener class),
 * and dispatch continues with the next listener. {@link Error}s still propagate.
 *
 * <p><b>Privacy (POPIA/GDPR).</b> The log line names only the bus, the listener's class, the
 * exception's class and its top stack frame. It deliberately omits the exception message and the
 * event payload: both can carry player UUIDs or names.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class ListenerGuard {

    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    private ListenerGuard() {
    }

    /** Deliver {@code event} to every listener, isolating each listener's failures. */
    public static <T> void dispatch(String bus, Iterable<? extends Consumer<? super T>> listeners, T event) {
        for (Consumer<? super T> listener : listeners) {
            try {
                listener.accept(event);
            } catch (RuntimeException failure) {
                report(bus, listener, failure);
            }
        }
    }

    /**
     * Record a listener failure: logged at warn the first time this (bus, listener class) pair fails,
     * silently counted afterwards.
     */
    public static void report(String bus, Object listener, RuntimeException failure) {
        String listenerClass = listener == null ? "null" : listener.getClass().getName();
        if (!REPORTED.add(bus + '|' + listenerClass)) {
            return;
        }
        StackTraceElement[] trace = failure.getStackTrace();
        String where = trace.length > 0 ? trace[0].toString() : "unknown";
        NerolandCoreCommon.LOGGER.warn(
                "[Neroland Core] A {} listener ({}) threw {} at {}; skipped it and continued. "
                        + "Further failures from this listener class are not logged.",
                bus, listenerClass, failure.getClass().getName(), where);
    }
}
