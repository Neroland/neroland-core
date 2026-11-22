package za.co.neroland.nerolandcore.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConfigNaNTest {

    @Test
    void nanFallsBackToTheClampedDefault() {
        ConfigValue<Double> value = ConfigSchema.create("nantest", "test")
                .doubleRange("ratio", 2.5D, 0D, 10D, false, "test");
        value.loadFrom("NaN");
        assertEquals(2.5D, value.get());
        value.loadFrom("Infinity");
        assertEquals(10D, value.get());
        value.loadFrom("-3");
        assertEquals(0D, value.get());
    }
}
