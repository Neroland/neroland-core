package za.co.neroland.nerolandcore.sideconfig;

import java.util.Locale;

/**
 * A face named <em>relative to the machine's facing</em>, exactly like Mekanism, so
 * a configuration travels with the block when it is rotated or re-placed — a face
 * set to OUTPUT stays the output regardless of which way the machine points.
 *
 * <p>This enum is deliberately free of {@code net.minecraft.*}: the relative&rarr;
 * absolute rotation is pure integer math (so it is unit-testable without the game),
 * and the conversion to/from a Minecraft {@code Direction} lives in
 * {@link FaceResolver}. Absolute faces are encoded
 * {@code 0=NORTH, 1=EAST, 2=SOUTH, 3=WEST, 4=UP, 5=DOWN}; the horizontal facing index
 * runs {@code 0=NORTH, 1=EAST, 2=SOUTH, 3=WEST} clockwise.
 *
 * <pre>
 *   FRONT  = facing
 *   BACK   = facing + 2 (opposite)
 *   RIGHT  = facing + 1 (clockwise)
 *   LEFT   = facing + 3 (counter-clockwise)
 *   TOP    = UP
 *   BOTTOM = DOWN
 * </pre>
 */
public enum RelativeFace {

    FRONT,
    BACK,
    LEFT,
    RIGHT,
    TOP,
    BOTTOM;

    /** Stable iteration/packing order (index 0..5); never reorder. */
    public static final RelativeFace[] VALUES = values();

    /** Packing index 0..5. */
    public int index() {
        return ordinal();
    }

    /** Lower-case id form, e.g. {@code "front"}. */
    public String lowerName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Resolve this relative face to an absolute face index for a given horizontal
     * facing index ({@code 0..3}). Pure math; see the class encoding.
     */
    public int absoluteIndex(int facingIndex) {
        int f = facingIndex & 3;
        return switch (this) {
            case FRONT -> f;
            case BACK -> (f + 2) & 3;
            case RIGHT -> (f + 1) & 3;
            case LEFT -> (f + 3) & 3;
            case TOP -> 4;
            case BOTTOM -> 5;
        };
    }

    /** Inverse of {@link #absoluteIndex}: which relative face an absolute index occupies. */
    public static RelativeFace fromAbsoluteIndex(int facingIndex, int absoluteIndex) {
        if (absoluteIndex == 4) {
            return TOP;
        }
        if (absoluteIndex == 5) {
            return BOTTOM;
        }
        int rel = (absoluteIndex - facingIndex) & 3;
        return switch (rel) {
            case 0 -> FRONT;
            case 1 -> RIGHT;
            case 2 -> BACK;
            default -> LEFT;
        };
    }
}
