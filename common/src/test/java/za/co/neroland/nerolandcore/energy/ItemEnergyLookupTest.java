package za.co.neroland.nerolandcore.energy;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ItemEnergyLookupTest {

    private static final NeroEnergyStorage CELL = new NeroEnergyStorage() {
        @Override
        public long getAmount() {
            return 5;
        }

        @Override
        public long getCapacity() {
            return 10;
        }

        @Override
        public long insert(long maxAmount, boolean simulate) {
            return 0;
        }

        @Override
        public long extract(long maxAmount, boolean simulate) {
            return 0;
        }
    };

    @AfterEach
    void reset() {
        ItemEnergyLookup.clearForTests();
    }

    @Test
    void noProvidersFindsNothing() {
        assertNull(ItemEnergyLookup.firstMatch(null));
        assertNull(ItemEnergyLookup.find(null));
    }

    @Test
    void firstNonNullProviderWinsAndThrowingProvidersAreSkipped() {
        ItemEnergyLookup.register(stack -> {
            throw new IllegalStateException("broken mod");
        });
        ItemEnergyLookup.register(stack -> null);
        ItemEnergyLookup.register(stack -> CELL);
        ItemEnergyLookup.register(stack -> {
            throw new AssertionError("never reached");
        });
        assertSame(CELL, ItemEnergyLookup.firstMatch(null));
    }
}
