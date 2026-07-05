package za.co.neroland.nerolandcore.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.energy.EnergyConversions;
import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;

/**
 * Forge energy bridge. Owns the shared {@link NeroEnergyStorage} capability;
 * downstream mods attach providers to their machine block-entities via
 * {@code AttachCapabilitiesEvent}. Registered via {@code META-INF/services}.
 *
 * <p>When a block exposes no Nero energy, the lookup falls back to the standard
 * {@link ForgeCapabilities#ENERGY} capability (Forge Energy) and adapts it to
 * {@link NeroEnergyStorage} with the config-driven NE↔FE conversion, so Universal
 * Pipes connect to — and batteries push into — third-party FE blocks such as
 * Energized Power cables and machines.</p>
 */
public final class ForgeEnergyLookup implements EnergyLookup {

    /** The cross-mod Nero energy capability. Downstream machines attach providers for it. */
    public static final Capability<NeroEnergyStorage> ENERGY =
            CapabilityManager.get(new CapabilityToken<>() { });

    @Nullable
    @Override
    public NeroEnergyStorage find(Level level, BlockPos pos, @Nullable Direction side) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return null;
        }
        NeroEnergyStorage nero = be.getCapability(ENERGY, side).orElse(null);
        if (nero != null) {
            return nero;
        }
        // Standard-FE fallback: adapt any third-party Forge Energy block to the Nero surface.
        IEnergyStorage forgeEnergy = be.getCapability(ForgeCapabilities.ENERGY, side).orElse(null);
        return forgeEnergy == null ? null : new EnergyStorageAdapter(forgeEnergy);
    }

    /** Adapts a standard Forge {@link IEnergyStorage} (FE) to {@link NeroEnergyStorage} (NE). */
    private static final class EnergyStorageAdapter implements NeroEnergyStorage {

        private final IEnergyStorage handler;

        private EnergyStorageAdapter(IEnergyStorage handler) {
            this.handler = handler;
        }

        @Override
        public long getAmount() {
            return EnergyConversions.forgeToNero(this.handler.getEnergyStored());
        }

        @Override
        public long getCapacity() {
            return EnergyConversions.forgeToNero(this.handler.getMaxEnergyStored());
        }

        @Override
        public long insert(long maxAmount, boolean simulate) {
            int fe = clampToInt(EnergyConversions.neroToForge(maxAmount));
            return fe <= 0 ? 0 : EnergyConversions.forgeToNero(this.handler.receiveEnergy(fe, simulate));
        }

        @Override
        public long extract(long maxAmount, boolean simulate) {
            int fe = clampToInt(EnergyConversions.neroToForge(maxAmount));
            return fe <= 0 ? 0 : EnergyConversions.forgeToNero(this.handler.extractEnergy(fe, simulate));
        }

        private static int clampToInt(long value) {
            return (int) Math.max(0, Math.min(Integer.MAX_VALUE, value));
        }
    }
}
