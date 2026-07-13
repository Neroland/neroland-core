package za.co.neroland.nerolandcore.link.display;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

/**
 * The cosmetic content a driver mod pushes onto a {@link DisplaySurface}: a line of
 * text, an optional icon, and a status colour. That is the <b>entire</b> payload — by
 * construction there is no field that could carry player-identifying data.
 *
 * <p><b>POPIA/GDPR.</b> A display payload is pure cosmetic world-state (text/icon/colour
 * a builder or driver chooses to show). It is exempt from Core's per-player erasure hook
 * because it stores nothing attributable to a player. Drivers must keep it that way:
 * never place a player's private data (names beyond what the game already makes public,
 * locations of other players, account identifiers, …) into a display payload.
 *
 * @param text         the text to show, or {@code null} to leave text unchanged/blank
 * @param icon         an optional icon id (a sprite/texture the surface knows how to
 *                     draw), or {@code null} for none
 * @param statusColour packed {@code 0xRRGGBB} accent/status colour (e.g. green = ok,
 *                     red = alert); {@code -1} means "unset / use the surface default"
 */
public record DisplayPayload(@Nullable Component text, @Nullable Identifier icon, int statusColour) {

    /** A blank payload (no text, no icon, default colour) — a surface's cleared state. */
    public static final DisplayPayload BLANK = new DisplayPayload(null, null, -1);

    /** Text only, default colour. */
    public static DisplayPayload text(Component text) {
        return new DisplayPayload(text, null, -1);
    }

    /** Text plus a status colour. */
    public static DisplayPayload text(Component text, int statusColour) {
        return new DisplayPayload(text, null, statusColour);
    }

    /** Whether a status colour is set (not {@code -1}). */
    public boolean hasStatusColour() {
        return statusColour != -1;
    }
}
