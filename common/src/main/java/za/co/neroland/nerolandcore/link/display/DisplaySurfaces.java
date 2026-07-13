package za.co.neroland.nerolandcore.link.display;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * The shared dashboard-surface registry (added in Core 1.9.0): the seam between the mod
 * that <i>provides</i> cosmetic surfaces (NeroDecor's holograms/control panels) and the
 * mods that <i>drive</i> them (NeroSecurity, NeroLogistics). Core holds only this
 * registry + a no-op default; it ships no surfaces itself, so a Core-only or
 * NeroDecor-only server is fully functional (pushes simply no-op).
 *
 * <p><b>Providers</b> call {@link #register(DisplayAddress, DisplaySurface)} when a
 * surface block loads and {@link #unregister(DisplayAddress)} when it unloads.
 * <b>Drivers</b> call {@link #push(DisplayAddress, DisplayPayload)} (or the per-field
 * helpers) addressed by {@link DisplayAddress}; if no surface is currently present the
 * call is cached and returns {@code false}, and the cached payload is re-applied when a
 * matching surface next registers — so a driver need not care about chunk-load timing.
 *
 * <p>All content is cosmetic (see {@link DisplayPayload} for the POPIA/GDPR exemption).
 * Thread-safe; registration and pushes happen on the server thread.
 */
public final class DisplaySurfaces {

    private static final ConcurrentMap<DisplayAddress, DisplaySurface> SURFACES = new ConcurrentHashMap<>();
    private static final ConcurrentMap<DisplayAddress, DisplayPayload> LAST_PAYLOAD = new ConcurrentHashMap<>();

    private DisplaySurfaces() {
    }

    // --- provider side ------------------------------------------------------

    /**
     * Register a surface at {@code address}. If a payload was pushed while no surface was
     * present, it is applied immediately so the surface restores its last content.
     */
    public static void register(DisplayAddress address, DisplaySurface surface) {
        SURFACES.put(address, surface);
        DisplayPayload cached = LAST_PAYLOAD.get(address);
        if (cached != null) {
            surface.apply(cached);
        }
    }

    /** Unregister the surface at {@code address} (call on unload). The cached payload is kept. */
    public static void unregister(DisplayAddress address) {
        SURFACES.remove(address);
    }

    /** Unregister and forget any cached payload for {@code address} (call when the block is broken). */
    public static void remove(DisplayAddress address) {
        SURFACES.remove(address);
        LAST_PAYLOAD.remove(address);
    }

    /** The live surface at {@code address}, if one is registered. */
    public static Optional<DisplaySurface> find(DisplayAddress address) {
        return Optional.ofNullable(SURFACES.get(address));
    }

    /** Whether a live surface is registered at {@code address}. */
    public static boolean isPresent(DisplayAddress address) {
        return SURFACES.containsKey(address);
    }

    // --- driver side --------------------------------------------------------

    /**
     * Push a whole payload to the surface at {@code address}. Always caches the payload;
     * applies it to the live surface if one is present.
     *
     * @return {@code true} if a live surface received it, {@code false} if only cached
     */
    public static boolean push(DisplayAddress address, DisplayPayload payload) {
        LAST_PAYLOAD.put(address, payload);
        DisplaySurface surface = SURFACES.get(address);
        if (surface != null) {
            surface.apply(payload);
            return true;
        }
        return false;
    }

    /** Convenience: set text only. */
    public static boolean setText(DisplayAddress address, Component text) {
        return push(address, DisplayPayload.text(text));
    }

    /** Convenience: set icon only. */
    public static boolean setIcon(DisplayAddress address, Identifier icon) {
        return push(address, new DisplayPayload(null, icon, -1));
    }

    /** Convenience: set status colour only ({@code 0xRRGGBB}). */
    public static boolean setStatusColour(DisplayAddress address, int rgb) {
        return push(address, new DisplayPayload(null, null, rgb));
    }

    /** Convenience: clear the surface and forget its cached payload state to blank. */
    public static boolean clear(DisplayAddress address) {
        LAST_PAYLOAD.put(address, DisplayPayload.BLANK);
        DisplaySurface surface = SURFACES.get(address);
        if (surface != null) {
            surface.clear();
            return true;
        }
        return false;
    }

    /**
     * Test/reload support: drop all registrations and cached payloads. Not part of the
     * stable API.
     */
    @org.jetbrains.annotations.ApiStatus.Internal
    public static void clearAll() {
        SURFACES.clear();
        LAST_PAYLOAD.clear();
    }
}
