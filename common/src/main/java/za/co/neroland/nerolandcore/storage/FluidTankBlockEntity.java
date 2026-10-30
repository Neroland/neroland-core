package za.co.neroland.nerolandcore.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import za.co.neroland.nerolandcore.fluid.FluidBuffer;
import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;
import za.co.neroland.nerolandcore.registry.ModBlockEntities;

/** Fluid Tank — a single-fluid buffer block entity, exposed via the mod's fluid capability/lookup. */
public class FluidTankBlockEntity extends BlockEntity {

    public static final int CAPACITY = 16_000; // mB (16 buckets)

    private final FluidBuffer tank = new FluidBuffer(CAPACITY, this::setChanged);

    public FluidTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_TANK.get(), pos, state);
    }

    public NeroFluidStorage getTank() {
        return this.tank;
    }

    //? if >=1.21.6 {
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
    //?} else {
    /*@Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ValueOutput output = ValueOutput.of(tag, registries);
    *///?}
        output.putString("Fluid", BuiltInRegistries.FLUID.getKey(this.tank.getRawFluid()).toString());
        output.putInt("Amount", this.tank.getRawAmount());
    }

    //? if >=1.21.6 {
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
    //?} else {
    /*@Override
    protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ValueInput input = ValueInput.of(tag, registries);
    *///?}
        Fluid fluid = BuiltInRegistries.FLUID.getValue(Identifier.parse(input.getStringOr("Fluid", "minecraft:empty")));
        this.tank.setRaw(fluid, input.getIntOr("Amount", 0));
    }
}
