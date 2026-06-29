# Meteor Material Registry

The Meteor Material Registry is a shared list of **grindable materials** that Neroland
Core keeps for the whole ecosystem. Machines that turn something into a *random* Nero
material — first of all the Nerospace **meteor grinder** — read this list instead of
hard-coding their own. Any mod (or you, with a datapack) can add a material to it, and
the odds rebalance automatically.

> Core itself does not add a grinder — it owns the list. You will see this list in
> action through mods that consume it, such as Nerospace.

## What's in the list by default

Core ships its four base materials:

| Material | Output | Rarity | Unlocked by |
| -------- | ------ | ------ | ----------- |
| Nero Alloy | Nero Alloy Dust | common | always |
| Plasma Glass | Plasma Glass | uncommon | always |
| Void Crystal | Void Crystal Dust | uncommon | reaching orbit |
| Starsteel | Starsteel Dust | rare | reaching orbit |

Other mods add their own (planet ores, exotic drops, …). If a mod isn't installed, its
entries simply aren't there — nothing breaks.

## How the odds work

Each material has a **rarity tier** that sets its base weight, and the grinder picks
one at random in proportion to those weights — but only from the materials you've
actually unlocked. As you progress and more materials become eligible, the common ones
naturally become rarer, with no fixed tables anywhere.

Worked through with Core's materials:

- **Before reaching orbit:** Nero Alloy Dust ~70.6%, Plasma Glass ~29.4%.
- **After reaching orbit:** ~49.2% / 20.5% / 20.5% / 9.8% across the four.
- **On a colony planet (with a planet-bound ore):** the local ore joins the pool with a
  bias, diluting the rest further; off that planet it disappears again.

There's also a small separate chance (8% by default) for a bonus **exotic** drop from
mods that register exotic materials.

## Tuning it (server owners)

The levers live in Core's config (`config/nerolandcore.properties`) and apply after
`/neroland config reload`:

- `meteorTierBaseWeightCommon` / `…Uncommon` / `…Rare` — the base weights (default 60 /
  25 / 12).
- `meteorPlanetBias` — how strongly a planet favours its own ore (default 2.0).
- `meteorExoticChance` — the exotic bonus chance per grind (default 0.08).

Inspect the live list in-game with `/neroland meteor list`, and re-read material files
with `/neroland meteor reload` (operator only). See [Commands](Commands.md) and
[Configuration](Configuration.md).

## Adding or changing materials with a datapack

Drop one JSON per material in a datapack at
`data/<your_namespace>/neroland/meteor_materials/<id>.json`:

```json
{
  "item": "yourmod:cobalt_dust",
  "tier": "uncommon",
  "min_gate": "nerolandcore:reached_orbit",
  "planet": null,
  "weight_override": null,
  "enabled": true
}
```

- `tier` is one of `common`, `uncommon`, `rare`, `exotic` (`exotic` entries must set a
  `weight_override`).
- `min_gate` is a [progression gate](Progression-Gates.md) id, or `null` for "always".
- `planet` binds the material to one dimension (a Nerospace planet id), or `null` for
  anywhere.
- To **retune** an existing material, ship a file at the same path in a higher-priority
  datapack — you only need to include the fields you want to change; the rest are kept.
  An explicit `null` clears a field.

Membership for recipe viewers is the tag `neroland:meteor/grindable`; add your item
there too if you want it recognised as grindable by JEI/REI and compat layers.

Mod authors can instead annotate items in code with `@GrindableMaterial` — see the
[developer docs](../docs/METEOR-MATERIAL-REGISTRY.md).

## Privacy

The registry is just item metadata — it stores nothing about players, so there's
nothing to export or erase. See [Privacy & Data](Privacy-and-Data.md).
