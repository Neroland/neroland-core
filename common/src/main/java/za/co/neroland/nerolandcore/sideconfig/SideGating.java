package za.co.neroland.nerolandcore.sideconfig;

import java.util.function.Supplier;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;
import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;
import za.co.neroland.nerolandcore.gas.NeroGasStorage;

/**
 * Wraps a Core storage in a {@link SideMode}-gated view: an {@link SideMode#INPUT}
 * face yields an insert-only view, {@link SideMode#OUTPUT} an extract-only view,
 * {@link SideMode#IO} the full view, and {@link SideMode#DISABLED} no view at all
 * (the capability lambda returns {@code null}). The wrappers read the mode
 * <em>live</em> on every operation via a supplier, so a held reference always
 * reflects the current face mode — no stale handlers after a reconfigure.
 *
 * <p>Used by each loader's capability registration. Item gating instead flows
 * through the vanilla {@code WorldlyContainer} hooks the
 * {@link SideConfigComponent} answers, so no item wrapper is needed here.
 */
public final class SideGating {

    private SideGating() {
    }

    public static NeroEnergyStorage energy(NeroEnergyStorage delegate, Supplier<SideMode> mode) {
        return new NeroEnergyStorage() {
            @Override
            public long getAmount() {
                return delegate.getAmount();
            }

            @Override
            public long getCapacity() {
                return delegate.getCapacity();
            }

            @Override
            public long insert(long maxAmount, boolean simulate) {
                return mode.get().canInsert() ? delegate.insert(maxAmount, simulate) : 0;
            }

            @Override
            public long extract(long maxAmount, boolean simulate) {
                return mode.get().canExtract() ? delegate.extract(maxAmount, simulate) : 0;
            }
        };
    }

    public static NeroFluidStorage fluid(NeroFluidStorage delegate, Supplier<SideMode> mode) {
        return new NeroFluidStorage() {
            @Override
            public Fluid getFluid() {
                return delegate.getFluid();
            }

            @Override
            public long getAmount() {
                return delegate.getAmount();
            }

            @Override
            public long getCapacity() {
                return delegate.getCapacity();
            }

            @Override
            public long fill(Fluid fluid, long amount, boolean simulate) {
                return mode.get().canInsert() ? delegate.fill(fluid, amount, simulate) : 0;
            }

            @Override
            public long drain(long amount, boolean simulate) {
                return mode.get().canExtract() ? delegate.drain(amount, simulate) : 0;
            }
        };
    }

    public static NeroGasStorage gas(NeroGasStorage delegate, Supplier<SideMode> mode) {
        return new NeroGasStorage() {
            @Override
            public Identifier getGas() {
                return delegate.getGas();
            }

            @Override
            public long getAmount() {
                return delegate.getAmount();
            }

            @Override
            public long getCapacity() {
                return delegate.getCapacity();
            }

            @Override
            public long fill(Identifier gas, long amount, boolean simulate) {
                return mode.get().canInsert() ? delegate.fill(gas, amount, simulate) : 0;
            }

            @Override
            public long drain(long amount, boolean simulate) {
                return mode.get().canExtract() ? delegate.drain(amount, simulate) : 0;
            }
        };
    }
}
