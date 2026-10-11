package za.co.neroland.nerolandcore.event;

import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

/**
 * Validation for the caller-attribution strings Core APIs accept (added in Core 1.15.0): the
 * {@code sourceModId} naming the mod behind a currency movement or a threshold crossing, and the
 * short machine-readable {@code reason} code on a currency movement.
 *
 * <p>Both are deliberately narrow — lowercase ASCII only — so they are safe to store, aggregate
 * and show in a ledger, and can never smuggle personal data (a player name, a UUID with
 * upper-case hex, free text) into an audit trail.
 */
public final class SourceIds {

    /** A source mod id: {@code [a-z0-9_.-]{1,64}} (a mod id, e.g. {@code neroquests}). */
    public static final Pattern SOURCE_MOD_ID = Pattern.compile("[a-z0-9_.-]{1,64}");

    /** A reason code: {@code [a-z0-9_]{1,32}} (e.g. {@code quest_reward}, {@code shop_purchase}). */
    public static final Pattern REASON = Pattern.compile("[a-z0-9_]{1,32}");

    private SourceIds() {
    }

    /** Whether {@code sourceModId} is a non-null string matching {@link #SOURCE_MOD_ID}. */
    public static boolean isValidSourceModId(@Nullable String sourceModId) {
        return sourceModId != null && SOURCE_MOD_ID.matcher(sourceModId).matches();
    }

    /** Whether {@code reason} is a non-null string matching {@link #REASON}. */
    public static boolean isValidReason(@Nullable String reason) {
        return reason != null && REASON.matcher(reason).matches();
    }
}
