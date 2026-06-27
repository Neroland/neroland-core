package za.co.neroland.nerolandcore.progression;

import java.util.Collection;
import java.util.Set;

import net.minecraft.resources.Identifier;

/**
 * Client-side cache of the gates open for the local player, populated by the
 * server's gate-sync payload. Pure data (no client-only imports) so it is safe to
 * touch from common code. Clients never mutate gate state — they only read this
 * mirror of the server's authoritative answer.
 */
public final class ClientGates {

    private static volatile Set<String> open = Set.of();

    private ClientGates() {
    }

    /** Replace the cache with the server's latest snapshot. */
    public static void accept(Collection<String> gates) {
        open = Set.copyOf(gates);
    }

    /** Whether {@code gate} is open for the local player (per the last server sync). */
    public static boolean isOpen(Identifier gate) {
        return open.contains(gate.toString());
    }
}
