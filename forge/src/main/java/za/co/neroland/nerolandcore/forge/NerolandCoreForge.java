package za.co.neroland.nerolandcore.forge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
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
import za.co.neroland.nerolandcore.data.RetentionSchedule;
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

    //? if >=1.20.5 {
    public NerolandCoreForge(FMLJavaModLoadingContext context) {
    //?} else {
    /*public NerolandCoreForge() {
        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
    *///?}
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
        // Remap ids that moved into Core (old Nerospace / NeroEvents / NeroPower ores) in old worlds.
        ForgeLegacyAliases.register();
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
        // Scheduled data-retention housekeeping (server start, then daily).
        registerRetentionTick();
        // Nero Mods Demo (26.3+): build a new demo world's hub and exhibits once the server has started.
        //? if >=26.3 {
        /*net.minecraftforge.event.server.ServerStartedEvent.BUS.addListener(event ->
                za.co.neroland.nerolandcore.demo.DemoWorlds.onServerStarted(event.getServer()));
        *///?}
    }

    /** End-of-server-tick hook for {@link RetentionSchedule}; the tick event's shape differs by Forge line. */
    private static void registerRetentionTick() {
        //? if >=1.21.9 {
        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> RetentionSchedule.onServerTick(event.server()));
        //?} else if >=1.21.6 {
        /*TickEvent.ServerTickEvent.Post.BUS.addListener(event -> RetentionSchedule.onServerTick(event.getServer()));
        *///?} else if >=1.20.5 {
        /*MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent.Post event) ->
                RetentionSchedule.onServerTick(event.getServer()));
        *///?} else {
        /*MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                RetentionSchedule.onServerTick(event.getServer());
            }
        });
        *///?}
    }
}
