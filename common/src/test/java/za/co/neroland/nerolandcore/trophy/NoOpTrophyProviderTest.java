package za.co.neroland.nerolandcore.trophy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static za.co.neroland.nerolandcore.trophy.TrophyTestData.id;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Core's default provider stores nothing and fires nothing. */
class NoOpTrophyProviderTest {

    @AfterEach
    void reset() {
        TrophyApi.setProvider(null);
    }

    @Test
    void noOpHasNoInstanceState() {
        List<Field> instanceFields = Arrays.stream(NoOpTrophyProvider.class.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers())).toList();
        assertTrue(instanceFields.isEmpty(), "the no-op provider must not be able to hold data");
    }

    @Test
    void awardDoesNothingAndRetainsNothing() {
        TrophyApi.setProvider(null);
        assertFalse(TrophyApi.hasRealProvider());
        UUID player = UUID.randomUUID();
        TrophyDefinition definition = TrophyDefinition.of(id("x:a"), new TrophyDefinition.Data(id("minecraft:cake"),
                TrophyTier.GOLD, java.util.Optional.empty(), false, java.util.Optional.empty(),
                java.util.Optional.empty(), List.of(), java.util.Optional.empty(), java.util.Optional.empty(), 0, 1));
        TrophyDefinitions.Snapshot snapshot = new TrophyDefinitions.Snapshot(Map.of(id("x:a"), definition), Map.of(), 1);

        List<TrophyEvents.Awarded> fired = new ArrayList<>();
        TrophyEvents.onAwarded(fired::add);

        assertFalse(TrophyApi.award(snapshot, null, player, id("x:a"), AwardSource.API));
        NoOpTrophyProvider noOp = NoOpTrophyProvider.INSTANCE;
        assertFalse(noOp.award(null, player, id("x:a"), AwardSource.LIVE));
        assertFalse(noOp.has(null, player, id("x:a")));
        assertEquals(0, noOp.earned(null, player).size());
        assertFalse(noOp.revoke(null, player, id("x:a")));
        assertTrue(fired.isEmpty(), "no event without a recorded award");
    }

    @Test
    void providerEventsAreInertWithoutARealProvider() {
        List<Object> fired = new ArrayList<>();
        TrophyEvents.onCollectionCompleted(fired::add);
        TrophyEvents.onRecordErased(fired::add);
        TrophyEvents.fireCollectionCompleted(new TrophyEvents.CollectionCompleted(UUID.randomUUID(), id("x:set"), "gold"));
        TrophyEvents.fireRecordErased(new TrophyEvents.RecordErased(UUID.randomUUID()));
        assertTrue(fired.isEmpty());
    }
}
