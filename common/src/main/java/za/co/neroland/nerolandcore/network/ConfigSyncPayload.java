package za.co.neroland.nerolandcore.network;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * Server → client snapshot of every server-authoritative config value, keyed
 * {@code <modId>:<key>}. Sent on player join and after {@code /neroland config
 * reload}; the client applies it through
 * {@link za.co.neroland.nerolandcore.config.ConfigManager#applyServerValues(Map)}.
 *
 * <p><b>Privacy (POPIA/GDPR):</b> carries only config keys and their values — never
 * any player identity or world data.
 */
public record ConfigSyncPayload(Map<String, String> values) implements CustomPacketPayload {

    public static final Type<ConfigSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> STREAM_CODEC =
            StreamCodec.of(ConfigSyncPayload::write, ConfigSyncPayload::read);

    public static ConfigSyncPayload of(Map<String, String> values) {
        return new ConfigSyncPayload(values);
    }

    private static void write(RegistryFriendlyByteBuf buf, ConfigSyncPayload payload) {
        buf.writeVarInt(payload.values.size());
        payload.values.forEach((key, value) -> {
            buf.writeUtf(key);
            buf.writeUtf(value);
        });
    }

    private static ConfigSyncPayload read(RegistryFriendlyByteBuf buf) {
        int n = buf.readVarInt();
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            String key = buf.readUtf();
            String value = buf.readUtf();
            values.put(key, value);
        }
        return new ConfigSyncPayload(values);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
