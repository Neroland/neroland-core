package za.co.neroland.nerolandcore.reputation;

import java.util.UUID;

import net.minecraft.resources.Identifier;

/**
 * The player↔faction reputation storage contract. <b>Core defines this but stores
 * nothing</b> — NeroFactions implements it and registers via
 * {@link ReputationApi#setProvider(ReputationProvider)}. Any mod (shops giving
 * faction discounts, quests granting standing) reads/writes through
 * {@link ReputationApi}.
 *
 * <p>Reputation is an integer keyed by {@code (player UUID, faction id)}; its scale
 * and clamping are the provider's concern.
 */
public interface ReputationProvider {

    /** The player's standing with {@code faction} (0 if none recorded). */
    int getReputation(UUID player, Identifier faction);

    /** Set the player's standing with {@code faction}. */
    void setReputation(UUID player, Identifier faction, int value);

    /** Adjust standing by {@code delta}. @return the new value. */
    default int adjust(UUID player, Identifier faction, int delta) {
        int updated = getReputation(player, faction) + delta;
        setReputation(player, faction, updated);
        return updated;
    }

    /**
     * POPIA/GDPR erasure hook: drop every standing stored for {@code player}.
     *
     * @implSpec <b>Any provider that persists reputation MUST override this.</b>
     *     {@link za.co.neroland.nerolandcore.data.PlayerDataErasure} purges reputation by calling
     *     {@code ReputationApi.provider().forgetPlayer(uuid)} — this method is the <em>only</em>
     *     route an erasure request has into reputation storage. The default body is a no-op purely
     *     because making it abstract would break the frozen-between-majors API; inheriting it means
     *     a "successful" erasure leaves the player's standings on disk. Implement it even if storage
     *     is in-memory only (an explicit empty override documents the intent and silences the
     *     warning the default body logs).
     */
    default void forgetPlayer(UUID player) {
        za.co.neroland.nerolandcore.data.ErasureWarnings.warnDefaultForgetPlayer("ReputationProvider", this);
    }
}
