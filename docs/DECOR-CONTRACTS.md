# Neroland Core — Decor Contracts (palette, dashboard, decor tags)

The shared, cosmetic-only contracts Core exposes for the decorative layer of the
ecosystem. Added in **Core 1.9.0** (an additive minor — see
[`API-STABILITY.md`](API-STABILITY.md)). **NeroDecor** is the first consumer, but the
contracts are generic: any mod or data pack may read the palette, drive a display
surface, or opt blocks into the decor tags.

> **No player data crosses any of these APIs.** Palettes are colours; display payloads
> are text/icon/colour; decor tags are block groupings. None is player-attributable, so
> none routes through Core's per-player erasure hook (see [`COMPLIANCE.md`](COMPLIANCE.md)).

## 1. Palette export — `za.co.neroland.nerolandcore.palette`

A read-only registry of named **finishes** so decor blocks, machine trims, GUIs and
`gen_textures` all match the exact tone Core uses on a material.

- **`Finish`** — a record of cosmetic metadata: `id`, `name`, `rgb` (`0xRRGGBB`),
  `emissive`, `emissiveLight` (0–15), and an optional `material` tag id. Helpers:
  `red()/green()/blue()`, and `Finish.accent(...)` / `Finish.material(...)`.
- **`PaletteRegistry`** — `getFinish(Identifier)`, `all()`, `contains(id)`, `size()`,
  and `register(Finish)` for mods contributing their own (namespaced) finishes.
  `API_VERSION` is bumped only if `Finish`'s shape changes.
- **`CoreFinishes`** — Core's built-ins: the four materials
  (`neroland:nero_alloy`, `:starsteel`, `:void_crystal` (emissive), `:plasma_glass`
  (emissive)) plus a curated 16-colour accent set `neroland:accent/<dye-name>` aligned to
  the vanilla dye names (so a paint/pigment cost model maps cleanly).

```java
Finish steel = PaletteRegistry.getFinish(
        Identifier.fromNamespaceAndPath("neroland", "starsteel")).orElseThrow();
int rgb = steel.rgb();            // 0x9FC4E0
boolean glows = steel.emissive(); // false
```

The `neroland:` finish ids Core ships are a frozen contract within the 1.x major.

## 2. Dashboard content contract — `za.co.neroland.nerolandcore.link.display`

The seam between mods that **provide** cosmetic surfaces (NeroDecor holograms / control
panels) and mods that **drive** them (NeroSecurity, NeroLogistics). Core holds only the
interface + registry + a no-op default — **all real logic lives in the providing mod**.

- **`DisplayAddress(ResourceKey<Level> dimension, BlockPos pos)`** — how a surface is
  addressed, without holding a reference to the provider's block entity.
- **`DisplayPayload(text, icon, statusColour)`** — the entire content: a `Component`
  text line, an optional icon `Identifier`, and a `0xRRGGBB` status colour
  (`-1` = unset). By construction it can carry no player data.
- **`DisplaySurface`** — implemented by the provider's block entity:
  `setText / setIcon / setStatusColour / clear`, plus a default `apply(DisplayPayload)`.
- **`DisplaySurfaces`** — the registry/dispatcher. Providers `register(address, surface)`
  on load and `unregister(address)` on unload (`remove(address)` when broken). Drivers
  `push(address, payload)` (or `setText/setIcon/setStatusColour/clear`). If no surface is
  present the push is **cached and returns `false`**, then re-applied when a matching
  surface next registers — so drivers need not track chunk-load timing. A Core-only or
  NeroDecor-only server is fully functional (pushes simply no-op).

```java
// driver side (e.g. NeroLogistics)
DisplaySurfaces.setText(
        DisplayAddress.of(level.dimension(), pos),
        Component.literal("Route 7: OK"));
```

## 3. Decor tags — `za.co.neroland.nerolandcore.decor.DecorTags`

The shared `neroland:decor/*` **block-tag** vocabulary. Core defines the tag ids (frozen
contract) and ships empty base tag files (`required:false`); **membership is
hand-authored datapack JSON** in the mods that own the blocks. External mods (Create,
AE2, Mekanism, Ad Astra, Energized Power) may opt their own blocks in via these tags
only — purely tag-mediated, dormant until they port to 26.1+.

| Tag | Meaning |
| --- | --- |
| `neroland:decor/hull` | structural hull / plating surfaces |
| `neroland:decor/panel` | industrial wall / floor panels |
| `neroland:decor/glass` | reinforced / framed glazing |
| `neroland:decor/neon` | emissive light strips / bars / panels |
| `neroland:decor/trim` | accent trim pieces |
| `neroland:decor/dashboard_surface` | holograms / panels that accept driver payloads |
| `neroland:decor/planet` | parent tag for all planet-themed sets |
| `neroland:decor/planet/<planet>` | per-planet set (built on demand via `DecorTags.planet(id)`) |

## 4. Seams NeroDecor reuses (no Core change needed)

- **Creative tab** — `CoreCreativeTab.addDecor(Supplier)` appends into the separate
  **Neroland Decor** tab (`itemGroup.nerolandcore.decor`, id `nerolandcore:neroland_decor`),
  added in 1.9.0 so a large decor kit doesn't swamp the main Neroland tab.
- **Progression (read-only)** — `ProgressionGates.isOpen(player, gate)` /
  `isServerOpen(server, gate)` and the `CoreGates` ids (`REACHED_ORBIT`, `FIRST_COLONY`,
  `DEEP_SPACE`, …); client mirror via `ClientGates`. NeroDecor **reads** these to
  auto-resolve planet-themed unlocks and must degrade to "available" when progression /
  the source mod is absent — it never writes gates.

### Gap analysis (why these were added)

Before 1.9.0 Core exposed materials/colours only implicitly (block `MapColor` + hex
constants in each mod) with **no queryable palette**, **no display-surface contract**,
and **no decor tag family**. The creative-tab seam existed (`CoreCreativeTab.add`) but
had a single tab, and the progression read seam already existed and was sufficient. 1.9.0
closes the first three gaps additively and adds the decor sub-tab; the progression seam
was confirmed as-is.
