package za.co.neroland.nerolandcore.energy;

import za.co.neroland.nerolandcore.config.CoreConfig;

/**
 * Converts between Nero energy units (NE) and Forge Energy (FE), using the
 * config-driven ratio {@link CoreConfig#NERO_ENERGY_TO_FE} (1 NE = N FE; defaults
 * to rough parity). The per-loader energy bridges use this when adapting Core
 * machines to a loader's native FE-style energy capability.
 */
public final class EnergyConversions {

    private EnergyConversions() {
    }

    /** The current 1-NE-to-FE ratio. */
    public static double neroToForgeRatio() {
        return CoreConfig.NERO_ENERGY_TO_FE.get();
    }

    /** Convert Nero energy units to Forge Energy. */
    public static long neroToForge(long nero) {
        return Math.round(nero * neroToForgeRatio());
    }

    /** Convert Forge Energy to Nero energy units. */
    public static long forgeToNero(long forgeEnergy) {
        double ratio = neroToForgeRatio();
        return ratio <= 0 ? 0 : Math.round(forgeEnergy / ratio);
    }
}
