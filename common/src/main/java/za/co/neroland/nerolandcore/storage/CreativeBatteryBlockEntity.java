package za.co.neroland.nerolandcore.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;
import za.co.neroland.nerolandcore.registry.ModBlockEntities;

/**
 * Creative Battery — an endless energy SOURCE for testing power grids (pure provider; see insert).
 * Each server tick it pushes energy directly into adjacent receivers — including regular Batteries —
 * so creative testing needs no cables.
 */
public class CreativeBatteryBlockEntity extends BlockEntity {

    private static final NeroEnergyStorage INFINITE = new NeroEnergyStorage() {
        @Override
        public long getAmount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public long getCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public long insert(long maxAmount, boolean simulate) {
            // Pure source: must NOT accept energy back. If it did, a pipe with a default AUTO face would
            // pull power out and immediately push the network's energy straight back in (an infinite
            // sink), so nothing would ever reach the machines downstream.
            return 0;
        }

        @Override
        public long extract(long maxAmount, boolean simulate) {
            return Math.max(0, maxAmount);
        }
    };

    public CreativeBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_BATTERY.get(), pos, state);
    }

    public NeroEnergyStorage getEnergy() {
        return INFINITE;
    }

    /**
     * Server ticker: push into every adjacent receiver at the regular battery's I/O rate. Other
     * creative batteries are naturally immune (their {@code insert} returns 0), so no skip is needed.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativeBatteryBlockEntity battery) {
        AdjacentEnergyPusher.push(level, pos, INFINITE, BatteryBlockEntity.MAX_IO, false);
    }
}
