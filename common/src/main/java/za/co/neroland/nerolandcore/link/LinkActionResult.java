package za.co.neroland.nerolandcore.link;

import com.google.gson.JsonObject;

/**
 * The outcome of a {@link LinkActionHandler#execute(java.util.UUID, String, JsonObject)}
 * call. Either {@linkplain #ok(JsonObject) success} — carrying the resulting state the
 * app renders — or a {@linkplain #error(Error, String) failure} carrying a stable
 * {@link Error} code plus a human-readable message.
 *
 * <p>The bridge maps the {@link Error} code straight onto the REST error envelope's
 * {@code error.code}, so codes here mirror the API spec's shared action error set.
 *
 * @param ok      whether the action succeeded
 * @param error   the failure code when {@code !ok}, otherwise {@code null}
 * @param message a human-readable detail (safe to surface in the app); never null
 * @param state   the resulting state on success, or {@code null} on failure
 */
public record LinkActionResult(boolean ok, Error error, String message, JsonObject state) {

    /**
     * Stable action error codes, mirroring the shared action-error set in the NeroLink
     * API spec. The bridge relays the {@link #name()} of the code to the app.
     */
    public enum Error {
        /** The player does not own the targeted object (block, task, machine, …). */
        NOT_OWNER,
        /** A progression gate required for this action is not open for the player. */
        GATE_LOCKED,
        /** The parameters failed validation (missing/out-of-range/unknown values). */
        VALIDATION,
        /** This action id is disabled by server config. */
        ACTION_DISABLED,
        /** This action requires the player to be online and they are not. */
        PLAYER_OFFLINE_REQUIRED,
        /** An unexpected server-side error occurred. */
        INTERNAL
    }

    /** A success result carrying the resulting state (may be an empty object). */
    public static LinkActionResult ok(JsonObject state) {
        return new LinkActionResult(true, null, "", state == null ? new JsonObject() : state);
    }

    /** A failure result with a stable {@link Error} code and a human-readable message. */
    public static LinkActionResult error(Error error, String message) {
        return new LinkActionResult(false, error, message == null ? "" : message, null);
    }
}
