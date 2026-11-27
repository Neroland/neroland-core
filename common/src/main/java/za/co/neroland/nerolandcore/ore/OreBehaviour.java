package za.co.neroland.nerolandcore.ore;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

/**
 * Optional extra behaviour a mod attaches to one of Core's ores (see {@link CoreOres}).
 *
 * <p>Core owns the ore blocks, but some of them carry planet-specific rules that belong to the mod that
 * generates them: Nerospace's Hematite pulls its drops toward the breaker, and its Europa ores change their
 * drops with the liquid next to them. That mod registers an {@code OreBehaviour} through
 * {@link OreBehaviours#register} at init. Without the mod the ore is a plain block that drops through its
 * loot table.
 *
 * <p>Both hooks run on the logical server only. Implementations must not store or log player data.
 *
 * @since 1.25.0
 */
public interface OreBehaviour {

    /**
     * Adjusts the drops rolled from the block's loot table. Called for every break that rolls loot,
     * including explosions and non-player breaks.
     *
     * @param drops the loot-table drops; may be returned as is
     * @return the drops to spawn
     */
    default List<ItemStack> modifyDrops(BlockState state, LootParams.Builder params, List<ItemStack> drops) {
        return drops;
    }

    /**
     * Runs after a survival player has broken the block and its drops have spawned. The player reference
     * must not outlive the call except as a weak reference for at most a few ticks.
     */
    default void afterPlayerBreak(Level level, Player player, BlockPos pos, BlockState state, ItemStack tool) {
    }
}
