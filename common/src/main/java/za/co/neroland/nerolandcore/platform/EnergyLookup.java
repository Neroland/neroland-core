package za.co.neroland.nerolandcore.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;

/**
 * Query side of the energy seam: find the {@link NeroEnergyStorage} exposed by the
 * block at {@code pos} on {@code side}. Each loader implements it over its own
 * lookup mechanism (NeoForge {@code BlockCapability}, Fabric {@code BlockApiLookup},
 * Forge capability) and owns the shared {@code nerolandcore:energy} capability
 * object that downstream machines register their block-entities against — so Nero
 * machines across mods interoperate on one energy surface. Resolved via
 * {@link Services}.
 */
public interface EnergyLookup {

    EnergyLookup INSTANCE = Services.load(EnergyLookup.class);

    @Nullable
    NeroEnergyStorage find(Level level, BlockPos pos, @Nullable Direction side);
}
