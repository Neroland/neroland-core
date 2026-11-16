package za.co.neroland.nerolandcore.map;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

/**
 * A per-dimension badge with no position (Core 1.19.0): a storm raging somewhere in a dimension, a world
 * this viewer has visited. Returned by {@link MapLayerProvider#dimensionBadges} under the same scoping rule
 * as layers; at most {@link #MAX_PER_PROVIDER} per provider, and the id's namespace must be the provider's
 * mod id ({@link MapLayerRegistry#dimensionBadges} drops anything else).
 *
 * @param id        stable badge id, namespaced by the providing mod
 * @param dimension the dimension it applies to
 * @param icon      icon id resolved by the consumer's atlas
 * @param label     optional label (same privacy rule as {@link MapLabel})
 */
public record MapDimensionBadge(Identifier id, ResourceKey<Level> dimension, Identifier icon,
        @Nullable MapLabel label) {

    /** Most badges one provider returns per viewer. */
    public static final int MAX_PER_PROVIDER = 8;

    public MapDimensionBadge {
        if (id == null || dimension == null || icon == null) {
            throw new IllegalArgumentException("A dimension badge needs an id, a dimension and an icon");
        }
    }
}
