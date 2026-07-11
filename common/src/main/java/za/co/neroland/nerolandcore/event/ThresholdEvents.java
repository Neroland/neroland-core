package za.co.neroland.nerolandcore.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import net.minecraft.resources.Identifier;

/**
 * Generic threshold-crossing notifications (added in Core 1.7.0). Any mod that tracks a scalar
 * quantity — Nerotech's regional pollution, a reactor's containment stress, a colony's food
 * stock — publishes a {@link ThresholdCrossing} when the value passes a configured threshold,
 * and any mod (the intended consumer is NeroEvents, reacting with dynamic world events) can
 * listen without either side importing the other: both depend only on Core.
 *
 * <p>Unlike {@link za.co.neroland.nerolandcore.progression.GateEvents}, {@link #fire} is public:
 * the publishers are downstream mods, not Core itself. Listeners run on the server thread —
 * publishers must fire from server-side code only.
 *
 * <p>Privacy (POPIA/GDPR): the {@code scope} key identifies a <b>place or system</b> (a region
 * key, a dimension id, a machine class), never a person. Publishers must not encode player
 * UUIDs or names into crossings.
 */
public final class ThresholdEvents {

    /**
     * One threshold crossing.
     *
     * @param channel   the quantity that crossed, namespaced by the publisher
     *                  (e.g. {@code nerotech:pollution})
     * @param scope     publisher-defined scope key for <i>where</i> it crossed (e.g. a packed
     *                  region key + dimension id); never personal data
     * @param value     the quantity's value after the mutation that crossed the threshold
     * @param threshold the threshold that was crossed
     * @param rising    {@code true} if the value crossed upward (worsening), {@code false} if it
     *                  recovered back below
     */
    public record ThresholdCrossing(Identifier channel, String scope, long value, long threshold,
            boolean rising) {
    }

    private static final List<Consumer<ThresholdCrossing>> LISTENERS = new CopyOnWriteArrayList<>();

    private ThresholdEvents() {
    }

    /** Register a crossing listener (runs on the server thread). */
    public static void onCrossing(Consumer<ThresholdCrossing> listener) {
        LISTENERS.add(listener);
    }

    /** Publish a crossing to every listener. Server thread only. */
    public static void fire(ThresholdCrossing crossing) {
        for (Consumer<ThresholdCrossing> listener : LISTENERS) {
            listener.accept(crossing);
        }
    }
}
