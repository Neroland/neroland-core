package za.co.neroland.nerolandcore.trophy;

/**
 * Where an award came from. Carried on {@link TrophyEvents.Awarded} so listeners can tell a live
 * achievement from a retroactive catch-up, an operator command or a call from another mod.
 *
 * @since 1.20.0
 */
public enum AwardSource {
    /** Earned just now, by play. */
    LIVE,
    /** Found on a re-check for work already done (login, datapack reload). */
    RETROACTIVE,
    /** Granted by an operator command. */
    COMMAND,
    /** Granted by another mod through {@link TrophyApi#award}. */
    API
}
