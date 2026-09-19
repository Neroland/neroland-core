package za.co.neroland.nerolandcore.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;

/**
 * Adapters between Core's loader-neutral {@link NeroFluidStorage} and NeoForge's standard fluid
 * surface ({@code Capabilities.Fluid.BLOCK}, a {@link ResourceHandler} of {@link FluidResource}).
 *
 * <p>This is the fluid twin of the FE fallback in {@link NeoForgeEnergyLookup}: downstream mods
 * register {@link #asResourceHandler(NeroFluidStorage)} on {@code Capabilities.Fluid.BLOCK} so
 * third-party pipes can fill and drain a Nero tank, and {@link NeoForgeFluidLookup} uses
 * {@link #asNeroFluid(ResourceHandler)} so Nero blocks treat third-party tanks as Nero neighbours.
 *
 * <p>Both directions speak millibuckets — NeoForge fluid amounts are already mB
 * ({@code FluidType.BUCKET_VOLUME == 1000}), so no conversion is needed. A {@link NeroFluidStorage}
 * is a single unstacked tank with no data components, so resources carrying a component patch are
 * rejected rather than silently stripped.
 */
public final class NeoForgeFluidHandlers {

    private NeoForgeFluidHandlers() {
    }

    /** Expose a Nero tank on NeoForge's standard fluid capability. */
    public static ResourceHandler<FluidResource> asResourceHandler(NeroFluidStorage store) {
        return new NeroToStandard(store);
    }

    /**
     * Expose several Nero tanks as one multi-tank handler — index {@code i} is {@code stores.get(i)}.
     * A machine with both a fluid tank and a gas tank riding a transport fluid needs this: one block
     * can carry only one fluid capability, but that capability may have many tanks.
     */
    public static ResourceHandler<FluidResource> asResourceHandler(List<? extends NeroFluidStorage> stores) {
        List<ResourceHandler<FluidResource>> parts = new ArrayList<>(stores.size());
        for (NeroFluidStorage store : stores) {
            parts.add(asResourceHandler(store));
        }
        return new Composite(parts);
    }

    /** Adapt a third-party standard fluid handler to Core's {@link NeroFluidStorage} contract. */
    public static NeroFluidStorage asNeroFluid(ResourceHandler<FluidResource> handler) {
        return new StandardToNero(handler);
    }

    private static int clampToInt(long value) {
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, value));
    }

    /** The state a transaction rolls back to: the tank's fluid and amount before the mutation. */
    private record Snapshot(Fluid fluid, long amount) {
    }

    /**
     * Nero tank seen as a one-slot {@link ResourceHandler}. Mutations are applied immediately and
     * undone by {@link #revertToSnapshot(Snapshot)} if the transaction aborts, which is the
     * {@link SnapshotJournal} contract for storage that is not itself transactional.
     */
    private static final class NeroToStandard extends SnapshotJournal<Snapshot>
            implements ResourceHandler<FluidResource> {

        private final NeroFluidStorage store;

        private NeroToStandard(NeroFluidStorage store) {
            this.store = store;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public FluidResource getResource(int index) {
            Objects.checkIndex(index, size());
            Fluid held = this.store.getFluid();
            return held == Fluids.EMPTY ? FluidResource.EMPTY : FluidResource.of(held);
        }

        @Override
        public long getAmountAsLong(int index) {
            Objects.checkIndex(index, size());
            return this.store.getAmount();
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            Objects.checkIndex(index, size());
            return resource.isEmpty() || isValid(index, resource) ? this.store.getCapacity() : 0L;
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            Objects.checkIndex(index, size());
            if (resource.isEmpty() || !resource.getComponentsPatch().isEmpty()) {
                return false;
            }
            Fluid held = this.store.getFluid();
            return held == Fluids.EMPTY || held == resource.getFluid();
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            Objects.checkIndex(index, size());
            if (amount <= 0 || !isValid(index, resource)) {
                return 0;
            }
            long accepted = this.store.fill(resource.getFluid(), amount, true);
            if (accepted <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            return clampToInt(this.store.fill(resource.getFluid(), accepted, false));
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            Objects.checkIndex(index, size());
            if (amount <= 0 || resource.isEmpty() || !resource.getComponentsPatch().isEmpty()
                    || this.store.getFluid() != resource.getFluid()) {
                return 0;
            }
            long available = this.store.drain(amount, true);
            if (available <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            return clampToInt(this.store.drain(available, false));
        }

        @Override
        protected Snapshot createSnapshot() {
            return new Snapshot(this.store.getFluid(), this.store.getAmount());
        }

        @Override
        protected void revertToSnapshot(Snapshot snapshot) {
            if (this.store.getFluid() != snapshot.fluid()) {
                this.store.drain(this.store.getAmount(), false);
                if (snapshot.amount() > 0 && snapshot.fluid() != Fluids.EMPTY) {
                    this.store.fill(snapshot.fluid(), snapshot.amount(), false);
                }
                return;
            }
            long delta = snapshot.amount() - this.store.getAmount();
            if (delta > 0) {
                this.store.fill(snapshot.fluid(), delta, false);
            } else if (delta < 0) {
                this.store.drain(-delta, false);
            }
        }
    }

    /** Several single-tank handlers seen as one handler with a tank per index. */
    private record Composite(List<ResourceHandler<FluidResource>> parts)
            implements ResourceHandler<FluidResource> {

        @Override
        public int size() {
            return this.parts.size();
        }

        @Override
        public FluidResource getResource(int index) {
            return this.parts.get(index).getResource(0);
        }

        @Override
        public long getAmountAsLong(int index) {
            return this.parts.get(index).getAmountAsLong(0);
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            return this.parts.get(index).getCapacityAsLong(0, resource);
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return this.parts.get(index).isValid(0, resource);
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return this.parts.get(index).insert(0, resource, amount, transaction);
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return this.parts.get(index).extract(0, resource, amount, transaction);
        }
    }

    /**
     * Third-party standard fluid handler seen as a Nero tank. Multi-tank handlers are flattened:
     * the reported fluid is the first non-empty one and amounts/capacities are summed, which is the
     * same lossy-but-useful view Core's FE adapter takes of a multi-slot energy handler.
     */
    private static final class StandardToNero implements NeroFluidStorage {

        private final ResourceHandler<FluidResource> handler;

        private StandardToNero(ResourceHandler<FluidResource> handler) {
            this.handler = handler;
        }

        @Override
        public Fluid getFluid() {
            for (int i = 0; i < this.handler.size(); i++) {
                FluidResource resource = this.handler.getResource(i);
                if (!resource.isEmpty() && this.handler.getAmountAsLong(i) > 0) {
                    return resource.getFluid();
                }
            }
            return Fluids.EMPTY;
        }

        @Override
        public long getAmount() {
            long total = 0;
            for (int i = 0; i < this.handler.size(); i++) {
                total += this.handler.getAmountAsLong(i);
            }
            return total;
        }

        @Override
        public long getCapacity() {
            long total = 0;
            for (int i = 0; i < this.handler.size(); i++) {
                FluidResource held = this.handler.getResource(i);
                total += this.handler.getCapacityAsLong(i, held);
            }
            return total;
        }

        @Override
        public long fill(Fluid fluid, long amount, boolean simulate) {
            if (fluid == Fluids.EMPTY || amount <= 0) {
                return 0;
            }
            try (Transaction tx = Transaction.openRoot()) {
                int inserted = this.handler.insert(FluidResource.of(fluid), clampToInt(amount), tx);
                if (!simulate) {
                    tx.commit();
                }
                return inserted;
            }
        }

        @Override
        public long drain(long amount, boolean simulate) {
            Fluid held = getFluid();
            if (amount <= 0 || held == Fluids.EMPTY) {
                return 0;
            }
            try (Transaction tx = Transaction.openRoot()) {
                int extracted = this.handler.extract(FluidResource.of(held), clampToInt(amount), tx);
                if (!simulate) {
                    tx.commit();
                }
                return extracted;
            }
        }
    }
}
