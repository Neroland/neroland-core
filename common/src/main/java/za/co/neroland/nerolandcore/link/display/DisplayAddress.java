package za.co.neroland.nerolandcore.link.display;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Where a {@link DisplaySurface} lives: a dimension plus a block position. This is the
 * key a driver mod (NeroSecurity, NeroLogistics, …) uses to address a placed hologram or
 * control panel without holding a reference to NeroDecor's block entity.
 *
 * @param dimension the level the surface is in
 * @param pos       the block position (normalised to an immutable {@link BlockPos})
 */
public record DisplayAddress(ResourceKey<Level> dimension, BlockPos pos) {

    public DisplayAddress {
        pos = pos.immutable();
    }

    /** Convenience factory. */
    public static DisplayAddress of(ResourceKey<Level> dimension, BlockPos pos) {
        return new DisplayAddress(dimension, pos);
    }
}
