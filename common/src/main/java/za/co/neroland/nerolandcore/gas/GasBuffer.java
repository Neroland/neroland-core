package za.co.neroland.nerolandcore.gas;

import net.minecraft.resources.Identifier;

/**
 * A single-gas bounded store (millibuckets) backing a block entity, implementing
 * the {@link NeroGasStorage} gas type. Mirrors
 * {@link za.co.neroland.nerolandcore.fluid.FluidBuffer}; raw accessors back NBT
 * save/load.
 */
public final class GasBuffer implements NeroGasStorage {

    private Identifier gas = NeroGases.EMPTY;
    private long amount;
    private long capacity;
    private final Runnable onChanged;

    public GasBuffer(long capacity, Runnable onChanged) {
        this.capacity = capacity;
        this.onChanged = onChanged;
    }

    @Override
    public Identifier getGas() {
        return this.gas;
    }

    @Override
    public long getAmount() {
        return this.amount;
    }

    @Override
    public long getCapacity() {
        return this.capacity;
    }

    /** Adjust the store's capacity; clamps the held amount to the new bound. */
    public void resize(long newCapacity) {
        this.capacity = Math.max(0, newCapacity);
        if (this.amount > this.capacity) {
            this.amount = this.capacity;
            if (this.amount == 0) {
                this.gas = NeroGases.EMPTY;
            }
            this.onChanged.run();
        }
    }

    @Override
    public long fill(Identifier gas, long amount, boolean simulate) {
        if (amount <= 0 || NeroGases.isEmpty(gas)) {
            return 0;
        }
        if (!NeroGases.isEmpty(this.gas) && !this.gas.equals(gas)) {
            return 0;
        }
        long filled = Math.min(amount, this.capacity - this.amount);
        if (filled > 0 && !simulate) {
            if (NeroGases.isEmpty(this.gas)) {
                this.gas = gas;
            }
            this.amount += filled;
            this.onChanged.run();
        }
        return filled;
    }

    @Override
    public long drain(long amount, boolean simulate) {
        if (amount <= 0 || this.amount == 0) {
            return 0;
        }
        long drained = Math.min(amount, this.amount);
        if (!simulate) {
            this.amount -= drained;
            if (this.amount == 0) {
                this.gas = NeroGases.EMPTY;
            }
            this.onChanged.run();
        }
        return drained;
    }

    // Raw accessors for NBT save/load.
    public Identifier getRawGas() {
        return this.gas;
    }

    public int getRawAmount() {
        return (int) this.amount;
    }

    public void setRaw(Identifier gas, int amount) {
        this.gas = gas == null ? NeroGases.EMPTY : gas;
        this.amount = Math.max(0, Math.min((int) this.capacity, amount));
        if (this.amount == 0) {
            this.gas = NeroGases.EMPTY;
        }
    }
}
