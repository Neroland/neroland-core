package za.co.neroland.nerolandcore.sideconfig;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;

/**
 * Server-side cycle-mode API for an in-world configurator (wrench-style) tool. Per
 * the side-config design's resolved open question, <b>Core ships this API and the
 * actual item lives in a content mod</b> (Nerotech): the item's {@code useOn} /
 * {@code use} handlers call these helpers, so the routing model and validation stay
 * in Core while the item, texture and recipe stay in the content mod.
 *
 * <p>All methods are server-authoritative and operate directly on the targeted
 * machine's {@link SideConfigComponent}; copy/paste round-trips a compact snapshot
 * the calling item can stash in its own stack NBT. World/block data only — no player
 * identity is read or stored.
 */
public final class Configurator {

    private Configurator() {
    }

    /**
     * Right-click behaviour: cycle the clicked face to its next permitted mode for
     * {@code channel} (or the machine's first declared channel if {@code null}).
     * Returns the new mode, or {@code null} if the target is not side-configurable.
     */
    @Nullable
    public static SideMode cycle(Level level, BlockPos pos, Direction side, @Nullable Channel channel) {
        SideConfigComponent comp = component(level, pos);
        if (comp == null) {
            return null;
        }
        Channel target = resolve(comp, channel);
        if (target == null) {
            return null;
        }
        comp.cycleAbsolute(target, side);
        return comp.config().modeAbsolute(target, comp.facing(), side);
    }

    /**
     * Shift-click behaviour: read (without changing) the clicked face's mode for
     * {@code channel} (or the first declared channel). Returns {@code null} if the
     * target is not side-configurable.
     */
    @Nullable
    public static SideMode read(Level level, BlockPos pos, Direction side, @Nullable Channel channel) {
        SideConfigComponent comp = component(level, pos);
        if (comp == null) {
            return null;
        }
        Channel target = resolve(comp, channel);
        if (target == null) {
            return null;
        }
        return comp.config().modeAbsolute(target, comp.facing(), side);
    }

    /** Copy: a compact snapshot the configurator can stash in its stack NBT for paste. */
    @Nullable
    public static Map<Channel, Integer> snapshot(Level level, BlockPos pos) {
        SideConfigComponent comp = component(level, pos);
        return comp == null ? null : comp.config().packAll();
    }

    /** Paste: apply a previously-copied snapshot. Returns true if the target accepted it. */
    public static boolean apply(Level level, BlockPos pos, Map<Channel, Integer> packed) {
        SideConfigComponent comp = component(level, pos);
        if (comp == null) {
            return false;
        }
        comp.paste(packed);
        return true;
    }

    @Nullable
    private static SideConfigComponent component(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof SideConfigured configured ? configured.sideConfig() : null;
    }

    @Nullable
    private static Channel resolve(SideConfigComponent comp, @Nullable Channel channel) {
        if (channel != null && comp.config().has(channel)) {
            return channel;
        }
        var it = comp.config().channels().keySet().iterator();
        return it.hasNext() ? it.next() : null;
    }
}
