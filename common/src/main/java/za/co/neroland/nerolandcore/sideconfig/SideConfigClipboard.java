package za.co.neroland.nerolandcore.sideconfig;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

/**
 * Client-side clipboard for copy/paste of a side configuration between machines of
 * the same type. The Side Config widget copies the current machine's packed snapshot
 * plus a type key (typically the block id); paste is only offered when the target's
 * type key matches. Holds only routing modes — no player data.
 */
public final class SideConfigClipboard {

    @Nullable
    private static String typeKey;
    @Nullable
    private static Map<Channel, Integer> packed;

    private SideConfigClipboard() {
    }

    public static void copy(String typeKey, Map<Channel, Integer> packed) {
        SideConfigClipboard.typeKey = typeKey;
        SideConfigClipboard.packed = packed;
    }

    public static boolean hasFor(String typeKey) {
        return packed != null && typeKey != null && typeKey.equals(SideConfigClipboard.typeKey);
    }

    @Nullable
    public static Map<Channel, Integer> packed() {
        return packed;
    }

    public static void clear() {
        typeKey = null;
        packed = null;
    }
}
