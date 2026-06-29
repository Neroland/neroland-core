package za.co.neroland.nerolandcore.meteor;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * Loader-agnostic helpers shared by the per-loader {@link MeteorAnnotationScanner}
 * implementations: the package scan boundary and the conversion of a raw annotation
 * value map (element-name → value, as produced by an annotation index) into a
 * {@link MeteorMaterialEntry}. Keeping this in common keeps the three loader scanners
 * thin and identical in behaviour.
 */
public final class MeteorAnnotationScanners {

    /** The hard scan boundary: only annotated classes under this package root are read. */
    public static final String SCAN_ROOT = "za.co.neroland";

    private MeteorAnnotationScanners() {
    }

    /**
     * Build an entry from an annotation value map keyed by element name
     * ({@code item}, {@code tier}, {@code minGate}, {@code planet}, {@code weightOverride},
     * {@code enabled}). Absent elements take their annotation defaults. Returns {@code null}
     * (with a warning) if required fields are missing or invalid. The material id is the item id.
     */
    @Nullable
    public static MeteorMaterialEntry fromValues(Map<String, Object> values, String declaringClass,
            @Nullable String member) {
        String where = declaringClass + (member == null ? "" : "#" + member);

        String itemRaw = asString(values.get("item"));
        if (itemRaw == null || itemRaw.isBlank()) {
            NerolandCoreCommon.LOGGER.warn(
                    "[Neroland Core] @GrindableMaterial at {} has no item; skipping.", where);
            return null;
        }
        Identifier item = parseId(itemRaw, "item", where);
        if (item == null) {
            return null;
        }

        String tierRaw = enumName(values.get("tier"));
        if (tierRaw == null) {
            NerolandCoreCommon.LOGGER.warn(
                    "[Neroland Core] @GrindableMaterial at {} has no tier; skipping.", where);
            return null;
        }
        MeteorTier tier;
        try {
            tier = MeteorTier.byName(tierRaw);
        } catch (RuntimeException e) {
            NerolandCoreCommon.LOGGER.warn(
                    "[Neroland Core] @GrindableMaterial at {} has invalid tier '{}'; skipping.", where, tierRaw);
            return null;
        }

        Identifier minGate = optionalId(asString(values.get("minGate")), "minGate", where);
        Identifier planet = optionalId(asString(values.get("planet")), "planet", where);

        Integer weightOverride = null;
        Object wo = values.get("weightOverride");
        if (wo instanceof Number n && n.intValue() != GrindableMaterial.NO_WEIGHT_OVERRIDE) {
            weightOverride = n.intValue();
        }

        boolean enabled = !(values.get("enabled") instanceof Boolean b) || b;

        // Annotation entry's material id == its item id (a data file at that path overrides it).
        return new MeteorMaterialEntry(item, item, tier, minGate, planet, weightOverride, enabled);
    }

    /**
     * Build an entry from a typed {@link GrindableMaterial} annotation (the Fabric path, where the
     * annotation is read reflectively off an entrypoint-listed holder class). Same validation as
     * {@link #fromValues}. The material id is the item id.
     */
    @Nullable
    public static MeteorMaterialEntry fromAnnotation(GrindableMaterial a, String where) {
        if (a.item().isBlank()) {
            NerolandCoreCommon.LOGGER.warn(
                    "[Neroland Core] @GrindableMaterial at {} has no item; skipping.", where);
            return null;
        }
        Identifier item = parseId(a.item(), "item", where);
        if (item == null) {
            return null;
        }
        Identifier minGate = optionalId(a.minGate(), "minGate", where);
        Identifier planet = optionalId(a.planet(), "planet", where);
        Integer weightOverride =
                a.weightOverride() == GrindableMaterial.NO_WEIGHT_OVERRIDE ? null : a.weightOverride();
        return new MeteorMaterialEntry(item, item, a.tier(), minGate, planet, weightOverride, a.enabled());
    }

    @Nullable
    private static Identifier optionalId(@Nullable String raw, String field, String where) {
        return (raw == null || raw.isBlank()) ? null : parseId(raw, field, where);
    }

    @Nullable
    private static Identifier parseId(String raw, String field, String where) {
        try {
            return Identifier.parse(raw);
        } catch (RuntimeException e) {
            NerolandCoreCommon.LOGGER.warn(
                    "[Neroland Core] @GrindableMaterial at {} has invalid {} '{}'; ignoring entry.",
                    where, field, raw);
            return null;
        }
    }

    @Nullable
    private static String asString(@Nullable Object value) {
        return value instanceof String s ? s : null;
    }

    /**
     * Extract the enum constant name from an annotation index's enum representation. Annotation indexes
     * usually box an enum value in a holder (ASM); this reads it without importing any loader type:
     * a plain String, a {@code value()}/{@code getValue()} accessor, or the trailing identifier of the
     * holder's {@code toString()} (e.g. {@code "...;UNCOMMON"} → {@code "UNCOMMON"}).
     */
    @Nullable
    static String enumName(@Nullable Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            return s;
        }
        for (String accessor : new String[] {"value", "getValue"}) {
            try {
                Method m = value.getClass().getMethod(accessor);
                Object r = m.invoke(value);
                if (r instanceof String s) {
                    return s;
                }
            } catch (ReflectiveOperationException ignored) {
                // try next
            }
        }
        String s = value.toString();
        int semi = s.lastIndexOf(';');
        String tail = semi >= 0 ? s.substring(semi + 1) : s;
        tail = tail.trim();
        return tail.isEmpty() ? null : tail.toUpperCase(Locale.ROOT);
    }
}
