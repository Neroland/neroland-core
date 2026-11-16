package za.co.neroland.nerolandcore.map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static za.co.neroland.nerolandcore.map.MapTestSupport.MOON;
import static za.co.neroland.nerolandcore.map.MapTestSupport.OVERWORLD;
import static za.co.neroland.nerolandcore.map.MapTestSupport.id;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.junit.jupiter.api.Test;

import za.co.neroland.nerolandcore.map.MapLayerConformance.Report;
import za.co.neroland.nerolandcore.map.MapTestSupport.Entry;

/**
 * The conformance fixture passes an honest provider covering OWNER, PROXIMITY and PUBLIC layers, and catches
 * each deliberate leak: a roster, bounds-measured proximity, a mixed layer, identity in a label, a provider
 * that returns nothing, an exception, an absent dimension that is not absent, and a bad test setup.
 */
class MapLayerConformanceTest {

    private static final Identifier ICON = id("fakemod", "icon");
    private static final Identifier OWN = id("fakemod", "own");
    private static final Identifier PADS = id("fakemod", "pads");
    private static final Identifier EVENTS = id("fakemod", "events");

    private static final UUID ENTITLED = UUID.randomUUID();
    private static final UUID STRANGER = UUID.randomUUID();
    private static final UUID THIRD = UUID.randomUUID();

    private static final List<Entry> STORE = List.of(
            new Entry("own.base", ENTITLED, OVERWORLD, 10, 10, "Home Base"),
            new Entry("third.base", THIRD, OVERWORLD, 20, 20, "Trudy's Base"),
            new Entry("pad.near", null, OVERWORLD, 30, 0, "Pad"),
            new Entry("pad.far", null, OVERWORLD, 300, 0, "Pad"));

    private static final Set<String> PUBLIC_EVENTS = Set.of("event.storm");

    /** Deliberate defects the fake provider can be told to commit. */
    enum Defect {
        ROSTER, PROXIMITY_FROM_BOUNDS, MIXED_OWNER_LAYER, LABEL_LEAK, UUID_IN_ID, OWN_EMPTY, THROWS, IGNORES_DIMENSION
    }

    private static MapLayerProvider provider(Set<Defect> defects) {
        List<MapLayerInfo> layers = List.of(
                MapLayerInfo.of(OWN, "map.fakemod.own", ICON, MapScope.OWNER),
                MapLayerInfo.proximityBuilt(PADS, "map.fakemod.pads", ICON),
                MapLayerInfo.of(EVENTS, "map.fakemod.events", ICON, MapScope.PUBLIC));
        return new MapTestSupport.FakeProvider("fakemod", layers, (viewer, dimension, bounds, sink) -> {
            if (defects.contains(Defect.THROWS)) {
                throw new IllegalStateException("bug");
            }
            boolean known = dimension.equals(OVERWORLD) || defects.contains(Defect.IGNORES_DIMENSION);
            if (!known) {
                return;
            }
            for (Entry e : STORE) {
                if (e.owner() != null) {
                    boolean mine = e.owner().equals(viewer.id());
                    if ((mine && !defects.contains(Defect.OWN_EMPTY)) || defects.contains(Defect.ROSTER)) {
                        String markerId = defects.contains(Defect.UUID_IN_ID) && mine ? "own." + e.owner() : e.id();
                        MapMarker.Builder b = MapMarker.builder(markerId, OWN, e.dimension(), e.x(), 64, e.z())
                                .label(MapLabel.literal(e.name()));
                        if (defects.contains(Defect.LABEL_LEAK)) {
                            b.detail(MapLabel.translatable("map.fakemod.visited_by", "Trudy"));
                        }
                        sink.marker(b.build());
                    }
                } else {
                    boolean near = defects.contains(Defect.PROXIMITY_FROM_BOUNDS)
                            ? MapViewer.forTest(viewer.id(), null, dimension,
                                    new net.minecraft.core.BlockPos(bounds.centreX(), 64, bounds.centreZ()))
                                    .within(dimension, e.x(), e.z(), MapLayerInfo.BUILT_PROXIMITY)
                            : viewer.within(dimension, e.x(), e.z(), MapLayerInfo.BUILT_PROXIMITY);
                    if (near) {
                        sink.marker(MapMarker.builder(e.id(), PADS, e.dimension(), e.x(), 64, e.z()).build());
                        if (defects.contains(Defect.MIXED_OWNER_LAYER)) {
                            sink.marker(MapMarker.builder("mixed." + e.id(), OWN, e.dimension(), e.x(), 64, e.z())
                                    .build());
                        }
                    }
                }
            }
            for (String event : PUBLIC_EVENTS) {
                sink.area(new MapArea.Circle(event, EVENTS, OVERWORLD, MapAreaStyle.of(0x44FF0000, 0xFFFF0000), 40,
                        40, 24));
            }
        });
    }

    private static MapLayerConformance harness(Set<Defect> defects) {
        return harness(provider(defects), 5, 0);
    }

    private static MapLayerConformance harness(MapLayerProvider p, int strangerX, int strangerZ) {
        ResourceKey<Level> present = OVERWORLD;
        return MapLayerConformance.create(p)
                .viewers(MapTestSupport.online(ENTITLED, present, 0, 0),
                        MapTestSupport.online(STRANGER, present, strangerX, strangerZ))
                .dimensions(present, MOON)
                .bounds(MapBounds.around(0, 0, 512))
                .farBounds(MapBounds.around(20_000, 20_000, 256))
                .forbiddenText("Trudy", "Sam")
                .forbiddenIds(THIRD)
                .expectEntitledSees(OWN, 1)
                .expectEntitledSees(PADS, 1)
                .expectEntitledSees(EVENTS, 1);
    }

    private static Report run(Defect defect) {
        return harness(EnumSet.of(defect)).run();
    }

    private static boolean mentions(Report r, String text) {
        return r.failures().stream().anyMatch(f -> f.contains(text));
    }

    @Test
    void anHonestProviderPasses() {
        Report r = harness(EnumSet.noneOf(Defect.class)).run();
        assertTrue(r.passed(), () -> String.join("\n", r.failures()));
        harness(EnumSet.noneOf(Defect.class)).verify();
    }

    @Test
    void aRosterFailsTheStrangerChecks() {
        Report r = run(Defect.ROSTER);
        assertFalse(r.passed());
        assertTrue(mentions(r, "stranger beside received"));
        assertTrue(mentions(r, "(G5)"), "the roster also shows other players' names");
    }

    @Test
    void proximityMeasuredFromTheBoundsFails() {
        Report r = run(Defect.PROXIMITY_FROM_BOUNDS);
        assertFalse(r.passed());
        assertTrue(mentions(r, "stranger far received"));
        assertTrue(mentions(r, "outside the proximity radius"));
    }

    @Test
    void anOwnerLayerThatAlsoEmitsUnownedThingsFails() {
        Report r = run(Defect.MIXED_OWNER_LAYER);
        assertFalse(r.passed());
        assertTrue(mentions(r, "layer " + OWN + " (OWNER): stranger beside received"));
    }

    @Test
    void identityInALabelOrIdFails() {
        assertTrue(mentions(run(Defect.LABEL_LEAK), "(G5)"));
        assertTrue(mentions(run(Defect.UUID_IN_ID), "(G5)"));
        Report r = run(Defect.UUID_IN_ID);
        assertTrue(r.failures().stream().noneMatch(f -> f.contains(ENTITLED.toString())),
                "the report never echoes a player id");
    }

    @Test
    void aProviderThatReturnsNothingCannotPassVacuously() {
        assertTrue(mentions(run(Defect.OWN_EMPTY), "expected at least 1"));
        Report declared = harness(provider(EnumSet.of(Defect.OWN_EMPTY)), 5, 0).expectEmpty(OWN).run();
        assertTrue(mentions(declared, "both expectEntitledSees and expectEmpty"));
    }

    @Test
    void expectEmptyIsAllowedButNoted() {
        MapLayerProvider p = provider(EnumSet.of(Defect.OWN_EMPTY));
        Report r = MapLayerConformance.create(p)
                .viewers(MapTestSupport.online(ENTITLED, OVERWORLD, 0, 0), MapTestSupport.online(STRANGER, OVERWORLD, 5, 0))
                .dimensions(OVERWORLD, MOON).bounds(MapBounds.around(0, 0, 512))
                .farBounds(MapBounds.around(20_000, 20_000, 256))
                .expectEmpty(OWN).expectEntitledSees(PADS, 1).expectEntitledSees(EVENTS, 1).run();
        assertTrue(r.passed(), () -> String.join("\n", r.failures()));
        assertTrue(r.notes().stream().anyMatch(n -> n.contains("expectEmpty")));
    }

    @Test
    void aMissingExpectationFails() {
        MapLayerProvider p = provider(EnumSet.noneOf(Defect.class));
        Report r = MapLayerConformance.create(p)
                .viewers(MapTestSupport.online(ENTITLED, OVERWORLD, 0, 0), MapTestSupport.online(STRANGER, OVERWORLD, 5, 0))
                .dimensions(OVERWORLD, MOON).bounds(MapBounds.around(0, 0, 512))
                .farBounds(MapBounds.around(20_000, 20_000, 256)).expectEntitledSees(OWN, 1).run();
        assertTrue(mentions(r, "no expectEntitledSees or expectEmpty"));
    }

    @Test
    void aThrowingProviderFails() {
        Report r = run(Defect.THROWS);
        assertTrue(mentions(r, "the provider threw"));
        assertThrows(AssertionError.class, () -> harness(EnumSet.of(Defect.THROWS)).verify());
    }

    @Test
    void anAbsentDimensionThatReturnsDataFails() {
        Report r = run(Defect.IGNORES_DIMENSION);
        assertTrue(mentions(r, "WRONG_DIMENSION"));
    }

    @Test
    void aStrangerWhoIsNotBesideOrFarBoundsThatAreNearAreSetupFailures() {
        Report apart = harness(provider(EnumSet.noneOf(Defect.class)), 100, 0).run();
        assertTrue(mentions(apart, "within 8 blocks"));
        Report near = harness(EnumSet.noneOf(Defect.class)).farBounds(MapBounds.around(1000, 0, 16)).run();
        assertTrue(mentions(near, "at least 5000 blocks"));
    }

    @Test
    void invalidLayerDeclarationsFail() {
        MapLayerProvider foreign = new MapTestSupport.FakeProvider("fakemod",
                List.of(MapLayerInfo.of(id("other", "x"), "map.x", ICON, MapScope.OWNER)), (v, d, b, s) -> { });
        assertTrue(mentions(MapLayerConformance.create(foreign).run(), "layer declarations are invalid"));
        MapLayerProvider none = new MapTestSupport.FakeProvider("fakemod", List.of(), (v, d, b, s) -> { });
        assertTrue(mentions(MapLayerConformance.create(none).run(), "declares no layers"));
    }
}
