package za.co.neroland.nerolandcore.platform;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;

import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;

/**
 * Adapters between Core's loader-neutral {@link NeroFluidStorage} and Forge's standard
 * {@link IFluidHandler} capability ({@code ForgeCapabilities.FLUID_HANDLER}).
 *
 * <p>The fluid twin of the FE bridge in {@link ForgeEnergyLookup}: downstream mods attach
 * {@link #asFluidHandler(NeroFluidStorage)} so third-party pipes can fill and drain a Nero tank,
 * and {@link ForgeFluidLookup} uses {@link #asNeroFluid(IFluidHandler)} so Nero blocks treat
 * third-party tanks as Nero neighbours. Both sides count in millibuckets, so nothing is converted;
 * Forge's {@code int} amounts are clamped, since a Nero tank counts in {@code long}.
 */
public final class ForgeFluidHandlers {

    private ForgeFluidHandlers() {
    }

    /** Expose a Nero tank on Forge's standard fluid capability. */
    public static IFluidHandler asFluidHandler(NeroFluidStorage store) {
        return new NeroToStandard(store);
    }

    /**
     * Expose several Nero tanks as one multi-tank {@link IFluidHandler} — tank {@code i} is
     * {@code stores.get(i)}. A machine with both a fluid tank and a gas tank riding a transport fluid
     * needs this: one block carries one fluid capability, but that capability may have many tanks.
     */
    public static IFluidHandler asFluidHandler(List<? extends NeroFluidStorage> stores) {
        List<IFluidHandler> parts = new ArrayList<>(stores.size());
        for (NeroFluidStorage store : stores) {
            parts.add(asFluidHandler(store));
        }
        return new Composite(parts);
    }

    /** Adapt a third-party {@link IFluidHandler} to Core's {@link NeroFluidStorage} contract. */
    public static NeroFluidStorage asNeroFluid(IFluidHandler handler) {
        return new StandardToNero(handler);
    }

    private static int clampToInt(long value) {
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, value));
    }

    /** Nero tank seen as a single-tank {@link IFluidHandler}. */
    private static final class NeroToStandard implements IFluidHandler {

        private final NeroFluidStorage store;

        private NeroToStandard(NeroFluidStorage store) {
            this.store = store;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            Fluid held = this.store.getFluid();
            if (tank != 0 || held == Fluids.EMPTY || this.store.getAmount() <= 0) {
                return FluidStack.EMPTY;
            }
            return new FluidStack(held, clampToInt(this.store.getAmount()));
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? clampToInt(this.store.getCapacity()) : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            if (tank != 0 || stack.isEmpty()) {
                return false;
            }
            Fluid held = this.store.getFluid();
            return held == Fluids.EMPTY || held == stack.getFluid();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) {
                return 0;
            }
            return clampToInt(this.store.fill(resource.getFluid(), resource.getAmount(), action.simulate()));
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty() || this.store.getFluid() != resource.getFluid()) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), action);
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            Fluid held = this.store.getFluid();
            if (maxDrain <= 0 || held == Fluids.EMPTY) {
                return FluidStack.EMPTY;
            }
            long drained = this.store.drain(maxDrain, action.simulate());
            return drained <= 0 ? FluidStack.EMPTY : new FluidStack(held, clampToInt(drained));
        }
    }

    /** Several single-tank handlers seen as one handler with a tank per index. */
    private record Composite(List<IFluidHandler> parts) implements IFluidHandler {

        @Override
        public int getTanks() {
            return this.parts.size();
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return valid(tank) ? this.parts.get(tank).getFluidInTank(0) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return valid(tank) ? this.parts.get(tank).getTankCapacity(0) : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return valid(tank) && this.parts.get(tank).isFluidValid(0, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            for (IFluidHandler part : this.parts) {
                int filled = part.fill(resource, action);
                if (filled > 0) {
                    return filled;
                }
            }
            return 0;
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            for (IFluidHandler part : this.parts) {
                FluidStack drained = part.drain(resource, action);
                if (!drained.isEmpty()) {
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            for (IFluidHandler part : this.parts) {
                FluidStack drained = part.drain(maxDrain, action);
                if (!drained.isEmpty()) {
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }

        private boolean valid(int tank) {
            return tank >= 0 && tank < this.parts.size();
        }
    }

    /**
     * Third-party {@link IFluidHandler} seen as a Nero tank. Multi-tank handlers are flattened: the
     * reported fluid is the first non-empty tank's and amounts/capacities are summed — the same
     * lossy-but-useful view Core's FE adapter takes of a foreign energy handler.
     */
    private static final class StandardToNero implements NeroFluidStorage {

        private final IFluidHandler handler;

        private StandardToNero(IFluidHandler handler) {
            this.handler = handler;
        }

        @Override
        public Fluid getFluid() {
            for (int i = 0; i < this.handler.getTanks(); i++) {
                FluidStack stack = this.handler.getFluidInTank(i);
                if (!stack.isEmpty()) {
                    return stack.getFluid();
                }
            }
            return Fluids.EMPTY;
        }

        @Override
        public long getAmount() {
            long total = 0;
            for (int i = 0; i < this.handler.getTanks(); i++) {
                total += this.handler.getFluidInTank(i).getAmount();
            }
            return total;
        }

        @Override
        public long getCapacity() {
            long total = 0;
            for (int i = 0; i < this.handler.getTanks(); i++) {
                total += this.handler.getTankCapacity(i);
            }
            return total;
        }

        @Override
        public long fill(Fluid fluid, long amount, boolean simulate) {
            if (fluid == Fluids.EMPTY || amount <= 0) {
                return 0;
            }
            FluidStack offer = new FluidStack(fluid, clampToInt(amount));
            return this.handler.fill(offer, simulate
                    ? IFluidHandler.FluidAction.SIMULATE
                    : IFluidHandler.FluidAction.EXECUTE);
        }

        @Override
        public long drain(long amount, boolean simulate) {
            Fluid held = getFluid();
            if (amount <= 0 || held == Fluids.EMPTY) {
                return 0;
            }
            FluidStack request = new FluidStack(held, clampToInt(amount));
            FluidStack drained = this.handler.drain(request, simulate
                    ? IFluidHandler.FluidAction.SIMULATE
                    : IFluidHandler.FluidAction.EXECUTE);
            return drained.isEmpty() ? 0 : drained.getAmount();
        }
    }
}
