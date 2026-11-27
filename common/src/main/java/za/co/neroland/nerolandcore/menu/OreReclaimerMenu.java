package za.co.neroland.nerolandcore.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import za.co.neroland.nerolandcore.reclaim.OreReclaimerBlockEntity;
import za.co.neroland.nerolandcore.registry.ModMenuTypes;

/** Ore Reclaimer menu: input slot, output slot (take only), progress and status synced as data slots. */
public class OreReclaimerMenu extends AbstractContainerMenu {

    public static final int INPUT_X = 56;
    public static final int OUTPUT_X = 116;
    public static final int SLOT_Y = 35;

    private static final int MACHINE_SLOTS = OreReclaimerBlockEntity.SIZE;
    private static final int PLAYER_END = MACHINE_SLOTS + 36;

    private final Container machine;
    private final ContainerData data;

    /** Client constructor. */
    public OreReclaimerMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(OreReclaimerBlockEntity.SIZE),
                new SimpleContainerData(OreReclaimerBlockEntity.DATA_COUNT));
    }

    public OreReclaimerMenu(int id, Inventory playerInventory, Container machine, ContainerData data) {
        super(ModMenuTypes.ORE_RECLAIMER.get(), id);
        checkContainerSize(machine, OreReclaimerBlockEntity.SIZE);
        checkContainerDataCount(data, OreReclaimerBlockEntity.DATA_COUNT);
        this.machine = machine;
        this.data = data;

        this.addSlot(new Slot(machine, OreReclaimerBlockEntity.INPUT, INPUT_X, SLOT_Y));
        this.addSlot(new Slot(machine, OreReclaimerBlockEntity.OUTPUT, OUTPUT_X, SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
        this.addDataSlots(data);
    }

    public int progress() {
        return this.data.get(OreReclaimerBlockEntity.DATA_PROGRESS);
    }

    public int total() {
        return this.data.get(OreReclaimerBlockEntity.DATA_TOTAL);
    }

    public int state() {
        return this.data.get(OreReclaimerBlockEntity.DATA_STATE);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < MACHINE_SLOTS) {
            if (!this.moveItemStackTo(stack, MACHINE_SLOTS, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, OreReclaimerBlockEntity.INPUT, OreReclaimerBlockEntity.INPUT + 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.machine.stillValid(player);
    }
}
