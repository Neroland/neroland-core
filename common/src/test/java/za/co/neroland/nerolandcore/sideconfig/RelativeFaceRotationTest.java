package za.co.neroland.nerolandcore.sideconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Locks the relative&harr;absolute face resolution across every horizontal rotation.
 * Pure JVM (no {@code net.minecraft.*}) — exercises {@link RelativeFace}'s integer
 * rotation math directly; the trivial {@code Direction} lookup table lives in
 * {@link FaceResolver}.
 *
 * <p>Absolute encoding: NORTH=0, EAST=1, SOUTH=2, WEST=3, UP=4, DOWN=5. Facing index
 * runs 0..3 clockwise from NORTH.
 */
class RelativeFaceRotationTest {

    @Test
    void frontAlwaysFollowsFacing() {
        for (int facing = 0; facing < 4; facing++) {
            assertEquals(facing, RelativeFace.FRONT.absoluteIndex(facing));
        }
    }

    @Test
    void backIsAlwaysOppositeOfFront() {
        for (int facing = 0; facing < 4; facing++) {
            assertEquals((facing + 2) & 3, RelativeFace.BACK.absoluteIndex(facing));
        }
    }

    @Test
    void rightIsClockwiseLeftIsCounter() {
        for (int facing = 0; facing < 4; facing++) {
            assertEquals((facing + 1) & 3, RelativeFace.RIGHT.absoluteIndex(facing));
            assertEquals((facing + 3) & 3, RelativeFace.LEFT.absoluteIndex(facing));
        }
    }

    @Test
    void verticalFacesAreFacingIndependent() {
        for (int facing = 0; facing < 4; facing++) {
            assertEquals(4, RelativeFace.TOP.absoluteIndex(facing));
            assertEquals(5, RelativeFace.BOTTOM.absoluteIndex(facing));
        }
    }

    @Test
    void everyRotationIsABijectionOverTheSixFaces() {
        for (int facing = 0; facing < 4; facing++) {
            boolean[] seen = new boolean[6];
            for (RelativeFace face : RelativeFace.VALUES) {
                int abs = face.absoluteIndex(facing);
                assertFalse(seen[abs], "absolute face " + abs + " mapped twice for facing " + facing);
                seen[abs] = true;
            }
            for (boolean b : seen) {
                assertTrue(b, "facing " + facing + " left an absolute face unmapped");
            }
        }
    }

    @Test
    void relativeToAbsoluteRoundTrips() {
        for (int facing = 0; facing < 4; facing++) {
            for (RelativeFace face : RelativeFace.VALUES) {
                assertEquals(face, RelativeFace.fromAbsoluteIndex(facing, face.absoluteIndex(facing)));
            }
        }
    }

    @Test
    void configTravelsWithRotation() {
        // A face configured relative to the machine resolves to a different absolute
        // direction as the machine rotates, but remains the SAME relative face — i.e.
        // OUTPUT on FRONT is always the front, whichever way the machine points.
        assertEquals(0, RelativeFace.FRONT.absoluteIndex(0)); // facing NORTH -> front is NORTH
        assertEquals(1, RelativeFace.FRONT.absoluteIndex(1)); // facing EAST  -> front is EAST
        assertEquals(RelativeFace.FRONT, RelativeFace.fromAbsoluteIndex(0, 0));
        assertEquals(RelativeFace.FRONT, RelativeFace.fromAbsoluteIndex(1, 1));
        // The right of a north-facing machine (EAST) is the back of an east-facing one? No:
        // it stays "right" only for the same facing; rotating reassigns which side is right.
        assertEquals(RelativeFace.RIGHT, RelativeFace.fromAbsoluteIndex(0, 1)); // facing N, EAST = right
        assertEquals(RelativeFace.FRONT, RelativeFace.fromAbsoluteIndex(1, 1)); // facing E, EAST = front
    }
}
