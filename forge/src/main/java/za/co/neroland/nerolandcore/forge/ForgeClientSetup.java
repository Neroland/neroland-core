package za.co.neroland.nerolandcore.forge;

import net.minecraft.client.gui.screens.MenuScreens;
//? if >=1.21.6 {
import net.minecraftforge.eventbus.api.bus.BusGroup;
//?} else {
/*import net.minecraftforge.eventbus.api.IEventBus;
*///?}
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

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
    }

    private static void registerScreens() {
        MenuScreens.register(ModMenuTypes.TRASH_CAN.get(), TrashCanScreen::new);
    }
}
