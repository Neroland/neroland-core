package za.co.neroland.nerolandcore.data;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * Shared diagnostics for the erasure seams that Core can only offer as <b>default no-op</b>
 * methods.
 *
 * <p>{@code CurrencyProvider.forgetPlayer} and {@code ReputationProvider.forgetPlayer} cannot be
 * made abstract without breaking the frozen-between-majors API contract, so a downstream provider
 * that persists balances or standings but forgets to override them would silently defeat
 * {@link PlayerDataErasure} — the erasure would report success while the data survived. Routing
 * the default bodies through here turns that silent failure into a loud one.
 *
 * <p>The warning fires <b>once per provider class</b> (not once per erasure request), so a
 * misconfigured server logs a single actionable line rather than one per affected player — which
 * also keeps player identity out of the log, as {@link PlayerDataErasure} requires.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class ErasureWarnings {

    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private ErasureWarnings() {
    }

    /**
     * Warn (once per {@code provider} class) that a registered provider inherited the default
     * no-op {@code forgetPlayer}. No player identity is logged.
     *
     * @param seam     the API the provider implements, e.g. {@code "CurrencyProvider"}
     * @param provider the registered provider instance whose class is named in the warning
     */
    public static void warnDefaultForgetPlayer(String seam, Object provider) {
        String owner = provider == null ? "<unknown>" : provider.getClass().getName();
        if (!WARNED.add(seam + '#' + owner)) {
            return;
        }
        NerolandCoreCommon.LOGGER.warn(
                "[Neroland Core] {} '{}' does not override forgetPlayer(UUID), so a POPIA/GDPR erasure "
                        + "request cannot purge anything it stores. If this provider persists player data, "
                        + "it MUST override forgetPlayer; if it stores nothing, override it as an explicit "
                        + "no-op to silence this warning.",
                seam, owner);
    }
}
