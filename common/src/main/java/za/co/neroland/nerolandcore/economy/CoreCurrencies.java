package za.co.neroland.nerolandcore.economy;

import net.minecraft.resources.Identifier;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/** Core's built-in currencies. NeroEconomy may add more; everything is keyed by {@link Currency}. */
public final class CoreCurrencies {

    /** The default global currency. */
    public static final Currency CREDITS = Currency.of(
            Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "credits"),
            "currency.nerolandcore.credits");

    private CoreCurrencies() {
    }
}
