package za.co.neroland.nerolandcore.registry;

import java.util.Set;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.registry.RegistrationProvider.RegistryEntry;
import za.co.neroland.nerolandcore.storage.BatteryBlockEntity;
import za.co.neroland.nerolandcore.storage.CreativeBatteryBlockEntity;
import za.co.neroland.nerolandcore.storage.CreativeFluidTankBlockEntity;
import za.co.neroland.nerolandcore.storage.CreativeGasTankBlockEntity;
import za.co.neroland.nerolandcore.storage.CreativeItemStoreBlockEntity;
import za.co.neroland.nerolandcore.storage.FluidTankBlockEntity;
import za.co.neroland.nerolandcore.storage.GasTankBlockEntity;
import za.co.neroland.nerolandcore.storage.ItemStoreBlockEntity;

/**
 * Neroland Core's block-entity types, registered cross-loader through
 * {@link RegistrationProvider} over the vanilla {@code BLOCK_ENTITY_TYPE}
 * registry. These back the shared storage blocks (see {@link ModBlocks}); each
 * loader exposes their energy/fluid/gas/item handlers to other mods through its
 * own capability seam (see each loader's {@code CoreCapabilities}). Registration
 * itself is loader-agnostic.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class ModBlockEntities {

    public static final RegistrationProvider<BlockEntityType<?>> BLOCK_ENTITIES =
            RegistrationProvider.get(Registries.BLOCK_ENTITY_TYPE, NerolandCoreCommon.MOD_ID);

    public static final RegistryEntry<BlockEntityType<BatteryBlockEntity>> BATTERY =
            BLOCK_ENTITIES.register("battery",
                    key -> new BlockEntityType<>(BatteryBlockEntity::new, Set.of(ModBlocks.BATTERY.get())));

    public static final RegistryEntry<BlockEntityType<FluidTankBlockEntity>> FLUID_TANK =
            BLOCK_ENTITIES.register("fluid_tank",
                    key -> new BlockEntityType<>(FluidTankBlockEntity::new, Set.of(ModBlocks.FLUID_TANK.get())));

    public static final RegistryEntry<BlockEntityType<GasTankBlockEntity>> GAS_TANK =
            BLOCK_ENTITIES.register("gas_tank",
                    key -> new BlockEntityType<>(GasTankBlockEntity::new, Set.of(ModBlocks.GAS_TANK.get())));

    public static final RegistryEntry<BlockEntityType<ItemStoreBlockEntity>> ITEM_STORE =
            BLOCK_ENTITIES.register("item_store",
                    key -> new BlockEntityType<>(ItemStoreBlockEntity::new, Set.of(ModBlocks.ITEM_STORE.get())));

    public static final RegistryEntry<BlockEntityType<CreativeBatteryBlockEntity>> CREATIVE_BATTERY =
            BLOCK_ENTITIES.register("creative_battery",
                    key -> new BlockEntityType<>(CreativeBatteryBlockEntity::new, Set.of(ModBlocks.CREATIVE_BATTERY.get())));

    public static final RegistryEntry<BlockEntityType<CreativeFluidTankBlockEntity>> CREATIVE_FLUID_TANK =
            BLOCK_ENTITIES.register("creative_fluid_tank",
                    key -> new BlockEntityType<>(CreativeFluidTankBlockEntity::new, Set.of(ModBlocks.CREATIVE_FLUID_TANK.get())));

    public static final RegistryEntry<BlockEntityType<CreativeGasTankBlockEntity>> CREATIVE_GAS_TANK =
            BLOCK_ENTITIES.register("creative_gas_tank",
                    key -> new BlockEntityType<>(CreativeGasTankBlockEntity::new, Set.of(ModBlocks.CREATIVE_GAS_TANK.get())));

    public static final RegistryEntry<BlockEntityType<CreativeItemStoreBlockEntity>> CREATIVE_ITEM_STORE =
            BLOCK_ENTITIES.register("creative_item_store",
                    key -> new BlockEntityType<>(CreativeItemStoreBlockEntity::new, Set.of(ModBlocks.CREATIVE_ITEM_STORE.get())));

    private ModBlockEntities() {
    }

    /** Force class-load so the static block-entity registrations run. */
    public static void init() {
    }
}
