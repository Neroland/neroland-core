package za.co.neroland.nerolandcore.energy;

/**
 * A simple bounded energy buffer backing a machine, implementing the
 * {@link NeroEnergyStorage} power type. Values fit in an int (NBT uses
 * {@code putInt}/{@code getIntOr} in 26.x); the interface is {@code long} for
 * headroom. {@code maxInsert}/{@code maxExtract} bound external I/O;
 * {@link #generate}/{@link #consume} bypass those for the machine's own internal
 * production/consumption.
 */
public final class EnergyBuffer implements NeroEnergyStorage {

    private int amount;
    private final int capacity;
    private final int maxInsert;
    private final int maxExtract;
    private final Runnable onChanged;

    public EnergyBuffer(int capacity, int maxInsert, int maxExtract, Runnable onChanged) {
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
        this.onChanged = onChanged;
    }

    @Override
    public long getAmount() {
        return this.amount;
    }

    @Override
    public long getCapacity() {
        return this.capacity;
    }

    @Override
    public long insert(long maxAmount, boolean simulate) {
        int accepted = (int) Math.max(0, Math.min(maxAmount, Math.min(this.maxInsert, this.capacity - this.amount)));
        if (accepted > 0 && !simulate) {
            this.amount += accepted;
            this.onChanged.run();
        }
        return accepted;
    }

    @Override
    public long extract(long maxAmount, boolean simulate) {
        int removed = (int) Math.max(0, Math.min(maxAmount, Math.min(this.maxExtract, this.amount)));
        if (removed > 0 && !simulate) {
            this.amount -= removed;
            this.onChanged.run();
        }
        return removed;
    }

    /** Internal generation (bypasses the insert limit) — for generators. */
    public void generate(int amount) {
        int add = Math.max(0, Math.min(amount, this.capacity - this.amount));
        if (add > 0) {
            this.amount += add;
            this.onChanged.run();
        }
    }

    /** Internal consumption (bypasses the extract limit) — for machines. */
    public void consume(int amount) {
        int removed = Math.max(0, Math.min(amount, this.amount));
        if (removed > 0) {
            this.amount -= removed;
            this.onChanged.run();
        }
    }

    /** True when at least {@code amount} NE is available (for "can I afford this work?" checks). */
    public boolean has(int amount) {
        return this.amount >= amount;
    }

    /** Raw accessors for NBT save/load. */
    public int getRaw() {
        return this.amount;
    }

    public void setRaw(int value) {
        this.amount = Math.max(0, Math.min(this.capacity, value));
    }
}
