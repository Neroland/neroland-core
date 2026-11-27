package za.co.neroland.nerolandcore.forge;

import java.util.Map;
import java.util.Set;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
//? if <1.21.6 {
/*import net.minecraftforge.common.MinecraftForge;
*///?}
import net.minecraftforge.registries.MissingMappingsEvent;

import za.co.neroland.nerolandcore.registry.LegacyIds;

/**
 * Forge missing-mappings remaps for ids that moved into Core
 * ({@link za.co.neroland.nerolandcore.registry.LegacyIds}). A world saved with {@code nerospace:hematite_ore}
 * lists that id in its registry snapshot; Forge fires {@link MissingMappingsEvent} for it once the id is no
 * longer registered, and this points it at {@code nerolandcore:hematite_ore}. Ids an installed mod still
 * registers never reach the event. The shared registry mixin covers the lookups the snapshot does not.
 */
final class ForgeLegacyAliases {

    private ForgeLegacyAliases() {
    }

    static void register() {
        //? if >=1.21.6 {
        MissingMappingsEvent.BUS.addListener(ForgeLegacyAliases::onMissingMappings);
        //?} else {
        /*MinecraftForge.EVENT_BUS.addListener(ForgeLegacyAliases::onMissingMappings);
        *///?}
    }

    private static void onMissingMappings(MissingMappingsEvent event) {
        remap(event, Registries.BLOCK, BuiltInRegistries.BLOCK, LegacyIds.blocksAndItems());
        remap(event, Registries.ITEM, BuiltInRegistries.ITEM, LegacyIds.blocksAndItems());
        remap(event, Registries.BLOCK_ENTITY_TYPE, BuiltInRegistries.BLOCK_ENTITY_TYPE, LegacyIds.blockEntityTypes());
    }

    private static <T> void remap(MissingMappingsEvent event, ResourceKey<? extends Registry<T>> registryKey,
            Registry<T> registry, Map<String, Set<String>> moved) {
        for (Map.Entry<String, Set<String>> entry : moved.entrySet()) {
            for (MissingMappingsEvent.Mapping<T> mapping : event.getMappings(registryKey, entry.getKey())) {
                String path = mapping.getKey().getPath();
                if (!entry.getValue().contains(path)) {
                    continue;
                }
                Identifier coreId = Identifier.fromNamespaceAndPath(LegacyIds.CORE, path);
                //? if >=1.21.2 {
                T value = registry.getValue(coreId);
                //?} else {
                /*T value = registry.get(coreId);
                *///?}
                if (value != null) {
                    mapping.remap(value);
                }
            }
        }
    }
}
