package za.co.neroland.nerolandcore.platform;

import java.nio.file.Path;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * The loader-specific behaviour the common module is allowed to depend on.
 *
 * <p>Each loader module ships exactly one implementation, registered via a
 * {@code META-INF/services} file so {@link Services} can load it with
 * {@link java.util.ServiceLoader}. This is the lightweight, dependency-free
 * alternative to Architectury's {@code @ExpectPlatform}.
 *
 * <p>This is the seam where loader-divergent foundations (environment queries,
 * config dir, mod metadata) get a single cross-loader abstraction. Grow it as the
 * Neroland Core API surface expands — but keep it free of gameplay specifics:
 * those belong in dedicated systems that build on Core, not on the platform seam.
 */
public interface IPlatformHelper {

    /** Human-readable platform name ("Fabric" / "Forge" / "NeoForge"). */
    String getPlatformName();

    /** True when running in a development (dev/data/test) environment. */
    boolean isDevelopmentEnvironment();

    /** True when the named mod is loaded. */
    boolean isModLoaded(String modId);

    /** True on the physical client (renderers, screens, HUD available). */
    boolean isClient();

    /** The loader config directory (Fabric {@code getConfigDir}, NeoForge/Forge {@code FMLPaths.CONFIGDIR}). */
    Path getConfigDir();

    /** This mod's version string (a public manifest value — safe for telemetry release tags), or "unknown". */
    String getModVersion();

    /**
     * The ids + versions of every loaded mod ("modid version"), sorted, for crash mod-conflict
     * triage. These are public manifest strings only — never personal data (POPIA/GDPR).
     */
    List<String> getLoadedModIds();

    /**
     * Invalidate cached block capabilities at a position after a side-config mode change so
     * adjacent pipes/cables re-query (NeoForge {@code Level#invalidateCapabilities}). A no-op
     * on loaders that re-resolve capabilities per query (Fabric) or via block updates (Forge);
     * the common layer separately fires a neighbour update for connection re-evaluation.
     */
    default void invalidateCapabilities(Level level, BlockPos pos) {
    }
}
