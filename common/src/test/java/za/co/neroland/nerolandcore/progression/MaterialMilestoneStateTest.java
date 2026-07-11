package za.co.neroland.nerolandcore.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

class MaterialMilestoneStateTest {

    private static final Identifier MILESTONE = Identifier.parse("nerolandcore:material_discovered");
    private static final Identifier IRON = Identifier.parse("minecraft:iron");
    private static final Identifier COPPER = Identifier.parse("minecraft:copper");

    @Test
    void scopesResolveWithoutLeakingSharedRowsIntoPlayerExport() {
        MaterialMilestoneState state = new MaterialMilestoneState();
        UUID player = UUID.randomUUID();

        state.setServer(MILESTONE, COPPER, true);
        state.setTeam("builders", MILESTONE, IRON, true);
        state.setPlayer(player, MILESTONE, IRON, true);

        assertTrue(state.containsServer(MILESTONE, COPPER));
        assertTrue(state.containsTeam("builders", MILESTONE, IRON));
        assertTrue(state.containsPlayer(player, MILESTONE, IRON));
        assertEquals(2, state.resolved(player, "builders").get(MILESTONE).size());
        assertEquals(java.util.Set.of(IRON), state.playerValues(player).get(MILESTONE));
    }

    @Test
    void playerErasureDropsOnlyUuidKeyedMaterialMilestones() {
        MaterialMilestoneState state = new MaterialMilestoneState();
        UUID player = UUID.randomUUID();
        state.setPlayer(player, MILESTONE, IRON, true);
        state.setServer(MILESTONE, COPPER, true);

        state.forgetPlayer(player);

        assertFalse(state.containsPlayer(player, MILESTONE, IRON));
        assertTrue(state.containsServer(MILESTONE, COPPER));
        assertTrue(state.playerValues(player).isEmpty());
    }

    @Test
    void revocationRemovesEmptyBuckets() {
        MaterialMilestoneState state = new MaterialMilestoneState();
        UUID player = UUID.randomUUID();
        assertTrue(state.setPlayer(player, MILESTONE, IRON, true));
        assertTrue(state.setPlayer(player, MILESTONE, IRON, false));
        assertFalse(state.setPlayer(player, MILESTONE, IRON, false));
        assertTrue(state.playerValues(player).isEmpty());
    }
}
