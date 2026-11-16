package za.co.neroland.nerolandcore.map;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.Identifier;

/**
 * What one {@link MapLayerRegistry#query} returned (Core 1.19.0). Every collection is an immutable copy.
 *
 * @param markers         accepted markers, in the order the provider emitted them
 * @param areas           accepted areas, in the order the provider emitted them
 * @param truncatedLayers layers that hit their cap or payload budget
 * @param rejected        refused items by reason (absent reasons are 0; counts saturate)
 * @param failed          {@code true} when the provider threw: its partial output was discarded and both
 *                        lists are empty
 * @param elapsedNanos    wall time the provider took, so a consumer can enforce its own time budget
 */
public record MapQueryResult(List<MapMarker> markers, List<MapArea> areas, Set<Identifier> truncatedLayers,
        Map<MapLayerSink.Reject, Integer> rejected, boolean failed, long elapsedNanos) {

    private static final MapQueryResult EMPTY = new MapQueryResult(List.of(), List.of(), Set.of(), Map.of(), false, 0L);

    public MapQueryResult {
        markers = markers == null ? List.of() : List.copyOf(markers);
        areas = areas == null ? List.of() : List.copyOf(areas);
        truncatedLayers = truncatedLayers == null ? Set.of() : Set.copyOf(truncatedLayers);
        rejected = rejected == null || rejected.isEmpty() ? Map.of() : Map.copyOf(new EnumMap<>(rejected));
        elapsedNanos = Math.max(0L, elapsedNanos);
    }

    /** An empty, non-failed result (unknown mod id, null argument). */
    public static MapQueryResult empty() {
        return EMPTY;
    }

    /** A failed result: nothing returned, the provider's exception already reported. */
    static MapQueryResult failure(long elapsedNanos) {
        return new MapQueryResult(List.of(), List.of(), Set.of(), Map.of(), true, elapsedNanos);
    }

    /** How many items were refused for {@code reason}. */
    public int rejected(MapLayerSink.Reject reason) {
        return rejected.getOrDefault(reason, 0);
    }
}
