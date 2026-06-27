# For Developers

This page is the contributor- and integrator-facing entry point. The deep API reference lives in the
repo's [`../docs/`](../docs/USING-CORE.md) folder; this page orients you and links there.

## Building on Core

Core is the single, stable foundation every Neroland mod depends on. Declare it a **required**
dependency ordered **after** Core, and pin to a major range (`[1.0,2.0)`):

- Fabric (`fabric.mod.json`): `"depends": { "nerolandcore": ">=1.0.0" }`
- NeoForge (`neoforge.mods.toml`): a required `[[dependencies.<modid>]]` on `nerolandcore`, `ordering = "AFTER"`.
- Forge (`mods.toml`): the equivalent required, `AFTER` dependency.

Full instructions: [`../docs/USING-CORE.md`](../docs/USING-CORE.md).

## The systems and their facades

Every system is reached through a small facade; per-loader wiring is Core's job.

| You want to… | Use | Wiki | Developer doc |
| ------------ | --- | ---- | ------------- |
| Require a material by tag | `c:ingots/<m>` / `neroland:materials/<m>` | [Tags & Datapacks](Tags-and-Datapacks.md) | [TAGS-AND-DATAPACKS](../docs/TAGS-AND-DATAPACKS.md) |
| Register your own config | `ConfigManager.register(schema)` | [Configuration](Configuration.md) | [CONFIG](../docs/CONFIG.md) |
| Gate content on progress | `ProgressionGates.isOpen(player, gate)` | [Progression Gates](Progression-Gates.md) | [PROGRESSION](../docs/PROGRESSION.md) |
| Read/move money | `CurrencyApi` | [Economy & Reputation](Economy-and-Reputation.md) | [ECONOMY-REPUTATION](../docs/ECONOMY-REPUTATION.md) |
| Adjust faction standing | `ReputationApi` | [Economy & Reputation](Economy-and-Reputation.md) | [ECONOMY-REPUTATION](../docs/ECONOMY-REPUTATION.md) |
| Build a machine | extend `AbstractMachineBlockEntity` | [Machines, Power & Upgrades](Machines-Power-and-Upgrades.md) | [MACHINES-POWER-UPGRADES](../docs/MACHINES-POWER-UPGRADES.md) |
| Expose/find energy | `EnergyLookup` + `*EnergyLookup.ENERGY` | [Machines, Power & Upgrades](Machines-Power-and-Upgrades.md) | [MACHINES-POWER-UPGRADES](../docs/MACHINES-POWER-UPGRADES.md) |
| Store player data | register a `PlayerDataEraser` | [Privacy & Data](Privacy-and-Data.md) | [COMPLIANCE](../docs/COMPLIANCE.md) |

## Implementing a Core provider

Core defines the contracts and stores nothing for economy/reputation; your mod registers the store:

- `CurrencyApi.setProvider(new MyCurrencyStore())` — NeroEconomy.
- `ReputationApi.setProvider(new MyReputationStore())` — NeroFactions.

## API stability

Core follows semantic versioning on its **public API**, frozen between majors. Implementation types are
marked `@ApiStatus.Internal` and may change at any time — reference materials by tag and use the
facades, never the internals. See [`../docs/API-STABILITY.md`](../docs/API-STABILITY.md).

## Contributing

- Resources are **hand-authored** in `common/src/main/resources` — validate JSON after edits.
- Keep `common/` free of loader-specific imports; use the platform seams.
- Whenever you add, change, or remove a feature, **update this wiki in the same change**.
- See [`../AGENTS.md`](../AGENTS.md) / [`../CLAUDE.md`](../CLAUDE.md) for the full build-and-verify rules.

## See also

- [Getting Started](Getting-Started.md)
- [Home](Home.md)
- [Tags & Datapacks](Tags-and-Datapacks.md)
