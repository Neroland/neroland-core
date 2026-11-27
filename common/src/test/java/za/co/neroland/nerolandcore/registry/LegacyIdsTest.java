package za.co.neroland.nerolandcore.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

/** Save compatibility for ids that moved into Core: every old id resolves, nothing else does. */
class LegacyIdsTest {

    @Test
    void everyMovedOreKeepsItsPath() {
        for (String ore : Set.of("hematite_ore", "raw_hematite", "nerosium_ore", "deepslate_nerosium_ore",
                "raw_nerosium_block", "cheese_ore", "moon_cheese", "europium_ore", "raw_europium",
                "voidglass_ore", "voidglass", "amber_sap_ore", "brine_salt", "raw_vent_sulfide")) {
            assertEquals(ore, LegacyIds.blockOrItem("nerospace", ore), ore);
        }
        assertEquals("embedded_meteor_ore", LegacyIds.blockOrItem("neroevents", "embedded_meteor_ore"));
        assertEquals("pallurite_dust", LegacyIds.blockOrItem("neropower", "pallurite_dust"));
    }

    @Test
    void storageBlocksStillMapIncludingBlockEntities() {
        assertEquals("battery", LegacyIds.blockOrItem("nerospace", "battery"));
        assertEquals("trash_can", LegacyIds.blockEntityType("nerospace", "trash_can"));
    }

    @Test
    void unmovedIdsAreLeftAlone() {
        assertNull(LegacyIds.blockOrItem("nerospace", "helium3_pocket"));
        assertNull(LegacyIds.blockOrItem("nerospace", "nerosium_ingot"));
        assertNull(LegacyIds.blockOrItem("neropower", "pallurite_pellet"));
        assertNull(LegacyIds.blockOrItem("neroevents", "meteor_slag"));
        assertNull(LegacyIds.blockOrItem("minecraft", "iron_ore"));
        assertNull(LegacyIds.blockEntityType("nerospace", "hematite_ore"));
    }

    @Test
    void movedOresExcludeTheOlderStorageBlocks() {
        assertTrue(LegacyIds.movedOre("nerospace", "raw_hematite"));
        assertTrue(LegacyIds.movedOre("neroevents", "embedded_meteor_ore"));
        assertTrue(LegacyIds.movedOre("neropower", "pallurite_dust"));
        org.junit.jupiter.api.Assertions.assertFalse(LegacyIds.movedOre("nerospace", "battery"));
        org.junit.jupiter.api.Assertions.assertFalse(LegacyIds.movedOre("nerospace", "nerosium_ingot"));
    }

    @Test
    void tableCoversThirtyOreIds() {
        int ores = 0;
        for (Set<String> paths : LegacyIds.blocksAndItems().values()) {
            ores += paths.size();
        }
        // 9 storage ids + 28 Nerospace ore ids + NeroEvents' meteor ore + NeroPower's Pallurite Dust.
        assertEquals(39, ores);
        assertTrue(LegacyIds.blocksAndItems().keySet().containsAll(Set.of("nerospace", "neroevents", "neropower")));
    }
}
