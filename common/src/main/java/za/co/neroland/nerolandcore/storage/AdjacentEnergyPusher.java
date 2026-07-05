package za.co.neroland.nerolandcore.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;
import za.co.neroland.nerolandcore.platform.EnergyLookup;

/**
 * Direct-to-neighbour energy distribution for power storage blocks. Each server tick a source block
 * offers up to {@code rate} NE per face to whatever energy storage the {@link EnergyLookup} seam finds
 * next door — Nero machines, Universal Pipes, and (via the per-loader FE fallback) third-party
 * Forge-Energy blocks such as Energized Power. This is what lets a Battery power an adjacent machine
 * with no cable in between.
 */
final class AdjacentEnergyPusher {

    private AdjacentEnergyPusher() {
    }

    /**
     * Push up to {@code rate} NE into each adjacent receiver.
     *
     * @param skipBatteries when true, never push into another (non-creative) Battery — two half-full
     *                      batteries would otherwise slosh energy back and forth every tick.
     */
    static void push(Level level, BlockPos pos, NeroEnergyStorage source, int rate, boolean skipBatteries) {
        if (level.isClientSide() || rate <= 0) {
            return;
        }
        for (Direction dir : Direction.values()) {
            if (source.extract(1, true) <= 0) {
                return; // drained — nothing left to offer
            }
            BlockPos neighbour = pos.relative(dir);
            if (skipBatteries && level.getBlockEntity(neighbour) instanceof BatteryBlockEntity) {
                continue;
            }
            NeroEnergyStorage target = EnergyLookup.INSTANCE.find(level, neighbour, dir.getOpposite());
            if (target == null || target == source) {
                continue;
            }
            long give = source.extract(rate, true);
            long take = give > 0 ? target.insert(give, true) : 0;
            long moved = Math.min(give, take);
            if (moved > 0) {
                source.extract(moved, false);
                target.insert(moved, false);
            }
        }
    }
}
