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

    /**
     * Attributed deposit (added in Core 1.15.0). {@link CurrencyApi} validates both strings before
     * calling this. The default delegates to {@link #deposit(UUID, Currency, long)}, so existing
     * providers need no change; a provider that keeps a ledger overrides it to record who moved the
     * money and why.
     *
     * @param sourceModId the requesting mod ({@code [a-z0-9_.-]{1,64}})
     * @param reason      a short reason code ({@code [a-z0-9_]{1,32}})
     */
    default boolean deposit(UUID player, Currency currency, long amount, String sourceModId, String reason) {
        return deposit(player, currency, amount);
    }

    /**
     * Attributed withdraw (added in Core 1.15.0). Defaults to
     * {@link #withdraw(UUID, Currency, long)}; see {@link #deposit(UUID, Currency, long, String, String)}.
     */
    default boolean withdraw(UUID player, Currency currency, long amount, String sourceModId, String reason) {
        return withdraw(player, currency, amount);
    }

    /**
     * Attributed transfer (added in Core 1.15.0). Defaults to
     * {@link #transfer(UUID, UUID, Currency, long)}; see {@link #deposit(UUID, Currency, long, String, String)}.
     */
    default boolean transfer(UUID from, UUID to, Currency currency, long amount, String sourceModId,
            String reason) {
        return transfer(from, to, currency, amount);
    }

    /**
     * POPIA/GDPR erasure hook: drop every balance stored for {@code player}.
     *
     * @implSpec <b>Any provider that persists balances MUST override this.</b>
     *     {@link za.co.neroland.nerolandcore.data.PlayerDataErasure} purges the economy by calling
     *     {@code CurrencyApi.provider().forgetPlayer(uuid)} — this method is the <em>only</em> route
     *     an erasure request has into currency storage. The default body is a no-op purely because
     *     making it abstract would break the frozen-between-majors API; inheriting it means a
     *     "successful" erasure leaves the player's balances on disk. Implement it even if storage is
     *     in-memory only (an explicit empty override documents the intent and silences the warning
     *     the default body logs).
     */
    default void forgetPlayer(UUID player) {
        za.co.neroland.nerolandcore.data.ErasureWarnings.warnDefaultForgetPlayer("CurrencyProvider", this);
    }
}
