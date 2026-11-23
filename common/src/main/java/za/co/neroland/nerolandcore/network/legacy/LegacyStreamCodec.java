package za.co.neroland.nerolandcore.network.legacy;

//? if <1.20.5 {
/*import java.util.function.BiConsumer;
import java.util.function.Function;

public record LegacyStreamCodec<B, T>(BiConsumer<B, T> encoder, Function<B, T> decoder) {
    public static <B, T> LegacyStreamCodec<B, T> of(BiConsumer<B, T> encoder, Function<B, T> decoder) {
        return new LegacyStreamCodec<>(encoder, decoder);
    }

    public void encode(B buffer, T value) {
        encoder.accept(buffer, value);
    }

    public T decode(B buffer) {
        return decoder.apply(buffer);
    }
}
*///?}
