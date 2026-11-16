package za.co.neroland.nerolandcore.map;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.event.ListenerGuard;

/**
 * "This layer changed here" notifications (Core 1.19.0), so a map consumer refreshes without polling.
 * Providers fire; a consumer (NeroMaps) listens. With no listener registered a fire costs one empty-list
 * iteration. Dispatch goes through {@code ListenerGuard}: a throwing listener is logged once per class and
 * skipped.
 *
 * <p>A {@code LayerChanged} is a server-side hint. A consumer never forwards it, its bounds or its timing to
 * a client or the app; it re-queries per viewer and sends that viewer only the resulting difference, and
 * nothing when there is none. When {@code audience} is set, only that viewer is re-queried. Consumers
 * coalesce: many events for one layer in a tick cost one re-query. Providers do not fire for things that
 * merely move (companions, creatures); the scheduled refresh picks those up. Fire on the server thread;
 * listeners only mark state dirty.
 *
 * <p>Events carry no content and are never logged.
 */
public final class MapEvents {

    /**
     * One change hint.
     *
     * @param layer     the layer that changed
     * @param dimension where
     * @param area      the region that changed, or {@code null} for anywhere in the dimension
     * @param audience  the single viewer whose view changed (for example a member who was removed), or
     *                  {@code null} when any viewer may be affected
     */
    public record LayerChanged(Identifier layer, ResourceKey<Level> dimension, @Nullable MapBounds area,
            @Nullable UUID audience) {

        public LayerChanged {
            if (layer == null || dimension == null) {
                throw new IllegalArgumentException("A layer change needs a layer and a dimension");
            }
        }

        /** Omits the audience: a UUID is personal data. */
        @Override
        public String toString() {
            return "LayerChanged[layer=" + layer + ", dimension=" + dimension + ", area=" + area
                    + (audience == null ? ", audience=any]" : ", audience=one]");
        }
    }

    private static final List<Consumer<LayerChanged>> LISTENERS = new CopyOnWriteArrayList<>();

    private MapEvents() {
    }

    /** Register a listener (runs on the server thread; mark state dirty only). */
    public static void onLayerChanged(Consumer<LayerChanged> listener) {
        if (listener != null) {
            LISTENERS.add(listener);
        }
    }

    /** Remove a listener; {@code true} if it was registered. */
    public static boolean removeListener(Consumer<LayerChanged> listener) {
        return LISTENERS.remove(listener);
    }

    /** Publish a change. Server thread only. */
    public static void fire(LayerChanged change) {
        if (change == null || LISTENERS.isEmpty()) {
            return;
        }
        ListenerGuard.dispatch("MapEvents", LISTENERS, change);
    }

    /** Publish a change any viewer may see. {@code area} {@code null} means anywhere in the dimension. */
    public static void fire(Identifier layer, ResourceKey<Level> dimension, @Nullable MapBounds area) {
        fire(new LayerChanged(layer, dimension, area, null));
    }

    /** Publish a change that affects only {@code audience}'s view (membership removal is the main use). */
    public static void fireFor(UUID audience, Identifier layer, ResourceKey<Level> dimension,
            @Nullable MapBounds area) {
        if (audience == null) {
            throw new IllegalArgumentException("fireFor needs an audience; use fire(..) for any viewer");
        }
        fire(new LayerChanged(layer, dimension, area, audience));
    }
}
