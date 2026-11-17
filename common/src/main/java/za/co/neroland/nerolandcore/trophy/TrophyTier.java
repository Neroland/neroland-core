package za.co.neroland.nerolandcore.trophy;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

/**
 * The four trophy tiers, lowest first. Serialized by name ({@code "bronze"}, {@code "silver"},
 * {@code "gold"}, {@code "nero"}). {@link #rank()} orders them for comparison.
 *
 * @since 1.20.0
 */
public enum TrophyTier implements StringRepresentable {
    BRONZE("bronze", 10),
    SILVER("silver", 20),
    GOLD("gold", 30),
    NERO("nero", 40);

    public static final Codec<TrophyTier> CODEC = StringRepresentable.fromEnum(TrophyTier::values);

    private final String serializedName;
    private final int rank;

    TrophyTier(String serializedName, int rank) {
        this.serializedName = serializedName;
        this.rank = rank;
    }

    /** A stable sort key: 10 bronze, 20 silver, 30 gold, 40 nero. Higher is rarer. */
    public int rank() {
        return rank;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
