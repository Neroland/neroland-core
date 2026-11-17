package za.co.neroland.nerolandcore.trophy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static za.co.neroland.nerolandcore.trophy.TrophyTestData.id;
import static za.co.neroland.nerolandcore.trophy.TrophyTestData.trophyJson;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** The identity cache: same server + same resources is cached; a new resource manager reloads. */
class TrophyDefinitionsReloadTest {

    private final Object server = new Object();
    private int lists;

    @AfterEach
    void reset() {
        TrophyDefinitions.clear();
    }

    private TrophyDefinitions.Snapshot read(Object resources, TrophyTestData data) {
        TrophyDefinitions.Lister counting = directory -> {
            lists++;
            return data.lister().list(directory);
        };
        return TrophyDefinitions.cached(server, resources, counting, mod -> true, item -> true);
    }

    @Test
    void newResourceManagerReloadsAndBumpsTheGeneration() {
        Object first = new Object();
        TrophyTestData data = new TrophyTestData().trophy("x:a", trophyJson("minecraft:cake", ""));
        TrophyDefinitions.Snapshot one = read(first, data);
        assertSame(one, read(first, data), "same server and resources: cached");
        assertEquals(2, lists, "one load lists both roots once");

        data.trophy("x:b", trophyJson("minecraft:cake", ""));
        TrophyDefinitions.Snapshot two = read(new Object(), data);
        assertEquals(one.generation() + 1, two.generation());
        assertEquals(Set.of(id("x:a"), id("x:b")), two.trophies().keySet());
        assertEquals(TrophyDefinitions.generation(), two.generation());
        assertEquals(2, TrophyDefinitions.report().trophies());
    }

    @Test
    void failedListingIsNotRetriedAndKeepsPreviousDataOnlyForTheSameServer() {
        Object resources = new Object();
        TrophyTestData data = new TrophyTestData().trophy("x:a", trophyJson("minecraft:cake", ""));
        TrophyDefinitions.Snapshot good = read(resources, data);

        int[] attempts = {0};
        TrophyDefinitions.Lister failing = directory -> {
            attempts[0]++;
            throw new IllegalStateException("listing failed");
        };
        Object reloaded = new Object();
        TrophyDefinitions.Snapshot kept = TrophyDefinitions.cached(server, reloaded, failing, m -> true, i -> true);
        assertSame(good, kept, "same server: previous snapshot kept");
        TrophyDefinitions.cached(server, reloaded, failing, m -> true, i -> true);
        assertEquals(1, attempts[0], "a failed load is not retried on the next read");

        TrophyDefinitions.Snapshot otherWorld =
                TrophyDefinitions.cached(new Object(), new Object(), failing, m -> true, i -> true);
        assertTrue(otherWorld.trophies().isEmpty(), "another server never sees the previous world's trophies");
    }
}
