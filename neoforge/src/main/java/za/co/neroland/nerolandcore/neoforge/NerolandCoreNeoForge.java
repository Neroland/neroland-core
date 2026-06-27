package za.co.neroland.nerolandcore.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.command.CoreCommands;
import za.co.neroland.nerolandcore.network.CoreNetwork;
import za.co.neroland.nerolandcore.registry.NeoForgeRegistrationFactory;
import za.co.neroland.nerolandcore.telemetry.NerolandCoreTelemetry;

/**
 * NeoForge entry point. Runs shared init (building the DeferredRegisters via the
 * RegistrationProvider seam), attaches them to the mod event bus, registers the
 * networking payloads, and wires the shared {@code /neroland} command + config
 * sync-on-join on the game bus.
 */
@Mod(NerolandCoreCommon.MOD_ID)
public final class NerolandCoreNeoForge {

    public NerolandCoreNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        NerolandCoreCommon.LOGGER.info("[Neroland Core] NeoForge bootstrap");
        NerolandCoreCommon.init();
        // Anonymous, Neroland-Core-only crash reporting (opt-out via config; off-tagged in dev).
        NerolandCoreTelemetry.init();
        NeoForgeRegistrationFactory.registerAll(modEventBus);
        NeoForgeNetwork.register(modEventBus);

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                CoreCommands.register(event.getDispatcher()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                CoreNetwork.onPlayerJoin(serverPlayer);
            }
        });
    }
}
