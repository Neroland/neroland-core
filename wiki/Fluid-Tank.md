# Fluid Tank

A passive single-fluid store. The Fluid Tank is one of the four storage blocks Neroland Core
ships (see [Machines, Power & Upgrades](Machines-Power-and-Upgrades.md)); it holds one fluid
and offers it up for pipes and buckets.

> These blocks moved into Core in `1.1.0` from Nerospace. Existing `nerospace:fluid_tank`
> placements do not auto-migrate — see the
> [Nerospace changelog](https://github.com/Neroland/nerospace) for details.

## How it works

- Holds **one** fluid at a time, measured in millibuckets (mB).
- **Buckets:** right-click with a filled bucket to pour in, an empty bucket to draw out.
- **Pipes:** fills and drains on **every side**, through both Core's `nerolandcore:fluid`
  capability and the loader's **standard** fluid capability (NeoForge `Capabilities.Fluid`,
  Forge `FLUID_HANDLER`, Fabric `FluidStorage.SIDED`) — so any Neroland mod's fluid pipe *and*
  any other mod's fluid pipe can move fluid in or out. The tank also draws from, and fills,
  third-party tanks it sits next to.
- Bare-hand right-click reads out the contents.

Core ships no specific fluids — the tank holds whatever fluid a downstream mod or vanilla
provides.

A **Creative Fluid Tank** variant supplies an endless fluid of your choice — see
[Creative Source Blocks](Creative-Source-Blocks.md).

## Details

- ID: `nerolandcore:fluid_tank` · Tool: pickaxe, iron tier · Drops: itself
- Recipe: see [Recipes](Recipes.md).

## See also

- [Machines, Power & Upgrades](Machines-Power-and-Upgrades.md)
- [Battery](Battery.md)
- [Gas Tank](Gas-Tank.md)
- [Item Store](Item-Store.md)
- [Recipes](Recipes.md)
- [Home](Home.md)
