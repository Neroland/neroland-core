package za.co.neroland.nerolandcore.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;

/**
 * NeoForge fluid bridge. Owns the shared {@code nerolandcore:fluid}
 * {@link BlockCapability} over {@link NeroFluidStorage}; downstream mods register
 * their tank/machine block-entities against {@link #FLUID} during
 * {@code RegisterCapabilitiesEvent}. Registered via {@code META-INF/services}.
 *
 * <p>When a block exposes no Nero fluid, the lookup falls back to the standard
 * NeoForge fluid capability ({@code Capabilities.Fluid.BLOCK}) and adapts it to
 * {@link NeroFluidStorage} — both sides count in millibuckets, so nothing is
 * converted. This is what lets Nero tanks and machines treat third-party fluid
 * pipes and tanks as first-class neighbours, mirroring the FE fallback in
 * {@link NeoForgeEnergyLookup}. The other direction — a Nero tank seen BY a
 * third-party pipe — is {@link NeoForgeFluidHandlers#asResourceHandler}, which
 * downstream mods register on {@code Capabilities.Fluid.BLOCK}.</p>
 */
public final class NeoForgeFluidLookup implements FluidLookup {

    /** The cross-mod Nero fluid capability. Downstream tanks register providers for it. */
    public static final BlockCapability<NeroFluidStorage, Direction> FLUID =
            BlockCapability.createSided(
                    Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "fluid"),
                    NeroFluidStorage.class);

    @Nullable
    @Override
    public NeroFluidStorage find(Level level, BlockPos pos, @Nullable Direction side) {
        NeroFluidStorage nero = level.getCapability(FLUID, pos, side);
        if (nero != null) {
            return nero;
        }
        // Standard-fluid fallback: adapt any third-party fluid handler to the Nero surface.
        ResourceHandler<FluidResource> standard =
                Capabilities.Fluid.BLOCK.getCapability(level, pos, null, null, side);
        return standard == null ? null : NeoForgeFluidHandlers.asNeroFluid(standard);
    }
}
