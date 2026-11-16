package za.co.neroland.nerolandcore.map;

import java.util.List;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * The map-layer SPI a Nero mod implements and registers with {@link MapLayerRegistry#register} during common
 * init (Core 1.19.0). Core renders nothing, stores nothing and never calls a provider on its own initiative:
 * a map consumer (NeroMaps) calls through {@link MapLayerRegistry#query}, and the test-time
 * {@link MapLayerConformance} fixture is the only other caller. On a server with no consumer, registering
 * costs one map entry.
 *
 * <p><b>Scoping rule.</b> {@code query} returns only what <i>this viewer</i> is entitled to see, and every
 * entry in a layer follows that layer's single declared {@link MapScope}. Where the mod records an owner UUID
 * or an access/member list, that is the whole rule: return only entries the viewer owns or is listed on.
 * <b>Proximity never applies to anything that has an owner other than the viewer</b>; it is only for records
 * with no owner field at all. For such records, measure proximity from the viewer's own <b>server-side</b>
 * position ({@code viewer.within(...)}), never from the requested {@code bounds}: same dimension, within the
 * layer's {@code proximityRadius} (64 blocks for player-built things, 128 for world hazards), and nothing at
 * all when the viewer is offline. Bounds only narrow a result; they never widen it. World features (ruins,
 * lairs, outposts) are returned only once <b>this viewer</b> has discovered them. Never return a server-wide
 * roster. Never use {@code getNearestPlayer} or any nearest-player lookup to decide ownership or visibility.
 * No marker id, label, detail line or colour may carry another player's name, UUID or online state; owner
 * text appears only on the viewer's own things.
 *
 * <p>A direction-only target (for example an expedition target the in-game tool shows only as a compass point
 * and a range band) is a marker placed a fixed 96 blocks from the viewer along the centre of the direction
 * sector the in-game tool reports, with the band as a detail; a target in the viewer's own spot is drawn at the
 * viewer. Never the true bearing or distance: the true position never leaves the provider.
 *
 * <p><b>Cost.</b> Called on the server thread by a consumer, at most once per viewer per layer refresh. Read
 * existing in-memory state only: never load or generate a chunk, never touch disk, never block. Stop as soon
 * as {@code sink.isFull(layer)}. A dimension the mod knows nothing about returns nothing. Exceptions are caught
 * by {@link MapLayerRegistry#query}; the whole provider is skipped for that query.
 *
 * <p>Test every provider with {@link MapLayerConformance}.
 */
public interface MapLayerProvider {

    /** The providing mod's id; every layer id's namespace must equal it. */
    String modId();

    /**
     * The layers this provider offers (at most {@link MapLayerRegistry#MAX_LAYERS_PER_PROVIDER}). Read once,
     * at registration; later changes to the returned list are ignored.
     */
    List<MapLayerInfo> layers();

    /**
     * Write what {@code viewer} may see in {@code dimension} within {@code bounds} into {@code sink}. See the
     * scoping rule above.
     */
    void query(MapViewer viewer, ResourceKey<Level> dimension, MapBounds bounds, MapLayerSink sink);

    /**
     * Positionless per-dimension badges for {@code viewer} (storms, visited worlds), under the same scoping
     * rule. At most {@link MapDimensionBadge#MAX_PER_PROVIDER}. Default: none.
     */
    default List<MapDimensionBadge> dimensionBadges(MapViewer viewer) {
        return List.of();
    }
}
