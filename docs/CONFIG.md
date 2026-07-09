# Neroland Core — Config Framework

How any Neroland mod registers config with Core instead of hand-rolling file I/O.
Part of [V1](V1-PLAN.md) Phase 3.

## What you get

One service (`ConfigManager`) owns: a typed schema per mod, a single
`<modId>.properties` file in the loader config dir, defaults, range/validation,
hot-reload via `/neroland config reload`, and server→client sync of values you mark
server-authoritative. No per-mod config plumbing, one consistent file format, one
reload command across the whole ecosystem.

## Registering a schema

Declare a schema, hold the typed handles, register it once during your common
`init()`:

```java
public final class MyModConfig {
    public static final ConfigSchema SCHEMA =
            ConfigSchema.create(MyMod.MOD_ID, "MyMod configuration.");

    // doubleRange(key, default, min, max, serverAuthoritative, comment)
    public static final ConfigValue<Double> ORE_RATE = SCHEMA.doubleRange(
            "oreSpawnRate", 1.0D, 0.1D, 10.0D, true, "Multiplier on MyMod ore frequency.");
    public static final ConfigValue<Boolean> FANCY_FX = SCHEMA.bool(
            "fancyParticles", true, false, "Client-only particle effects.");

    public static void init() { ConfigManager.register(SCHEMA); }
}
```

Read a value anywhere with `MyModConfig.ORE_RATE.get()` — always the current,
validated value.

## Typed builders

`ConfigSchema` exposes `bool`, `intRange`, `longRange`, `doubleRange`, and `string`.
Each takes the key, default, (range bounds where relevant), a `serverAuthoritative`
flag, and a comment, and returns a `ConfigValue<T>` handle. Declaration order drives
both the file layout and the `/neroland config list` output.

## Validation & files

- Numeric builders clamp to their `[min, max]`; bad/blank entries fall back to the
  default. Your code never sees an out-of-range value.
- The file is created with documented defaults on first run. New keys added in a
  later version are migrated into an existing file automatically; user edits and
  comments are otherwise left untouched.

## Server-authoritative values & sync

Mark a value `serverAuthoritative = true` when the server must dictate it to every
client (gameplay balance). Core collects all such values into a snapshot and pushes
it to each player on join and after `/neroland config reload`
(`CoreNetwork` + `ConfigSyncPayload`); the client applies it through
`ConfigManager.applyServerValues`. Local-only values (debug toggles, client visual
options) stay `false` and are read straight from the local file.

**Privacy (POPIA/GDPR):** the sync payload carries only config keys and values —
never player identity or world data.

## Command

- `/neroland config reload` — re-reads every registered schema from disk and
  re-syncs server-authoritative values to online clients (op level 2 /
  `LEVEL_GAMEMASTERS`).
- `/neroland config list` — prints each schema's values, flagging the
  server-authoritative ones.

## Core's own schema

`CoreConfig` registers the shared tuning surface later phases read: material stat
baselines (`materialHardnessMultiplier`, `materialBlastResistanceMultiplier`), the
upgrade-module framework (`upgradeModuleSlotCap`, `upgradeStackingDiminish`), the
energy conversion ratio (`neroEnergyToForgeEnergyRatio`), the client-side item
highlights (`itemHighlightsEnabled`, `itemHighlightOpacity`, `itemHighlightThickness`
— local-only), plus local `debugLogging` and a reserved `telemetryEnabled` opt-out.
