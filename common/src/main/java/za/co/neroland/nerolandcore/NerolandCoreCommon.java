package za.co.neroland.nerolandcore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import za.co.neroland.nerolandcore.config.CoreConfig;
import za.co.neroland.nerolandcore.data.CoreData;
import za.co.neroland.nerolandcore.network.CoreNetwork;
import za.co.neroland.nerolandcore.platform.Services;
import za.co.neroland.nerolandcore.registry.CoreRegistries;

/**
 * Loader-agnostic entry point for Neroland Core. Each loader entry point
 * (Fabric / Forge / NeoForge) calls {@link #init()} once during mod
 * construction. Loader-specific behaviour is reached only through
 * {@link Services}, keeping this module free of {@code net.neoforged.*} /
 * {@code net.fabricmc.*} / {@code net.minecraftforge.*} imports.
 */
public final class NerolandCoreCommon {

    public static final String MOD_ID = "nerolandcore";
    public static final Logger LOGGER = LoggerFactory.getLogger("Neroland Core");

    private NerolandCoreCommon() {
    }

    /** Called once per loader during mod construction. */
    public static void init() {
        LOGGER.info("[Neroland Core] common init on platform: {} (dev={})",
                Services.PLATFORM.getPlatformName(),
                Services.PLATFORM.isDevelopmentEnvironment());

        // Config first: register + load Core's schema so later systems can read it.
        CoreConfig.init();

        // Register Core's systems with the shared per-player erasure hook (POPIA/GDPR).
        CoreData.init();

        // Shared content registration via the RegistrationProvider seam. On
        // NeoForge / Forge this builds DeferredRegisters (the loader entry point
        // then attaches them to the mod bus); on Fabric it registers eagerly.
        CoreRegistries.init();

        // Populate the payload registry before each loader wires it to its network API.
        CoreNetwork.init();
    }
}
