package za.co.neroland.nerolandcore.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;

/**
 * Forge fluid bridge. Owns the shared {@link NeroFluidStorage} capability;
 * downstream mods attach providers to their tank/machine block-entities via
 * {@code AttachCapabilitiesEvent}. Registered via {@code META-INF/services}.
 */
public final class ForgeFluidLookup implements FluidLookup {

    /** The cross-mod Nero fluid capability. Downstream tanks attach providers for it. */
    public static final Capability<NeroFluidStorage> FLUID =
            CapabilityManager.get(new CapabilityToken<>() { });

    @Nullable
    @Override
    public NeroFluidStorage find(Level level, BlockPos pos, @Nullable Direction side) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return null;
        }
        return be.getCapability(FLUID, side).orElse(null);
    }
}
