package za.co.neroland.nerolandcore.data;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Argument guards for the shared saved-data recovery helper.
 *
 * <p>The interesting behaviour — recovering a corrupt {@code .dat} from the last-known-good backup
 * — needs a live {@link net.minecraft.server.level.ServerLevel} and its data storage, so it is
 * covered by runtime verification rather than here. What <em>is</em> worth pinning in a plain-JVM
 * test is that a malformed call fails fast at the call site instead of NPE-ing somewhere inside the
 * recovery ladder mid-tick, which is the failure mode the helper exists to prevent.
 */
class SavedDataRecoveryTest {

    @Test
    void aMissingLevelIsRejectedAtTheCallSite() {
        assertThrows(IllegalArgumentException.class, () -> SavedDataRecovery.get(
                null, PlayerActivity.TYPE, PlayerActivity::new, "nerolandcore:player_activity"));
    }

    @Test
    void aMissingBackupTargetIsRejectedAtTheCallSite() {
        assertThrows(IllegalArgumentException.class, () -> SavedDataRecovery.backupNow(
                null, PlayerActivity.TYPE, new PlayerActivity(), "nerolandcore:player_activity"));
    }

    @Test
    void theRegistryAwareBackupOverloadRejectsAMissingLevel() {
        assertThrows(IllegalArgumentException.class, () -> SavedDataRecovery.backupNow(
                null, PlayerActivity.TYPE, new PlayerActivity(), "nerolandcore:player_activity", null));
    }

    @Test
    void forcedBackupFailuresReachTheCallerWhilePeriodicFailuresRemainNonfatal() {
        Exception cause = new java.io.IOException("simulated backup failure");
        var failure = assertThrows(IllegalStateException.class,
                () -> SavedDataRecovery.reportBackupFailure("test", true, cause));
        assertSame(cause, failure.getCause());
        assertDoesNotThrow(() -> SavedDataRecovery.reportBackupFailure("test", false, cause));
    }

    @Test
    void anUnreadablePrimaryIsMovedAsideSoVanillaNeverReadsItAgain(@TempDir Path dir) throws Exception {
        Path primary = dir.resolve("store.dat");
        Files.write(primary, new byte[] {0x1f, (byte) 0x8b, 0x08, 0x00, 't', 'r', 'u', 'n', 'c'});
        assertTrue(SavedDataRecovery.quarantine(primary, "test"));
        assertFalse(Files.exists(primary));
        assertTrue(Files.isRegularFile(dir.resolve("store.dat.corrupt")));

        // A later corruption replaces the single quarantined copy instead of piling up copies.
        Files.write(primary, new byte[] {0x01});
        assertTrue(SavedDataRecovery.quarantine(primary, "test"));
        assertEquals(1L, Files.size(SavedDataRecovery.quarantineFile(primary)));
    }

    @Test
    void aMissingPrimaryIsNotQuarantined(@TempDir Path dir) {
        assertFalse(SavedDataRecovery.quarantine(dir.resolve("absent.dat"), "test"));
    }
}
