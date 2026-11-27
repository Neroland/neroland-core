package za.co.neroland.nerolandcore.reclaim;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.registry.BlockCodecs;
import za.co.neroland.nerolandcore.registry.ModBlockEntities;

/**
 * Ore Reclaimer block: right-click opens its GUI; holds an {@link OreReclaimerBlockEntity}. No power.
 * The front faces the player who placed it, and {@code lit} is set while it is converting.
 */
public class OreReclaimerBlock extends BaseEntityBlock {

    public static final MapCodec<OreReclaimerBlock> CODEC = BlockCodecs.simple(OreReclaimerBlock::new);

    public OreReclaimerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING,
                rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    protected MapCodec<OreReclaimerBlock> codec() {
        return CODEC;
    }

    @Override
    //? if >=1.20.5 {
    protected RenderShape getRenderShape(BlockState state) {
    //?} else {
    /*public RenderShape getRenderShape(BlockState state) {
    *///?}
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OreReclaimerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.ORE_RECLAIMER.get(), OreReclaimerBlockEntity::serverTick);
    }

    //? if >=1.20.5 {
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    //?} else {
    /*@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
    *///?}
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof OreReclaimerBlockEntity be) {
            player.openMenu(be);
        }
        return InteractionResult.SUCCESS;
    }

    // Before 1.21.5 a container block entity does not drop its contents when the block is removed.
    //? if >=1.20.5 && <1.21.5 {
    /*@Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        dropContents(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, moved);
    }
    *///?}
    //? if <1.20.5 {
    /*@Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        dropContents(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, moved);
    }
    *///?}

    static void dropContents(BlockState state, Level level, BlockPos pos, BlockState newState) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof OreReclaimerBlockEntity be) {
            net.minecraft.world.Containers.dropContents(level, pos, be);
        }
    }
}
