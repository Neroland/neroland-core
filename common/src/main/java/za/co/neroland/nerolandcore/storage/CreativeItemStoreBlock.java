package za.co.neroland.nerolandcore.storage;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.registry.BlockCodecs;

/**
 * Creative Item Store block — holds a {@link CreativeItemStoreBlockEntity}. Right-click with an item to
 * set the endless source; sneak-right-click (empty hand) to clear it.
 */
public class CreativeItemStoreBlock extends AbstractStorageBlock {

    public static final MapCodec<CreativeItemStoreBlock> CODEC = BlockCodecs.simple(CreativeItemStoreBlock::new);

    public CreativeItemStoreBlock(Properties properties) {
        super(properties);
    }

    protected MapCodec<CreativeItemStoreBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeItemStoreBlockEntity(pos, state);
    }

    //? if >=1.21.2 {
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
    //?} else {
    /*@Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
    *///?}
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CreativeItemStoreBlockEntity store) {
            store.setSource(stack);
            //? if >=26.1 {
            player.sendSystemMessage(Component.translatable(
                    "block.nerolandcore.creative_item_store.set", stack.getHoverName()));
            //?} else {
            /*player.displayClientMessage(Component.translatable(
                    "block.nerolandcore.creative_item_store.set", stack.getHoverName()), false);
            *///?}
        }
        //? if >=1.21.2 {
        return InteractionResult.SUCCESS;
        //?} else {
        /*return net.minecraft.world.ItemInteractionResult.SUCCESS;
        *///?}
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CreativeItemStoreBlockEntity store) {
            store.setSource(ItemStack.EMPTY);
            //? if >=26.1 {
            player.sendSystemMessage(Component.translatable("block.nerolandcore.creative_item_store.cleared"));
            //?} else {
            /*player.displayClientMessage(Component.translatable("block.nerolandcore.creative_item_store.cleared"), false);
            *///?}
        }
        return InteractionResult.SUCCESS;
    }
}
