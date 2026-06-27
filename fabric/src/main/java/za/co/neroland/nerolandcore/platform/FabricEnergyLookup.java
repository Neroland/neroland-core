package za.co.neroland.nerolandcore.platform;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;

/**
 * Fabric energy bridge. Owns the shared {@code nerolandcore:energy}
 * {@link BlockApiLookup} over {@link NeroEnergyStorage}; downstream mods register
 * their machine block-entities against {@link #ENERGY}. Registered via
 * {@code META-INF/services}.
 */
public final class FabricEnergyLookup implements EnergyLookup {

    /** The cross-mod Nero energy lookup. Downstream machines register providers for it. */
    public static final BlockApiLookup<NeroEnergyStorage, Direction> ENERGY =
            BlockApiLookup.get(
                    Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "energy"),
                    NeroEnergyStorage.class, Direction.class);

    @Nullable
    @Override
    public NeroEnergyStorage find(Level level, BlockPos pos, @Nullable Direction side) {
        return ENERGY.find(level, pos, side);
    }
}
