package za.co.neroland.nerolandcore.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/** MinecraftForge entry point for Neroland Core. */
@Mod(NerolandCoreCommon.MOD_ID)
public final class NerolandCoreForge {

    public NerolandCoreForge(FMLJavaModLoadingContext context) {
        NerolandCoreCommon.LOGGER.info("[Neroland Core] Forge bootstrap");
        NerolandCoreCommon.init();
    }
}
