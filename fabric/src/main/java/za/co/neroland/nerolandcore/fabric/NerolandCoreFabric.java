package za.co.neroland.nerolandcore.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.command.CoreCommands;
import za.co.neroland.nerolandcore.network.CoreNetwork;
import za.co.neroland.nerolandcore.telemetry.NerolandCoreTelemetry;

/** Fabric entry point for Neroland Core. */
public final class NerolandCoreFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        NerolandCoreCommon.LOGGER.info("[Neroland Core] Fabric bootstrap");
        NerolandCoreCommon.init();
        // Anonymous, Neroland-Core-only crash reporting (opt-out via config; off-tagged in dev).
        NerolandCoreTelemetry.init();
        FabricNetwork.registerCommon();
        // Expose the shared storage blocks' energy/fluid/gas/item handlers cross-mod.
        FabricCoreCapabilities.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                CoreCommands.register(dispatcher));

        // Push server-authoritative config + the player's open gates as they join.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                CoreNetwork.onPlayerJoin(handler.player));
    }
}
