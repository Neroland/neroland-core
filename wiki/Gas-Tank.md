# Gas Tank

A pressurised store for **one gas**. The Gas Tank is one of the four storage blocks Neroland
Core ships (see [Machines, Power & Upgrades](Machines-Power-and-Upgrades.md)); it holds a
single gas and exchanges it through the gas capability.

> These blocks moved into Core in `1.1.0` from Nerospace. Existing `nerospace:gas_tank`
> placements do not auto-migrate — see the
> [Nerospace changelog](https://github.com/Neroland/nerospace) for details.

## How it works

- Holds **one** gas at a time, measured in millibuckets (mB).
- Fills and drains on **every side** through Core's `nerolandcore:gas` capability, so any
  Neroland mod's gas pipe can move gas in or out.
- Bare-hand right-click reads out the contents.

Core ships no specific gases — the tank holds whichever gas a downstream mod pipes into it
(gases are identified internally by an id; the tank takes on the first gas it receives).

A **Creative Gas Tank** variant supplies an endless gas — see
[Creative Source Blocks](Creative-Source-Blocks.md).

## Details

- ID: `nerolandcore:gas_tank` · Tool: pickaxe, iron tier · Drops: itself
- Recipe: see [Recipes](Recipes.md).

## See also

- [Machines, Power & Upgrades](Machines-Power-and-Upgrades.md)
- [Battery](Battery.md)
- [Fluid Tank](Fluid-Tank.md)
- [Item Store](Item-Store.md)
- [Recipes](Recipes.md)
- [Home](Home.md)
