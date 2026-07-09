# Item Highlights

Core draws a **subtle coloured border** inside inventory slots that hold Nero
ecosystem items, so they stand out at a glance in any container screen —
similar to the classic *Item Borders* mod, but ecosystem-aware. The border sits
beneath the item and is drawn as a soft glow — three pixels by default, fading
inwards and towards the top of the slot.

## Colours by category

The colour tells you **what an item is**, not which mod it comes from. The
category is read from `neroland:highlight/*` item tags; the most specific tag
wins:

| Category | Tag | Colour |
| --- | --- | --- |
| Machines & functional blocks | `neroland:highlight/machines` | Amber |
| Tools, weapons & gear | `neroland:highlight/tools` | Violet |
| Upgrade modules & augments | `neroland:highlight/upgrades` | Green |
| Crafting materials | `neroland:highlight/materials` | Teal |

Core tags its own content out of the box: the four materials and all their
forms (teal) and the passive storage blocks — Battery, Fluid Tank, Gas Tank,
Item Store, Trash Can and their creative variants (amber). Downstream Nero mods
add their items to the same tags, so highlights work uniformly across the whole
ecosystem.

## Configuration

Both settings are client-side and local-only, in `nerolandcore.properties`
(hot-reloadable via `/neroland config reload`):

| Key | Default | Purpose |
| --- | --- | --- |
| `itemHighlightsEnabled` | `true` | Turn the borders on or off |
| `itemHighlightOpacity` | `65` | Border opacity in percent (0-100) |
| `itemHighlightThickness` | `3` | Border thickness in pixels (1-4, fades inwards) |

## For pack makers

Membership is plain datapack tag JSON — no code needed. Add or remove entries
under `data/neroland/tags/item/highlight/` in a datapack to retune which items
glow and in which category, exactly like any other tag (see
[Tags & Datapacks](Tags-and-Datapacks.md)). Entries should use
`"required": false` so the tag loads even when a referenced mod is absent.

## For developers

Add your mod's items to the same tags from your own jar, e.g.
`data/neroland/tags/item/highlight/machines.json` with `"replace": false`.
Rendering is entirely Core's job — depending on Core is all it takes.

## See also

- [Configuration](Configuration.md)
- [Tags & Datapacks](Tags-and-Datapacks.md)
- [Materials](Materials.md)
