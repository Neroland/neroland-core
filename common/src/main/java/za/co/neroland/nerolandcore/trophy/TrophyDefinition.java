package za.co.neroland.nerolandcore.trophy;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;

/**
 * One trophy, loaded from {@code data/<namespace>/neroland_trophies/<path>.json}; its id is
 * {@code <namespace>:<path>}.
 *
 * <ul>
 *   <li>{@code trigger} is optional: a trophy without one is earned only through
 *       {@link TrophyApi#award} (or as a collection's completion trophy).</li>
 *   <li>{@code title} and {@code description} are lang keys, never display text.</li>
 *   <li>{@code requiresMods} lists the mods the trophy belongs to; when any is absent the file is
 *       skipped quietly.</li>
 *   <li>{@code category} is the value written in the file; {@link #bookCategory()} resolves the default.</li>
 *   <li>{@code order} sorts trophies within a category (then by id); default 0.</li>
 * </ul>
 *
 * <p>Read definitions through the accessors. Do not construct or deconstruct this record outside
 * Core: components may be added in a later minor version.
 *
 * @since 1.20.0
 */
public record TrophyDefinition(Identifier id, Identifier icon, TrophyTier tier, Optional<TrophyTrigger> trigger,
        boolean hidden, String title, String description, List<String> requiresMods, Optional<String> category,
        Optional<TrophyReward> reward, int order, int schema) {

    /** The schema version this Core writes and fully understands. */
    public static final int SCHEMA = 1;

    /** The file body: everything but the id, with the defaults still unresolved. */
    public record Data(Identifier icon, TrophyTier tier, Optional<TrophyTrigger> trigger, boolean hidden,
            Optional<String> title, Optional<String> description, List<String> requiresMods,
            Optional<String> category, Optional<TrophyReward> reward, int order, int schema) {
    }

    /** Decodes a definition file. Unknown top-level keys, of any shape, are ignored. */
    public static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TrophyCodecs.optionalField(TrophyCodecs.POSITIVE, "schema", SCHEMA).forGetter(Data::schema),
            TrophyCodecs.ID.fieldOf("icon").forGetter(Data::icon),
            TrophyTier.CODEC.fieldOf("tier").forGetter(Data::tier),
            TrophyCodecs.optionalField(TrophyTrigger.CODEC, "trigger").forGetter(Data::trigger),
            TrophyCodecs.optionalField(Codec.BOOL, "hidden", false).forGetter(Data::hidden),
            TrophyCodecs.optionalField(TrophyCodecs.LANG_KEY_CODEC, "title").forGetter(Data::title),
            TrophyCodecs.optionalField(TrophyCodecs.LANG_KEY_CODEC, "description").forGetter(Data::description),
            TrophyCodecs.optionalField(TrophyCodecs.MOD_IDS, "requires_mod", List.of()).forGetter(Data::requiresMods),
            TrophyCodecs.optionalField(TrophyCodecs.MOD_ID, "category").forGetter(Data::category),
            TrophyCodecs.optionalField(TrophyReward.CODEC, "reward").forGetter(Data::reward),
            TrophyCodecs.optionalField(TrophyCodecs.ANY_INT, "order", 0).forGetter(Data::order))
            .apply(instance, (schema, icon, tier, trigger, hidden, title, description, requiresMods, category,
                    reward, order) -> new Data(icon, tier, trigger, hidden, title, description, requiresMods,
                    category, reward, order, schema)));

    public TrophyDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(tier, "tier");
        Objects.requireNonNull(trigger, "trigger");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(reward, "reward");
        requiresMods = List.copyOf(requiresMods);
        if (!TrophyCodecs.isValidResolvedKey(title) || !TrophyCodecs.isValidResolvedKey(description)) {
            throw new IllegalArgumentException("title and description must be lang keys");
        }
    }

    /**
     * Builds a definition from its id and file body, filling the defaults: title
     * {@code trophy.<ns>.<path>.title}, description {@code trophy.<ns>.<path>.desc} ({@code /} in the path
     * becomes {@code .}).
     */
    public static TrophyDefinition of(Identifier id, Data data) {
        return new TrophyDefinition(id, data.icon(), data.tier(), data.trigger(), data.hidden(),
                data.title().orElseGet(() -> defaultKey("trophy", id, "title")),
                data.description().orElseGet(() -> defaultKey("trophy", id, "desc")),
                data.requiresMods(), data.category(), data.reward(), data.order(), data.schema());
    }

    /** The file body that decodes back to this definition. */
    public Data toData() {
        return new Data(icon, tier, trigger, hidden, Optional.of(title), Optional.of(description), requiresMods,
                category, reward, order, schema);
    }

    /** Whether the trophy is earned only through {@link TrophyApi#award} (it has no trigger). */
    public boolean awardOnly() {
        return trigger.isEmpty();
    }

    /** The Trophy Book tab: {@code category}, else the first {@code requires_mod}, else the id's namespace. */
    public String bookCategory() {
        return category.orElseGet(() -> requiresMods.isEmpty() ? id.getNamespace() : requiresMods.get(0));
    }

    static String defaultKey(String prefix, Identifier id, String suffix) {
        return prefix + "." + id.getNamespace() + "." + id.getPath().replace('/', '.') + "." + suffix;
    }
}
