package za.co.neroland.nerolandcore.gas;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * Helpers for Core's loader-neutral gas layer. A gas is identified generically by
 * an {@link Identifier} (Core deliberately ships no concrete gases — content mods
 * such as Nerospace own their own gases, e.g. {@code nerospace:oxygen}). The
 * {@link #EMPTY} sentinel marks an empty store.
 */
public final class NeroGases {

    /** Sentinel id for "no gas stored". */
    public static final Identifier EMPTY =
            Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "empty");

    private NeroGases() {
    }

    /** True when {@code gas} is null or the {@link #EMPTY} sentinel. */
    public static boolean isEmpty(Identifier gas) {
        return gas == null || gas.equals(EMPTY);
    }

    /**
     * Display label for a gas, derived from its id as
     * {@code gas.<namespace>.<path>} — so a content mod's existing translation key
     * (e.g. {@code gas.nerospace.oxygen}) resolves automatically.
     */
    public static Component label(Identifier gas) {
        if (isEmpty(gas)) {
            return Component.translatable("gas." + NerolandCoreCommon.MOD_ID + ".empty");
        }
        return Component.translatable("gas." + gas.getNamespace() + "." + gas.getPath());
    }
}
