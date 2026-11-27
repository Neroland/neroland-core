package za.co.neroland.nerolandcore.reclaim;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.platform.Services;

/**
 * One Ore Reclaimer conversion, read from {@code data/<namespace>/neroland/ore_reclaiming/<name>.json}.
 *
 * <pre>{@code
 * {
 *   "input": "nerolandcore:hematite_ore",          // an item id, or "#namespace:tag"
 *   "result": { "id": "minecraft:iron_ore", "count": 1 },
 *   "time": 1600,                                  // ticks per item; default 1600 (eight furnace smelts)
 *   "when_absent": ["nerospace"]                   // runs only while none of these mods is installed
 * }
 * }</pre>
 *
 * @param id         the file's id, for logs
 * @param item       the input item id, or {@code null} when {@code tag} is set
 * @param tag        the input item tag, or {@code null} when {@code item} is set
 * @param result     the output item id
 * @param count      output count per input item (1 to 64)
 * @param time       ticks per input item (at least 1)
 * @param whenAbsent mod ids that must all be absent for the conversion to run
 * @since 1.25.0
 */
public record ReclaimRecipe(Identifier id, @Nullable Identifier item, @Nullable TagKey<Item> tag, Identifier result,
        int count, int time, List<String> whenAbsent) {

    /** Default ticks per item: eight times a furnace's 200. */
    public static final int DEFAULT_TIME = 1600;

    public ReclaimRecipe {
        whenAbsent = List.copyOf(whenAbsent);
    }

    /** Whether {@code stack} is this conversion's input. */
    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (this.item != null) {
            return this.item.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
        return this.tag != null && stack.is(this.tag);
    }

    /** Whether every mod in {@code when_absent} is missing, so the conversion may run. */
    public boolean active() {
        for (String mod : this.whenAbsent) {
            if (Services.PLATFORM.isModLoaded(mod)) {
                return false;
            }
        }
        return true;
    }

    /** A fresh output stack, or empty if the result item is not registered. */
    public ItemStack resultStack() {
        Item out = BuiltInRegistries.ITEM.getValue(this.result);
        if (out == null || out == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(out, this.count);
    }

    static TagKey<Item> itemTag(Identifier id) {
        return TagKey.create(Registries.ITEM, id);
    }
}
