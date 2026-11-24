package za.co.neroland.nerolandcore.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Erasure tombstones (Core 1.18.0): owner hashes, entity markers, expiry, the cap, persistence and the
 * conformance helper; plus the retention schedule's timing. Plain JVM, random UUIDs, nothing logged.
 */
class ErasureTombstonesTest {

    private static final long DAY = 86_400_000L;

    private long now = 1_000L * DAY;

    private final List<PlayerDataEraser> registered = new ArrayList<>();

    ErasureTombstonesTest() {
        ErasureTombstones.clock = () -> now;
    }

    @AfterEach
    void restore() {
        ErasureTombstones.clock = System::currentTimeMillis;
        PlayerDataErasure.durability = PlayerDataErasure.LIVE;
        registered.forEach(PlayerDataErasure::unregister);
        registered.clear();
    }

    @Test
    void ownerTombstoneIsAHashNotTheUuid() {
        ErasureTombstones store = new ErasureTombstones();
        UUID owner = UUID.randomUUID();
        assertFalse(store.isOwnerErased(owner));
        assertFalse(store.hasSalt(), "no salt until something needs it");

        store.markOwner(owner);
        assertTrue(store.isOwnerErased(owner));
        assertFalse(store.isOwnerErased(UUID.randomUUID()));
        assertTrue(store.isDirty());

        String saved = encode(store).toString();
        assertFalse(saved.contains(owner.toString()), "the owner UUID must never be stored");
    }

    @Test
    void sameUuidHashesDifferentlyUnderAnotherSalt() {
        UUID owner = UUID.randomUUID();
        assertNotEquals(ErasureTombstones.hash("aa", owner), ErasureTombstones.hash("bb", owner));
        assertEquals(32, ErasureTombstones.hash("aa", owner).length(), "128-bit truncation, hex");
    }

    @Test
    void entityMarkersLiveUntilFreed() {
        ErasureTombstones store = new ErasureTombstones();
        UUID entity = UUID.randomUUID();
        store.markEntity(entity);
        assertTrue(store.isEntityMarked(entity));
        assertTrue(store.shouldRelease(null, entity));

        now += 10_000L * DAY; // entity markers never expire
        store.pruneExpiredOwners(ErasureTombstones.ownerLifetimeDays(0));
        assertTrue(store.isEntityMarked(entity));

        store.entityFreed(entity);
        assertFalse(store.isEntityMarked(entity));
        assertEquals(0, store.count());
    }

    @Test
    void ownerTombstonesExpireAfterAtLeastAYearAndTakeTheSaltWithThem() {
        assertEquals(365, ErasureTombstones.ownerLifetimeDays(0));
        assertEquals(365, ErasureTombstones.ownerLifetimeDays(30));
        assertEquals(800, ErasureTombstones.ownerLifetimeDays(800));

        ErasureTombstones store = new ErasureTombstones();
        UUID owner = UUID.randomUUID();
        store.markOwner(owner);

        now += 364L * DAY;
        assertEquals(0, store.pruneExpiredOwners(365));
        assertTrue(store.isOwnerErased(owner));

        now += 2L * DAY;
        assertFalse(store.isOwnerErased(owner), "an expired tombstone stops answering before it is pruned");
        assertEquals(1, store.pruneExpiredOwners(365));
        assertFalse(store.hasSalt(), "the salt is deleted with the last owner tombstone");
        assertEquals(0, store.count());
    }

    @Test
    void aReturningOwnerClearsOnlyTheOwnerTombstone() {
        ErasureTombstones store = new ErasureTombstones();
        UUID owner = UUID.randomUUID();
        UUID oldPet = UUID.randomUUID();
        store.markOwner(owner);
        store.markEntity(oldPet);

        store.ownerReturned(owner);
        assertFalse(store.isOwnerErased(owner));
        assertFalse(store.shouldRelease(owner, UUID.randomUUID()), "a new pet is not released");
        assertTrue(store.shouldRelease(owner, oldPet), "the erased pet still is");
        assertFalse(store.hasSalt());
    }

    @Test
    void theCapDropsTheOldestAndCountsItForTheWarning() {
        java.util.Map<Integer, Long> set = new java.util.LinkedHashMap<>();
        int dropped = 0;
        for (int i = 0; i <= ErasureTombstones.MAX_TOMBSTONES; i++) {
            dropped += ErasureTombstones.addBounded(set, i, i);
        }
        assertEquals(1, dropped);
        assertEquals(ErasureTombstones.MAX_TOMBSTONES, set.size());
        assertFalse(set.containsKey(0), "the oldest goes first");

        ErasureTombstones store = new ErasureTombstones();
        store.markEntity(UUID.randomUUID());
        assertEquals(0, store.droppedAtCap());
    }

    @Test
    void roundTripsThroughTheCodec() {
        ErasureTombstones store = new ErasureTombstones();
        UUID owner = UUID.randomUUID();
        UUID entity = UUID.randomUUID();
        store.markOwner(owner);
        store.markEntity(entity);

        ErasureTombstones loaded = ErasureTombstones.storeCodec().parse(JsonOps.INSTANCE, encode(store))
                .result().orElseThrow();
        assertTrue(loaded.isOwnerErased(owner));
        assertTrue(loaded.isEntityMarked(entity));
        assertEquals(2, loaded.count());

        ErasureTombstones empty = ErasureTombstones.storeCodec()
                .parse(JsonOps.INSTANCE, new com.google.gson.JsonObject()).result().orElseThrow();
        assertEquals(0, empty.count(), "a missing file field reads as empty");
    }

    @Test
    void conformanceHelperPassesWhenTheEraserMarksTheUnloadedEntity() {
        ErasureTombstones store = new ErasureTombstones();
        UUID player = UUID.randomUUID();
        UUID unloadedPet = UUID.randomUUID();
        register((server, uuid) -> {
            if (player.equals(uuid)) {
                store.markEntity(unloadedPet);
            }
        });
        ErasureConformance.create()
                .tombstoneReleases("test:unloaded_pet", store, unloadedPet)
                .verify(null, player);
    }

    @Test
    void conformanceHelperFailsWhenNothingIsMarked() {
        ErasureTombstones store = new ErasureTombstones();
        register((server, uuid) -> { });
        ErasureConformance.Report report = ErasureConformance.create()
                .tombstoneReleases("test:forgotten_pet", store, UUID.randomUUID())
                .run(null, UUID.randomUUID());
        assertFalse(report.passed());
        assertTrue(report.retainedSubsystems().contains("test:forgotten_pet"));
        assertThrows(IllegalArgumentException.class,
                () -> ErasureConformance.create().tombstoneReleases("x", null, UUID.randomUUID()));
    }

    @Test
    void erasureWritesTheTombstoneFirstAndSavesLast() {
        List<String> order = new ArrayList<>();
        UUID player = UUID.randomUUID();
        PlayerDataErasure.durability = recording(order, true, true);
        register((server, uuid) -> order.add("eraser"));

        assertTrue(PlayerDataErasure.eraseAndReport(null, player));
        assertEquals("tombstone", order.get(0), "the owner tombstone is written before any eraser runs");
        assertEquals("flush", order.get(order.size() - 1), "the save comes after every eraser");
        assertTrue(order.contains("eraser"));
    }

    @Test
    void aFailedSaveOrTombstoneReportsTheErasureIncomplete() {
        register((server, uuid) -> { });
        PlayerDataErasure.durability = recording(new ArrayList<>(), true, false);
        assertFalse(PlayerDataErasure.eraseAndReport(null, UUID.randomUUID()), "not on disk: not complete");
        PlayerDataErasure.durability = recording(new ArrayList<>(), false, true);
        assertFalse(PlayerDataErasure.eraseAndReport(null, UUID.randomUUID()), "no tombstone: not complete");
        PlayerDataErasure.durability = PlayerDataErasure.LIVE;
        assertTrue(PlayerDataErasure.eraseAndReport(null, UUID.randomUUID()), "no server: nothing to save");
    }

    @Test
    void retentionScheduleRunsAtStartThenDaily() {
        assertTrue(RetentionSchedule.due(true, 0L, 5L), "always on a newly started server");
        assertFalse(RetentionSchedule.due(false, now, now + DAY - 1));
        assertTrue(RetentionSchedule.due(false, now, now + DAY));
        // A null server is ignored (no NPE from the tick hook).
        RetentionSchedule.onServerTick(null);
    }

    private static PlayerDataErasure.Durability recording(List<String> order, boolean tombstone, boolean flush) {
        return new PlayerDataErasure.Durability() {
            @Override
            public boolean tombstone(net.minecraft.server.MinecraftServer server, UUID player) {
                order.add("tombstone");
                return tombstone;
            }

            @Override
            public boolean flush(net.minecraft.server.MinecraftServer server) {
                order.add("flush");
                return flush;
            }
        };
    }

    private void register(PlayerDataEraser eraser) {
        registered.add(eraser);
        PlayerDataErasure.register(eraser);
    }

    private static JsonElement encode(ErasureTombstones store) {
        return ErasureTombstones.storeCodec().encodeStart(JsonOps.INSTANCE, store).result().orElseThrow();
    }
}
