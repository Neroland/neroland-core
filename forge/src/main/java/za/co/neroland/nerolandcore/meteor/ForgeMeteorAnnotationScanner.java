package za.co.neroland.nerolandcore.meteor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.locating.IModFile;

import org.objectweb.asm.Type;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * MinecraftForge implementation of {@link MeteorAnnotationScanner}. Reads the
 * {@link GrindableMaterial} annotation from each loaded mod file's
 * {@code ModFileScanData} index (no mod classloading — remap-proof) and keeps only
 * declarations whose declaring class is rooted at {@code za.co.neroland} (the
 * ecosystem scan boundary).
 */
public final class ForgeMeteorAnnotationScanner implements MeteorAnnotationScanner {

    private static final Type GRINDABLE = Type.getType(
            "Lza/co/neroland/nerolandcore/meteor/GrindableMaterial;");

    @Override
    public List<MeteorMaterialEntry> scan() {
        List<MeteorMaterialEntry> out = new ArrayList<>();
        Set<ModFileScanData> seen = new HashSet<>();
        for (IModInfo info : ModList.getMods()) {
            try {
                IModFile file = info.getOwningFile().getFile();
                ModFileScanData data = file.getScanResult();
                if (data == null || !seen.add(data)) {
                    continue;
                }
                for (ModFileScanData.AnnotationData ann : data.getAnnotations()) {
                    if (!GRINDABLE.equals(ann.annotationType())) {
                        continue;
                    }
                    String declaringClass = ann.clazz().getClassName();
                    if (!declaringClass.startsWith(MeteorAnnotationScanners.SCAN_ROOT)) {
                        continue; // scan boundary: only za.co.neroland mods
                    }
                    MeteorMaterialEntry entry = MeteorAnnotationScanners.fromValues(
                            ann.annotationData(), declaringClass, ann.memberName());
                    if (entry != null) {
                        out.add(entry);
                    }
                }
            } catch (RuntimeException | LinkageError e) {
                NerolandCoreCommon.LOGGER.warn(
                        "[Neroland Core] Forge meteor scan skipped a mod file ({}).", info.getModId(), e);
            }
        }
        NerolandCoreCommon.LOGGER.debug("[Neroland Core] Forge meteor annotation scan: {} entries", out.size());
        return out;
    }
}
