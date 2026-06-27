package za.co.neroland.nerolandcore.upgrade;

/**
 * The kinds of machine upgrade-module effect. A module item declares which type it
 * is (via the host machine's {@link UpgradeContainer.Classifier}); a machine counts
 * matching modules across its slots and reads the aggregated effect from
 * {@link UpgradeModifiers}. Shared across mods so modules are interchangeable
 * between any machines built on Core's framework.
 */
public enum UpgradeType {

    /** Raises the per-cycle work ceiling (machine does more when fed more power). */
    SPEED,
    /** Lowers the energy cost per unit of work. */
    EFFICIENCY,
    /** Extends a machine's working radius / reach. */
    RANGE,
    /** Increases a machine's internal buffer (energy / fluid / item) capacity. */
    CAPACITY
}
