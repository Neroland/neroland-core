package za.co.neroland.nerolandcore.map;

import net.minecraft.core.BlockPos;

/**
 * An inclusive, horizontal block-coordinate box (Core 1.19.0). The compact constructor normalises the
 * corners, so {@code min <= max} on both axes.
 *
 * <p>Bounds only ever <b>narrow</b> a query. They are chosen by the consumer and, through a companion
 * app, ultimately by the player, so a provider never derives visibility from them: proximity is measured
 * from the viewer ({@link MapViewer#within}).
 */
public record MapBounds(int minX, int minZ, int maxX, int maxZ) {

    /** Widest span per axis a query or an area may cover; {@link MapLayerRegistry#query} clamps wider queries. */
    public static final int MAX_SPAN = 16_384;

    public MapBounds {
        if (minX > maxX) {
            int t = minX;
            minX = maxX;
            maxX = t;
        }
        if (minZ > maxZ) {
            int t = minZ;
            minZ = maxZ;
            maxZ = t;
        }
    }

    /** A square of half-width {@code radius} centred on {@code centre}. */
    public static MapBounds around(BlockPos centre, int radius) {
        return around(centre.getX(), centre.getZ(), radius);
    }

    /** A square of half-width {@code radius} (negative is treated as 0) centred on {@code (x, z)}. */
    public static MapBounds around(int x, int z, int radius) {
        long r = Math.max(0, radius);
        return new MapBounds(sat(x - r), sat(z - r), sat(x + r), sat(z + r));
    }

    /** {@code true} when the column {@code (x, z)} lies inside. */
    public boolean contains(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    /** {@code true} when the two boxes share at least one column. */
    public boolean intersects(MapBounds other) {
        return other != null && other.minX <= maxX && other.maxX >= minX && other.minZ <= maxZ && other.maxZ >= minZ;
    }

    /** {@code x} clamped into {@code [minX, maxX]}: the nearest x inside this box. */
    public int nearestX(int x) {
        return Math.max(minX, Math.min(maxX, x));
    }

    /** {@code z} clamped into {@code [minZ, maxZ]}: the nearest z inside this box. */
    public int nearestZ(int z) {
        return Math.max(minZ, Math.min(maxZ, z));
    }

    /** The centre column's x (rounded down). */
    public int centreX() {
        return (int) Math.floorDiv((long) minX + maxX, 2L);
    }

    /** The centre column's z (rounded down). */
    public int centreZ() {
        return (int) Math.floorDiv((long) minZ + maxZ, 2L);
    }

    /** Span along x, in blocks (inclusive), as a long so it cannot overflow. */
    public long spanX() {
        return (long) maxX - minX + 1;
    }

    /** Span along z, in blocks (inclusive), as a long so it cannot overflow. */
    public long spanZ() {
        return (long) maxZ - minZ + 1;
    }

    /** This box, shrunk about its centre on any axis wider than {@link #MAX_SPAN}. */
    public MapBounds clampSpan() {
        if (spanX() <= MAX_SPAN && spanZ() <= MAX_SPAN) {
            return this;
        }
        int half = MAX_SPAN / 2;
        int nMinX = minX;
        int nMaxX = maxX;
        int nMinZ = minZ;
        int nMaxZ = maxZ;
        if (spanX() > MAX_SPAN) {
            long c = centreX();
            nMinX = sat(c - half);
            nMaxX = sat(c + half - 1);
        }
        if (spanZ() > MAX_SPAN) {
            long c = centreZ();
            nMinZ = sat(c - half);
            nMaxZ = sat(c + half - 1);
        }
        return new MapBounds(nMinX, nMinZ, nMaxX, nMaxZ);
    }

    private static int sat(long v) {
        return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, v));
    }
}
