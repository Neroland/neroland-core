package za.co.neroland.nerolandcore.fabric;

import net.fabricmc.api.ModInitializer;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/** Fabric entry point for Neroland Core. */
public final class NerolandCoreFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        NerolandCoreCommon.LOGGER.info("[Neroland Core] Fabric bootstrap");
        NerolandCoreCommon.init();
    }
}
