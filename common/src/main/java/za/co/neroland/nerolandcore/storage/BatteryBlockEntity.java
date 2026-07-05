package za.co.neroland.nerolandcore.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import za.co.neroland.nerolandcore.energy.EnergyBuffer;
import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;
import za.co.neroland.nerolandcore.registry.ModBlockEntities;

/**
 * Battery — an energy buffer block entity. Exposes {@link NeroEnergyStorage} to the
 * mod's energy capability/lookup on both loaders (see the loader entry points), and each server
 * tick pushes up to {@link #MAX_IO} NE per face directly into adjacent receivers (machines, pipes,
 * third-party FE blocks) so no cable is needed between a battery and its consumer. It never pushes
 * into another Battery (two half-full batteries would slosh energy back and forth forever). No GUI.
 */
public class BatteryBlockEntity extends BlockEntity {

    public static final int CAPACITY = 1_000_000;
    public static final int MAX_IO = 10_000;

    private final EnergyBuffer energy = new EnergyBuffer(CAPACITY, MAX_IO, MAX_IO, this::setChanged);

    public BatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY.get(), pos, state);
    }

    public NeroEnergyStorage getEnergy() {
        return this.energy;
    }

    /** Server ticker: push stored energy directly into adjacent receivers (never other batteries). */
    public static void serverTick(Level level, BlockPos pos, BlockState state, BatteryBlockEntity battery) {
        AdjacentEnergyPusher.push(level, pos, battery.energy, MAX_IO, true);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Energy", this.energy.getRaw());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.energy.setRaw(input.getIntOr("Energy", 0));
    }
}
