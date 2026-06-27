# Recipes

Neroland Core ships the standard compaction recipes for its
[materials](Materials.md). Every recipe is plain data under
`data/nerolandcore/recipe/*.json`, so a datapack can replace, remove, or add
recipes freely. No code path hard-codes a material item, and recipes reference
the `c:`/`neroland:` tags rather than item ids.

## Compaction set

| Material | Recipes (both directions) |
| --- | --- |
| Nero Alloy | block <-> ingot (9), ingot <-> nugget (9) |
| Starsteel | block <-> ingot (9), ingot <-> nugget (9) |
| Void Crystal | block <-> gem (9), gem <-> shard (9) |
| Plasma Glass | glass block -> pane |

Void Crystal compacts along the chain block <-> gem <-> shard.

## Overriding and extending

Because recipes are plain data and ingredients are tag-based, a pack can:

- replace any Core recipe by shipping the same recipe id
- remove a recipe
- add new recipes that consume the `c:` or `neroland:` material tags

Reference materials by tag (for example `c:ingots/nero_alloy`), never by item
id. See [Tags and Datapacks](Tags-and-Datapacks.md).

## Loot tables and mining

Material block loot tables live at
`data/nerolandcore/loot_table/blocks/*.json`. Each material block drops itself
by default; packs can override these for silk touch or bonus drops.

Mining tags list the solid blocks so tool requirements are pack-tunable:

- `minecraft:mineable/pickaxe`
- `needs_iron_tool`

## For developers

Tag and datapack details for mod authors live in
[../docs/TAGS-AND-DATAPACKS.md](../docs/TAGS-AND-DATAPACKS.md).

## See also

- [Materials](Materials.md)
- [Tags and Datapacks](Tags-and-Datapacks.md)
- [Getting Started](Getting-Started.md)
- [Home](Home.md)
