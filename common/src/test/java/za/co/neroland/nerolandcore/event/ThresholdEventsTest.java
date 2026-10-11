package za.co.neroland.nerolandcore.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.resources.Identifier;

import org.junit.jupiter.api.Test;

import za.co.neroland.nerolandcore.event.ThresholdEvents.SourcedCrossing;
import za.co.neroland.nerolandcore.event.ThresholdEvents.ThresholdCrossing;

/** Publisher identity, source-id validation and listener isolation on the threshold bus. */
class ThresholdEventsTest {

    private static ThresholdCrossing crossing(String channelPath) {
        return new ThresholdCrossing(Identifier.fromNamespaceAndPath("test", channelPath), "region:0", 10, 5, true);
    }

    @Test
    void sourceIdsAcceptOnlyTheNarrowAlphabet() {
        assertTrue(SourceIds.isValidSourceModId("neroquests"));
        assertTrue(SourceIds.isValidSourceModId("my_mod.addon-2"));
        assertFalse(SourceIds.isValidSourceModId(null));
        assertFalse(SourceIds.isValidSourceModId(""));
        assertFalse(SourceIds.isValidSourceModId("NeroQuests"));
        assertFalse(SourceIds.isValidSourceModId("a".repeat(65)));
        assertTrue(SourceIds.isValidReason("quest_reward"));
        assertFalse(SourceIds.isValidReason("quest-reward"));
        assertFalse(SourceIds.isValidReason("a".repeat(33)));
        assertFalse(SourceIds.isValidReason(null));
    }

    @Test
    void sourcedListenersSeeThePublisherAndLegacyFireReportsUnknown() {
        List<SourcedCrossing> seen = new ArrayList<>();
        ThresholdEvents.onSourcedCrossing(c -> {
            if (c.crossing().channel().getPath().startsWith("sourced")) {
                seen.add(c);
            }
        });
        ThresholdEvents.fire("nerotech", crossing("sourced_a"));
        ThresholdEvents.fire(crossing("sourced_b"));
        ThresholdEvents.fire("Not Valid!", crossing("sourced_c"));
        assertEquals(3, seen.size());
        assertEquals("nerotech", seen.get(0).sourceModId());
        assertNull(seen.get(1).sourceModId());
        assertNull(seen.get(2).sourceModId());
    }

    @Test
    void aThrowingListenerDoesNotStopTheOthers() {
        AtomicInteger delivered = new AtomicInteger();
        ThresholdEvents.onCrossing(c -> {
            if (c.channel().getPath().equals("isolation")) {
                throw new IllegalStateException("boom");
            }
        });
        ThresholdEvents.onCrossing(c -> {
            if (c.channel().getPath().equals("isolation")) {
                delivered.incrementAndGet();
            }
        });
        ThresholdEvents.fire(crossing("isolation"));
        ThresholdEvents.fire("nerotech", crossing("isolation"));
        assertEquals(2, delivered.get());
    }
}
