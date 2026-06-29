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
 * Server &rarr; client authoritative snapshot of a machine's side configuration: one
 * packed int per declared {@link Channel}, keyed by block position. Sent on request
 * (screen open) and after any server-applied change, so an open screen always shows
 * the real routing.
 *
 * <p><b>Privacy (POPIA/GDPR):</b> carries only a position and routing modes.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public record SideConfigSyncPayload(BlockPos pos, Map<Channel, Integer> packed) implements CustomPacketPayload {

    public static final Type<SideConfigSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "side_config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SideConfigSyncPayload> STREAM_CODEC =
            StreamCodec.of(SideConfigSyncPayload::write, SideConfigSyncPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, SideConfigSyncPayload payload) {
        buf.writeBlockPos(payload.pos);
        buf.writeVarInt(payload.packed.size());
        payload.packed.forEach((channel, packed) -> {
            buf.writeByte(channel.ordinal());
            buf.writeVarInt(packed);
        });
    }

    private static SideConfigSyncPayload read(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int n = buf.readVarInt();
        Map<Channel, Integer> packed = new EnumMap<>(Channel.class);
        for (int i = 0; i < n; i++) {
            int ordinal = buf.readByte();
            int p = buf.readVarInt();
            if (ordinal >= 0 && ordinal < Channel.VALUES.length) {
                packed.put(Channel.VALUES[ordinal], p);
            }
        }
        return new SideConfigSyncPayload(pos, packed);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
