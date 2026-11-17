package za.co.neroland.nerolandcore.trophy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * An optional per-trophy reward. {@code coins} (0 to {@value #MAX_COINS}) overrides the provider's
 * tier default. Core pays nothing: the trophy provider decides whether and how to pay.
 *
 * @since 1.20.0
 */
public record TrophyReward(int coins) {

    public static final int MAX_COINS = 10_000;

    public static final Codec<TrophyReward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TrophyCodecs.boundedInt(0, MAX_COINS).fieldOf("coins").forGetter(TrophyReward::coins))
            .apply(instance, TrophyReward::new));

    public TrophyReward {
        if (coins < 0 || coins > MAX_COINS) {
            throw new IllegalArgumentException("coins out of range");
        }
    }
}
