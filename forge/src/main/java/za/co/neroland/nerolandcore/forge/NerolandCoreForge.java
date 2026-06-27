package za.co.neroland.nerolandcore.forge;

import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.registry.ForgeRegistrationFactory;

/**
 * MinecraftForge entry point. Runs shared init (building the DeferredRegisters
 * via the RegistrationProvider seam), then attaches them to the mod bus group.
 */
@Mod(NerolandCoreCommon.MOD_ID)
public final class NerolandCoreForge {

    public NerolandCoreForge(FMLJavaModLoadingContext context) {
        NerolandCoreCommon.LOGGER.info("[Neroland Core] Forge bootstrap");
        BusGroup modBusGroup = context.getModBusGroup();
        NerolandCoreCommon.init();
        ForgeRegistrationFactory.registerAll(modBusGroup);
    }
}
