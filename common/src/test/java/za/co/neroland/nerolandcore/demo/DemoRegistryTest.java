package za.co.neroland.nerolandcore.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import za.co.neroland.nerolandcore.demo.DemoTestSupport.FakeModule;

/** Registration rules, plot order, required-mod filtering and selection resolution. */
class DemoRegistryTest {

    @AfterEach
    void clear() {
        DemoRegistry.clearForTest();
    }

    private static List<String> ids(List<DemoModule> modules) {
        return modules.stream().map(DemoModule::modId).toList();
    }

    @Test
    void modulesAreListedBySortOrderThenModId() {
        DemoRegistry.register(new FakeModule("zeta", 10));
        DemoRegistry.register(new FakeModule("alpha", 10));
        DemoRegistry.register(new FakeModule("core", 0));
        DemoRegistry.register(new FakeModule("late", 2000));
        assertEquals(List.of("core", "alpha", "zeta", "late"), ids(DemoRegistry.modules()));
    }

    @Test
    void availableOnlyOffersModulesWhoseRequiredModsAreLoaded() {
        DemoRegistry.register(new FakeModule("solo", 1));
        DemoRegistry.register(new FakeModule("cross", 2, DemoFootprint.of(8, 8), List.of("solo", "other")));
        DemoRegistry.register(new FakeModule("self", 3, DemoFootprint.of(8, 8), List.of("self")));
        Set<String> loaded = Set.of("solo", "self");
        assertEquals(List.of("solo", "self"), ids(DemoRegistry.available(loaded::contains)),
                "a module listing its own id is not blocked by it; a missing other mod hides the module");
        Set<String> all = Set.of("solo", "other", "self");
        assertEquals(List.of("solo", "cross", "self"), ids(DemoRegistry.available(all::contains)));
    }

    @Test
    void resolveReturnsPlotOrderAndSkipsUnknownIdsAndDuplicates() {
        DemoRegistry.register(new FakeModule("b", 5));
        DemoRegistry.register(new FakeModule("a", 5));
        DemoRegistry.register(new FakeModule("first", 1));
        List<DemoModule> picked = DemoRegistry.resolve(List.of("b", "missing", "first", "b"));
        assertEquals(List.of("first", "b"), ids(picked));
        assertEquals(ids(picked), ids(DemoRegistry.resolve(List.of("first", "b"))),
                "the same selection in any order resolves identically");
        assertTrue(DemoRegistry.resolve(List.of()).isEmpty());
        assertTrue(DemoRegistry.resolve(null).isEmpty());
    }

    @Test
    void registrationRejectsBadModules() {
        assertThrows(IllegalArgumentException.class, () -> DemoRegistry.register(null));
        assertThrows(IllegalArgumentException.class, () -> DemoRegistry.register(new FakeModule("Bad Id", 1)));
        assertThrows(IllegalArgumentException.class,
                () -> DemoRegistry.register(new FakeModule("nofoot", 1, null, List.of())));
        assertThrows(IllegalArgumentException.class,
                () -> DemoRegistry.register(new FakeModule("badreq", 1, DemoFootprint.of(8, 8), List.of("No!"))));
        assertThrows(IllegalArgumentException.class, () -> DemoFootprint.of(4, 10));
        assertThrows(IllegalArgumentException.class, () -> DemoFootprint.of(10, DemoFootprint.MAX_SIZE + 1));
        assertTrue(DemoRegistry.modules().isEmpty());
    }

    @Test
    void reRegisteringReplacesAndTheRegistryIsCapped() {
        FakeModule first = new FakeModule("same", 1);
        FakeModule second = new FakeModule("same", 2);
        DemoRegistry.register(first);
        DemoRegistry.register(second);
        assertEquals(1, DemoRegistry.modules().size());
        assertSame(second, DemoRegistry.module("same").orElseThrow());
        for (int i = 1; i < DemoRegistry.MAX_MODULES; i++) {
            DemoRegistry.register(new FakeModule("m" + i, i));
        }
        assertThrows(IllegalArgumentException.class, () -> DemoRegistry.register(new FakeModule("overflow", 1)));
        DemoRegistry.register(new FakeModule("same", 3)); // replacing an existing id is still allowed when full
    }

    @Test
    void layoutUsesTheFootprintSnapshotTakenAtRegistration() {
        FakeModule m = new FakeModule("snap", 1, DemoFootprint.of(12, 7), List.of());
        DemoRegistry.register(m);
        m.footprint = DemoFootprint.of(40, 40);
        List<DemoLayout.Plot> plots = DemoRegistry.layout(DemoRegistry.modules());
        assertEquals(12, plots.get(0).sizeX());
        assertEquals(7, plots.get(0).sizeZ());
        assertEquals(1, m.footprintCalls);
    }
}
