package za.co.neroland.nerolandcore.demo;

/**
 * The ground a {@link DemoModule}'s exhibit needs in a Nero Mods Demo world (Core 1.20.0), in blocks along X
 * (east) and Z (south). Core lays the plot floor over exactly this area and the module builds on top of it.
 *
 * @param sizeX width along X, {@value #MIN_SIZE} to {@value #MAX_SIZE}
 * @param sizeZ depth along Z, {@value #MIN_SIZE} to {@value #MAX_SIZE}
 */
public record DemoFootprint(int sizeX, int sizeZ) {

    /** Smallest plot side, in blocks. */
    public static final int MIN_SIZE = 5;

    /** Largest plot side, in blocks. */
    public static final int MAX_SIZE = 96;

    public DemoFootprint {
        if (sizeX < MIN_SIZE || sizeX > MAX_SIZE || sizeZ < MIN_SIZE || sizeZ > MAX_SIZE) {
            throw new IllegalArgumentException("A demo footprint must be " + MIN_SIZE + ".." + MAX_SIZE
                    + " blocks on each side, got " + sizeX + "x" + sizeZ);
        }
    }

    /** A footprint of {@code sizeX} by {@code sizeZ} blocks. */
    public static DemoFootprint of(int sizeX, int sizeZ) {
        return new DemoFootprint(sizeX, sizeZ);
    }
}
