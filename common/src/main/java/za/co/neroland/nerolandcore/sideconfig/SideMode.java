package za.co.neroland.nerolandcore.sideconfig;

import java.util.Locale;

/**
 * How a single face interacts with a neighbour for one {@link Channel}.
 *
 * <ul>
 *   <li>{@link #DISABLED} — no connection, no capability exposed on that face.</li>
 *   <li>{@link #INPUT} — accepts the resource from the neighbour (insert-only view); pulled if auto-input is on.</li>
 *   <li>{@link #OUTPUT} — emits the resource to the neighbour (extract-only view); pushed if auto-eject is on.</li>
 *   <li>{@link #IO} — both insert and extract on the same face (where the channel allows it).</li>
 *   <li>{@link #PUSH} — an output that actively ejects even without a logistics network; behaves like
 *       {@link #OUTPUT} for capability views, but is always auto-ejected by the base tick.</li>
 * </ul>
 *
 * <p>The player-facing summary <em>input / output / power / disabled</em> maps directly:
 * <em>power</em> is the {@link Channel#ENERGY} channel's INPUT/OUTPUT/IO on a face, and
 * <em>disabled</em> is {@link #DISABLED} on every channel for that face.
 */
public enum SideMode {

    DISABLED,
    INPUT,
    OUTPUT,
    IO,
    PUSH;

    /** Stable ordinal order used for 3-bit packing; never reorder (max 8 values). */
    public static final SideMode[] VALUES = values();

    /** Whether a neighbour may insert the resource through this face. */
    public boolean canInsert() {
        return this == INPUT || this == IO;
    }

    /** Whether a neighbour may extract the resource through this face. */
    public boolean canExtract() {
        return this == OUTPUT || this == IO || this == PUSH;
    }

    /** Whether this face forms any connection at all (everything but {@link #DISABLED}). */
    public boolean connects() {
        return this != DISABLED;
    }

    /** Whether the base tick should actively push from this face when auto-eject is enabled. */
    public boolean autoEjects() {
        return this == OUTPUT || this == PUSH || this == IO;
    }

    /** Whether the base tick should actively pull into this face when auto-input is enabled. */
    public boolean autoInputs() {
        return this == INPUT || this == IO;
    }

    /** Lower-case id form, e.g. {@code "input"}. */
    public String lowerName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
