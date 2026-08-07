package za.co.neroland.nerolandcore.data;

import java.util.function.Supplier;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.telemetry.NerolandCoreTelemetry;

/**
 * Shared crash-safe accessor for ecosystem {@link SavedData} stores.
 *
 * <p>A corrupt or unreadable vanilla saved-data file must degrade to a fresh dirty store rather
 * than repeatedly crashing a server tick. Recovery metadata is deliberately non-identifying: only
 * the stable store label and dimension are logged or attached to telemetry.
 */
public final class SavedDataRecovery {
    private SavedDataRecovery() {
    }

    /**
     * Loads a store through vanilla storage, recovering from exceptions and unexpected nulls.
     * The replacement is inserted into the storage cache and marked dirty so the next save writes
     * a clean file.
     */
    public static <T extends SavedData> T get(ServerLevel level, SavedDataType<T> type,
            Supplier<T> fallback, String name) {
        if (level == null || type == null || fallback == null || name == null || name.isBlank()) {
            throw new IllegalArgumentException("SavedData recovery arguments must be present");
        }
        try {
            T loaded = level.getDataStorage().computeIfAbsent(type);
            if (loaded != null) {
                return loaded;
            }
        } catch (Exception failure) {
            return recover(level, type, fallback, name, failure);
        }
        return recover(level, type, fallback, name, null);
    }

    private static <T extends SavedData> T recover(ServerLevel level, SavedDataType<T> type,
            Supplier<T> fallback, String name, Exception failure) {
        T fresh = fallback.get();
        if (fresh == null) {
            throw new IllegalStateException("SavedData fallback returned null for " + name);
        }
        try {
            level.getDataStorage().set(type, fresh);
            fresh.setDirty();
        } catch (Exception installationFailure) {
            if (failure != null) {
                failure.addSuppressed(installationFailure);
            }
        }
        NerolandCoreCommon.LOGGER.warn("[Neroland Core] Recovered saved data '{}' in {} with a fresh store.",
                name, level.dimension(), failure);
        NerolandCoreTelemetry.breadcrumb("saved_data_recovery", name);
        if (failure != null) {
            NerolandCoreTelemetry.captureHandledException(failure, "saved_data_recovery", name);
        }
        return fresh;
    }
}
