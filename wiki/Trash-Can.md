# Trash Can

A bottomless void sink for unwanted resources. The Trash Can is one of the storage blocks
Neroland Core ships (see [Machines, Power & Upgrades](Machines-Power-and-Upgrades.md)); pipe or
hopper anything into it and it is destroyed.

> This block moved into Core in `1.1.0` from Nerospace. Existing `nerospace:trash_can`
> placements are remapped to `nerolandcore:trash_can` on load by Core's block-id alias
> mechanism — see the
> [Nerospace changelog](https://github.com/Neroland/nerospace) for details.

## How it works

- **Voids every layer:** accepts **items, fluid, and gas** from any side and discards them —
  there is no extraction surface, so nothing can ever be pulled back out.
- **Never backs up:** its internal sinks are emptied so it always has room and never rejects or
  returns anything.
- **Input only:** there is no way to extract from it; only what is explicitly piped or hoppered
  *into* the Trash Can is lost.
- **No energy:** it does not accept power (energy isn't "trash") — only items, fluid, and gas.

## GUI

Right-click to open a **vanilla chest-style GUI** with a **single drop slot**: drop a stack in
and it is voided on the next insert. This is Core's first menu type, backed by Core's client
screen infrastructure.

## Details

- ID: `nerolandcore:trash_can` · Tool: pickaxe, iron tier · Drops: itself
- Recipe: a ring of iron ingots around a cactus (see [Recipes](Recipes.md)).

## See also

- [Machines, Power & Upgrades](Machines-Power-and-Upgrades.md)
- [Battery](Battery.md)
- [Fluid Tank](Fluid-Tank.md)
- [Gas Tank](Gas-Tank.md)
- [Item Store](Item-Store.md)
- [Recipes](Recipes.md)
- [Home](Home.md)
