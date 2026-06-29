package za.co.neroland.nerolandcore.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.client.TrashCanScreen;
import za.co.neroland.nerolandcore.registry.ModMenuTypes;

/** Fabric client entry point for Neroland Core. */
public final class NerolandCoreFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        NerolandCoreCommon.LOGGER.info("[Neroland Core] Fabric client bootstrap");
        // Clientbound receivers (client-only API) — registered here, off the dedicated server.
        FabricNetwork.registerClient();
        // Menu screens for Core's shared block GUIs.
        MenuScreens.register(ModMenuTypes.TRASH_CAN.get(), TrashCanScreen::new);
    }
}
