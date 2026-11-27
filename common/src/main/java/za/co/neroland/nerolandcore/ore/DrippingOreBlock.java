package za.co.neroland.nerolandcore.ore;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import za.co.neroland.nerolandcore.registry.BlockCodecs;

/**
 * An ore that drips: one honey-drip particle under the block about every 100 ticks when the space below is
 * air (Amber Sap Ore). Purely decorative and client-side.
 *
 * @since 1.25.0
 */
public class DrippingOreBlock extends NeroOreBlock {

    public static final MapCodec<DrippingOreBlock> CODEC = BlockCodecs.simple(DrippingOreBlock::new);

    public static final int DRIP_ONE_IN = 100;

    public DrippingOreBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DrippingOreBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(DRIP_ONE_IN) != 0) {
            return;
        }
        if (!level.getBlockState(pos.below()).isAir()) {
            return;
        }
        level.addParticle(ParticleTypes.DRIPPING_HONEY, pos.getX() + random.nextDouble(), pos.getY() - 0.05,
                pos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
    }
}
