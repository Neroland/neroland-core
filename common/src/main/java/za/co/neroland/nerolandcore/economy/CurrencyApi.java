package za.co.neroland.nerolandcore.economy;

import java.util.UUID;

import za.co.neroland.nerolandcore.economy.CurrencyEvents.BalanceChange;
import za.co.neroland.nerolandcore.event.SourceIds;

/**
 * The single entry point any mod uses to read or move money. Routes to the
 * registered {@link CurrencyProvider} (NeroEconomy's store) and fires
 * {@link CurrencyEvents} on change. Until a real provider registers, an
 * {@link InMemoryCurrencyProvider} backs the API so callers work — and can be
 * tested — without NeroEconomy.
 *
 * <p>Core itself stores nothing economic: this facade only delegates.
 *
 * <p><b>Attributed overloads (Core 1.15.0).</b> {@link #deposit(UUID, Currency, long, String, String)},
 * {@link #withdraw(UUID, Currency, long, String, String)} and
 * {@link #transfer(UUID, UUID, Currency, long, String, String)} additionally name the calling mod and a
 * reason code, so a ledger-keeping provider can record who moved the money and why. Both strings are
 * validated first ({@link SourceIds#SOURCE_MOD_ID}, {@link SourceIds#REASON}); an invalid value makes
 * the call return {@code false} without touching the provider. The attribution reaches
 * {@link CurrencyEvents#onAttributedChange} listeners.
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

    // --- attributed overloads (1.15.0) ---------------------------------------

    /**
     * Deposit on behalf of {@code sourceModId} for {@code reason}.
     *
     * @return {@code false} if either attribution string is invalid, or the provider rejected the deposit
     */
    public static boolean deposit(UUID player, Currency currency, long amount, String sourceModId, String reason) {
        if (!SourceIds.isValidSourceModId(sourceModId) || !SourceIds.isValidReason(reason)) {
            return false;
        }
        long before = provider.getBalance(player, currency);
        boolean ok = provider.deposit(player, currency, amount, sourceModId, reason);
        if (ok) {
            CurrencyEvents.fire(new BalanceChange(player, currency, before, provider.getBalance(player, currency)),
                    sourceModId, reason);
        }
        return ok;
    }

    /**
     * Withdraw on behalf of {@code sourceModId} for {@code reason}.
     *
     * @return {@code false} if either attribution string is invalid, or the provider rejected the withdrawal
     */
    public static boolean withdraw(UUID player, Currency currency, long amount, String sourceModId, String reason) {
        if (!SourceIds.isValidSourceModId(sourceModId) || !SourceIds.isValidReason(reason)) {
            return false;
        }
        long before = provider.getBalance(player, currency);
        boolean ok = provider.withdraw(player, currency, amount, sourceModId, reason);
        if (ok) {
            CurrencyEvents.fire(new BalanceChange(player, currency, before, provider.getBalance(player, currency)),
                    sourceModId, reason);
        }
        return ok;
    }

    /**
     * Transfer on behalf of {@code sourceModId} for {@code reason}.
     *
     * @return {@code false} if either attribution string is invalid, or the provider rejected the transfer
     */
    public static boolean transfer(UUID from, UUID to, Currency currency, long amount, String sourceModId,
            String reason) {
        if (!SourceIds.isValidSourceModId(sourceModId) || !SourceIds.isValidReason(reason)) {
            return false;
        }
        long fromBefore = provider.getBalance(from, currency);
        long toBefore = provider.getBalance(to, currency);
        boolean ok = provider.transfer(from, to, currency, amount, sourceModId, reason);
        if (ok) {
            CurrencyEvents.fire(new BalanceChange(from, currency, fromBefore, provider.getBalance(from, currency)),
                    sourceModId, reason);
            CurrencyEvents.fire(new BalanceChange(to, currency, toBefore, provider.getBalance(to, currency)),
                    sourceModId, reason);
        }
        return ok;
    }
}
