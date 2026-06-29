package za.co.neroland.nerolandcore.sideconfig;

import java.util.Locale;

/**
 * A resource channel a machine can expose on its faces. Each maps onto an existing
 * Core storage layer (see the side-config docs):
 *
 * <ul>
 *   <li>{@link #ITEM} — the item/inventory capability (slots are grouped, see {@link SlotGroup}).</li>
 *   <li>{@link #FLUID} — {@link za.co.neroland.nerolandcore.fluid.NeroFluidStorage} / {@code FluidBuffer}.</li>
 *   <li>{@link #GAS} — {@link za.co.neroland.nerolandcore.gas.NeroGasStorage} / {@code GasBuffer}.</li>
 *   <li>{@link #ENERGY} — the Core power-type capability (the UI labels this <b>Power</b>).</li>
 * </ul>
 *
 * <p>Part of Neroland Core's universal machine side-configuration system. The model
 * is loader-neutral; per-loader capability wiring lives in each loader module.
 */
public enum Channel {

    ITEM,
    FLUID,
    GAS,
    ENERGY;

    /** Stable iteration order used for packing/serialisation; never reorder. */
    public static final Channel[] VALUES = values();

    /** Lower-case id form, e.g. {@code "item"} — used for NBT keys and translation keys. */
    public String lowerName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
