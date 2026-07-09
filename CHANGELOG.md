# Changelog

All notable changes to **Neroland Core** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
See [`docs/API-STABILITY.md`](docs/API-STABILITY.md) for the versioning policy.

## [1.5.0]

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

## [1.4.0]

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

## [1.3.2]

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

## [1.3.1]

Maintenance patch — no API or content changes.

### Changed

- Bumped loader/API dependency pins within the current Minecraft line.
- Publish workflow updates (release metadata).

[1.3.1]: https://github.com/Neroland/neroland-core/releases/tag/v1.3.1

## [1.3.0]

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

## [1.1.0]

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
