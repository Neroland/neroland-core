package za.co.neroland.nerolandcore.registry;

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
import za.co.neroland.nerolandcore.storage.TrashCanBlockEntity;

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
                    key -> BlockEntityTypeCompat.create(BatteryBlockEntity::new, ModBlocks.BATTERY.get()));

    public static final RegistryEntry<BlockEntityType<FluidTankBlockEntity>> FLUID_TANK =
            BLOCK_ENTITIES.register("fluid_tank",
                    key -> BlockEntityTypeCompat.create(FluidTankBlockEntity::new, ModBlocks.FLUID_TANK.get()));

    public static final RegistryEntry<BlockEntityType<GasTankBlockEntity>> GAS_TANK =
            BLOCK_ENTITIES.register("gas_tank",
                    key -> BlockEntityTypeCompat.create(GasTankBlockEntity::new, ModBlocks.GAS_TANK.get()));

    public static final RegistryEntry<BlockEntityType<ItemStoreBlockEntity>> ITEM_STORE =
            BLOCK_ENTITIES.register("item_store",
                    key -> BlockEntityTypeCompat.create(ItemStoreBlockEntity::new, ModBlocks.ITEM_STORE.get()));

    public static final RegistryEntry<BlockEntityType<CreativeBatteryBlockEntity>> CREATIVE_BATTERY =
            BLOCK_ENTITIES.register("creative_battery",
                    key -> BlockEntityTypeCompat.create(CreativeBatteryBlockEntity::new, ModBlocks.CREATIVE_BATTERY.get()));

    public static final RegistryEntry<BlockEntityType<CreativeFluidTankBlockEntity>> CREATIVE_FLUID_TANK =
            BLOCK_ENTITIES.register("creative_fluid_tank",
                    key -> BlockEntityTypeCompat.create(CreativeFluidTankBlockEntity::new, ModBlocks.CREATIVE_FLUID_TANK.get()));

    public static final RegistryEntry<BlockEntityType<CreativeGasTankBlockEntity>> CREATIVE_GAS_TANK =
            BLOCK_ENTITIES.register("creative_gas_tank",
                    key -> BlockEntityTypeCompat.create(CreativeGasTankBlockEntity::new, ModBlocks.CREATIVE_GAS_TANK.get()));

    public static final RegistryEntry<BlockEntityType<CreativeItemStoreBlockEntity>> CREATIVE_ITEM_STORE =
            BLOCK_ENTITIES.register("creative_item_store",
                    key -> BlockEntityTypeCompat.create(CreativeItemStoreBlockEntity::new, ModBlocks.CREATIVE_ITEM_STORE.get()));

    public static final RegistryEntry<BlockEntityType<TrashCanBlockEntity>> TRASH_CAN =
            BLOCK_ENTITIES.register("trash_can",
                    key -> BlockEntityTypeCompat.create(TrashCanBlockEntity::new, ModBlocks.TRASH_CAN.get()));

    private ModBlockEntities() {
    }

    /** Force class-load so the static block-entity registrations run. */
    public static void init() {
    }
}
