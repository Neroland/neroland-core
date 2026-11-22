package za.co.neroland.nerolandcore.telemetry;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;

import io.sentry.Breadcrumb;
import io.sentry.IScopes;
import io.sentry.ITransaction;
import io.sentry.Scope;
import io.sentry.Scopes;
import io.sentry.SentryClient;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.SentryOptions;
import io.sentry.SpanStatus;
import io.sentry.UncaughtExceptionHandlerIntegration;
import io.sentry.protocol.Message;
import io.sentry.protocol.SentryException;
import io.sentry.protocol.SentryStackFrame;
import io.sentry.protocol.SentryStackTrace;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.config.CoreConfig;
import za.co.neroland.nerolandcore.platform.Services;

/**
 * Crash/error reporting for Neroland Core via Sentry (EU ingest), built to satisfy both the CurseForge
 * moderation rule (external analytics must be disclosed and opt-out-able) and POPIA/GDPR
 * data-minimisation:
 *
 * <ul>
 *   <li><b>Opt-out:</b> gated on {@code telemetryEnabled} in {@link CoreConfig} (default ON, disclosed).
 *       Set it false to stop reporting (takes effect on restart).</li>
 *   <li><b>Neroland Core errors only:</b> {@code beforeSend} drops any event whose stack trace does not
 *       touch {@code za.co.neroland.nerolandcore}.</li>
 *   <li><b>No personal data:</b> {@code sendDefaultPii=false} (no IP), no server/host name, no user
 *       identity, and OS-account names are scrubbed from file paths. Remaining payload: stack trace,
 *       mod/MC/loader/OS/Java versions.</li>
 *   <li><b>Bounded volume:</b> per-session de-duplication plus a hard cap of
 *       {@value #MAX_EVENTS_PER_SESSION} events per game session.</li>
 * </ul>
 *
 * <p><b>Isolated client:</b> every Nero mod embeds the same deduplicated Sentry jar, so the global
 * {@code Sentry.init} is shared and the last mod to call it replaces everyone else's client (and its
 * {@code beforeSend} then drops their events). Core therefore never touches the global {@code Sentry}
 * API: it builds its own {@link SentryOptions}, {@link SentryClient} and {@link Scopes} and reports only
 * through them, so other mods (Nero or not) calling {@code Sentry.init} cannot silence it.</p>
 *
 * <p>{@link #init()} is called once per loader at bootstrap and reads loader facts through
 * {@link Services#PLATFORM}. Dev/IDE runs report too, tagged {@code environment=development} so they
 * stay out of release metrics. Full disclosure text: {@code PRIVACY.md}.</p>
 */
public final class NerolandCoreTelemetry {

    /** Sentry DSN — a public client key (write-only ingest), safe to ship in the jar. */
    private static final String DSN =
            "https://efe0be37108a2b35364c81a62728d03a@o4511183823241216.ingest.de.sentry.io/4511639358406736";

    /** Stack traces must contain this package for an event to be sent. */
    private static final String PACKAGE_MARKER = "za.co.neroland.nerolandcore";

    /** Hard cap on events per game session (data minimisation + noise control). */
    private static final int MAX_EVENTS_PER_SESSION = 10;

    /** Cap on how many mod ids are attached as crash context (payload bound). */
    private static final int MAX_MODS_REPORTED = 300;

    /** Masks OS-account names in Windows/macOS/Linux home-directory paths. */
    private static final Pattern USER_PATH =
            Pattern.compile("(?i)(?:[A-Z]:)?[/\\\\](?:Users|home)[/\\\\][^/\\\\\\s:;,'\"]+");

    private static volatile boolean active;
    /** Core's own, non-global Sentry scopes; {@code null} until {@link #start()} succeeds. */
    private static volatile IScopes scopes;
    /** The uncaught-exception integration bound to {@link #scopes}; never closed (process lifetime). */
    private static UncaughtExceptionHandlerIntegration uncaughtHandler;
    private static final AtomicInteger eventsSent = new AtomicInteger();
    private static final Set<String> seenFingerprints = ConcurrentHashMap.newKeySet();
    private static SentryLogAppender appender;

    private NerolandCoreTelemetry() {
    }

    /**
     * Called once per loader at bootstrap. Starts reporting iff the player has not opted out
     * ({@code telemetryEnabled=true}, the default). Dev (IDE) runs also report — tagged
     * {@code environment=development} — so they are trivially filtered out of production metrics. Set
     * {@code telemetryEnabled=false} to silence everything (incl. dev).
     */
    public static void init() {
        if (!CoreConfig.TELEMETRY_ENABLED.get()) {
            return;
        }
        start();
    }

    /**
     * Fires a single synthetic Sentry event (a Neroland-originated {@link IllegalStateException}) to
     * confirm end-to-end reporting on a real jar. The per-session de-dup means repeat calls in one
     * session collapse to one event (restart to test again).
     *
     * @return {@code true} if telemetry is active and the event was dispatched; {@code false} otherwise.
     */
    public static boolean sendTestEvent(String origin) {
        if (!active) {
            return false;
        }
        capture(new IllegalStateException(
                "Neroland Core Sentry test (" + origin + ") — synthetic event, safe to ignore"));
        return true;
    }

    private static synchronized void start() {
        if (active) {
            return;
        }
        String modVersion = Services.PLATFORM.getModVersion();
        boolean dev = Services.PLATFORM.isDevelopmentEnvironment();
        SentryOptions options = baseOptions(modVersion, dev ? "development" : environmentOf(modVersion));
        options.setBeforeSend((event, hint) -> ForeignMixinFailure.matches(event) ? null : filterAndScrub(event));
        IScopes own = isolatedScopes(options, "nerolandcore");
        scopes = own;
        own.configureScope(scope -> {
            scope.setTag("loader", Services.PLATFORM.getPlatformName().toLowerCase(Locale.ROOT));
            scope.setTag("dist", Services.PLATFORM.isClient() ? "client" : "dedicated_server");
            scope.setTag("runtime", dev ? "development" : "production");
            scope.setTag("mc_version", minecraftVersion());
            // Config values — surfaces crashes that only reproduce at non-default settings.
            scope.setTag("cfg_material_hardness", fmt(CoreConfig.MATERIAL_HARDNESS_MULTIPLIER.get()));
            scope.setTag("cfg_material_blast", fmt(CoreConfig.MATERIAL_BLAST_MULTIPLIER.get()));
            scope.setTag("cfg_energy_ratio", fmt(CoreConfig.NERO_ENERGY_TO_FE.get()));
            scope.setTag("cfg_upgrade_cap", Integer.toString(CoreConfig.UPGRADE_MODULE_SLOT_CAP.get()));
            scope.setTag("cfg_upgrade_diminish", fmt(CoreConfig.UPGRADE_STACKING_DIMINISH.get()));
            // Loaded-mod list (public manifest ids + versions only) for mod-conflict triage.
            try {
                List<String> mods = Services.PLATFORM.getLoadedModIds();
                if (mods != null && !mods.isEmpty()) {
                    if (mods.size() > MAX_MODS_REPORTED) {
                        mods = mods.subList(0, MAX_MODS_REPORTED);
                    }
                    scope.setTag("mod_count", Integer.toString(mods.size()));
                    java.util.Map<String, Object> modContext = new java.util.HashMap<>();
                    modContext.put("count", mods.size());
                    modContext.put("ids", mods);
                    scope.setContexts("loaded_mods", modContext);
                }
            } catch (RuntimeException | LinkageError e) {
                // Mod list not available this early — skip it; the rest of the report is unaffected.
            }
        });
        // Manual release-health session for this play session; closed on a clean JVM shutdown.
        own.startSession();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                own.endSession();
                own.flush(2000L);
            } catch (RuntimeException ignored) {
                // best-effort flush on shutdown
            }
        }, "nerolandcore-sentry-shutdown"));
        if (appender == null) {
            appender = new SentryLogAppender();
            appender.start();
            ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(appender);
        }
        active = true;
        NerolandCoreCommon.LOGGER.info(
                "[Neroland Core] Telemetry enabled (anonymous error reports, EU servers; opt out via "
                        + "telemetryEnabled=false in config/nerolandcore.properties).");
    }

    /**
     * Drops a lightweight, non-identifying breadcrumb onto the current scope — a trail of what the mod
     * was doing that rides along with the next error report. No-op when telemetry is off. The message is
     * scrubbed of OS-account paths exactly like every other payload.
     */
    public static void breadcrumb(String category, String message) {
        if (!active) {
            return;
        }
        Breadcrumb crumb = new Breadcrumb();
        crumb.setType("default");
        crumb.setCategory(category);
        crumb.setLevel(SentryLevel.INFO);
        crumb.setMessage(scrub(message));
        IScopes s = scopes;
        if (s != null) {
            s.addBreadcrumb(crumb);
        }
    }

    /**
     * Times a unit of work as a sampled Sentry transaction (performance tracing). Returns the body's
     * value. When telemetry is off the body simply runs untraced. Only timing + operation name are
     * recorded — never personal data.
     */
    public static <T> T trace(String operation, String name, java.util.function.Supplier<T> body) {
        IScopes s = scopes;
        if (!active || s == null) {
            return body.get();
        }
        ITransaction tx = s.startTransaction(name, operation);
        try {
            return body.get();
        } catch (RuntimeException | Error e) {
            tx.setThrowable(e);
            tx.setStatus(SpanStatus.INTERNAL_ERROR);
            throw e;
        } finally {
            tx.finish();
        }
    }

    /** {@link #trace(String, String, java.util.function.Supplier)} for a body with no return value. */
    public static void trace(String operation, String name, Runnable body) {
        trace(operation, name, () -> {
            body.run();
            return null;
        });
    }

    /**
     * The privacy-preserving options every Core report is sent with, before the {@code beforeSend} gate
     * is attached. Package-private for tests.
     */
    static SentryOptions baseOptions(String modVersion, String environment) {
        SentryOptions options = new SentryOptions();
        options.setDsn(DSN);
        options.setRelease("nerolandcore@" + modVersion);
        // Dev/IDE runs report under a dedicated environment so they never mix with real releases.
        options.setEnvironment(environment);
        // POPIA/GDPR: never store the sender's IP address or identity.
        options.setSendDefaultPii(false);
        // The machine's hostname is identifying; never attach it.
        options.setAttachServerName(false);
        options.setEnableUncaughtExceptionHandler(true);
        // The global shutdown-hook integration is never registered for isolated scopes; Core flushes in
        // its own hook instead.
        options.setEnableShutdownHook(false);
        // Manual session lifecycle (started in start(), ended on JVM shutdown); random per-launch id, no
        // user/IP attached, not linked across sessions.
        options.setEnableAutoSessionTracking(false);
        // Performance: sample a small fraction of traced operations. Timing data only — no personal data.
        options.setTracesSampleRate(0.05D);
        return options;
    }

    /**
     * Builds non-global scopes bound to their own {@link SentryClient}: nothing here reads or writes the
     * static {@code Sentry} hub, so another mod's {@code Sentry.init} cannot replace or silence Core's
     * client. Package-private for tests.
     */
    static IScopes isolatedScopes(SentryOptions options, String creator) {
        // What Sentry.init would do for the options: start the executors it uses for lazy work and timers.
        options.activate();
        Scopes own = new Scopes(new Scope(options), new Scope(options), new Scope(options), creator);
        own.bindClient(new SentryClient(options));
        if (options.isEnableUncaughtExceptionHandler()) {
            // Chains to the previous default handler (each mod's own global scope keeps the chain intact),
            // and the beforeSend gate keeps only events that touch this mod's package.
            // Kept for the life of the JVM: it is the process-wide handler, so it is never closed.
            uncaughtHandler = new UncaughtExceptionHandlerIntegration();
            uncaughtHandler.register(own, options);
        }
        return own;
    }

    /** Formats a config value as a short, stable tag value. */
    private static String fmt(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    /**
     * The running Minecraft version (e.g. "26.1.2"), read from the loader's mod list (where "minecraft"
     * is always present). "unknown" if unavailable.
     */
    private static String minecraftVersion() {
        try {
            for (String mod : Services.PLATFORM.getLoadedModIds()) {
                if (mod.startsWith("minecraft ")) {
                    return mod.substring("minecraft ".length());
                }
            }
        } catch (RuntimeException | LinkageError e) {
            // fall through to unknown
        }
        return "unknown";
    }

    /** Maps the mod version's release channel to a Sentry environment. */
    private static String environmentOf(String version) {
        String v = version.toLowerCase(Locale.ROOT);
        if (v.contains("-alpha")) {
            return "alpha";
        }
        if (v.contains("-beta")) {
            return "beta";
        }
        return "production";
    }

    static boolean isActive() {
        return active;
    }

    /** True if any frame of the throwable (or its causes/suppressed) is Neroland Core code. */
    static boolean touchesNeroland(Throwable t) {
        int depth = 0;
        while (t != null && depth++ < 16) {
            for (StackTraceElement el : t.getStackTrace()) {
                if (el.getClassName().startsWith(PACKAGE_MARKER)) {
                    return true;
                }
            }
            for (Throwable s : t.getSuppressed()) {
                if (touchesNeroland(s)) {
                    return true;
                }
            }
            t = t.getCause();
        }
        return false;
    }

    /** Capture an exception already known to touch Neroland Core code. */
    static void capture(Throwable t) {
        IScopes s = scopes;
        if (!active || s == null || t == null) {
            return;
        }
        s.captureException(t);
    }

    /** Capture a handled exception if it is still clearly from Neroland Core code. */
    public static void captureHandledException(Throwable t) {
        if (t != null && touchesNeroland(t)) {
            capture(t);
        }
    }

    /** Capture a handled exception with a non-identifying source label for triage. */
    public static void captureHandledException(Throwable t, String source, String operation) {
        captureHandledException(t, source, operation, false);
    }

    /**
     * Capture a handled exception with a non-identifying source label for triage.
     *
     * @param recovered {@code true} when the guard fully recovered (for example saved data restored from
     *                  its backup): reported at WARNING instead of ERROR, so it stays out of the crash queue
     */
    public static void captureHandledException(Throwable t, String source, String operation, boolean recovered) {
        IScopes s = scopes;
        if (!active || s == null || t == null || !touchesNeroland(t)) {
            return;
        }
        // The (Throwable, ScopeCallback) overload: with Sentry 8.x, tags set inside withScope(...) do not
        // reliably reach an event captured there.
        s.captureException(t, scope -> {
            scope.setLevel(recovered ? SentryLevel.WARNING : SentryLevel.ERROR);
            scope.setTag("handled", "true");
            scope.setTag("source", source);
            scope.setExtra("operation", operation);
            scope.setFingerprint(List.of("nerolandcore-handled", String.valueOf(source), String.valueOf(operation)));
        });
    }

    /** Capture a (scrubbed, truncated) FATAL log line that names Neroland Core without a throwable. */
    static void captureMessage(String message) {
        IScopes s = scopes;
        if (!active || s == null) {
            return;
        }
        String scrubbed = scrub(message);
        if (scrubbed.length() > 4000) {
            scrubbed = scrubbed.substring(0, 4000) + "…[truncated]";
        }
        SentryEvent event = new SentryEvent();
        event.setLevel(SentryLevel.FATAL);
        Message msg = new Message();
        msg.setFormatted(scrubbed);
        event.setMessage(msg);
        s.captureEvent(event);
    }

    /**
     * The single privacy/noise gate every outgoing event passes through: keep only Neroland-related
     * events, de-duplicate, rate-limit, and scrub PII. Returning {@code null} drops the event.
     */
    private static SentryEvent filterAndScrub(SentryEvent event) {
        if (!isNerolandRelated(event)) {
            return null;
        }
        String fingerprint = fingerprintOf(event);
        if (!seenFingerprints.add(fingerprint)) {
            return null; // already reported this session
        }
        if (eventsSent.incrementAndGet() > MAX_EVENTS_PER_SESSION) {
            return null;
        }
        // POPIA/GDPR scrubbing: no user identity, no hostname, no OS-account names in paths.
        event.setUser(null);
        event.setServerName(null);
        List<SentryException> scrubExceptions = event.getExceptions();
        if (scrubExceptions != null) {
            for (SentryException ex : scrubExceptions) {
                String value = ex.getValue();
                if (value != null) {
                    ex.setValue(scrub(value));
                }
                SentryStackTrace st = ex.getStacktrace();
                List<SentryStackFrame> frames = st == null ? null : st.getFrames();
                if (frames != null) {
                    for (SentryStackFrame frame : frames) {
                        frame.setAbsPath(null);
                    }
                }
            }
        }
        Message message = event.getMessage();
        if (message != null && message.getFormatted() != null) {
            message.setFormatted(scrub(message.getFormatted()));
        }
        return event;
    }

    private static boolean isNerolandRelated(SentryEvent event) {
        Throwable t = event.getThrowable();
        if (t != null && touchesNeroland(t)) {
            return true;
        }
        List<SentryException> exceptions = event.getExceptions();
        if (exceptions != null) {
            for (SentryException ex : exceptions) {
                SentryStackTrace st = ex.getStacktrace();
                List<SentryStackFrame> frames = st == null ? null : st.getFrames();
                if (frames == null) {
                    continue;
                }
                for (SentryStackFrame frame : frames) {
                    String module = frame.getModule();
                    if (module != null && module.startsWith(PACKAGE_MARKER)) {
                        return true;
                    }
                }
            }
        }
        Message message = event.getMessage();
        String formatted = message == null ? null : message.getFormatted();
        return formatted != null && formatted.contains(PACKAGE_MARKER);
    }

    private static String fingerprintOf(SentryEvent event) {
        StringBuilder sb = new StringBuilder();
        List<SentryException> exceptions = event.getExceptions();
        Message message = event.getMessage();
        if (exceptions != null) {
            for (SentryException ex : exceptions) {
                sb.append(ex.getType()).append('|');
                SentryStackTrace st = ex.getStacktrace();
                List<SentryStackFrame> frames = st == null ? null : st.getFrames();
                if (frames != null) {
                    for (SentryStackFrame frame : frames) {
                        String module = frame.getModule();
                        if (module != null && module.startsWith(PACKAGE_MARKER)) {
                            sb.append(module).append(':').append(frame.getLineno()).append('|');
                        }
                    }
                }
            }
        } else if (message != null) {
            String formatted = message.getFormatted();
            if (formatted != null) {
                sb.append(formatted, 0, Math.min(200, formatted.length()));
            }
        }
        return sb.toString();
    }

    /** Replaces home-directory paths (which contain the OS account name) with a neutral marker. */
    static String scrub(String text) {
        return USER_PATH.matcher(text).replaceAll("/~");
    }
}
