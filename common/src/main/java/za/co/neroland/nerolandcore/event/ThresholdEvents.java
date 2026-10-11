package za.co.neroland.nerolandcore.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

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
 *
 * <p><b>Publisher identity (Core 1.15.0).</b> A publisher may name itself with
 * {@link #fire(String, ThresholdCrossing)}; listeners that care register with
 * {@link #onSourcedCrossing(Consumer)} and receive a {@link SourcedCrossing}. The original
 * {@link #fire(ThresholdCrossing)} still works and reports an unknown ({@code null}) source, and
 * {@link #onCrossing(Consumer)} listeners keep receiving the bare {@link ThresholdCrossing} either way.
 *
 * <p>Listener failures are isolated: a listener that throws a {@link RuntimeException} is logged once
 * per listener class (without the crossing) and skipped, and the remaining listeners still run.
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

    /**
     * A crossing together with the publisher that fired it (added in Core 1.15.0).
     *
     * @param sourceModId the publishing mod's id ({@code [a-z0-9_.-]{1,64}}), or {@code null} when the
     *                    publisher used {@link #fire(ThresholdCrossing)} or supplied an invalid id
     * @param crossing    the crossing itself
     */
    public record SourcedCrossing(@Nullable String sourceModId, ThresholdCrossing crossing) {
    }

    private static final List<Consumer<ThresholdCrossing>> LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<Consumer<SourcedCrossing>> SOURCED_LISTENERS = new CopyOnWriteArrayList<>();

    private ThresholdEvents() {
    }

    /** Register a crossing listener (runs on the server thread). */
    public static void onCrossing(Consumer<ThresholdCrossing> listener) {
        LISTENERS.add(listener);
    }

    /**
     * Register a listener that also learns which mod published each crossing (runs on the server
     * thread). Receives every crossing, including those fired without a source.
     */
    public static void onSourcedCrossing(Consumer<SourcedCrossing> listener) {
        SOURCED_LISTENERS.add(listener);
    }

    /** Publish a crossing to every listener, with an unknown source. Server thread only. */
    public static void fire(ThresholdCrossing crossing) {
        dispatch(null, crossing);
    }

    /**
     * Publish a crossing on behalf of {@code sourceModId}. Server thread only. An id that does not
     * match {@link SourceIds#SOURCE_MOD_ID} is reported to listeners as {@code null} (unknown) rather
     * than rejected, so a publisher bug never drops the crossing itself.
     */
    public static void fire(@Nullable String sourceModId, ThresholdCrossing crossing) {
        dispatch(SourceIds.isValidSourceModId(sourceModId) ? sourceModId : null, crossing);
    }

    private static void dispatch(@Nullable String sourceModId, ThresholdCrossing crossing) {
        ListenerGuard.dispatch("ThresholdEvents", LISTENERS, crossing);
        if (!SOURCED_LISTENERS.isEmpty()) {
            ListenerGuard.dispatch("ThresholdEvents", SOURCED_LISTENERS, new SourcedCrossing(sourceModId, crossing));
        }
    }
}
