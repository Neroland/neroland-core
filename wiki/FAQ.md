# FAQ

## Does Neroland Core add gameplay on its own?

Not much. Core is a **library**: it ships some crafting materials, tags, and a stack of APIs that other
Neroland mods build on. Install it because a mod you want requires it. See
[Getting Started](Getting-Started.md).

## Which Minecraft versions and loaders are supported?

Minecraft **26.1.2 and 26.2** on **NeoForge, Forge, and Fabric**. On Fabric you also need Fabric API.

## What materials does Core add?

Nero Alloy, Starsteel, Void Crystal, and Plasma Glass, plus their forms (ingots, nuggets, dusts,
plates, shards, gems, blocks, panes). See [Materials](Materials.md). Core deliberately ships **no ores** —
the mods that introduce a material's ore smelt it into these Core items.

## Why do the textures look like placeholders?

The 1.0.0 textures are procedural placeholders meant to be re-skinned later. Functionality and tags are
final; the art is not.

## Can a server pack retune Core without code?

Yes. Recipes, loot tables, tags, mining requirements, and gate definitions are all plain data and
datapack-overridable, and numeric balance lives in config. See
[Tags & Datapacks](Tags-and-Datapacks.md) and [Configuration](Configuration.md).

## Does Core have machines or power?

Core ships the **framework** (Nero Flux, a base machine block-entity, the upgrade-module system) but
no concrete machines or generators — those come from mods like Nerotech and NeroPower. See
[Machines, Power & Upgrades](Machines-Power-and-Upgrades.md).

## Does Core handle money or factions?

Core defines the **currency** and **reputation** contracts and ships a non-persistent in-memory
fallback, but stores nothing permanently. NeroEconomy and NeroFactions provide the real storage. See
[Economy & Reputation](Economy-and-Reputation.md).

## How do I delete my data?

Run `/neroland data eraseme` to erase your own Neroland data across every system. Admins and retention
sweeps use the other `/neroland data` commands. See [Privacy & Data](Privacy-and-Data.md) and
[Commands](Commands.md).

## I'm a mod developer — how do I build against Core?

See [For Developers](For-Developers.md) and the deeper [`../docs/USING-CORE.md`](../docs/USING-CORE.md).

## See also

- [Home](Home.md)
- [Getting Started](Getting-Started.md)
- [Commands](Commands.md)
