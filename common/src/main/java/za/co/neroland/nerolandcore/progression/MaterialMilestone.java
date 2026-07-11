package za.co.neroland.nerolandcore.progression;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;

/** Datapack definition for a milestone whose values are stable material identifiers. */
public record MaterialMilestone(Identifier id, GateScope scope, List<MaterialObservation> observations,
        String title) {

    public static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GateScope.CODEC.optionalFieldOf("scope", GateScope.PLAYER).forGetter(Data::scope),
            MaterialObservation.CODEC.listOf().optionalFieldOf("observations", List.of(
                    MaterialObservation.OWNER_MOD, MaterialObservation.PLANET_VISIT,
                    MaterialObservation.PLAYER_PICKUP, MaterialObservation.ADMIN))
                    .forGetter(Data::observations),
            Codec.STRING.optionalFieldOf("title", "").forGetter(Data::title)
    ).apply(instance, Data::new));

    public MaterialMilestone(Identifier id, Data data) {
        this(id, data.scope(), List.copyOf(data.observations()), data.title());
    }

    public record Data(GateScope scope, List<MaterialObservation> observations, String title) {
    }
}
