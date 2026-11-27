package za.co.neroland.nerolandcore.reclaim;

//? if >=1.21.5 {
import java.util.function.Consumer;
//?} else {
/*import java.util.List;
*///?}

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if >=1.21.5 {
import net.minecraft.world.item.component.TooltipDisplay;
//?}
import net.minecraft.world.level.block.Block;

/** The Ore Reclaimer's item: explains what it converts and when it refuses. */
public class OreReclaimerBlockItem extends BlockItem {

    private static final String[] LINES = {
            "tooltip.nerolandcore.ore_reclaimer.what",
            "tooltip.nerolandcore.ore_reclaimer.refuses"
    };

    public OreReclaimerBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    //? if >=1.21.5 {
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        for (String line : LINES) {
            tooltip.accept(Component.translatable(line).withStyle(ChatFormatting.GRAY));
        }
    }
    //?} else if >=1.20.5 {
    /*@Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        for (String line : LINES) {
            tooltip.add(Component.translatable(line).withStyle(ChatFormatting.GRAY));
        }
    }
    *///?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        for (String line : LINES) {
            tooltip.add(Component.translatable(line).withStyle(ChatFormatting.GRAY));
        }
    }
    *///?}
}
