package za.co.neroland.nerolandcore.sideconfig;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Locks the side-config model logic that does not need the game: the
 * mode&rarr;capability-view mapping, packed-int persistence round-trips, mode
 * cycling that skips forbidden modes, preset seeding with {@code allow(...)}
 * clamping, and slot-group gating. Pure JVM.
 */
class SideConfigModelTest {

    @Test
    void modeToCapabilityViewMapping() {
        assertTrue(SideMode.INPUT.canInsert());
        assertFalse(SideMode.INPUT.canExtract());
        assertFalse(SideMode.OUTPUT.canInsert());
        assertTrue(SideMode.OUTPUT.canExtract());
        assertTrue(SideMode.IO.canInsert());
        assertTrue(SideMode.IO.canExtract());
        assertFalse(SideMode.DISABLED.canInsert());
        assertFalse(SideMode.DISABLED.canExtract());
        assertFalse(SideMode.DISABLED.connects());
        assertTrue(SideMode.OUTPUT.connects());
        // PUSH is an active-eject output: extract-only as a capability view.
        assertTrue(SideMode.PUSH.canExtract());
        assertFalse(SideMode.PUSH.canInsert());
        assertTrue(SideMode.PUSH.autoEjects());
        assertTrue(SideMode.IO.autoInputs());
    }

    @Test
    void packUnpackRoundTrip() {
        ChannelConfig cfg = processorItem();
        cfg.setMode(RelativeFace.TOP, SideMode.INPUT);
        cfg.setMode(RelativeFace.BOTTOM, SideMode.OUTPUT);
        cfg.setMode(RelativeFace.LEFT, SideMode.IO);
        cfg.setAutoEjectLive(true);

        int packed = cfg.pack();
        ChannelConfig restored = processorItem();
        restored.unpack(packed);

        for (RelativeFace face : RelativeFace.VALUES) {
            assertEquals(cfg.mode(face), restored.mode(face), "face " + face);
        }
        assertTrue(restored.autoEject());
        assertFalse(restored.autoInput());
    }

    @Test
    void cycleSkipsForbiddenModes() {
        ChannelConfig cfg = new ChannelConfig(Channel.ENERGY);
        cfg.setAllowed(SideMode.INPUT, false);
        cfg.setAllowed(SideMode.IO, false);
        // Permitted now: DISABLED, OUTPUT (PUSH is off by default).
        cfg.setPreset(SidePreset.ALL_DISABLED);
        cfg.applyPreset();
        assertEquals(SideMode.DISABLED, cfg.mode(RelativeFace.FRONT));
        assertEquals(SideMode.OUTPUT, cfg.cycle(RelativeFace.FRONT));
        assertEquals(SideMode.DISABLED, cfg.cycle(RelativeFace.FRONT));
    }

    @Test
    void presetClampsForbiddenModeToAllowed() {
        ChannelConfig cfg = new ChannelConfig(Channel.ITEM);
        cfg.setAllowed(SideMode.IO, false); // STORAGE wants IO everywhere; forbidden -> INPUT
        cfg.setPreset(SidePreset.STORAGE);
        cfg.applyPreset();
        for (RelativeFace face : RelativeFace.VALUES) {
            assertEquals(SideMode.INPUT, cfg.mode(face));
        }
    }

    @Test
    void presetsSeedSensibleFaces() {
        ChannelConfig energy = new ChannelConfig(Channel.ENERGY);
        energy.setPreset(SidePreset.GENERATOR);
        energy.applyPreset();
        for (RelativeFace face : RelativeFace.VALUES) {
            assertEquals(SideMode.OUTPUT, energy.mode(face));
        }

        ChannelConfig item = processorItem();
        item.setPreset(SidePreset.PROCESSOR);
        item.applyPreset();
        assertEquals(SideMode.OUTPUT, item.mode(RelativeFace.BOTTOM));
        assertEquals(SideMode.INPUT, item.mode(RelativeFace.TOP));
        assertEquals(SideMode.INPUT, item.mode(RelativeFace.FRONT));
    }

    @Test
    void slotGroupsGateByMode() {
        ChannelConfig cfg = new ChannelConfig(Channel.ITEM);
        cfg.addInputGroup(SlotGroup.of("input", 0));
        cfg.addOutputGroup(SlotGroup.of("output", 1));
        cfg.setMode(RelativeFace.TOP, SideMode.INPUT);
        cfg.setMode(RelativeFace.BOTTOM, SideMode.OUTPUT);
        cfg.setMode(RelativeFace.LEFT, SideMode.IO);

        assertArrayEquals(new int[] { 0 }, cfg.insertSlots(RelativeFace.TOP));
        assertArrayEquals(new int[] {}, cfg.extractSlots(RelativeFace.TOP));
        assertArrayEquals(new int[] { 1 }, cfg.extractSlots(RelativeFace.BOTTOM));
        assertArrayEquals(new int[] {}, cfg.insertSlots(RelativeFace.BOTTOM));
        // I/O exposes both.
        assertArrayEquals(new int[] { 0 }, cfg.insertSlots(RelativeFace.LEFT));
        assertArrayEquals(new int[] { 1 }, cfg.extractSlots(RelativeFace.LEFT));
        // Disabled faces expose nothing.
        assertArrayEquals(new int[] {}, cfg.insertSlots(RelativeFace.BACK));
        assertArrayEquals(new int[] {}, cfg.extractSlots(RelativeFace.BACK));
    }

    @Test
    void perFaceOutputOverrideBindsSpecificGroup() {
        // The Item Sorter pattern: each output face binds to its own filtered group.
        ChannelConfig cfg = new ChannelConfig(Channel.ITEM);
        cfg.addInputGroup(SlotGroup.of("input", 0));
        cfg.bindFaceOutput(RelativeFace.LEFT, SlotGroup.of("left", 5));
        cfg.bindFaceOutput(RelativeFace.RIGHT, SlotGroup.of("right", 6));
        cfg.setMode(RelativeFace.LEFT, SideMode.OUTPUT);
        cfg.setMode(RelativeFace.RIGHT, SideMode.OUTPUT);
        assertArrayEquals(new int[] { 5 }, cfg.extractSlots(RelativeFace.LEFT));
        assertArrayEquals(new int[] { 6 }, cfg.extractSlots(RelativeFace.RIGHT));
    }

    private static ChannelConfig processorItem() {
        ChannelConfig cfg = new ChannelConfig(Channel.ITEM);
        cfg.addInputGroup(SlotGroup.of("input", 0));
        cfg.addOutputGroup(SlotGroup.of("output", 1));
        return cfg;
    }
}
