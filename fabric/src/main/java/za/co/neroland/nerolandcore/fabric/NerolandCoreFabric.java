package za.co.neroland.nerolandcore.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.command.CoreCommands;
import za.co.neroland.nerolandcore.network.CoreNetwork;
import za.co.neroland.nerolandcore.recipe.RecipeViewerSync;
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

        syncVanillaRecipes();
    }

    /**
     * Sends the vanilla recipe serializers' recipes to clients so JEI lists every Nero crafting/smelting
     * recipe even when the server does not run JEI (see {@link RecipeViewerSync}). Fabric keeps a set,
     * so JEI registering the same serializers again is harmless.
     */
    private static void syncVanillaRecipes() {
        for (RecipeSerializer<?> serializer : BuiltInRegistries.RECIPE_SERIALIZER) {
            Identifier id = BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer);
            if (id == null || !RecipeViewerSync.isVanillaSerializer(id)) {
                continue;
            }
            try {
                RecipeSynchronization.synchronizeRecipeSerializer(serializer);
            } catch (RuntimeException e) {
                NerolandCoreCommon.LOGGER.warn("[Neroland Core] Could not sync recipe serializer {}", id, e);
            }
        }
    }
}
