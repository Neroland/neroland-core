package za.co.neroland.nerolandcore.forge;

import net.minecraft.client.gui.screens.MenuScreens;
//? if >=1.21.6 {
import net.minecraftforge.eventbus.api.bus.BusGroup;
//?} else {
/*import net.minecraftforge.eventbus.api.IEventBus;
*///?}
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.client.TrashCanScreen;
import za.co.neroland.nerolandcore.registry.ModMenuTypes;

/**
 * Forge client setup — registers the screens for Core's shared block menus on the
 * main thread during {@code FMLClientSetupEvent}. Loaded only on the client (gated
 * behind {@code Dist.CLIENT} in the entry point).
 */
public final class ForgeClientSetup {

    private ForgeClientSetup() {
    }

    //? if >=1.21.6 {
    public static void init(BusGroup modBusGroup) {
        FMLClientSetupEvent.getBus(modBusGroup).addListener(ForgeClientSetup::onClientSetup);
    }
    //?} else {
    /*public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ForgeClientSetup::onClientSetup);
    }
    *///?}

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ForgeClientSetup::registerScreens);
        // Nero Mods Demo (26.3+): the button on the singleplayer world list.
        //? if >=26.3 {
        /*net.minecraftforge.client.event.ScreenEvent.Init.Post.BUS.addListener(e ->
                za.co.neroland.nerolandcore.demo.DemoClient.afterScreenInit(e.getScreen(), e::addListener));
        *///?}
    }

    private static void registerScreens() {
        try {
            MenuScreens.register(ModMenuTypes.TRASH_CAN.get(), TrashCanScreen::new);
            MenuScreens.register(ModMenuTypes.ORE_RECLAIMER.get(), za.co.neroland.nerolandcore.client.OreReclaimerScreen::new);
        } catch (NoClassDefFoundError | ExceptionInInitializerError brokenMenuScreens) {
            // MC-NEROLAND-CORE-8: vanilla's MenuScreens failed its static initialiser before we got here,
            // because another mod's mixin into a screen class it loads crashed (MixinTransformerError). The JVM
            // then refuses the class for everyone, so this call is only a secondary victim: the original failure
            // has already been logged and reported by the mod that first touched MenuScreens, and the client
            // cannot finish loading either way. Log at WARN (not reported) instead of adding a misleading
            // Neroland-attributed crash on top of the real one.
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Could not register the Trash Can screen: vanilla's "
                    + "MenuScreens failed to initialise earlier in startup, which is usually another mod's mixin "
                    + "failing to apply. Check the log above for the first MenuScreens / MixinTransformerError.",
                    brokenMenuScreens);
        }
    }
}
