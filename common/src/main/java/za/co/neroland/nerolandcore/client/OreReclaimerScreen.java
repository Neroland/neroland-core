package za.co.neroland.nerolandcore.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import za.co.neroland.nerolandcore.menu.OreReclaimerMenu;
import za.co.neroland.nerolandcore.reclaim.OreReclaimerBlockEntity;

/**
 * Ore Reclaimer screen: the Trash Can's bevelled panel with two slot cells, a progress bar between them and
 * one status line that says why the machine is idle. No texture asset.
 */
public class OreReclaimerScreen extends AbstractContainerScreen<OreReclaimerMenu> {

    private static final int PANEL = 0xFFC6C6C6;
    private static final int HILITE = 0xFFFFFFFF;
    private static final int SHADOW = 0xFF555555;
    private static final int CELL_BORDER = 0xFF373737;
    private static final int CELL_FILL = 0xFF8B8B8B;
    private static final int BAR_TRACK = 0xFF5A5A5A;
    private static final int BAR_FILL = 0xFF4FB36B;
    private static final int TEXT = 0xFF404040;
    private static final int WARN = 0xFFA03030;

    private static final int BAR_X = 79;
    private static final int BAR_Y = 40;
    private static final int BAR_W = 30;
    private static final int BAR_H = 6;
    private static final int STATUS_Y = 58;

    public OreReclaimerScreen(OreReclaimerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    //? if >=26.1 {
    @Override
    public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
    //?} else {
    /*@Override
    public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphicsExtractor g, float partialTick, int mouseX, int mouseY) {
    *///?}
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        g.fill(x, y, x + w, y + h, PANEL);
        g.fill(x, y, x + w, y + 1, HILITE);
        g.fill(x, y, x + 1, y + h, HILITE);
        g.fill(x, y + h - 1, x + w, y + h, SHADOW);
        g.fill(x + w - 1, y, x + w, y + h, SHADOW);

        for (Slot slot : this.menu.slots) {
            int sx = x + slot.x;
            int sy = y + slot.y;
            g.fill(sx - 1, sy - 1, sx + 17, sy + 17, CELL_BORDER);
            g.fill(sx, sy, sx + 16, sy + 16, CELL_FILL);
        }

        g.fill(x + BAR_X, y + BAR_Y, x + BAR_X + BAR_W, y + BAR_Y + BAR_H, BAR_TRACK);
        int total = this.menu.total();
        if (total > 0 && this.menu.progress() > 0) {
            int filled = Math.min(BAR_W, this.menu.progress() * BAR_W / total);
            g.fill(x + BAR_X, y + BAR_Y, x + BAR_X + filled, y + BAR_Y + BAR_H, BAR_FILL);
        }

        Component status = status();
        int color = this.menu.state() == OreReclaimerBlockEntity.STATE_WORKING
                || this.menu.state() == OreReclaimerBlockEntity.STATE_IDLE ? TEXT : WARN;
        g.text(this.font, status, x + (w - this.font.width(status)) / 2, y + STATUS_Y, color, false);

        //? if >=26.1 {
        super.extractContents(g, mouseX, mouseY, partialTick);
        //?}
    }

    private Component status() {
        return switch (this.menu.state()) {
            case OreReclaimerBlockEntity.STATE_WORKING -> {
                int total = Math.max(1, this.menu.total());
                yield Component.translatable("gui.nerolandcore.ore_reclaimer.working",
                        this.menu.progress() * 100 / total);
            }
            case OreReclaimerBlockEntity.STATE_HOME_MOD -> Component.translatable("gui.nerolandcore.ore_reclaimer.home_mod");
            case OreReclaimerBlockEntity.STATE_UNKNOWN -> Component.translatable("gui.nerolandcore.ore_reclaimer.unknown");
            case OreReclaimerBlockEntity.STATE_OUTPUT_FULL -> Component.translatable("gui.nerolandcore.ore_reclaimer.output_full");
            default -> Component.translatable("gui.nerolandcore.ore_reclaimer.idle");
        };
    }
}
