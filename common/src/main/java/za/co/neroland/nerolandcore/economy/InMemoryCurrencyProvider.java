package za.co.neroland.nerolandcore.economy;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.Identifier;

/**
 * The default reference {@link CurrencyProvider}: non-persistent, in-memory balances.
 * Active until NeroEconomy registers a real store, so any mod can read/write the API
 * (and be tested) without NeroEconomy installed. <b>Not</b> Core "storing" the
 * economy — it holds nothing across a restart and is replaced the moment a real
 * provider registers.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class InMemoryCurrencyProvider implements CurrencyProvider {

    private final Map<UUID, Map<Identifier, Long>> balances = new ConcurrentHashMap<>();

    @Override
    public long getBalance(UUID player, Currency currency) {
        return balances.getOrDefault(player, Map.of()).getOrDefault(currency.id(), 0L);
    }

    @Override
    public boolean deposit(UUID player, Currency currency, long amount) {
        if (amount < 0) {
            return false;
        }
        balances.computeIfAbsent(player, ignored -> new ConcurrentHashMap<>())
                .merge(currency.id(), amount, Long::sum);
        return true;
    }

    @Override
    public boolean withdraw(UUID player, Currency currency, long amount) {
        if (amount < 0) {
            return false;
        }
        Map<Identifier, Long> wallet = balances.get(player);
        if (wallet == null || wallet.getOrDefault(currency.id(), 0L) < amount) {
            return false;
        }
        wallet.merge(currency.id(), -amount, Long::sum);
        return true;
    }

    @Override
    public void forgetPlayer(UUID player) {
        balances.remove(player);
    }
}
