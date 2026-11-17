package za.co.neroland.nerolandcore.trophy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static za.co.neroland.nerolandcore.trophy.TrophyTestData.id;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

/** A fake in-memory provider: TrophyApi delegates, fires once per new award, ignores unknown ids. */
class TrophyApiFakeProviderTest {

    private static final class FakeProvider implements TrophyProvider {
        final Map<UUID, Set<Identifier>> store = new HashMap<>();

        @Override
        public boolean award(MinecraftServer server, UUID player, Identifier trophy, AwardSource source) {
            return store.computeIfAbsent(player, ignored -> new HashSet<>()).add(trophy);
        }

        @Override
        public boolean has(MinecraftServer server, UUID player, Identifier trophy) {
            return store.getOrDefault(player, Set.of()).contains(trophy);
        }

        @Override
        public Set<Identifier> earned(MinecraftServer server, UUID player) {
            return Set.copyOf(store.getOrDefault(player, Set.of()));
        }
    }

    @AfterEach
    void reset() {
        TrophyApi.setProvider(null);
    }

    private static TrophyDefinitions.Snapshot snapshot() {
        TrophyDefinition definition = TrophyDefinition.of(id("x:boss"), new TrophyDefinition.Data(id("minecraft:cake"),
                TrophyTier.NERO, Optional.empty(), false, Optional.empty(), Optional.empty(), List.of(),
                Optional.empty(), Optional.empty(), 0, 1));
        return new TrophyDefinitions.Snapshot(Map.of(id("x:boss"), definition), Map.of(), 1);
    }

    @Test
    void awardRoundTripFiresOnce() {
        FakeProvider fake = new FakeProvider();
        TrophyApi.setProvider(fake);
        assertTrue(TrophyApi.hasRealProvider());
        assertSame(fake, TrophyApi.provider());

        List<TrophyEvents.Awarded> fired = new ArrayList<>();
        TrophyEvents.onAwarded(fired::add);
        UUID player = UUID.randomUUID();

        assertTrue(TrophyApi.award(snapshot(), null, player, id("x:boss"), AwardSource.LIVE));
        assertFalse(TrophyApi.award(snapshot(), null, player, id("x:boss"), AwardSource.LIVE), "idempotent");
        assertEquals(List.of(new TrophyEvents.Awarded(player, id("x:boss"), TrophyTier.NERO, AwardSource.LIVE)), fired);
        assertTrue(fake.has(null, player, id("x:boss")));
        assertEquals(Set.of(id("x:boss")), fake.earned(null, player));
    }

    @Test
    void unknownIdReturnsFalseAndNeverReachesTheProvider() {
        FakeProvider fake = new FakeProvider();
        TrophyApi.setProvider(fake);
        assertFalse(TrophyApi.award(snapshot(), null, UUID.randomUUID(), id("absent:pack_trophy"), AwardSource.API));
        assertTrue(fake.store.isEmpty());
    }

    @Test
    void providerEventsFlowWithARealProvider() {
        TrophyApi.setProvider(new FakeProvider());
        List<Object> fired = new ArrayList<>();
        TrophyEvents.onRecordErased(fired::add);
        TrophyEvents.fireRecordErased(new TrophyEvents.RecordErased(UUID.randomUUID()));
        assertEquals(1, fired.size());
    }

    @Test
    void nullProviderRestoresTheNoOp() {
        TrophyApi.setProvider(new FakeProvider());
        TrophyApi.setProvider(null);
        assertFalse(TrophyApi.hasRealProvider());
        assertSame(NoOpTrophyProvider.INSTANCE, TrophyApi.provider());
    }
}
