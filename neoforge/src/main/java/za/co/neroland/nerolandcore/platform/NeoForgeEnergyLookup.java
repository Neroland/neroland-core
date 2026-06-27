package za.co.neroland.nerolandcore.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;

/**
 * NeoForge energy bridge. Owns the shared {@code nerolandcore:energy}
 * {@link BlockCapability} over {@link NeroEnergyStorage}; downstream mods register
 * their machine block-entities against {@link #ENERGY} during
 * {@code RegisterCapabilitiesEvent}. Registered via {@code META-INF/services}.
 */
public final class NeoForgeEnergyLookup implements EnergyLookup {

    /** The cross-mod Nero energy capability. Downstream machines register providers for it. */
    public static final BlockCapability<NeroEnergyStorage, Direction> ENERGY =
            BlockCapability.createSided(
                    Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "energy"),
                    NeroEnergyStorage.class);

    @Nullable
    @Override
    public NeroEnergyStorage find(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(ENERGY, pos, side);
    }
}
