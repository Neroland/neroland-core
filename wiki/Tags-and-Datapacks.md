# Tags and Datapacks

Neroland Core exposes its [materials](Materials.md) through tags and keeps every
hook as plain data, so packs and other mods can retune the ecosystem without
touching code. **Reference materials by tag, never by item id.**

## Two tag families

Core ships two parallel tag families.

### `c:` — cross-mod convention

The `c:` namespace is the same convention namespace used by Create, AE2,
Mekanism, and the Fabric/NeoForge platforms. Use `c:` in recipes and machine
I/O — it is the interop surface and the most stable contract.

| Form | Per-material tag | Aggregate tag |
| --- | --- | --- |
| Ingot | `c:ingots/<m>` | `c:ingots` |
| Nugget | `c:nuggets/<m>` | `c:nuggets` |
| Dust | `c:dusts/<m>` | `c:dusts` |
| Plate | `c:plates/<m>` | `c:plates` |
| Gem | `c:gems/<m>` | `c:gems` |
| Storage block (item + block) | `c:storage_blocks/<m>` | `c:storage_blocks` |
| Glass block (item + block) | — | `c:glass_blocks` |
| Glass pane (item + block) | — | `c:glass_panes` |

Aggregates reference the per-material sub-tags, so adding a material extends the
aggregate automatically.

### `neroland:` — ecosystem namespace

The `neroland:` namespace is Core-owned and stable across minor versions. The
tag `neroland:materials/<material>` means "everything that is this material, in
any form." Use it for ecosystem-internal "is this one of our materials?"
checks.

### Rule of thumb

- recipe ingredients and external interop -> `c:`
- "is this our material?" checks -> `neroland:`

## Datapack hooks

All hooks are plain data and fully overridable; later packs win. Tags extend
additively.

| Hook | Location |
| --- | --- |
| Recipes | `data/nerolandcore/recipe/*.json` |
| Loot tables | `data/nerolandcore/loot_table/blocks/*.json` |
| Tags (`c:`) | `data/c/tags/**` |
| Tags (`neroland:`) | `data/nerolandcore/tags/**` |
| Mining (pickaxe) | `data/minecraft/tags/block/mineable/pickaxe` |
| Mining (tool level) | `needs_iron_tool` |

## For developers

The full tag map and datapack reference for mod authors lives in
[../docs/TAGS-AND-DATAPACKS.md](../docs/TAGS-AND-DATAPACKS.md).

## See also

- [Materials](Materials.md)
- [Recipes](Recipes.md)
- [Progression Gates](Progression-Gates.md)
- [Home](Home.md)
