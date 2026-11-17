package za.co.neroland.nerolandcore.trophy;

import java.util.List;

import net.minecraft.resources.Identifier;

/**
 * What the last trophy-data load did: how many trophies and collections loaded, how many files were
 * skipped quietly (absent {@code requires_mod}, missing icon item) and which files were rejected.
 *
 * <p>An {@link Issue} names the data file and a fixed English reason token ({@link #TOO_LARGE},
 * {@link #LIMIT}, {@link #MALFORMED}, {@link #UNKNOWN_MEMBER}, {@link #ID_CLASH}), never file
 * content. At most {@value #MAX_ISSUES} issues are kept; the rest are counted in {@code issuesDropped}.
 *
 * @since 1.20.0
 */
public record TrophyLoadReport(int trophies, int collections, int skippedAbsentMod, int skippedMissingIcon,
        List<Issue> issues, int issuesDropped) {

    public static final String TOO_LARGE = "too_large";
    public static final String LIMIT = "limit";
    public static final String MALFORMED = "malformed";
    public static final String UNKNOWN_MEMBER = "unknown_member";
    public static final String ID_CLASH = "id_clash";

    /** Most issues kept; further ones are only counted in {@code issuesDropped}. */
    public static final int MAX_ISSUES = 64;

    public static final TrophyLoadReport EMPTY = new TrophyLoadReport(0, 0, 0, 0, List.of(), 0);

    /** One rejected file. {@code file} is the resource path of the data file. */
    public record Issue(Identifier file, String reason) {
    }

    public TrophyLoadReport {
        issues = List.copyOf(issues);
    }

    /** Every rejected file, kept or not. */
    public int issueCount() {
        return issues.size() + issuesDropped;
    }
}
