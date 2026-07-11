package za.co.neroland.nerolandcore.event;

import java.util.function.Consumer;

import za.co.neroland.nerolandcore.economy.CurrencyEvents;
import za.co.neroland.nerolandcore.economy.CurrencyEvents.BalanceChange;
import za.co.neroland.nerolandcore.progression.GateEvents;
import za.co.neroland.nerolandcore.progression.GateEvents.GateChange;
import za.co.neroland.nerolandcore.reputation.ReputationEvents;
import za.co.neroland.nerolandcore.reputation.ReputationEvents.ReputationChange;

/**
 * The single discoverable entry point for the Neroland change-event bus — one place
 * any mod subscribes to economic, reputation and progression changes (e.g. NeroEvents
 * reacting to {@code reached_orbit}, a faction perk unlocking on a reputation
 * threshold, an analytics hook on every transaction). Each method delegates to the
 * domain's own typed channel ({@link GateEvents} / {@link CurrencyEvents} /
 * {@link ReputationEvents}); use those directly if you prefer.
 *
 * <p>Listeners run on the server thread. These are loader-agnostic (pure server-side
 * registries) — no per-loader implementation is needed.
 */
public final class CoreEvents {

    private CoreEvents() {
    }

    /** React to a progression gate opening or closing. */
    public static void onProgression(Consumer<GateChange> listener) {
        GateEvents.onChange(listener);
    }

    /** React to a player's currency balance changing. */
    public static void onCurrency(Consumer<BalanceChange> listener) {
        CurrencyEvents.onChange(listener);
    }

    /** React to a player's faction reputation changing. */
    public static void onReputation(Consumer<ReputationChange> listener) {
        ReputationEvents.onChange(listener);
    }

    /**
     * React to a scalar quantity crossing a publisher-defined threshold (added in Core 1.7.0) —
     * e.g. Nerotech's regional pollution passing its event threshold. See
     * {@link ThresholdEvents.ThresholdCrossing} for the payload contract (scopes are places or
     * systems, never people).
     */
    public static void onThreshold(Consumer<ThresholdEvents.ThresholdCrossing> listener) {
        ThresholdEvents.onCrossing(listener);
    }
}
