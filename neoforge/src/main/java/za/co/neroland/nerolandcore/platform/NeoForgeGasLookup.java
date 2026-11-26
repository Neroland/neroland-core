package za.co.neroland.nerolandcore.platform;

//? if >=1.20.3 {
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.gas.NeroGasStorage;

/**
 * NeoForge gas bridge. Owns the shared {@code nerolandcore:gas}
 * {@link BlockCapability} over {@link NeroGasStorage}; downstream mods register
 * their tank/machine block-entities against {@link #GAS} during
 * {@code RegisterCapabilitiesEvent}. Registered via {@code META-INF/services}.
 */
public final class NeoForgeGasLookup implements GasLookup {

    /** The cross-mod Nero gas capability. Downstream tanks register providers for it. */
    public static final BlockCapability<NeroGasStorage, Direction> GAS =
            BlockCapability.createSided(
                    Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "gas"),
                    NeroGasStorage.class);

    @Nullable
    @Override
    public NeroGasStorage find(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GAS, pos, side);
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

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.gas.NeroGasStorage;


public final class NeoForgeGasLookup implements GasLookup {


    public static final Capability<NeroGasStorage> GAS =
            CapabilityManager.get(new CapabilityToken<>() { });

    @Nullable
    @Override
    public NeroGasStorage find(Level level, BlockPos pos, @Nullable Direction side) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return null;
        }
        return be.getCapability(GAS, side).orElse(null);
    }
}
*///?}
