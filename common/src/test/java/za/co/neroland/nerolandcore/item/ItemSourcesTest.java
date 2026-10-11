package za.co.neroland.nerolandcore.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.UUID;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Aggregation, ordering, clamping and failure isolation in the item-source registry. */
class ItemSourcesTest {

    private static final Identifier IRON = Identifier.fromNamespaceAndPath("minecraft", "iron_ingot");
    private static final UUID PLAYER = UUID.randomUUID();

    /** A fixed-stock source. */
    private static final class Stock implements ItemSource {
        int held;

        Stock(int held) {
            this.held = held;
        }

        @Override
        public long count(MinecraftServer server, UUID player, Identifier itemId) {
            return held;
        }

        @Override
        public int extract(MinecraftServer server, UUID player, Identifier itemId, int amount, boolean simulate) {
            int taken = Math.min(held, amount);
            if (!simulate) {
                held -= taken;
            }
            return taken;
        }
    }

    /** A source that always throws. */
    private static final class Broken implements ItemSource {
        @Override
        public long count(MinecraftServer server, UUID player, Identifier itemId) {
            throw new IllegalStateException("broken");
        }

        @Override
        public int extract(MinecraftServer server, UUID player, Identifier itemId, int amount, boolean simulate) {
            throw new IllegalStateException("broken");
        }
    }

    @AfterEach
    void clear() {
        ItemSources.modIds().forEach(ItemSources::unregister);
    }

    @Test
    void countsAggregateAcrossSourcesAndSkipFailures() {
        ItemSources.register("alpha", new Stock(10));
        ItemSources.register("broken", new Broken());
        ItemSources.register("beta", new Stock(5));
        assertEquals(15, ItemSources.count(null, PLAYER, IRON));
        assertEquals(Map.of("alpha", 10L, "beta", 5L), ItemSources.countBySource(null, PLAYER, IRON));
    }

    @Test
    void extractDrawsInRegistrationOrderAndSimulateChangesNothing() {
        Stock alpha = new Stock(4);
        Stock beta = new Stock(10);
        ItemSources.register("alpha", alpha);
        ItemSources.register("broken", new Broken());
        ItemSources.register("beta", beta);
        assertEquals(7, ItemSources.extract(null, PLAYER, IRON, 7, true));
        assertEquals(4, alpha.held);
        assertEquals(7, ItemSources.extract(null, PLAYER, IRON, 7, false));
        assertEquals(0, alpha.held);
        assertEquals(7, beta.held);
        assertEquals(7, ItemSources.extract(null, PLAYER, IRON, 100, false));
        assertEquals(0, ItemSources.extract(null, PLAYER, IRON, 0, false));
    }

    @Test
    void invalidModIdsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ItemSources.register("Bad Id", new Stock(1)));
        assertThrows(IllegalArgumentException.class, () -> ItemSources.register("ok", null));
    }
}
