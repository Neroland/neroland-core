package za.co.neroland.nerolandcore.meteor;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Tag keys owned by the Meteor Material Registry. The registry is the source of
 * truth for <i>metadata</i> (tier, gate, weight); the tag is the source of truth for
 * <i>membership</i>, so interop layers (JEI/REI, recipe viewers, compat) can ask "is
 * this grindable?" by tag without touching the registry API.
 *
 * <p>Membership is declared by hand-authored datapack JSON (Core ships its own base
 * materials at {@code data/neroland/tags/item/meteor/grindable.json} with
 * {@code required:false}; every other mod ships its own entries the same way),
 * consistent with this project's hand-authored-resources rule. Code that needs the
 * <i>live</i> aggregated set (including annotation-sourced entries) should query
 * {@link MeteorMaterials} instead.
 */
public final class MeteorMaterialTags {

    /** {@code neroland:meteor/grindable} — membership tag for grindable meteor materials. */
    public static final TagKey<Item> GRINDABLE = TagKey.create(
            Registries.ITEM, Identifier.fromNamespaceAndPath("neroland", "meteor/grindable"));

    private MeteorMaterialTags() {
    }
}
