package za.co.neroland.nerolandcore.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;

/**
 * NeoForge fluid bridge. Owns the shared {@code nerolandcore:fluid}
 * {@link BlockCapability} over {@link NeroFluidStorage}; downstream mods register
 * their tank/machine block-entities against {@link #FLUID} during
 * {@code RegisterCapabilitiesEvent}. Registered via {@code META-INF/services}.
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
        return level.getCapability(FLUID, pos, side);
    }
}
