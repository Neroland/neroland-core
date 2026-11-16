package za.co.neroland.nerolandcore.map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static za.co.neroland.nerolandcore.map.MapTestSupport.MOON;
import static za.co.neroland.nerolandcore.map.MapTestSupport.OVERWORLD;
import static za.co.neroland.nerolandcore.map.MapTestSupport.id;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import org.junit.jupiter.api.Test;

/** Validation, clamping and sanitising rules of the map API's value types. */
class MapTypesTest {

    private static final Identifier LAYER = id("fake", "things");
    private static final Identifier ICON = id("fake", "icon");

    @Test
    void labelStripsControlFormattingAndBidiCharacters() {
        MapLabel label = MapLabel.literal("A\u00A7cB\nC\u202ED\u2066E\u0007F");
        assertEquals("ABCDEF", label.text());
        MapLabel arg = MapLabel.translatable("map.fake.key", "x\u202Ey\r\nz");
        assertEquals(List.of("xyz"), arg.args());
    }

    @Test
    void labelClampsLiteralAndArgumentsAndDropsExtraArguments() {
        MapLabel literal = MapLabel.literal("x".repeat(200));
        assertEquals(MapLabel.MAX_LITERAL, literal.text().length());
        MapLabel many = MapLabel.translatable("map.fake.key", "a".repeat(100), "b", "c", "d", "e", "f");
        assertEquals(MapLabel.MAX_ARGS, many.args().size());
        assertEquals(MapLabel.MAX_ARG, many.args().get(0).length());
        assertTrue(literal.args().isEmpty());
    }

    @Test
    void labelRejectsABadKey() {
        assertThrows(IllegalArgumentException.class, () -> MapLabel.translatable("Has Spaces"));
        assertThrows(IllegalArgumentException.class, () -> MapLabel.translatable(""));
        assertThrows(IllegalArgumentException.class, () -> MapLabel.translatable("a".repeat(129)));
    }

    @Test
    void layerInfoValidatesScopeRadiusSchemaAndKey() {
        assertThrows(IllegalArgumentException.class, () -> MapLayerInfo.of(LAYER, "map.k", ICON, MapScope.PROXIMITY));
        assertThrows(IllegalArgumentException.class,
                () -> new MapLayerInfo(LAYER, "map.k", ICON, true, true, MapScope.OWNER, 5, 1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new MapLayerInfo(LAYER, "map.k", ICON, true, true, MapScope.PROXIMITY, 129, 1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new MapLayerInfo(LAYER, "map.k", ICON, true, true, MapScope.OWNER, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> MapLayerInfo.of(LAYER, "Bad Key", ICON, MapScope.OWNER));
        assertThrows(IllegalArgumentException.class, () -> MapLayerInfo.of(LAYER, "map.k", null, MapScope.OWNER));
        MapLayerInfo info = MapLayerInfo.of(LAYER, "map.k", ICON, MapScope.OWNER);
        assertEquals("fake", info.modId());
        assertEquals(MapLayerInfo.DEFAULT_CAP, info.maxPerQuery());
        assertEquals(MapLayerInfo.HARD_CAP, info.withCap(100_000).maxPerQuery());
        assertEquals(1, info.withCap(-3).withCap(1).maxPerQuery());
        assertEquals(3, info.withSchema(3).schema());
        assertThrows(IllegalArgumentException.class, () -> info.withProximityRadius(10));
    }

    @Test
    void proximityFactoriesCarryTheG4Defaults() {
        MapLayerInfo built = MapLayerInfo.proximityBuilt(LAYER, "map.k", ICON);
        assertEquals(MapLayerInfo.BUILT_PROXIMITY, built.proximityRadius());
        assertFalse(built.defaultOnMinimap());
        MapLayerInfo hazard = MapLayerInfo.proximityHazard(LAYER, "map.k", ICON);
        assertEquals(MapLayerInfo.HAZARD_PROXIMITY, hazard.proximityRadius());
        assertTrue(hazard.defaultOnMinimap());
        assertEquals(48, built.withProximityRadius(48).proximityRadius());
        MapLayerInfo worldOnly = built.withDefaults(true, false);
        assertTrue(worldOnly.defaultVisible());
    }

    @Test
    void markerValidatesIdAndDropsExtraDetails() {
        assertThrows(IllegalArgumentException.class, () -> MapMarker.builder("Bad Id", LAYER, OVERWORLD, 0, 0, 0).build());
        assertThrows(IllegalArgumentException.class, () -> MapMarker.builder("ok", null, OVERWORLD, 0, 0, 0).build());
        MapMarker.Builder b = MapMarker.builder("ok", LAYER, OVERWORLD, new BlockPos(1, 2, 3)).argb(0xFF00FF00);
        for (int i = 0; i < 7; i++) {
            b.detail(MapLabel.literal("d" + i));
        }
        MapMarker m = b.build();
        assertEquals(MapMarker.MAX_DETAILS, m.details().size());
        assertEquals(0xFF00FF00, m.argb());
        assertEquals(3, m.z());
    }

    @Test
    void rectIsNormalisedAndSpanLimited() {
        MapArea.Rect r = new MapArea.Rect("r", LAYER, OVERWORLD, null, 10, 10, -10, -10);
        assertEquals(-10, r.minX());
        assertEquals(new MapBounds(-10, -10, 10, 10), r.bounds());
        assertThrows(IllegalArgumentException.class,
                () -> new MapArea.Rect("r", LAYER, OVERWORLD, null, 0, 0, MapBounds.MAX_SPAN, 1));
    }

    @Test
    void circleRadiusIsClamped() {
        assertEquals(1, new MapArea.Circle("c", LAYER, OVERWORLD, null, 0, 0, -5).radius());
        assertEquals(MapArea.Circle.MAX_RADIUS,
                new MapArea.Circle("c", LAYER, OVERWORLD, null, 0, 0, 1_000_000).radius());
    }

    @Test
    void chunkPackingRoundTripsAndEqualityComparesContents() {
        long p = MapArea.Chunks.pack(-3, 7);
        assertEquals(-3, MapArea.Chunks.chunkX(p));
        assertEquals(7, MapArea.Chunks.chunkZ(p));
        long q = MapArea.Chunks.pack(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertEquals(Integer.MAX_VALUE, MapArea.Chunks.chunkX(q));
        assertEquals(Integer.MIN_VALUE, MapArea.Chunks.chunkZ(q));
        long[] raw = {MapArea.Chunks.pack(0, 0), MapArea.Chunks.pack(1, 0)};
        MapArea.Chunks a = new MapArea.Chunks("c", LAYER, OVERWORLD, null, raw);
        MapArea.Chunks b = new MapArea.Chunks("c", LAYER, OVERWORLD, null, raw.clone());
        raw[0] = 99;
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotSame(a.chunks(), a.chunks());
        assertEquals(new MapBounds(0, 0, 31, 15), a.bounds());
        assertThrows(IllegalArgumentException.class,
                () -> new MapArea.Chunks("c", LAYER, OVERWORLD, null, new long[MapArea.Chunks.MAX_CHUNKS + 1]));
        assertThrows(IllegalArgumentException.class,
                () -> new MapArea.Chunks("c", LAYER, OVERWORLD, null, new long[0]));
        assertThrows(IllegalArgumentException.class, () -> new MapArea.Chunks("c", LAYER, OVERWORLD, null,
                new long[] {MapArea.Chunks.pack(0, 0), MapArea.Chunks.pack(2000, 0)}));
    }

    @Test
    void polygonChecksVerticesAndCopiesItsArray() {
        int[] xz = {0, 0, 10, 0, 10, 10};
        MapArea.Polygon p = new MapArea.Polygon("p", LAYER, OVERWORLD, null, xz);
        xz[0] = 500;
        assertArrayEquals(new int[] {0, 0, 10, 0, 10, 10}, p.xz());
        assertEquals(3, p.vertices());
        assertEquals(p, new MapArea.Polygon("p", LAYER, OVERWORLD, null, new int[] {0, 0, 10, 0, 10, 10}));
        assertThrows(IllegalArgumentException.class,
                () -> new MapArea.Polygon("p", LAYER, OVERWORLD, null, new int[] {0, 0, 1, 1}));
        assertThrows(IllegalArgumentException.class,
                () -> new MapArea.Polygon("p", LAYER, OVERWORLD, null, new int[] {0, 0, 1, 1, 2}));
        assertThrows(IllegalArgumentException.class,
                () -> new MapArea.Polygon("p", LAYER, OVERWORLD, null, new int[(MapArea.Polygon.MAX_VERTICES + 1) * 2]));
    }

    @Test
    void boundsClampSpanAndNearestPoint() {
        MapBounds wide = new MapBounds(-100_000, -10, 100_000, 10);
        MapBounds clamped = wide.clampSpan();
        assertEquals(MapBounds.MAX_SPAN, clamped.spanX());
        assertEquals(21, clamped.spanZ());
        assertTrue(clamped.contains(0, 0));
        MapBounds box = new MapBounds(0, 0, 10, 10);
        assertEquals(10, box.nearestX(50));
        assertEquals(0, box.nearestZ(-50));
        assertTrue(box.intersects(new MapBounds(10, 10, 20, 20)));
        assertFalse(box.intersects(new MapBounds(11, 0, 20, 20)));
        assertEquals(new MapBounds(-5, -5, 5, 5), MapBounds.around(0, 0, 5));
    }

    @Test
    void viewerWithinIsHorizontalSameDimensionAndOnlineOnly() {
        UUID id = UUID.randomUUID();
        MapViewer v = MapTestSupport.online(id, OVERWORLD, 0, 0);
        assertTrue(v.online());
        assertTrue(v.within(OVERWORLD, 30, 40, 50));
        assertFalse(v.within(OVERWORLD, 30, 41, 50));
        assertFalse(v.within(MOON, 0, 0, 50));
        assertTrue(v.within(OVERWORLD, new MapBounds(40, -5, 100, 5), 40));
        assertFalse(v.within(OVERWORLD, new MapBounds(41, -5, 100, 5), 40));
        MapViewer off = MapViewer.forTest(id, null, null, null);
        assertFalse(off.online());
        assertFalse(off.within(OVERWORLD, 0, 0, 1000));
        assertEquals("MapViewer[online]", v.toString());
        assertEquals("MapViewer[offline]", off.toString());
        assertFalse(v.toString().contains(id.toString()));
    }
}
