package za.co.neroland.nerolandcore.platform;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
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
        return FLUID.find(level, pos, side);
    }
}
