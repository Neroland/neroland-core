# Battery

A passive **Nero energy (NE)** store that buffers your power grid. The Battery is one of the
four storage blocks Neroland Core ships (see [Machines, Power & Upgrades](Machines-Power-and-Upgrades.md));
it stores energy but does no work of its own.

> These blocks moved into Core in `1.1.0` from Nerospace, so every Neroland mod shares one
> set of storage endpoints. Existing `nerospace:battery` placements do not auto-migrate —
> see the [Nerospace changelog](https://github.com/Neroland/nerospace) for details.

## How it works

- Holds a large buffer of **NE**, accepting and providing power on **every side**.
- Generators fill it through the network; machines drain it the same way — so a production
  hiccup doesn't black out your base.
- Exposes Core's `nerolandcore:energy` capability, so any Nero machine, cable, or pipe (from
  any Neroland mod) can charge or draw from it.

A **Creative Battery** variant (creative tab only) is an endless source and sink of energy
for testing grids — see [Creative Source Blocks](Creative-Source-Blocks.md).

## Details

- ID: `nerolandcore:battery` · Tool: pickaxe, iron tier · Drops: itself
- Recipe: see [Recipes](Recipes.md).

## See also

- [Machines, Power & Upgrades](Machines-Power-and-Upgrades.md)
- [Fluid Tank](Fluid-Tank.md)
- [Gas Tank](Gas-Tank.md)
- [Item Store](Item-Store.md)
- [Recipes](Recipes.md)
- [Home](Home.md)
