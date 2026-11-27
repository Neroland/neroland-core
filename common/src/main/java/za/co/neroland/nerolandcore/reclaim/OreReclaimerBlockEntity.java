package za.co.neroland.nerolandcore.reclaim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.menu.OreReclaimerMenu;
import za.co.neroland.nerolandcore.registry.ModBlockEntities;

/**
 * Ore Reclaimer: an unpowered machine that slowly turns a Nero ore or raw material into a vanilla one, but
 * only while the ore's home mod is not installed ({@link ReclaimRecipes}). One input slot and one output
 * slot; hoppers and pipes insert from the top and sides and extract from the bottom.
 *
 * @since 1.25.0
 */
public class OreReclaimerBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    public static final int SIZE = 2;

    /** Nothing to do (empty input). */
    public static final int STATE_IDLE = 0;
    /** Converting the input. */
    public static final int STATE_WORKING = 1;
    /** The input's home mod is installed, so it is not converted. */
    public static final int STATE_HOME_MOD = 2;
    /** The input has no conversion. */
    public static final int STATE_UNKNOWN = 3;
    /** The output slot cannot take the result. */
    public static final int STATE_OUTPUT_FULL = 4;

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_TOTAL = 1;
    public static final int DATA_STATE = 2;
    public static final int DATA_COUNT = 3;

    private static final int[] TOP_AND_SIDES = {INPUT};
    private static final int[] BOTTOM = {OUTPUT};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int progress;
    private int total;
    private int state = STATE_IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROGRESS -> progress;
                case DATA_TOTAL -> total;
                case DATA_STATE -> state;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_PROGRESS -> progress = value;
                case DATA_TOTAL -> total = value;
                case DATA_STATE -> state = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public OreReclaimerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ORE_RECLAIMER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState blockState, OreReclaimerBlockEntity be) {
        be.tick(level.getServer());
        boolean working = be.state == STATE_WORKING;
        if (blockState.hasProperty(BlockStateProperties.LIT) && blockState.getValue(BlockStateProperties.LIT) != working) {
            level.setBlock(pos, blockState.setValue(BlockStateProperties.LIT, working), 3);
        }
    }

    private void tick(@Nullable MinecraftServer server) {
        ItemStack input = this.items.get(INPUT);
        if (server == null || input.isEmpty()) {
            setStatus(STATE_IDLE, 0, 0);
            return;
        }
        ReclaimRecipe recipe = ReclaimRecipes.find(server, input);
        if (recipe == null || recipe.resultStack().isEmpty()) {
            setStatus(STATE_UNKNOWN, 0, 0);
            return;
        }
        if (!recipe.active()) {
            setStatus(STATE_HOME_MOD, 0, 0);
            return;
        }
        ItemStack result = recipe.resultStack();
        if (!fits(this.items.get(OUTPUT), result)) {
            setStatus(STATE_OUTPUT_FULL, this.progress, recipe.time());
            return;
        }
        int next = this.progress + 1;
        if (next < recipe.time()) {
            setStatus(STATE_WORKING, next, recipe.time());
            return;
        }
        input.shrink(1);
        ItemStack out = this.items.get(OUTPUT);
        if (out.isEmpty()) {
            this.items.set(OUTPUT, result);
        } else {
            out.grow(result.getCount());
        }
        setStatus(STATE_WORKING, 0, recipe.time());
        this.setChanged();
    }

    private void setStatus(int newState, int newProgress, int newTotal) {
        boolean dirty = this.progress != newProgress;
        this.state = newState;
        this.progress = newProgress;
        this.total = newTotal;
        if (dirty && (newProgress % 20 == 0)) {
            this.setChanged();
        }
    }

    private static boolean fits(ItemStack out, ItemStack result) {
        if (out.isEmpty()) {
            return true;
        }
        //? if >=1.20.5 {
        boolean same = ItemStack.isSameItemSameComponents(out, result);
        //?} else {
        /*boolean same = ItemStack.isSameItemSameTags(out, result);
        *///?}
        return same && out.getCount() + result.getCount() <= out.getMaxStackSize();
    }

    /** Whether automation may insert {@code stack}: only items with a conversion that runs right now. */
    private boolean acceptsAutomated(ItemStack stack) {
        MinecraftServer server = this.level == null ? null : this.level.getServer();
        if (server == null) {
            return false;
        }
        ReclaimRecipe recipe = ReclaimRecipes.find(server, stack);
        return recipe != null && recipe.active();
    }

    public ContainerData data() {
        return this.data;
    }

    //? if >=1.21.6 {
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
    //?} else if >=1.20.5 {
    /*@Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ValueOutput output = ValueOutput.of(tag, registries);
    *///?} else {
    /*@Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag) {
        super.saveAdditional(tag);
        ValueOutput output = ValueOutput.of(tag, level == null ? net.minecraft.core.RegistryAccess.EMPTY : level.registryAccess());
    *///?}
        //? if >=1.21.6 {
        ContainerHelper.saveAllItems(output, this.items);
        //?} else if >=1.20.5 {
        /*ContainerHelper.saveAllItems(output.tag(), this.items, output.registries());
        *///?} else {
        /*ContainerHelper.saveAllItems(output.tag(), this.items);
        *///?}
        output.putInt("Progress", this.progress);
    }

    //? if >=1.21.6 {
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
    //?} else if >=1.20.5 {
    /*@Override
    protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ValueInput input = ValueInput.of(tag, registries);
    *///?} else {
    /*@Override
    public void load(net.minecraft.nbt.CompoundTag tag) {
        super.load(tag);
        ValueInput input = ValueInput.of(tag, level == null ? net.minecraft.core.RegistryAccess.EMPTY : level.registryAccess());
    *///?}
        this.items.clear();
        //? if >=1.21.6 {
        ContainerHelper.loadAllItems(input, this.items);
        //?} else if >=1.20.5 {
        /*ContainerHelper.loadAllItems(input.tag(), this.items, input.registries());
        *///?} else {
        /*ContainerHelper.loadAllItems(input.tag(), this.items);
        *///?}
        this.progress = input.getIntOr("Progress", 0);
    }

    // --- MenuProvider -----------------------------------------------------------------------------------
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.nerolandcore.ore_reclaimer");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new OreReclaimerMenu(containerId, playerInventory, this, this.data);
    }

    // --- WorldlyContainer -------------------------------------------------------------------------------
    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? BOTTOM : TOP_AND_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot == INPUT && side != Direction.DOWN && acceptsAutomated(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == OUTPUT && side == Direction.DOWN;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == INPUT;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        boolean newInput = slot == INPUT
                && !ItemStack.isSameItem(stack, this.items.get(INPUT));
        //? if >=1.20.5 {
        stack.limitSize(this.getMaxStackSize());
        //?} else {
        /*stack.setCount(Math.min(stack.getCount(), this.getMaxStackSize()));
        *///?}
        this.items.set(slot, stack);
        if (newInput) {
            this.progress = 0;
        }
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }
}
