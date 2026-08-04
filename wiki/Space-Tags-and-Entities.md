# Space Tags & Entity Registration

Two APIs added in **Core 1.10.0** for the mods that put creatures and content on other
worlds. Neither adds anything to the game on its own, and neither stores player data.

## Space tags

Core owns a small shared vocabulary that answers "what kind of place is this?" —
`neroland:space/dark_biomes`, `moon_biomes`, `crystalline_biomes`, `asteroid_biomes`, the
umbrella `planet_biomes`, and the dimension-type tag `neroland:space/dimensions`.

Why it matters: a mod that adds space creatures, ruins or alien crops can say "spawn on moon
biomes" without knowing which mod supplies the moons. Core ships the membership as **optional**
entries, so the tags resolve to whatever planets you actually have installed.

- With **Nerospace** installed: Cindara is a dark world, Glacira is a low-gravity crystal
  moon, Greenxertz is a crystal field, and every Nerospace biome (including the terraformed
  ones) counts as a planet biome.
- With **Ad Astra** installed (once it ports to 26.1+): its Moon, Mars, Venus, Mercury, Glacio
  and Orbit slot into the same tags.
- With **neither**: every tag is empty, and that is fine — content gated on these tags simply
  does not appear. **Earth stays quiet by design.**

Pack makers can retune every tag with a normal data pack — add a biome from any mod to
`data/neroland/tags/worldgen/biome/space/moon_biomes.json` and anything that spawns on moons
now spawns there.

## Entity registration

A developer-facing seam. Minecraft's three mod loaders each register a mob's default
attributes and natural spawn placement differently; Core hides that behind one call so a mod
can declare both once, in shared code, and have it work on NeoForge, Forge and Fabric.

Nothing here is player-visible — it exists so creature mods built on Core behave identically
on all three loaders.

See the developer docs:
[`../docs/SPACE-TAGS-AND-ENTITIES.md`](../docs/SPACE-TAGS-AND-ENTITIES.md).

## See also

- [Tags & Datapacks](Tags-and-Datapacks.md)
- [For Developers](For-Developers.md)
- [Home](Home.md)
