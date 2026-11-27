# Ore Reclaimer

An unpowered block that slowly turns a Nero ore or raw material into a vanilla one. It is meant for packs
that keep Neroland Core but drop the mod an ore comes from, so leftover ores are still useful.

## Crafting

| | | |
| --- | --- | --- |
| Smooth Stone | Hopper | Smooth Stone |
| Iron Ingot | Furnace | Iron Ingot |
| Smooth Stone | Smooth Stone | Smooth Stone |

## How it works

- Put an ore or raw material in the left slot. The bar fills, and the vanilla result appears on the right.
- It is slow: **80 seconds per ore block** and **40 seconds per raw item** (eight and four times a
  furnace), and **160 seconds** for a Block of Raw Nerosium. It needs no power or fuel.
- It **refuses** an ore while that ore's home mod is installed: the status line reads *Its home mod is
  installed: use it there*. Mine and smelt it the normal way instead.
- **Automation:** hoppers and pipes insert from the top and sides, and extract from the bottom. They
  only insert items the machine will convert right now.
- Breaking the block drops its contents.

## Default conversions

| Input | Result | Only while missing |
| --- | --- | --- |
| Nerosium Ore / Deepslate Nerosium Ore | Copper Ore / Deepslate Copper Ore | Nerospace |
| Raw Nerosium · Block of Raw Nerosium | Raw Copper · Block of Raw Copper | Nerospace |
| Nerosteel Ore · Raw Nerosteel | Iron Ore · Raw Iron | Nerospace |
| Hematite Ore · Raw Hematite | Iron Ore · Raw Iron | Nerospace |
| Ilmenite Ore · Raw Ilmenite | Iron Ore · Raw Iron | Nerospace |
| Xertz Quartz Ore · Xertz Quartz | Nether Quartz Ore · Nether Quartz | Nerospace |
| Cindrite Ore · Cindrite | Gold Ore · Raw Gold | Nerospace |
| Glacite Ore · Glacite | Lapis Lazuli Ore · 3 Lapis Lazuli | Nerospace |
| Voidglass Ore · Voidglass | Block of Amethyst · 2 Amethyst Shards | Nerospace |
| Europium Ore · Raw Europium | Redstone Ore · 2 Glowstone Dust | Nerospace |
| Vent Sulfide Ore · Raw Vent Sulfide | Copper Ore · Raw Copper | Nerospace |
| Brine Salt Ore · Brine Salt | Calcite · Sugar | Nerospace |
| Clathrate Ore · Methane Clathrate | Packed Ice · Ice | Nerospace |
| Amber Sap Ore · Amber Sap | Honeycomb Block · Honeycomb | Nerospace |
| Cheese Ore · Moon Cheese | 2 Bread · Bread | Nerospace |
| Embedded Meteor Ore | Deepslate Iron Ore | NeroEvents |
| Pallurite Dust | Glowstone Dust | NeroPower |

## Datapacks

Conversions are JSON files in `data/<namespace>/neroland/ore_reclaiming/`. A file with the same id
replaces Core's, and `/reload` applies changes.

```json
{
  "input": "nerolandcore:hematite_ore",
  "result": { "id": "minecraft:iron_ore", "count": 1 },
  "time": 1600,
  "when_absent": ["nerospace"]
}
```

`input` is an item id or a `#tag`. `time` is ticks per item (default 1600). The conversion runs only while
none of the mods in `when_absent` is installed; leave the list empty to make it always run.

## Details

- ID: `nerolandcore:ore_reclaimer` · Tool: pickaxe · Drops: itself
