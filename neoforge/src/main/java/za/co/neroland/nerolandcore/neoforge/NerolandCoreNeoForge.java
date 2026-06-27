package za.co.neroland.nerolandcore.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/** NeoForge entry point for Neroland Core. */
@Mod(NerolandCoreCommon.MOD_ID)
public final class NerolandCoreNeoForge {

    public NerolandCoreNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        NerolandCoreCommon.LOGGER.info("[Neroland Core] NeoForge bootstrap");
        NerolandCoreCommon.init();
    }
}
