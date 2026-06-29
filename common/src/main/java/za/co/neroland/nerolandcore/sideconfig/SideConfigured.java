package za.co.neroland.nerolandcore.sideconfig;

import org.jetbrains.annotations.Nullable;

/**
 * Implemented by any block-entity that carries a {@link SideConfigComponent} — both
 * Core's base machine BE and downstream BEs that compose the component in. The
 * server-authoritative sync layer and the Configurator look a BE up by position and
 * route intents through this seam, so they need no knowledge of the concrete machine
 * type or its menu.
 */
public interface SideConfigured {

    /** The side-config component, or {@code null} if this BE has none configured. */
    @Nullable
    SideConfigComponent sideConfig();
}
