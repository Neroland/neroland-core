package za.co.neroland.nerolandcore.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.ApiStatus;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.event.ListenerGuard;
import za.co.neroland.nerolandcore.event.SourceIds;

/**
 * The static registry of map-layer providers (Core 1.19.0), and the <b>only</b> way a consumer runs one.
 * Mirrors {@code NeroLinkRegistry}: keyed by mod id, a second registration replaces the first with a WARN,
 * one INFO line per registration, thread-safe maps.
 *
 * <p>{@link #register} snapshots {@code provider.layers()} once; {@link #layers()}, {@link #layer} and
 * {@link #query} use that snapshot and never call {@code layers()} again. {@link #query} builds a sink from
 * that provider's layers only, catches a {@link RuntimeException} (reported through {@code ListenerGuard}:
 * class and top frame, once per class), discards partial output, seals the sink and returns copies.
 *
 * <p>Core never calls {@link #query} itself. There is deliberately no {@code unregister}: one mod must not
 * be able to drop another's layers.
 */
public final class MapLayerRegistry {

    /** Most layers one provider may declare. */
    public static final int MAX_LAYERS_PER_PROVIDER = 16;

    private static final Map<String, Registered> PROVIDERS = new ConcurrentHashMap<>();

    private MapLayerRegistry() {
    }

    private record Registered(MapLayerProvider provider, List<MapLayerInfo> layers) {
    }

    /**
     * Register {@code provider} under its {@link MapLayerProvider#modId()}. Throws
     * {@link IllegalArgumentException} when the mod id does not match {@code [a-z0-9_.-]{1,64}}, when it
     * declares more than {@link #MAX_LAYERS_PER_PROVIDER} layers, when a layer's namespace is not the mod
     * id, or when two layers share an id. Re-registering a mod id replaces the earlier provider (WARN).
     */
    public static void register(MapLayerProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Cannot register a null map layer provider");
        }
        List<MapLayerInfo> layers = validate(provider);
        String modId = provider.modId();
        if (PROVIDERS.put(modId, new Registered(provider, layers)) != null) {
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Map layer provider for '{}' replaced.", modId);
        }
        NerolandCoreCommon.LOGGER.info("[Neroland Core] Map layer provider '{}' registered ({} layer(s)).", modId,
                layers.size());
    }

    /** The provider registered for {@code modId}, if any. */
    public static Optional<MapLayerProvider> provider(String modId) {
        Registered r = modId == null ? null : PROVIDERS.get(modId);
        return r == null ? Optional.empty() : Optional.of(r.provider());
    }

    /** Every registered provider; a snapshot copy. */
    public static List<MapLayerProvider> providers() {
        List<MapLayerProvider> out = new ArrayList<>();
        for (Registered r : PROVIDERS.values()) {
            out.add(r.provider());
        }
        return out;
    }

    /** Every registered layer, from the registration snapshots; a snapshot copy. */
    public static List<MapLayerInfo> layers() {
        List<MapLayerInfo> out = new ArrayList<>();
        for (Registered r : PROVIDERS.values()) {
            out.addAll(r.layers());
        }
        return out;
    }

    /** The registered layer with {@code id}, if any. */
    public static Optional<MapLayerInfo> layer(Identifier id) {
        if (id == null) {
            return Optional.empty();
        }
        Registered r = PROVIDERS.get(id.getNamespace());
        if (r == null) {
            return Optional.empty();
        }
        for (MapLayerInfo info : r.layers()) {
            if (info.id().equals(id)) {
                return Optional.of(info);
            }
        }
        return Optional.empty();
    }

    /**
     * Run {@code modId}'s provider for {@code viewer} (server thread). Unknown mod id or a null argument
     * returns {@link MapQueryResult#empty()}. {@code bounds} wider than {@link MapBounds#MAX_SPAN} are
     * shrunk about their centre. A provider that throws yields a {@link MapQueryResult#failed() failed}
     * result with nothing in it.
     */
    public static MapQueryResult query(String modId, MapViewer viewer, ResourceKey<Level> dimension,
            MapBounds bounds) {
        Registered r = modId == null ? null : PROVIDERS.get(modId);
        if (r == null || viewer == null || dimension == null || bounds == null) {
            return MapQueryResult.empty();
        }
        return run(r.provider(), r.layers(), viewer, dimension, bounds);
    }

    /**
     * {@code modId}'s positionless dimension badges for {@code viewer}, guarded like {@link #query}: a
     * throwing provider yields none, badges whose id namespace is not {@code modId} are dropped, duplicates
     * by id are dropped, and at most {@link MapDimensionBadge#MAX_PER_PROVIDER} are kept.
     */
    public static List<MapDimensionBadge> dimensionBadges(String modId, MapViewer viewer) {
        Registered r = modId == null ? null : PROVIDERS.get(modId);
        if (r == null || viewer == null) {
            return List.of();
        }
        return badges(r.provider(), viewer);
    }

    // ---- shared with MapLayerConformance ------------------------------------------------------------

    static List<MapLayerInfo> validate(MapLayerProvider provider) {
        String modId = provider.modId();
        if (!SourceIds.isValidSourceModId(modId)) {
            throw new IllegalArgumentException("A map layer provider's mod id must match [a-z0-9_.-]{1,64}");
        }
        List<MapLayerInfo> declared = provider.layers();
        List<MapLayerInfo> layers = declared == null ? List.of() : List.copyOf(declared);
        if (layers.size() > MAX_LAYERS_PER_PROVIDER) {
            throw new IllegalArgumentException("Map layer provider '" + modId + "' declares more than "
                    + MAX_LAYERS_PER_PROVIDER + " layers");
        }
        Set<Identifier> seen = new HashSet<>();
        for (MapLayerInfo info : layers) {
            if (!modId.equals(info.modId())) {
                throw new IllegalArgumentException("Map layer '" + info.id() + "' is not in namespace '" + modId + "'");
            }
            if (!seen.add(info.id())) {
                throw new IllegalArgumentException("Map layer '" + info.id() + "' is declared twice");
            }
        }
        return layers;
    }

    static MapQueryResult run(MapLayerProvider provider, List<MapLayerInfo> layers, MapViewer viewer,
            ResourceKey<Level> dimension, MapBounds bounds) {
        MapBounds clamped = bounds.clampSpan();
        MapLayerSink sink = MapLayerSink.create(layers, dimension, clamped);
        long start = System.nanoTime();
        try {
            provider.query(viewer, dimension, clamped, sink);
        } catch (RuntimeException failure) {
            sink.seal();
            ListenerGuard.report("MapLayerProvider", provider, failure);
            return MapQueryResult.failure(System.nanoTime() - start);
        } finally {
            sink.seal();
        }
        return sink.result(System.nanoTime() - start);
    }

    static List<MapDimensionBadge> badges(MapLayerProvider provider, MapViewer viewer) {
        List<MapDimensionBadge> raw;
        try {
            raw = provider.dimensionBadges(viewer);
        } catch (RuntimeException failure) {
            ListenerGuard.report("MapLayerProvider", provider, failure);
            return List.of();
        }
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        Map<Identifier, MapDimensionBadge> kept = new LinkedHashMap<>();
        for (MapDimensionBadge badge : raw) {
            if (kept.size() == MapDimensionBadge.MAX_PER_PROVIDER) {
                break;
            }
            if (badge != null && provider.modId().equals(badge.id().getNamespace())) {
                kept.putIfAbsent(badge.id(), badge);
            }
        }
        return List.copyOf(kept.values());
    }

    /** Test support: drop every registration. Never called in production. */
    @ApiStatus.Internal
    static void clearForTest() {
        PROVIDERS.clear();
    }
}
