package za.co.neroland.nerolandcore.forge;

import java.util.EnumMap;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;
import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;
import za.co.neroland.nerolandcore.gas.NeroGasStorage;
import za.co.neroland.nerolandcore.platform.ForgeEnergyLookup;
import za.co.neroland.nerolandcore.platform.ForgeFluidLookup;
import za.co.neroland.nerolandcore.platform.ForgeGasLookup;
import za.co.neroland.nerolandcore.storage.BatteryBlockEntity;
import za.co.neroland.nerolandcore.storage.CreativeBatteryBlockEntity;
import za.co.neroland.nerolandcore.storage.CreativeFluidTankBlockEntity;
import za.co.neroland.nerolandcore.storage.CreativeGasTankBlockEntity;
import za.co.neroland.nerolandcore.storage.CreativeItemStoreBlockEntity;
import za.co.neroland.nerolandcore.storage.FluidTankBlockEntity;
import za.co.neroland.nerolandcore.storage.GasTankBlockEntity;
import za.co.neroland.nerolandcore.storage.ItemStoreBlockEntity;

/**
 * Forge side of Core's capability seams: attaches the shared storage blocks'
 * energy / fluid / gas handlers to Core's {@code nerolandcore:*} capabilities
 * (owned by the {@code *Lookup} classes), and the Item Store's inventory to the
 * standard Forge {@code ITEM_HANDLER}, so any mod's pipes and machines
 * interoperate with Core's Battery / Fluid Tank / Gas Tank / Item Store. The
 * block-entities already return Core's storage types, so no adapter is needed.
 * Wired from {@link NerolandCoreForge}.
 */
public final class ForgeCoreCapabilities {

    private static final Identifier STORAGE_CAPS =
            Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "storage_caps");

    private ForgeCoreCapabilities() {
    }

    public static void register() {
        AttachCapabilitiesEvent.BlockEntities.BUS.addListener(ForgeCoreCapabilities::onAttachBlockEntity);
    }

    private static void onAttachBlockEntity(AttachCapabilitiesEvent.BlockEntities event) {
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
        }

        Container container = (be instanceof ItemStoreBlockEntity || be instanceof CreativeItemStoreBlockEntity)
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
            this.gas = gas == null ? LazyOptional.empty() : LazyOptional.<NeroGasStorage>of(gas::get);
            this.container = container;
            this.itemUnsided = container == null ? null : LazyOptional.of(() -> itemHandler(container, null));
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            if (cap == ForgeEnergyLookup.ENERGY) {
                return energy.cast();
            }
            if (cap == ForgeFluidLookup.FLUID) {
                return fluid.cast();
            }
            if (cap == ForgeGasLookup.GAS) {
                return gas.cast();
            }
            if (cap == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER && container != null) {
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
            gas.invalidate();
            if (itemUnsided != null) {
                itemUnsided.invalidate();
            }
            itemBySide.values().forEach(LazyOptional::invalidate);
        }
    }
}
