package za.co.neroland.nerolandcore.meteor;

import java.util.Locale;

import com.mojang.serialization.Codec;

/**
 * The rarity tier of a grindable meteor material. A tier decides which pool an
 * entry joins and (for the primary pool) its tier-derived base weight.
 *
 * <ul>
 *   <li>{@link #COMMON} / {@link #UNCOMMON} / {@link #RARE} feed the grinder's
 *       <b>primary</b> pool; their base weights come from the Core config table
 *       ({@code meteorTierBaseWeight*}).</li>
 *   <li>{@link #EXOTIC} is routed to the separate <b>exotic</b> bonus pool and has
 *       no tier base weight — an exotic entry must carry its own
 *       {@code weight_override} to be eligible.</li>
 * </ul>
 *
 * <p>Serialised lower-case in data files and annotation values
 * ({@code "common"}, {@code "uncommon"}, {@code "rare"}, {@code "exotic"}).
 */
public enum MeteorTier {
    COMMON,
    UNCOMMON,
    RARE,
    EXOTIC;

    /** Lower-case string codec, matching the data-file and annotation spelling. */
    public static final Codec<MeteorTier> CODEC = Codec.STRING.xmap(
            MeteorTier::byName,
            tier -> tier.name().toLowerCase(Locale.ROOT));

    /** Parse a tier name case-insensitively (e.g. {@code "uncommon"}). */
    public static MeteorTier byName(String name) {
        return MeteorTier.valueOf(name.trim().toUpperCase(Locale.ROOT));
    }

    /** Whether this tier belongs to the separate exotic bonus pool. */
    public boolean isExotic() {
        return this == EXOTIC;
    }

    /** Whether this tier belongs to the primary (common/uncommon/rare) pool. */
    public boolean isPrimary() {
        return this != EXOTIC;
    }
}
