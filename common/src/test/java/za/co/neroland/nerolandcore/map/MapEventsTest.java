package za.co.neroland.nerolandcore.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static za.co.neroland.nerolandcore.map.MapTestSupport.OVERWORLD;
import static za.co.neroland.nerolandcore.map.MapTestSupport.id;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.resources.Identifier;

import org.junit.jupiter.api.Test;

import za.co.neroland.nerolandcore.map.MapEvents.LayerChanged;

/** Broadcast and audience hints, listener isolation and privacy of the change event. */
class MapEventsTest {

    private static final Identifier LAYER = id("evt", "layer");

    @Test
    void listenersReceiveBroadcastAndAudienceHints() {
        List<LayerChanged> seen = new ArrayList<>();
        Consumer<LayerChanged> listener = c -> {
            if (c.layer().equals(LAYER)) {
                seen.add(c);
            }
        };
        MapEvents.onLayerChanged(listener);
        try {
            UUID member = UUID.randomUUID();
            MapEvents.fire(LAYER, OVERWORLD, MapBounds.around(0, 0, 16));
            MapEvents.fireFor(member, LAYER, OVERWORLD, null);
            assertEquals(2, seen.size());
            assertNull(seen.get(0).audience());
            assertEquals(member, seen.get(1).audience());
            assertNull(seen.get(1).area());
        } finally {
            assertTrue(MapEvents.removeListener(listener));
        }
        MapEvents.fire(LAYER, OVERWORLD, null);
        assertEquals(2, seen.size(), "a removed listener hears nothing");
        assertFalse(MapEvents.removeListener(listener));
    }

    @Test
    void aThrowingListenerDoesNotStopTheOthers() {
        List<LayerChanged> seen = new ArrayList<>();
        Consumer<LayerChanged> bad = c -> {
            throw new IllegalStateException("listener bug");
        };
        Consumer<LayerChanged> good = c -> {
            if (c.layer().equals(LAYER)) {
                seen.add(c);
            }
        };
        MapEvents.onLayerChanged(bad);
        MapEvents.onLayerChanged(good);
        try {
            MapEvents.fire(LAYER, OVERWORLD, null);
            assertEquals(1, seen.size());
        } finally {
            MapEvents.removeListener(bad);
            MapEvents.removeListener(good);
        }
    }

    @Test
    void theEventNeverPrintsItsAudience() {
        UUID member = UUID.randomUUID();
        LayerChanged c = new LayerChanged(LAYER, OVERWORLD, null, member);
        assertFalse(c.toString().contains(member.toString()));
        assertTrue(c.toString().contains("audience=one"));
    }

    @Test
    void malformedEventsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> MapEvents.fireFor(null, LAYER, OVERWORLD, null));
        assertThrows(IllegalArgumentException.class, () -> MapEvents.fire(null, OVERWORLD, null));
        assertThrows(IllegalArgumentException.class, () -> MapEvents.fire(LAYER, null, null));
        MapEvents.fire((LayerChanged) null);
    }
}
