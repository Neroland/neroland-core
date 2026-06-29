package za.co.neroland.nerolandcore.sideconfig;

/**
 * A named starting layout so a new machine ships with reasonable faces instead of a
 * blank slate. The chosen preset seeds every declared channel's six faces; the
 * player can then reconfigure freely. If a preset would pick a mode a channel
 * forbids (see {@code SideConfig.Builder#allow}), the build clamps it to the nearest
 * permitted mode.
 *
 * <ul>
 *   <li>{@link #GENERATOR} — power out on every face; material channels accept fuel in.</li>
 *   <li>{@link #PROCESSOR} — material in on most faces, out of the bottom; power in.</li>
 *   <li>{@link #STORAGE} — every face I/O (a passive endpoint that both fills and empties).</li>
 *   <li>{@link #ALL_INPUT} — every face accepts input on every channel.</li>
 *   <li>{@link #ALL_DISABLED} — every face disabled; a fully manual starting point.</li>
 * </ul>
 */
public enum SidePreset {

    GENERATOR,
    PROCESSOR,
    STORAGE,
    ALL_INPUT,
    ALL_DISABLED;

    /**
     * The default mode this preset assigns to one face of one channel, before any
     * {@code allow(...)} clamping.
     */
    public SideMode defaultMode(Channel channel, RelativeFace face) {
        return switch (this) {
            case ALL_DISABLED -> SideMode.DISABLED;
            case ALL_INPUT -> SideMode.INPUT;
            case STORAGE -> SideMode.IO;
            case GENERATOR -> channel == Channel.ENERGY ? SideMode.OUTPUT : SideMode.INPUT;
            case PROCESSOR -> {
                if (channel == Channel.ENERGY) {
                    yield SideMode.INPUT;
                }
                // Material channels: output from the bottom, accept input on every other face.
                yield face == RelativeFace.BOTTOM ? SideMode.OUTPUT : SideMode.INPUT;
            }
        };
    }
}
