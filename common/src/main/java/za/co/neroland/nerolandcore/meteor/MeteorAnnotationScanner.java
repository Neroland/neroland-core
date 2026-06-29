package za.co.neroland.nerolandcore.meteor;

import java.util.List;

import za.co.neroland.nerolandcore.platform.Services;

/**
 * Loader seam for the {@link GrindableMaterial} annotation path. Each loader resolves
 * annotated declarations through its own annotation index and returns the equivalent
 * {@link MeteorMaterialEntry}s, so common code stays free of loader APIs.
 *
 * <p><b>Scan boundary.</b> Implementations must only return entries whose declaring
 * class is rooted at the package {@code za.co.neroland} (the ecosystem root) — a hard
 * prefix filter, never a classpath-wide sweep. Discovery must read annotation
 * metadata without classloading mod code:
 * <ul>
 *   <li>NeoForge / Forge — the {@code ModFileScanData} annotation index;</li>
 *   <li>Fabric — the {@code nerolandcore:grindable} entrypoint, whose listed classes
 *       (which live under {@code za.co.neroland}) are read for the annotation.</li>
 * </ul>
 *
 * <p>Annotation entries form the <b>lowest</b> precedence layer: any data file at the
 * matching material id overrides them ({@link MeteorMaterialRegistry}).
 */
public interface MeteorAnnotationScanner {

    MeteorAnnotationScanner INSTANCE = Services.load(MeteorAnnotationScanner.class);

    /** All {@code @GrindableMaterial} entries declared by loaded {@code za.co.neroland} mods. */
    List<MeteorMaterialEntry> scan();
}
