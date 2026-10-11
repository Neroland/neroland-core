package za.co.neroland.nerolandcore.item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.event.ListenerGuard;
import za.co.neroland.nerolandcore.event.SourceIds;

/**
 * Registry of {@link ItemSource}s keyed by the owning mod's id (added in Core 1.15.0). Lets any mod
 * count or draw a player's items across every participating mod's storage while depending only on
 * Core.
 *
 * <p>Aggregate queries visit sources in registration order. A source that throws a
 * {@link RuntimeException} is logged once per source class (without the player or item) and treated
 * as holding nothing; the remaining sources are still consulted. Out-of-contract results are clamped
 * (a negative count counts as 0, an extraction never exceeds what was asked).
 *
 * <p>Server thread only for queries; registration is thread-safe and normally happens at mod init.
 */
public final class ItemSources {

    private static final String BUS = "ItemSources";

    /** Insertion-ordered for a deterministic extraction order; mutated only under its own monitor. */
    private static final Map<String, ItemSource> SOURCES = new LinkedHashMap<>();
    /** Immutable, ordered copy read by queries, so a registration mid-query cannot break iteration. */
    private static volatile List<Map.Entry<String, ItemSource>> snapshot = List.of();

    private ItemSources() {
    }

    /**
     * Register (or replace) the item source for {@code modId}.
     *
     * @throws IllegalArgumentException if {@code modId} does not match {@link SourceIds#SOURCE_MOD_ID}
     *                                  or {@code source} is null
     */
    public static void register(String modId, ItemSource source) {
        if (!SourceIds.isValidSourceModId(modId) || source == null) {
            throw new IllegalArgumentException("Item source needs a valid mod id ([a-z0-9_.-]{1,64}) and a source");
        }
        synchronized (SOURCES) {
            SOURCES.put(modId, source);
            refresh();
        }
    }

    /** Remove {@code modId}'s source. @return {@code true} if one was registered. */
    public static boolean unregister(String modId) {
        synchronized (SOURCES) {
            boolean removed = SOURCES.remove(modId) != null;
            if (removed) {
                refresh();
            }
            return removed;
        }
    }

    private static void refresh() {
        List<Map.Entry<String, ItemSource>> copy = new ArrayList<>();
        SOURCES.forEach((id, source) -> copy.add(Map.entry(id, source)));
        snapshot = List.copyOf(copy);
    }

    /** The source registered for {@code modId}, or {@code null}. */
    @Nullable
    public static ItemSource get(String modId) {
        for (Map.Entry<String, ItemSource> entry : snapshot) {
            if (entry.getKey().equals(modId)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** The registered mod ids, in registration order. */
    public static List<String> modIds() {
        List<String> ids = new ArrayList<>();
        for (Map.Entry<String, ItemSource> entry : snapshot) {
            ids.add(entry.getKey());
        }
        return List.copyOf(ids);
    }

    /** Total of {@code itemId} held for {@code player} across every source (saturating at {@code Long.MAX_VALUE}). */
    public static long count(MinecraftServer server, UUID player, Identifier itemId) {
        long total = 0L;
        for (Map.Entry<String, ItemSource> entry : snapshot) {
            long n = safeCount(entry.getValue(), server, player, itemId);
            total = n > Long.MAX_VALUE - total ? Long.MAX_VALUE : total + n;
        }
        return total;
    }

    /** Per-source counts for {@code itemId}, in registration order; sources holding none are omitted. */
    public static Map<String, Long> countBySource(MinecraftServer server, UUID player, Identifier itemId) {
        Map<String, Long> out = new LinkedHashMap<>();
        for (Map.Entry<String, ItemSource> entry : snapshot) {
            long n = safeCount(entry.getValue(), server, player, itemId);
            if (n > 0) {
                out.put(entry.getKey(), n);
            }
        }
        return out;
    }

    /**
     * Remove up to {@code amount} of {@code itemId} for {@code player}, drawing from each source in
     * registration order until satisfied.
     *
     * @param simulate when {@code true}, nothing changes — the result is what a real call would take
     * @return how many were (or would be) removed, between {@code 0} and {@code amount}
     */
    public static int extract(MinecraftServer server, UUID player, Identifier itemId, int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int remaining = amount;
        for (Map.Entry<String, ItemSource> entry : snapshot) {
            if (remaining <= 0) {
                break;
            }
            ItemSource source = entry.getValue();
            int taken;
            try {
                taken = source.extract(server, player, itemId, remaining, simulate);
            } catch (RuntimeException failure) {
                ListenerGuard.report(BUS, source, failure);
                taken = 0;
            }
            remaining -= Math.max(0, Math.min(taken, remaining));
        }
        return amount - remaining;
    }

    private static long safeCount(ItemSource source, MinecraftServer server, UUID player, Identifier itemId) {
        try {
            return Math.max(0L, source.count(server, player, itemId));
        } catch (RuntimeException failure) {
            ListenerGuard.report(BUS, source, failure);
            return 0L;
        }
    }
}
