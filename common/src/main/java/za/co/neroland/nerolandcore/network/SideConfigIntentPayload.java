package za.co.neroland.nerolandcore.network;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.sideconfig.Channel;

/**
 * Client &rarr; server intent for the universal side-config system: "do {@code op} to
 * the machine at {@code pos}". The server validates it (range + the channel's
 * {@code allow(...)} rules) and applies it; clients never mutate routing directly.
 *
 * <p><b>Privacy (POPIA/GDPR):</b> carries only a block position and routing modes —
 * no player identity or other personal data.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public record SideConfigIntentPayload(
        BlockPos pos, int op, int channel, int face, int mode, boolean value,
        Map<Channel, Integer> packed) implements CustomPacketPayload {

    // Operation codes.
    public static final int OP_SET_MODE = 0;
    public static final int OP_CYCLE = 1;
    public static final int OP_AUTO_EJECT = 2;
    public static final int OP_AUTO_INPUT = 3;
    public static final int OP_RESET = 4;
    public static final int OP_PASTE = 5;
    public static final int OP_REQUEST = 6;

    public static final Type<SideConfigIntentPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "side_config_intent"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SideConfigIntentPayload> STREAM_CODEC =
            StreamCodec.of(SideConfigIntentPayload::write, SideConfigIntentPayload::read);

    public static SideConfigIntentPayload setMode(BlockPos pos, int channel, int face, int mode) {
        return new SideConfigIntentPayload(pos, OP_SET_MODE, channel, face, mode, false, Map.of());
    }

    public static SideConfigIntentPayload cycle(BlockPos pos, int channel, int face) {
        return new SideConfigIntentPayload(pos, OP_CYCLE, channel, face, 0, false, Map.of());
    }

    public static SideConfigIntentPayload autoEject(BlockPos pos, int channel, boolean on) {
        return new SideConfigIntentPayload(pos, OP_AUTO_EJECT, channel, 0, 0, on, Map.of());
    }

    public static SideConfigIntentPayload autoInput(BlockPos pos, int channel, boolean on) {
        return new SideConfigIntentPayload(pos, OP_AUTO_INPUT, channel, 0, 0, on, Map.of());
    }

    public static SideConfigIntentPayload reset(BlockPos pos) {
        return new SideConfigIntentPayload(pos, OP_RESET, 0, 0, 0, false, Map.of());
    }

    public static SideConfigIntentPayload paste(BlockPos pos, Map<Channel, Integer> packed) {
        return new SideConfigIntentPayload(pos, OP_PASTE, 0, 0, 0, false, packed);
    }

    public static SideConfigIntentPayload request(BlockPos pos) {
        return new SideConfigIntentPayload(pos, OP_REQUEST, 0, 0, 0, false, Map.of());
    }

    private static void write(RegistryFriendlyByteBuf buf, SideConfigIntentPayload payload) {
        buf.writeBlockPos(payload.pos);
        buf.writeByte(payload.op);
        buf.writeByte(payload.channel);
        buf.writeByte(payload.face);
        buf.writeByte(payload.mode);
        buf.writeBoolean(payload.value);
        buf.writeVarInt(payload.packed.size());
        payload.packed.forEach((channel, packed) -> {
            buf.writeByte(channel.ordinal());
            buf.writeVarInt(packed);
        });
    }

    private static SideConfigIntentPayload read(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int op = buf.readByte();
        int channel = buf.readByte();
        int face = buf.readByte();
        int mode = buf.readByte();
        boolean value = buf.readBoolean();
        int n = buf.readVarInt();
        Map<Channel, Integer> packed = new EnumMap<>(Channel.class);
        for (int i = 0; i < n; i++) {
            int ordinal = buf.readByte();
            int p = buf.readVarInt();
            if (ordinal >= 0 && ordinal < Channel.VALUES.length) {
                packed.put(Channel.VALUES[ordinal], p);
            }
        }
        return new SideConfigIntentPayload(pos, op, channel, face, mode, value, packed);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
