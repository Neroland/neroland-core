package za.co.neroland.nerolandcore.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;

/**
 * Forge fluid bridge. Owns the shared {@link NeroFluidStorage} capability;
 * downstream mods attach providers to their tank/machine block-entities via
 * {@code AttachCapabilitiesEvent}. Registered via {@code META-INF/services}.
 *
 * <p>When a block exposes no Nero fluid, the lookup falls back to Forge's standard
 * {@code FLUID_HANDLER} capability and adapts it to {@link NeroFluidStorage} — both
 * count in millibuckets, so nothing is converted. Nero tanks and machines therefore
 * treat third-party fluid pipes and tanks as first-class neighbours, mirroring the FE
 * fallback in {@link ForgeEnergyLookup}. The other direction — a Nero tank seen BY a
 * third-party pipe — is {@link ForgeFluidHandlers#asFluidHandler}, which downstream
 * mods attach to {@code FLUID_HANDLER}.</p>
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
        NeroFluidStorage nero = be.getCapability(FLUID, side).orElse(null);
        if (nero != null) {
            return nero;
        }
        // Standard-fluid fallback: adapt any third-party fluid handler to the Nero surface.
        return be.getCapability(ForgeCapabilities.FLUID_HANDLER, side)
                .map(ForgeFluidHandlers::asNeroFluid)
                .orElse(null);
    }
}
