package za.co.neroland.nerolandcore.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * Server → client snapshot of the gate ids open for the receiving player (already
 * resolved across server/team/player scope). The client stores it in
 * {@link za.co.neroland.nerolandcore.progression.ClientGates}.
 *
 * <p>Privacy (POPIA/GDPR): carries only gate ids — never identity or world data.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public record GateSyncPayload(List<String> gates) implements CustomPacketPayload {

    public static final Type<GateSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "gate_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GateSyncPayload> STREAM_CODEC =
            StreamCodec.of(GateSyncPayload::write, GateSyncPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, GateSyncPayload payload) {
        buf.writeVarInt(payload.gates.size());
        payload.gates.forEach(buf::writeUtf);
    }

    private static GateSyncPayload read(RegistryFriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<String> gates = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            gates.add(buf.readUtf());
        }
        return new GateSyncPayload(gates);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
