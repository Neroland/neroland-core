package za.co.neroland.nerolandcore.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Plot layout determinism and geometry, and the client-to-server selection hand-off. */
class DemoLayoutTest {

    private static List<DemoLayout.Request> requests(int... sizes) {
        List<DemoLayout.Request> out = new ArrayList<>();
        for (int i = 0; i < sizes.length; i += 2) {
            out.add(new DemoLayout.Request("m" + i, DemoFootprint.of(sizes[i], sizes[i + 1])));
        }
        return out;
    }

    @Test
    void sameRequestsAlwaysGiveTheSameLayout() {
        List<DemoLayout.Plot> a = DemoLayout.plan(requests(13, 11, 40, 30, 7, 9));
        List<DemoLayout.Plot> b = DemoLayout.plan(requests(13, 11, 40, 30, 7, 9));
        assertEquals(a, b);
    }

    @Test
    void plotsRunEastOfTheHubAlongThePathWithoutOverlapping() {
        List<DemoLayout.Plot> plots = DemoLayout.plan(requests(13, 11, 40, 30, 7, 9));
        assertEquals(DemoLayout.HUB_RADIUS + 1 + DemoLayout.PLOT_GAP, plots.get(0).minX());
        for (int i = 0; i < plots.size(); i++) {
            DemoLayout.Plot p = plots.get(i);
            assertEquals(i, p.index());
            assertTrue(p.minZ() <= 0 && p.maxZ() >= 0, "every plot straddles the path at z = 0");
            assertTrue(Math.abs(p.minZ() + p.maxZ()) <= 1, "plots are centred on the path");
            if (i > 0) {
                assertEquals(plots.get(i - 1).maxX() + 1 + DemoLayout.PLOT_GAP, p.minX());
            }
        }
        assertEquals(plots.get(2).maxX(), DemoLayout.pathEndX(plots));
        assertEquals(DemoLayout.HUB_RADIUS, DemoLayout.pathEndX(List.of()));
    }

    @Test
    void layoutBoundsAreEnforced() {
        List<DemoLayout.Request> many = new ArrayList<>();
        for (int i = 0; i <= DemoLayout.MAX_PLOTS; i++) {
            many.add(new DemoLayout.Request("m" + i, DemoFootprint.of(5, 5)));
        }
        assertThrows(IllegalArgumentException.class, () -> DemoLayout.plan(many));
        List<DemoLayout.Request> withNull = new ArrayList<>();
        withNull.add(null);
        assertThrows(IllegalArgumentException.class, () -> DemoLayout.plan(withNull));
        assertTrue(DemoLayout.plan(null).isEmpty());
    }

    @Test
    void pendingSelectionIsTakenOnceAndOnlyByTheMatchingWorld() {
        DemoPending.offer("Nero Mods Demo", List.of("nerolandcore", "nerospace"));
        assertNull(DemoPending.take("Some Other World"));
        DemoPending.Request r = DemoPending.take("Nero Mods Demo");
        assertEquals(List.of("nerolandcore", "nerospace"), r.moduleIds());
        assertNull(DemoPending.take("Nero Mods Demo"), "a request is handed over once");
        DemoPending.offer("Nero Mods Demo", List.of("x"));
        DemoPending.clear();
        assertNull(DemoPending.take("Nero Mods Demo"));
        assertTrue(new DemoPending.Request("w", null).moduleIds().isEmpty());
    }
}
