# Changelog

All notable changes to **Neroland Core** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
See [`docs/API-STABILITY.md`](docs/API-STABILITY.md) for the versioning policy.

## [1.11.0] - 2026-08-08

Crash-safety and POPIA/GDPR hardening. No existing API signature, tag, id, capability or config
key changes.

### Added

**Erasure conformance harness** (`za.co.neroland.nerolandcore.data.ErasureConformance`)

- A reusable, mod-agnostic harness in the **main** source set, so any downstream mod can run it from
  its own test suite: `ErasureConformance.create().probe("mymod:rows", uuid -> …).verify(server, uuid)`.
  Erasure is the one contract that spans the whole ecosystem, and this makes it mechanically
  provable instead of assumed.
- A run asserts that every probe held data **before** the request and none **after** (a probe that
  never held data is a failure, not a vacuous pass); that an eraser which throws — `RuntimeException`
  or `Error` — does not stop the erasers registered after it; and that
  `CurrencyProvider.forgetPlayer` / `ReputationProvider.forgetPlayer` were actually reached rather
  than silently inheriting Core's default no-op body.
- Failures are actionable: `ErasureConformance.Report` names each subsystem that retained data and
  `verify(...)` throws an `AssertionError` carrying the summary. The report never carries the player
  UUID. No test dependency is added to Core's main source set.
- `SavedDataRecovery.backupNow(level, type, instance, name)` — forces an immediate last-known-good
  backup refresh, for use straight after erasing a player.
- `PlayerActivity.hasRecord(UUID)` — whether an activity record exists, for subject-access checks
  and conformance probes.
- `PlayerDataErasure.unregister(PlayerDataEraser)` and `registeredCount()`, both
  `@ApiStatus.Internal` — test teardown and diagnostics only.
- Core-side unit tests covering all six of Core's registered erasers, the failure-isolation
  guarantee, and the default-no-op provider case.

### Changed

- **All four of Core's `SavedData` stores now load through `SavedDataRecovery`** —
  `ProgressionState`, `MaterialMilestoneState`, `PlayerActivity` and `LinkAlerts`. A corrupt or
  unreadable `.dat` previously propagated out of `computeIfAbsent` on every access and hard-crashed
  the server tick loop repeatedly (the MC-NEROSPACE-H failure class); it now falls back to the
  last-known-good backup, then to a fresh store, and reports the failure as handled telemetry.
  `getDataStorage()` no longer appears anywhere in Core outside `SavedDataRecovery` itself.
- `SavedDataRecovery` gained the backup ladder and periodic last-known-good backup writing already
  proven in Nerospace (throttled, hash-compared, write-temp-then-atomic-rename), on top of the
  argument validation and null-return handling Core already had.
- Each store gained an `eraseFor(server, uuid)` entry point, and `CoreData` registers those instead
  of bare `forget(uuid)` calls: the recovery backup is a second copy of the same player-keyed rows,
  so an erasure request now refreshes it immediately rather than leaving the erased rows on disk
  until the next periodic pass.
- `CoreData`'s currency and reputation eraser registration is idempotent and split out, so a
  plain-JVM conformance run can guarantee those seams are wired without a live server.

### Documentation

- [`docs/COMPLIANCE.md`](docs/COMPLIANCE.md) — new "Known gap — team-scoped progression rows survive
  individual erasure" section: what survives, why a scoreboard team name is not inherently personal
  data, the residual risk of a single-member team named after its player, and the recommended
  mitigations (naming policy now, membership-aware purge plus a team-scope admin command in the next
  major). Also documents erasure reaching the recovery backups and the conformance harness.
- [`wiki/Privacy-and-Data.md`](wiki/Privacy-and-Data.md) — the same gap in admin-facing terms, plus
  the two stores that were missing from the "what Core stores" list.
- [`docs/USING-CORE.md`](docs/USING-CORE.md) — how a downstream mod runs the conformance harness and
  adopts `SavedDataRecovery`; [`docs/API-STABILITY.md`](docs/API-STABILITY.md) — both new surfaces
  added to the frozen-API list.

## [1.10.0] - 2026-08-04

Additive public-API release for off-Earth content: a shared "where is space?" tag vocabulary
and a cross-loader entity registration seam. **Every existing API signature, tag, id,
capability and config key is unchanged.**

### Added

**Space tags** (`za.co.neroland.nerolandcore.worldgen.SpaceTags`)

- The `neroland:space/*` vocabulary — biome tags `space/dark_biomes`, `space/moon_biomes`,
  `space/crystalline_biomes`, `space/asteroid_biomes` and the umbrella `space/planet_biomes`,
  plus the dimension-**type** tag `space/dimensions`. Core owns the ids; the mods that own the
  planets supply the members.
- Base membership shipped in `data/neroland/tags/worldgen/biome/space/` and
  `data/neroland/tags/dimension_type/space/`, with **every entry `"required": false`** —
  Nerospace's biomes themed from their own JSON plus `nerospace:gravity_*` membership, and
  best-effort Ad Astra entries on the usual tag-mediated, no-hard-dependency footing.
  `space/planet_biomes` includes the four themed tags by reference.
- Helpers `isSpace(ServerLevel)`, `biomeIn(Holder<Biome>, TagKey<Biome>)` and
  `biomeIn(LevelReader, BlockPos, TagKey<Biome>)`. **Empty tags are a supported state** —
  consumers must read an empty tag as "no such place here", never as an error.

**Entity registration seam** (`za.co.neroland.nerolandcore.entity.EntityRegistrationSupport`)

- Declare a mob's **default attributes** and **natural spawn placement** once from common code:
  `get(modId).registerAttributes(typeSupplier, builderSupplier)` and
  `registerSpawnPlacement(typeSupplier, placementType, heightmap, predicate)`. Both take
  suppliers so a `RegistrationProvider.RegistryEntry` can be passed straight in on the
  deferred-register loaders.
- Buffered in common and flushed by per-loader plumbing (a ServiceLoader seam,
  `EntityRegistrationSupport$Plumbing`): NeoForge `EntityAttributeCreationEvent` /
  `RegisterSpawnPlacementsEvent`, Forge `EntityAttributeCreationEvent` /
  `SpawnPlacementRegisterEvent`, Fabric `FabricDefaultAttributeRegistry` plus the vanilla
  `SpawnPlacements.register` applied immediately.
- Requires **no loader wiring downstream** — Core installs the flush listeners during its own
  bootstrap and the events reach every mod. `attach(loaderEventBus)` is offered for parity
  with `RegistrationProvider.attach(...)` and is idempotent; every buffered entry is applied at
  most once, and a failing entry is logged (mod id only) rather than aborting the batch.
- New docs page [`docs/SPACE-TAGS-AND-ENTITIES.md`](docs/SPACE-TAGS-AND-ENTITIES.md) and wiki
  page [`wiki/Space-Tags-and-Entities.md`](wiki/Space-Tags-and-Entities.md).

## [1.9.0] - 2026-07-17

Additive public-API release for **decor contracts** — the surfaces NeroDecor and the NeroLink
dashboard build against. All new surfaces are cosmetic-only and carry **no player data**;
**every existing API signature, tag, id, capability and config key is unchanged**. See
[`docs/DECOR-CONTRACTS.md`](docs/DECOR-CONTRACTS.md).

### Added

**Palette export** (`za.co.neroland.nerolandcore.palette`)

- `PaletteRegistry`, `Finish` and `CoreFinishes` plus the `neroland:` finish ids — the shared
  material/finish vocabulary a decor mod reads so its blocks match the ecosystem's look without
  hard-coding colours.

**Dashboard content contract** (`za.co.neroland.nerolandcore.link.display`)

- `DisplaySurface`, `DisplaySurfaces`, `DisplayAddress` and `DisplayPayload` — the seam through
  which a mod publishes content to an in-world display or dashboard panel. Sits alongside the
  1.4.0 link API and depends only on Core.

**Decor block tags** (`za.co.neroland.nerolandcore.decor.DecorTags`)

- The `neroland:decor/*` block-tag family, with empty base tag files shipped so downstream mods
  and datapacks can add members immediately. Tag ids are a frozen contract as usual.

**Second creative tab**

- `CoreCreativeTab.NEROLAND_DECOR` and `CoreCreativeTab.addDecor(...)`, backed by the
  `itemGroup.nerolandcore.decor` translation key, so decor content no longer crowds the main tab.

### Build & CI

- New `wiki.yml` workflow publishing the wiki from the in-repo `wiki/` folder.
- Publish workflow updates (release metadata).

## [1.8.0] - 2026-07-13

Additive public-API release for cross-mod material discovery.

### Added

- Typed per-material milestone definitions under
  `data/<namespace>/neroland_material_milestones/*.json`, with player/team/server scopes.
- Server-authoritative `MaterialMilestones` observation/query API, validated observation kinds,
  change events, client sync, direct player export, and shared `PlayerDataErasure` integration.
- Canonical `nerolandcore:material_discovered` definition for owner-mod, planet-visit,
  legitimate player-pickup, and administrator observations.
- Downstream registration attachment and configured-stack contributions to the shared Neroland
  creative tab, allowing component-backed variants without per-variant registry ids.

### Changed

- Formalised **NF (Nero Flux)** as the player-facing power name. Existing energy Java types,
  capability ids, persisted values, config keys, and FE conversion semantics are unchanged.

## [1.7.0] - 2026-07-11

Additive minor — a generic **threshold-crossing event system**, so two mods can react to each
other's scalar quantities without either importing the other. Purely additive; **every existing
API signature, tag, id, capability and config key is unchanged**.

> No `1.6.0` was released; the version went straight from `1.5.0` to `1.7.0`.

### Added

**Threshold events** (`za.co.neroland.nerolandcore.event.ThresholdEvents`)

- A mod that tracks a scalar quantity — Nerotech's regional pollution, a reactor's containment
  stress, a colony's food stock — publishes a `ThresholdCrossing` when the value passes a
  threshold, and any mod (the intended consumer is NeroEvents, reacting with dynamic world
  events) subscribes via `onCrossing(...)`. Both sides depend only on Core.
- `ThresholdCrossing` is a record of `channel` (the quantity, namespaced by the publisher, e.g.
  `nerotech:pollution`), `scope` (publisher-defined key for *where* it crossed), `value`,
  `threshold`, and `rising` (`true` crossing upward, `false` recovering back below).
- Unlike `GateEvents`, `fire(...)` is **public**: the publishers are downstream mods, not Core.
  Listeners run on the server thread, so publishers must fire from server-side code only.
- Exposed through the unified facade as `CoreEvents.onThreshold(...)`.
- **Privacy (POPIA/GDPR):** the `scope` key identifies a **place or system** (a region key, a
  dimension id, a machine class) and never a person — publishers must not encode player UUIDs
  or names into crossings.

### Fixed

- Side-config auto-eject rules corrected.

## [1.5.0] - 2026-07-10

Minor release — adds **item highlights**, a client-side quality-of-life feature: a subtle coloured
border inside inventory slots holding Nero ecosystem items (in the spirit of the classic *Item
Borders* mod, but ecosystem-aware and tag-driven). Purely additive; **every existing API signature,
tag, id, capability and config key is unchanged**.

### Added

**Item highlights** (`za.co.neroland.nerolandcore.client.ItemHighlights`)

- A coloured frame drawn just inside the 16×16 slot, beneath the item — concentric one-pixel rings
  (three by default) fading inwards like a soft glow, and fading towards the top of the slot to stay
  subtle. Rendered from Core's first mixin
  (`mixin/AbstractContainerScreenMixin`, hooked at the head of
  `AbstractContainerScreen.extractSlot`), so it appears in every container screen — vanilla and
  modded — on all three loaders.
- **Colour by category, not by mod**, resolved from four new Core-owned item tags
  (`data/neroland/tags/item/highlight/`; most specific wins):
  `neroland:highlight/machines` (amber), `neroland:highlight/tools` (violet),
  `neroland:highlight/upgrades` (green), `neroland:highlight/materials` (teal).
  Downstream Nero mods add their own items to the same tags (entries `"required": false`,
  `"replace": false`, matching the `neroland:meteor/grindable` precedent); datapacks can retune
  membership with no code.
- Core ships its own membership: all four materials in every form → `materials`; Battery, Fluid
  Tank, Gas Tank, Item Store, Trash Can and the creative source blocks → `machines`. `tools` and
  `upgrades` ship empty for downstream mods.
- **Config** (local-only, client-side, hot-reloadable): `itemHighlightsEnabled` (default `true`),
  `itemHighlightOpacity` (percent, default `65`) and `itemHighlightThickness` (pixels, 1-4,
  default `3`).
- New wiki page [`wiki/Item-Highlights.md`](wiki/Item-Highlights.md); tag and config docs updated.

## [1.4.0] - 2026-07-06

Minor release — introduces the **NeroLink integration surface** (`za.co.neroland.nerolandcore.link`),
the provider SPI the NeroLink Bridge and companion app are built on. The version is a **minor bump
only because a new integration surface enters the frozen-between-majors API**; **every existing API
signature, tag, id, capability and config key is unchanged** and no code is removed or altered — a
mod built against `1.3.x` continues to compile and run against `1.4.0`.

### Added

**NeroLink link API** (`za.co.neroland.nerolandcore.link`)

- The small, loader-neutral provider SPI a Nero mod uses to plug into NeroLink, and the one surface
  the (Core-only) NeroLink Bridge reads from. Core ships **only** the SPI, an event bus and an alert
  store — it registers **no** `core` module itself; the bridge provides `core`'s
  energy/storage/gates/alerts directly from Core's capabilities, so a Core-only server is fully
  functional.
- **Discovery** — `LinkModuleInfo` (module id, mod version, per-module `schemaVersion`, data sections,
  action ids). The bridge builds its discovery response and the app builds its UI from
  `NeroLinkRegistry.modules()`.
- **Read side** — `LinkSnapshotProvider` (`moduleId`, `schemaVersion`, `sections`,
  `snapshot(playerId, section, params)` → Gson `JsonObject`). Everything returned is already scoped
  to the player (own-data-only), so authorisation lives at the seam.
- **Write side** — `LinkActionHandler` (`moduleId`, `actionIds`, `execute(...)` on the server thread,
  `allowOffline(actionId)` defaulting to `false`) returning a `LinkActionResult` — ok + resulting
  state, or a stable `LinkActionResult.Error` code (`NOT_OWNER`, `GATE_LOCKED`, `VALIDATION`,
  `ACTION_DISABLED`, `PLAYER_OFFLINE_REQUIRED`, `INTERNAL`).
- **Live events** — `LinkEvent` (module, topic, nullable player for broadcasts, `JsonObject` payload,
  timestamp) published on a thread-safe `LinkEventBus`; the bridge turns these into WebSocket deltas
  and notifications.
- **Registry** — `NeroLinkRegistry`, the central static registry: register snapshot providers and
  action handlers (each with their `LinkModuleInfo`), look them up by module id, enumerate `modules()`
  for discovery, and reach the shared `LinkEventBus`. Thread-safe (`ConcurrentHashMap` / copy-on-write).
- **Alerts** — `LinkAlerts`, a persistent per-player alert store (`LinkAlert`: id, module, severity,
  text, timestamps, acked/snoozed) behind the `core/alerts` section: raise / list / ack / snooze /
  dismiss. Persisted as vanilla `SavedData` on the overworld using the same `SavedDataType` + Codec
  pattern as `ProgressionState` / `PlayerActivity`, so a Core-only server persists alerts with no
  bridge-side storage.
- **Privacy (POPIA/GDPR)** — `LinkAlerts` is registered with the shared `data.PlayerDataErasure`
  hook, so one erasure request purges a player's alerts alongside every other mod's data. Alert rows
  are keyed only by the owning player's UUID and hold non-personal gameplay metadata; snapshots and
  events are player-scoped by contract; nothing here logs player identity at info.

[1.4.0]: https://github.com/Neroland/neroland-core/releases/tag/v1.4.0

## [1.3.2] - 2026-07-06

Additive patch — cross-mod energy interop and battery quality-of-life. No API removals or
signature changes; the `EnergyLookup` contract is unchanged (the fallback widens what it finds).

### Added

**Batteries push power into adjacent blocks**

- The **Battery** and **Creative Battery** now push stored energy directly into adjacent
  receivers every server tick — machines, pipes, and third-party Forge-Energy blocks — so no
  cable is needed between a battery and its consumer. Per-face rate is the Battery's existing
  10,000 NE/t I/O bound.
- A Battery never pushes into another Battery (two half-full batteries would slosh energy back
  and forth forever). The Creative Battery pushes into everything, Batteries included, so
  creative grid testing needs no cables at all.

**Dust smelting recipes**

- The material dusts now have a use in Core itself: **Nero Alloy Dust** and **Starsteel Dust**
  smelt or blast into their ingots, and **Void Crystal Dust** smelts or blasts into the
  **Void Crystal** gem — six furnace/blast-furnace recipes with tuned XP and cook times.

### Changed

**Standard Forge-Energy fallback in the energy lookup (NeoForge + Forge)**

- `EnergyLookup.find(...)` now falls back to the loader's standard energy capability
  (NeoForge `Capabilities.Energy.BLOCK`, Forge `ForgeCapabilities.ENERGY`) when a block
  exposes no Nero energy, adapting FE↔NE with the config-driven `EnergyConversions` ratio.
  Universal Pipes, batteries, and side-config auto-eject therefore treat third-party FE
  cables and machines (e.g. **Energized Power**) as first-class energy neighbours.
- On NeoForge the **Creative Battery** is additionally exposed on the standard energy
  capability as an infinite FE source, so third-party cables connect to and draw from it
  directly. (Exposing the regular Battery on the standard capability — letting external FE
  networks *pull* from it — is a documented follow-up; it already *pushes* into them.)
- The **Fabric** lookup remains Nero-only until the Team Reborn Energy API ports to 26.x.

### Build & CI

- New auto-assign workflow for newly opened issues and PRs; `/forge/versions` is now ignored.

[1.3.2]: https://github.com/Neroland/neroland-core/releases/tag/v1.3.2

## [1.3.1] - 2026-07-04

Maintenance patch — no API or content changes.

### Changed

- Bumped loader/API dependency pins within the current Minecraft line.
- Publish workflow updates (release metadata).

[1.3.1]: https://github.com/Neroland/neroland-core/releases/tag/v1.3.1

## [1.3.0] - 2026-06-29

Additive minor — new APIs and content only; no removals. Every existing API signature,
tag, id, and capability is unchanged (frozen-between-majors policy, see
[`docs/API-STABILITY.md`](docs/API-STABILITY.md)).

### Added

**Universal machine side configuration** (`za.co.neroland.nerolandcore.sideconfig`)

- A Mekanism-style, per-face routing system every machine inherits — by extending the base
  `AbstractMachineBlockEntity`, or by composing a `SideConfigComponent` (so a mod with its own
  machine hierarchy adopts it without re-parenting). See
  [`docs/SIDE-CONFIG.md`](docs/SIDE-CONFIG.md).
- **Model:** `Channel` (`ITEM`/`FLUID`/`GAS`/`ENERGY`), `SideMode`
  (`DISABLED`/`INPUT`/`OUTPUT`/`IO`/`PUSH`), `RelativeFace` (relative to facing, resolved each
  query via `FaceResolver`), `SlotGroup`, `SidePreset`
  (`GENERATOR`/`PROCESSOR`/`STORAGE`/`ALL_INPUT`/`ALL_DISABLED`), and a `SideConfig.builder()`
  author API (declare channels, bind slot groups, set a preset, `allow(...)` to forbid modes).
- **Capability gating:** Core exposes a `(face, channel)` capability only when the resolved
  mode permits it — `INPUT` insert-only, `OUTPUT` extract-only, `IO` full, `DISABLED` none —
  wired to the item / `NeroFluidStorage` / `NeroGasStorage` / energy seams, with neighbour
  update + capability invalidation on every change.
- **Auto-eject / auto-input:** optional per-channel toggles, off by default, bounded by the new
  server-authoritative `sideConfigAutoTransferRate` config value.
- **Persistence & sync:** compact per-channel packed-int NBT (survives unload + dimension
  travel); server-authoritative `SideConfigIntentPayload` / `SideConfigSyncPayload` — clients
  send intents, never mutate routing directly.
- **UI:** a reusable `SideConfigWidget` — flattened cube/net, click-to-cycle, colour-coded
  modes, per-channel sub-tabs, auto-eject/auto-input toggles, copy/paste, reset-to-preset.
- **Configurator API:** `sideconfig.Configurator` (cycle / read / snapshot / apply) for an
  in-world wrench item shipped by a content mod (Nerotech).
- **Privacy:** side config is block/world data keyed by position — no player identity, not
  routed through `PlayerDataErasure`, never logged at info, no telemetry (POPIA/GDPR).

[1.3.0]: https://github.com/Neroland/neroland-core/releases/tag/v1.3.0

## [1.2.0] - 2026-06-29

Additive minor — introduces the **Meteor Material Registry**, the datapack-driven pool the
meteor grinder rolls against. New APIs and content only; no removals. Every existing API
signature, tag, id, and capability is unchanged. See
[`docs/METEOR-MATERIAL-REGISTRY.md`](docs/METEOR-MATERIAL-REGISTRY.md).

### Added

**Meteor Material Registry** (`za.co.neroland.nerolandcore.meteor`)

- `MeteorMaterialRegistry` — the central registry of grindable meteor materials, plus the
  `MeteorMaterialEntry` model, the `MeteorTier` rarity scale (common / uncommon / rare),
  `MeteorPlanets` for planet binding, and `MeteorResolution`, the weighted resolution
  algorithm that picks an output for a finished grind.
- **Datapack format** `data/<namespace>/meteor_materials/*.json`, so a pack or a downstream mod
  contributes materials with no code. Core ships four entries of its own: `nero_alloy`,
  `starsteel`, `void_crystal` and `plasma_glass`.
- **Tags** — `MeteorMaterialTags` and the `neroland:meteor/grindable` item tag naming what the
  grinder will accept as input.
- **Annotation SPI** — `@GrindableMaterial` plus the `MeteorAnnotationScanner` seam, with a
  per-loader scanner on Fabric, Forge and NeoForge, so a mod can declare grindables in code
  instead of JSON. Malformed declarations are logged and skipped rather than failing the load.
- **Config** (server-authoritative, hot-reloadable; read live on every roll) — the base tier
  weights `meteorTierBaseWeightCommon` (60), `meteorTierBaseWeightUncommon` (25) and
  `meteorTierBaseWeightRare` (12), the `meteorPlanetBias` multiplier applied when grinding
  inside a material's bound planet dimension (2.0), and `meteorExoticChance`, the per-grind
  probability that the separate exotic bonus pool also fires (0.08).
- **Commands** — `/neroland meteor list` and `/neroland meteor reload`.

### Build & CI

- Core is now published to **GitHub Packages**, which is how every downstream mod resolves it
  at build time.
- New [`USING-CORE.md`](USING-CORE.md) — the downstream integration guide.

## [1.1.0] - 2026-06-29

Additive minor — new APIs and content only; no removals. Every existing API signature,
tag, id, and capability is unchanged.

### Added

**Storage blocks (moved in from Nerospace)**

- **Battery**, **Fluid Tank**, **Gas Tank**, **Item Store**, and the **Trash Can** — passive
  storage endpoints, plus a **Creative** variant of the first four (`nerolandcore:creative_*`).
  These are Core's first block-entities. New ids: `nerolandcore:battery`,
  `nerolandcore:fluid_tank`, `nerolandcore:gas_tank`, `nerolandcore:item_store`,
  `nerolandcore:trash_can`.
- The **Trash Can** is a bottomless void sink: pipe or hopper items, fluid, or gas into it
  and they are discarded (input-only, no extraction). It opens a vanilla chest-style GUI with
  a single drop slot that voids on the next insert, and brought Core's **first menu type plus
  the client screen infrastructure** (a `MenuType` registration seam and a per-loader client
  screen registration hook).
- All are pickaxe / iron-tier mineable, drop themselves, and ship crafting recipes whose
  ingredients reference the existing `#c:` material tags (the Trash Can is cactus + iron
  ingots).
- Behaviour is unchanged from Nerospace except, because Core ships no specific fluids or
  gases, two generic defaults: the **Creative Fluid Tank** now starts **empty** (right-click
  a filled bucket to set its endless fluid) and the **Creative Gas Tank** now **learns its
  gas from the first gas piped into it**.

**Generic fluid & gas storage APIs**

- **Fluid** — `NeroFluidStorage` (contract) + `FluidBuffer` (bounded impl) + the cross-mod
  `nerolandcore:fluid` capability.
- **Gas** — `NeroGasStorage` (contract) + `GasBuffer` (bounded impl) + `NeroGases` (helper;
  gases identified by an `Identifier`) + the cross-mod `nerolandcore:gas` capability.
- Both mirror the existing energy seam (`NeroEnergyStorage` / `nerolandcore:energy`):
  downstream mods register their own block-entities against `nerolandcore:fluid` /
  `nerolandcore:gas` exactly as they do against `nerolandcore:energy`.

[1.1.0]: https://github.com/Neroland/neroland-core/releases/tag/v1.1.0

## [1.0.1] - 2026-06-28

Maintenance patch — documentation and build plumbing only. No API, content or data-contract
changes.

### Added

- The V1 wiki, and refreshed project docs.

### Build & CI

- Maven-local publishing enabled across all three loaders, so a downstream mod can build
  against an unreleased Core.

## [1.0.0] - 2026-06-28

**First release.** Core's public API opens here and is frozen until `2.0` — see
[`docs/API-STABILITY.md`](docs/API-STABILITY.md). Every mod in the Neroland ecosystem hard-depends
on this and consumes its shared APIs.

### Added

**Platform & registration seams**

- `RegistrationProvider`, `Services`, `IPlatformHelper` and `NetworkPlatform` — the multiloader
  seams that let common code register content and send packets once and run on NeoForge, Forge
  and Fabric alike. Plus the Gradle MCP tooling the whole ecosystem builds with.

**Materials, tags & datapacks**

- The four Core materials and their forms — **Nero Alloy** and **Starsteel** (ingot, nugget,
  dust, plate, block), **Void Crystal** (gem, shard, dust, block) and **Plasma Glass** (item,
  block, pane) — the shared Neroland creative tab, and the `c:` + `neroland:` tag set that is
  the sole integration path to third-party mods. Tag ids are a frozen contract; see
  `docs/TAGS-AND-DATAPACKS.md`.

**Config framework**

- `ConfigManager`, `ConfigSchema`, `ConfigValue` and the `CoreConfig` keys — a typed,
  validated, hot-reloadable config with server-authoritative values synced to clients, plus the
  `/neroland` command framework.

**Progression gates**

- `ProgressionGates`, the `CoreGates` arc ids (`industrial_power`, `reached_orbit`,
  `first_colony`, `deep_space`), `GateScope` (server / team / player), `GateEvents`,
  `ClientGates`, the `neroland_gates` datapack format and the `ProgressionState` store — the
  shared "has the server unlocked this yet?" vocabulary every mod gates content behind.

**Economy & reputation APIs**

- `CurrencyApi`, `Currency`, `CurrencyProvider`, `CurrencyEvents`; `ReputationApi`,
  `ReputationProvider`, `ReputationEvents`. **Core defines these but stores nothing** —
  NeroEconomy and NeroFactions supply the providers; Core ships in-memory defaults so a
  Core-only server still works.

**Machines, energy & upgrades**

- `NeroEnergyStorage`, `EnergyBuffer`, `EnergyConversions` (NE↔FE), `EnergyLookup` and the
  per-loader energy capability objects, `AbstractMachineBlockEntity`, and the upgrade framework
  (`UpgradeType`, `UpgradeContainer`, `UpgradeModifiers`).

**Events facade**

- `CoreEvents`, the unified event-bus facade over Core's individual event surfaces.

**Data & compliance (POPIA/GDPR)**

- `PlayerDataErasure` and `PlayerDataEraser` — the shared per-player erasure hook every
  Core-storing system and downstream mod registers with, so one request purges a player across
  the whole ecosystem. `PlayerActivity` backs the `dataRetentionDays` inactivity sweep. Erasure
  never logs player identity.

**Telemetry**

- Opt-out Sentry error reporting (EU-hosted, `sendDefaultPii` disabled, scrubbed, capped and
  de-duplicated), disabled with `telemetryEnabled=false`. Full disclosure in
  [`PRIVACY.md`](PRIVACY.md). Logo and store assets shipped alongside.

### Notes

- Implementation types are annotated `@ApiStatus.Internal` — that annotation, not a package
  split, is the enforced api/impl boundary.

[1.0.0]: https://github.com/Neroland/neroland-core/releases/tag/v1.0.0
[1.0.1]: https://github.com/Neroland/neroland-core/releases/tag/v1.0.1
[1.2.0]: https://github.com/Neroland/neroland-core/releases/tag/v1.2.0
[1.5.0]: https://github.com/Neroland/neroland-core/releases/tag/v1.5.0
[1.7.0]: https://github.com/Neroland/neroland-core/releases/tag/v1.7.0
[1.8.0]: https://github.com/Neroland/neroland-core/releases/tag/v1.8.0
[1.9.0]: https://github.com/Neroland/neroland-core/releases/tag/v1.9.0
[1.10.0]: https://github.com/Neroland/neroland-core/releases/tag/v1.10.0
