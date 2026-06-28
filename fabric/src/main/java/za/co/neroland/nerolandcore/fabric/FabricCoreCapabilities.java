package za.co.neroland.nerolandcore.fabric;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

import za.co.neroland.nerolandcore.platform.FabricEnergyLookup;
import za.co.neroland.nerolandcore.platform.FabricFluidLookup;
import za.co.neroland.nerolandcore.platform.FabricGasLookup;
import za.co.neroland.nerolandcore.registry.ModBlockEntities;

/**
 * Fabric side of Core's capability seams: exposes the shared storage blocks'
 * energy / fluid / gas / item handlers on the cross-mod {@code nerolandcore:*}
 * {@code BlockApiLookup}s (and the standard Fabric {@code ItemStorage.SIDED}) so
 * any mod's pipes and machines interoperate with Core's Battery / Fluid Tank /
 * Gas Tank / Item Store. Called from {@link NerolandCoreFabric} after common init
 * has registered the block-entity types.
 */
public final class FabricCoreCapabilities {

    private FabricCoreCapabilities() {
    }

    public static void register() {
        // Energy — Battery (buffer) + Creative Battery (endless source).
        FabricEnergyLookup.ENERGY.registerForBlockEntity(
                (be, side) -> be.getEnergy(), ModBlockEntities.BATTERY.get());
        FabricEnergyLookup.ENERGY.registerForBlockEntity(
                (be, side) -> be.getEnergy(), ModBlockEntities.CREATIVE_BATTERY.get());

        // Fluid — Fluid Tank + Creative Fluid Tank.
        FabricFluidLookup.FLUID.registerForBlockEntity(
                (be, side) -> be.getTank(), ModBlockEntities.FLUID_TANK.get());
        FabricFluidLookup.FLUID.registerForBlockEntity(
                (be, side) -> be.getTank(), ModBlockEntities.CREATIVE_FLUID_TANK.get());

        // Gas — Gas Tank + Creative Gas Tank.
        FabricGasLookup.GAS.registerForBlockEntity(
                (be, side) -> be.getTank(), ModBlockEntities.GAS_TANK.get());
        FabricGasLookup.GAS.registerForBlockEntity(
                (be, side) -> be.getTank(), ModBlockEntities.CREATIVE_GAS_TANK.get());

        // Items — Item Store + Creative Item Store, via the standard Fabric Transfer API
        // (both are vanilla Containers, so they also interoperate with hoppers).
        ItemStorage.SIDED.registerForBlockEntity(
                (be, side) -> ContainerStorage.of(be, side), ModBlockEntities.ITEM_STORE.get());
        ItemStorage.SIDED.registerForBlockEntity(
                (be, side) -> ContainerStorage.of(be, side), ModBlockEntities.CREATIVE_ITEM_STORE.get());
    }
}
