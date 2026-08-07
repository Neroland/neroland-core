package za.co.neroland.nerolandcore.data;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.telemetry.NerolandCoreTelemetry;

/**
 * Shared crash-safe accessor for ecosystem {@link SavedData} stores.
 *
 * <p>Vanilla's {@code SavedDataStorage.computeIfAbsent} reads {@code data/<id>.dat} on first access
 * and lets any failure (corrupt, truncated or unreadable file) propagate unchecked. Because these
 * stores are fetched from per-tick and per-command paths, one bad file crashes the server tick loop
 * on <em>every</em> tick — the failure class behind MC-NEROSPACE-H. Every Neroland
 * {@code SavedData.get()} must therefore route through {@link #get}, which degrades to a recovered
 * store rather than a repeating hard crash:</p>
 *
 * <ol>
 *   <li><b>Primary:</b> vanilla storage ({@code computeIfAbsent}).</li>
 *   <li><b>Backup:</b> on a read failure, restore from Core's last-known-good file
 *       ({@code data/<namespace>_<path>_backup.dat} in the same dimension folder), so state rolls
 *       back to the last backup instead of being lost outright.</li>
 *   <li><b>Fresh:</b> if the backup is missing or unreadable too, start with a fresh instance —
 *       degraded but playable, never a hard crash.</li>
 * </ol>
 *
 * <p>Whichever step wins is installed into the storage cache via {@code set} (so later ticks hit the
 * cache instead of re-reading the bad file) and marked dirty (so a clean primary file is rewritten
 * at the next level save). The failure is logged once and reported as a <em>handled</em>
 * (non-fatal) telemetry event through the existing scrubbed pipeline. Recovery metadata is
 * deliberately non-identifying: only the stable store label and the dimension are logged or
 * attached to telemetry.
 *
 * <p><b>Backup writing</b> piggybacks on {@link #get}: at most every {@value #DIRTY_INTERVAL_MS} ms
 * while the store is dirty (state changed) or every {@value #IDLE_INTERVAL_MS} ms otherwise, the
 * store is re-encoded with its own {@link SavedDataType#codec() codec}; the file is only rewritten
 * when the content actually changed (hash compare) or the backup has gone missing, via
 * write-temp-then-atomic-rename so a crash mid-write can never corrupt the backup itself.
 * Best-effort by design: a backup failure only logs at debug and never affects gameplay.
 *
 * <p><b>Privacy (POPIA/GDPR).</b> The backup holds exactly the same fields as the vanilla primary
 * file, inside the same world save — so it is a <em>second copy of the same player-keyed rows</em>
 * and an erasure request must reach it. Every Core store therefore calls {@link #backupNow}
 * immediately after purging a player (see {@code ProgressionState.eraseFor} and friends), instead
 * of leaving the erased rows in the backup until the next periodic pass. Downstream mods that adopt
 * this helper <b>must do the same</b> in their {@link PlayerDataEraser}.
 *
 * @see PlayerDataErasure
 */
public final class SavedDataRecovery {

    /** While dirty (state changed since the last vanilla save), refresh the backup at most this often. */
    private static final long DIRTY_INTERVAL_MS = 5_000L;
    /** Re-check interval when not dirty (still hash-compared, so unchanged state writes nothing). */
    private static final long IDLE_INTERVAL_MS = 5L * 60_000L;

    /**
     * Throttle + content bookkeeping, one entry per (dimension, store). A concurrent map rather than
     * a plain one: Core is a library and cannot assume every downstream call site is on the server
     * thread. The key is a record rather than a concatenated string because {@link #get} sits on
     * per-tick paths (gate checks) and must not allocate a formatted dimension string per call.
     */
    private static final Map<BackupKey, BackupState> BACKUPS = new ConcurrentHashMap<>();

    private SavedDataRecovery() {
    }

    /**
     * Loads a store through vanilla storage, recovering from exceptions and unexpected nulls, and
     * opportunistically refreshing this store's last-known-good backup.
     *
     * @param level    the level whose data storage owns the store (Core's own stores use the overworld)
     * @param type     the vanilla saved-data type
     * @param fallback supplier for a fresh, empty store; must never return {@code null}
     * @param name     stable, non-identifying label for logs/telemetry and the backup file name
     *                 (e.g. {@code "nerolandcore:progression"})
     */
    public static <T extends SavedData> T get(ServerLevel level, SavedDataType<T> type,
            Supplier<T> fallback, String name) {
        if (level == null || type == null || fallback == null || name == null || name.isBlank()) {
            throw new IllegalArgumentException("SavedData recovery arguments must be present");
        }
        T instance;
        try {
            instance = level.getDataStorage().computeIfAbsent(type);
        } catch (Exception failure) {
            instance = recover(level, type, fallback, name, failure);
        }
        if (instance == null) {
            // computeIfAbsent's generic return is unannotated; a null here would NPE the caller
            // mid-tick just as surely as a thrown exception, so it takes the same recovery path.
            instance = recover(level, type, fallback, name, null);
        }
        maybeBackup(level, type, instance, name, false);
        return instance;
    }

    /**
     * Forces an immediate backup refresh (bypassing the interval throttle, still hash-compared).
     *
     * <p><b>Call this straight after erasing a player</b> so the anonymisation reaches the backup
     * file in the same request rather than at the next periodic pass — the backup is a second copy
     * of the same personal data and POPIA/GDPR erasure has to cover it.
     *
     * <p>The write is still skipped when the encoded content is unchanged, but a forced call does
     * re-encode the store, so a bulk operation (the retention sweep erasing many players in one go)
     * pays one encode + write per player per store. That is deliberate: correctness of the erasure
     * outranks the latency of an operator-triggered sweep.
     */
    public static <T extends SavedData> void backupNow(ServerLevel level, SavedDataType<T> type,
            T instance, String name) {
        if (level == null || type == null || instance == null || name == null || name.isBlank()) {
            throw new IllegalArgumentException("SavedData backup arguments must be present");
        }
        maybeBackup(level, type, instance, name, true);
    }

    // --- Recovery -----------------------------------------------------------

    private static <T extends SavedData> T recover(ServerLevel level, SavedDataType<T> type,
            Supplier<T> fallback, String name, Exception failure) {
        T restored = readBackup(level, type, name, failure);
        boolean fromBackup = restored != null;
        T instance = restored;
        if (instance == null) {
            instance = fallback.get();
            if (instance == null) {
                throw new IllegalStateException("SavedData fallback returned null for " + name);
            }
        }
        try {
            // Install into the storage cache (stops the per-tick re-read of the bad file) and mark
            // dirty so the next level save replaces the corrupt primary with a clean one.
            level.getDataStorage().set(type, instance);
            instance.setDirty();
        } catch (Exception installationFailure) {
            if (failure != null) {
                failure.addSuppressed(installationFailure);
            } else {
                NerolandCoreCommon.LOGGER.warn(
                        "[Neroland Core] Could not install the recovered saved data '{}'.",
                        name, installationFailure);
            }
        }
        NerolandCoreCommon.LOGGER.warn(
                "[Neroland Core] Recovered saved data '{}' in {}: {}. "
                        + "A clean file will be rewritten at the next save.",
                name, level.dimension(),
                fromBackup ? "restored the last-known-good backup" : "started with a fresh store",
                failure);
        NerolandCoreTelemetry.breadcrumb("saved_data_recovery", name);
        if (failure != null) {
            NerolandCoreTelemetry.captureHandledException(failure, "saved_data_recovery",
                    name + (fromBackup ? "|backup_restored" : "|fresh_start"));
        }
        return instance;
    }

    private static <T extends SavedData> T readBackup(ServerLevel level, SavedDataType<T> type,
            String name, Exception primaryFailure) {
        try {
            Path file = backupFile(level, name);
            if (!Files.isRegularFile(file)) {
                return null;
            }
            CompoundTag tag = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            Optional<T> parsed = tag.read("data", type.codec());
            return parsed.orElse(null);
        } catch (Exception backupFailure) {
            if (primaryFailure != null) {
                primaryFailure.addSuppressed(backupFailure); // backup unreadable too (same failing disk?)
            }
            return null;
        }
    }

    // --- Backup writing -----------------------------------------------------

    private static <T extends SavedData> void maybeBackup(ServerLevel level, SavedDataType<T> type,
            T instance, String name, boolean force) {
        BackupState state = BACKUPS.computeIfAbsent(new BackupKey(level.dimension(), name),
                ignored -> new BackupState());
        long now = System.currentTimeMillis();
        if (!force) {
            long interval = instance.isDirty() ? DIRTY_INTERVAL_MS : IDLE_INTERVAL_MS;
            if (state.lastAttemptMs != 0L && now - state.lastAttemptMs < interval) {
                return;
            }
        }
        state.lastAttemptMs = now;
        try {
            Path file = backupFile(level, name);
            CompoundTag tag = new CompoundTag();
            tag.store("data", type.codec(), instance);
            int hash = tag.hashCode();
            if (state.written && state.lastWrittenHash == hash && Files.isRegularFile(file)) {
                return; // unchanged since the last written backup, and that backup is still there
            }
            Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            NbtIo.writeCompressed(tag, tmp);
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
            state.lastWrittenHash = hash;
            state.written = true;
        } catch (Exception backupFailure) {
            // Best-effort: a backup failure must never affect gameplay, and on a genuinely failing
            // disk this would fire repeatedly — keep it at debug.
            NerolandCoreCommon.LOGGER.debug("[Neroland Core] Could not write saved-data backup '{}'",
                    name, backupFailure);
        }
    }

    /** One store in one dimension. {@code ResourceKey} hashes by identity, so this is cheap. */
    private record BackupKey(ResourceKey<Level> dimension, String name) {
    }

    /** Mutable bookkeeping for one {@link BackupKey}; only ever touched under that key's entry. */
    private static final class BackupState {
        private volatile long lastAttemptMs;
        private volatile int lastWrittenHash;
        private volatile boolean written;
    }

    /** {@code <dimension folder>/data/<namespace>_<path>_backup.dat} — colon-free for Windows. */
    private static Path backupFile(ServerLevel level, String name) {
        Path dimensionRoot = DimensionType.getStorageFolder(
                level.dimension(), level.getServer().getWorldPath(LevelResource.ROOT));
        String fileName = name.replace(':', '_').replace('/', '_') + "_backup.dat";
        return dimensionRoot.resolve("data").resolve(fileName);
    }
}
