package za.co.neroland.nerolandcore.meteor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

/**
 * The pure, Minecraft-free resolution algorithm for the Meteor Material Registry —
 * filtering, weighting, live-sum normalisation and weighted picking, exactly as
 * specified in {@code docs/METEOR-MATERIAL-REGISTRY.md}. It is deliberately free of
 * any {@code net.minecraft.*} type (ids are plain strings, gate satisfaction is a
 * {@link Predicate}, randomness is a {@code double} roll in {@code [0,1)}) so the
 * math can be unit-tested without loading the game. {@link MeteorMaterials} adapts
 * live {@link MeteorMaterialEntry} registry data onto this core.
 *
 * <p>The defining property is <b>normalisation by the live sum</b>: weights are
 * relative, so percentages auto-rebalance as materials enter or leave the candidate
 * set (gated in, planet-bound, or uninstalled) with no table anywhere to edit.
 */
public final class MeteorResolution {

    private MeteorResolution() {
    }

    /** The tier-base-weight table plus the two scalar tuning knobs (sourced live from Core config). */
    public record Tuning(int commonWeight, int uncommonWeight, int rareWeight,
                         double planetBias, double exoticChance) {

        /** The base weight for a primary-pool tier; {@code 0} for {@link MeteorTier#EXOTIC} (no tier weight). */
        public int tierBaseWeight(MeteorTier tier) {
            return switch (tier) {
                case COMMON -> commonWeight;
                case UNCOMMON -> uncommonWeight;
                case RARE -> rareWeight;
                case EXOTIC -> 0;
            };
        }
    }

    /** The per-roll context: where the grind happens and which gates the actor satisfies. */
    public record Context(@Nullable String currentPlanet, Predicate<String> gateSatisfied) {

        public boolean satisfiesGate(@Nullable String gate) {
            return gate == null || gateSatisfied.test(gate);
        }

        public boolean matchesPlanet(@Nullable String planet) {
            return planet != null && planet.equals(currentPlanet);
        }
    }

    /**
     * A registry entry reduced to the fields the algorithm needs. Ids are strings so the core stays
     * Minecraft-free; {@link MeteorMaterials} builds these from {@link MeteorMaterialEntry}.
     */
    public record Candidate(String id, MeteorTier tier, @Nullable String minGate,
                            @Nullable String planet, @Nullable Integer weightOverride, boolean enabled) {
        public Candidate {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(tier, "tier");
        }
    }

    // --- primary pool -------------------------------------------------------

    /** The eligible primary-pool candidates: non-exotic, enabled, gate satisfied, planet matches (or unbound). */
    public static List<Candidate> primaryCandidates(Collection<Candidate> all, Context ctx) {
        List<Candidate> out = new ArrayList<>();
        for (Candidate m : all) {
            if (m.tier().isExotic() || !m.enabled()) {
                continue;
            }
            if (!ctx.satisfiesGate(m.minGate())) {
                continue;
            }
            if (m.planet() != null && !ctx.matchesPlanet(m.planet())) {
                continue;
            }
            out.add(m);
        }
        return out;
    }

    /**
     * The selection weight of a primary candidate: {@code base × (on its bound planet ? planetBias : 1.0)},
     * where {@code base = weight_override ?? tierBaseWeight[tier]}. Never negative.
     */
    public static double weight(Candidate m, Context ctx, Tuning tuning) {
        int base = m.weightOverride() != null ? m.weightOverride() : tuning.tierBaseWeight(m.tier());
        double w = base * (ctx.matchesPlanet(m.planet()) ? tuning.planetBias() : 1.0D);
        return Math.max(0.0D, w);
    }

    /** The raw (un-normalised) weights for {@code ordered}, index-aligned. */
    public static double[] weights(List<Candidate> ordered, Context ctx, Tuning tuning) {
        double[] w = new double[ordered.size()];
        for (int i = 0; i < w.length; i++) {
            w[i] = weight(ordered.get(i), ctx, tuning);
        }
        return w;
    }

    /** Normalise weights to fractions summing to 1.0 (all-zero / empty input yields an empty array). */
    public static double[] normalise(double[] weights) {
        double sum = 0.0D;
        for (double v : weights) {
            sum += v;
        }
        double[] out = new double[weights.length];
        if (sum <= 0.0D) {
            return out;
        }
        for (int i = 0; i < weights.length; i++) {
            out[i] = weights[i] / sum;
        }
        return out;
    }

    /**
     * Pick an index from raw {@code weights} for a uniform {@code roll} in {@code [0,1)} (it is scaled by
     * the live sum internally — no pre-normalisation needed). Returns {@code -1} if no positive weight.
     */
    public static int pickIndex(double[] weights, double roll) {
        double sum = 0.0D;
        for (double v : weights) {
            sum += Math.max(0.0D, v);
        }
        if (sum <= 0.0D) {
            return -1;
        }
        double target = roll * sum;
        double cursor = 0.0D;
        for (int i = 0; i < weights.length; i++) {
            cursor += Math.max(0.0D, weights[i]);
            if (target < cursor) {
                return i;
            }
        }
        return weights.length - 1; // floating-point guard
    }

    /** Resolve a single primary output, or {@code null} if the candidate set is empty / all-zero weight. */
    @Nullable
    public static Candidate pickPrimary(Collection<Candidate> all, Context ctx, Tuning tuning, double roll) {
        List<Candidate> candidates = primaryCandidates(all, ctx);
        int idx = pickIndex(weights(candidates, ctx, tuning), roll);
        return idx < 0 ? null : candidates.get(idx);
    }

    // --- exotic pool --------------------------------------------------------

    /**
     * The eligible exotic-pool candidates: exotic tier, enabled, gate satisfied, and carrying a
     * positive {@code weight_override} (exotic has no tier base weight, so an override is mandatory;
     * a null or non-positive override makes the entry ineligible).
     */
    public static List<Candidate> exoticCandidates(Collection<Candidate> all, Context ctx) {
        List<Candidate> out = new ArrayList<>();
        for (Candidate m : all) {
            if (!m.tier().isExotic() || !m.enabled()) {
                continue;
            }
            if (m.weightOverride() == null || m.weightOverride() <= 0) {
                continue;
            }
            if (!ctx.satisfiesGate(m.minGate())) {
                continue;
            }
            out.add(m);
        }
        return out;
    }

    /** Whether the exotic bonus pool fires this operation, for a uniform {@code roll} in {@code [0,1)}. */
    public static boolean rollExotic(Tuning tuning, double roll) {
        return roll < tuning.exoticChance();
    }

    /** Pick a single exotic output (weighted by {@code weight_override}), or {@code null} if none eligible. */
    @Nullable
    public static Candidate pickExotic(Collection<Candidate> all, Context ctx, double roll) {
        List<Candidate> candidates = exoticCandidates(all, ctx);
        double[] w = new double[candidates.size()];
        for (int i = 0; i < w.length; i++) {
            Integer override = candidates.get(i).weightOverride();
            w[i] = override == null ? 0.0D : override;
        }
        int idx = pickIndex(w, roll);
        return idx < 0 ? null : candidates.get(idx);
    }
}
