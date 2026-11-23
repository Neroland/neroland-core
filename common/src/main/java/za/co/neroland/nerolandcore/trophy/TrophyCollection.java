package za.co.neroland.nerolandcore.trophy;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;

/**
 * A named set of trophies, loaded from {@code data/<namespace>/neroland_trophy_collections/<path>.json}.
 * Completing every member unlocks the cabinet {@code trim} (a cosmetic id the trophy provider
 * understands; Core does not know the trim table) and, if {@code trophy} is present, awards that trophy.
 *
 * <p>After the load-time skip rules, {@code members} holds only loaded trophies (members skipped for an
 * absent mod or a missing icon are dropped quietly) and {@code trophy} is present only if it names a
 * loaded trophy. A collection left with fewer than two members is not loaded. A collection may not share
 * its id with a trophy.
 *
 * <p>Read collections through the accessors. Do not construct or deconstruct this record outside Core:
 * components may be added in a later minor version.
 *
 * @since 1.20.0
 */
public record TrophyCollection(Identifier id, String title, String description, Identifier icon,
        List<Identifier> members, String trim, Optional<Identifier> trophy, List<String> requiresMods, int schema) {

    /** The file body: everything but the id, with the defaults still unresolved. */
    public record Data(Identifier icon, Optional<String> title, Optional<String> description,
            List<Identifier> members, String trim, Optional<Identifier> trophy, List<String> requiresMods,
            int schema) {
    }

    /** Decodes a collection file. Unknown top-level keys, of any shape, are ignored. */
    public static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TrophyCodecs.optionalField(TrophyCodecs.POSITIVE, "schema", TrophyDefinition.SCHEMA).forGetter(Data::schema),
            TrophyCodecs.ID.fieldOf("icon").forGetter(Data::icon),
            TrophyCodecs.optionalField(TrophyCodecs.LANG_KEY_CODEC, "title").forGetter(Data::title),
            TrophyCodecs.optionalField(TrophyCodecs.LANG_KEY_CODEC, "description").forGetter(Data::description),
            TrophyCodecs.ID.listOf().fieldOf("members").forGetter(Data::members),
            TrophyCodecs.TRIM_CODEC.fieldOf("trim").forGetter(Data::trim),
            TrophyCodecs.optionalField(TrophyCodecs.ID, "trophy").forGetter(Data::trophy),
            TrophyCodecs.optionalField(TrophyCodecs.MOD_IDS, "requires_mod", List.of()).forGetter(Data::requiresMods))
            .apply(instance, (schema, icon, title, description, members, trim, trophy, requiresMods) ->
                    new Data(icon, title, description, members, trim, trophy, requiresMods, schema)));

    public TrophyCollection {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(trim, "trim");
        Objects.requireNonNull(trophy, "trophy");
        members = List.copyOf(members);
        requiresMods = List.copyOf(requiresMods);
        if (!TrophyCodecs.isValidResolvedKey(title) || !TrophyCodecs.isValidResolvedKey(description)) {
            throw new IllegalArgumentException("title and description must be lang keys");
        }
    }

    /**
     * Builds a collection from its id, file body, resolved members and resolved completion trophy, filling
     * the defaults: title {@code collection.<ns>.<path>.title}, description {@code collection.<ns>.<path>.desc}.
     */
    public static TrophyCollection of(Identifier id, Data data, List<Identifier> members, Optional<Identifier> trophy) {
        return new TrophyCollection(id,
                data.title().orElseGet(() -> TrophyDefinition.defaultKey("collection", id, "title")),
                data.description().orElseGet(() -> TrophyDefinition.defaultKey("collection", id, "desc")),
                data.icon(), members, data.trim(), trophy, data.requiresMods(), data.schema());
    }

    /** Whether {@code member} is one of this collection's trophies. */
    public boolean contains(Identifier member) {
        return members.contains(member);
    }
}
