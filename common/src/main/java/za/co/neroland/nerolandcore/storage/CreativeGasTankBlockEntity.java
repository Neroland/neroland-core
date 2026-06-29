package za.co.neroland.nerolandcore.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import za.co.neroland.nerolandcore.gas.NeroGasStorage;
import za.co.neroland.nerolandcore.gas.NeroGases;
import za.co.neroland.nerolandcore.registry.ModBlockEntities;

/**
 * Creative Gas Tank — an endless source of one gas for testing gas logistics. It is generic: it learns
 * its gas from the first fill (latching it as an endless source), voids further input, and supplies that
 * gas without limit. The chosen source persists in NBT; sneak-empty-hand clears it.
 */
public class CreativeGasTankBlockEntity extends BlockEntity {

    private Identifier source = NeroGases.EMPTY;

    private final NeroGasStorage infinite = new NeroGasStorage() {
        @Override
        public Identifier getGas() {
            return source;
        }

        @Override
        public long getAmount() {
            return NeroGases.isEmpty(source) ? 0 : Integer.MAX_VALUE;
        }

        @Override
        public long getCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public long fill(Identifier gas, long amount, boolean simulate) {
            if (!simulate && NeroGases.isEmpty(source) && !NeroGases.isEmpty(gas)) {
                source = gas;
                setChanged();
            }
            return Math.max(0, amount); // voids input, latching the first gas as its endless source
        }

        @Override
        public long drain(long amount, boolean simulate) {
            return NeroGases.isEmpty(source) ? 0 : Math.max(0, amount);
        }
    };

    public CreativeGasTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_GAS_TANK.get(), pos, state);
    }

    public NeroGasStorage getTank() {
        return infinite;
    }

    /** The gas this tank endlessly supplies (or {@link NeroGases#EMPTY} when cleared/unlatched). */
    public Identifier source() {
        return source;
    }

    /** Choose the endless source gas; {@code null} or {@link NeroGases#EMPTY} clears it. */
    public void setSource(Identifier gas) {
        this.source = gas == null ? NeroGases.EMPTY : gas;
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Source", this.source.toString());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.source = Identifier.parse(input.getStringOr("Source", NeroGases.EMPTY.toString()));
    }
}
