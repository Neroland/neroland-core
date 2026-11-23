package za.co.neroland.nerolandcore.network;

//? if <1.20.5 {
/*import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

class LegacyNetworkCodecTest {
    @Test
    void roundTripsSideConfigurationIntent() {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            var intent = SideConfigIntentPayload.setMode(new BlockPos(2, 64, -9), 1, 2, 3);
            SideConfigIntentPayload.STREAM_CODEC.encode(buffer, intent);
            assertEquals(intent, SideConfigIntentPayload.STREAM_CODEC.decode(buffer));
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Test
    void rejectsTruncatedIntent() {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeBlockPos(BlockPos.ZERO);
            assertThrows(IndexOutOfBoundsException.class, () -> SideConfigIntentPayload.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }
}
*///?}
