package za.co.neroland.nerolandcore.data;

import java.lang.ref.WeakReference;
import java.util.function.LongSupplier;

import net.minecraft.server.MinecraftServer;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.config.CoreConfig;

/**
 * Runs Core's data-retention housekeeping on a schedule (Core 1.18.0): once when a server starts (its
 * first tick) and then once a day while it runs.
 *
 * <ul>
 *   <li>the {@code dataRetentionDays} inactivity purge ({@link PlayerDataErasure#purgeInactiveReport}),
 *       when {@code dataRetentionDays > 0} and {@code dataRetentionScheduledPurge} is on (default);</li>
 *   <li>expiry of {@linkplain ErasureTombstones owner tombstones} older than
 *       {@code max(dataRetentionDays, 365)} days (always).</li>
 * </ul>
 *
 * <p>Each loader calls {@link #onServerTick} from its end-of-server-tick event. The per-tick cost is one
 * counter increment; the clock is read once a minute. Logs counts only, never a player.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class RetentionSchedule {

    /** Ticks between clock checks (one minute at 20 TPS). */
    static final int CHECK_EVERY_TICKS = 1_200;
    static final long INTERVAL_MS = 86_400_000L;

    /** Wall clock; tests swap it. */
    static LongSupplier clock = System::currentTimeMillis;

    /** The server the last run was for; weak so a stopped integrated server is not kept alive. */
    private static WeakReference<MinecraftServer> lastServer = new WeakReference<>(null);
    private static long lastRunAt;
    private static int ticks;

    private RetentionSchedule() {
    }

    /** Called by each loader at the end of every server tick (server thread). */
    public static void onServerTick(MinecraftServer server) {
        if (server == null) {
            return;
        }
        boolean newServer = lastServer.get() != server;
        if (!newServer && ++ticks < CHECK_EVERY_TICKS) {
            return;
        }
        ticks = 0;
        long now = clock.getAsLong();
        if (!due(newServer, lastRunAt, now)) {
            return;
        }
        // Set first, so a failing run is retried tomorrow rather than every minute.
        lastServer = new WeakReference<>(server);
        lastRunAt = now;
        run(server);
    }

    /** Whether a run is due: always for a newly started server, else once {@link #INTERVAL_MS} has passed. */
    static boolean due(boolean newServer, long lastRunAt, long now) {
        return newServer || now - lastRunAt >= INTERVAL_MS;
    }

    /** One housekeeping pass. Never throws; failures are logged with the exception class only. */
    static void run(MinecraftServer server) {
        int days = CoreConfig.DATA_RETENTION_DAYS.get();
        try {
            ErasureTombstones tombstones = ErasureTombstones.get(server);
            int expired = tombstones.pruneExpiredOwners(ErasureTombstones.ownerLifetimeDays(days));
            if (expired > 0) {
                tombstones.backupNow(server);
                NerolandCoreCommon.LOGGER.info("[Neroland Core] Expired {} erasure tombstone(s).", expired);
            }
        } catch (RuntimeException failure) {
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Erasure tombstone expiry failed ({}).",
                    failure.getClass().getName());
        }
        if (days <= 0 || !CoreConfig.DATA_RETENTION_SCHEDULED.get()) {
            return;
        }
        try {
            PlayerDataErasure.PurgeResult result = PlayerDataErasure.purgeInactiveReport(server);
            if (result.failed() > 0) {
                NerolandCoreCommon.LOGGER.warn("[Neroland Core] Scheduled retention sweep: {} purged, {} incomplete "
                        + "(retried at the next sweep).", result.purged(), result.failed());
            }
        } catch (RuntimeException failure) {
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Scheduled retention sweep failed ({}).",
                    failure.getClass().getName());
        }
    }
}
