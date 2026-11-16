package za.co.neroland.nerolandcore.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static za.co.neroland.nerolandcore.map.MapTestSupport.MOON;
import static za.co.neroland.nerolandcore.map.MapTestSupport.OVERWORLD;
import static za.co.neroland.nerolandcore.map.MapTestSupport.id;

import java.util.List;

import net.minecraft.resources.Identifier;

import org.junit.jupiter.api.Test;

import za.co.neroland.nerolandcore.map.MapLayerSink.Reject;

/** Caps, payload budgets, reject reasons and sealing of the capped sink. */
class MapLayerSinkTest {

    private static final Identifier A = id("fake", "a");
    private static final Identifier B = id("fake", "b");
    private static final Identifier ICON = id("fake", "icon");
    private static final MapBounds NEAR = MapBounds.around(0, 0, 1000);

    private static MapLayerSink sink(int capA) {
        return MapLayerSink.create(List.of(MapLayerInfo.of(A, "map.a", ICON, MapScope.OWNER).withCap(capA),
                MapLayerInfo.of(B, "map.b", ICON, MapScope.OWNER)), OVERWORLD, NEAR);
    }

    private static MapMarker marker(Identifier layer, String id, int x, int z) {
        return MapMarker.builder(id, layer, OVERWORLD, x, 64, z).build();
    }

    @Test
    void capHoldsPerLayerAndMarksTheLayerTruncated() {
        MapLayerSink sink = sink(3);
        for (int i = 0; i < 10; i++) {
            sink.marker(marker(A, "m" + i, i, 0));
        }
        assertTrue(sink.isFull(A));
        assertFalse(sink.isFull(B));
        assertTrue(sink.marker(marker(B, "m0", 0, 0)), "ids are per layer and B has its own cap");
        sink.seal();
        MapQueryResult r = sink.result(0);
        assertEquals(4, r.markers().size());
        assertEquals(7, r.rejected(Reject.CAP));
        assertEquals(java.util.Set.of(A), r.truncatedLayers());
    }

    @Test
    void everyRejectReasonIsCounted() {
        MapLayerSink sink = sink(10);
        assertFalse(sink.marker(marker(id("other", "layer"), "x", 0, 0)));
        assertFalse(sink.marker(MapMarker.builder("x", A, MOON, 0, 0, 0).build()));
        assertFalse(sink.marker(marker(A, "far", 5000, 0)));
        assertTrue(sink.marker(marker(A, "dup", 0, 0)));
        assertFalse(sink.area(new MapArea.Circle("dup", A, OVERWORLD, null, 0, 0, 5)));
        assertFalse(sink.marker(null));
        MapQueryResult r = sink.result(0);
        assertEquals(2, r.rejected(Reject.UNKNOWN_LAYER));
        assertEquals(1, r.rejected(Reject.WRONG_DIMENSION));
        assertEquals(1, r.rejected(Reject.OUT_OF_BOUNDS));
        assertEquals(1, r.rejected(Reject.DUPLICATE_ID));
        assertEquals(0, r.rejected(Reject.CAP));
    }

    @Test
    void areasIntersectingTheBoundsAreAcceptedAndShareTheCap() {
        MapLayerSink sink = sink(2);
        assertTrue(sink.area(new MapArea.Rect("edge", A, OVERWORLD, null, 990, 0, 1500, 10)));
        assertFalse(sink.area(new MapArea.Rect("out", A, OVERWORLD, null, 1001, 0, 1500, 10)));
        assertTrue(sink.marker(marker(A, "m", 0, 0)));
        assertFalse(sink.area(new MapArea.Circle("c", A, OVERWORLD, null, 0, 0, 5)));
        MapQueryResult r = sink.result(0);
        assertEquals(1, r.areas().size());
        assertEquals(1, r.rejected(Reject.CAP));
    }

    @Test
    void chunkAndVertexBudgetsHoldPerLayer() {
        MapLayerSink sink = sink(MapLayerInfo.HARD_CAP);
        int accepted = 0;
        for (int i = 0; i < 20; i++) {
            long[] chunks = new long[MapArea.Chunks.MAX_CHUNKS];
            for (int c = 0; c < chunks.length; c++) {
                chunks[c] = MapArea.Chunks.pack(c % 32, c / 32);
            }
            if (sink.area(new MapArea.Chunks("c" + i, A, OVERWORLD, null, chunks))) {
                accepted++;
            }
        }
        assertEquals(MapLayerSink.MAX_CHUNKS_PER_LAYER / MapArea.Chunks.MAX_CHUNKS, accepted);
        int polygons = 0;
        int[] square64 = new int[MapArea.Polygon.MAX_VERTICES * 2];
        for (int v = 0; v < MapArea.Polygon.MAX_VERTICES; v++) {
            square64[v * 2] = v;
            square64[v * 2 + 1] = v % 2;
        }
        for (int i = 0; i < 50; i++) {
            if (sink.area(new MapArea.Polygon("p" + i, A, OVERWORLD, null, square64))) {
                polygons++;
            }
        }
        assertEquals(MapLayerSink.MAX_VERTICES_PER_LAYER / MapArea.Polygon.MAX_VERTICES, polygons);
        assertTrue(sink.result(0).truncatedLayers().contains(A));
    }

    @Test
    void aSealedSinkRefusesLateWrites() {
        MapLayerSink sink = sink(10);
        assertTrue(sink.marker(marker(A, "early", 0, 0)));
        sink.seal();
        assertFalse(sink.marker(marker(A, "late", 0, 0)));
        assertFalse(sink.area(new MapArea.Circle("late2", A, OVERWORLD, null, 0, 0, 5)));
        assertFalse(sink.isFull(A));
        assertEquals(1, sink.result(0).markers().size());
        assertEquals(0, sink.result(0).rejected().size());
    }
}
