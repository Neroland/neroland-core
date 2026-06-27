package za.co.neroland.nerolandcore.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import za.co.neroland.nerolandcore.config.ConfigManager;
import za.co.neroland.nerolandcore.platform.Services;
import za.co.neroland.nerolandcore.progression.ClientGates;
import za.co.neroland.nerolandcore.progression.ProgressionGates;

/**
 * Cross-loader networking registry. Subsystems declare their payloads here once
 * (type + stream codec + handler); each loader iterates these lists and wires them
 * to its own networking API (NeoForge {@code PayloadRegistrar}, Forge
 * {@code ChannelBuilder}, Fabric {@code PayloadTypeRegistry} + receivers). Sending
 * goes through the {@link Services#NETWORK} seam.
 *
 * <p>V1 registers one payload: the {@link ConfigSyncPayload} that pushes
 * server-authoritative config to clients. Its clientbound handler is pure common
 * code ({@link ConfigManager#applyServerValues}), so it is safe to register from
 * either side.
 */
public final class CoreNetwork {

    /** A server → client payload + the client-side handler that consumes it. */
    public record Clientbound<T extends CustomPacketPayload>(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            Consumer<T> handler) {
    }

    /** A client → server payload + the server-side handler (with the sending player). */
    public record Serverbound<T extends CustomPacketPayload>(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            BiConsumer<T, ServerPlayer> handler) {
    }

    private static final List<Clientbound<?>> CLIENTBOUND = new ArrayList<>();
    private static final List<Serverbound<?>> SERVERBOUND = new ArrayList<>();

    private CoreNetwork() {
    }

    public static <T extends CustomPacketPayload> void clientbound(
            CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, Consumer<T> handler) {
        CLIENTBOUND.add(new Clientbound<>(type, codec, handler));
    }

    public static <T extends CustomPacketPayload> void serverbound(
            CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler) {
        SERVERBOUND.add(new Serverbound<>(type, codec, handler));
    }

    public static List<Clientbound<?>> clientbound() {
        return CLIENTBOUND;
    }

    public static List<Serverbound<?>> serverbound() {
        return SERVERBOUND;
    }

    /** Called from common init so the payload lists exist before each loader registers them. */
    public static void init() {
        clientbound(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC,
                payload -> ConfigManager.applyServerValues(payload.values()));
        clientbound(GateSyncPayload.TYPE, GateSyncPayload.STREAM_CODEC,
                payload -> ClientGates.accept(payload.gates()));
    }

    /** Everything a joining player needs pushed to them: server-authoritative config + their open gates. */
    public static void onPlayerJoin(ServerPlayer player) {
        sendConfigTo(player);
        ProgressionGates.syncTo(player);
    }

    /** Push the current server-authoritative config snapshot to one player. */
    public static void sendConfigTo(ServerPlayer player) {
        Services.NETWORK.sendToPlayer(player, ConfigSyncPayload.of(ConfigManager.serverAuthoritativeSnapshot()));
    }

    /** Push the current snapshot to every online player (call after a reload). */
    public static void syncAll(MinecraftServer server) {
        ConfigSyncPayload payload = ConfigSyncPayload.of(ConfigManager.serverAuthoritativeSnapshot());
        server.getPlayerList().getPlayers().forEach(player -> Services.NETWORK.sendToPlayer(player, payload));
    }
}
