package za.co.neroland.nerolandcore.link;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * The central, static registry every Nero mod uses to plug into NeroLink, and the one
 * surface the NeroLink Bridge reads from. Core ships only this SPI, the
 * {@linkplain #eventBus() event bus} and the {@link LinkAlerts} service — it registers
 * <em>no</em> {@code core} module itself; the bridge provides {@code core}'s
 * energy/storage/gates/alerts providers directly from Core's capabilities, so a
 * Core-only server is fully functional.
 *
 * <p>Mods register a {@link LinkSnapshotProvider} and/or a {@link LinkActionHandler}
 * (each declaring its {@link LinkModuleInfo} for discovery) during common init. The
 * bridge then looks up providers/handlers by module id, enumerates {@link #modules()}
 * for its discovery response, and subscribes to the shared {@link LinkEventBus} for WS
 * deltas and notifications.
 *
 * <p>Thread-safe: backing maps are {@link ConcurrentHashMap}s, matching Core's
 * facade+registry conventions. Registration is expected once per module at init.
 */
public final class NeroLinkRegistry {

    private static final Map<String, LinkModuleInfo> MODULES = new ConcurrentHashMap<>();
    private static final Map<String, LinkSnapshotProvider> SNAPSHOT_PROVIDERS = new ConcurrentHashMap<>();
    private static final Map<String, LinkActionHandler> ACTION_HANDLERS = new ConcurrentHashMap<>();
    private static final LinkEventBus EVENT_BUS = new LinkEventBus();

    private NeroLinkRegistry() {
    }

    /**
     * Register a module's read-side provider plus its discovery metadata. A second
     * registration for the same module id replaces the first (and logs a warning); this
     * lets a mod ship a single provider covering all its sections.
     *
     * @param provider the snapshot provider (its {@code moduleId} keys the registry)
     * @param info     the module's discovery metadata; its {@code moduleId} must match
     *                 {@code provider.moduleId()}
     */
    public static void registerSnapshotProvider(LinkSnapshotProvider provider, LinkModuleInfo info) {
        requireMatch(provider.moduleId(), info.moduleId(), "snapshot provider");
        if (SNAPSHOT_PROVIDERS.put(provider.moduleId(), provider) != null) {
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] NeroLink snapshot provider for module '{}' replaced.",
                    provider.moduleId());
        }
        MODULES.put(info.moduleId(), info);
        NerolandCoreCommon.LOGGER.info("[Neroland Core] NeroLink module '{}' registered a snapshot provider "
                + "(schema {}, {} section(s)).", info.moduleId(), info.schemaVersion(), info.dataSections().size());
    }

    /**
     * Register a module's write-side handler plus its discovery metadata. A second
     * registration for the same module id replaces the first (and logs a warning).
     *
     * @param handler the action handler (its {@code moduleId} keys the registry)
     * @param info    the module's discovery metadata; its {@code moduleId} must match
     *                {@code handler.moduleId()}
     */
    public static void registerActionHandler(LinkActionHandler handler, LinkModuleInfo info) {
        requireMatch(handler.moduleId(), info.moduleId(), "action handler");
        if (ACTION_HANDLERS.put(handler.moduleId(), handler) != null) {
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] NeroLink action handler for module '{}' replaced.",
                    handler.moduleId());
        }
        MODULES.put(info.moduleId(), info);
        NerolandCoreCommon.LOGGER.info("[Neroland Core] NeroLink module '{}' registered an action handler "
                + "({} action(s)).", info.moduleId(), info.actionIds().size());
    }

    /** The snapshot provider for {@code moduleId}, if one is registered. */
    public static Optional<LinkSnapshotProvider> snapshotProvider(String moduleId) {
        return Optional.ofNullable(SNAPSHOT_PROVIDERS.get(moduleId));
    }

    /** The action handler for {@code moduleId}, if one is registered. */
    public static Optional<LinkActionHandler> actionHandler(String moduleId) {
        return Optional.ofNullable(ACTION_HANDLERS.get(moduleId));
    }

    /** Discovery metadata for {@code moduleId}, if the module is present. */
    public static Optional<LinkModuleInfo> module(String moduleId) {
        return Optional.ofNullable(MODULES.get(moduleId));
    }

    /**
     * Every registered module's discovery metadata, for the bridge's discovery
     * endpoint. A snapshot copy — mutating the returned list does not affect the
     * registry.
     */
    public static List<LinkModuleInfo> modules() {
        return new ArrayList<>(MODULES.values());
    }

    /** The shared event bus for live deltas and notifications. */
    public static LinkEventBus eventBus() {
        return EVENT_BUS;
    }

    private static void requireMatch(String providerModuleId, String infoModuleId, String what) {
        if (!providerModuleId.equals(infoModuleId)) {
            throw new IllegalArgumentException("NeroLink " + what + " module id '" + providerModuleId
                    + "' does not match its LinkModuleInfo module id '" + infoModuleId + "'.");
        }
    }

    /**
     * Test/reload support: drop all registrations. Not part of the stable API — the
     * bridge and mods never call this in production.
     */
    @org.jetbrains.annotations.ApiStatus.Internal
    static void clearForTest() {
        MODULES.clear();
        SNAPSHOT_PROVIDERS.clear();
        ACTION_HANDLERS.clear();
    }

    /**
     * A defensive, insertion-ordered copy of the module map — internal helper for
     * tooling/tests that want a stable snapshot of the registry.
     */
    @org.jetbrains.annotations.ApiStatus.Internal
    static Map<String, LinkModuleInfo> moduleMapSnapshot() {
        return new LinkedHashMap<>(MODULES);
    }
}
