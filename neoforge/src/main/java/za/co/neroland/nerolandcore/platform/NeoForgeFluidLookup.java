package za.co.neroland.nerolandcore.platform;

//? if >=1.20.3 {
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
//? if >=1.21.9 {
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
//?} else {
/*import net.neoforged.neoforge.fluids.capability.IFluidHandler;
*///?}

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
        //? if >=1.21.9 {
        ResourceHandler<FluidResource> standard =
                Capabilities.Fluid.BLOCK.getCapability(level, pos, null, null, side);
        //?} else {
        /*IFluidHandler standard = Capabilities.FluidHandler.BLOCK.getCapability(level, pos, null, null, side);
        *///?}
        return standard == null ? null : NeoForgeFluidHandlers.asNeroFluid(standard);
    }
}
//?} else {
/*import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.capabilities.CapabilityToken;
import net.neoforged.neoforge.common.capabilities.Capabilities;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;


public final class NeoForgeFluidLookup implements FluidLookup {


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
        return be.getCapability(Capabilities.FLUID_HANDLER, side)
                .map(NeoForgeFluidHandlers::asNeroFluid)
                .orElse(null);
    }
}
*///?}
