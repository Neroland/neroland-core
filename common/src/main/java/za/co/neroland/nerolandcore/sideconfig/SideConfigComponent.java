package za.co.neroland.nerolandcore.sideconfig;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;
import za.co.neroland.nerolandcore.fluid.NeroFluidStorage;
import za.co.neroland.nerolandcore.gas.NeroGasStorage;
import za.co.neroland.nerolandcore.platform.EnergyLookup;
import za.co.neroland.nerolandcore.platform.FluidLookup;
import za.co.neroland.nerolandcore.platform.GasLookup;
import za.co.neroland.nerolandcore.platform.Services;

/**
 * The attachable side-configuration component a block-entity holds. Bundles a
 * {@link SideConfig} with the wiring every machine needs: capability gating per
 * face, the vanilla {@code WorldlyContainer} hooks for item gating, optional
 * auto-eject / auto-input transfer, persistence, and the change notification that
 * makes adjacent pipes reconnect.
 *
 * <p>Composable, not inheritance-only: a BE either lets Core's
 * {@code AbstractMachineBlockEntity} own one, or constructs its own and exposes it
 * via {@link SideConfigured}. Keep loader-native types out — this is common code.
 *
 * <p>The data here is world/block state keyed by position; it holds no player
 * identity and is never logged or sent to telemetry (POPIA/GDPR).
 */
public final class SideConfigComponent {

    private static final Function<BlockState, Direction> DEFAULT_FACING = state ->
            state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                    ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                    : Direction.NORTH;

    private final SideConfig config;
    private final BlockEntity owner;
    private final Function<BlockState, Direction> facingFn;

    @Nullable
    private Supplier<NeroEnergyStorage> energy;
    @Nullable
    private Supplier<NeroFluidStorage> fluid;
    @Nullable
    private Supplier<NeroGasStorage> gas;
    @Nullable
    private Supplier<Container> container;

    public SideConfigComponent(SideConfig config, BlockEntity owner) {
        this(config, owner, DEFAULT_FACING);
    }

    public SideConfigComponent(SideConfig config, BlockEntity owner, Function<BlockState, Direction> facingFn) {
        this.config = config;
        this.owner = owner;
        this.facingFn = facingFn;
    }

    // --- wiring -------------------------------------------------------------

    public SideConfigComponent withEnergy(Supplier<NeroEnergyStorage> supplier) {
        this.energy = supplier;
        return this;
    }

    public SideConfigComponent withFluid(Supplier<NeroFluidStorage> supplier) {
        this.fluid = supplier;
        return this;
    }

    public SideConfigComponent withGas(Supplier<NeroGasStorage> supplier) {
        this.gas = supplier;
        return this;
    }

    public SideConfigComponent withItems(Supplier<Container> supplier) {
        this.container = supplier;
        return this;
    }

    public SideConfig config() {
        return config;
    }

    public Direction facing() {
        return facingFn.apply(owner.getBlockState());
    }

    // --- gated capability views (called by each loader's cap registration) --

    @Nullable
    public NeroEnergyStorage energyView(@Nullable Direction side) {
        if (energy == null || !config.has(Channel.ENERGY)) {
            return null;
        }
        NeroEnergyStorage delegate = energy.get();
        if (side == null) {
            return delegate; // unsided/internal access is ungated
        }
        RelativeFace face = FaceResolver.fromAbsolute(facing(), side);
        if (config.mode(Channel.ENERGY, face) == SideMode.DISABLED) {
            return null;
        }
        return SideGating.energy(delegate, () -> config.modeAbsolute(Channel.ENERGY, facing(), side));
    }

    @Nullable
    public NeroFluidStorage fluidView(@Nullable Direction side) {
        if (fluid == null || !config.has(Channel.FLUID)) {
            return null;
        }
        NeroFluidStorage delegate = fluid.get();
        if (side == null) {
            return delegate;
        }
        RelativeFace face = FaceResolver.fromAbsolute(facing(), side);
        if (config.mode(Channel.FLUID, face) == SideMode.DISABLED) {
            return null;
        }
        return SideGating.fluid(delegate, () -> config.modeAbsolute(Channel.FLUID, facing(), side));
    }

    @Nullable
    public NeroGasStorage gasView(@Nullable Direction side) {
        if (gas == null || !config.has(Channel.GAS)) {
            return null;
        }
        NeroGasStorage delegate = gas.get();
        if (side == null) {
            return delegate;
        }
        RelativeFace face = FaceResolver.fromAbsolute(facing(), side);
        if (config.mode(Channel.GAS, face) == SideMode.DISABLED) {
            return null;
        }
        return SideGating.gas(delegate, () -> config.modeAbsolute(Channel.GAS, facing(), side));
    }

    // --- item gating (drive the BE's WorldlyContainer methods from here) ----

    /** Slots exposed on {@code side} for the item channel (union of insertable + extractable). */
    public int[] itemSlotsForFace(Direction side) {
        ChannelConfig cfg = config.get(Channel.ITEM);
        if (cfg == null) {
            return EMPTY;
        }
        RelativeFace face = FaceResolver.fromAbsolute(facing(), side);
        int[] in = cfg.insertSlots(face);
        int[] out = cfg.extractSlots(face);
        if (in.length == 0) {
            return out;
        }
        if (out.length == 0) {
            return in;
        }
        java.util.TreeSet<Integer> set = new java.util.TreeSet<>();
        for (int s : in) {
            set.add(s);
        }
        for (int s : out) {
            set.add(s);
        }
        int[] merged = new int[set.size()];
        int i = 0;
        for (int s : set) {
            merged[i++] = s;
        }
        return merged;
    }

    public boolean canInsertItem(int slot, Direction side) {
        ChannelConfig cfg = config.get(Channel.ITEM);
        if (cfg == null) {
            return false;
        }
        return contains(cfg.insertSlots(FaceResolver.fromAbsolute(facing(), side)), slot);
    }

    public boolean canExtractItem(int slot, Direction side) {
        ChannelConfig cfg = config.get(Channel.ITEM);
        if (cfg == null) {
            return false;
        }
        return contains(cfg.extractSlots(FaceResolver.fromAbsolute(facing(), side)), slot);
    }

    // --- mutation + change notification -------------------------------------

    public boolean setModeAbsolute(Channel channel, Direction side, SideMode mode) {
        boolean changed = config.setModeAbsolute(channel, facing(), side, mode);
        if (changed) {
            markChanged();
        }
        return changed;
    }

    public boolean cycleAbsolute(Channel channel, Direction side) {
        RelativeFace face = FaceResolver.fromAbsolute(facing(), side);
        boolean changed = config.cycle(channel, face);
        if (changed) {
            markChanged();
        }
        return changed;
    }

    public boolean cycle(Channel channel, RelativeFace face) {
        boolean changed = config.cycle(channel, face);
        if (changed) {
            markChanged();
        }
        return changed;
    }

    public boolean setMode(Channel channel, RelativeFace face, SideMode mode) {
        boolean changed = config.setMode(channel, face, mode);
        if (changed) {
            markChanged();
        }
        return changed;
    }

    public void setAutoEject(Channel channel, boolean value) {
        if (config.setAutoEject(channel, value)) {
            markChanged();
        }
    }

    public void setAutoInput(Channel channel, boolean value) {
        if (config.setAutoInput(channel, value)) {
            markChanged();
        }
    }

    public void resetToPreset() {
        config.resetToPreset();
        markChanged();
    }

    public void paste(java.util.Map<Channel, Integer> packed) {
        config.applyPacked(packed);
        markChanged();
    }

    /** Persist, invalidate cached capabilities, and notify neighbours so pipes reconnect. */
    public void markChanged() {
        owner.setChanged();
        Level level = owner.getLevel();
        if (level != null && !level.isClientSide()) {
            BlockPos pos = owner.getBlockPos();
            Services.PLATFORM.invalidateCapabilities(level, pos);
            level.updateNeighborsAt(pos, owner.getBlockState().getBlock());
        }
    }

    // --- persistence --------------------------------------------------------

    public void save(ValueOutput output) {
        config.save(output);
    }

    public void load(ValueInput input) {
        config.load(input);
    }

    // --- auto-eject / auto-input tick ---------------------------------------

    /** Run optional auto-transfer for every channel whose toggle is on. Server-side only. */
    public void serverTick(Level level, BlockPos pos, int rate) {
        if (level.isClientSide() || rate <= 0) {
            return;
        }
        Direction machineFacing = facing();
        for (java.util.Map.Entry<Channel, ChannelConfig> entry : config.channels().entrySet()) {
            ChannelConfig cfg = entry.getValue();
            if (!cfg.autoEject() && !cfg.autoInput()) {
                continue;
            }
            for (Direction side : Direction.values()) {
                SideMode mode = config.mode(entry.getKey(), FaceResolver.fromAbsolute(machineFacing, side));
                BlockPos neighbour = pos.relative(side);
                if (cfg.autoEject() && mode.autoEjects()) {
                    push(entry.getKey(), level, neighbour, side, rate);
                }
                if (cfg.autoInput() && mode.autoInputs()) {
                    pull(entry.getKey(), level, neighbour, side, rate);
                }
            }
        }
    }

    private void push(Channel channel, Level level, BlockPos neighbour, Direction side, int rate) {
        Direction from = side.getOpposite();
        switch (channel) {
            case ENERGY -> {
                if (energy == null) {
                    return;
                }
                NeroEnergyStorage mine = energy.get();
                NeroEnergyStorage target = EnergyLookup.INSTANCE.find(level, neighbour, from);
                if (target == null) {
                    return;
                }
                long give = mine.extract(rate, true);
                long take = give > 0 ? target.insert(give, true) : 0;
                long moved = Math.min(give, take);
                if (moved > 0) {
                    mine.extract(moved, false);
                    target.insert(moved, false);
                }
            }
            case FLUID -> {
                if (fluid == null) {
                    return;
                }
                NeroFluidStorage mine = fluid.get();
                NeroFluidStorage target = FluidLookup.INSTANCE.find(level, neighbour, from);
                if (target == null || mine.getAmount() == 0) {
                    return;
                }
                Fluid f = mine.getFluid();
                long give = mine.drain(rate, true);
                long take = give > 0 ? target.fill(f, give, true) : 0;
                long moved = Math.min(give, take);
                if (moved > 0) {
                    mine.drain(moved, false);
                    target.fill(f, moved, false);
                }
            }
            case GAS -> {
                if (gas == null) {
                    return;
                }
                NeroGasStorage mine = gas.get();
                NeroGasStorage target = GasLookup.INSTANCE.find(level, neighbour, from);
                if (target == null || mine.getAmount() == 0) {
                    return;
                }
                Identifier g = mine.getGas();
                long give = mine.drain(rate, true);
                long take = give > 0 ? target.fill(g, give, true) : 0;
                long moved = Math.min(give, take);
                if (moved > 0) {
                    mine.drain(moved, false);
                    target.fill(g, moved, false);
                }
            }
            case ITEM -> pushItems(level, neighbour, side, rate);
        }
    }

    private void pull(Channel channel, Level level, BlockPos neighbour, Direction side, int rate) {
        Direction from = side.getOpposite();
        switch (channel) {
            case ENERGY -> {
                if (energy == null) {
                    return;
                }
                NeroEnergyStorage mine = energy.get();
                NeroEnergyStorage source = EnergyLookup.INSTANCE.find(level, neighbour, from);
                if (source == null) {
                    return;
                }
                long room = mine.insert(rate, true);
                long avail = room > 0 ? source.extract(room, true) : 0;
                long moved = Math.min(room, avail);
                if (moved > 0) {
                    source.extract(moved, false);
                    mine.insert(moved, false);
                }
            }
            case FLUID -> {
                if (fluid == null) {
                    return;
                }
                NeroFluidStorage mine = fluid.get();
                NeroFluidStorage source = FluidLookup.INSTANCE.find(level, neighbour, from);
                if (source == null || source.getAmount() == 0) {
                    return;
                }
                Fluid f = source.getFluid();
                long avail = source.drain(rate, true);
                long room = avail > 0 ? mine.fill(f, avail, true) : 0;
                long moved = Math.min(avail, room);
                if (moved > 0) {
                    source.drain(moved, false);
                    mine.fill(f, moved, false);
                }
            }
            case GAS -> {
                if (gas == null) {
                    return;
                }
                NeroGasStorage mine = gas.get();
                NeroGasStorage source = GasLookup.INSTANCE.find(level, neighbour, from);
                if (source == null || source.getAmount() == 0) {
                    return;
                }
                Identifier g = source.getGas();
                long avail = source.drain(rate, true);
                long room = avail > 0 ? mine.fill(g, avail, true) : 0;
                long moved = Math.min(avail, room);
                if (moved > 0) {
                    source.drain(moved, false);
                    mine.fill(g, moved, false);
                }
            }
            case ITEM -> pullItems(level, neighbour, side, rate);
        }
    }

    // Items have no Core lookup seam; auto-transfer covers adjacent vanilla Containers
    // (chests, hoppers, Core's Item Store). Mod pipes still pull via the gated WorldlyContainer.
    private void pushItems(Level level, BlockPos neighbour, Direction side, int rate) {
        if (container == null) {
            return;
        }
        if (!(level.getBlockEntity(neighbour) instanceof Container target)) {
            return;
        }
        Container mine = container.get();
        ChannelConfig cfg = config.get(Channel.ITEM);
        int[] outSlots = cfg == null ? EMPTY : cfg.extractSlots(FaceResolver.fromAbsolute(facing(), side));
        ItemMover.move(mine, outSlots, target, side.getOpposite(), rate);
    }

    private void pullItems(Level level, BlockPos neighbour, Direction side, int rate) {
        if (container == null) {
            return;
        }
        if (!(level.getBlockEntity(neighbour) instanceof Container source)) {
            return;
        }
        Container mine = container.get();
        ChannelConfig cfg = config.get(Channel.ITEM);
        int[] inSlots = cfg == null ? EMPTY : cfg.insertSlots(FaceResolver.fromAbsolute(facing(), side));
        ItemMover.moveInto(source, side.getOpposite(), mine, inSlots, rate);
    }

    private static boolean contains(int[] slots, int slot) {
        for (int s : slots) {
            if (s == slot) {
                return true;
            }
        }
        return false;
    }

    private static final int[] EMPTY = new int[0];

    /** Minimal loader-neutral vanilla-Container item shuttling for auto-transfer. */
    private static final class ItemMover {

        static void move(Container from, int[] fromSlots, Container to, Direction toSide, int max) {
            int budget = max;
            for (int slot : fromSlots) {
                if (budget <= 0) {
                    return;
                }
                net.minecraft.world.item.ItemStack stack = from.getItem(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                int moved = insert(to, toSide, stack, Math.min(budget, stack.getCount()));
                if (moved > 0) {
                    stack.shrink(moved);
                    from.setChanged();
                    budget -= moved;
                }
            }
        }

        static void moveInto(Container from, Direction fromSide, Container to, int[] toSlots, int max) {
            int budget = max;
            int size = from.getContainerSize();
            int[] sourceSlots = from instanceof WorldlyContainer wc ? wc.getSlotsForFace(fromSide) : range(size);
            for (int slot : sourceSlots) {
                if (budget <= 0) {
                    return;
                }
                net.minecraft.world.item.ItemStack stack = from.getItem(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                if (from instanceof WorldlyContainer wc && !wc.canTakeItemThroughFace(slot, stack, fromSide)) {
                    continue;
                }
                int moved = insertToSlots(to, toSlots, stack, Math.min(budget, stack.getCount()));
                if (moved > 0) {
                    stack.shrink(moved);
                    from.setChanged();
                    budget -= moved;
                }
            }
        }

        private static int insert(Container to, Direction toSide, net.minecraft.world.item.ItemStack stack, int count) {
            int[] slots = to instanceof WorldlyContainer wc ? wc.getSlotsForFace(toSide) : range(to.getContainerSize());
            return insertToSlots(to, slots, stack, count, toSide);
        }

        private static int insertToSlots(Container to, int[] slots, net.minecraft.world.item.ItemStack stack, int count) {
            return insertToSlots(to, slots, stack, count, null);
        }

        private static int insertToSlots(Container to, int[] slots, net.minecraft.world.item.ItemStack stack,
                int count, @Nullable Direction toSide) {
            int remaining = count;
            for (int slot : slots) {
                if (remaining <= 0) {
                    break;
                }
                if (toSide != null && to instanceof WorldlyContainer wc
                        && !wc.canPlaceItemThroughFace(slot, stack, toSide)) {
                    continue;
                }
                net.minecraft.world.item.ItemStack existing = to.getItem(slot);
                int cap = Math.min(to.getMaxStackSize(), stack.getMaxStackSize());
                if (existing.isEmpty()) {
                    int put = Math.min(remaining, cap);
                    net.minecraft.world.item.ItemStack copy = stack.copy();
                    copy.setCount(put);
                    to.setItem(slot, copy);
                    remaining -= put;
                    to.setChanged();
                } else if (net.minecraft.world.item.ItemStack.isSameItemSameComponents(existing, stack)) {
                    int put = Math.min(remaining, cap - existing.getCount());
                    if (put > 0) {
                        existing.grow(put);
                        remaining -= put;
                        to.setChanged();
                    }
                }
            }
            return count - remaining;
        }

        private static int[] range(int size) {
            int[] out = new int[size];
            for (int i = 0; i < size; i++) {
                out[i] = i;
            }
            return out;
        }
    }
}
