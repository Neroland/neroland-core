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
        MenuScreens.register(ModMenuTypes.ORE_RECLAIMER.get(), za.co.neroland.nerolandcore.client.OreReclaimerScreen::new);
        // Nero Mods Demo (26.3+): the button on the singleplayer world list.
        //? if >=26.3 {
        /*net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, w, h) ->
                za.co.neroland.nerolandcore.demo.DemoClient.afterScreenInit(screen,
                        net.fabricmc.fabric.api.client.screen.v1.Screens.getWidgets(screen)::add));
        *///?}
    }
}
