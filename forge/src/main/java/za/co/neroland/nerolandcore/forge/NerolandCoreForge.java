package za.co.neroland.nerolandcore.forge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
//? if >=1.21.6 {
import net.minecraftforge.eventbus.api.bus.BusGroup;
//?} else {
/*import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
*///?}
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.command.CoreCommands;
import za.co.neroland.nerolandcore.entity.ForgeEntityRegistrationPlumbing;
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
        //? if >=1.21.6 {
        BusGroup modBusGroup = context.getModBusGroup();
        //?} else {
        /*IEventBus modBusGroup = context.getModEventBus();
        *///?}
        NerolandCoreCommon.init();
        // Anonymous, Neroland-Core-only crash reporting (opt-out via config; off-tagged in dev).
        NerolandCoreTelemetry.init();
        ForgeRegistrationFactory.registerAll(modBusGroup);
        // Flush listeners for the shared entity seam (default attributes + spawn placements). Both
        // events share one static bus, so this single installation also serves downstream mods.
        //? if >=1.21.9 {
        ForgeEntityRegistrationPlumbing.installListeners();
        //?} else {
        /*ForgeEntityRegistrationPlumbing.installListeners(modBusGroup);
        *///?}
        ForgeNetwork.register();
        // Expose the shared storage blocks' energy/fluid/gas/item handlers cross-mod.
        ForgeCoreCapabilities.register();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeClientSetup.init(modBusGroup);
        }

        //? if >=1.21.6 {
        RegisterCommandsEvent.BUS.addListener(event -> CoreCommands.register(event.getDispatcher()));
        PlayerEvent.PlayerLoggedInEvent.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                CoreNetwork.onPlayerJoin(serverPlayer);
            }
        });
        //?} else {
        /*MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> CoreCommands.register(event.getDispatcher()));
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                CoreNetwork.onPlayerJoin(serverPlayer);
            }
        });
        *///?}
    }
}
