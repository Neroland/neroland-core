package za.co.neroland.nerolandcore.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;

/**
 * Query side of the fluid seam: find the {@link NeroFluidStorage} exposed by the
 * block at {@code pos} on {@code side}. Each loader implements it over its own
 * lookup mechanism (NeoForge {@code BlockCapability}, Fabric {@code BlockApiLookup},
 * Forge capability) and owns the shared {@code nerolandcore:fluid} capability
 * object that downstream tanks/machines register their block-entities against — so
 * Nero fluid handling interoperates across mods on one surface. Resolved via
 * {@link Services}. The fluid analogue of {@link EnergyLookup}.
 */
public interface FluidLookup {

    FluidLookup INSTANCE = Services.load(FluidLookup.class);

    @Nullable
    NeroFluidStorage find(Level level, BlockPos pos, @Nullable Direction side);
}
