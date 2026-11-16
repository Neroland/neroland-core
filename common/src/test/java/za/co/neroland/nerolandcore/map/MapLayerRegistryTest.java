package za.co.neroland.nerolandcore.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static za.co.neroland.nerolandcore.map.MapTestSupport.OVERWORLD;
import static za.co.neroland.nerolandcore.map.MapTestSupport.id;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.resources.Identifier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import za.co.neroland.nerolandcore.map.MapLayerSink.Reject;
import za.co.neroland.nerolandcore.map.MapTestSupport.FakeProvider;

/** Registration rules, the guarded query path and badge filtering. */
class MapLayerRegistryTest {

    private static final Identifier ICON = id("fake", "icon");
    private static final MapViewer VIEWER = MapTestSupport.online(UUID.randomUUID(), OVERWORLD, 0, 0);
    private static final MapBounds NEAR = MapBounds.around(0, 0, 500);

    @AfterEach
    void clear() {
        MapLayerRegistry.clearForTest();
    }

    private static MapLayerInfo layer(String ns, String path) {
        return MapLayerInfo.of(id(ns, path), "map." + path, ICON, MapScope.OWNER);
    }

    private static MapMarker marker(Identifier layer, String markerId) {
        return MapMarker.builder(markerId, layer, OVERWORLD, 1, 64, 1).build();
    }

    @Test
    void registrationSnapshotsLayersOnce() {
        FakeProvider p = new FakeProvider("fake", List.of(layer("fake", "a")), (v, d, b, s) -> { });
        MapLayerRegistry.register(p);
        p.layers.add(layer("fake", "sneaky"));
        p.layers.add(layer("other", "foreign"));
        assertEquals(1, MapLayerRegistry.layers().size());
        assertTrue(MapLayerRegistry.layer(id("fake", "a")).isPresent());
        assertFalse(MapLayerRegistry.layer(id("fake", "sneaky")).isPresent());
        MapLayerRegistry.query("fake", VIEWER, OVERWORLD, NEAR);
        assertEquals(1, p.layersCalls, "layers() is read once, at registration");
        assertSame(p, MapLayerRegistry.provider("fake").orElseThrow());
    }

    @Test
    void registrationRejectsForeignNamespacesDuplicatesTooManyLayersAndBadIds() {
        assertThrows(IllegalArgumentException.class, () -> MapLayerRegistry.register(
                new FakeProvider("fake", List.of(layer("other", "a")), (v, d, b, s) -> { })));
        assertThrows(IllegalArgumentException.class, () -> MapLayerRegistry.register(
                new FakeProvider("fake", List.of(layer("fake", "a"), layer("fake", "a")), (v, d, b, s) -> { })));
        List<MapLayerInfo> seventeen = new ArrayList<>();
        for (int i = 0; i <= MapLayerRegistry.MAX_LAYERS_PER_PROVIDER; i++) {
            seventeen.add(layer("fake", "l" + i));
        }
        assertThrows(IllegalArgumentException.class,
                () -> MapLayerRegistry.register(new FakeProvider("fake", seventeen, (v, d, b, s) -> { })));
        assertThrows(IllegalArgumentException.class,
                () -> MapLayerRegistry.register(new FakeProvider("Bad Mod", List.of(), (v, d, b, s) -> { })));
        assertTrue(MapLayerRegistry.providers().isEmpty());
    }

    @Test
    void reRegisteringReplacesTheProvider() {
        MapLayerRegistry.register(new FakeProvider("fake", List.of(layer("fake", "a")), (v, d, b, s) -> { }));
        FakeProvider second = new FakeProvider("fake", List.of(layer("fake", "b")), (v, d, b, s) -> { });
        MapLayerRegistry.register(second);
        assertEquals(1, MapLayerRegistry.providers().size());
        assertEquals(List.of(id("fake", "b")), MapLayerRegistry.layers().stream().map(MapLayerInfo::id).toList());
    }

    @Test
    void unknownModAndNullArgumentsReturnAnEmptyResult() {
        assertSame(MapQueryResult.empty(), MapLayerRegistry.query("nobody", VIEWER, OVERWORLD, NEAR));
        MapLayerRegistry.register(new FakeProvider("fake", List.of(layer("fake", "a")), (v, d, b, s) -> { }));
        assertSame(MapQueryResult.empty(), MapLayerRegistry.query("fake", null, OVERWORLD, NEAR));
        assertSame(MapQueryResult.empty(), MapLayerRegistry.query("fake", VIEWER, null, NEAR));
        assertSame(MapQueryResult.empty(), MapLayerRegistry.query("fake", VIEWER, OVERWORLD, null));
    }

    @Test
    void aProviderCannotWriteIntoAnotherModsLayer() {
        Identifier victim = id("victim", "fields");
        MapLayerRegistry.register(new FakeProvider("victim", List.of(layer("victim", "fields")), (v, d, b, s) -> { }));
        MapLayerRegistry.register(new FakeProvider("rogue", List.of(layer("rogue", "own")), (v, d, b, s) -> {
            for (int i = 0; i < 1000; i++) {
                s.marker(marker(victim, "x" + i));
            }
            s.marker(marker(id("rogue", "own"), "fine"));
        }));
        MapQueryResult r = MapLayerRegistry.query("rogue", VIEWER, OVERWORLD, NEAR);
        assertEquals(1, r.markers().size());
        assertEquals(1000, r.rejected(Reject.UNKNOWN_LAYER));
        assertTrue(MapLayerRegistry.query("victim", VIEWER, OVERWORLD, NEAR).markers().isEmpty());
    }

    @Test
    void aThrowingProviderYieldsAFailedEmptyResultAndOthersStillWork() {
        Identifier a = id("boom", "a");
        MapLayerRegistry.register(new FakeProvider("boom", List.of(layer("boom", "a")), (v, d, b, s) -> {
            s.marker(marker(a, "partial"));
            throw new IllegalStateException("provider bug");
        }));
        Identifier ok = id("calm", "a");
        MapLayerRegistry.register(new FakeProvider("calm", List.of(layer("calm", "a")),
                (v, d, b, s) -> s.marker(marker(ok, "one"))));
        MapQueryResult failed = MapLayerRegistry.query("boom", VIEWER, OVERWORLD, NEAR);
        assertTrue(failed.failed());
        assertTrue(failed.markers().isEmpty(), "partial output is discarded");
        MapQueryResult fine = MapLayerRegistry.query("calm", VIEWER, OVERWORLD, NEAR);
        assertFalse(fine.failed());
        assertEquals(1, fine.markers().size());
    }

    @Test
    void aSlowProviderReportsItsTimeAndDoesNotSlowOthers() {
        MapLayerRegistry.register(new FakeProvider("slow", List.of(layer("slow", "a")), (v, d, b, s) -> {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        MapLayerRegistry.register(new FakeProvider("quick", List.of(layer("quick", "a")), (v, d, b, s) -> { }));
        MapQueryResult slow = MapLayerRegistry.query("slow", VIEWER, OVERWORLD, NEAR);
        MapQueryResult quick = MapLayerRegistry.query("quick", VIEWER, OVERWORLD, NEAR);
        assertTrue(slow.elapsedNanos() >= 25_000_000L, "elapsed time lets the consumer enforce a budget");
        assertTrue(quick.elapsedNanos() < slow.elapsedNanos());
    }

    @Test
    void aKeptSinkCannotWriteAfterTheQueryReturns() {
        Identifier a = id("keeper", "a");
        AtomicReference<MapLayerSink> kept = new AtomicReference<>();
        MapLayerRegistry.register(new FakeProvider("keeper", List.of(layer("keeper", "a")), (v, d, b, s) -> {
            kept.set(s);
            s.marker(marker(a, "in"));
        }));
        MapQueryResult r = MapLayerRegistry.query("keeper", VIEWER, OVERWORLD, NEAR);
        assertFalse(kept.get().marker(marker(a, "late")));
        assertEquals(1, r.markers().size());
        assertThrows(UnsupportedOperationException.class, () -> r.markers().add(marker(a, "x")));
    }

    @Test
    void wideBoundsAreClampedBeforeTheProviderSeesThem() {
        AtomicReference<MapBounds> seen = new AtomicReference<>();
        MapLayerRegistry.register(new FakeProvider("wide", List.of(layer("wide", "a")), (v, d, b, s) -> seen.set(b)));
        MapLayerRegistry.query("wide", VIEWER, OVERWORLD, new MapBounds(-1_000_000, -1_000_000, 1_000_000, 1_000_000));
        assertEquals(MapBounds.MAX_SPAN, seen.get().spanX());
        assertEquals(MapBounds.MAX_SPAN, seen.get().spanZ());
    }

    @Test
    void dimensionBadgesAreGuardedFilteredDedupedAndCapped() {
        FakeProvider p = new FakeProvider("fake", List.of(), (v, d, b, s) -> { });
        p.badges = v -> {
            List<MapDimensionBadge> out = new ArrayList<>();
            out.add(new MapDimensionBadge(id("other", "spoof"), OVERWORLD, ICON, null));
            for (int i = 0; i < 12; i++) {
                out.add(new MapDimensionBadge(id("fake", "b" + (i % 10)), OVERWORLD, ICON, null));
            }
            return out;
        };
        MapLayerRegistry.register(p);
        List<MapDimensionBadge> badges = MapLayerRegistry.dimensionBadges("fake", VIEWER);
        assertEquals(MapDimensionBadge.MAX_PER_PROVIDER, badges.size());
        assertTrue(badges.stream().allMatch(b -> b.id().getNamespace().equals("fake")));
        p.badges = v -> {
            throw new IllegalStateException("badge bug");
        };
        assertTrue(MapLayerRegistry.dimensionBadges("fake", VIEWER).isEmpty());
        assertTrue(MapLayerRegistry.dimensionBadges("nobody", VIEWER).isEmpty());
    }

    @Test
    void fakeProviderRoundTrip() {
        Identifier stations = id("fake", "stations");
        Identifier zones = id("fake", "zones");
        MapLayerRegistry.register(new FakeProvider("fake", List.of(MapLayerInfo.of(stations, "map.s", ICON,
                MapScope.MEMBERS), MapLayerInfo.of(zones, "map.z", ICON, MapScope.OWNER).withDefaults(true, false)),
                (v, d, b, s) -> {
                    s.marker(MapMarker.builder("station.1", stations, d, 10, 70, 10)
                            .label(MapLabel.literal("Home Base")).detail(MapLabel.translatable("map.fake.crew", "3"))
                            .build());
                    s.area(new MapArea.Circle("zone.1", zones, d, MapAreaStyle.of(0x4400FF00, 0xFF00FF00), 0, 0, 32));
                }));
        MapQueryResult r = MapLayerRegistry.query("fake", VIEWER, OVERWORLD, NEAR);
        assertFalse(r.failed());
        assertEquals("Home Base", r.markers().get(0).label().text());
        assertEquals(List.of("3"), r.markers().get(0).details().get(0).args());
        assertEquals(1, r.areas().size());
        assertTrue(r.truncatedLayers().isEmpty());
        assertEquals(2, MapLayerRegistry.layers().size());
        assertFalse(MapLayerRegistry.layer(zones).orElseThrow().defaultOnMinimap());
    }
}
