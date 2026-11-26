package za.co.neroland.nerolandcore.energy;

/** Actual energy moved over the most recent 20 game ticks. Idle ticks contribute zero. */
public final class EnergyRate {
    private static final int WINDOW = 20;
    private final long[] ticks = new long[WINDOW];
    private final long[] amounts = new long[WINDOW];

    public void record(long tick, long amount) {
        if (amount <= 0) {
            return;
        }
        int slot = Math.floorMod(tick, WINDOW);
        if (this.ticks[slot] != tick) {
            this.ticks[slot] = tick;
            this.amounts[slot] = 0;
        }
        this.amounts[slot] += amount;
    }

    public double perTick(long now) {
        long total = 0;
        for (int slot = 0; slot < WINDOW; slot++) {
            long age = now - this.ticks[slot];
            if (age >= 0 && age < WINDOW) {
                total += this.amounts[slot];
            }
        }
        return (double) total / WINDOW;
    }
}
