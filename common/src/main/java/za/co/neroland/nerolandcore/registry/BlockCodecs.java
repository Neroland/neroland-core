package za.co.neroland.nerolandcore.registry;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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
 * runtime (via {@link MethodHandles}, never {@code Class#getMethod}) where they still exist and hands back an inert placeholder on 26.3+, where nothing reads a
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

    private static final MethodHandle SIMPLE_CODEC = findSimpleCodec();
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
            return (MapCodec<B>) SIMPLE_CODEC.invoke(factory);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
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

    // Resolved with MethodHandles, NOT Class#getMethod / #getField. Class#getMethod builds the full
    // public-method table of Block (and every supertype), which links the parameter and return types of
    // every method — including loader-patched, client-only ones. On a Forge 26.2 dedicated server that
    // linked net.minecraft.client.renderer.block.BlockAndTintGetter, threw NoClassDefFoundError from this
    // class's static initialiser and aborted block registration (Sentry MC-NEROLAND-CORE-4, with
    // CORE-3/5/6 as the cascading "Registry Object not present: nerolandcore:battery" failures).
    // A MethodHandles lookup resolves only the one named member, so unrelated signatures are never
    // touched. LinkageError is caught as well so a codec lookup can never take registration down: the
    // fallback is the same inert placeholder 26.3 uses, and nothing reads a block codec at runtime.

    private static MethodHandle findSimpleCodec() {
        try {
            return MethodHandles.lookup().findStatic(Block.class, "simpleCodec",
                    MethodType.methodType(MapCodec.class, Function.class));
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Codec<BlockBehaviour.Properties> findPropertiesCodec() {
        try {
            MethodHandle getter = MethodHandles.lookup().findStaticGetter(
                    BlockBehaviour.Properties.class, "CODEC", Codec.class);
            return (Codec<BlockBehaviour.Properties>) getter.invoke();
        } catch (Throwable e) {
            // ReflectiveOperationException (26.3+: the field is gone) or LinkageError — placeholder either way.
            return null;
        }
    }
}
