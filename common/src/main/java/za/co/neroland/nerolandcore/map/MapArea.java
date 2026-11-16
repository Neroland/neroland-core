package za.co.neroland.nerolandcore.map;

import java.util.Arrays;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * A shaped region on a map layer (Core 1.19.0): a {@link Rect}, {@link Circle}, {@link Chunks chunk set} or
 * simple {@link Polygon}. Block coordinates; areas have no height in this version (a height band is a
 * later, additive decision, not an ad hoc field).
 *
 * <p>Validation: ids follow {@link MapMarker#ID_PATTERN}; layer and dimension are non-null; every shape's
 * {@link #bounds()} spans at most {@link MapBounds#MAX_SPAN} blocks per axis; polygon vertex and chunk
 * counts are checked. Those are programmer errors and throw {@link IllegalArgumentException}. A circle's
 * radius is data-derived and clamped. A null style becomes {@code MapAreaStyle.of(0, 0)}.
 *
 * <p>Areas share their layer's per-query cap with markers, and the sink also budgets the total chunks and
 * vertices per layer ({@link MapLayerSink#MAX_CHUNKS_PER_LAYER}, {@link MapLayerSink#MAX_VERTICES_PER_LAYER}).
 */
public sealed interface MapArea permits MapArea.Rect, MapArea.Circle, MapArea.Chunks, MapArea.Polygon {

    /** Stable id within the layer (markers and areas share one id space per layer). */
    String id();

    /** The layer this area belongs to. */
    Identifier layer();

    /** The dimension it is in. */
    ResourceKey<Level> dimension();

    /** How it is drawn. */
    MapAreaStyle style();

    /** The enclosing box, used for the sink's bounds check and proximity. */
    MapBounds bounds();

    private static MapAreaStyle orDefault(MapAreaStyle style) {
        return style == null ? MapAreaStyle.of(0, 0) : style;
    }

    private static void requireSpan(MapBounds b, String what) {
        if (b.spanX() > MapBounds.MAX_SPAN || b.spanZ() > MapBounds.MAX_SPAN) {
            throw new IllegalArgumentException("A map " + what + " may span at most " + MapBounds.MAX_SPAN
                    + " blocks per axis");
        }
    }

    /** An axis-aligned rectangle, inclusive; corners are normalised. */
    record Rect(String id, Identifier layer, ResourceKey<Level> dimension, MapAreaStyle style, int minX, int minZ,
            int maxX, int maxZ) implements MapArea {

        public Rect {
            MapMarker.requireItem(id, layer, dimension, "area");
            style = MapArea.orDefault(style);
            MapBounds b = new MapBounds(minX, minZ, maxX, maxZ);
            minX = b.minX();
            minZ = b.minZ();
            maxX = b.maxX();
            maxZ = b.maxZ();
            MapArea.requireSpan(b, "rect");
        }

        @Override
        public MapBounds bounds() {
            return new MapBounds(minX, minZ, maxX, maxZ);
        }
    }

    /** A disc; {@code radius} is clamped to {@code 1..MAX_RADIUS}. */
    record Circle(String id, Identifier layer, ResourceKey<Level> dimension, MapAreaStyle style, int centreX,
            int centreZ, int radius) implements MapArea {

        /** Largest radius, in blocks. */
        public static final int MAX_RADIUS = 4096;

        public Circle {
            MapMarker.requireItem(id, layer, dimension, "area");
            style = MapArea.orDefault(style);
            radius = Math.max(1, Math.min(MAX_RADIUS, radius));
        }

        @Override
        public MapBounds bounds() {
            return MapBounds.around(centreX, centreZ, radius);
        }
    }

    /**
     * A set of whole chunks, packed with {@link #pack} (x in the low 32 bits, z in the high 32), so callers
     * never need {@code ChunkPos}, whose helpers differ between Minecraft versions. 1..{@link #MAX_CHUNKS}
     * entries; the array is cloned in and out. Equality compares contents.
     */
    record Chunks(String id, Identifier layer, ResourceKey<Level> dimension, MapAreaStyle style, long[] chunks)
            implements MapArea {

        /** Most chunks in one area. */
        public static final int MAX_CHUNKS = 1024;

        public Chunks {
            MapMarker.requireItem(id, layer, dimension, "area");
            style = MapArea.orDefault(style);
            if (chunks == null || chunks.length < 1 || chunks.length > MAX_CHUNKS) {
                throw new IllegalArgumentException("A chunk area holds 1.." + MAX_CHUNKS + " chunks");
            }
            chunks = chunks.clone();
            MapArea.requireSpan(boundsOf(chunks), "chunk area");
        }

        /** Pack a chunk position: x in the low 32 bits, z in the high 32. */
        public static long pack(int chunkX, int chunkZ) {
            return (chunkX & 0xFFFFFFFFL) | ((long) chunkZ << 32);
        }

        /** The chunk x of a packed position. */
        public static int chunkX(long packed) {
            return (int) packed;
        }

        /** The chunk z of a packed position. */
        public static int chunkZ(long packed) {
            return (int) (packed >>> 32);
        }

        /** A copy of the packed chunk positions. */
        @Override
        public long[] chunks() {
            return chunks.clone();
        }

        /** Number of chunks, without copying. */
        public int size() {
            return chunks.length;
        }

        @Override
        public MapBounds bounds() {
            return boundsOf(chunks);
        }

        private static MapBounds boundsOf(long[] packed) {
            int minCx = Integer.MAX_VALUE;
            int minCz = Integer.MAX_VALUE;
            int maxCx = Integer.MIN_VALUE;
            int maxCz = Integer.MIN_VALUE;
            for (long p : packed) {
                minCx = Math.min(minCx, chunkX(p));
                maxCx = Math.max(maxCx, chunkX(p));
                minCz = Math.min(minCz, chunkZ(p));
                maxCz = Math.max(maxCz, chunkZ(p));
            }
            return new MapBounds(block(minCx, 0), block(minCz, 0), block(maxCx, 15), block(maxCz, 15));
        }

        private static int block(int chunk, int offset) {
            long v = ((long) chunk << 4) + offset;
            return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, v));
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Chunks c && id.equals(c.id) && layer.equals(c.layer) && dimension.equals(c.dimension)
                    && style.equals(c.style) && Arrays.equals(chunks, c.chunks);
        }

        @Override
        public int hashCode() {
            int h = id.hashCode();
            h = 31 * h + layer.hashCode();
            h = 31 * h + dimension.hashCode();
            h = 31 * h + style.hashCode();
            return 31 * h + Arrays.hashCode(chunks);
        }

        @Override
        public String toString() {
            return "Chunks[id=" + id + ", layer=" + layer + ", dimension=" + dimension + ", style=" + style
                    + ", chunks=" + chunks.length + "]";
        }
    }

    /**
     * A simple polygon: {@code xz = {x0, z0, x1, z1, ...}}, 3..{@link #MAX_VERTICES} vertices, implicitly
     * closed. The array is cloned in and out. Equality compares contents.
     */
    record Polygon(String id, Identifier layer, ResourceKey<Level> dimension, MapAreaStyle style, int[] xz)
            implements MapArea {

        /** Most vertices in one polygon. */
        public static final int MAX_VERTICES = 64;

        public Polygon {
            MapMarker.requireItem(id, layer, dimension, "area");
            style = MapArea.orDefault(style);
            if (xz == null || xz.length % 2 != 0 || xz.length < 6 || xz.length > MAX_VERTICES * 2) {
                throw new IllegalArgumentException("A polygon has 3.." + MAX_VERTICES + " vertices as x,z pairs");
            }
            xz = xz.clone();
            MapArea.requireSpan(boundsOf(xz), "polygon");
        }

        /** A copy of the vertex array. */
        @Override
        public int[] xz() {
            return xz.clone();
        }

        /** Number of vertices, without copying. */
        public int vertices() {
            return xz.length / 2;
        }

        @Override
        public MapBounds bounds() {
            return boundsOf(xz);
        }

        private static MapBounds boundsOf(int[] v) {
            int minX = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for (int i = 0; i < v.length; i += 2) {
                minX = Math.min(minX, v[i]);
                maxX = Math.max(maxX, v[i]);
                minZ = Math.min(minZ, v[i + 1]);
                maxZ = Math.max(maxZ, v[i + 1]);
            }
            return new MapBounds(minX, minZ, maxX, maxZ);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Polygon p && id.equals(p.id) && layer.equals(p.layer) && dimension.equals(p.dimension)
                    && style.equals(p.style) && Arrays.equals(xz, p.xz);
        }

        @Override
        public int hashCode() {
            int h = id.hashCode();
            h = 31 * h + layer.hashCode();
            h = 31 * h + dimension.hashCode();
            h = 31 * h + style.hashCode();
            return 31 * h + Arrays.hashCode(xz);
        }

        @Override
        public String toString() {
            return "Polygon[id=" + id + ", layer=" + layer + ", dimension=" + dimension + ", style=" + style
                    + ", vertices=" + vertices() + "]";
        }
    }
}
