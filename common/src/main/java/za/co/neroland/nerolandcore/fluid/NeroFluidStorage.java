package za.co.neroland.nerolandcore.fluid;

import net.minecraft.world.level.material.Fluid;

/**
 * Neroland Core's fluid-storage contract — the loader-neutral single-fluid tank
 * interface every Nero tank, pipe and machine exposes (amount in millibuckets).
 * Each loader bridges it to its own block-lookup mechanism (NeoForge
 * {@code BlockCapability}, Fabric {@code BlockApiLookup}, Forge capability) so
 * fluid handling interoperates across mods on a single surface — the fluid
 * analogue of {@link za.co.neroland.nerolandcore.energy.NeroEnergyStorage}.
 *
 * <p>Bridging to the platforms' native fluid handlers (NeoForge
 * {@code Capabilities.Fluid} / Fabric {@code FluidStorage}) plus vanilla bucket
 * interop is a deferred enhancement; downstream mods code against this contract.
 */
public interface NeroFluidStorage {

    /** The stored fluid, or {@code Fluids.EMPTY} if empty. */
    Fluid getFluid();

    /** Current stored amount (mB). */
    long getAmount();

    /** Maximum storable amount (mB). */
    long getCapacity();

    /** Fill with {@code fluid} (must match the stored fluid unless empty). @return mB filled. */
    long fill(Fluid fluid, long amount, boolean simulate);

    /** Drain the stored fluid. @return mB drained. */
    long drain(long amount, boolean simulate);
}
