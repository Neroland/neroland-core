# Depending on Neroland Core

How another Nero mod (or a third-party mod) builds against Neroland Core. Part of
[V1](V1-PLAN.md) Phase 7.

## Declare the dependency

**Manifests** — Core must load first, so declare it a required dependency:

- Fabric (`fabric.mod.json`): `"depends": { "nerolandcore": ">=1.0.0" }`
- NeoForge (`neoforge.mods.toml`): a `[[dependencies.<modid>]]` on `nerolandcore`,
  `type = "required"`, `versionRange = "[1.0,2.0)"`, `ordering = "AFTER"`.
- Forge (`mods.toml`): the equivalent required dependency, ordered `AFTER`.

Pin to a **major range** (`[1.0,2.0)`) — see [API-STABILITY.md](API-STABILITY.md).

**Gradle** — add Core's published artifact to each loader module's dependencies
(once Core is on a Maven you publish to), matching the loader (`nerolandcore-fabric`,
`-neoforge`, `-forge`).

## Use the systems

Every system is reached through a small facade; the per-loader wiring is Core's job.

| You want to… | Use | Docs |
| ------------ | --- | ---- |
| Require a material by tag | `c:ingots/<m>` / `neroland:materials/<m>` | [TAGS-AND-DATAPACKS](TAGS-AND-DATAPACKS.md) |
| Register your own config | `ConfigManager.register(schema)` | [CONFIG](CONFIG.md) |
| Gate content on progress | `ProgressionGates.isOpen(player, gate)` | [PROGRESSION](PROGRESSION.md) |
| Read/move money | `CurrencyApi` | [ECONOMY-REPUTATION](ECONOMY-REPUTATION.md) |
| Adjust faction standing | `ReputationApi` | [ECONOMY-REPUTATION](ECONOMY-REPUTATION.md) |
| Build a machine | extend `AbstractMachineBlockEntity` | [MACHINES-POWER-UPGRADES](MACHINES-POWER-UPGRADES.md) |
| Expose/find energy | `EnergyLookup` + `*EnergyLookup.ENERGY` | [MACHINES-POWER-UPGRADES](MACHINES-POWER-UPGRADES.md) |
| Store player data | register a `PlayerDataEraser` | [COMPLIANCE](COMPLIANCE.md) |

Put loader-agnostic logic in your `common` module and reach loader specifics through
Core's seams (or your own), mirroring Core's structure.

## Implement a Core provider (NeroEconomy / NeroFactions)

Core defines the contract and stores nothing; your mod registers the store:

```java
CurrencyApi.setProvider(new MyCurrencyStore());      // NeroEconomy
ReputationApi.setProvider(new MyReputationStore());  // NeroFactions
```

## If you store player data

Register a `PlayerDataEraser` so one erase request clears your data too, and route
player-keyed storage through it — see [COMPLIANCE.md](COMPLIANCE.md).
