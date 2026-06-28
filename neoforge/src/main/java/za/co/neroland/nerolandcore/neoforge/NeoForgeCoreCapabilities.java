package za.co.neroland.nerolandcore.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

import za.co.neroland.nerolandcore.platform.NeoForgeEnergyLookup;
import za.co.neroland.nerolandcore.platform.NeoForgeFluidLookup;
import za.co.neroland.nerolandcore.platform.NeoForgeGasLookup;
import za.co.neroland.nerolandcore.registry.ModBlockEntities;

/**
 * NeoForge side of Core's capability seams: exposes the shared storage blocks'
 * energy / fluid / gas handlers on the cross-mod {@code nerolandcore:*}
 * {@link net.neoforged.neoforge.capabilities.BlockCapability}s, and the Item
 * Store's inventory on the standard {@code Capabilities.Item.BLOCK}, so any mod's
 * pipes and machines interoperate with Core's Battery / Fluid Tank / Gas Tank /
 * Item Store. Attached to the mod event bus from {@link NerolandCoreNeoForge}.
 */
public final class NeoForgeCoreCapabilities {

    private NeoForgeCoreCapabilities() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NeoForgeCoreCapabilities::onRegisterCapabilities);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        // Energy — Battery (buffer) + Creative Battery (endless source).
        event.registerBlockEntity(NeoForgeEnergyLookup.ENERGY,
                ModBlockEntities.BATTERY.get(), (be, side) -> be.getEnergy());
        event.registerBlockEntity(NeoForgeEnergyLookup.ENERGY,
                ModBlockEntities.CREATIVE_BATTERY.get(), (be, side) -> be.getEnergy());

        // Fluid — Fluid Tank + Creative Fluid Tank.
        event.registerBlockEntity(NeoForgeFluidLookup.FLUID,
                ModBlockEntities.FLUID_TANK.get(), (be, side) -> be.getTank());
        event.registerBlockEntity(NeoForgeFluidLookup.FLUID,
                ModBlockEntities.CREATIVE_FLUID_TANK.get(), (be, side) -> be.getTank());

        // Gas — Gas Tank + Creative Gas Tank.
        event.registerBlockEntity(NeoForgeGasLookup.GAS,
                ModBlockEntities.GAS_TANK.get(), (be, side) -> be.getTank());
        event.registerBlockEntity(NeoForgeGasLookup.GAS,
                ModBlockEntities.CREATIVE_GAS_TANK.get(), (be, side) -> be.getTank());

        // Items — Item Store (worldly, all faces) + Creative Item Store (endless source).
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ITEM_STORE.get(),
                (be, side) -> side != null
                        ? new WorldlyContainerWrapper(be, side)
                        : VanillaContainerWrapper.of(be));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.CREATIVE_ITEM_STORE.get(),
                (be, side) -> VanillaContainerWrapper.of(be));
    }
}
