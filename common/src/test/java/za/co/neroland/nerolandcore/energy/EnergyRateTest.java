package za.co.neroland.nerolandcore.energy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EnergyRateTest {
    @Test
    void aggregatesTransferEventsAndKeepsIdleTicksInTheAverage() {
        EnergyRate rate = new EnergyRate();
        rate.record(30, 100);
        rate.record(30, 40);
        rate.record(31, 20);
        assertEquals(8.0, rate.perTick(31));
        assertEquals(1.0, rate.perTick(50));
        assertEquals(0.0, rate.perTick(51));
    }

    @Test
    void ignoresMeterReadsAndNonPositiveAmountsAndReusesExpiredSlots() {
        EnergyRate rate = new EnergyRate();
        rate.record(30, 100);
        rate.record(50, 40);
        rate.record(50, 0);
        rate.record(50, -40);
        assertEquals(2.0, rate.perTick(50));
        assertEquals(2.0, rate.perTick(50));
        assertEquals(0.0, rate.perTick(49));
    }
}
