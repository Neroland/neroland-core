package za.co.neroland.nerolandcore.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Version seam for item / block {@code Properties}. From 1.21.2 every item and block must carry its own
 * registry key ({@code Properties.setId(key)}); 1.21.1 has no {@code setId} and takes the id from the
 * registry instead. Core's registrations (and downstream mods') start from these factories so the
 * registration code is identical on every line.
 */
public final class PropertiesCompat {

    private PropertiesCompat() {
    }

    /** Fresh item properties bound to {@code key} (where the Minecraft line needs that). */
    public static Item.Properties item(ResourceKey<Item> key) {
        //? if >=1.21.2 {
        return new Item.Properties().setId(key);
        //?} else {
        /*return new Item.Properties();
        *///?}
    }

    /** Fresh block properties ({@code Properties.of()}) bound to {@code key} (where the line needs that). */
    public static BlockBehaviour.Properties block(ResourceKey<Block> key) {
        //? if >=1.21.2 {
        return BlockBehaviour.Properties.of().setId(key);
        //?} else {
        /*return BlockBehaviour.Properties.of();
        *///?}
    }
}
