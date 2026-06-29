package za.co.neroland.nerolandcore.sideconfig;

import net.minecraft.core.Direction;

/**
 * Converts between Minecraft {@link Direction}s and the pure {@link RelativeFace}
 * model, resolving against a machine's horizontal facing (the front). Isolated here
 * so {@link RelativeFace} can stay free of {@code net.minecraft.*} and unit-testable.
 *
 * <p>Vertically-facing (omnidirectional) machines are clamped to a NORTH horizontal
 * reference for v1; full 3-axis orientation is a future enhancement.
 */
public final class FaceResolver {

    private FaceResolver() {
    }

    /** Horizontal facing index: NORTH=0, EAST=1, SOUTH=2, WEST=3; non-horizontal clamps to 0. */
    public static int horizontalIndex(Direction facing) {
        if (facing == null) {
            return 0;
        }
        return switch (facing) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    /** Absolute face index: NORTH=0, EAST=1, SOUTH=2, WEST=3, UP=4, DOWN=5. */
    public static int absoluteIndex(Direction side) {
        return switch (side) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            case UP -> 4;
            case DOWN -> 5;
        };
    }

    /** The {@link Direction} for an absolute face index. */
    public static Direction direction(int absoluteIndex) {
        return switch (absoluteIndex) {
            case 0 -> Direction.NORTH;
            case 1 -> Direction.EAST;
            case 2 -> Direction.SOUTH;
            case 3 -> Direction.WEST;
            case 4 -> Direction.UP;
            default -> Direction.DOWN;
        };
    }

    /** Resolve a relative face to an absolute {@link Direction} for the given facing. */
    public static Direction toAbsolute(RelativeFace face, Direction facing) {
        return direction(face.absoluteIndex(horizontalIndex(facing)));
    }

    /** Which relative face the absolute {@code side} occupies for the given facing. */
    public static RelativeFace fromAbsolute(Direction facing, Direction side) {
        return RelativeFace.fromAbsoluteIndex(horizontalIndex(facing), absoluteIndex(side));
    }
}
