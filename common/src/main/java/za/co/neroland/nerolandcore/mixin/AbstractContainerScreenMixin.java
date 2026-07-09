package za.co.neroland.nerolandcore.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import za.co.neroland.nerolandcore.client.ItemHighlights;

/**
 * Draws the Nero item-highlight border (see {@link ItemHighlights}) at the head of
 * {@code extractSlot}, i.e. before the slot's item is drawn, so the border renders
 * beneath the item in every {@code AbstractContainerScreen} on all three loaders.
 */
@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenMixin {

    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void nerolandcore$extractSlotHighlight(
            GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        ItemHighlights.extractSlotHighlight(graphics, slot);
    }
}
