package za.co.neroland.nerolandcore.link;

/**
 * One per-player alert surfaced through NeroLink's {@code core/alerts} section — a
 * "something needs your attention" flag raised by a module (low power, a completed
 * craft, a lost companion) and shown in the app until acknowledged or snoozed.
 *
 * <p><b>Privacy (POPIA/GDPR).</b> An alert is stored under its owning player's UUID
 * (in {@link LinkAlerts}) and holds only gameplay metadata — a module id, a severity, a
 * short text, timestamps and flags. The {@code text} must describe game state, never
 * personal data. Alerts are purged for a player by the shared erasure hook.
 *
 * @param id        a stable id for this alert (used by {@code core/ack_alert})
 * @param moduleId  the module that raised it (e.g. {@code "core"}, {@code "nerologistics"})
 * @param severity  the severity level
 * @param text      a short, non-personal description of the game state
 * @param createdAt epoch millis (UTC) at which the alert was raised
 * @param acked     whether the player has acknowledged it
 * @param snoozedUntil epoch millis (UTC) until which the alert is snoozed, or {@code 0}
 *                     if not snoozed
 */
public record LinkAlert(String id,
                        String moduleId,
                        Severity severity,
                        String text,
                        long createdAt,
                        boolean acked,
                        long snoozedUntil) {

    /** Alert severity, ordered least-to-most urgent. */
    public enum Severity {
        INFO,
        WARN,
        CRITICAL
    }

    /** A freshly raised, un-acked, un-snoozed alert stamped with the current time. */
    public static LinkAlert raise(String id, String moduleId, Severity severity, String text) {
        return new LinkAlert(id, moduleId, severity, text, System.currentTimeMillis(), false, 0L);
    }

    /** A copy of this alert marked acknowledged. */
    public LinkAlert withAcked() {
        return new LinkAlert(id, moduleId, severity, text, createdAt, true, snoozedUntil);
    }

    /** A copy of this alert snoozed until {@code until} (epoch millis UTC). */
    public LinkAlert withSnoozedUntil(long until) {
        return new LinkAlert(id, moduleId, severity, text, createdAt, acked, until);
    }

    /** Whether this alert is currently snoozed at {@code now} (epoch millis UTC). */
    public boolean isSnoozedAt(long now) {
        return snoozedUntil > now;
    }
}
