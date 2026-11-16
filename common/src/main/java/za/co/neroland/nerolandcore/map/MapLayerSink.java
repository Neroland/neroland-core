package za.co.neroland.nerolandcore.map;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * The capped sink a {@link MapLayerProvider} writes into (Core 1.19.0). Built by
 * {@link MapLayerRegistry#query} from <b>that provider's registered layers only</b>, so a provider cannot
 * write into another mod's layer or spend its cap.
 *
 * <p>An item is refused (returns {@code false}, counted by {@link Reject} reason, never thrown) when its
 * layer is not one of the provider's ({@link Reject#UNKNOWN_LAYER}), it is in another dimension
 * ({@link Reject#WRONG_DIMENSION}), it lies outside the query bounds ({@link Reject#OUT_OF_BOUNDS}; an area
 * must intersect them), its id was already used in that layer by a marker or an area
 * ({@link Reject#DUPLICATE_ID}), or the layer's cap is reached ({@link Reject#CAP}). Markers and areas share
 * each layer's {@link MapLayerInfo#maxPerQuery()}; areas also share per-layer payload budgets of
 * {@link #MAX_CHUNKS_PER_LAYER} chunks and {@link #MAX_VERTICES_PER_LAYER} polygon vertices. A refused item
 * on a full layer marks that layer truncated.
 *
 * <p>A provider may leave bounds filtering to the sink. When a layer can exceed its cap, emit the items
 * nearest the viewer first, so the truncated set is stable between refreshes, and stop once
 * {@link #isFull} says so.
 *
 * <p>Once the query returns the sink is sealed: every method returns {@code false} and nothing more is
 * recorded, so a provider that keeps a reference cannot write late. Not thread-safe; one sink per query.
 */
public final class MapLayerSink {

    /** Why an item was refused. */
    public enum Reject {
        /** The item's layer is not one of this provider's registered layers. */
        UNKNOWN_LAYER,
        /** The item is in a different dimension from the query. */
        WRONG_DIMENSION,
        /** The item lies outside the query bounds. */
        OUT_OF_BOUNDS,
        /** The item's id was already used in its layer. */
        DUPLICATE_ID,
        /** The layer's item cap or payload budget is reached. */
        CAP
    }

    /** Most chunks, summed over every {@link MapArea.Chunks} area, one layer may return per query. */
    public static final int MAX_CHUNKS_PER_LAYER = 8_192;
    /** Most polygon vertices, summed over every {@link MapArea.Polygon}, one layer may return per query. */
    public static final int MAX_VERTICES_PER_LAYER = 2_048;

    private final Map<Identifier, LayerState> layers = new HashMap<>();
    private final ResourceKey<Level> dimension;
    private final MapBounds bounds;
    private final List<MapMarker> markers = new ArrayList<>();
    private final List<MapArea> areas = new ArrayList<>();
    private final Set<Identifier> truncated = new LinkedHashSet<>();
    private final EnumMap<Reject, Integer> rejected = new EnumMap<>(Reject.class);
    private boolean sealed;

    private MapLayerSink(List<MapLayerInfo> infos, ResourceKey<Level> dimension, MapBounds bounds) {
        for (MapLayerInfo info : infos) {
            layers.put(info.id(), new LayerState(info.maxPerQuery()));
        }
        this.dimension = dimension;
        this.bounds = bounds;
    }

    static MapLayerSink create(List<MapLayerInfo> layers, ResourceKey<Level> dimension, MapBounds bounds) {
        return new MapLayerSink(layers, dimension, bounds);
    }

    /** Offer a marker; {@code false} when it was refused (see the class javadoc). Never throws. */
    public boolean marker(MapMarker marker) {
        if (sealed) {
            return false;
        }
        if (marker == null) {
            return reject(Reject.UNKNOWN_LAYER);
        }
        LayerState layer = layers.get(marker.layer());
        Reject why = check(layer, marker.layer(), marker.dimension(), marker.id(),
                bounds.contains(marker.x(), marker.z()));
        if (why != null) {
            return reject(why);
        }
        layer.ids.add(marker.id());
        layer.count++;
        markers.add(marker);
        return true;
    }

    /** Offer an area; {@code false} when it was refused (see the class javadoc). Never throws. */
    public boolean area(MapArea area) {
        if (sealed) {
            return false;
        }
        if (area == null) {
            return reject(Reject.UNKNOWN_LAYER);
        }
        LayerState layer = layers.get(area.layer());
        Reject why = check(layer, area.layer(), area.dimension(), area.id(), bounds.intersects(area.bounds()));
        if (why != null) {
            return reject(why);
        }
        int chunks = area instanceof MapArea.Chunks c ? c.size() : 0;
        int vertices = area instanceof MapArea.Polygon p ? p.vertices() : 0;
        if (layer.chunks + chunks > MAX_CHUNKS_PER_LAYER || layer.vertices + vertices > MAX_VERTICES_PER_LAYER) {
            truncated.add(area.layer());
            return reject(Reject.CAP);
        }
        layer.ids.add(area.id());
        layer.count++;
        layer.chunks += chunks;
        layer.vertices += vertices;
        areas.add(area);
        return true;
    }

    /**
     * {@code true} when {@code layer} takes no more items: its cap is reached or it is not one of this
     * provider's layers. Providers stop iterating as soon as this is {@code true}. A sealed sink answers
     * {@code false} here too, like every other method; it is only sealed after the query has returned.
     */
    public boolean isFull(Identifier layer) {
        if (sealed) {
            return false;
        }
        LayerState state = layers.get(layer);
        return state == null || state.count >= state.cap;
    }

    private Reject check(LayerState layer, Identifier layerId, ResourceKey<Level> itemDimension, String id,
            boolean inBounds) {
        if (layer == null) {
            return Reject.UNKNOWN_LAYER;
        }
        if (!dimension.equals(itemDimension)) {
            return Reject.WRONG_DIMENSION;
        }
        if (!inBounds) {
            return Reject.OUT_OF_BOUNDS;
        }
        if (layer.ids.contains(id)) {
            return Reject.DUPLICATE_ID;
        }
        if (layer.count >= layer.cap) {
            truncated.add(layerId);
            return Reject.CAP;
        }
        return null;
    }

    private boolean reject(Reject why) {
        rejected.merge(why, 1, (a, b) -> a == Integer.MAX_VALUE ? a : a + b);
        return false;
    }

    void seal() {
        sealed = true;
    }

    MapQueryResult result(long elapsedNanos) {
        return new MapQueryResult(markers, areas, truncated, rejected, false, elapsedNanos);
    }

    private static final class LayerState {
        final int cap;
        final Set<String> ids = new HashSet<>();
        int count;
        int chunks;
        int vertices;

        LayerState(int cap) {
            this.cap = cap;
        }
    }
}
