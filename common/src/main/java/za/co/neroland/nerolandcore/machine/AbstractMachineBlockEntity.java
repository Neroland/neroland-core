package za.co.neroland.nerolandcore.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import za.co.neroland.nerolandcore.energy.EnergyBuffer;
import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;
import za.co.neroland.nerolandcore.upgrade.UpgradeContainer;
import za.co.neroland.nerolandcore.upgrade.UpgradeModifiers;

/**
 * The base machine block-entity downstream mods extend. It bundles the two things
 * almost every Nero machine needs — an {@link EnergyBuffer} and an
 * {@link UpgradeContainer} — plus their persistence, and a server-tick hook.
 * Nerotech's generators and processors, NeroPower's machines, etc. extend this
 * instead of re-implementing the boilerplate, so energy and upgrade modules behave
 * identically across the ecosystem.
 *
 * <p>Subclasses add their own slots/state, override {@link #serverTick}, and expose
 * the energy buffer to their loader's capability via {@link #getEnergy()}. Register
 * a ticker that calls {@link #tick(Level, BlockPos, BlockState, AbstractMachineBlockEntity)}.
 */
public abstract class AbstractMachineBlockEntity extends BlockEntity {

    protected final EnergyBuffer energy;
    protected final UpgradeContainer upgrades;

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
            int energyCapacity, int maxTransfer, int upgradeSlots, UpgradeContainer.Classifier classifier) {
        super(type, pos, state);
        this.energy = new EnergyBuffer(energyCapacity, maxTransfer, maxTransfer, this::setChanged);
        this.upgrades = new UpgradeContainer(upgradeSlots, classifier, this::setChanged);
    }

    /** The machine's energy buffer (expose this to the loader's energy capability). */
    public NeroEnergyStorage getEnergy() {
        return this.energy;
    }

    /** Mutable buffer view for the machine's own generation/consumption. */
    protected EnergyBuffer energyBuffer() {
        return this.energy;
    }

    public UpgradeContainer upgrades() {
        return this.upgrades;
    }

    /** A fresh resolver over the current modules — read multipliers from this each tick. */
    public UpgradeModifiers modifiers() {
        return new UpgradeModifiers(this.upgrades);
    }

    /** Per-tick server logic. Runs only server-side. Override in subclasses. */
    protected void serverTick(Level level, BlockPos pos, BlockState state) {
    }

    /** Ticker entry point: register {@code (l, p, s, be) -> AbstractMachineBlockEntity.tick(l, p, s, be)}. */
    public static <T extends AbstractMachineBlockEntity> void tick(Level level, BlockPos pos, BlockState state, T machine) {
        if (!level.isClientSide()) {
            machine.serverTick(level, pos, state);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Energy", this.energy.getRaw());
        this.upgrades.save(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.energy.setRaw(input.getIntOr("Energy", 0));
        this.upgrades.load(input);
    }
}
