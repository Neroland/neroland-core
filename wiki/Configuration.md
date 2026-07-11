# Configuration

Neroland Core provides one config service so every Neroland mod registers its
config with Core instead of hand-rolling file I/O. Each mod gets one
`<modId>.properties` file in the loader's config directory, with a typed schema,
defaults, validation, hot-reload, and optional server-to-client sync.

## How config files behave

- the file is created with documented defaults on first run
- numeric values clamp to their `[min, max]` range
- bad or blank entries fall back to the default
- new keys are migrated into an existing file automatically
- your edits and comments are otherwise left untouched

## Typed builders

Schemas are built from typed entries. Each takes a key, a default, (range
bounds where applicable), a `serverAuthoritative` flag, and a comment.

- `bool`
- `intRange`
- `longRange`
- `doubleRange`
- `string`

## Server-authoritative values

A value is marked `serverAuthoritative = true` when the server must dictate it
to every client — typically gameplay balance. Core pushes a snapshot to each
player on join and after a reload. Local-only values (debug toggles, client
visuals) stay `false` and are read from the local file.

The sync payload carries only config keys and values — never player identity.

## Core's own schema (`CoreConfig`)

| Key | Purpose |
| --- | --- |
| `materialHardnessMultiplier` | Scales material block hardness |
| `materialBlastResistanceMultiplier` | Scales material block blast resistance |
| `upgradeModuleSlotCap` | Clamps upgrade module slot count |
| `upgradeStackingDiminish` | Diminishing-returns curve for stacked upgrades |
| `neroEnergyToForgeEnergyRatio` | NF <-> Forge Energy conversion ratio |
| `itemHighlightsEnabled` | Coloured slot borders on Nero items (see Item Highlights) |
| `itemHighlightOpacity` | Highlight border opacity, 0-100 percent |
| `itemHighlightThickness` | Highlight border thickness in pixels, 1-4 |
| `debugLogging` | Enables verbose debug logging |
| `telemetryEnabled` | Reserved opt-out (see Privacy and Data) |
| `dataRetentionDays` | Inactive-player retention; 0 = never |

## Commands

Both require op level 2.

| Command | Effect |
| --- | --- |
| `/neroland config reload` | Re-reads every schema and re-syncs |
| `/neroland config list` | Prints each schema's values, flagging server-authoritative ones |

## For developers

The full config API is documented in [../docs/CONFIG.md](../docs/CONFIG.md).

## See also

- [Machines, Power, and Upgrades](Machines-Power-and-Upgrades.md)
- [Privacy and Data](Privacy-and-Data.md)
- [Commands](Commands.md)
- [Home](Home.md)
