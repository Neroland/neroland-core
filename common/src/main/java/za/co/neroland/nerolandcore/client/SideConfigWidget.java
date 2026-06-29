package za.co.neroland.nerolandcore.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.network.SideConfigIntentPayload;
import za.co.neroland.nerolandcore.platform.Services;
import za.co.neroland.nerolandcore.sideconfig.Channel;
import za.co.neroland.nerolandcore.sideconfig.ChannelConfig;
import za.co.neroland.nerolandcore.sideconfig.ClientSideConfig;
import za.co.neroland.nerolandcore.sideconfig.RelativeFace;
import za.co.neroland.nerolandcore.sideconfig.SideConfig;
import za.co.neroland.nerolandcore.sideconfig.SideConfigClipboard;
import za.co.neroland.nerolandcore.sideconfig.SideMode;
import za.co.neroland.nerolandcore.sideconfig.SideModeColors;

/**
 * The reusable Side Config tab: a flattened cube/net of the six relative faces a
 * player clicks to route each channel, plus per-channel sub-tabs, auto-eject /
 * auto-input toggles, and reset / copy / paste controls. Colour-coded by
 * {@link SideModeColors} so state reads at a glance.
 *
 * <p>Self-contained and composable: a downstream {@code AbstractContainerScreen}
 * constructs one (it sends a snapshot request on open), forwards
 * {@link #render} from its draw pass and {@link #mouseClicked} from its handler.
 * Server-authoritative: clicks send {@link SideConfigIntentPayload} intents; the
 * authoritative snapshot streams back via {@link ClientSideConfig} and is applied
 * here. Built so its primitives can seed the shared GUI toolkit.
 *
 * <p>Interaction: left-click a face cycles its mode forward; right-click disables
 * it; middle-click opens a palette of the modes that face permits.
 */
public final class SideConfigWidget {

    private static final int CELL = 18;
    private static final int STEP = 20;
    private static final int PANEL = 0xF0202024;
    private static final int BORDER = 0xFF000000;
    private static final int LABEL = 0xFFB0B0C0;
    private static final int ACTIVE = 0xFFFFFFFF;
    private static final int TAB_CLOSED = 0xFF3CB043;
    private static final int BTN = 0xFF454552;
    private static final int BTN_ON = 0xFF3CB043;

    private final BlockPos pos;
    private final SideConfig config;
    private final String typeKey;
    private final int anchorX;
    private final int anchorY;

    private boolean open;
    private Channel activeChannel;
    @Nullable
    private RelativeFace paletteFace;

    public SideConfigWidget(BlockPos pos, SideConfig config, String typeKey, int anchorX, int anchorY) {
        this.pos = pos.immutable();
        this.config = config;
        this.typeKey = typeKey;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.activeChannel = config.channels().keySet().iterator().hasNext()
                ? config.channels().keySet().iterator().next()
                : Channel.ENERGY;
        // Ask the server for the authoritative state of this machine's faces.
        Services.NETWORK.sendToServer(SideConfigIntentPayload.request(this.pos));
    }

    private void applyPending() {
        Map<Channel, Integer> packed = ClientSideConfig.poll(pos);
        if (packed != null) {
            config.applyPacked(packed);
        }
    }

    // --- rendering ----------------------------------------------------------

    public void render(GuiGraphicsExtractor g, int guiLeft, int guiTop, int mouseX, int mouseY) {
        applyPending();
        int px = guiLeft + anchorX;
        int py = guiTop + anchorY;

        // Tab toggle.
        box(g, px, py, CELL, CELL, open ? BTN_ON : TAB_CLOSED);
        if (!open) {
            return;
        }

        int contentTop = py + STEP;
        int width = 4 * STEP + 8;

        // Channel sub-tabs (skipped for single-channel machines).
        int netTop = contentTop;
        List<Channel> channels = new ArrayList<>(config.channels().keySet());
        if (config.isMultiChannel()) {
            int tx = px + 4;
            for (Channel channel : channels) {
                box(g, tx, contentTop, CELL, CELL, channel == activeChannel ? ACTIVE : BTN);
                inner(g, tx, contentTop, channelColor(channel));
                tx += STEP;
            }
            netTop = contentTop + STEP;
        }

        // Panel background behind the net + controls.
        int netOriginX = px + 4;
        int netOriginY = netTop + 2;
        int controlsY = netOriginY + 3 * STEP + 4;
        int panelBottom = controlsY + CELL + 4;
        panel(g, px, netTop, width, panelBottom - netTop);

        // The flattened cube net.
        ChannelConfig cfg = config.get(activeChannel);
        for (RelativeFace face : RelativeFace.VALUES) {
            int[] xy = faceCell(netOriginX, netOriginY, face);
            SideMode mode = cfg == null ? SideMode.DISABLED : cfg.mode(face);
            box(g, xy[0], xy[1], CELL, CELL, BORDER);
            inner(g, xy[0], xy[1], SideModeColors.of(mode));
        }

        // Controls row: auto-eject, auto-input, reset, copy, paste.
        int cx = netOriginX;
        boolean autoEject = cfg != null && cfg.autoEject();
        boolean autoInput = cfg != null && cfg.autoInput();
        box(g, cx, controlsY, CELL, CELL, autoEject ? BTN_ON : BTN);
        inner(g, cx, controlsY, SideModeColors.OUTPUT);
        cx += STEP;
        box(g, cx, controlsY, CELL, CELL, autoInput ? BTN_ON : BTN);
        inner(g, cx, controlsY, SideModeColors.INPUT);
        cx += STEP;
        box(g, cx, controlsY, CELL, CELL, BTN);
        inner(g, cx, controlsY, LABEL);
        cx += STEP;
        box(g, cx, controlsY, CELL, CELL, BTN);
        inner(g, cx, controlsY, 0xFF60C0FF);
        cx += STEP;
        box(g, cx, controlsY, CELL, CELL, SideConfigClipboard.hasFor(typeKey) ? BTN_ON : BTN);
        inner(g, cx, controlsY, 0xFFC0FF60);

        // Mode palette (shift-clicked a face).
        if (paletteFace != null && cfg != null) {
            int swx = netOriginX + 4 * STEP + 2;
            int swy = netOriginY;
            for (SideMode mode : SideMode.VALUES) {
                if (!cfg.isAllowed(mode)) {
                    continue;
                }
                box(g, swx, swy, CELL, CELL, BORDER);
                inner(g, swx, swy, SideModeColors.of(mode));
                swy += STEP;
            }
        }
    }

    // --- input --------------------------------------------------------------

    public boolean mouseClicked(double mx, double my, int button, int guiLeft, int guiTop) {
        int px = guiLeft + anchorX;
        int py = guiTop + anchorY;

        // Tab toggle.
        if (in(mx, my, px, py, CELL, CELL)) {
            open = !open;
            paletteFace = null;
            return true;
        }
        if (!open) {
            return false;
        }

        int contentTop = py + STEP;
        int netTop = contentTop;
        List<Channel> channels = new ArrayList<>(config.channels().keySet());
        if (config.isMultiChannel()) {
            int tx = px + 4;
            for (Channel channel : channels) {
                if (in(mx, my, tx, contentTop, CELL, CELL)) {
                    activeChannel = channel;
                    paletteFace = null;
                    return true;
                }
                tx += STEP;
            }
            netTop = contentTop + STEP;
        }

        int netOriginX = px + 4;
        int netOriginY = netTop + 2;
        ChannelConfig cfg = config.get(activeChannel);

        // Palette swatches take priority while open.
        if (paletteFace != null && cfg != null) {
            int swx = netOriginX + 4 * STEP + 2;
            int swy = netOriginY;
            for (SideMode mode : SideMode.VALUES) {
                if (!cfg.isAllowed(mode)) {
                    continue;
                }
                if (in(mx, my, swx, swy, CELL, CELL)) {
                    send(SideConfigIntentPayload.setMode(pos, activeChannel.ordinal(), paletteFace.index(), mode.ordinal()));
                    if (cfg.setMode(paletteFace, mode)) {
                        // optimistic; server re-syncs authoritative state
                    }
                    paletteFace = null;
                    return true;
                }
                swy += STEP;
            }
            paletteFace = null;
        }

        // Faces.
        for (RelativeFace face : RelativeFace.VALUES) {
            int[] xy = faceCell(netOriginX, netOriginY, face);
            if (in(mx, my, xy[0], xy[1], CELL, CELL)) {
                if (button == 2) {
                    paletteFace = face;
                } else if (button == 1) {
                    send(SideConfigIntentPayload.setMode(pos, activeChannel.ordinal(), face.index(), SideMode.DISABLED.ordinal()));
                    if (cfg != null) {
                        cfg.setMode(face, SideMode.DISABLED);
                    }
                } else {
                    send(SideConfigIntentPayload.cycle(pos, activeChannel.ordinal(), face.index()));
                    if (cfg != null) {
                        cfg.cycle(face);
                    }
                }
                return true;
            }
        }

        // Controls row.
        int controlsY = netOriginY + 3 * STEP + 4;
        int cx = netOriginX;
        if (in(mx, my, cx, controlsY, CELL, CELL)) {
            boolean on = cfg != null && cfg.autoEject();
            send(SideConfigIntentPayload.autoEject(pos, activeChannel.ordinal(), !on));
            return true;
        }
        cx += STEP;
        if (in(mx, my, cx, controlsY, CELL, CELL)) {
            boolean on = cfg != null && cfg.autoInput();
            send(SideConfigIntentPayload.autoInput(pos, activeChannel.ordinal(), !on));
            return true;
        }
        cx += STEP;
        if (in(mx, my, cx, controlsY, CELL, CELL)) {
            send(SideConfigIntentPayload.reset(pos));
            return true;
        }
        cx += STEP;
        if (in(mx, my, cx, controlsY, CELL, CELL)) {
            SideConfigClipboard.copy(typeKey, config.packAll());
            return true;
        }
        cx += STEP;
        if (in(mx, my, cx, controlsY, CELL, CELL)) {
            if (SideConfigClipboard.hasFor(typeKey)) {
                send(SideConfigIntentPayload.paste(pos, SideConfigClipboard.packed()));
            }
            return true;
        }

        // Click anywhere else in the open panel is still consumed so it does not fall through to slots.
        int width = 4 * STEP + 8;
        return in(mx, my, px, netTop, width, controlsY + CELL + 4 - netTop);
    }

    public boolean isOpen() {
        return open;
    }

    private static void send(SideConfigIntentPayload payload) {
        Services.NETWORK.sendToServer(payload);
    }

    private static int[] faceCell(int ox, int oy, RelativeFace face) {
        int col;
        int row;
        switch (face) {
            case TOP -> {
                col = 1;
                row = 0;
            }
            case LEFT -> {
                col = 0;
                row = 1;
            }
            case FRONT -> {
                col = 1;
                row = 1;
            }
            case RIGHT -> {
                col = 2;
                row = 1;
            }
            case BACK -> {
                col = 3;
                row = 1;
            }
            default -> {
                col = 1;
                row = 2;
            }
        }
        return new int[] { ox + col * STEP, oy + row * STEP };
    }

    private static int channelColor(Channel channel) {
        return switch (channel) {
            case ITEM -> 0xFFE8E8F4;
            case FLUID -> 0xFF3C78F0;
            case GAS -> 0xFF78D2F0;
            case ENERGY -> 0xFFE0C020;
        };
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static void box(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + h, color);
    }

    private static void inner(GuiGraphicsExtractor g, int x, int y, int color) {
        g.fill(x + 3, y + 3, x + CELL - 3, y + CELL - 3, color);
    }

    private static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, PANEL);
    }
}
