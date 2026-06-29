package za.co.neroland.nerolandcore.sideconfig;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Per-{@link Channel} configuration: the {@link SideMode} of each of the six
 * relative faces, which modes the machine permits, the bound {@link SlotGroup}s
 * (for slotted channels), optional per-face group overrides, and the auto-eject /
 * auto-input toggles. Owned by a {@link SideConfig}.
 *
 * <p>The dynamic state that round-trips through NBT is just the six face modes plus
 * the two auto toggles, packed into a single {@code int}; everything else
 * (permitted modes, slot groups, preset) is static declaration rebuilt from code.
 */
public final class ChannelConfig {

    // Packed-int layout: 6 faces x 3 bits (0..17), autoEject = bit 24, autoInput = bit 25.
    private static final int FACE_BITS = 3;
    private static final int FACE_MASK = 0b111;
    private static final int AUTO_EJECT_BIT = 1 << 24;
    private static final int AUTO_INPUT_BIT = 1 << 25;

    private final Channel channel;
    private final SideMode[] modes = new SideMode[RelativeFace.VALUES.length];
    private final boolean[] allowed = new boolean[SideMode.VALUES.length];
    private final List<SlotGroup> inputGroups = new ArrayList<>();
    private final List<SlotGroup> outputGroups = new ArrayList<>();
    private final Map<RelativeFace, SlotGroup> inputOverride = new EnumMap<>(RelativeFace.class);
    private final Map<RelativeFace, SlotGroup> outputOverride = new EnumMap<>(RelativeFace.class);
    private SidePreset preset = SidePreset.ALL_DISABLED;
    private boolean autoEject;
    private boolean autoInput;

    ChannelConfig(Channel channel) {
        this.channel = channel;
        // Permit the four common modes by default; PUSH is opt-in per machine.
        this.allowed[SideMode.DISABLED.ordinal()] = true;
        this.allowed[SideMode.INPUT.ordinal()] = true;
        this.allowed[SideMode.OUTPUT.ordinal()] = true;
        this.allowed[SideMode.IO.ordinal()] = true;
        java.util.Arrays.fill(this.modes, SideMode.DISABLED);
    }

    public Channel channel() {
        return channel;
    }

    // --- declaration (set by the builder) -----------------------------------

    void addInputGroup(SlotGroup group) {
        inputGroups.add(group);
    }

    void addOutputGroup(SlotGroup group) {
        outputGroups.add(group);
    }

    void setAllowed(SideMode mode, boolean value) {
        allowed[mode.ordinal()] = value;
    }

    void bindFaceOutput(RelativeFace face, SlotGroup group) {
        outputOverride.put(face, group);
    }

    void bindFaceInput(RelativeFace face, SlotGroup group) {
        inputOverride.put(face, group);
    }

    void setPreset(SidePreset preset) {
        this.preset = preset;
    }

    void setAutoEject(boolean value) {
        this.autoEject = value;
    }

    void setAutoInput(boolean value) {
        this.autoInput = value;
    }

    /** Seed every face from the preset, clamping to a permitted mode. */
    void applyPreset() {
        for (RelativeFace face : RelativeFace.VALUES) {
            modes[face.index()] = clamp(preset.defaultMode(channel, face));
        }
    }

    // --- queries ------------------------------------------------------------

    public boolean isAllowed(SideMode mode) {
        return allowed[mode.ordinal()];
    }

    public SidePreset preset() {
        return preset;
    }

    public SideMode mode(RelativeFace face) {
        return modes[face.index()];
    }

    public boolean autoEject() {
        return autoEject;
    }

    public boolean autoInput() {
        return autoInput;
    }

    public List<SlotGroup> inputGroups() {
        return inputGroups;
    }

    public List<SlotGroup> outputGroups() {
        return outputGroups;
    }

    /** Slots a neighbour may insert into through {@code face} (empty if the mode forbids it). */
    public int[] insertSlots(RelativeFace face) {
        if (!modes[face.index()].canInsert()) {
            return EMPTY;
        }
        SlotGroup override = inputOverride.get(face);
        return override != null ? override.slots() : union(inputGroups);
    }

    /** Slots a neighbour may extract from through {@code face} (empty if the mode forbids it). */
    public int[] extractSlots(RelativeFace face) {
        if (!modes[face.index()].canExtract()) {
            return EMPTY;
        }
        SlotGroup override = outputOverride.get(face);
        return override != null ? override.slots() : union(outputGroups);
    }

    // --- mutation (server-authoritative) ------------------------------------

    /** Set a face's mode if permitted; returns true if the value actually changed. */
    public boolean setMode(RelativeFace face, SideMode mode) {
        if (!allowed[mode.ordinal()] || modes[face.index()] == mode) {
            return false;
        }
        modes[face.index()] = mode;
        return true;
    }

    /** Advance a face to the next permitted mode in cycle order. Returns the new mode. */
    public SideMode cycle(RelativeFace face) {
        SideMode current = modes[face.index()];
        int n = SideMode.VALUES.length;
        for (int step = 1; step <= n; step++) {
            SideMode candidate = SideMode.VALUES[(current.ordinal() + step) % n];
            if (allowed[candidate.ordinal()]) {
                modes[face.index()] = candidate;
                return candidate;
            }
        }
        return current;
    }

    void setAutoEjectLive(boolean value) {
        this.autoEject = value;
    }

    void setAutoInputLive(boolean value) {
        this.autoInput = value;
    }

    void resetToPreset() {
        applyPreset();
    }

    SideMode clamp(SideMode desired) {
        if (allowed[desired.ordinal()]) {
            return desired;
        }
        // Nearest sensible fallback chain.
        SideMode[] fallback = switch (desired) {
            case IO -> new SideMode[] { SideMode.INPUT, SideMode.OUTPUT, SideMode.DISABLED };
            case PUSH -> new SideMode[] { SideMode.OUTPUT, SideMode.IO, SideMode.DISABLED };
            case INPUT -> new SideMode[] { SideMode.IO, SideMode.DISABLED };
            case OUTPUT -> new SideMode[] { SideMode.IO, SideMode.DISABLED };
            case DISABLED -> new SideMode[] { SideMode.DISABLED };
        };
        for (SideMode m : fallback) {
            if (allowed[m.ordinal()]) {
                return m;
            }
        }
        return SideMode.DISABLED;
    }

    // --- persistence --------------------------------------------------------

    int pack() {
        int packed = 0;
        for (RelativeFace face : RelativeFace.VALUES) {
            packed |= (modes[face.index()].ordinal() & FACE_MASK) << (face.index() * FACE_BITS);
        }
        if (autoEject) {
            packed |= AUTO_EJECT_BIT;
        }
        if (autoInput) {
            packed |= AUTO_INPUT_BIT;
        }
        return packed;
    }

    void unpack(int packed) {
        for (RelativeFace face : RelativeFace.VALUES) {
            int ordinal = (packed >> (face.index() * FACE_BITS)) & FACE_MASK;
            SideMode mode = ordinal < SideMode.VALUES.length ? SideMode.VALUES[ordinal] : SideMode.DISABLED;
            modes[face.index()] = clamp(mode);
        }
        this.autoEject = (packed & AUTO_EJECT_BIT) != 0;
        this.autoInput = (packed & AUTO_INPUT_BIT) != 0;
    }

    /** Copy another channel's dynamic state (modes + auto toggles), clamped to this machine's allowances. */
    void copyStateFrom(ChannelConfig other) {
        for (RelativeFace face : RelativeFace.VALUES) {
            modes[face.index()] = clamp(other.modes[face.index()]);
        }
        this.autoEject = other.autoEject;
        this.autoInput = other.autoInput;
    }

    private static int[] union(List<SlotGroup> groups) {
        if (groups.isEmpty()) {
            return EMPTY;
        }
        java.util.TreeSet<Integer> set = new java.util.TreeSet<>();
        for (SlotGroup group : groups) {
            for (int slot : group.slots()) {
                set.add(slot);
            }
        }
        int[] out = new int[set.size()];
        int i = 0;
        for (int slot : set) {
            out[i++] = slot;
        }
        return out;
    }

    private static final int[] EMPTY = new int[0];
}
