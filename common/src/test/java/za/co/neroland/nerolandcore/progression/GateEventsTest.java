package za.co.neroland.nerolandcore.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.resources.Identifier;

import org.junit.jupiter.api.Test;

/** A throwing gate listener is isolated from the rest. */
class GateEventsTest {

    @Test
    void aThrowingListenerDoesNotStopTheOthers() {
        Identifier gate = Identifier.fromNamespaceAndPath("test", "isolation_gate");
        AtomicInteger delivered = new AtomicInteger();
        GateEvents.onChange(c -> {
            if (c.gate().equals(gate)) {
                throw new IllegalStateException("boom");
            }
        });
        GateEvents.onChange(c -> {
            if (c.gate().equals(gate)) {
                delivered.incrementAndGet();
            }
        });
        GateEvents.fire(new GateEvents.GateChange(GateScope.SERVER, "", gate, true));
        GateEvents.fire(new GateEvents.GateChange(GateScope.SERVER, "", gate, false));
        assertEquals(2, delivered.get());
    }
}
