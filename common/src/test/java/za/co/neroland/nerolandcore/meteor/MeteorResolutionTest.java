package za.co.neroland.nerolandcore.meteor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import za.co.neroland.nerolandcore.meteor.MeteorResolution.Candidate;
import za.co.neroland.nerolandcore.meteor.MeteorResolution.Context;
import za.co.neroland.nerolandcore.meteor.MeteorResolution.Tuning;

/**
 * Locks the Meteor Material Registry math against the worked example in
 * {@code docs/METEOR-MATERIAL-REGISTRY.md}: the three progression-stage percentage
 * tables, off-world collapse, the weighted-pick boundaries, and the exotic pool.
 *
 * <p>These tests are pure JVM (no {@code net.minecraft.*}); they exercise
 * {@link MeteorResolution} directly so the algorithm is verifiable without the game.
 */
class MeteorResolutionTest {

    // Core config defaults from the spec.
    private static final Tuning TUNING = new Tuning(60, 25, 12, 2.0D, 0.08D);

    private static final String REACHED_ORBIT = "nerolandcore:reached_orbit";
    private static final String FIRST_COLONY = "nerolandcore:first_colony";
    private static final String KEPTARIS = "nerospace:keptaris";

    // The five registrations from the worked example.
    private static final Candidate NERO_ALLOY =
            new Candidate("nerolandcore:nero_alloy", MeteorTier.COMMON, null, null, null, true);
    private static final Candidate PLASMA_GLASS =
            new Candidate("nerolandcore:plasma_glass", MeteorTier.UNCOMMON, null, null, null, true);
    private static final Candidate VOID_CRYSTAL =
            new Candidate("nerolandcore:void_crystal", MeteorTier.UNCOMMON, REACHED_ORBIT, null, null, true);
    private static final Candidate STARSTEEL =
            new Candidate("nerolandcore:starsteel", MeteorTier.RARE, REACHED_ORBIT, null, null, true);
    private static final Candidate PLANET_SIGNATURE =
            new Candidate("nerospace:keptarite", MeteorTier.RARE, FIRST_COLONY, KEPTARIS, null, true);

    private static final List<Candidate> ALL =
            List.of(NERO_ALLOY, PLASMA_GLASS, VOID_CRYSTAL, STARSTEEL, PLANET_SIGNATURE);

    private static Context context(Set<String> openGates, String planet) {
        Predicate<String> gate = openGates::contains;
        return new Context(planet, gate);
    }

    /** Percentages of the eligible primary pool, rounded to one decimal, keyed by candidate id. */
    private static java.util.Map<String, Double> primaryPercentages(Context ctx) {
        List<Candidate> pool = MeteorResolution.primaryCandidates(ALL, ctx);
        double[] norm = MeteorResolution.normalise(MeteorResolution.weights(pool, ctx, TUNING));
        java.util.LinkedHashMap<String, Double> out = new java.util.LinkedHashMap<>();
        for (int i = 0; i < pool.size(); i++) {
            out.put(pool.get(i).id(), Math.round(norm[i] * 1000.0D) / 10.0D);
        }
        return out;
    }

    @Test
    void stage1PreOrbit() {
        var pct = primaryPercentages(context(Set.of(), null));
        assertEquals(2, pct.size());
        assertEquals(70.6D, pct.get("nerolandcore:nero_alloy"));
        assertEquals(29.4D, pct.get("nerolandcore:plasma_glass"));
    }

    @Test
    void stage2ReachedOrbit() {
        var pct = primaryPercentages(context(Set.of(REACHED_ORBIT), null));
        assertEquals(4, pct.size());
        assertEquals(49.2D, pct.get("nerolandcore:nero_alloy"));
        assertEquals(20.5D, pct.get("nerolandcore:plasma_glass"));
        assertEquals(20.5D, pct.get("nerolandcore:void_crystal"));
        assertEquals(9.8D, pct.get("nerolandcore:starsteel"));
    }

    @Test
    void stage3FirstColonyOnBoundPlanet() {
        var pct = primaryPercentages(context(Set.of(REACHED_ORBIT, FIRST_COLONY), KEPTARIS));
        assertEquals(5, pct.size());
        assertEquals(41.1D, pct.get("nerolandcore:nero_alloy"));
        assertEquals(17.1D, pct.get("nerolandcore:plasma_glass"));
        assertEquals(17.1D, pct.get("nerolandcore:void_crystal"));
        assertEquals(8.2D, pct.get("nerolandcore:starsteel"));
        assertEquals(16.4D, pct.get("nerospace:keptarite")); // 12 * 2.0 planetBias = 24
    }

    @Test
    void stage3OffWorldCollapsesToStage2() {
        // Same gates as stage 3 but grinding off the bound planet: the planet entry drops out.
        var offWorld = primaryPercentages(context(Set.of(REACHED_ORBIT, FIRST_COLONY), null));
        var stage2 = primaryPercentages(context(Set.of(REACHED_ORBIT), null));
        assertEquals(stage2, offWorld);
    }

    @Test
    void weightedPickBoundaries() {
        // Pool [nero_alloy 60, plasma_glass 25], live sum 85.
        Context ctx = context(Set.of(), null);
        List<Candidate> pool = MeteorResolution.primaryCandidates(ALL, ctx);
        double[] w = MeteorResolution.weights(pool, ctx, TUNING);
        assertEquals(0, MeteorResolution.pickIndex(w, 0.0D));                  // start of band 0
        assertEquals(0, MeteorResolution.pickIndex(w, 60.0D / 85.0D - 1e-6)); // just inside band 0
        assertEquals(1, MeteorResolution.pickIndex(w, 60.0D / 85.0D + 1e-6)); // just inside band 1
        assertEquals(1, MeteorResolution.pickIndex(w, 0.999999D));            // end of band 1
    }

    @Test
    void emptyPoolPicksNothing() {
        // Both candidates require reached_orbit, which is not open: empty primary pool, no pick.
        Context ctx = context(Set.of(), null);
        List<Candidate> gatedOnly = List.of(VOID_CRYSTAL, STARSTEEL);
        assertTrue(MeteorResolution.primaryCandidates(gatedOnly, ctx).isEmpty());
        assertNull(MeteorResolution.pickPrimary(gatedOnly, ctx, TUNING, 0.5D));
    }

    @Test
    void exoticPoolWeightedByOverrideAndRequiresIt() {
        Candidate creditChip =
                new Candidate("neroeconomy:credit_chip", MeteorTier.EXOTIC, null, null, 30, true);
        Candidate relic =
                new Candidate("neroruins:relic_fragment", MeteorTier.EXOTIC, null, null, 25, true);
        Candidate noOverride =
                new Candidate("mod:exotic_bad", MeteorTier.EXOTIC, null, null, null, true);
        List<Candidate> all = List.of(NERO_ALLOY, creditChip, relic, noOverride);
        Context ctx = context(Set.of(), null);

        // Exotic with no weight_override is ineligible; the two with overrides remain.
        List<String> eligible = MeteorResolution.exoticCandidates(all, ctx).stream()
                .map(Candidate::id).collect(Collectors.toList());
        assertEquals(List.of("neroeconomy:credit_chip", "neroruins:relic_fragment"), eligible);

        // Σ override = 55; band credit_chip [0,30/55), relic [30/55,1).
        assertEquals("neroeconomy:credit_chip", MeteorResolution.pickExotic(all, ctx, 0.0D).id());
        assertEquals("neroeconomy:credit_chip", MeteorResolution.pickExotic(all, ctx, 0.5D).id());
        assertEquals("neroruins:relic_fragment", MeteorResolution.pickExotic(all, ctx, 0.9D).id());
    }

    @Test
    void exoticChanceGate() {
        assertTrue(MeteorResolution.rollExotic(TUNING, 0.0D));
        assertTrue(MeteorResolution.rollExotic(TUNING, 0.079D));
        assertFalse(MeteorResolution.rollExotic(TUNING, 0.08D)); // boundary: not less-than
        assertFalse(MeteorResolution.rollExotic(TUNING, 0.5D));
    }
}
