package za.co.neroland.nerolandcore.link;

import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;

/**
 * A module's write-side contribution to NeroLink: it answers "what may this player
 * do?" by executing a bounded set of safe actions. Mods register an implementation with
 * {@link NeroLinkRegistry#registerActionHandler(LinkActionHandler)} during common init.
 *
 * <p><b>Server-authoritative.</b> The bridge validates the token, module presence,
 * action-enabled config, rate limit and {@link #allowOffline(String)} before calling
 * {@link #execute}; the handler itself must still re-check ownership, progression gates,
 * claims and mod-specific rules — the app holds no authority. {@link #execute} runs
 * <em>on the server thread</em>, so it may touch world/block-entity state directly.
 *
 * <p>Actions should be idempotent where possible and always answer with the resulting
 * state via {@link LinkActionResult#ok(JsonObject)}, or a stable
 * {@link LinkActionResult.Error} code on refusal.
 */
public interface LinkActionHandler {

    /** The module this handler serves (matches its {@link LinkModuleInfo#moduleId()}). */
    String moduleId();

    /** The action ids this handler accepts (e.g. {@code ["ack_alert"]}). */
    List<String> actionIds();

    /**
     * Execute one action on the server thread. Implementations must re-validate
     * ownership, gates and mod rules; a compromised app can do no more than this player
     * could do in-game.
     *
     * @param playerId the authenticated player on whose behalf the action runs
     * @param actionId one of {@link #actionIds()}
     * @param params   the action's JSON parameter object; never null (may be empty)
     * @return the outcome — success with resulting state, or a stable error code
     */
    LinkActionResult execute(UUID playerId, String actionId, JsonObject params);

    /**
     * Whether {@code actionId} may run while the player is offline (e.g. claim a quest
     * reward, reassign a drone). Defaults to {@code false} — online-only. Server config
     * can additionally force all actions online-only regardless of this flag.
     */
    default boolean allowOffline(String actionId) {
        return false;
    }
}
