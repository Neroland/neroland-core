# Getting Started

**Neroland Core** is the foundation library for the Neroland sci-fi mod ecosystem. It is built for
**Minecraft 26.1.2 and 26.2** on **NeoForge, Forge, and Fabric**, with the mod id `nerolandcore`.

## What Core is (and isn't)

Core is a **library, not a content mod**. It provides the shared materials, tags, and APIs that the
other Neroland mods rely on, so the whole ecosystem behaves consistently. By itself Core adds:

- a handful of crafting materials and their blocks (see [Materials](Materials.md)),
- a shared **Neroland** creative tab,
- a `/neroland …` command tree (see [Commands](Commands.md)).

The real gameplay — machines, planets, quests, economy — lives in the mods that build on Core
(Nerospace, Nerotech, NeroPower, NeroEconomy, NeroFactions, NeroQuests, NeroEvents).

## Who it's for

- **Players** — you install Core because a mod you want requires it. It runs quietly underneath.
- **Pack makers** — Core is highly tunable from a datapack and config without touching code; see
  [Tags & Datapacks](Tags-and-Datapacks.md) and [Configuration](Configuration.md).
- **Mod developers** — Core is the single, stable dependency you build against; see
  [For Developers](For-Developers.md).

## Installing

1. Install your loader of choice (NeoForge, Forge, or Fabric) for Minecraft 26.1.2 or 26.2.
2. On **Fabric**, also install Fabric API.
3. Drop `nerolandcore` and any mods that depend on it into your `mods/` folder.

Core must load **before** the mods that depend on it; loaders handle this automatically from the
dependency declarations.

## First things to try

- Open the creative inventory and find the **Neroland** tab to see the materials Core ships.
- Run `/neroland gate list` to view the progression milestones (see [Progression Gates](Progression-Gates.md)).
- Run `/neroland config list` to see Core's tunable values (see [Configuration](Configuration.md)).

## See also

- [Commands](Commands.md)
- [Materials](Materials.md)
- [For Developers](For-Developers.md)
- [FAQ](FAQ.md)
