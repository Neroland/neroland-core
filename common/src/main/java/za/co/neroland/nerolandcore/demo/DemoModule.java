package za.co.neroland.nerolandcore.demo;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * One mod's exhibit in a <b>Nero Mods Demo</b> world (Core 1.20.0). A mod implements this and registers it
 * once with {@link DemoRegistry#register} from its common init; Core does the rest: it lists the module on
 * the demo picker (from the singleplayer world list), creates a fixed-seed creative world, lays out a hub
 * and one plot per selected module in a fixed order, and calls {@link #build} exactly once per world, on
 * the server thread, when that world first starts.
 *
 * <p><b>Determinism.</b> A demo world must look the same every time it is created from the same
 * selection. {@link #build} must therefore place the same things in the same places every time: no
 * randomness, no reading the clock, no dependence on which other modules were selected. It may build
 * outside its plot only in its own dimensions (for example a viewing outpost on a planet).
 *
 * <p><b>Privacy.</b> A build must not record or log anything about the player. Core itself stores only
 * that the world is a demo, the selected module ids and whether the build finished.
 */
public interface DemoModule {

    /** The registering mod's id; also the module id. Must match {@code [a-z0-9_.-]{1,64}}. */
    String modId();

    /** The name shown on the picker and above the plot. */
    Component displayName();

    /**
     * The icon shown on the picker: a square texture, for example an item texture such as
     * {@code mymod:textures/item/hero.png}. A texture rather than an item, because the picker runs on the main
     * menu, before any world has loaded, when item stacks cannot be made yet. Called on the client only.
     */
    Identifier icon();

    /** Plot order: lower comes first (nearer the hub). Ties are broken by mod id. Core's own module is 0. */
    default int sortOrder() {
        return 1000;
    }

    /**
     * Other mod ids this exhibit needs (for a cross-mod exhibit). The module is offered only when every one
     * of them is loaded. Its own mod id need not be listed.
     */
    default List<String> requiredMods() {
        return List.of();
    }

    /** The plot this exhibit needs. Must return the same value every call. */
    DemoFootprint footprint();

    /**
     * A few short lines shown floating at the plot entrance, top line first. Keep each line under about 48
     * characters; at most {@link DemoBuildContext#MAX_GUIDE_LINES} lines are shown.
     */
    default List<Component> guide() {
        return List.of();
    }

    /**
     * Places the exhibit. {@link DemoBuildContext#origin()} is the north-west corner of the plot floor; build
     * at {@code y + 1} and above, inside {@link #footprint()}. Runs once per world, on the server thread. An
     * exception is caught and logged by Core, and the other modules still build.
     */
    void build(DemoBuildContext context);
}
