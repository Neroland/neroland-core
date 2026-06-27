package za.co.neroland.nerolandcore.upgrade;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.config.CoreConfig;

/**
 * A reusable bank of upgrade-module slots any machine embeds. It owns the slots and
 * counts modules by {@link UpgradeType}; {@link UpgradeModifiers} turns those counts
 * into effects. The host supplies a {@link Classifier} mapping a stack to its upgrade
 * type, so Core doesn't have to ship the module items itself — a downstream mod
 * defines the items and the mapping (often a simple tag check).
 *
 * <p>Slot count is clamped to {@link CoreConfig#UPGRADE_MODULE_SLOT_CAP} so the
 * server caps how many modules any machine can expose.
 */
public final class UpgradeContainer {

    /** Maps an item stack to the upgrade type it provides, or {@code null} if it is not a module. */
    @FunctionalInterface
    public interface Classifier {
        @Nullable
        UpgradeType classify(ItemStack stack);
    }

    private final NonNullList<ItemStack> items;
    private final Classifier classifier;
    private final Runnable onChange;

    public UpgradeContainer(int slots, Classifier classifier, Runnable onChange) {
        int capped = Math.max(0, Math.min(slots, CoreConfig.UPGRADE_MODULE_SLOT_CAP.get()));
        this.items = NonNullList.withSize(capped, ItemStack.EMPTY);
        this.classifier = classifier;
        this.onChange = onChange;
    }

    public NonNullList<ItemStack> items() {
        return this.items;
    }

    public int slots() {
        return this.items.size();
    }

    public ItemStack getStack(int index) {
        return this.items.get(index);
    }

    public void setStack(int index, ItemStack stack) {
        this.items.set(index, stack);
        this.onChange.run();
    }

    public boolean isModule(ItemStack stack) {
        return !stack.isEmpty() && this.classifier.classify(stack) != null;
    }

    /** Total modules of {@code type} across all slots (counts stack sizes). */
    public int count(UpgradeType type) {
        int total = 0;
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty() && this.classifier.classify(stack) == type) {
                total += stack.getCount();
            }
        }
        return total;
    }

    // --- persistence (host calls these from save/loadAdditional) ------------

    public void save(ValueOutput output) {
        for (int i = 0; i < this.items.size(); i++) {
            output.store("Upgrade" + i, ItemStack.OPTIONAL_CODEC, this.items.get(i));
        }
    }

    public void load(ValueInput input) {
        for (int i = 0; i < this.items.size(); i++) {
            this.items.set(i, input.read("Upgrade" + i, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        }
    }
}
