package za.co.neroland.nerolandcore.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.gas.NeroGasStorage;

/**
 * Query side of the gas seam: find the {@link NeroGasStorage} exposed by the block
 * at {@code pos} on {@code side}. Each loader implements it over its own lookup
 * mechanism (NeoForge {@code BlockCapability}, Fabric {@code BlockApiLookup}, Forge
 * capability) and owns the shared {@code nerolandcore:gas} capability object that
 * downstream tanks/machines register their block-entities against — so Nero gas
 * handling interoperates across mods on one surface. Resolved via {@link Services}.
 * The gas analogue of {@link EnergyLookup}.
 */
public interface GasLookup {

    GasLookup INSTANCE = Services.load(GasLookup.class);

    @Nullable
    NeroGasStorage find(Level level, BlockPos pos, @Nullable Direction side);
}
