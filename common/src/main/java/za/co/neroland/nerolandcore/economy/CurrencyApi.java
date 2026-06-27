package za.co.neroland.nerolandcore.economy;

import java.util.UUID;

import za.co.neroland.nerolandcore.economy.CurrencyEvents.BalanceChange;

/**
 * The single entry point any mod uses to read or move money. Routes to the
 * registered {@link CurrencyProvider} (NeroEconomy's store) and fires
 * {@link CurrencyEvents} on change. Until a real provider registers, an
 * {@link InMemoryCurrencyProvider} backs the API so callers work — and can be
 * tested — without NeroEconomy.
 *
 * <p>Core itself stores nothing economic: this facade only delegates.
 */
public final class CurrencyApi {

    private static volatile CurrencyProvider provider = new InMemoryCurrencyProvider();

    private CurrencyApi() {
    }

    /** Register the backing store (NeroEconomy calls this at init). Replaces the in-memory default. */
    public static void setProvider(CurrencyProvider newProvider) {
        provider = newProvider;
    }

    /** The active provider. */
    public static CurrencyProvider provider() {
        return provider;
    }

    /** Whether a real provider (not the in-memory default) is registered. */
    public static boolean hasRealProvider() {
        return !(provider instanceof InMemoryCurrencyProvider);
    }

    public static long getBalance(UUID player, Currency currency) {
        return provider.getBalance(player, currency);
    }

    public static boolean deposit(UUID player, Currency currency, long amount) {
        long before = provider.getBalance(player, currency);
        boolean ok = provider.deposit(player, currency, amount);
        if (ok) {
            CurrencyEvents.fire(new BalanceChange(player, currency, before, provider.getBalance(player, currency)));
        }
        return ok;
    }

    public static boolean withdraw(UUID player, Currency currency, long amount) {
        long before = provider.getBalance(player, currency);
        boolean ok = provider.withdraw(player, currency, amount);
        if (ok) {
            CurrencyEvents.fire(new BalanceChange(player, currency, before, provider.getBalance(player, currency)));
        }
        return ok;
    }

    public static boolean transfer(UUID from, UUID to, Currency currency, long amount) {
        long fromBefore = provider.getBalance(from, currency);
        long toBefore = provider.getBalance(to, currency);
        boolean ok = provider.transfer(from, to, currency, amount);
        if (ok) {
            CurrencyEvents.fire(new BalanceChange(from, currency, fromBefore, provider.getBalance(from, currency)));
            CurrencyEvents.fire(new BalanceChange(to, currency, toBefore, provider.getBalance(to, currency)));
        }
        return ok;
    }
}
