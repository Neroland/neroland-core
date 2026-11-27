package za.co.neroland.nerolandcore.neoforge;

//? if >=1.20.3 {
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
//? if >=1.21.9 {
import net.neoforged.neoforge.transfer.energy.InfiniteEnergyHandler;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
//?} else {
/*import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
*///?}

import za.co.neroland.nerolandcore.platform.NeoForgeEnergyLookup;
import za.co.neroland.nerolandcore.platform.NeoForgeFluidHandlers;
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

    //? if <1.21.9 {
    /*// Pre-1.21.9 stand-in for the transfer API's InfiniteEnergyHandler: an endless FE source that
    // accepts no insertion and grants every extraction in full.
    private static final IEnergyStorage INFINITE_ENERGY = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            return Math.max(0, toExtract);
        }

        @Override
        public int getEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxEnergyStored() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };
    *///?}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NeoForgeCoreCapabilities::onRegisterCapabilities);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        // Energy — Battery (buffer) + Creative Battery (endless source).
        event.registerBlockEntity(NeoForgeEnergyLookup.ENERGY,
                ModBlockEntities.BATTERY.get(), (be, side) -> be.getEnergy());
        event.registerBlockEntity(NeoForgeEnergyLookup.ENERGY,
                ModBlockEntities.CREATIVE_BATTERY.get(), (be, side) -> be.getEnergy());
        // Also expose the Creative Battery on the STANDARD energy capability (Forge Energy) so
        // third-party cables (e.g. Energized Power) connect to and draw from it directly. The regular
        // Battery reaches third-party blocks the other way round — it pushes into them each tick via
        // the EnergyLookup FE fallback (see BatteryBlockEntity#serverTick).
        //? if >=1.21.9 {
        event.registerBlockEntity(Capabilities.Energy.BLOCK,
                ModBlockEntities.CREATIVE_BATTERY.get(), (be, side) -> InfiniteEnergyHandler.INSTANCE);
        //?} else {
        /*event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.CREATIVE_BATTERY.get(), (be, side) -> INFINITE_ENERGY);
        *///?}

        // Fluid — Fluid Tank + Creative Fluid Tank.
        event.registerBlockEntity(NeoForgeFluidLookup.FLUID,
                ModBlockEntities.FLUID_TANK.get(), (be, side) -> be.getTank());
        event.registerBlockEntity(NeoForgeFluidLookup.FLUID,
                ModBlockEntities.CREATIVE_FLUID_TANK.get(), (be, side) -> be.getTank());
        // Also expose both tanks on the STANDARD fluid capability so third-party fluid pipes and
        // machines fill and drain them directly, the way the Item Store rides Capabilities.Item.BLOCK.
        //? if >=1.21.9 {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.FLUID_TANK.get(),
                (be, side) -> NeoForgeFluidHandlers.asResourceHandler(be.getTank()));
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.CREATIVE_FLUID_TANK.get(),
                (be, side) -> NeoForgeFluidHandlers.asResourceHandler(be.getTank()));
        //?} else {
        /*event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.FLUID_TANK.get(),
                (be, side) -> NeoForgeFluidHandlers.asResourceHandler(be.getTank()));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.CREATIVE_FLUID_TANK.get(),
                (be, side) -> NeoForgeFluidHandlers.asResourceHandler(be.getTank()));
        *///?}

        // Gas — Gas Tank + Creative Gas Tank.
        event.registerBlockEntity(NeoForgeGasLookup.GAS,
                ModBlockEntities.GAS_TANK.get(), (be, side) -> be.getTank());
        event.registerBlockEntity(NeoForgeGasLookup.GAS,
                ModBlockEntities.CREATIVE_GAS_TANK.get(), (be, side) -> be.getTank());

        // Items — Item Store (worldly, all faces) + Creative Item Store (endless source).
        //? if >=1.21.9 {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ITEM_STORE.get(),
                (be, side) -> side != null
                        ? new WorldlyContainerWrapper(be, side)
                        : VanillaContainerWrapper.of(be));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.CREATIVE_ITEM_STORE.get(),
                (be, side) -> VanillaContainerWrapper.of(be));
        //?} else {
        /*event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ITEM_STORE.get(),
                (be, side) -> side != null ? new SidedInvWrapper(be, side) : new InvWrapper(be));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CREATIVE_ITEM_STORE.get(),
                (be, side) -> new InvWrapper(be));
        *///?}

        // Ore Reclaimer — input from the top and sides, output from the bottom.
        //? if >=1.21.9 {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.ORE_RECLAIMER.get(),
                (be, side) -> side != null
                        ? new WorldlyContainerWrapper(be, side)
                        : VanillaContainerWrapper.of(be));
        //?} else {
        /*event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ORE_RECLAIMER.get(),
                (be, side) -> side != null ? new SidedInvWrapper(be, side) : new InvWrapper(be));
        *///?}

        // Trash Can — bottomless void sink for items / fluid / gas.
        //? if >=1.21.9 {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.TRASH_CAN.get(),
                (be, side) -> side != null
                        ? new WorldlyContainerWrapper(be, side)
                        : VanillaContainerWrapper.of(be));
        //?} else {
        /*event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.TRASH_CAN.get(),
                (be, side) -> side != null ? new SidedInvWrapper(be, side) : new InvWrapper(be));
        *///?}
        event.registerBlockEntity(NeoForgeFluidLookup.FLUID,
                ModBlockEntities.TRASH_CAN.get(), (be, side) -> be.getFluid());
        //? if >=1.21.9 {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlockEntities.TRASH_CAN.get(),
                (be, side) -> NeoForgeFluidHandlers.asResourceHandler(be.getFluid()));
        //?} else {
        /*event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.TRASH_CAN.get(),
                (be, side) -> NeoForgeFluidHandlers.asResourceHandler(be.getFluid()));
        *///?}
        event.registerBlockEntity(NeoForgeGasLookup.GAS,
                ModBlockEntities.TRASH_CAN.get(), (be, side) -> be.getGas());
    }
}
//?} else {
/*import java.util.EnumMap;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;

import net.neoforged.neoforge.common.NeoForge;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;
import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;
import za.co.neroland.nerolandcore.gas.NeroGasStorage;
import za.co.neroland.nerolandcore.platform.NeoForgeEnergyLookup;
import za.co.neroland.nerolandcore.platform.NeoForgeFluidHandlers;
import za.co.neroland.nerolandcore.platform.NeoForgeFluidLookup;
import za.co.neroland.nerolandcore.platform.NeoForgeGasLookup;
import za.co.neroland.nerolandcore.storage.BatteryBlockEntity;
import za.co.neroland.nerolandcore.storage.CreativeBatteryBlockEntity;
import za.co.neroland.nerolandcore.storage.CreativeFluidTankBlockEntity;
import za.co.neroland.nerolandcore.storage.CreativeGasTankBlockEntity;
import za.co.neroland.nerolandcore.storage.CreativeItemStoreBlockEntity;
import za.co.neroland.nerolandcore.storage.FluidTankBlockEntity;
import za.co.neroland.nerolandcore.storage.GasTankBlockEntity;
import za.co.neroland.nerolandcore.storage.ItemStoreBlockEntity;
import za.co.neroland.nerolandcore.storage.TrashCanBlockEntity;


public final class NeoForgeCoreCapabilities {

    private static final ResourceLocation STORAGE_CAPS =
            new ResourceLocation(NerolandCoreCommon.MOD_ID, "storage_caps");

    private NeoForgeCoreCapabilities() {
    }

    public static void register(net.neoforged.bus.api.IEventBus modEventBus) {


        NeoForge.EVENT_BUS.addGenericListener(BlockEntity.class, NeoForgeCoreCapabilities::onAttachBlockEntity);

    }


    private static void onAttachBlockEntity(AttachCapabilitiesEvent<BlockEntity> event) {

        StorageCaps caps = createCaps(event.getObject());
        if (caps != null) {
            event.addCapability(STORAGE_CAPS, caps);
            event.addListener(caps::invalidate);
        }
    }

    @Nullable
    private static StorageCaps createCaps(BlockEntity be) {
        Supplier<NeroEnergyStorage> energy = null;
        Supplier<NeroFluidStorage> fluid = null;
        Supplier<NeroGasStorage> gas = null;

        if (be instanceof BatteryBlockEntity battery) {
            energy = battery::getEnergy;
        } else if (be instanceof CreativeBatteryBlockEntity battery) {
            energy = battery::getEnergy;
        } else if (be instanceof FluidTankBlockEntity tank) {
            fluid = tank::getTank;
        } else if (be instanceof CreativeFluidTankBlockEntity tank) {
            fluid = tank::getTank;
        } else if (be instanceof GasTankBlockEntity tank) {
            gas = tank::getTank;
        } else if (be instanceof CreativeGasTankBlockEntity tank) {
            gas = tank::getTank;
        } else if (be instanceof TrashCanBlockEntity trash) {
            fluid = trash::getFluid;
            gas = trash::getGas;
        }

        Container container = (be instanceof ItemStoreBlockEntity || be instanceof CreativeItemStoreBlockEntity
                || be instanceof TrashCanBlockEntity
                || be instanceof za.co.neroland.nerolandcore.reclaim.OreReclaimerBlockEntity)
                ? (Container) be : null;
        if (energy == null && fluid == null && gas == null && container == null) {
            return null;
        }
        return new StorageCaps(energy, fluid, gas, container);
    }

    private static IItemHandler itemHandler(Container container, @Nullable Direction side) {
        if (container instanceof WorldlyContainer worldly && side != null) {
            return new SidedInvWrapper(worldly, side);
        }
        return new InvWrapper(container);
    }

    private static final class StorageCaps implements ICapabilityProvider {

        private final LazyOptional<NeroEnergyStorage> energy;
        private final LazyOptional<NeroFluidStorage> fluid;
        private final LazyOptional<IFluidHandler> fluidStandard;
        private final LazyOptional<NeroGasStorage> gas;
        @Nullable
        private final Container container;
        @Nullable
        private final LazyOptional<IItemHandler> itemUnsided;
        private final EnumMap<Direction, LazyOptional<IItemHandler>> itemBySide = new EnumMap<>(Direction.class);

        StorageCaps(@Nullable Supplier<NeroEnergyStorage> energy, @Nullable Supplier<NeroFluidStorage> fluid,
                @Nullable Supplier<NeroGasStorage> gas, @Nullable Container container) {
            this.energy = energy == null ? LazyOptional.empty() : LazyOptional.<NeroEnergyStorage>of(energy::get);
            this.fluid = fluid == null ? LazyOptional.empty() : LazyOptional.<NeroFluidStorage>of(fluid::get);
            Supplier<NeroFluidStorage> fluidSupplier = fluid;
            this.fluidStandard = fluid == null
                    ? LazyOptional.empty()
                    : LazyOptional.<IFluidHandler>of(() -> NeoForgeFluidHandlers.asResourceHandler(fluidSupplier.get()));
            this.gas = gas == null ? LazyOptional.empty() : LazyOptional.<NeroGasStorage>of(gas::get);
            this.container = container;
            this.itemUnsided = container == null ? null : LazyOptional.of(() -> itemHandler(container, null));
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            if (cap == NeoForgeEnergyLookup.ENERGY) {
                return energy.cast();
            }
            if (cap == NeoForgeFluidLookup.FLUID) {
                return fluid.cast();
            }
            if (cap == NeoForgeGasLookup.GAS) {
                return gas.cast();
            }
            if (cap == net.neoforged.neoforge.common.capabilities.Capabilities.FLUID_HANDLER) {
                // Standard Forge fluid surface, so third-party pipes fill/drain Core's tanks directly.
                return fluidStandard.cast();
            }
            if (cap == net.neoforged.neoforge.common.capabilities.Capabilities.ITEM_HANDLER && container != null) {
                return item(side).cast();
            }
            return LazyOptional.empty();
        }

        private LazyOptional<IItemHandler> item(@Nullable Direction side) {
            if (side == null) {
                return itemUnsided == null ? LazyOptional.empty() : itemUnsided;
            }
            return itemBySide.computeIfAbsent(side, d -> LazyOptional.of(() -> itemHandler(container, d)));
        }

        void invalidate() {
            energy.invalidate();
            fluid.invalidate();
            fluidStandard.invalidate();
            gas.invalidate();
            if (itemUnsided != null) {
                itemUnsided.invalidate();
            }
            itemBySide.values().forEach(LazyOptional::invalidate);
        }
    }
}
*///?}
