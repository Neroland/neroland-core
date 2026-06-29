package za.co.neroland.nerolandcore.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.menu.TrashCanMenu;
import za.co.neroland.nerolandcore.registry.RegistrationProvider.RegistryEntry;

/**
 * Neroland Core's menu types, registered cross-loader through
 * {@link RegistrationProvider} over the vanilla {@code MENU} registry. Each loader
 * registers the matching client {@code Screen} in its client setup.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class ModMenuTypes {

    public static final RegistrationProvider<MenuType<?>> MENUS =
            RegistrationProvider.get(Registries.MENU, NerolandCoreCommon.MOD_ID);

    public static final RegistryEntry<MenuType<TrashCanMenu>> TRASH_CAN =
            MENUS.register("trash_can",
                    key -> new MenuType<>(TrashCanMenu::new, FeatureFlags.VANILLA_SET));

    private ModMenuTypes() {
    }

    /** Force class-load so the static menu registrations run. */
    public static void init() {
    }
}
