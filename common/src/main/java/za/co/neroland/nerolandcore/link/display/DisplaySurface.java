package za.co.neroland.nerolandcore.link.display;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A placed cosmetic surface (a NeroDecor hologram sign or control panel) that a driver
 * mod can push display content onto. Core defines <b>only this interface</b> plus the
 * {@link DisplaySurfaces} registry and a no-op default — <b>all real logic lives in the
 * providing mod</b> (NeroDecor stores the payload in its block entity and renders it).
 *
 * <p>A surface registers itself with {@link DisplaySurfaces} under its
 * {@link DisplayAddress} while loaded, and unregisters on unload. Driver mods
 * (NeroSecurity, NeroLogistics) look surfaces up by address and call these methods; if no
 * surface is present the call degrades gracefully (see {@link DisplaySurfaces}).
 *
 * <p>All content is cosmetic (text/icon/colour); see {@link DisplayPayload} for the
 * POPIA/GDPR note. Methods are invoked on the server thread.
 */
public interface DisplaySurface {

    /** Set the surface's text line. */
    void setText(Component text);

    /** Set the surface's icon (a sprite/texture id the surface knows how to draw). */
    void setIcon(Identifier icon);

    /** Set the surface's status/accent colour, packed {@code 0xRRGGBB}. */
    void setStatusColour(int rgb);

    /** Clear the surface back to blank. */
    void clear();

    /**
     * Apply a whole {@link DisplayPayload} at once. The default applies each non-null
     * field then the status colour; implementations may override for atomic updates.
     */
    default void apply(DisplayPayload payload) {
        if (payload == null || payload == DisplayPayload.BLANK) {
            clear();
            return;
        }
        if (payload.text() != null) {
            setText(payload.text());
        }
        if (payload.icon() != null) {
            setIcon(payload.icon());
        }
        if (payload.hasStatusColour()) {
            setStatusColour(payload.statusColour());
        }
    }
}
