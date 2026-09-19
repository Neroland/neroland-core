package za.co.neroland.nerolandcore.platform;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;

/**
 * Fabric fluid bridge. Owns the shared {@code nerolandcore:fluid}
 * {@link BlockApiLookup} over {@link NeroFluidStorage}; downstream mods register
 * their tank/machine block-entities against {@link #FLUID}. Registered via
 * {@code META-INF/services}.
 *
 * <p>When a block exposes no Nero fluid, the lookup falls back to the standard Fabric
 * {@code FluidStorage.SIDED} lookup and adapts it to {@link NeroFluidStorage}, converting
 * droplets to millibuckets. Nero tanks and machines therefore treat third-party fluid pipes
 * and tanks as first-class neighbours. The other direction — a Nero tank seen BY a
 * third-party pipe — is {@link FabricFluidHandlers#asFluidStorage}, which downstream mods
 * register on {@code FluidStorage.SIDED}.</p>
 */
public final class FabricFluidLookup implements FluidLookup {

    /** The cross-mod Nero fluid lookup. Downstream tanks register providers for it. */
    public static final BlockApiLookup<NeroFluidStorage, Direction> FLUID =
            BlockApiLookup.get(
                    Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "fluid"),
                    NeroFluidStorage.class, Direction.class);

    @Nullable
    @Override
    public NeroFluidStorage find(Level level, BlockPos pos, @Nullable Direction side) {
        NeroFluidStorage nero = FLUID.find(level, pos, side);
        if (nero != null) {
            return nero;
        }
        // Standard-fluid fallback: adapt any third-party fluid storage to the Nero surface.
        Storage<FluidVariant> standard = FluidStorage.SIDED.find(level, pos, side);
        return standard == null ? null : FabricFluidHandlers.asNeroFluid(standard);
    }
}
