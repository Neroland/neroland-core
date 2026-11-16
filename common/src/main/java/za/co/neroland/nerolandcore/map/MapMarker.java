package za.co.neroland.nerolandcore.map;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

/**
 * One point on a map layer (Core 1.19.0). Block coordinates.
 *
 * <p>Validation: {@code id} must match {@link #ID_PATTERN}, and {@code layer} and {@code dimension} must be
 * non-null ({@link IllegalArgumentException}: a programmer error). {@code details} are data-derived: null
 * entries and anything beyond {@link #MAX_DETAILS} are dropped.
 *
 * <p><b>Privacy.</b> No id, label, detail or colour may carry another player's name, UUID or online state.
 *
 * @param id        stable within the layer, so consumers can diff refreshes
 * @param layer     the layer this marker belongs to
 * @param dimension the dimension it is in
 * @param x         block x
 * @param y         block y (informational; maps are horizontal)
 * @param z         block z
 * @param icon      icon id, or {@code null} for the layer's icon
 * @param label     label, or {@code null}
 * @param argb      tint colour, or {@code 0} for the consumer's default
 * @param details   up to {@link #MAX_DETAILS} extra lines for a tooltip
 */
public record MapMarker(String id, Identifier layer, ResourceKey<Level> dimension, int x, int y, int z,
        @Nullable Identifier icon, @Nullable MapLabel label, int argb, List<MapLabel> details) {

    /** Marker and area ids. */
    public static final String ID_PATTERN = "[a-z0-9_.:/-]{1,64}";
    /** Most detail lines a marker carries; extra lines are dropped. */
    public static final int MAX_DETAILS = 4;

    static final Pattern ID = Pattern.compile(ID_PATTERN);

    public MapMarker {
        requireItem(id, layer, dimension, "marker");
        List<MapLabel> clean = new ArrayList<>();
        if (details != null) {
            for (MapLabel detail : details) {
                if (detail != null && clean.size() < MAX_DETAILS) {
                    clean.add(detail);
                }
            }
        }
        details = List.copyOf(clean);
    }

    static void requireItem(String id, Identifier layer, ResourceKey<Level> dimension, String what) {
        if (id == null || !ID.matcher(id).matches()) {
            throw new IllegalArgumentException("A map " + what + " id must match " + ID_PATTERN);
        }
        if (layer == null || dimension == null) {
            throw new IllegalArgumentException("A map " + what + " needs a layer and a dimension");
        }
    }

    /** Start a marker at {@code pos}. */
    public static Builder builder(String id, Identifier layer, ResourceKey<Level> dimension, BlockPos pos) {
        return new Builder(id, layer, dimension, pos.getX(), pos.getY(), pos.getZ());
    }

    /** Start a marker at {@code (x, y, z)}. */
    public static Builder builder(String id, Identifier layer, ResourceKey<Level> dimension, int x, int y, int z) {
        return new Builder(id, layer, dimension, x, y, z);
    }

    /** Fluent construction for the optional parts. */
    public static final class Builder {
        private final String id;
        private final Identifier layer;
        private final ResourceKey<Level> dimension;
        private final int x;
        private final int y;
        private final int z;
        private @Nullable Identifier icon;
        private @Nullable MapLabel label;
        private int argb;
        private final List<MapLabel> details = new ArrayList<>();

        private Builder(String id, Identifier layer, ResourceKey<Level> dimension, int x, int y, int z) {
            this.id = id;
            this.layer = layer;
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /** Icon id; {@code null} uses the layer's icon. */
        public Builder icon(@Nullable Identifier newIcon) {
            this.icon = newIcon;
            return this;
        }

        /** Label; {@code null} for none. */
        public Builder label(@Nullable MapLabel newLabel) {
            this.label = newLabel;
            return this;
        }

        /** Tint colour as ARGB; {@code 0} for the consumer's default. */
        public Builder argb(int newArgb) {
            this.argb = newArgb;
            return this;
        }

        /** Add a detail line (beyond {@link #MAX_DETAILS} it is dropped). */
        public Builder detail(MapLabel detail) {
            this.details.add(detail);
            return this;
        }

        /** Build the marker; throws {@link IllegalArgumentException} for a bad id, layer or dimension. */
        public MapMarker build() {
            return new MapMarker(id, layer, dimension, x, y, z, icon, label, argb, details);
        }
    }
}
