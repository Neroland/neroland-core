package za.co.neroland.nerolandcore.platform;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;

/**
 * Adapters between Core's loader-neutral {@link NeroFluidStorage} and Fabric's standard fluid
 * surface ({@code FluidStorage.SIDED}, a {@link Storage} of {@link FluidVariant}).
 *
 * <p>Downstream mods register {@link #asFluidStorage(NeroFluidStorage)} on {@code FluidStorage.SIDED}
 * so third-party pipes can fill and drain a Nero tank, and {@link FabricFluidLookup} uses
 * {@link #asNeroFluid(Storage)} so Nero blocks treat third-party tanks as Nero neighbours.
 *
 * <p><b>Units.</b> Core counts millibuckets; Fabric counts droplets, at
 * {@code FluidConstants.BUCKET / 1000 == 81} droplets per mB. Conversion always floors, so a pipe
 * moving a sub-millibucket sliver moves nothing rather than rounding fluid into existence — and a
 * Nero tank never reports more than it can actually deliver.
 */
public final class FabricFluidHandlers {

    /** Droplets in one millibucket (81 — Fabric's bucket is 81000 droplets, Core's is 1000 mB). */
    public static final long DROPLETS_PER_MB = FluidConstants.BUCKET / 1000L;

    private FabricFluidHandlers() {
    }

    /** Expose a Nero tank on Fabric's standard fluid storage surface. */
    public static Storage<FluidVariant> asFluidStorage(NeroFluidStorage store) {
        return new NeroToStandard(store);
    }

    /**
     * Expose several Nero tanks as one storage with a slot each. A machine with both a fluid tank and
     * a gas tank riding a transport fluid needs this: one block offers one fluid storage, but that
     * storage may hold many slots.
     */
    public static Storage<FluidVariant> asFluidStorage(List<? extends NeroFluidStorage> stores) {
        List<SingleSlotStorage<FluidVariant>> parts = new ArrayList<>(stores.size());
        for (NeroFluidStorage store : stores) {
            parts.add(new NeroToStandard(store));
        }
        return new CombinedStorage<>(parts);
    }

    /** Adapt a third-party {@link Storage} of {@link FluidVariant} to Core's contract. */
    public static NeroFluidStorage asNeroFluid(Storage<FluidVariant> storage) {
        return new StandardToNero(storage);
    }

    private static long toDroplets(long millibuckets) {
        return millibuckets * DROPLETS_PER_MB;
    }

    private static long toMillibuckets(long droplets) {
        return droplets / DROPLETS_PER_MB;
    }

    /** The state a transaction rolls back to: the tank's fluid and amount (mB) before the mutation. */
    private record Snapshot(Fluid fluid, long millibuckets) {
    }

    /**
     * Nero tank seen as a single-slot {@link Storage}. Mutations are applied immediately and undone
     * by {@link #readSnapshot(Snapshot)} if the transaction aborts, which is the
     * {@link SnapshotParticipant} contract for storage that is not itself transactional.
     */
    private static final class NeroToStandard extends SnapshotParticipant<Snapshot>
            implements SingleSlotStorage<FluidVariant> {

        private final NeroFluidStorage store;

        private NeroToStandard(NeroFluidStorage store) {
            this.store = store;
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            if (resource.hasComponents()) {
                return 0; // a Nero tank holds a bare fluid; never silently strip components
            }
            long offered = toMillibuckets(maxAmount);
            if (offered <= 0) {
                return 0;
            }
            long accepted = this.store.fill(resource.getFluid(), offered, true);
            if (accepted <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            return toDroplets(this.store.fill(resource.getFluid(), accepted, false));
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            if (resource.hasComponents() || this.store.getFluid() != resource.getFluid()) {
                return 0;
            }
            long requested = toMillibuckets(maxAmount);
            if (requested <= 0) {
                return 0;
            }
            long available = this.store.drain(requested, true);
            if (available <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            return toDroplets(this.store.drain(available, false));
        }

        @Override
        public boolean isResourceBlank() {
            return this.store.getFluid() == Fluids.EMPTY || this.store.getAmount() <= 0;
        }

        @Override
        public FluidVariant getResource() {
            return isResourceBlank() ? FluidVariant.blank() : FluidVariant.of(this.store.getFluid());
        }

        @Override
        public long getAmount() {
            return toDroplets(this.store.getAmount());
        }

        @Override
        public long getCapacity() {
            return toDroplets(this.store.getCapacity());
        }

        @Override
        protected Snapshot createSnapshot() {
            return new Snapshot(this.store.getFluid(), this.store.getAmount());
        }

        @Override
        protected void readSnapshot(Snapshot snapshot) {
            if (this.store.getFluid() != snapshot.fluid()) {
                this.store.drain(this.store.getAmount(), false);
                if (snapshot.millibuckets() > 0 && snapshot.fluid() != Fluids.EMPTY) {
                    this.store.fill(snapshot.fluid(), snapshot.millibuckets(), false);
                }
                return;
            }
            long delta = snapshot.millibuckets() - this.store.getAmount();
            if (delta > 0) {
                this.store.fill(snapshot.fluid(), delta, false);
            } else if (delta < 0) {
                this.store.drain(-delta, false);
            }
        }
    }

    /**
     * Third-party fluid storage seen as a Nero tank. Multi-slot storages are flattened: the reported
     * fluid is the first non-empty view's and amounts/capacities are summed.
     */
    private static final class StandardToNero implements NeroFluidStorage {

        private final Storage<FluidVariant> storage;

        private StandardToNero(Storage<FluidVariant> storage) {
            this.storage = storage;
        }

        @Override
        public Fluid getFluid() {
            for (StorageView<FluidVariant> view : this.storage.nonEmptyViews()) {
                return view.getResource().getFluid();
            }
            return Fluids.EMPTY;
        }

        @Override
        public long getAmount() {
            long droplets = 0;
            for (StorageView<FluidVariant> view : this.storage) {
                droplets += view.getAmount();
            }
            return toMillibuckets(droplets);
        }

        @Override
        public long getCapacity() {
            long droplets = 0;
            for (StorageView<FluidVariant> view : this.storage) {
                droplets += view.getCapacity();
            }
            return toMillibuckets(droplets);
        }

        // Both transfers run in two phases: probe in a transaction that is always rolled back to
        // learn the whole-millibucket amount, then move exactly that. Committing the raw droplet
        // figure would move a sub-millibucket remainder the caller never sees, which — because the
        // caller drains its own side by the RETURNED amount — creates or destroys fluid.

        @Override
        public long fill(Fluid fluid, long amount, boolean simulate) {
            if (fluid == Fluids.EMPTY || amount <= 0 || !this.storage.supportsInsertion()) {
                return 0;
            }
            FluidVariant variant = FluidVariant.of(fluid);
            long accepted;
            try (Transaction probe = Transaction.openOuter()) {
                accepted = toMillibuckets(this.storage.insert(variant, toDroplets(amount), probe));
            }
            if (accepted <= 0 || simulate) {
                return Math.max(0, accepted);
            }
            try (Transaction tx = Transaction.openOuter()) {
                long inserted = this.storage.insert(variant, toDroplets(accepted), tx);
                if (inserted != toDroplets(accepted)) {
                    return 0; // partial accept on the second pass: roll back rather than split a mB
                }
                tx.commit();
                return accepted;
            }
        }

        @Override
        public long drain(long amount, boolean simulate) {
            Fluid held = getFluid();
            if (amount <= 0 || held == Fluids.EMPTY || !this.storage.supportsExtraction()) {
                return 0;
            }
            FluidVariant variant = FluidVariant.of(held);
            long available;
            try (Transaction probe = Transaction.openOuter()) {
                available = toMillibuckets(this.storage.extract(variant, toDroplets(amount), probe));
            }
            if (available <= 0 || simulate) {
                return Math.max(0, available);
            }
            try (Transaction tx = Transaction.openOuter()) {
                long extracted = this.storage.extract(variant, toDroplets(available), tx);
                if (extracted != toDroplets(available)) {
                    return 0;
                }
                tx.commit();
                return available;
            }
        }
    }
}
