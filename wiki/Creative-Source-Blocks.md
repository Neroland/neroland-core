# Creative Source Blocks

Endless sources (and voids) for testing networks — **creative tab only**, unbreakable in
survival, no recipes. There is a creative variant of three of Core's
[storage blocks](Machines-Power-and-Upgrades.md): the Battery, Fluid Tank, and Gas Tank.
(The plain [Item Store](Item-Store.md) covers most testing on its own; downstream mods may
add an endless item source.)

> These blocks moved into Core in `1.1.0` from Nerospace. They are generic — Core ships no
> specific fluids or gases — so the fluid and gas sources are configured in-world rather
> than hard-wired.

| Block | Provides | Configure |
| --- | --- | --- |
| **Creative Battery** | endless energy; accepts (voids) any inserted NF | — |
| **Creative Fluid Tank** | endless fluid of your choice | starts **empty** — right-click with a **filled bucket** to set the fluid |
| **Creative Gas Tank** | endless gas | **learns its gas from the first gas piped into it** |

Each also acts as a void: anything pushed into it disappears, which makes them ideal sinks
for throughput testing.

## Notes on the generic defaults

Because Core is a generic library and ships no specific fluids or gases, two of these blocks
are not pre-filled:

- **Creative Fluid Tank** — starts empty; the fluid you set with a filled bucket becomes its
  endless supply.
- **Creative Gas Tank** — takes on whichever gas is first piped into it, then supplies that
  gas endlessly.

A downstream mod that ships its own fluids/gases (for example Nerospace's rocket fuel and
oxygen) provides the buckets and gas sources used to configure these.

## Details

- IDs: `nerolandcore:creative_battery`, `nerolandcore:creative_fluid_tank`,
  `nerolandcore:creative_gas_tank` (and `nerolandcore:creative_item_store` where present).
- Creative tab only · unbreakable in survival · no crafting recipe.

## See also

- [Machines, Power & Upgrades](Machines-Power-and-Upgrades.md)
- [Battery](Battery.md)
- [Fluid Tank](Fluid-Tank.md)
- [Gas Tank](Gas-Tank.md)
- [Item Store](Item-Store.md)
- [Home](Home.md)
