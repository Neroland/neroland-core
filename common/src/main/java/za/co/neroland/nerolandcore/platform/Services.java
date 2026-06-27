package za.co.neroland.nerolandcore.platform;

import java.util.ServiceLoader;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * Loads loader-specific service implementations via {@link ServiceLoader}.
 *
 * <p>This is the lightweight, dependency-free alternative to Architectury's
 * {@code @ExpectPlatform}. Common code calls {@code Services.PLATFORM.xxx()};
 * the correct Fabric / Forge / NeoForge implementation is resolved at runtime
 * from the {@code META-INF/services} entry shipped in each loader module.
 *
 * <p>Additional seams (config, networking, registration) load through the same
 * {@link #load(Class)} helper — see {@code RegistrationProvider.Factory}.
 */
public final class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    private Services() {
    }

    public static <T> T load(Class<T> clazz) {
        final T loaded = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException(
                        "No implementation found for service " + clazz.getName()));
        NerolandCoreCommon.LOGGER.debug("Loaded service {} -> {}",
                clazz.getSimpleName(), loaded.getClass().getName());
        return loaded;
    }
}
