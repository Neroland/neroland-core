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

### 1.1.0 — additive minor

`1.1.0` is an additive minor: it adds the four passive storage blocks (Battery, Fluid Tank,
Gas Tank, Item Store, plus their Creative variants) and the generic **fluid** and **gas**
storage APIs (`NeroFluidStorage` / `FluidBuffer` / `nerolandcore:fluid`; `NeroGasStorage` /
`GasBuffer` / `NeroGases` / `nerolandcore:gas`), mirroring the existing energy seam. Nothing
was removed: every existing API signature, tag, id, and capability still works unchanged.

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
- **Storage (fluid / gas)** — `NeroFluidStorage`, `FluidBuffer`, `NeroGasStorage`,
  `GasBuffer`, `NeroGases`, and the `nerolandcore:fluid` / `nerolandcore:gas` capabilities
  (added in 1.1.0; see below).
- **Data / compliance** — `PlayerDataErasure`, `PlayerDataEraser`.

## What is internal (may change any time)

Implementation types are **marked `@org.jetbrains.annotations.ApiStatus.Internal`**,
so IDEs and tooling flag any downstream use. This is the enforced api/impl boundary:
anything annotated `@ApiStatus.Internal` (or living in a loader module —
`fabric` / `forge` / `neoforge`) is not API and may change in any release.

Internal includes:

- Loader-specific classes behind the seams (`FabricPlatformHelper`,
  `NeoForgeRegistrationFactory`, the loader `*Network` / `*EnergyLookup` impls — note
  the `*EnergyLookup.ENERGY` capability fields they expose *are* API).
- Core's content-registration internals (`ModBlocks`, `ModItems`, `CoreRegistries`),
  the in-memory providers, the `SavedData` stores (`ProgressionState`,
  `PlayerActivity`), the gate loader (`GateDefinitions`), and the wire payloads
  (`ConfigSyncPayload`, `GateSyncPayload`) — all `@ApiStatus.Internal`. Use the
  facade/interface and reference materials by tag, never these classes.
- Procedural placeholder textures and exact balance constants (config- or
  datapack-tunable; expected to be re-skinned / re-tuned).

> The boundary is enforced by annotation rather than a separate package tree, which
> keeps the ServiceLoader wiring and downstream imports stable. A future minor may
> additionally relocate internals into `…internal` packages; because they are already
> `@ApiStatus.Internal`, that move can't break any supported (non-internal) usage.

## Deprecation

When an API must change within a major, the old form is kept and marked
`@Deprecated` (with the replacement named) for at least one minor release before it's
removed in the next major. Tag ids and datapack formats are deprecated the same way —
never silently repurposed.
