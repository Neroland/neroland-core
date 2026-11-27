package za.co.neroland.nerolandcore.ore;

//? if >=1.21.5 {
import java.util.function.Consumer;
//?} else {
/*import java.util.List;
*///?}

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if >=1.21.5 {
import net.minecraft.world.item.component.TooltipDisplay;
//?}

/**
 * An item with one translated, styled tooltip line (Moon Cheese, Raw Europium).
 *
 * @since 1.25.0
 */
public class TooltipItem extends Item {

    private final String tooltipKey;
    private final ChatFormatting[] style;

    public TooltipItem(Properties properties, String tooltipKey, ChatFormatting... style) {
        super(properties);
        this.tooltipKey = tooltipKey;
        this.style = style;
    }

    /** The tooltip line's translation key. */
    public String tooltipKey() {
        return this.tooltipKey;
    }

    //? if >=1.21.5 {
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable(this.tooltipKey).withStyle(this.style));
    }
    //?} else if >=1.20.5 {
    /*@Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(this.tooltipKey).withStyle(this.style));
    }
    *///?} else {
    /*@Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(this.tooltipKey).withStyle(this.style));
    }
    *///?}
}
