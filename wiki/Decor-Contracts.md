# Decor Contracts

Shared, **cosmetic-only** contracts Core exposes for the ecosystem's decorative layer
(added in Core 1.9.0). NeroDecor is the first consumer, but any mod or data pack can use
them. None of these carry player data, so none is subject to erasure.

## Palette export

A read-only registry of named **finishes** (colour + emissive metadata) so decor blocks,
machine trims and textures match Core's exact material tones. Core ships the four
materials (`neroland:nero_alloy`, `neroland:starsteel`, `neroland:void_crystal`,
`neroland:plasma_glass`) plus a 16-colour accent set (`neroland:accent/<dye-name>`). Read
them via `PaletteRegistry.getFinish(id)`.

## Dashboard surfaces

Holograms and control panels expose a tiny content API — text, icon, status colour,
addressed by dimension + block position — that mods like NeroSecurity and NeroLogistics
can push readouts onto. Core defines only the interface and a no-op registry; the
provider mod does the rendering. With no driver present, a surface simply stays blank.

## Decor tags

The `neroland:decor/*` block tags (`hull`, `panel`, `glass`, `neon`, `trim`,
`dashboard_surface`, `planet` + per-planet) group decor surfaces so other mods (and
external mods, via tags only) can sit alongside NeroDecor builds. Membership is declared
by datapack JSON in the owning mod.

See the developer docs: [`../docs/DECOR-CONTRACTS.md`](../docs/DECOR-CONTRACTS.md).
