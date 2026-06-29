# Changelog

All notable changes to **Neroland Core** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
See [`docs/API-STABILITY.md`](docs/API-STABILITY.md) for the versioning policy.

## [1.1.0]

Additive minor — new APIs and content only; no removals. Every existing API signature,
tag, id, and capability is unchanged.

### Added

**Storage blocks (moved in from Nerospace)**

- **Battery**, **Fluid Tank**, **Gas Tank**, and **Item Store** — four passive storage
  endpoints, plus a **Creative** variant of each (`nerolandcore:creative_*`). These are
  Core's first block-entities. New ids: `nerolandcore:battery`, `nerolandcore:fluid_tank`,
  `nerolandcore:gas_tank`, `nerolandcore:item_store`.
- All four are pickaxe / iron-tier mineable, drop themselves, and ship crafting recipes
  whose ingredients reference the existing `#c:` material tags.
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
