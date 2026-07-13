package za.co.neroland.nerolandcore.decor;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * The shared {@code neroland:decor/*} block-tag vocabulary (added in Core 1.9.0). Core
 * defines the tag <i>ids</i> as a frozen contract; <b>membership is declared by
 * hand-authored datapack JSON</b> in the mods that own the blocks (NeroDecor tags its
 * hull/panel/glass/neon/trim/dashboard/planet blocks; external mods may opt their own
 * blocks in via the same tags — purely tag-mediated, dormant until they port to 26.1+).
 *
 * <p>These are aesthetic grouping tags: they let CTM/compat layers and other mods ask
 * "is this a hull surface?" or "is this a dashboard surface?" by tag alone, without a
 * code dependency. Core ships empty base tag files ({@code required:false}) so the tags
 * always exist; they carry no behaviour and no player data.
 */
public final class DecorTags {

    /** {@code neroland:decor/hull} — structural hull/plating surfaces. */
    public static final TagKey<Block> HULL = tag("decor/hull");
    /** {@code neroland:decor/panel} — industrial wall/floor panels. */
    public static final TagKey<Block> PANEL = tag("decor/panel");
    /** {@code neroland:decor/glass} — reinforced/framed sci-fi glazing. */
    public static final TagKey<Block> GLASS = tag("decor/glass");
    /** {@code neroland:decor/neon} — emissive light strips/bars/panels. */
    public static final TagKey<Block> NEON = tag("decor/neon");
    /** {@code neroland:decor/trim} — accent trim pieces. */
    public static final TagKey<Block> TRIM = tag("decor/trim");
    /** {@code neroland:decor/dashboard_surface} — holograms/panels that accept driver payloads. */
    public static final TagKey<Block> DASHBOARD_SURFACE = tag("decor/dashboard_surface");
    /** {@code neroland:decor/planet} — parent tag for all planet-themed sets. */
    public static final TagKey<Block> PLANET = tag("decor/planet");

    private DecorTags() {
    }

    /**
     * The per-planet decor tag {@code neroland:decor/planet/<planet>} (e.g.
     * {@code decor/planet/luna}). Built on demand so new planets need no Core change; the
     * owning mod ships the membership JSON and should also add the block to {@link #PLANET}.
     */
    public static TagKey<Block> planet(String planetId) {
        return tag("decor/planet/" + planetId);
    }

    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("neroland", path));
    }
}
