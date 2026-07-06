package za.co.neroland.nerolandcore.link;

import java.util.List;

/**
 * Discovery metadata for one NeroLink module. The bridge builds its
 * {@code GET /api/v1/discovery} response and the app builds its UI from a list of
 * these ({@link NeroLinkRegistry#modules()}), so a server adding a mod lights up the
 * matching app section on the next connect.
 *
 * <p>Everything here is public, non-personal metadata: a module id, its mod version,
 * a per-module {@code schemaVersion} (bumped when that module's data shape changes so
 * one module can evolve without touching others), and the data sections + action ids
 * it exposes. It carries no player data.
 *
 * @param moduleId      the module id (e.g. {@code "core"}, {@code "nerologistics"});
 *                      matches the owning {@link LinkSnapshotProvider#moduleId()} /
 *                      {@link LinkActionHandler#moduleId()}
 * @param modVersion    the owning mod's version string (e.g. {@code "1.0.0"}), for display
 * @param schemaVersion the module's data-schema revision, incremented on shape changes
 * @param dataSections  the snapshot sections this module serves (e.g.
 *                      {@code ["energy", "storage", "gates", "alerts"]})
 * @param actionIds     the action ids this module accepts (e.g. {@code ["ack_alert"]});
 *                      empty if the module is read-only
 */
public record LinkModuleInfo(String moduleId,
                             String modVersion,
                             int schemaVersion,
                             List<String> dataSections,
                             List<String> actionIds) {

    /**
     * Canonicalises the collections to immutable copies so a registered module's
     * advertised surface cannot be mutated after registration.
     */
    public LinkModuleInfo {
        dataSections = dataSections == null ? List.of() : List.copyOf(dataSections);
        actionIds = actionIds == null ? List.of() : List.copyOf(actionIds);
    }
}
