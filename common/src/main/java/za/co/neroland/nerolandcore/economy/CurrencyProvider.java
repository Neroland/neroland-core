package za.co.neroland.nerolandcore.economy;

import java.util.UUID;

/**
 * The currency storage contract. <b>Core defines this but stores nothing</b> —
 * NeroEconomy implements it and registers via
 * {@link CurrencyApi#setProvider(CurrencyProvider)}. Any mod reads/writes balances
 * through {@link CurrencyApi}, never against a concrete store.
 *
 * <p>All amounts are non-negative whole units; implementations reject negative
 * amounts and overdrafts. Balances are keyed by {@code (player UUID, currency)}.
 */
public interface CurrencyProvider {

    /** The player's balance in {@code currency} (0 if they have none). */
    long getBalance(UUID player, Currency currency);

    /** Add {@code amount} to the player's balance. @return false if rejected (e.g. negative amount). */
    boolean deposit(UUID player, Currency currency, long amount);

    /** Remove {@code amount} if the player can afford it. @return false on insufficient funds / bad amount. */
    boolean withdraw(UUID player, Currency currency, long amount);

    /** Move {@code amount} between players (atomic: withdraw then deposit). @return false if it can't complete. */
    default boolean transfer(UUID from, UUID to, Currency currency, long amount) {
        if (withdraw(from, currency, amount)) {
            deposit(to, currency, amount);
            return true;
        }
        return false;
    }

    /** POPIA/GDPR erasure hook: drop everything stored for a player. No-op by default. */
    default void forgetPlayer(UUID player) {
    }
}
