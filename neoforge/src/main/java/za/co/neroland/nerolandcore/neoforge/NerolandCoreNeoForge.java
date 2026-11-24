package za.co.neroland.nerolandcore.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
//? if >=1.21.4 {
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
//?}
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
//? if >=1.20.5 {
import net.neoforged.neoforge.event.tick.ServerTickEvent;
//?} else {
/*import net.neoforged.neoforge.event.TickEvent;
*///?}

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.command.CoreCommands;
import za.co.neroland.nerolandcore.data.RetentionSchedule;
import za.co.neroland.nerolandcore.entity.NeoForgeEntityRegistrationPlumbing;
import za.co.neroland.nerolandcore.network.CoreNetwork;
//? if >=1.21.4 {
import za.co.neroland.nerolandcore.recipe.RecipeViewerSync;
//?}
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
        // Flush listeners for the shared entity seam (default attributes + spawn placements). Mod-bus
        // events reach every mod, so this one attachment also serves downstream mods' registrations.
        NeoForgeEntityRegistrationPlumbing.attachBus(modEventBus);
        NeoForgeNetwork.register(modEventBus);
        // Expose the shared storage blocks' energy/fluid/gas/item handlers cross-mod.
        NeoForgeCoreCapabilities.register(modEventBus);
        //? if >=1.21.9 {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
        //?} else {
        /*if (FMLEnvironment.dist == Dist.CLIENT) {
        *///?}
            NeoForgeClientSetup.init(modEventBus);
        }

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                CoreCommands.register(event.getDispatcher()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                CoreNetwork.onPlayerJoin(serverPlayer);
            }
        });
        // Scheduled data-retention housekeeping (server start, then daily).
        //? if >=1.20.5 {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) ->
                RetentionSchedule.onServerTick(event.getServer()));
        //?} else {
        /*NeoForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                RetentionSchedule.onServerTick(event.getServer());
            }
        });
        *///?}
        // Nero Mods Demo (26.3+): build a new demo world's hub and exhibits once the server has started.
        //? if >=26.3 {
        /*NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStartedEvent event) ->
                za.co.neroland.nerolandcore.demo.DemoWorlds.onServerStarted(event.getServer()));
        *///?}
        // Send the vanilla recipe types to clients so JEI lists every Nero crafting/smelting recipe even
        // when the server does not run JEI. NeoForge 1.21.3 and earlier lack the sendRecipes hook.
        //? if >=1.21.4 {
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) ->
                event.sendRecipes(RecipeViewerSync.vanillaTypes()));
        //?}
    }
}
