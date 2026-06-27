package za.co.neroland.nerolandcore.config;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * Neroland Core's own config schema — the shared tuning surface later phases read.
 * Registered once from {@link NerolandCoreCommon#init()}.
 *
 * <p>Gameplay-affecting balance values are {@code serverAuthoritative} so a server
 * dictates them to every client; local-only toggles (debug, telemetry opt-out) are
 * not. All of this is also datapack-adjacent — see {@code docs/TAGS-AND-DATAPACKS.md}.
 */
public final class CoreConfig {

    public static final ConfigSchema SCHEMA =
            ConfigSchema.create(NerolandCoreCommon.MOD_ID, "Neroland Core configuration.");

    // --- Material stat baselines (server-authoritative) ---------------------
    public static final ConfigValue<Double> MATERIAL_HARDNESS_MULTIPLIER = SCHEMA.doubleRange(
            "materialHardnessMultiplier", 1.0D, 0.1D, 10.0D, true,
            "Global multiplier on Nero material block hardness.");
    public static final ConfigValue<Double> MATERIAL_BLAST_MULTIPLIER = SCHEMA.doubleRange(
            "materialBlastResistanceMultiplier", 1.0D, 0.1D, 10.0D, true,
            "Global multiplier on Nero material block blast resistance.");

    // --- Upgrade-module framework (read by Phase 6) -------------------------
    public static final ConfigValue<Integer> UPGRADE_MODULE_SLOT_CAP = SCHEMA.intRange(
            "upgradeModuleSlotCap", 4, 1, 16, true,
            "Maximum upgrade-module slots any machine may expose.");
    public static final ConfigValue<Double> UPGRADE_STACKING_DIMINISH = SCHEMA.doubleRange(
            "upgradeStackingDiminish", 1.0D, 0.1D, 1.0D, true,
            "Diminishing factor on stacked identical upgrade modules (1.0 = linear, lower = stronger falloff).");

    // --- Energy conversion (read by Phase 6) --------------------------------
    public static final ConfigValue<Double> NERO_ENERGY_TO_FE = SCHEMA.doubleRange(
            "neroEnergyToForgeEnergyRatio", 1.0D, 0.01D, 100.0D, true,
            "Conversion ratio: 1 Nero energy unit equals N Forge Energy (rough parity by default).");

    // --- Data retention (POPIA/GDPR) ----------------------------------------
    public static final ConfigValue<Integer> DATA_RETENTION_DAYS = SCHEMA.intRange(
            "dataRetentionDays", 0, 0, 3650, false,
            "Days of inactivity after which a player's stored data is purged (0 = never; opt-in).");

    // --- Local-only toggles -------------------------------------------------
    public static final ConfigValue<Boolean> DEBUG_LOGGING = SCHEMA.bool(
            "debugLogging", false, false,
            "Verbose Neroland Core debug logging (local only).");
    public static final ConfigValue<Boolean> TELEMETRY_ENABLED = SCHEMA.bool(
            "telemetryEnabled", true, false,
            "Send anonymous, Neroland-Core-only crash reports (Sentry, EU servers): stack trace, mod/MC/loader/OS/Java versions, your other installed mods, this mod's config, and anonymous stability/timing data; no IP, username, UUID, world data or chat; file paths are scrubbed of your account name. Set false to opt out. See PRIVACY.md.");

    private CoreConfig() {
    }

    /** Register + load Core's schema. Called once from common init. */
    public static void init() {
        ConfigManager.register(SCHEMA);
    }
}
