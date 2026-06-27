package za.co.neroland.nerolandcore.platform;

import java.nio.file.Path;
import java.util.List;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * NeoForge implementation of {@link IPlatformHelper}. Registered via
 * {@code META-INF/services/za.co.neroland.nerolandcore.platform.IPlatformHelper}.
 */
public final class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        // 26.1.x exposes these as methods (the old FMLEnvironment.production / .dist fields were removed).
        return !FMLEnvironment.isProduction();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.getDist() == Dist.CLIENT;
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public String getModVersion() {
        return ModList.get().getModContainerById(NerolandCoreCommon.MOD_ID)
                .map(c -> c.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    @Override
    public List<String> getLoadedModIds() {
        return ModList.get().getMods().stream()
                .map(m -> m.getModId() + " " + m.getVersion())
                .sorted()
                .toList();
    }
}
