package za.co.neroland.nerolandcore.item;

import java.util.UUID;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

/**
 * A place a mod keeps items <i>on behalf of a player</i> that other mods may count or draw from
 * (added in Core 1.15.0) — a storage network, a colony warehouse, a bank vault. Registered with
 * {@link ItemSources} under the owning mod's id; consumers (a shop, a quest turn-in, a NeroLink view)
 * query {@link ItemSources} and never import the owning mod.
 *
 * <p>Both methods run on the server thread and must work for <b>offline</b> players (the UUID is
 * the key, not a live entity). An implementation should only report items the player is entitled to
 * — its own owner-scoped storage — never a server-wide view.
 *
 * <p>Privacy (POPIA/GDPR): the UUID is used as a lookup key only; implementations must not log it.
 */
public interface ItemSource {

    /**
     * How many of {@code itemId} this source holds for {@code player}. Return {@code 0} if none or if
     * the player is unknown to this source; never negative.
     */
    long count(MinecraftServer server, UUID player, Identifier itemId);

    /**
     * Remove up to {@code amount} of {@code itemId} held for {@code player}.
     *
     * @param simulate when {@code true}, report what would be removed without changing anything
     * @return how many were (or would be) removed, between {@code 0} and {@code amount}
     */
    int extract(MinecraftServer server, UUID player, Identifier itemId, int amount, boolean simulate);
}
