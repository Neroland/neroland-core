package za.co.neroland.nerolandcore.economy;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.event.ListenerGuard;

/**
 * Balance-change notifications, fired by {@link CurrencyApi} after a successful
 * deposit / withdraw / transfer. Any mod (shops, quest rewards, HUDs) can subscribe.
 *
 * <p><b>Caller attribution (Core 1.15.0).</b> {@link #onChange(Consumer)} listeners receive the bare
 * {@link BalanceChange} exactly as before. Listeners that also want to know <i>who</i> moved the
 * money register with {@link #onAttributedChange(Consumer)} and receive an {@link AttributedChange}
 * carrying the {@code sourceModId} and {@code reason} passed to the attributed
 * {@link CurrencyApi} overloads ({@code null} for the unattributed 3-argument calls).
 *
 * <p>Listener failures are isolated: a listener that throws a {@link RuntimeException} is logged once
 * per listener class (without the change, which names a player) and skipped.
 */
public final class CurrencyEvents {

    /** A balance change for one player + currency. */
    public record BalanceChange(UUID player, Currency currency, long oldBalance, long newBalance) {
    }

    /**
     * A balance change together with its caller attribution (added in Core 1.15.0).
     *
     * @param change      the balance change
     * @param sourceModId the mod that requested the movement ({@code [a-z0-9_.-]{1,64}}), or
     *                    {@code null} for an unattributed call
     * @param reason      a short reason code ({@code [a-z0-9_]{1,32}}), or {@code null} for an
     *                    unattributed call
     */
    public record AttributedChange(BalanceChange change, @Nullable String sourceModId, @Nullable String reason) {
    }

    private static final List<Consumer<BalanceChange>> LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<Consumer<AttributedChange>> ATTRIBUTED_LISTENERS = new CopyOnWriteArrayList<>();

    private CurrencyEvents() {
    }

    public static void onChange(Consumer<BalanceChange> listener) {
        LISTENERS.add(listener);
    }

    /** Register a listener that also receives the caller attribution of every change. */
    public static void onAttributedChange(Consumer<AttributedChange> listener) {
        ATTRIBUTED_LISTENERS.add(listener);
    }

    static void fire(BalanceChange change) {
        fire(change, null, null);
    }

    static void fire(BalanceChange change, @Nullable String sourceModId, @Nullable String reason) {
        ListenerGuard.dispatch("CurrencyEvents", LISTENERS, change);
        if (!ATTRIBUTED_LISTENERS.isEmpty()) {
            ListenerGuard.dispatch("CurrencyEvents", ATTRIBUTED_LISTENERS,
                    new AttributedChange(change, sourceModId, reason));
        }
    }
}
