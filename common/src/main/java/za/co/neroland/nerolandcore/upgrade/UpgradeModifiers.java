package za.co.neroland.nerolandcore.upgrade;

import za.co.neroland.nerolandcore.config.CoreConfig;

/**
 * Resolves the aggregated effect of the modules in an {@link UpgradeContainer} into
 * the multipliers a machine reads each tick. Stacked identical modules combine with
 * a configurable diminishing factor ({@link CoreConfig#UPGRADE_STACKING_DIMINISH};
 * 1.0 = linear, lower = stronger falloff), and every effect is clamped so a stack of
 * modules can't trivialise balance. All machines across all Nero mods share this one
 * resolver, so balance is tuned in one place.
 */
public final class UpgradeModifiers {

    // Per-module effect sizes (the diminishing curve + caps temper these).
    private static final double SPEED_PER_MODULE = 0.5D;
    private static final double EFFICIENCY_PER_MODULE = 0.15D;
    private static final double CAPACITY_PER_MODULE = 0.5D;

    private static final double MAX_SPEED = 8.0D;
    private static final double MIN_ENERGY_FRACTION = 0.25D;
    private static final double MAX_CAPACITY = 8.0D;

    private final UpgradeContainer container;

    public UpgradeModifiers(UpgradeContainer container) {
        this.container = container;
    }

    /** Work-cap multiplier (>= 1): more Speed modules raise the per-cycle ceiling. */
    public double speedMultiplier() {
        return Math.min(MAX_SPEED, 1.0D + effective(UpgradeType.SPEED) * SPEED_PER_MODULE);
    }

    /** Energy-cost multiplier (<= 1): more Efficiency modules make work cheaper, floored. */
    public double energyMultiplier() {
        return Math.max(MIN_ENERGY_FRACTION, 1.0D - effective(UpgradeType.EFFICIENCY) * EFFICIENCY_PER_MODULE);
    }

    /** Buffer-capacity multiplier (>= 1): more Capacity modules enlarge internal storage. */
    public double capacityMultiplier() {
        return Math.min(MAX_CAPACITY, 1.0D + effective(UpgradeType.CAPACITY) * CAPACITY_PER_MODULE);
    }

    /** Extra working radius in blocks (>= 0), one per effective Range module. */
    public int rangeBonus() {
        return (int) Math.round(effective(UpgradeType.RANGE));
    }

    /** Raw module count of a type (before the diminishing curve). */
    public int count(UpgradeType type) {
        return container.count(type);
    }

    /**
     * The diminishing-adjusted effective count: with diminish {@code d}, the i-th
     * module contributes {@code d^i}, so the total is {@code 1 + d + d^2 + …}. At
     * {@code d = 1} this is just the raw count (linear).
     */
    private double effective(UpgradeType type) {
        int count = container.count(type);
        double diminish = CoreConfig.UPGRADE_STACKING_DIMINISH.get();
        if (diminish >= 1.0D) {
            return count;
        }
        double sum = 0.0D;
        double term = 1.0D;
        for (int i = 0; i < count; i++) {
            sum += term;
            term *= diminish;
        }
        return sum;
    }
}
