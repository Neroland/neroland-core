# Materials

Neroland Core ships four backbone materials for the Neroland ecosystem. Core
provides the items, blocks, tags, and compaction recipes — but **no ores**.
Mods that add a material's ore (for example Nerospace planet ores) smelt into
these Core items by tag, so every mod shares one set of materials.

The textures shipped in 1.0.0 are procedural placeholder 16x16 textures meant
to be re-skinned later.

## The four materials

| Material | Theme | Forms | Storage block |
| --- | --- | --- | --- |
| Nero Alloy | Industrial | nugget, ingot, dust, plate | Block of Nero Alloy |
| Starsteel | Space-era | nugget, ingot, dust, plate | Block of Starsteel |
| Void Crystal | Alien | shard, gem ("Void Crystal"), dust | Block of Void Crystal (faint glow) |
| Plasma Glass | Transparent | shard | Plasma Glass block + pane |

## Forms and item ids

### Nero Alloy and Starsteel

Both are metals and share the same four forms plus a storage block.

- nugget
- ingot
- dust
- plate
- storage block ("Block of Nero Alloy" / "Block of Starsteel")

### Void Crystal

A crystal, so it has **no plate**.

- shard (`void_crystal_shard`)
- gem item displayed "Void Crystal" (item id `void_crystal`)
- dust
- storage block ("Block of Void Crystal", faint glow)

### Plasma Glass

- shard item displayed "Plasma Glass Shard" (item id `plasma_glass`)
- transparent block "Plasma Glass" (`plasma_glass_block`)
- pane "Plasma Glass Pane" (`plasma_glass_pane`)

## Always reference by tag

Never depend on a material item id. Reference materials through tags so packs
and other mods can substitute, retune, or extend them:

- cross-mod recipes and machine I/O use the `c:` family (for example
  `c:ingots/nero_alloy`)
- "is this one of our materials?" checks use `neroland:materials/<material>`

See [Tags and Datapacks](Tags-and-Datapacks.md) for the full tag map.

## For developers

Deeper material and tag details for mod authors live in
[../docs/TAGS-AND-DATAPACKS.md](../docs/TAGS-AND-DATAPACKS.md).

## See also

- [Recipes](Recipes.md)
- [Tags and Datapacks](Tags-and-Datapacks.md)
- [Getting Started](Getting-Started.md)
- [Home](Home.md)
