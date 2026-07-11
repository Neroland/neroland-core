package za.co.neroland.nerolandcore.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/** Server-to-client bounded snapshot of resolved material milestone pairs. */
@org.jetbrains.annotations.ApiStatus.Internal
public record MaterialMilestoneSyncPayload(List<String> values) implements CustomPacketPayload {

    public static final Type<MaterialMilestoneSyncPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "material_milestone_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MaterialMilestoneSyncPayload> STREAM_CODEC =
            StreamCodec.of(MaterialMilestoneSyncPayload::write, MaterialMilestoneSyncPayload::read);
    private static final int MAX_VALUES = 4096;
    private static final int MAX_VALUE_LENGTH = 512;

    private static void write(RegistryFriendlyByteBuf buffer, MaterialMilestoneSyncPayload payload) {
        int count = Math.min(payload.values().size(), MAX_VALUES);
        buffer.writeVarInt(count);
        for (int index = 0; index < count; index++) {
            buffer.writeUtf(payload.values().get(index), MAX_VALUE_LENGTH);
        }
    }

    private static MaterialMilestoneSyncPayload read(RegistryFriendlyByteBuf buffer) {
        int count = Math.min(buffer.readVarInt(), MAX_VALUES);
        List<String> values = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            values.add(buffer.readUtf(MAX_VALUE_LENGTH));
        }
        return new MaterialMilestoneSyncPayload(values);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
