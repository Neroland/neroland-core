package za.co.neroland.nerolandcore.progression;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;

/**
 * A progression-gate definition: its id, {@link GateScope}, the gates that must be
 * open before it may open ({@link #requires()}), and an optional display title.
 *
 * <p>Definitions are datapack-driven — one JSON per gate under
 * {@code data/<namespace>/neroland_gates/<path>.json}; the id comes from the file
 * path, the rest from {@link #DATA_CODEC}. Core ships the canonical arc gates and a
 * pack may add, override or reorder them. State (which gates are open) lives
 * separately in {@link ProgressionState}.
 */
public record Gate(Identifier id, GateScope scope, List<Identifier> requires, String title) {

    /** The file-body codec (everything except the id, which is taken from the file path). */
    public static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            GateScope.CODEC.optionalFieldOf("scope", GateScope.SERVER).forGetter(Data::scope),
            Identifier.CODEC.listOf().optionalFieldOf("requires", List.of()).forGetter(Data::requires),
            Codec.STRING.optionalFieldOf("title", "").forGetter(Data::title)
    ).apply(inst, Data::new));

    public Gate(Identifier id, Data data) {
        this(id, data.scope(), data.requires(), data.title());
    }

    /** The decoded JSON body of a gate file, before the id (from the path) is attached. */
    public record Data(GateScope scope, List<Identifier> requires, String title) {
    }
}
