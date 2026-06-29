# Neroland Core — Meteor Material Registry

An additive, Core-owned aggregation point where any Nero mod declares a *grindable*
material once — with a rarity tier and a progression gate — and any random-output
machine (the Nerospace meteor grinder is the first consumer) reads the aggregate. It
is the inversion point that keeps the grinder free of hard dependencies: mods publish
*into* Core, consumers read *from* Core, and no mod ever needs to know which other
mods are installed.

Shipped in **1.2.0** as a strictly additive API — it does not touch the frozen V1
surface (see [API-STABILITY.md](API-STABILITY.md)). Package root:
`za.co.neroland.nerolandcore.meteor`.

## The idea

A material describes itself once (an item, a tier, an optional gate and planet), Core
aggregates every declaration server-side, and a consumer resolves a weighted random
output per operation. Weights are *relative* and normalised by the live sum, so the
distribution auto-rebalances as materials are gated in, bound to a planet, or removed
with the mod that shipped them — there are no static loot tables to edit.

## Declaring a material

Two equivalent paths produce the same registry entry. **Data files win over
annotations** on conflict, preserving the datapack-override principle.

### Path A — data files (primary, datapack-overridable)

One JSON per material at `data/<namespace>/neroland/meteor_materials/<id>.json`:

```json
{
  "item": "nerotech:cobalt_dust",
  "tier": "uncommon",
  "min_gate": "nerolandcore:reached_orbit",
  "planet": null,
  "weight_override": null,
  "enabled": true
}
```

| Field | Meaning | Default |
| ----- | ------- | ------- |
| `item` | the item this entry outputs (`namespace:path`) | the material id (file path id) |
| `tier` | `common` \| `uncommon` \| `rare` \| `exotic` — sets the pool and base weight | **required** |
| `min_gate` | a Core gate id that must be open before the entry is eligible | none (always) |
| `planet` | a Nerospace planet id; entry only enters the pool in that dimension, with the planet-bias multiplier | none (any dimension) |
| `weight_override` | integer replacing the tier base weight; **mandatory for `exotic`** | none |
| `enabled` | per-entry kill switch | `true` |

The **material id** is the file path id (`<namespace>:<path>` under
`meteor_materials/`), not the item id — so several files may output the same item, and
a pack overrides an entry by shipping a file at the same path.

### Path B — `@GrindableMaterial` annotation (convenience, code)

```java
@GrindableMaterial(
    item = "nerotech:cobalt_dust",
    tier = MeteorTier.UNCOMMON,
    minGate = "nerolandcore:reached_orbit")
public static final RegistryObject<Item> COBALT_DUST = ...;
```

- **Full class path:** `za.co.neroland.nerolandcore.meteor.GrindableMaterial`
  (`@Target({TYPE, FIELD})`, `RUNTIME` retention).
- **Scan boundary:** Core reads the annotation **only from loaded mods whose annotated
  class is rooted at the package `za.co.neroland`** — a hard package-prefix filter, not
  a classpath-wide sweep and not a detection heuristic. A Nero mod that is not
  installed contributes nothing.
- An annotation entry's **material id is its item id**, so a data file at the matching
  path overrides it.

## Resolved design decisions

The umbrella spec flagged four open questions; this implementation resolves them as
follows.

1. **Annotation class path + scan boundary.** Class path
   `za.co.neroland.nerolandcore.meteor.GrindableMaterial`; boundary is the
   `za.co.neroland` package prefix on the *declaring class*, enforced per loader
   through `MeteorAnnotationScanner`. Discovery reads annotation metadata **without
   classloading mod code** (remap-proof): NeoForge/Forge use the `ModFileScanData`
   index; Fabric (which has no annotation index) reads the annotation reflectively off
   classes listed under the `nerolandcore:grindable` entrypoint. Because the scan
   never loads the annotated field's value, **`item` is required on the annotation**
   (the spec's field-only example becomes an explicit `item = "…"`).

2. **Data-file partial-override / merge rules.** For each material id, every datapack
   layer is read low→high and overlaid **field by field** onto the annotation base. A
   layer overrides only the keys it names; an **absent key inherits** the lower layer,
   while an **explicit JSON `null` overrides to "none"** (clears the field). `tier` is
   mandatory across the merged result (an entry with no tier is dropped with a
   warning); `item` defaults to the material id; `enabled` defaults to `true`. This is
   a true partial merge across the full pack stack, not last-file-wins.

3. **Exotic + `weight_override`.** Exotic entries have **no tier base weight**, so an
   exotic entry **must** carry a positive `weight_override` — a `null` (or
   non-positive) override makes it ineligible for the exotic pool (skipped, logged).
   Primary-tier entries may omit it and fall back to the tier base weight.

4. **`currentPlanet` lookup contract.** Resolved through `MeteorPlanets`, a settable
   provider (`MeteorPlanets.PlanetContext`) — the same inversion pattern as Core's
   currency/reputation seams, **not a hard dependency on Nerospace**. The default
   provider returns `null` (off-world / single-dimension), so planet-bound entries
   simply drop out; Nerospace installs a real provider at init.

## Membership tag

Every Core material is also in the item tag **`neroland:meteor/grindable`** so interop
layers (JEI/REI, recipe viewers, compat) can ask "is this grindable?" by tag without
touching the registry API. Consistent with this project's hand-authored-resources rule
(no datagen), membership is declared by hand-authored datapack JSON — Core ships its
own base materials at `data/neroland/tags/item/meteor/grindable.json` with
`required: false`, and every other mod ships its own entries the same way (the spec's
"entries may also be added directly by datapack"). For the *live* aggregated set
(including annotation entries) query the registry via `MeteorMaterials` instead;
`MeteorMaterials.isGrindable(server, item)` answers from registry truth.

## Tier → base weight (config)

The primary economy levers live in Core config (`nerolandcore.properties`,
server-authoritative, hot-reloadable via `/neroland config reload`):

| Key | Default | Meaning |
| --- | ------: | ------- |
| `meteorTierBaseWeightCommon` | 60 | common base weight (primary pool) |
| `meteorTierBaseWeightUncommon` | 25 | uncommon base weight (primary pool) |
| `meteorTierBaseWeightRare` | 12 | rare base weight (primary pool) |
| `meteorPlanetBias` | 2.0 | multiplier for a material on its bound planet |
| `meteorExoticChance` | 0.08 | chance the exotic bonus pool fires per grind |

The resolver reads these live on every roll, so a config reload retunes the pool
immediately. `exotic` entries use their `weight_override` for in-pool weight.

## Resolution algorithm

Per finished operation, given `ctx = { player, currentPlanet }`:

```text
candidates = registry.all()
  .filter(m -> m.tier != EXOTIC)
  .filter(m -> m.enabled)
  .filter(m -> m.minGate == null || gates.satisfied(player, m.minGate))
  .filter(m -> m.planet == null || m.planet == currentPlanet)

base(m)   = m.weightOverride ?? config.tierBaseWeight[m.tier]
weight(m) = base(m) * (m.planet == currentPlanet ? config.planetBias : 1.0)

primary   = weightedPick(candidates, weight)          // normalised by Σ weight

output = [ primary ]
if random() < config.exoticChance:                    // default 0.08
    exotics = registry.all().filter(EXOTIC, enabled, gate satisfied, weightOverride > 0)
    output += weightedPick(exotics, m -> m.weightOverride)
```

Gate satisfaction is queried through the existing `ProgressionGates` API (reusing the
`CoreGates` ids `reached_orbit`, `first_colony`, …); the registry stores nothing per
player.

### Worked example (locked by unit tests)

Registrations: `nero_alloy` (common, no gate), `plasma_glass` (uncommon, no gate),
`void_crystal` (uncommon, `reached_orbit`), `starsteel` (rare, `reached_orbit`), and a
planet-bound signature ore (rare, `first_colony`, on its planet).

- **Pre-orbit** (no gates): Nero Alloy 70.6%, Plasma Glass 29.4% (Σ 85).
- **`reached_orbit`**: 49.2 / 20.5 / 20.5 / 9.8% (Σ 122).
- **`first_colony`, on the bound planet**: 41.1 / 17.1 / 17.1 / 8.2 / 16.4% (Σ 146; the
  planet ore enters at 12 × 2.0 = 24). Off-world it drops out and the table collapses
  back to the `reached_orbit` distribution.

These exact percentages are asserted in
`common/src/test/java/.../meteor/MeteorResolutionTest.java`, which exercises the pure
`MeteorResolution` core (no Minecraft classpath needed).

## API surface

- `MeteorMaterials` — the facade: `all(server)`, `reload(server)`, `isGrindable(...)`,
  `resolve(player, random)` (returns the produced `Item`s), plus `tuning()` /
  `contextFor(player)` for custom consumers.
- `MeteorResolution` — the pure, Minecraft-free algorithm (filter / weight / normalise
  / pick / exotic), for testing and reuse.
- `MeteorMaterialEntry` — one aggregated declaration (item metadata only).
- `MeteorTier` — `COMMON` / `UNCOMMON` / `RARE` / `EXOTIC`.
- `MeteorMaterialTags.GRINDABLE` — the `neroland:meteor/grindable` tag key.
- `MeteorPlanets` — the Nerospace-facing planet-lookup seam.
- `GrindableMaterial` — the annotation; `MeteorAnnotationScanner` — the per-loader scan seam.
- Commands: `/neroland meteor list` (inspect the aggregate) and `/neroland meteor
  reload` (op; re-read data files).

## Multiplayer, performance, reload

Server-authoritative: aggregation, gate checks, weighting and rolls all run on the
server; the client receives only the resulting item(s). The aggregate is cached per
server and recomputed on world load or `/neroland meteor reload`; each grind is one
normalised pick — O(registered materials). Aggregation happens after all mods finish
registration, so the result is independent of mod load order (only datapack priority,
for overrides, is ordered).

## Compliance (POPIA / GDPR)

The registry holds **item metadata only** — no player data whatsoever. Gate
satisfaction is queried by gate name through the progression API; nothing is stored
per player, so there is nothing to export or erase and no `PlayerDataErasure` eraser is
registered. If a server later adds owner-tagged grind analytics, it must route through
Core's `PlayerDataErasure` hook, carry no personal identifiers, minimise fields, set a
retention limit, and stay opt-out. See [COMPLIANCE.md](COMPLIANCE.md).

## See also

- [Tags & Datapacks](TAGS-AND-DATAPACKS.md) · [Progression](PROGRESSION.md) ·
  [Config](CONFIG.md) · [Using Core](USING-CORE.md)
- Umbrella spec: `../../neroland-mc-ecosystem/neroland-core/METEOR-MATERIAL-REGISTRY.md`
