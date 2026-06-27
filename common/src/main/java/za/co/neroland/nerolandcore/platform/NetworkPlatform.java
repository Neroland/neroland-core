package za.co.neroland.nerolandcore.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Cross-loader packet-send seam (counterpart to NeoForge {@code PacketDistributor} /
 * Forge {@code Channel} / Fabric {@code Server|ClientPlayNetworking.send}). Payload
 * <em>types</em> and handlers are declared once in
 * {@link za.co.neroland.nerolandcore.network.CoreNetwork}; each loader registers
 * them and implements this send interface, resolved via {@link Services}.
 */
public interface NetworkPlatform {

    /** Server → one client. */
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    /** Client → server (call only on the physical client). */
    void sendToServer(CustomPacketPayload payload);
}
