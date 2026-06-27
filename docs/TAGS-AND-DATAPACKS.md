# Neroland Core — Tags & Datapack Hooks

How Neroland Core exposes its materials to other mods, and how packs retune Core
without code. Part of [V1](V1-PLAN.md) Phase 2.

## Namespace policy: `c:` vs `neroland:`

Core publishes two parallel tag families. **Reference materials by tag, never by item id.**

### `c:` — cross-mod convention tags

The community/common convention namespace (the same one Create, AE2, Mekanism and
Fabric/NeoForge content use). Core registers its material forms into the standard
`c:` paths so external automation interops out of the box:

| Form | Per-material tag | Aggregate |
| ---- | ---------------- | --------- |
| Ingot | `c:ingots/<material>` | `c:ingots` |
| Nugget | `c:nuggets/<material>` | `c:nuggets` |
| Dust | `c:dusts/<material>` | `c:dusts` |
| Plate | `c:plates/<material>` | `c:plates` |
| Gem | `c:gems/<material>` | `c:gems` |
| Storage block | `c:storage_blocks/<material>` (item + block) | `c:storage_blocks` |
| Glass block | — | `c:glass_blocks` (item + block) |
| Glass pane | — | `c:glass_panes` (item + block) |

Aggregates reference the per-material sub-tags (e.g. `c:ingots` contains
`#c:ingots/nero_alloy` and `#c:ingots/starsteel`), so adding a material extends the
aggregate automatically. **Use `c:` in recipes and machine I/O** — it is the
interop surface and the most stable contract.

### `neroland:` — canonical ecosystem tags

Core-owned tags for "everything that is this material, in any form":
`neroland:materials/<material>` (item tag). Use these for ecosystem-internal logic
that means "any Nero Alloy item" regardless of form. Core owns this namespace and
will keep it stable across minor versions; treat it as the Neroland-specific
counterpart to `c:`.

**Rule of thumb:** recipe ingredients and external interop → `c:`. "Is this one of
our materials?" checks inside the ecosystem → `neroland:`.

## Materials shipped in V1

| Material | Forms | Block forms |
| -------- | ----- | ----------- |
| Nero Alloy (industrial) | nugget, ingot, dust, plate | block |
| Starsteel (space-era) | nugget, ingot, dust, plate | block |
| Void Crystal (alien) | shard, gem (`void_crystal`), dust | block (faint glow) |
| Plasma Glass | shard (`plasma_glass`) | transparent block + pane |

Ores are intentionally **not** in Core — the mods that introduce a material's ore
(e.g. Nerospace planet ores) smelt into these Core items by tag.

## Datapack hooks — retuning without code

Everything below is plain data in `common/src/main/resources` and is fully
**datapack-overridable** by server packs (later packs win):

- **Recipes** — `data/nerolandcore/recipe/*.json`. Core ships the standard
  compaction set (block ↔ ingot ↔ nugget, block ↔ gem ↔ shard, plasma glass block
  → pane). A pack can replace or remove any of these, or add new ones that consume
  the `c:`/`neroland:` tags.
- **Loot tables** — `data/nerolandcore/loot_table/blocks/*.json`. Each material
  block drops itself; a pack can override (e.g. require silk touch, add bonus drops).
- **Tags** — `data/c/tags/**` and `data/nerolandcore/tags/**`. Packs (and other
  mods) extend these additively; a pack can fold a third-party material into
  `c:ingots` or a Nero material tag without touching code.
- **Mining** — `data/minecraft/tags/block/mineable/pickaxe` and `needs_iron_tool`
  list the solid material blocks, so tool requirements are pack-tunable too.

No code path hard-codes a material item; Core resolves everything through the tags
above, which is what makes the whole set retunable from a datapack.
