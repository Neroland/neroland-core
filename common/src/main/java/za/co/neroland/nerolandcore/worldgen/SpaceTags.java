package za.co.neroland.nerolandcore.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;

import org.jetbrains.annotations.Nullable;

/**
 * The shared {@code neroland:space/*} biome- and dimension-type-tag vocabulary (added in
 * Core 1.10.0) — the ecosystem's answer to "where is off-Earth?" without any mod having to
 * hard-depend on the mod that ships the planets.
 *
 * <p>Core defines the tag <b>ids</b> as a frozen contract and ships base membership JSON with
 * every entry marked {@code "required": false}, so a tag resolves to the subset of planets
 * that are actually installed. Downstream mods (NeroCreatures spawn rules, NeroRuins structure
 * placement, NeroAgriculture growth rules) read the tags; the mods that own the planets
 * (Nerospace today, Ad Astra via optional entries) supply the members. Data packs may retune
 * membership with no code.
 *
 * <h2>Empty tags are normal</h2>
 * On a Core-only server every one of these tags is <b>empty</b>. That is a supported, expected
 * state and never an error: a consumer must read an empty tag as "no such place exists here"
 * (⇒ no spawns, no placement, no bonus) and must never assume a member exists, never call
 * {@code getRandomElement(...).orElseThrow()}, and never crash. Vanilla Earth stays untouched
 * by design.
 *
 * <h2>Tags</h2>
 * <table>
 *   <caption>The {@code neroland:space/*} vocabulary</caption>
 *   <tr><th>Tag</th><th>Meaning</th></tr>
 *   <tr><td>{@code neroland:space/dark_biomes}</td>
 *       <td>perpetually dim, sunless or ash-choked surfaces — low-light ambush territory</td></tr>
 *   <tr><td>{@code neroland:space/moon_biomes}</td>
 *       <td>low-/micro-gravity satellite-like regolith surfaces</td></tr>
 *   <tr><td>{@code neroland:space/crystalline_biomes}</td>
 *       <td>crystal, quartz and ice-lattice terrain</td></tr>
 *   <tr><td>{@code neroland:space/asteroid_biomes}</td>
 *       <td>loose, airless rubble fields and orbital debris</td></tr>
 *   <tr><td>{@code neroland:space/planet_biomes}</td>
 *       <td>umbrella: every off-Earth biome, including all of the above</td></tr>
 *   <tr><td>{@code neroland:space/dimensions}</td>
 *       <td>dimension <i>types</i> that are off-Earth (coarse guard; the biome tags are the
 *           precise lever)</td></tr>
 * </table>
 *
 * <p><b>No player data crosses this API.</b> These are world-shape groupings only — nothing is
 * player-attributable, so nothing routes through Core's per-player erasure hook.
 */
public final class SpaceTags {

    /** {@code neroland:space/dark_biomes} — perpetually dim / sunless surfaces. */
    public static final TagKey<Biome> DARK_BIOMES = biomeTag("space/dark_biomes");

    /** {@code neroland:space/moon_biomes} — low-/micro-gravity regolith surfaces. */
    public static final TagKey<Biome> MOON_BIOMES = biomeTag("space/moon_biomes");

    /** {@code neroland:space/crystalline_biomes} — crystal / quartz / ice-lattice terrain. */
    public static final TagKey<Biome> CRYSTALLINE_BIOMES = biomeTag("space/crystalline_biomes");

    /** {@code neroland:space/asteroid_biomes} — airless rubble fields and orbital debris. */
    public static final TagKey<Biome> ASTEROID_BIOMES = biomeTag("space/asteroid_biomes");

    /**
     * {@code neroland:space/planet_biomes} — the umbrella tag: every off-Earth biome. Core's
     * shipped file includes the four themed tags above by reference, so anything added to a
     * themed tag is automatically a planet biome too.
     */
    public static final TagKey<Biome> PLANET_BIOMES = biomeTag("space/planet_biomes");

    /**
     * {@code neroland:space/dimensions} — off-Earth dimension <b>types</b>. Note this is the
     * dimension <i>type</i> registry, not the dimension registry: a planet that reuses
     * {@code minecraft:overworld} as its type cannot be tagged here without dragging the
     * Overworld in with it, so treat this as a coarse guard and use the biome tags for
     * per-place decisions.
     */
    public static final TagKey<DimensionType> SPACE_DIMENSIONS =
            TagKey.create(Registries.DIMENSION_TYPE, id("space/dimensions"));

    private SpaceTags() {
    }

    /**
     * Whether {@code level} runs on an off-Earth dimension type
     * ({@link #SPACE_DIMENSIONS}). {@code false} for a {@code null} level and for every
     * vanilla dimension.
     */
    public static boolean isSpace(@Nullable ServerLevel level) {
        return level != null && level.dimensionTypeRegistration().is(SPACE_DIMENSIONS);
    }

    /**
     * Null-safe tag test for a biome holder — the shape spawn predicates and structure checks
     * want. {@code false} for a {@code null} holder and for an empty tag.
     */
    public static boolean biomeIn(@Nullable Holder<Biome> biome, TagKey<Biome> tag) {
        return biome != null && biome.is(tag);
    }

    /** Convenience: {@link #biomeIn(Holder, TagKey)} for the biome at {@code pos}. */
    public static boolean biomeIn(@Nullable LevelReader level, BlockPos pos, TagKey<Biome> tag) {
        return level != null && biomeIn(level.getBiome(pos), tag);
    }

    private static TagKey<Biome> biomeTag(String path) {
        return TagKey.create(Registries.BIOME, id(path));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("neroland", path);
    }
}
