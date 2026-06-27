package za.co.neroland.nerolandcore.energy;

/**
 * Neroland Core's power-type contract — the loader-neutral energy interface every
 * Nero machine, generator and cable exposes. Each loader bridges it to its own
 * block-lookup mechanism (NeoForge {@code BlockCapability}, Fabric
 * {@code BlockApiLookup}, Forge capability) so machines across mods interoperate
 * on a single energy surface. Nerotech and NeroPower build on this rather than
 * inventing their own.
 *
 * <p>Energy is measured in <b>Nero energy units (NE)</b>; convert to/from Forge
 * Energy via {@link EnergyConversions} (ratio is config-driven).
 */
public interface NeroEnergyStorage {

    /** Current stored energy (NE). */
    long getAmount();

    /** Maximum storable energy (NE). */
    long getCapacity();

    /** Insert up to {@code maxAmount}; @return the amount actually inserted (0 if none). */
    long insert(long maxAmount, boolean simulate);

    /** Extract up to {@code maxAmount}; @return the amount actually extracted (0 if none). */
    long extract(long maxAmount, boolean simulate);

    /** Whether this storage can ever receive energy (false for pure sources). */
    default boolean canReceive() {
        return insert(1, true) > 0;
    }

    /** Whether this storage can ever provide energy (false for pure sinks). */
    default boolean canExtract() {
        return extract(1, true) > 0;
    }
}
