package za.co.neroland.nerolandcore.data;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

import za.co.neroland.nerolandcore.economy.Currency;
import za.co.neroland.nerolandcore.economy.CurrencyApi;
import za.co.neroland.nerolandcore.economy.CurrencyProvider;
import za.co.neroland.nerolandcore.reputation.ReputationApi;
import za.co.neroland.nerolandcore.reputation.ReputationProvider;

/**
 * A reusable POPIA/GDPR <b>erasure conformance harness</b> that any Neroland mod can run from its
 * own test suite against its own stores.
 *
 * <p>Erasure is the one contract that spans the whole ecosystem: {@link PlayerDataErasure} fans a
 * single request out to every registered {@link PlayerDataEraser}, and a mod that registers an
 * eraser which quietly does nothing produces a "successful" erasure with the data still on disk.
 * This class turns that into a mechanical check. It lives in the <b>main</b> source set on purpose —
 * downstream mods depend on Core's published artifact, not on its test classes.
 *
 * <h2>What a run proves</h2>
 * <ol>
 *   <li><b>Nothing is retained.</b> Every probe you register must report data <em>before</em> the
 *       erasure and none <em>after</em> it. A probe that reported nothing to begin with is a
 *       failure, not a pass — it proved nothing.</li>
 *   <li><b>One bad eraser cannot hide behind another.</b> A deliberately failing canary is
 *       registered around a throwaway UUID and the request re-run: erasers registered after the
 *       failing one must still run, and {@link PlayerDataErasure#erase} must not propagate the
 *       failure. (Core catches {@link Throwable}, not just {@link RuntimeException}, so an
 *       {@link Error} from a version-mismatched mod cannot abort the fan-out either.)</li>
 *   <li><b>The economy and reputation seams were actually reached.</b> Both
 *       {@code CurrencyProvider.forgetPlayer} and {@code ReputationProvider.forgetPlayer} are
 *       <em>default no-op</em> methods on their interfaces — a provider that persists data but
 *       forgets to override them defeats erasure silently. The run fails if the bound provider
 *       inherits Core's default body, and fails again if {@code forgetPlayer} was never invoked at
 *       all during the request.</li>
 *   <li><b>Failures are actionable.</b> The {@link Report} names each subsystem that retained data.</li>
 * </ol>
 *
 * <h2>Using it from a downstream mod</h2>
 * <pre>{@code
 * @Test
 * void erasurePurgesEverythingMyModStores() {
 *     UUID player = UUID.randomUUID();
 *     MyState state = new MyState();
 *     state.record(player, ...);                       // seed, so the probes are not vacuous
 *
 *     PlayerDataErasure.register((server, uuid) -> state.forgetPlayer(uuid));
 *
 *     ErasureConformance.create()
 *             .probe("my_mod:player_rows", uuid -> state.has(uuid))
 *             .verify(null, player);                   // null server: this eraser does not need one
 * }
 * }</pre>
 *
 * <p>Pass the real {@link MinecraftServer} instead of {@code null} from a game test whose erasers
 * reach {@code SavedData} stores. {@link #verify} throws an {@link AssertionError} listing every
 * failure, so no JUnit dependency is added to Core's main source set; use {@link #run} if you would
 * rather inspect the {@link Report} yourself.
 *
 * <h2>Caveats</h2>
 * <ul>
 *   <li><b>Not thread-safe and not for production.</b> A run temporarily swaps the bound currency
 *       and reputation providers for recording wrappers and temporarily registers canary erasers,
 *       restoring both in a {@code finally}. Run conformance on one thread at a time.</li>
 *   <li>The isolation pass deliberately makes one eraser throw, so the log will contain one
 *       {@code "Data eraser ... failed"} warning and one {@code "erasure INCOMPLETE"} line per run.
 *       That is the assertion working, not a bug. It runs against a throwaway random UUID so the
 *       real erasure pass stays clean — which does mean every registered eraser is invoked once for
 *       a player that has no data. Erasers must already tolerate that (the retention sweep does the
 *       same for players no system has heard of).</li>
 *   <li>No player identity is written into the {@link Report} — a UUID is personal data and Core's
 *       rule is that erasure logs anonymous counts only.</li>
 * </ul>
 */
public final class ErasureConformance {

    private final Map<String, Probe> probes = new LinkedHashMap<>();

    private ErasureConformance() {
    }

    /** A fresh, empty harness. */
    public static ErasureConformance create() {
        return new ErasureConformance();
    }

    /**
     * Register a subsystem probe.
     *
     * @param subsystem  a stable, human-meaningful name reported back on failure
     *                   (e.g. {@code "nerotech:pollution_attribution"})
     * @param retainsData {@code true} while the subsystem still holds data for the given player.
     *                   It must report {@code true} before the erasure — otherwise the probe proves
     *                   nothing and the run fails.
     */
    public ErasureConformance probe(String subsystem, Predicate<UUID> retainsData) {
        return add(subsystem, retainsData, true);
    }

    /**
     * Register a subsystem probe that is only checked <em>after</em> the erasure.
     *
     * <p>Use this only where the store genuinely cannot be seeded from the test — it weakens the
     * run, because a probe that can never report data will pass regardless.
     */
    public ErasureConformance probeAfterErasureOnly(String subsystem, Predicate<UUID> retainsData) {
        return add(subsystem, retainsData, false);
    }

    private ErasureConformance add(String subsystem, Predicate<UUID> retainsData, boolean seedRequired) {
        if (subsystem == null || subsystem.isBlank()) {
            throw new IllegalArgumentException("A conformance probe needs a subsystem name");
        }
        if (retainsData == null) {
            throw new IllegalArgumentException("A conformance probe needs a retains-data predicate");
        }
        if (probes.putIfAbsent(subsystem, new Probe(subsystem, retainsData, seedRequired)) != null) {
            throw new IllegalArgumentException("Duplicate conformance probe: " + subsystem);
        }
        return this;
    }

    /**
     * Run the harness and throw an {@link AssertionError} describing every failure if it did not
     * pass. Equivalent to {@code run(server, player).assertPassed()}.
     */
    public void verify(MinecraftServer server, UUID player) {
        run(server, player).assertPassed();
    }

    /**
     * Run the harness and return the {@link Report}.
     *
     * @param server the live server, or {@code null} in a plain-JVM test whose erasers do not need one
     * @param player the subject of the erasure request; never written into the report
     */
    public Report run(MinecraftServer server, UUID player) {
        if (player == null) {
            throw new IllegalArgumentException("A conformance run needs a player UUID");
        }
        // Core's own currency/reputation erasers are what carry a request into those seams. In a
        // plain-JVM test CoreData.init() has not run, so make sure they are there (idempotent).
        CoreData.registerProviderErasers();

        List<String> failures = new ArrayList<>();
        List<String> retained = new ArrayList<>();

        if (probes.isEmpty()) {
            failures.add("no probes were registered — this run cannot prove anything was erased");
        }

        // --- 1. Pre-conditions: a probe must actually see data, or it proves nothing ------------
        for (Probe probe : probes.values()) {
            if (!probe.seedRequired()) {
                continue;
            }
            Boolean before = probe.evaluate(player, failures, "before erasure");
            if (Boolean.FALSE.equals(before)) {
                failures.add(probe.subsystem() + ": reported no data BEFORE erasure — the probe proves "
                        + "nothing; seed the store first, or use probeAfterErasureOnly(...)");
            }
        }

        // --- 2. Isolation: a failing eraser must not stop the ones after it --------------------
        checkFailureIsolation(server, failures);

        // --- 3. The erasure request itself, with the two provider seams instrumented -----------
        CurrencyProvider currencyBefore = CurrencyApi.provider();
        ReputationProvider reputationBefore = ReputationApi.provider();
        RecordingCurrencyProvider currencySpy = new RecordingCurrencyProvider(currencyBefore, player);
        RecordingReputationProvider reputationSpy = new RecordingReputationProvider(reputationBefore, player);

        if (!overridesForgetPlayer(currencyBefore, CurrencyProvider.class)) {
            failures.add("CurrencyProvider '" + className(currencyBefore) + "' inherits Core's default "
                    + "no-op forgetPlayer(UUID) — an erasure request cannot reach anything it stores");
        }
        if (!overridesForgetPlayer(reputationBefore, ReputationProvider.class)) {
            failures.add("ReputationProvider '" + className(reputationBefore) + "' inherits Core's default "
                    + "no-op forgetPlayer(UUID) — an erasure request cannot reach anything it stores");
        }

        int registered = PlayerDataErasure.registeredCount();
        try {
            CurrencyApi.setProvider(currencySpy);
            ReputationApi.setProvider(reputationSpy);
            try {
                PlayerDataErasure.erase(server, player);
            } catch (Throwable propagated) {
                failures.add("PlayerDataErasure.erase propagated " + propagated
                        + " — a single failing eraser aborted the whole request");
            }
        } finally {
            restoreCurrency(currencySpy, currencyBefore);
            restoreReputation(reputationSpy, reputationBefore);
        }

        if (!currencySpy.invoked) {
            failures.add("CurrencyApi.provider().forgetPlayer(UUID) was never invoked during the "
                    + "erasure — the economy seam is not wired into PlayerDataErasure");
        }
        if (!reputationSpy.invoked) {
            failures.add("ReputationApi.provider().forgetPlayer(UUID) was never invoked during the "
                    + "erasure — the reputation seam is not wired into PlayerDataErasure");
        }

        // --- 4. Post-conditions: nothing may still hold the player's data ----------------------
        for (Probe probe : probes.values()) {
            Boolean after = probe.evaluate(player, failures, "after erasure");
            if (Boolean.TRUE.equals(after)) {
                retained.add(probe.subsystem());
                failures.add(probe.subsystem() + ": STILL HOLDS DATA after erasure");
            }
        }

        return new Report(List.copyOf(failures), List.copyOf(retained), registered);
    }

    // --- Isolation pass ---------------------------------------------------------------------

    /**
     * Re-runs the fan-out for a throwaway UUID with a deliberately failing eraser wedged between two
     * recording canaries, to prove {@link PlayerDataErasure} keeps going. A throwaway UUID is used
     * so the real erasure pass is not polluted by the injected failure.
     */
    private static void checkFailureIsolation(MinecraftServer server, List<String> failures) {
        UUID canaryPlayer = UUID.randomUUID();
        boolean[] ranBefore = {false};
        boolean[] ranAfter = {false};

        PlayerDataEraser first = (s, uuid) -> {
            if (canaryPlayer.equals(uuid)) {
                ranBefore[0] = true;
            }
        };
        PlayerDataEraser failing = (s, uuid) -> {
            if (canaryPlayer.equals(uuid)) {
                throw new IllegalStateException(
                        "ErasureConformance canary: deliberate failure, safe to ignore");
            }
        };
        PlayerDataEraser last = (s, uuid) -> {
            if (canaryPlayer.equals(uuid)) {
                ranAfter[0] = true;
            }
        };

        try {
            PlayerDataErasure.register(first);
            PlayerDataErasure.register(failing);
            PlayerDataErasure.register(last);
            try {
                PlayerDataErasure.erase(server, canaryPlayer);
            } catch (Throwable propagated) {
                failures.add("PlayerDataErasure.erase propagated " + propagated
                        + " from a failing eraser instead of continuing with the rest");
            }
        } finally {
            PlayerDataErasure.unregister(last);
            PlayerDataErasure.unregister(failing);
            PlayerDataErasure.unregister(first);
        }

        if (!ranBefore[0]) {
            failures.add("erasers registered before a failing one did not run");
        }
        if (!ranAfter[0]) {
            failures.add("erasers registered AFTER a failing one did not run — one broken mod would "
                    + "silently leave every later system's data in place");
        }
    }

    // --- Provider instrumentation -------------------------------------------------------------

    private static void restoreCurrency(CurrencyProvider spy, CurrencyProvider original) {
        if (CurrencyApi.provider() == spy) {
            CurrencyApi.setProvider(original);
        }
    }

    private static void restoreReputation(ReputationProvider spy, ReputationProvider original) {
        if (ReputationApi.provider() == spy) {
            ReputationApi.setProvider(original);
        }
    }

    /**
     * Whether {@code provider} declares its own {@code forgetPlayer(UUID)} rather than inheriting the
     * interface's default no-op body. {@link Class#getMethod} resolves an un-overridden default to
     * the interface that declares it, which is exactly the case that must fail.
     */
    private static boolean overridesForgetPlayer(Object provider, Class<?> contract) {
        if (provider == null) {
            return false;
        }
        try {
            Method method = provider.getClass().getMethod("forgetPlayer", UUID.class);
            return !contract.equals(method.getDeclaringClass());
        } catch (NoSuchMethodException | SecurityException unresolvable) {
            return false;
        }
    }

    private static String className(Object o) {
        return o == null ? "<none bound>" : o.getClass().getName();
    }

    private record Probe(String subsystem, Predicate<UUID> retainsData, boolean seedRequired) {

        /** {@code null} if the probe itself blew up (already recorded as a failure). */
        Boolean evaluate(UUID player, List<String> failures, String phase) {
            try {
                return retainsData.test(player);
            } catch (Throwable t) {
                failures.add(subsystem + ": probe threw " + t + " when evaluated " + phase);
                return null;
            }
        }
    }

    /** Delegating {@link CurrencyProvider} that records whether the watched player was forgotten. */
    private static final class RecordingCurrencyProvider implements CurrencyProvider {

        private final CurrencyProvider delegate;
        private final UUID watched;
        private volatile boolean invoked;

        RecordingCurrencyProvider(CurrencyProvider delegate, UUID watched) {
            this.delegate = delegate;
            this.watched = watched;
        }

        @Override
        public long getBalance(UUID player, Currency currency) {
            return delegate.getBalance(player, currency);
        }

        @Override
        public boolean deposit(UUID player, Currency currency, long amount) {
            return delegate.deposit(player, currency, amount);
        }

        @Override
        public boolean withdraw(UUID player, Currency currency, long amount) {
            return delegate.withdraw(player, currency, amount);
        }

        @Override
        public boolean transfer(UUID from, UUID to, Currency currency, long amount) {
            return delegate.transfer(from, to, currency, amount);
        }

        @Override
        public void forgetPlayer(UUID player) {
            if (watched.equals(player)) {
                invoked = true;
            }
            delegate.forgetPlayer(player);
        }
    }

    /** Delegating {@link ReputationProvider} that records whether the watched player was forgotten. */
    private static final class RecordingReputationProvider implements ReputationProvider {

        private final ReputationProvider delegate;
        private final UUID watched;
        private volatile boolean invoked;

        RecordingReputationProvider(ReputationProvider delegate, UUID watched) {
            this.delegate = delegate;
            this.watched = watched;
        }

        @Override
        public int getReputation(UUID player, Identifier faction) {
            return delegate.getReputation(player, faction);
        }

        @Override
        public void setReputation(UUID player, Identifier faction, int value) {
            delegate.setReputation(player, faction, value);
        }

        @Override
        public int adjust(UUID player, Identifier faction, int delta) {
            return delegate.adjust(player, faction, delta);
        }

        @Override
        public void forgetPlayer(UUID player) {
            if (watched.equals(player)) {
                invoked = true;
            }
            delegate.forgetPlayer(player);
        }
    }

    /**
     * The outcome of a conformance run. Carries no player identity — a UUID is personal data, and
     * Core's rule is that erasure produces anonymous diagnostics only.
     */
    public static final class Report {

        private final List<String> failures;
        private final List<String> retained;
        private final int erasersRegistered;

        private Report(List<String> failures, List<String> retained, int erasersRegistered) {
            this.failures = failures;
            this.retained = retained;
            this.erasersRegistered = erasersRegistered;
        }

        /** Whether every check passed. */
        public boolean passed() {
            return failures.isEmpty();
        }

        /** Every failure, in the order detected. Empty when {@link #passed()}. */
        public List<String> failures() {
            return failures;
        }

        /** The names of the subsystems whose probes still reported data after the erasure. */
        public List<String> retainedSubsystems() {
            return retained;
        }

        /** How many erasers were registered when the request went out. */
        public int erasersRegistered() {
            return erasersRegistered;
        }

        /** A multi-line, actionable summary — safe to log or attach to an assertion message. */
        public String describe() {
            StringBuilder out = new StringBuilder("Erasure conformance ")
                    .append(passed() ? "PASSED" : "FAILED")
                    .append(" (").append(erasersRegistered).append(" registered eraser(s))");
            if (!retained.isEmpty()) {
                out.append("\n  retained data: ").append(String.join(", ", retained));
            }
            for (String failure : failures) {
                out.append("\n  - ").append(failure);
            }
            return out.toString();
        }

        /** Throw an {@link AssertionError} carrying {@link #describe()} unless the run passed. */
        public void assertPassed() {
            if (!passed()) {
                throw new AssertionError(describe());
            }
        }

        @Override
        public String toString() {
            return describe();
        }
    }
}
