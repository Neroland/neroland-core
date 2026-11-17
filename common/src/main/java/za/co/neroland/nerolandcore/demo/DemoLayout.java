package za.co.neroland.nerolandcore.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * Where everything goes in a Nero Mods Demo world (Core 1.20.0). Pure arithmetic, so the same selection
 * always produces the same layout (and the layout is unit-tested).
 *
 * <p>The hub is a square centred on {@code (0, 0)} with its floor at {@link #FLOOR_Y}; the player spawns
 * in its middle facing east. A path runs east along {@code z = 0}, and the plots sit along it in the order
 * given, each centred on the path, {@link #PLOT_GAP} blocks apart.
 */
public final class DemoLayout {

    /** Y of the hub, path and plot floors. Exhibits build from {@code FLOOR_Y + 1}. */
    public static final int FLOOR_Y = 64;

    /** The hub floor spans {@code -HUB_RADIUS..HUB_RADIUS} on both axes. */
    public static final int HUB_RADIUS = 8;

    /** Blocks of path between the hub and the first plot, and between plots. */
    public static final int PLOT_GAP = 6;

    /** The path is {@code 2 * PATH_HALF_WIDTH + 1} blocks wide, centred on {@code z = 0}. */
    public static final int PATH_HALF_WIDTH = 1;

    /** Most plots one demo world lays out. */
    public static final int MAX_PLOTS = 32;

    private DemoLayout() {
    }

    /** One module and the footprint it asked for, in plot order. */
    public record Request(String moduleId, DemoFootprint footprint) {
    }

    /**
     * One laid-out plot.
     *
     * @param moduleId the module it belongs to
     * @param index    0 for the plot nearest the hub
     * @param minX     west edge (inclusive)
     * @param minZ     north edge (inclusive)
     * @param sizeX    width along X
     * @param sizeZ    depth along Z
     */
    public record Plot(String moduleId, int index, int minX, int minZ, int sizeX, int sizeZ) {

        /** East edge (inclusive). */
        public int maxX() {
            return this.minX + this.sizeX - 1;
        }

        /** South edge (inclusive). */
        public int maxZ() {
            return this.minZ + this.sizeZ - 1;
        }
    }

    /**
     * Lays out {@code requests} in the order given. Throws {@link IllegalArgumentException} for more than
     * {@link #MAX_PLOTS} requests or a null entry.
     */
    public static List<Plot> plan(List<Request> requests) {
        if (requests == null) {
            return List.of();
        }
        if (requests.size() > MAX_PLOTS) {
            throw new IllegalArgumentException("A demo world holds at most " + MAX_PLOTS + " plots");
        }
        List<Plot> plots = new ArrayList<>(requests.size());
        int cursor = HUB_RADIUS + 1 + PLOT_GAP;
        for (int i = 0; i < requests.size(); i++) {
            Request r = requests.get(i);
            if (r == null || r.moduleId() == null || r.footprint() == null) {
                throw new IllegalArgumentException("Demo layout request " + i + " is incomplete");
            }
            DemoFootprint f = r.footprint();
            plots.add(new Plot(r.moduleId(), i, cursor, -(f.sizeZ() / 2), f.sizeX(), f.sizeZ()));
            cursor += f.sizeX() + PLOT_GAP;
        }
        return List.copyOf(plots);
    }

    /** The X the path runs to: the east edge of the last plot, or the hub edge when there are none. */
    public static int pathEndX(List<Plot> plots) {
        return plots == null || plots.isEmpty() ? HUB_RADIUS : plots.get(plots.size() - 1).maxX();
    }
}
