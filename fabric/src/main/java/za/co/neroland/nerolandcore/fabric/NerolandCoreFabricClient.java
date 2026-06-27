package za.co.neroland.nerolandcore.fabric;

import net.fabricmc.api.ClientModInitializer;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/** Fabric client entry point for Neroland Core. */
public final class NerolandCoreFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        NerolandCoreCommon.LOGGER.info("[Neroland Core] Fabric client bootstrap");
    }
}
