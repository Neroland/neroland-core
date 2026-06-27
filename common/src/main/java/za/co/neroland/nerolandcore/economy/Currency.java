package za.co.neroland.nerolandcore.economy;

import net.minecraft.resources.Identifier;

/**
 * A named currency. The economy supports multiple currencies from day one (a
 * global credit, per-faction or per-planet scrip, …) — every balance is keyed by a
 * {@code Currency}, so providers and callers never assume a single wallet.
 *
 * @param id             the currency's unique id (e.g. {@code nerolandcore:credits})
 * @param translationKey lang key for its display name (e.g. {@code currency.nerolandcore.credits})
 */
public record Currency(Identifier id, String translationKey) {

    public static Currency of(Identifier id, String translationKey) {
        return new Currency(id, translationKey);
    }
}
