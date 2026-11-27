package za.co.neroland.nerolandcore.neoforge;

//? if >=1.20.4 {
import java.util.Map;
import java.util.Set;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

import za.co.neroland.nerolandcore.platform.Services;
import za.co.neroland.nerolandcore.registry.LegacyIds;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
*///?}

/**
 * NeoForge registry aliases for ids that moved into Core ({@link za.co.neroland.nerolandcore.registry.LegacyIds}).
 * An alias is only added when the old home mod is not installed: while it is, the old id might still be
 * registered by an older copy of it, and the shared registry mixin redirects the old id only when it is
 * free. NeoForge 1.20.2 and 1.20.3 predate the registry rework and rely on the mixin alone.
 */
final class NeoForgeLegacyAliases {

    private NeoForgeLegacyAliases() {
    }

    static void register(IEventBus modEventBus) {
        //? if >=1.20.4 {
        modEventBus.addListener(NeoForgeLegacyAliases::onRegister);
        //?}
    }

    //? if >=1.20.4 {
    private static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK) || event.getRegistryKey().equals(Registries.ITEM)) {
            alias(event.getRegistry(), LegacyIds.blocksAndItems());
        } else if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
            alias(event.getRegistry(), LegacyIds.blockEntityTypes());
        }
    }

    private static void alias(Registry<?> registry, Map<String, Set<String>> moved) {
        for (Map.Entry<String, Set<String>> entry : moved.entrySet()) {
            if (Services.PLATFORM.isModLoaded(entry.getKey())) {
                continue;
            }
            for (String path : entry.getValue()) {
                registry.addAlias(Identifier.fromNamespaceAndPath(entry.getKey(), path),
                        Identifier.fromNamespaceAndPath(LegacyIds.CORE, path));
            }
        }
    }
    //?}
}
