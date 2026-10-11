package za.co.neroland.nerolandcore.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.resources.Identifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.co.neroland.nerolandcore.economy.CurrencyEvents.AttributedChange;

/** The attributed CurrencyApi overloads: validation, provider delegation and the attribution side-channel. */
class CurrencyApiAttributionTest {

    private static final Currency CREDITS = Currency.of(
            Identifier.fromNamespaceAndPath("test", "credits"), "currency.test.credits");

    /** Records the attribution it receives; otherwise in-memory behaviour. */
    private static final class LedgerProvider extends InMemoryLedger {
    }

    private static class InMemoryLedger implements CurrencyProvider {
        final InMemoryCurrencyProvider store = new InMemoryCurrencyProvider();
        final List<String> ledger = new ArrayList<>();

        @Override
        public long getBalance(UUID player, Currency currency) {
            return store.getBalance(player, currency);
        }

        @Override
        public boolean deposit(UUID player, Currency currency, long amount) {
            return store.deposit(player, currency, amount);
        }

        @Override
        public boolean withdraw(UUID player, Currency currency, long amount) {
            return store.withdraw(player, currency, amount);
        }

        @Override
        public boolean deposit(UUID player, Currency currency, long amount, String sourceModId, String reason) {
            ledger.add("deposit:" + sourceModId + ":" + reason);
            return deposit(player, currency, amount);
        }

        @Override
        public void forgetPlayer(UUID player) {
            store.forgetPlayer(player);
        }
    }

    private LedgerProvider provider;

    @BeforeEach
    void install() {
        provider = new LedgerProvider();
        CurrencyApi.setProvider(provider);
    }

    @Test
    void invalidAttributionIsRejectedWithoutTouchingTheProvider() {
        UUID player = UUID.randomUUID();
        assertFalse(CurrencyApi.deposit(player, CREDITS, 10, "Bad Mod", "quest_reward"));
        assertFalse(CurrencyApi.deposit(player, CREDITS, 10, "neroquests", "has space"));
        assertFalse(CurrencyApi.withdraw(player, CREDITS, 10, null, "x"));
        assertFalse(CurrencyApi.transfer(player, UUID.randomUUID(), CREDITS, 1, "neroquests", null));
        assertEquals(0, CurrencyApi.getBalance(player, CREDITS));
        assertTrue(provider.ledger.isEmpty());
    }

    @Test
    void attributedCallsReachTheProviderOverloadAndTheAttributedListeners() {
        UUID player = UUID.randomUUID();
        List<AttributedChange> seen = new ArrayList<>();
        CurrencyEvents.onAttributedChange(c -> {
            if (c.change().player().equals(player)) {
                seen.add(c);
            }
        });
        assertTrue(CurrencyApi.deposit(player, CREDITS, 100, "neroquests", "quest_reward"));
        assertEquals(List.of("deposit:neroquests:quest_reward"), provider.ledger);
        // withdraw falls through the provider's default overload to the 3-arg method
        assertTrue(CurrencyApi.withdraw(player, CREDITS, 40, "neroeconomy", "shop_purchase"));
        assertTrue(CurrencyApi.deposit(player, CREDITS, 5));
        assertEquals(65, CurrencyApi.getBalance(player, CREDITS));

        assertEquals(3, seen.size());
        assertEquals("neroquests", seen.get(0).sourceModId());
        assertEquals(0, seen.get(0).change().oldBalance());
        assertEquals(100, seen.get(0).change().newBalance());
        assertEquals("shop_purchase", seen.get(1).reason());
        assertNull(seen.get(2).sourceModId());
        assertNull(seen.get(2).reason());
    }

    @Test
    void aThrowingBalanceListenerDoesNotBreakTheMovement() {
        UUID player = UUID.randomUUID();
        List<Long> after = new ArrayList<>();
        CurrencyEvents.onChange(c -> {
            if (c.player().equals(player)) {
                throw new IllegalStateException("listener bug");
            }
        });
        CurrencyEvents.onChange(c -> {
            if (c.player().equals(player)) {
                after.add(c.newBalance());
            }
        });
        assertTrue(CurrencyApi.deposit(player, CREDITS, 7, "neroquests", "quest_reward"));
        assertEquals(List.of(7L), after);
    }
}
