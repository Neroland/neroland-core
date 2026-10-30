package za.co.neroland.nerolandcore.registry;

import java.util.Set;
import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Version seam for constructing a {@link BlockEntityType}. 1.21.1's constructor takes a third, data-fixer
 * {@code Type} argument (Core passes {@code null}, as vanilla does for unfixed types); 1.21.2+ dropped it.
 * The loaders widen the constructor where vanilla made it private.
 */
public final class BlockEntityTypeCompat {

    private BlockEntityTypeCompat() {
    }

    /** A block-entity type created by {@code factory}, valid on {@code blocks}. */
    public static <T extends BlockEntity> BlockEntityType<T> create(BiFunction<BlockPos, BlockState, T> factory,
            Block... blocks) {
        //? if >=1.21.2 {
        return new BlockEntityType<>(factory::apply, Set.of(blocks));
        //?} else {
        /*return new BlockEntityType<>(factory::apply, Set.of(blocks), null);
        *///?}
    }
}
