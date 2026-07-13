package za.co.neroland.nerolandcore.palette;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

/**
 * A single named colour/finish in the shared Neroland palette — the unit the
 * {@link PaletteRegistry} hands out so any mod or data pack can match trims to the
 * exact tone Core uses on a material or a machine casing.
 *
 * <p>A finish is <b>pure cosmetic metadata</b>: an sRGB colour, whether it reads as
 * emissive (self-lit), the block light level it implies when emissive, and an optional
 * pointer to the canonical material tag it derives from. It carries no behaviour and no
 * player data — it is safe to read on either side and to serialise into a data pack.
 *
 * @param id           the finish id, namespaced (Core's own live under {@code neroland:…})
 * @param name         a stable, human-readable label (consumers may translate via their
 *                     own lang; not itself a translation key)
 * @param rgb          packed sRGB colour as {@code 0xRRGGBB} (no alpha)
 * @param emissive     whether this finish is meant to render fullbright / self-lit
 * @param emissiveLight the block light level (0–15) implied when {@code emissive}; 0 otherwise
 * @param material     optional canonical material tag/id this finish derives from
 *                     (e.g. {@code c:ingots/nero_alloy}); {@code null} for pure accents
 */
public record Finish(Identifier id, String name, int rgb, boolean emissive, int emissiveLight,
                     @Nullable Identifier material) {

    /** Convenience: a non-emissive finish with no material link. */
    public static Finish accent(Identifier id, String name, int rgb) {
        return new Finish(id, name, rgb & 0xFFFFFF, false, 0, null);
    }

    /** Convenience: a material-derived finish (optionally emissive). */
    public static Finish material(Identifier id, String name, int rgb, boolean emissive, int emissiveLight,
                                  @Nullable Identifier material) {
        return new Finish(id, name, rgb & 0xFFFFFF, emissive, emissive ? emissiveLight : 0, material);
    }

    /** Red channel, 0–255. */
    public int red() {
        return (rgb >> 16) & 0xFF;
    }

    /** Green channel, 0–255. */
    public int green() {
        return (rgb >> 8) & 0xFF;
    }

    /** Blue channel, 0–255. */
    public int blue() {
        return rgb & 0xFF;
    }
}
