package za.co.neroland.nerolandcore.map;

/**
 * How every marker and area in one map layer is scoped (Core 1.19.0). <b>One scope per layer</b>: a layer
 * must not mix owner/member entries with proximity entries; such a layer is split into two layer ids (for
 * example {@code nerospace:launch_pads} for the viewer's own and access-list pads and
 * {@code nerospace:pads_nearby} for unowned pads by proximity).
 *
 * <p>The scope is a declaration, not a permission: the provider still decides per viewer, and the
 * {@link MapLayerConformance} fixture checks each returned item against the declared scope, so a
 * mislabelled mixed layer fails.
 */
public enum MapScope {

    /** The viewer's own records only. */
    OWNER,

    /**
     * The record's owner plus its access or member list (stations, colonies, security installations).
     * The provider may narrow individual entries by the viewer's role.
     */
    MEMBERS,

    /**
     * Only records with <b>no owner field at all</b>: same dimension as the viewer, within the layer's
     * {@link MapLayerInfo#proximityRadius()} of the viewer's own server-side position, and nothing when
     * the viewer is offline. Measured with {@link MapViewer#within}, never from the requested bounds.
     *
     * <p>An area is in range when the nearest point of its shape's {@link MapArea#bounds()} is within
     * {@code proximityRadius} of the viewer ({@link MapViewer#within(net.minecraft.resources.ResourceKey,
     * MapBounds, int)}); the whole area may then be returned.
     *
     * <p>Proximity results are <b>transient</b>: consumers never persist them (no client disk cache, no
     * world-map retention after the viewer leaves range) and serve them to a companion app only while the
     * requester is online and in range.
     */
    PROXIMITY,

    /** World features (ruins, lairs, outposts) returned only once <b>this viewer</b> has discovered them. */
    DISCOVERED,

    /**
     * Identical for every viewer, including offline ones (for example live public event areas). The only
     * scope exempt from the "a stranger sees nothing" check.
     */
    PUBLIC
}
