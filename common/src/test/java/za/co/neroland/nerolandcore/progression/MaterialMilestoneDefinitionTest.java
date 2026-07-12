package za.co.neroland.nerolandcore.progression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;

import net.minecraft.resources.Identifier;

class MaterialMilestoneDefinitionTest {

    @Test
    void datapackDefinitionRoundTripsTypedObservationKinds() {
        MaterialMilestone.Data data = new MaterialMilestone.Data(GateScope.TEAM,
                java.util.List.of(MaterialObservation.OWNER_MOD, MaterialObservation.PLANET_VISIT),
                "Known Material");
        var json = MaterialMilestone.DATA_CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        MaterialMilestone.Data decoded = MaterialMilestone.DATA_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();

        assertEquals(data, decoded);
        MaterialMilestone definition = new MaterialMilestone(Identifier.parse("test:known"), decoded);
        assertEquals(GateScope.TEAM, definition.scope());
        assertEquals(java.util.List.of(MaterialObservation.OWNER_MOD, MaterialObservation.PLANET_VISIT),
                definition.observations());
    }
}
