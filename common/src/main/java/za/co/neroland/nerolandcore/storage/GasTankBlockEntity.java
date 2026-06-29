package za.co.neroland.nerolandcore.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import za.co.neroland.nerolandcore.gas.GasBuffer;
import za.co.neroland.nerolandcore.gas.NeroGasStorage;
import za.co.neroland.nerolandcore.registry.ModBlockEntities;

/** Gas Tank — a single-gas buffer block entity, exposed via the mod's gas capability/lookup. */
public class GasTankBlockEntity extends BlockEntity {

    public static final int CAPACITY = 16_000; // mB

    private final GasBuffer tank = new GasBuffer(CAPACITY, this::setChanged);

    public GasTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GAS_TANK.get(), pos, state);
    }

    public NeroGasStorage getTank() {
        return this.tank;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Gas", this.tank.getRawGas().toString());
        output.putInt("Amount", this.tank.getRawAmount());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        net.minecraft.resources.Identifier gas = net.minecraft.resources.Identifier.parse(
                input.getStringOr("Gas", za.co.neroland.nerolandcore.gas.NeroGases.EMPTY.toString()));
        this.tank.setRaw(gas, input.getIntOr("Amount", 0));
    }
}
