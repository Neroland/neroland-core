package za.co.neroland.nerolandcore.sideconfig;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The universal, Mekanism-style side-configuration model: a per-{@link Channel} map
 * of the six relative faces to a {@link SideMode}, resolved against the block's
 * facing on every query. A machine declares its surface once via {@link #builder()};
 * Core does capability gating, persistence, sync and UI from this single object.
 *
 * <p>This type is loader-neutral world/block data keyed by position — it holds no
 * player identity and nothing personal (POPIA/GDPR: outside the player-erasure scope).
 *
 * @see SideConfigComponent for the attachable per-block-entity wrapper.
 */
public final class SideConfig {

    private static final String NBT_PREFIX = "NeroSideCfg_";

    private final EnumMap<Channel, ChannelConfig> channels;

    private SideConfig(EnumMap<Channel, ChannelConfig> channels) {
        this.channels = channels;
    }

    public static Builder builder() {
        return new Builder();
    }

    // --- structure ----------------------------------------------------------

    public boolean has(Channel channel) {
        return channels.containsKey(channel);
    }

    public ChannelConfig get(Channel channel) {
        return channels.get(channel);
    }

    public Map<Channel, ChannelConfig> channels() {
        return Collections.unmodifiableMap(channels);
    }

    /** Whether the machine declares more than one channel (drives whether the UI shows tabs). */
    public boolean isMultiChannel() {
        return channels.size() > 1;
    }

    // --- queries ------------------------------------------------------------

    /** The mode of a relative face for a channel, or {@link SideMode#DISABLED} if the channel is absent. */
    public SideMode mode(Channel channel, RelativeFace face) {
        ChannelConfig cfg = channels.get(channel);
        return cfg == null ? SideMode.DISABLED : cfg.mode(face);
    }

    /** The mode of an absolute face, resolving against {@code facing}. */
    public SideMode modeAbsolute(Channel channel, Direction facing, Direction side) {
        return mode(channel, FaceResolver.fromAbsolute(facing, side));
    }

    // --- mutation (server side) ---------------------------------------------

    /** Set an absolute face's mode; validates against the channel's allowances. Returns true if changed. */
    public boolean setModeAbsolute(Channel channel, Direction facing, Direction side, SideMode mode) {
        ChannelConfig cfg = channels.get(channel);
        return cfg != null && cfg.setMode(FaceResolver.fromAbsolute(facing, side), mode);
    }

    /** Set a relative face's mode; validates against the channel's allowances. Returns true if changed. */
    public boolean setMode(Channel channel, RelativeFace face, SideMode mode) {
        ChannelConfig cfg = channels.get(channel);
        return cfg != null && cfg.setMode(face, mode);
    }

    /** Cycle a relative face to the next permitted mode. Returns true if the channel exists. */
    public boolean cycle(Channel channel, RelativeFace face) {
        ChannelConfig cfg = channels.get(channel);
        if (cfg == null) {
            return false;
        }
        cfg.cycle(face);
        return true;
    }

    public boolean setAutoEject(Channel channel, boolean value) {
        ChannelConfig cfg = channels.get(channel);
        if (cfg == null) {
            return false;
        }
        cfg.setAutoEjectLive(value);
        return true;
    }

    public boolean setAutoInput(Channel channel, boolean value) {
        ChannelConfig cfg = channels.get(channel);
        if (cfg == null) {
            return false;
        }
        cfg.setAutoInputLive(value);
        return true;
    }

    /** Reset every channel's faces back to its declared preset. */
    public void resetToPreset() {
        channels.values().forEach(ChannelConfig::resetToPreset);
    }

    /** Paste another config's dynamic state onto this one (only channels both declare). */
    public void copyStateFrom(SideConfig other) {
        for (Map.Entry<Channel, ChannelConfig> entry : channels.entrySet()) {
            ChannelConfig source = other.channels.get(entry.getKey());
            if (source != null) {
                entry.getValue().copyStateFrom(source);
            }
        }
    }

    // --- persistence --------------------------------------------------------

    /** Serialise the dynamic state (one packed int per declared channel) into the BE's NBT. */
    public void save(ValueOutput output) {
        for (Map.Entry<Channel, ChannelConfig> entry : channels.entrySet()) {
            output.putInt(NBT_PREFIX + entry.getKey().lowerName(), entry.getValue().pack());
        }
    }

    /** Restore the dynamic state from NBT; absent keys keep the preset-seeded defaults. */
    public void load(ValueInput input) {
        for (Map.Entry<Channel, ChannelConfig> entry : channels.entrySet()) {
            // The packed value never sets the high bits, so -1 is a safe "absent" sentinel
            // (absent keys keep the preset-seeded defaults rather than resetting to ALL_DISABLED).
            int packed = input.getIntOr(NBT_PREFIX + entry.getKey().lowerName(), -1);
            if (packed != -1) {
                entry.getValue().unpack(packed);
            }
        }
    }

    // --- network snapshot (packed-int per channel) --------------------------

    /** A compact snapshot of the dynamic state for sync: one packed int per declared channel. */
    public Map<Channel, Integer> packAll() {
        EnumMap<Channel, Integer> out = new EnumMap<>(Channel.class);
        channels.forEach((channel, cfg) -> out.put(channel, cfg.pack()));
        return out;
    }

    /** Apply a packed snapshot (e.g. an authoritative server sync, or a paste). */
    public void applyPacked(Map<Channel, Integer> packed) {
        packed.forEach((channel, value) -> {
            ChannelConfig cfg = channels.get(channel);
            if (cfg != null) {
                cfg.unpack(value);
            }
        });
    }

    // --- builder ------------------------------------------------------------

    /**
     * Fluent author API. Declare channels, bind slot groups, set a preset, and
     * forbid specific modes per channel; {@link #build()} seeds faces from the preset.
     */
    public static final class Builder {

        private final EnumMap<Channel, ChannelConfig> channels = new EnumMap<>(Channel.class);
        private SidePreset preset = SidePreset.ALL_DISABLED;

        private Builder() {
        }

        private ChannelConfig channelConfig(Channel channel) {
            return channels.computeIfAbsent(channel, ChannelConfig::new);
        }

        /** Declare a non-slotted channel (typically {@link Channel#ENERGY}). */
        public Builder channel(Channel channel) {
            channelConfig(channel);
            return this;
        }

        /** Declare a slotted channel with an input group and an output group. */
        public Builder channel(Channel channel, SlotGroup inputGroup, SlotGroup outputGroup) {
            ChannelConfig cfg = channelConfig(channel);
            if (inputGroup != null) {
                cfg.addInputGroup(inputGroup);
            }
            if (outputGroup != null) {
                cfg.addOutputGroup(outputGroup);
            }
            return this;
        }

        /** Add another input slot group to an already-declared channel. */
        public Builder inputGroup(Channel channel, SlotGroup group) {
            channelConfig(channel).addInputGroup(group);
            return this;
        }

        /** Add another output slot group to an already-declared channel. */
        public Builder outputGroup(Channel channel, SlotGroup group) {
            channelConfig(channel).addOutputGroup(group);
            return this;
        }

        /**
         * Bind one specific relative face's OUTPUT to a specific slot group (e.g. the
         * Item Sorter's per-face filtered outputs). Overrides the channel's default
         * output groups for that face only.
         */
        public Builder bindFaceOutput(Channel channel, RelativeFace face, SlotGroup group) {
            channelConfig(channel).bindFaceOutput(face, group);
            return this;
        }

        /** Bind one specific relative face's INPUT to a specific slot group. */
        public Builder bindFaceInput(Channel channel, RelativeFace face, SlotGroup group) {
            channelConfig(channel).bindFaceInput(face, group);
            return this;
        }

        /** Permit or forbid a mode on a channel (e.g. forbid item I/O on a machine). */
        public Builder allow(Channel channel, SideMode mode, boolean allowed) {
            channelConfig(channel).setAllowed(mode, allowed);
            return this;
        }

        /** Default the auto-eject toggle on for a channel (player can still turn it off). */
        public Builder autoEject(Channel channel, boolean on) {
            channelConfig(channel).setAutoEject(on);
            return this;
        }

        /** Default the auto-input toggle on for a channel. */
        public Builder autoInput(Channel channel, boolean on) {
            channelConfig(channel).setAutoInput(on);
            return this;
        }

        /** Set the starting preset that seeds every channel's faces. */
        public Builder defaultPreset(SidePreset preset) {
            this.preset = preset;
            return this;
        }

        public SideConfig build() {
            for (ChannelConfig cfg : channels.values()) {
                cfg.setPreset(preset);
                cfg.applyPreset();
            }
            return new SideConfig(channels);
        }
    }
}
