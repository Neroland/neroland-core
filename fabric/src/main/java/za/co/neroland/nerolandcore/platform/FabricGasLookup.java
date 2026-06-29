package za.co.neroland.nerolandcore.platform;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.gas.NeroGasStorage;

/**
 * Fabric gas bridge. Owns the shared {@code nerolandcore:gas}
 * {@link BlockApiLookup} over {@link NeroGasStorage}; downstream mods register
 * their tank/machine block-entities against {@link #GAS}. Registered via
 * {@code META-INF/services}.
 */
public final class FabricGasLookup implements GasLookup {

    /** The cross-mod Nero gas lookup. Downstream tanks register providers for it. */
    public static final BlockApiLookup<NeroGasStorage, Direction> GAS =
            BlockApiLookup.get(
                    Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "gas"),
                    NeroGasStorage.class, Direction.class);

    @Nullable
    @Override
    public NeroGasStorage find(Level level, BlockPos pos, @Nullable Direction side) {
        return GAS.find(level, pos, side);
    }
}
