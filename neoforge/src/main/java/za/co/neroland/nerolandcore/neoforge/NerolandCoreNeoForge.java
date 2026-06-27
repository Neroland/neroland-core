package za.co.neroland.nerolandcore.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.registry.NeoForgeRegistrationFactory;

/**
 * NeoForge entry point. Runs shared init (building the DeferredRegisters via the
 * RegistrationProvider seam), then attaches them to the mod event bus.
 */
@Mod(NerolandCoreCommon.MOD_ID)
public final class NerolandCoreNeoForge {

    public NerolandCoreNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        NerolandCoreCommon.LOGGER.info("[Neroland Core] NeoForge bootstrap");
        NerolandCoreCommon.init();
        NeoForgeRegistrationFactory.registerAll(modEventBus);
    }
}
