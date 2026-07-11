package za.co.neroland.nerolandcore.meteor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;

import org.objectweb.asm.Type;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * NeoForge implementation of {@link MeteorAnnotationScanner}. Reads the
 * {@link GrindableMaterial} annotation from the {@code ModFileScanData} index (no mod
 * classloading — remap-proof) and keeps only declarations whose declaring class is
 * rooted at {@code za.co.neroland} (the ecosystem scan boundary).
 */
public final class NeoForgeMeteorAnnotationScanner implements MeteorAnnotationScanner {

    private static final Type GRINDABLE = Type.getType(
            "Lza/co/neroland/nerolandcore/meteor/GrindableMaterial;");

    @Override
    public List<MeteorMaterialEntry> scan() {
        List<MeteorMaterialEntry> out = new ArrayList<>();
        for (ModFileScanData data : ModList.get().getAllScanData()) {
            for (ModFileScanData.AnnotationData ann : data.getAnnotations()) {
                if (!GRINDABLE.equals(ann.annotationType())) {
                    continue;
                }
                if (!ann.clazz().getClassName().startsWith(MeteorAnnotationScanners.SCAN_ROOT)) {
                    continue; // scan boundary: only za.co.neroland mods
                }
                MeteorMaterialEntry entry = MeteorAnnotationScanners.fromValues(
                        ann.annotationData(), ann.clazz().getClassName(), ann.memberName());
                if (entry != null) {
                    out.add(entry);
                }
            }
        }
        NerolandCoreCommon.LOGGER.debug("[Neroland Core] NeoForge meteor annotation scan: {} entries", out.size());
        return out;
    }

    /** Defensive accessor in case a future API exposes the data map differently. */
    @SuppressWarnings("unused")
    private static Map<String, Object> values(ModFileScanData.AnnotationData ann) {
        return ann.annotationData();
    }
}
