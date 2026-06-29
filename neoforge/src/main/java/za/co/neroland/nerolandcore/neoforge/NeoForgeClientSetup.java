package za.co.neroland.nerolandcore.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import za.co.neroland.nerolandcore.client.TrashCanScreen;
import za.co.neroland.nerolandcore.registry.ModMenuTypes;

/**
 * NeoForge client setup — registers the screens for Core's shared block menus.
 * Loaded only on the client (gated behind {@code Dist.CLIENT} in the entry point),
 * so its client-only references never touch a dedicated server.
 */
public final class NeoForgeClientSetup {

    private NeoForgeClientSetup() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(NeoForgeClientSetup::onRegisterScreens);
    }

    private static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.TRASH_CAN.get(), TrashCanScreen::new);
    }
}
