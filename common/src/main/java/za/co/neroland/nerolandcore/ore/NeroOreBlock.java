package za.co.neroland.nerolandcore.ore;

import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.registry.BlockCodecs;

/**
 * A Neroland ore block owned by Core. It drops through its loot table and hands the result to the
 * {@link OreBehaviour} the ore's home mod registered, if any.
 *
 * @since 1.25.0
 */
public class NeroOreBlock extends Block {

    public static final MapCodec<NeroOreBlock> CODEC = BlockCodecs.simple(NeroOreBlock::new);

    @Nullable
    private Identifier id;

    public NeroOreBlock(Properties properties) {
        super(properties);
    }

    protected MapCodec<? extends NeroOreBlock> codec() {
        return CODEC;
    }

    @Nullable
    private OreBehaviour behaviour() {
        if (this.id == null) {
            this.id = BuiltInRegistries.BLOCK.getKey(this);
        }
        return OreBehaviours.get(this.id);
    }

    //? if >=1.20.5 {
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    //?} else {
    /*@Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    *///?}
        List<ItemStack> drops = super.getDrops(state, params);
        OreBehaviour behaviour = behaviour();
        return behaviour == null ? drops : behaviour.modifyDrops(state, params, drops);
    }

    //? if >=26.3 {
    /*@Override
    public void playerDestroy(net.minecraft.server.level.ServerLevel level, net.minecraft.server.level.ServerPlayer player,
            BlockPos pos, BlockState state,
            @Nullable BlockEntity blockEntity, ItemStack tool) {
    *///?} else {
    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
            @Nullable BlockEntity blockEntity, ItemStack tool) {
    //?}
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!level.isClientSide()) {
            OreBehaviour behaviour = behaviour();
            if (behaviour != null) {
                behaviour.afterPlayerBreak(level, player, pos, state, tool);
            }
        }
    }
}
