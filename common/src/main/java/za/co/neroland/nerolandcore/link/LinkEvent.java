package za.co.neroland.nerolandcore.link;

import java.util.UUID;

import com.google.gson.JsonObject;

/**
 * A live event published by a module onto the {@link LinkEventBus}. The bridge turns
 * these into WebSocket deltas for subscribed clients and into notification candidates
 * for closed apps — events are just "deltas that matter".
 *
 * <p><b>Privacy (POPIA/GDPR).</b> When {@code playerId} is set, the event is private to
 * that player and the bridge routes it only to that player's sessions. When
 * {@code playerId} is {@code null} the event is a server-wide broadcast (e.g. an event
 * schedule change) and its {@code payload} must contain no personal identifiers beyond
 * what the game already makes public.
 *
 * @param moduleId  the publishing module (e.g. {@code "nerologistics"})
 * @param topic     the delta topic, mirroring a snapshot section (e.g.
 *                  {@code "drones"}); the bridge namespaces it as
 *                  {@code module.topic} on the wire
 * @param playerId  the owning player, or {@code null} for a server-wide broadcast
 * @param payload   the delta payload; never null (may be an empty object)
 * @param timestamp epoch millis (UTC) at which the event occurred
 */
public record LinkEvent(String moduleId,
                        String topic,
                        UUID playerId,
                        JsonObject payload,
                        long timestamp) {

    /** A player-scoped event stamped with the current time. */
    public static LinkEvent forPlayer(String moduleId, String topic, UUID playerId, JsonObject payload) {
        return new LinkEvent(moduleId, topic, playerId, payload, System.currentTimeMillis());
    }

    /** A server-wide broadcast event (no owning player) stamped with the current time. */
    public static LinkEvent broadcast(String moduleId, String topic, JsonObject payload) {
        return new LinkEvent(moduleId, topic, null, payload, System.currentTimeMillis());
    }

    /** Whether this event is a server-wide broadcast (no owning player). */
    public boolean isBroadcast() {
        return playerId == null;
    }
}
