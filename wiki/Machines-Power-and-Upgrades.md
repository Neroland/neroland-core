# Machines, Power, and Upgrades

Neroland Core provides a **machine framework** that other Neroland mods build
on. Core itself ships **no machines, generators, or upgrade items** — it defines
the power unit, the energy contracts, the upgrade resolver, and the cross-loader
energy interop so every Nero machine speaks the same language. It does, however,
ship a few **passive storage blocks** (see below) that ride these contracts.

## Nero energy (NE)

The shared power unit is **Nero energy (NE)**.

| Piece | Role |
| --- | --- |
| `NeroEnergyStorage` | The loader-neutral energy contract |
| `EnergyBuffer` | Ready bounded implementation: generate, consume, insert/extract, bounded by `maxIO` |
| `EnergyConversions` | Converts to/from Forge Energy via the `neroEnergyToForgeEnergyRatio` config ratio |

`AbstractMachineBlockEntity` bundles an `EnergyBuffer` plus an
`UpgradeContainer` and persists both. Downstream machines extend it.

## Upgrade modules

Machines accept upgrade modules that change how they run. The module **types**
are fixed by Core; the **items** are supplied by downstream mods.

| Upgrade type | Effect |
| --- | --- |
| SPEED | Faster operation (`speedMultiplier`) |
| EFFICIENCY | Lower energy use (`energyMultiplier`) |
| RANGE | Larger working area (`rangeBonus`) |
| CAPACITY | Larger energy buffer (`capacityMultiplier`) |

How it fits together:

- a machine has an `UpgradeContainer` of slots
- a Classifier maps an item stack to a type, usually via a tag check
- `UpgradeModifiers` turns module counts into multipliers

Because every machine across every Nero mod uses the **one** resolver, modules
are interchangeable and balance is tuned in a single place — the config:

- `upgradeStackingDiminish` sets the diminishing-returns curve for stacked
  modules
- `upgradeModuleSlotCap` clamps the slot count
- hard caps bound the resulting multipliers

Core does **not** ship module items; a downstream mod defines the items and the
stack-to-type mapping.

## Fluid and gas storage

Core mirrors the energy seam for two more resource types so mods can pipe fluids
and gases the same way they pipe NE. Both are **generic** — Core ships no specific
fluids or gases, only the contracts and their cross-loader capabilities.

| Piece | Role |
| --- | --- |
| `NeroFluidStorage` | The loader-neutral fluid contract |
| `FluidBuffer` | Ready bounded single-fluid implementation (mB-measured) |
| `NeroGasStorage` | The loader-neutral gas contract |
| `GasBuffer` | Ready bounded single-gas implementation |
| `NeroGases` | Helper for naming/resolving gases (each identified by an id) |

Core owns the shared `nerolandcore:fluid` and `nerolandcore:gas` capabilities on
each loader, exactly like `nerolandcore:energy`.

## Storage blocks Core ships

Core ships four **passive storage endpoints** (its first block-entities), plus a
Creative variant of each. They hold a resource and offer it to pipes and hoppers,
but do no work — they are not machines:

| Block | Holds | Page |
| --- | --- | --- |
| **Battery** | Nero energy (NE) | [Battery](Battery.md) |
| **Fluid Tank** | one fluid | [Fluid Tank](Fluid-Tank.md) |
| **Gas Tank** | one gas | [Gas Tank](Gas-Tank.md) |
| **Item Store** | items (vanilla container) | [Item Store](Item-Store.md) |

The Battery exposes `nerolandcore:energy`, the Fluid Tank `nerolandcore:fluid`, the
Gas Tank `nerolandcore:gas`, and the Item Store the standard vanilla item handler
(it opens the vanilla chest GUI). All four are pickaxe / iron-tier mineable and ship
crafting recipes (see [Recipes](Recipes.md)). The
[Creative Source Blocks](Creative-Source-Blocks.md) are endless source/sink variants
for testing.

## Cross-loader energy interop

Core owns the shared `nerolandcore:energy` capability/lookup on each loader
through the `EnergyLookup` seam. Downstream machines register their block-entity
against Core's capability, so any Nero machine (from any mod) can find any
other. The same pattern applies to `nerolandcore:fluid` and `nerolandcore:gas`.
Bridging to external Forge Energy or Fabric energy libraries is deferred until
those libraries port to MC 26.x.

## For developers

The full machine, power, and upgrade API is documented in
[../docs/MACHINES-POWER-UPGRADES.md](../docs/MACHINES-POWER-UPGRADES.md).

## See also

- [Configuration](Configuration.md)
- [Tags and Datapacks](Tags-and-Datapacks.md)
- [For Developers](For-Developers.md)
- [Home](Home.md)
