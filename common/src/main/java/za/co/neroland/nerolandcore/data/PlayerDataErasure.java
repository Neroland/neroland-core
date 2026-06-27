package za.co.neroland.nerolandcore.data;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.server.MinecraftServer;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.config.CoreConfig;

/**
 * The shared per-player data-erasure hook (POPIA/GDPR). Every Core-storing system —
 * and every downstream mod that stores player data (NeroEconomy, NeroFactions,
 * NeroSecurity, NeroQuests, NeroEvents) — registers a {@link PlayerDataEraser} here,
 * so a single {@link #erase(MinecraftServer, UUID)} call purges a player across the
 * whole ecosystem. The same registry drives the inactivity {@linkplain
 * #purgeInactive(MinecraftServer) retention sweep}.
 *
 * <p>Erasure does not log the player's identity (a UUID is personal data); only an
 * anonymous count/acknowledgement is logged.
 */
public final class PlayerDataErasure {

    private static final List<PlayerDataEraser> ERASERS = new CopyOnWriteArrayList<>();

    private PlayerDataErasure() {
    }

    /** Register a system's eraser. Call once at init. */
    public static void register(PlayerDataEraser eraser) {
        ERASERS.add(eraser);
    }

    /** Purge everything stored for {@code player} across every registered system. */
    public static void erase(MinecraftServer server, UUID player) {
        for (PlayerDataEraser eraser : ERASERS) {
            try {
                eraser.erase(server, player);
            } catch (RuntimeException e) {
                NerolandCoreCommon.LOGGER.warn("[Neroland Core] A data eraser failed during erasure.", e);
            }
        }
        NerolandCoreCommon.LOGGER.info("[Neroland Core] Player data erased on request ({} systems).", ERASERS.size());
    }

    /**
     * Erase data for every player inactive longer than the configured retention
     * period ({@code dataRetentionDays}; 0 disables). @return the number purged.
     */
    public static int purgeInactive(MinecraftServer server) {
        int days = CoreConfig.DATA_RETENTION_DAYS.get();
        if (days <= 0) {
            return 0;
        }
        List<UUID> stale = PlayerActivity.get(server).stalerThan(days);
        for (UUID player : stale) {
            erase(server, player);
        }
        if (!stale.isEmpty()) {
            NerolandCoreCommon.LOGGER.info("[Neroland Core] Retention sweep purged {} inactive player record(s).",
                    stale.size());
        }
        return stale.size();
    }
}
