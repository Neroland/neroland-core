package za.co.neroland.nerolandcore.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;

/**
 * Forge energy bridge. Owns the shared {@link NeroEnergyStorage} capability;
 * downstream mods attach providers to their machine block-entities via
 * {@code AttachCapabilitiesEvent}. Registered via {@code META-INF/services}.
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
        return be.getCapability(ENERGY, side).orElse(null);
    }
}
