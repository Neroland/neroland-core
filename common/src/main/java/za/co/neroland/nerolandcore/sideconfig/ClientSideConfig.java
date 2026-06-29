package za.co.neroland.nerolandcore.sideconfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;

/**
 * Client-side mailbox for authoritative side-config snapshots received from the
 * server, keyed by block position. The {@link Channel}-packed snapshot is dropped
 * here by the common clientbound handler (which must stay free of client-only
 * classes); the Side Config widget polls it each frame and applies it to its own
 * {@link SideConfig} so an open screen always reflects the server's routing.
 *
 * <p>Holds only positions and routing modes — no player data.
 */
public final class ClientSideConfig {

    private static final Map<BlockPos, Map<Channel, Integer>> PENDING = new ConcurrentHashMap<>();

    private ClientSideConfig() {
    }

    /** Called by the clientbound sync handler. */
    public static void accept(BlockPos pos, Map<Channel, Integer> packed) {
        PENDING.put(pos.immutable(), packed);
    }

    /** Take and clear the latest snapshot for a position, or {@code null} if none is pending. */
    public static Map<Channel, Integer> poll(BlockPos pos) {
        return PENDING.remove(pos);
    }

    public static void clear() {
        PENDING.clear();
    }
}
