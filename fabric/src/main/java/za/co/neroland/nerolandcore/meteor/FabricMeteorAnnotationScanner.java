package za.co.neroland.nerolandcore.meteor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * Fabric implementation of {@link MeteorAnnotationScanner}. Fabric has no
 * build-time annotation index, so Nero mods that want the annotation path list their
 * grindable-holder classes under the Fabric entrypoint {@code nerolandcore:grindable}
 * in {@code fabric.mod.json}:
 *
 * <pre>{@code
 * "entrypoints": { "nerolandcore:grindable": [ "za.co.neroland.tech.MeteorHolders" ] }
 * }</pre>
 *
 * Each listed class (which must live under {@code za.co.neroland} — the scan
 * boundary) is read reflectively for {@link GrindableMaterial} on the class itself
 * and on its declared fields. The same annotation is the single source of truth on
 * every loader; only the discovery mechanism differs.
 */
public final class FabricMeteorAnnotationScanner implements MeteorAnnotationScanner {

    private static final String ENTRYPOINT = "nerolandcore:grindable";

    @Override
    public List<MeteorMaterialEntry> scan() {
        List<MeteorMaterialEntry> out = new ArrayList<>();
        List<EntrypointContainer<Object>> holders =
                FabricLoader.getInstance().getEntrypointContainers(ENTRYPOINT, Object.class);
        for (EntrypointContainer<Object> container : holders) {
            final Class<?> holder;
            try {
                holder = container.getEntrypoint().getClass();
            } catch (Throwable t) {
                NerolandCoreCommon.LOGGER.warn(
                        "[Neroland Core] Could not load a '{}' entrypoint holder; skipping.", ENTRYPOINT, t);
                continue;
            }
            if (!holder.getName().startsWith(MeteorAnnotationScanners.SCAN_ROOT)) {
                continue; // scan boundary: only za.co.neroland classes
            }
            collect(out, holder);
        }
        NerolandCoreCommon.LOGGER.debug("[Neroland Core] Fabric meteor annotation scan: {} entries", out.size());
        return out;
    }

    private static void collect(List<MeteorMaterialEntry> out, Class<?> holder) {
        GrindableMaterial onType = holder.getAnnotation(GrindableMaterial.class);
        if (onType != null) {
            add(out, onType, holder.getName());
        }
        for (Field field : holder.getDeclaredFields()) {
            GrindableMaterial onField = field.getAnnotation(GrindableMaterial.class);
            if (onField != null) {
                add(out, onField, holder.getName() + "#" + field.getName());
            }
        }
    }

    private static void add(List<MeteorMaterialEntry> out, GrindableMaterial a, String where) {
        MeteorMaterialEntry entry = MeteorAnnotationScanners.fromAnnotation(a, where);
        if (entry != null) {
            out.add(entry);
        }
    }
}
