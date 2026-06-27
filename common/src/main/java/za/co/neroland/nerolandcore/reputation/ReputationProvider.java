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

    /** POPIA/GDPR erasure hook: drop everything stored for a player. No-op by default. */
    default void forgetPlayer(UUID player) {
    }
}
