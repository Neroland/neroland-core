# Map Layers

*Added in Neroland Core 1.19.0. For mod developers; players never see this API directly.*

Neroland Core ships a small **map layer API** (`za.co.neroland.nerolandcore.map`) so that any Neroland
mod can put its things on a map (stations, colony borders, event areas) and a map mod such as
**NeroMaps** can draw them, without either mod depending on the other. Both depend only on Core.

## What Core does and does not do

- Core defines the types, the registry and the change-hint bus. That is all.
- Core **renders nothing, stores nothing and never calls a provider on its own**. A map mod calls
  providers through `MapLayerRegistry.query`; on a server without a map mod, registering a provider
  costs one map entry.
- Core holds **no map data** and logs nothing about map queries (see [Privacy & Data](Privacy-and-Data.md)).

## The pieces

| Type | What it is |
| ---- | ---------- |
| `MapLayerProvider` | The interface your mod implements: `modId()`, `layers()`, `query(viewer, dimension, bounds, sink)` and an optional `dimensionBadges(viewer)` |
| `MapLayerRegistry` | `register(provider)` once at init; consumers call `query(modId, viewer, dimension, bounds)` |
| `MapLayerInfo` | One layer: id, title lang key, icon id, default visibility on the world map and minimap, `MapScope`, cap |
| `MapScope` | `OWNER`, `MEMBERS`, `PROXIMITY`, `DISCOVERED` or `PUBLIC`: one per layer |
| `MapMarker` / `MapArea` | A point, or a rectangle, circle, chunk set or polygon, each with an id stable within its layer |
| `MapLabel` | A lang key with arguments, or a short literal; sanitised and length-limited |
| `MapViewer` | Who is asking: their id, and while online their dimension and position. `within(...)` is the one proximity test |
| `MapLayerSink` | The capped sink a provider writes into; it refuses other mods' layers, wrong dimensions, out-of-bounds items, duplicate ids and anything over the cap |
| `MapQueryResult` | What a query returned, with reject counts and elapsed time |
| `MapEvents` | "This layer changed here" hints so a map mod refreshes without polling |
| `MapLayerConformance` | A test fixture your mod runs to prove its provider follows the rule below |

## The rule every provider follows

A provider returns only what **that viewer** is entitled to see:

- Where the mod records an **owner or a member list**, that is the whole rule.
- **Proximity** is only for things with **no owner at all**, measured from the viewer's own position on
  the server (never from the area being looked at), in the same dimension, within 64 blocks for
  player-built things or 128 for world hazards, and nothing while the viewer is offline. A map mod never
  keeps proximity results after the viewer leaves range.
- World features (ruins, lairs) appear only once **that viewer** has discovered them.
- Never a server-wide list, never a "nearest player" lookup, and **no other player's name, id or
  online state** in any label.

## Limits

At most 16 layers per mod. Per query, each layer holds at most its cap (default 128, never above 512)
items, 8 192 chunks and 2 048 polygon vertices across its areas. An area spans at most 16 384 blocks
per axis. Labels are at most 48 characters; markers carry at most four detail lines.

## Icons

A layer or marker names an **icon id**; the map mod draws it from its own atlas and falls back to a
generic icon, so mods ship no map art. The ids available are listed in
[`USING-CORE.md`](../USING-CORE.md#map-layers).

## Getting started

The full walkthrough with code (declare layers, register, fire hints, run the conformance fixture) is
the **Map layers** section of [`USING-CORE.md`](../USING-CORE.md#map-layers). See also
[For Developers](For-Developers.md).
