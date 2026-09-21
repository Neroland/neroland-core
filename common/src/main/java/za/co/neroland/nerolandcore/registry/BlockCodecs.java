package za.co.neroland.nerolandcore.registry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Version-neutral block-type codecs.
 *
 * <p>Minecraft 26.3 removed block-type codecs outright: {@code Block#simpleCodec},
 * {@code Block#propertiesCodec}, {@code BlockBehaviour#codec()}, {@code BlockBehaviour.Properties#CODEC}
 * and the {@code BLOCK_TYPE} registry are all gone. Shared {@code common} source is compiled unchanged
 * against 26.1.2, 26.2 and 26.3, so it cannot name those members directly. This helper resolves them at
 * runtime where they still exist and hands back an inert placeholder on 26.3+, where nothing reads a
 * block's codec.
 *
 * <p>Usage in a block class — note {@code codec()} is declared <em>without</em> {@code @Override}, since
 * on 26.3 there is nothing left to override:
 *
 * <pre>{@code
 * public static final MapCodec<MyBlock> CODEC = BlockCodecs.simple(MyBlock::new);
 *
 * protected MapCodec<MyBlock> codec() {
 *     return CODEC;
 * }
 * }</pre>
 *
 * <p>For record-style codecs, {@link #properties()} replaces {@code propertiesCodec()} inside
 * {@code RecordCodecBuilder.mapCodec(instance -> instance.group(..., BlockCodecs.properties()))}.
 *
 * @since 1.13.0
 */
public final class BlockCodecs {

    private static final Method SIMPLE_CODEC = findSimpleCodec();
    private static final Codec<BlockBehaviour.Properties> PROPERTIES_CODEC = findPropertiesCodec();

    private BlockCodecs() {
    }

    /** {@code true} while the running Minecraft still has block-type codecs (26.1.2 / 26.2). */
    public static boolean present() {
        return SIMPLE_CODEC != null;
    }

    /** Equivalent of {@code Block.simpleCodec(factory)}; an inert placeholder on 26.3+. */
    @SuppressWarnings("unchecked")
    public static <B extends Block> MapCodec<B> simple(Function<BlockBehaviour.Properties, B> factory) {
        if (SIMPLE_CODEC == null) {
            return MapCodec.<B>unit(BlockCodecs::removed);
        }
        try {
            return (MapCodec<B>) SIMPLE_CODEC.invoke(null, factory);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Block.simpleCodec could not be invoked", e);
        }
    }

    /** Equivalent of {@code Block.propertiesCodec()}; an inert placeholder field on 26.3+. */
    public static <B extends Block> RecordCodecBuilder<B, BlockBehaviour.Properties> properties() {
        if (PROPERTIES_CODEC == null) {
            return MapCodec.<BlockBehaviour.Properties>unit(BlockCodecs::removed).forGetter(BlockBehaviour::properties);
        }
        return PROPERTIES_CODEC.fieldOf("properties").forGetter(BlockBehaviour::properties);
    }

    private static <T> T removed() {
        throw new UnsupportedOperationException("Block-type codecs were removed in Minecraft 26.3");
    }

    private static Method findSimpleCodec() {
        try {
            return Block.class.getMethod("simpleCodec", Function.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Codec<BlockBehaviour.Properties> findPropertiesCodec() {
        try {
            Field field = BlockBehaviour.Properties.class.getField("CODEC");
            return (Codec<BlockBehaviour.Properties>) field.get(null);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
