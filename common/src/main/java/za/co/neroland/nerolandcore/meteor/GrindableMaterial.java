package za.co.neroland.nerolandcore.meteor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a grindable meteor material in code — the convenience alternative to a
 * {@code data/<ns>/neroland/meteor_materials/<id>.json} file. The full annotation
 * class path is
 * {@code za.co.neroland.nerolandcore.meteor.GrindableMaterial}.
 *
 * <h2>Scan boundary</h2>
 * Core scans for this annotation <b>only across loaded mods whose annotated classes
 * are rooted at the package {@code za.co.neroland}</b> (the shared ecosystem root).
 * The boundary is a hard package-prefix filter applied to the declaring class name,
 * not a classpath-wide sweep: a Nero mod that is not installed contributes nothing,
 * and no third-party class is ever read. Discovery uses each loader's annotation
 * index (NeoForge / Forge {@code ModFileScanData}) or, on Fabric, a declared
 * entrypoint — never runtime classloading of mod code, so it is remap-proof.
 *
 * <h2>Why {@code item} is required here</h2>
 * Because the scan reads annotation <i>metadata</i> without loading the annotated
 * class, it cannot read a field's runtime value. The output item id must therefore
 * be named explicitly in {@link #item()} (a fully-qualified {@code namespace:path}).
 * The resulting registry entry's material id is exactly that item id, so a data file
 * shipped at the matching path ({@code data/<ns>/neroland/meteor_materials/<path>.json})
 * overrides the annotation, preserving the datapack-override principle.
 *
 * <pre>{@code
 * @GrindableMaterial(
 *     item = "nerotech:cobalt_dust",
 *     tier = MeteorTier.UNCOMMON,
 *     minGate = "nerolandcore:reached_orbit")
 * public static final RegistryObject<Item> COBALT_DUST = ...;
 * }</pre>
 *
 * @see MeteorMaterialEntry
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface GrindableMaterial {

    /** Sentinel for {@link #weightOverride()} meaning "no override" (use the tier base weight). */
    int NO_WEIGHT_OVERRIDE = Integer.MIN_VALUE;

    /** The output item id this entry produces, as {@code namespace:path}. Required. */
    String item();

    /** The rarity tier — sets the pool and (for the primary pool) the base weight. */
    MeteorTier tier();

    /**
     * The Core progression gate id (e.g. {@code "nerolandcore:reached_orbit"}) that must be
     * satisfied before this entry is eligible. Empty (the default) means "always eligible".
     */
    String minGate() default "";

    /**
     * An optional Nerospace planet id (e.g. {@code "nerospace:keptaris"}). When set, the entry
     * only enters the pool while grinding in that planet's dimension and receives the planet-bias
     * multiplier there. Empty (the default) means "any dimension".
     */
    String planet() default "";

    /**
     * An optional integer that replaces the tier-derived base weight. {@link #NO_WEIGHT_OVERRIDE}
     * (the default) means "use the tier base weight". <b>Exotic-tier entries must set this</b> — the
     * exotic pool has no tier base weight, so an exotic entry with no override is ineligible.
     */
    int weightOverride() default NO_WEIGHT_OVERRIDE;

    /** A per-entry kill switch; {@code false} drops the entry from every pool. Default {@code true}. */
    boolean enabled() default true;
}
