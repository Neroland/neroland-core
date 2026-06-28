package za.co.neroland.nerolandcore.gas;

import net.minecraft.resources.Identifier;

/**
 * Neroland Core's gas-storage contract — the loader-neutral single-gas store
 * (amount in millibuckets), the gas analogue of
 * {@link za.co.neroland.nerolandcore.fluid.NeroFluidStorage} and
 * {@link za.co.neroland.nerolandcore.energy.NeroEnergyStorage}. A gas is
 * identified generically by an {@link Identifier} (see {@link NeroGases}); Core
 * ships no concrete gases, so content mods bring their own.
 */
public interface NeroGasStorage {

    /** The stored gas id, or {@link NeroGases#EMPTY} if empty. */
    Identifier getGas();

    /** Current stored amount (mB). */
    long getAmount();

    /** Maximum storable amount (mB). */
    long getCapacity();

    /** Fill with {@code gas} (must match the stored gas unless empty). @return mB filled. */
    long fill(Identifier gas, long amount, boolean simulate);

    /** Drain the stored gas. @return mB drained. */
    long drain(long amount, boolean simulate);
}
