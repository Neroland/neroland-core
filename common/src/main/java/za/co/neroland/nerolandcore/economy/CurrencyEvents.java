package za.co.neroland.nerolandcore.economy;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Balance-change notifications, fired by {@link CurrencyApi} after a successful
 * deposit / withdraw / transfer. Any mod (shops, quest rewards, HUDs) can subscribe.
 */
public final class CurrencyEvents {

    /** A balance change for one player + currency. */
    public record BalanceChange(UUID player, Currency currency, long oldBalance, long newBalance) {
    }

    private static final List<Consumer<BalanceChange>> LISTENERS = new CopyOnWriteArrayList<>();

    private CurrencyEvents() {
    }

    public static void onChange(Consumer<BalanceChange> listener) {
        LISTENERS.add(listener);
    }

    static void fire(BalanceChange change) {
        for (Consumer<BalanceChange> listener : LISTENERS) {
            listener.accept(change);
        }
    }
}
