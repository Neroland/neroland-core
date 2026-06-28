package za.co.neroland.nerolandcore.storage;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.gas.NeroGases;

/**
 * Creative Gas Tank block — holds a {@link CreativeGasTankBlockEntity}. Empty-hand reads out the
 * endless source gas; sneak-empty-hand clears it.
 */
public class CreativeGasTankBlock extends AbstractStorageBlock {

    public static final MapCodec<CreativeGasTankBlock> CODEC = simpleCodec(CreativeGasTankBlock::new);

    public CreativeGasTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CreativeGasTankBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeGasTankBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof CreativeGasTankBlockEntity tank) {
            if (player.isShiftKeyDown()) {
                tank.setSource(NeroGases.EMPTY);
                serverPlayer.sendSystemMessage(Component.translatable("block.nerolandcore.creative_tank.cleared"));
            } else if (NeroGases.isEmpty(tank.source())) {
                serverPlayer.sendSystemMessage(Component.translatable("block.nerolandcore.creative_gas.unset"));
            } else {
                serverPlayer.sendSystemMessage(Component.translatable("block.nerolandcore.creative_tank.readout", NeroGases.label(tank.source())));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
