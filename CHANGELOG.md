# Changelog

All notable changes to **Neroland Core** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
See [`docs/API-STABILITY.md`](docs/API-STABILITY.md) for the versioning policy.

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
