package za.co.neroland.nerolandcore.map;

import net.minecraft.resources.Identifier;

/**
 * Declares one map layer a {@link MapLayerProvider} offers (Core 1.19.0). The owning mod id is the layer
 * id's namespace. Use the factories ({@link #of}, {@link #proximityBuilt}, {@link #proximityHazard}) and
 * the {@code with*} copies; the canonical constructor is there for completeness.
 *
 * <p>Validation (all {@link IllegalArgumentException}): {@code id}, {@code icon} and {@code scope}
 * non-null; {@code titleKey} matches {@code [a-z0-9_.-]{1,128}}; a {@link MapScope#PROXIMITY} layer has
 * {@code 1 <= proximityRadius <= MAX_PROXIMITY} and every other scope has {@code proximityRadius == 0};
 * {@code schema >= 1}. {@code maxPerQuery <= 0} means {@link #DEFAULT_CAP}; the cap is then clamped to
 * {@code [1, HARD_CAP]}.
 *
 * @param id               layer id, e.g. {@code nerospace:stations}; its namespace is the owning mod id
 * @param titleKey         lang key for the layer panel, e.g. {@code map.layer.nerospace.stations}
 * @param icon             icon id resolved by the consumer's atlas, with a generic fallback; Core ships no art
 * @param defaultVisible   shown on the world map until the player turns it off
 * @param defaultOnMinimap shown on the minimap until the player turns it off ({@code false} for proximity
 *                         layers of player-built things)
 * @param scope            the single scope every entry in this layer follows
 * @param proximityRadius  {@link MapScope#PROXIMITY} only: the radius in blocks; otherwise 0
 * @param schema           bumped when the meaning of this layer's markers changes; at least 1
 * @param maxPerQuery      the per-query cap on markers plus areas for this layer
 */
public record MapLayerInfo(Identifier id, String titleKey, Identifier icon, boolean defaultVisible,
        boolean defaultOnMinimap, MapScope scope, int proximityRadius, int schema, int maxPerQuery) {

    /** No layer may return more items than this per query. */
    public static final int HARD_CAP = 512;
    /** The cap when none is given. */
    public static final int DEFAULT_CAP = 128;
    /** Proximity radius for player-built things with no owner. */
    public static final int BUILT_PROXIMITY = 64;
    /** Proximity radius for world hazards (scorch zones and the like). */
    public static final int HAZARD_PROXIMITY = 128;
    /** The widest proximity radius any layer may declare. */
    public static final int MAX_PROXIMITY = 128;

    public MapLayerInfo {
        if (id == null || icon == null || scope == null) {
            throw new IllegalArgumentException("A map layer needs an id, an icon and a scope");
        }
        MapLabel.requireKey(titleKey, "map layer title key");
        if (scope == MapScope.PROXIMITY) {
            if (proximityRadius < 1 || proximityRadius > MAX_PROXIMITY) {
                throw new IllegalArgumentException("A proximity layer's radius must be 1.." + MAX_PROXIMITY);
            }
        } else if (proximityRadius != 0) {
            throw new IllegalArgumentException("Only a proximity layer declares a proximity radius");
        }
        if (schema < 1) {
            throw new IllegalArgumentException("A map layer's schema starts at 1");
        }
        maxPerQuery = Math.max(1, Math.min(HARD_CAP, maxPerQuery <= 0 ? DEFAULT_CAP : maxPerQuery));
    }

    /** The owning mod id: the layer id's namespace. */
    public String modId() {
        return id.getNamespace();
    }

    /**
     * A non-proximity layer, visible on the world map and the minimap, schema 1, {@link #DEFAULT_CAP}.
     * Use {@link #proximityBuilt} or {@link #proximityHazard} for {@link MapScope#PROXIMITY}.
     */
    public static MapLayerInfo of(Identifier id, String titleKey, Identifier icon, MapScope scope) {
        if (scope == MapScope.PROXIMITY) {
            throw new IllegalArgumentException("Use proximityBuilt(..) or proximityHazard(..) for a proximity layer");
        }
        return new MapLayerInfo(id, titleKey, icon, true, true, scope, 0, 1, DEFAULT_CAP);
    }

    /** A proximity layer for player-built things with no owner: {@link #BUILT_PROXIMITY}, minimap off. */
    public static MapLayerInfo proximityBuilt(Identifier id, String titleKey, Identifier icon) {
        return new MapLayerInfo(id, titleKey, icon, true, false, MapScope.PROXIMITY, BUILT_PROXIMITY, 1, DEFAULT_CAP);
    }

    /** A proximity layer for world hazards: {@link #HAZARD_PROXIMITY}, minimap on. */
    public static MapLayerInfo proximityHazard(Identifier id, String titleKey, Identifier icon) {
        return new MapLayerInfo(id, titleKey, icon, true, true, MapScope.PROXIMITY, HAZARD_PROXIMITY, 1, DEFAULT_CAP);
    }

    /** A copy with another proximity radius ({@link MapScope#PROXIMITY} layers only). */
    public MapLayerInfo withProximityRadius(int radius) {
        return new MapLayerInfo(id, titleKey, icon, defaultVisible, defaultOnMinimap, scope, radius, schema,
                maxPerQuery);
    }

    /** A copy with other default visibility on the world map and the minimap. */
    public MapLayerInfo withDefaults(boolean visible, boolean onMinimap) {
        return new MapLayerInfo(id, titleKey, icon, visible, onMinimap, scope, proximityRadius, schema, maxPerQuery);
    }

    /** A copy with another per-query cap (clamped to {@code [1, HARD_CAP]}). */
    public MapLayerInfo withCap(int cap) {
        return new MapLayerInfo(id, titleKey, icon, defaultVisible, defaultOnMinimap, scope, proximityRadius, schema,
                cap);
    }

    /** A copy with another schema number. */
    public MapLayerInfo withSchema(int newSchema) {
        return new MapLayerInfo(id, titleKey, icon, defaultVisible, defaultOnMinimap, scope, proximityRadius,
                newSchema, maxPerQuery);
    }
}
