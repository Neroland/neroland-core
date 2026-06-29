package za.co.neroland.nerolandcore.meteor;

import java.util.Objects;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

/**
 * One aggregated grindable-material declaration: which item it outputs, its rarity
 * {@link MeteorTier}, the optional progression gate and planet binding, an optional
 * weight override and an enabled flag. Built by {@link MeteorMaterialRegistry} from
 * data files ({@code data/<ns>/neroland/meteor_materials/<id>.json}) and
 * {@link GrindableMaterial} annotations, with data winning over annotations.
 *
 * <p>The {@link #id()} is the material's registry key (the file path id, or the item
 * id for annotation entries); {@link #item()} is the produced item. The record holds
 * <b>item metadata only</b> — no player data (POPIA/GDPR): see
 * {@code docs/METEOR-MATERIAL-REGISTRY.md}.
 */
public record MeteorMaterialEntry(
        Identifier id,
        Identifier item,
        MeteorTier tier,
        @Nullable Identifier minGate,
        @Nullable Identifier planet,
        @Nullable Integer weightOverride,
        boolean enabled) {

    public MeteorMaterialEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(tier, "tier");
    }

    /** Reduce to the Minecraft-free {@link MeteorResolution.Candidate} the algorithm consumes. */
    public MeteorResolution.Candidate toCandidate() {
        return new MeteorResolution.Candidate(
                id.toString(),
                tier,
                minGate == null ? null : minGate.toString(),
                planet == null ? null : planet.toString(),
                weightOverride,
                enabled);
    }
}
