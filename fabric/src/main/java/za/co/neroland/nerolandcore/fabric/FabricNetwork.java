package za.co.neroland.nerolandcore.fabric;

//? if >=1.20.5 {
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import za.co.neroland.nerolandcore.network.CoreNetwork;
import za.co.neroland.nerolandcore.platform.NetworkPlatform;

// Fabric side of the networking seam. {@link #registerCommon()} (mod init, both
// sides) registers every payload type and the serverbound receivers;
// {@link #registerClient()} (client init) registers the clientbound receivers,
// keeping {@code ClientPlayNetworking} off the dedicated server until then.
// Registered via {@code META-INF/services}.
public final class FabricNetwork implements NetworkPlatform {

    // Mod-init (both sides): payload types + serverbound receivers.
    public static void registerCommon() {
        for (CoreNetwork.Clientbound<?> cb : CoreNetwork.clientbound()) {
            registerClientboundType(cb);
        }
        for (CoreNetwork.Serverbound<?> sb : CoreNetwork.serverbound()) {
            registerServerbound(sb);
        }
    }

    // Client-init: clientbound receivers (client-only API).
    public static void registerClient() {
        for (CoreNetwork.Clientbound<?> cb : CoreNetwork.clientbound()) {
            registerClientReceiver(cb);
        }
    }

    private static <T extends CustomPacketPayload> void registerClientboundType(CoreNetwork.Clientbound<T> cb) {
        PayloadTypeRegistry.clientboundPlay().register(cb.type(), cb.codec());
    }

    private static <T extends CustomPacketPayload> void registerServerbound(CoreNetwork.Serverbound<T> sb) {
        PayloadTypeRegistry.serverboundPlay().register(sb.type(), sb.codec());
        ServerPlayNetworking.registerGlobalReceiver(sb.type(), (payload, context) -> {
            ServerPlayer player = context.player();
            player.level().getServer().execute(() -> sb.handler().accept(payload, player));
        });
    }

    private static <T extends CustomPacketPayload> void registerClientReceiver(CoreNetwork.Clientbound<T> cb) {
        ClientPlayNetworking.registerGlobalReceiver(cb.type(), (payload, context) ->
                context.client().execute(() -> cb.handler().accept(payload)));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
//?} else {
/*import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import za.co.neroland.nerolandcore.network.CoreNetwork;
import za.co.neroland.nerolandcore.network.legacy.LegacyPayload;
import za.co.neroland.nerolandcore.network.legacy.LegacyStreamCodec;
import za.co.neroland.nerolandcore.platform.NetworkPlatform;

public final class FabricNetwork implements NetworkPlatform {
    public static void registerCommon() {
        for (CoreNetwork.Serverbound<?> packet : CoreNetwork.serverbound()) {
            registerServerbound(packet);
        }
    }

    public static void registerClient() {
        for (CoreNetwork.Clientbound<?> packet : CoreNetwork.clientbound()) {
            registerClientbound(packet);
        }
    }

    private static <T extends LegacyPayload> void registerServerbound(CoreNetwork.Serverbound<T> packet) {
        ServerPlayNetworking.registerGlobalReceiver(packet.type().id(), (server, player, handler, buffer, sender) -> {
            T message = packet.codec().decode(buffer);
            server.execute(() -> packet.handler().accept(message, player));
        });
    }

    private static <T extends LegacyPayload> void registerClientbound(CoreNetwork.Clientbound<T> packet) {
        ClientPlayNetworking.registerGlobalReceiver(packet.type().id(), (client, handler, buffer, sender) -> {
            T message = packet.codec().decode(buffer);
            client.execute(() -> packet.handler().accept(message));
        });
    }

    @Override
    public void sendToPlayer(ServerPlayer player, LegacyPayload payload) {
        var id = payload.type().id();
        if (ServerPlayNetworking.canSend(player, id)) {
            for (CoreNetwork.Clientbound<?> packet : CoreNetwork.clientbound()) {
                if (packet.type().equals(payload.type())) {
                    ServerPlayNetworking.send(player, id, encodeClientbound(packet, payload));
                    return;
                }
            }
            throw new IllegalArgumentException("Unregistered clientbound payload " + id);
        }
    }

    @Override
    public void sendToServer(LegacyPayload payload) {
        var id = payload.type().id();
        if (ClientPlayNetworking.canSend(id)) {
            for (CoreNetwork.Serverbound<?> packet : CoreNetwork.serverbound()) {
                if (packet.type().equals(payload.type())) {
                    ClientPlayNetworking.send(id, encodeServerbound(packet, payload));
                    return;
                }
            }
            throw new IllegalArgumentException("Unregistered serverbound payload " + id);
        }
    }

    private static <T extends LegacyPayload> FriendlyByteBuf encodeClientbound(CoreNetwork.Clientbound<T> packet,
            LegacyPayload payload) {
        return encode(packet.type(), packet.codec(), payload);
    }

    private static <T extends LegacyPayload> FriendlyByteBuf encodeServerbound(CoreNetwork.Serverbound<T> packet,
            LegacyPayload payload) {
        return encode(packet.type(), packet.codec(), payload);
    }

    private static <T extends LegacyPayload> FriendlyByteBuf encode(LegacyPayload.Type<T> type,
            LegacyStreamCodec<? super FriendlyByteBuf, T> codec, LegacyPayload payload) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        codec.encode(buffer, type.payloadClass().cast(payload));
        return buffer;
    }
}
*///?}
