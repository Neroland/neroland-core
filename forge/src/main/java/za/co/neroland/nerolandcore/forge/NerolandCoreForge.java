package za.co.neroland.nerolandcore.forge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.command.CoreCommands;
import za.co.neroland.nerolandcore.network.CoreNetwork;
import za.co.neroland.nerolandcore.registry.ForgeRegistrationFactory;
import za.co.neroland.nerolandcore.telemetry.NerolandCoreTelemetry;

/**
 * MinecraftForge entry point. Runs shared init (building the DeferredRegisters
 * via the RegistrationProvider seam), attaches them to the mod bus group,
 * registers the networking channel, and wires the shared {@code /neroland}
 * command + config sync-on-join on the game bus.
 */
@Mod(NerolandCoreCommon.MOD_ID)
public final class NerolandCoreForge {

    public NerolandCoreForge(FMLJavaModLoadingContext context) {
        NerolandCoreCommon.LOGGER.info("[Neroland Core] Forge bootstrap");
        BusGroup modBusGroup = context.getModBusGroup();
        NerolandCoreCommon.init();
        // Anonymous, Neroland-Core-only crash reporting (opt-out via config; off-tagged in dev).
        NerolandCoreTelemetry.init();
        ForgeRegistrationFactory.registerAll(modBusGroup);
        ForgeNetwork.register();
        // Expose the shared storage blocks' energy/fluid/gas/item handlers cross-mod.
        ForgeCoreCapabilities.register();

        RegisterCommandsEvent.BUS.addListener(event -> CoreCommands.register(event.getDispatcher()));
        PlayerEvent.PlayerLoggedInEvent.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                CoreNetwork.onPlayerJoin(serverPlayer);
            }
        });
    }
}
