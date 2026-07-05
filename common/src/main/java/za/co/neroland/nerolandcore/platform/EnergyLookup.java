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
 *
 * <p>Where the loader has a standard Forge-Energy-style capability (NeoForge
 * {@code Capabilities.Energy.BLOCK}, Forge {@code ForgeCapabilities.ENERGY}), the
 * implementation falls back to it when no Nero energy is found, adapting FE↔NE via
 * {@code EnergyConversions} — so cables, batteries and side-config auto-eject reach
 * third-party FE blocks (e.g. Energized Power) too. The Fabric implementation stays
 * Nero-only until the Team Reborn Energy API ports to 26.x.</p>
 */
public interface EnergyLookup {

    EnergyLookup INSTANCE = Services.load(EnergyLookup.class);

    @Nullable
    NeroEnergyStorage find(Level level, BlockPos pos, @Nullable Direction side);
}
