package za.co.neroland.nerolandcore.sideconfig;

/**
 * A named set of inventory slot indices a machine binds to a {@link Channel#ITEM}
 * (or other slotted channel) so the player configures faces as "this side is an
 * output" rather than "this side is slot 4". A face in {@link SideMode#INPUT} binds
 * to the channel's input group(s); {@link SideMode#OUTPUT} to the output group(s).
 *
 * <p>The four conventional roles are {@code input}, {@code output}, {@code battery}
 * and {@code upgrade}, but any name is allowed — the Item Sorter, for instance,
 * declares one output group per filtered face and binds each to a specific
 * {@link RelativeFace} (see {@code SideConfig.Builder#bindFaceGroup}).
 */
public record SlotGroup(String name, int[] slots) {

    public SlotGroup {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("SlotGroup name must be non-blank");
        }
        slots = slots == null ? new int[0] : slots.clone();
    }

    /** Convenience factory: {@code SlotGroup.of("input", 0, 1)}. */
    public static SlotGroup of(String name, int... slots) {
        return new SlotGroup(name, slots);
    }

    /** Conventional role names. */
    public static final String INPUT = "input";
    public static final String OUTPUT = "output";
    public static final String BATTERY = "battery";
    public static final String UPGRADE = "upgrade";

    @Override
    public int[] slots() {
        return slots.clone();
    }
}
