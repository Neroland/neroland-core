package za.co.neroland.nerolandcore.trophy;

import java.util.List;
import java.util.regex.Pattern;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.resources.Identifier;

import za.co.neroland.nerolandcore.event.SourceIds;

/** Bounded field codecs shared by the trophy definition schema. */
final class TrophyCodecs {

    /** Longest accepted identifier (trophy, icon or trigger target), as text. */
    static final int MAX_ID_LENGTH = 128;
    /** A lang key, never display text. */
    static final Pattern LANG_KEY = Pattern.compile("[a-z0-9_.-]{1,128}");
    /** A collection's cabinet trim id. */
    static final Pattern TRIM = Pattern.compile("[a-z0-9_]{1,32}");

    static final Codec<Identifier> ID = Identifier.CODEC.flatXmap(TrophyCodecs::checkId, TrophyCodecs::checkId);
    static final Codec<String> LANG_KEY_CODEC = pattern(LANG_KEY, "a lang key");
    static final Codec<String> MOD_ID = pattern(SourceIds.SOURCE_MOD_ID, "a mod id");
    static final Codec<String> TRIM_CODEC = pattern(TRIM, "a trim id");
    /** Most mods one {@code requires_mod} may list. */
    static final int MAX_REQUIRED_MODS = 8;
    /** {@code requires_mod}: one mod id, or a list of 1 to {@value #MAX_REQUIRED_MODS}; all must be loaded. */
    static final Codec<List<String>> MOD_IDS = Codec.either(MOD_ID, MOD_ID.listOf()).flatXmap(
            either -> checkModList(either.map(List::of, list -> list)),
            list -> checkModList(list).map(ok -> ok.size() == 1
                    ? Either.<String, List<String>>left(ok.get(0))
                    : Either.<String, List<String>>right(ok)));
    static final Codec<Integer> POSITIVE = boundedInt(1, Integer.MAX_VALUE);
    static final Codec<Integer> ANY_INT = boundedInt(Integer.MIN_VALUE, Integer.MAX_VALUE);

    private TrophyCodecs() {
    }

    static boolean isValidLangKey(String key) {
        return LANG_KEY.matcher(key).matches();
    }

    /** A resolved lang key: a written one, or a generated default (which may be longer than 128). */
    static boolean isValidResolvedKey(String key) {
        return RESOLVED_KEY.matcher(key).matches();
    }

    private static final Pattern RESOLVED_KEY = Pattern.compile("[a-z0-9_.-]{1,192}");

    private static DataResult<List<String>> checkModList(List<String> mods) {
        return mods.isEmpty() || mods.size() > MAX_REQUIRED_MODS
                ? DataResult.error(() -> "requires_mod must list 1 to " + MAX_REQUIRED_MODS + " mods")
                : DataResult.success(List.copyOf(mods));
    }

    private static DataResult<Identifier> checkId(Identifier id) {
        return id.toString().length() <= MAX_ID_LENGTH
                ? DataResult.success(id)
                : DataResult.error(() -> "Identifier longer than " + MAX_ID_LENGTH + " characters");
    }

    private static Codec<String> pattern(Pattern pattern, String what) {
        return Codec.STRING.flatXmap(value -> check(pattern, what, value), value -> check(pattern, what, value));
    }

    private static DataResult<String> check(Pattern pattern, String what, String value) {
        return pattern.matcher(value).matches()
                ? DataResult.success(value)
                : DataResult.error(() -> "Expected " + what + " matching " + pattern.pattern());
    }

    /**
     * A whole number in {@code [min, max]}. Unlike {@code Codec.INT} it rejects fractions and values
     * outside the range instead of truncating them.
     */
    static Codec<Integer> boundedInt(long min, long max) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<Integer, T>> decode(DynamicOps<T> ops, T input) {
                return ops.getNumberValue(input).flatMap(number -> {
                    double value = number.doubleValue();
                    if (value != Math.rint(value) || value < min || value > max) {
                        return DataResult.error(() -> "Expected a whole number in [" + min + ", " + max + "]");
                    }
                    return DataResult.success(Pair.of((int) value, ops.empty()));
                });
            }

            @Override
            public <T> DataResult<T> encode(Integer value, DynamicOps<T> ops, T prefix) {
                return ops.mergeToPrimitive(prefix, ops.createInt(value));
            }

            @Override
            public String toString() {
                return "TrophyInt[" + min + ", " + max + "]";
            }
        };
    }
}
