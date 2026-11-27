# Ores

Since `1.25.0` every Nero ore, and the raw material it drops, is a Neroland Core block or item. They
use `nerolandcore:` ids and appear in Core's creative tab. Core does **not** generate them: each ore's
**home mod** still places it in the world exactly where it did before.

| Ore block | Drops | Home mod | Found on |
| --- | --- | --- | --- |
| Nerosium Ore, Deepslate Nerosium Ore | Raw Nerosium | Nerospace | Overworld, Luna, Nyxara |
| Nerosteel Ore | Raw Nerosteel | Nerospace | Greenxertz, Mars |
| Xertz Quartz Ore | Xertz Quartz | Nerospace | Greenxertz |
| Amber Sap Ore | Amber Sap | Nerospace | Greenxertz |
| Ilmenite Ore | Raw Ilmenite | Nerospace | Luna |
| Cheese Ore | Moon Cheese (food) | Nerospace | Luna |
| Hematite Ore | Raw Hematite | Nerospace | Mars |
| Cindrite Ore | Cindrite | Nerospace | Cindara |
| Glacite Ore | Glacite | Nerospace | Glacira |
| Voidglass Ore | Voidglass | Nerospace | Nyxara |
| Brine Salt Ore | Brine Salt | Nerospace | Europa |
| Vent Sulfide Ore | Raw Vent Sulfide | Nerospace | Europa |
| Europium Ore | Raw Europium | Nerospace | Europa |
| Clathrate Ore (since 1.26.0) | Methane Clathrate | Nerospace | Titan |
| Embedded Meteor Ore | Nero Alloy Dust or Starsteel Dust | NeroEvents | meteor craters |

Also moved: the **Block of Raw Nerosium** (with its two compaction recipes) and NeroPower's
**Pallurite Dust**, which has no ore block (it is centrifuged from tuff and fertile sources).

Every ore needs a pickaxe; the tool tier and the `c:ores/*`, `c:raw_materials/*`, `c:gems/*` and
`c:dusts/*` tags are the same as before. Smelting and processing recipes stay in the mods that own
the machines and ingots.

## Home-mod behaviour

Some ores keep extra behaviour while their home mod is installed. With Nerospace, Hematite Ore pulls its
drops toward the miner, and the Europa ores change their drops with the liquid next to them. Without
Nerospace they are plain ores that drop through their loot tables.

## Old worlds

Blocks, items and stored stacks saved under the old ids (`nerospace:hematite_ore`,
`neroevents:embedded_meteor_ore`, `neropower:pallurite_dust` and so on) load as the Core ids. This works
on Fabric, Forge and NeoForge, and keeps working if the old home mod is later removed. Datapacks that
still name an old id keep resolving too, but should switch to the `nerolandcore:` id or a `c:` tag.

## Without the home mod

If an ore's home mod is not installed, the [Ore Reclaimer](Ore-Reclaimer.md) turns it into a vanilla
material.
