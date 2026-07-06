package za.co.neroland.nerolandcore.link;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonObject;

/**
 * A module's read-side contribution to NeroLink: it answers "what does this player
 * see?" for one or more named sections. Mods register an implementation with
 * {@link NeroLinkRegistry#registerSnapshotProvider(LinkSnapshotProvider)} during common
 * init; the bridge caches and serves the returned snapshots to any number of app
 * clients without recomputation.
 *
 * <p><b>Own-data-only contract (POPIA/GDPR).</b> Everything {@link #snapshot} returns
 * must already be scoped to {@code playerId} — the caller performs no further
 * filtering, so authorisation lives here at the seam. A provider must never leak another
 * player's private data; only explicitly public/aggregate data (server events, public
 * faction standing) may appear un-scoped, and it must carry no personal identifiers
 * beyond what the game already makes public.
 *
 * <p>Implementations are called on the server thread and should be cheap and
 * side-effect-free (reads only); the bridge governs cadence and caching.
 */
public interface LinkSnapshotProvider {

    /** The module this provider serves (matches its {@link LinkModuleInfo#moduleId()}). */
    String moduleId();

    /** The data-schema revision, bumped when this module's snapshot shape changes. */
    int schemaVersion();

    /** The section names this provider serves (e.g. {@code ["energy", "storage"]}). */
    List<String> sections();

    /**
     * Produce the player-scoped snapshot for one section.
     *
     * @param playerId the authenticated player; all returned data must be scoped to them
     * @param section  one of {@link #sections()}
     * @param params   optional query parameters (e.g. {@code tab}, {@code q}, {@code cursor});
     *                 never null (may be empty)
     * @return a JSON object of already-player-scoped data, or an empty object if the
     *         section has nothing for this player. Should not return {@code null}.
     */
    JsonObject snapshot(UUID playerId, String section, Map<String, String> params);
}
