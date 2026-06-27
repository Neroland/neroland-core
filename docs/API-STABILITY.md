# Neroland Core — API Stability & Versioning

Once downstream mods ship against Core, breaking changes cascade across the whole
lineup. This is the contract for what's stable and how versions move. Part of
[V1](V1-PLAN.md) Phase 7.

## Versioning policy

Neroland Core follows semantic versioning on its **public API**:

- **Major** (`1.x → 2.0`) — the only releases allowed to make breaking API changes.
  The public API is **frozen between majors**.
- **Minor** (`1.0 → 1.1`) — additive only: new APIs, new gates/currencies/upgrade
  types, new config keys. Existing signatures, tag names and ids keep working.
- **Patch** (`1.0.0 → 1.0.1`) — bug fixes, no API or data-contract changes.

Downstream mods should depend on a Core major (e.g. `[1.0,2.0)`).

## What is public (frozen) API

These are the surfaces downstream mods are meant to use; they will not break within a
major:

- **Registration / platform** — `RegistrationProvider`, `Services`, `IPlatformHelper`,
  `NetworkPlatform`, `EnergyLookup`, and the per-loader energy capability objects
  (`*EnergyLookup.ENERGY`).
- **Materials & tags** — the registered material items/blocks and the `c:` + `neroland:`
  tag set (see `TAGS-AND-DATAPACKS.md`). Tag ids are a frozen contract.
- **Config** — `ConfigManager`, `ConfigSchema`, `ConfigValue` and the `CoreConfig` keys.
- **Progression** — `ProgressionGates`, `CoreGates` ids, `GateScope`, `GateEvents`,
  `ClientGates`, and the `neroland_gates` datapack format.
- **Economy / reputation** — `CurrencyApi`, `Currency`, `CurrencyProvider`,
  `CurrencyEvents`; `ReputationApi`, `ReputationProvider`, `ReputationEvents`.
- **Machines / power / upgrades** — `NeroEnergyStorage`, `EnergyBuffer`,
  `EnergyConversions`, `AbstractMachineBlockEntity`, `UpgradeType`, `UpgradeContainer`,
  `UpgradeModifiers`.
- **Data / compliance** — `PlayerDataErasure`, `PlayerDataEraser`.

## What is internal (may change any time)

- The `*Impl` / loader-specific classes behind the seams (`FabricPlatformHelper`,
  `NeoForgeRegistrationFactory`, the in-memory providers, the `SavedData` classes,
  payload records, …). Use the facade/interface, never the implementation.
- Procedural placeholder textures and exact balance constants (these are config- or
  datapack-tunable and expected to be re-skinned / re-tuned).

> Note: V1 keeps the public and internal types in the same package tree. A future
> minor may physically split a published `…api` package and mark internals
> `@ApiStatus.Internal`; that move will be additive (the existing types stay or are
> deprecated-with-replacement, never removed within a major).

## Deprecation

When an API must change within a major, the old form is kept and marked
`@Deprecated` (with the replacement named) for at least one minor release before it's
removed in the next major. Tag ids and datapack formats are deprecated the same way —
never silently repurposed.
