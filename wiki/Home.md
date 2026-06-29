# Neroland Core Wiki

Player- and contributor-facing documentation for **Neroland Core**, part of the
[Neroland ecosystem](../../neroland-mc-ecosystem/README.md).

> **Status:** V1 foundation complete — version `1.3.0`. Core is a **library, not a content mod**:
> it ships materials, tags, and a stack of APIs that other Neroland mods (Nerospace, Nerotech,
> NeroPower, NeroEconomy, NeroFactions, NeroQuests, NeroEvents) build on. On its own it adds a few
> crafting materials, a creative tab, and a set of passive storage blocks (Battery, Fluid Tank, Gas
> Tank, Item Store, Trash Can); the gameplay — machines, generators, planets — lives in the mods that depend on it.

Built for **Minecraft 26.1.2 and 26.2** on **NeoForge, Forge, and Fabric**.

## Start here

- [Getting Started](Getting-Started.md) — what Core is, who it's for, and how to install it.
- [Commands](Commands.md) — every `/neroland …` command in one place.
- [FAQ](FAQ.md) — quick answers.

## Content & systems

- [Materials](Materials.md) — Nero Alloy, Starsteel, Void Crystal, Plasma Glass and their forms.
- [Recipes](Recipes.md) — the compaction recipes Core ships.
- [Machines, Power & Upgrades](Machines-Power-and-Upgrades.md) — the Nero energy + machine framework.
- [Side Configuration](Side-Config.md) — the per-face input/output/power/disabled control every machine shares.
- Storage blocks — passive endpoints Core ships: [Battery](Battery.md), [Fluid Tank](Fluid-Tank.md),
  [Gas Tank](Gas-Tank.md), [Item Store](Item-Store.md), [Trash Can](Trash-Can.md), and the [Creative Source Blocks](Creative-Source-Blocks.md).
- [Progression Gates](Progression-Gates.md) — the shared milestone system.
- [Meteor Material Registry](Meteor-Material-Registry.md) — the shared list of grindable random-output materials.
- [Economy & Reputation](Economy-and-Reputation.md) — currency and faction-standing contracts.
- [Configuration](Configuration.md) — the config file and reload command.
- [Tags & Datapacks](Tags-and-Datapacks.md) — `c:`/`neroland:` tags and pack retuning.
- [Privacy & Data](Privacy-and-Data.md) — POPIA/GDPR, erasure, and retention.

## For developers

- [For Developers](For-Developers.md) — how to depend on Core and use each system.
- Deeper API docs live in [`../docs/`](../docs/USING-CORE.md).

## See also

- [Build & contributor context](../AGENTS.md)
- [Ecosystem overview](../../neroland-mc-ecosystem/README.md)
- [This mod's planning docs](../../neroland-mc-ecosystem/neroland-core/)
