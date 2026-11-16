package za.co.neroland.nerolandcore.map;

import org.jetbrains.annotations.Nullable;

/**
 * How an area is drawn (Core 1.19.0): fill and border colours as ARGB ({@code 0} for the consumer's
 * default) and an optional label. The label follows {@link MapLabel}'s privacy rule.
 *
 * @param fillArgb   fill colour
 * @param borderArgb border colour
 * @param label      optional label, or {@code null}
 */
public record MapAreaStyle(int fillArgb, int borderArgb, @Nullable MapLabel label) {

    /** A style with no label. */
    public static MapAreaStyle of(int fillArgb, int borderArgb) {
        return new MapAreaStyle(fillArgb, borderArgb, null);
    }

    /** A copy with {@code newLabel}. */
    public MapAreaStyle withLabel(@Nullable MapLabel newLabel) {
        return new MapAreaStyle(fillArgb, borderArgb, newLabel);
    }
}
