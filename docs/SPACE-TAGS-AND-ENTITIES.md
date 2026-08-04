# Neroland Core — Space Tags & the Entity Registration Seam

Two additive APIs shipped in **Core 1.10.0** (see [`API-STABILITY.md`](API-STABILITY.md))
for mods that add creatures, structures or flora to off-Earth worlds:

1. **`SpaceTags`** — the shared `neroland:space/*` biome and dimension-type vocabulary, so a
   mod can ask "is this place a moon / a crystal field / off-Earth at all?" without depending
   on the mod that ships the planets.
2. **`EntityRegistrationSupport`** — the cross-loader seam for the two pieces of mob setup
   that have no vanilla cross-loader home: **default attributes** and **natural spawn
   placements**.

> **No player data crosses either API.** Space tags are world-shape groupings; the entity seam
> carries entity types, attribute values and spawn predicates. Neither is player-attributable,
> so neither routes through Core's per-player erasure hook (see [`COMPLIANCE.md`](COMPLIANCE.md)).

## 1. Space tags — `za.co.neroland.nerolandcore.worldgen.SpaceTags`

Core owns the tag **ids** (a frozen contract inside 1.x) and ships base membership JSON.
**Every shipped entry is `"required": false`**, so a tag simply resolves to whichever planets
are actually installed. The mods that own the planets supply the members; data packs retune
membership with no code.

| Constant | Tag | Meaning |
| -------- | --- | ------- |
| `DARK_BIOMES` | `neroland:space/dark_biomes` | perpetually dim, sunless or ash-choked surfaces |
| `MOON_BIOMES` | `neroland:space/moon_biomes` | low-/micro-gravity satellite-like regolith |
| `CRYSTALLINE_BIOMES` | `neroland:space/crystalline_biomes` | crystal, quartz and ice-lattice terrain |
| `ASTEROID_BIOMES` | `neroland:space/asteroid_biomes` | airless rubble fields and orbital debris |
| `PLANET_BIOMES` | `neroland:space/planet_biomes` | umbrella — **every** off-Earth biome |
| `SPACE_DIMENSIONS` | `neroland:space/dimensions` | off-Earth dimension **types** |

Biome tags live at `data/neroland/tags/worldgen/biome/space/*.json`; the dimension-type tag at
`data/neroland/tags/dimension_type/space/dimensions.json`.

`PLANET_BIOMES` includes the four themed tags **by reference**, so anything added to a themed
tag is automatically a planet biome — a downstream mod only needs to tag a new biome once.

### Helpers

```java
// Coarse dimension guard.
if (!SpaceTags.isSpace(serverLevel)) return;

// Per-place decision (null-safe, empty-tag-safe).
boolean darkPlace = SpaceTags.biomeIn(level.getBiome(pos), SpaceTags.DARK_BIOMES);
boolean sameThing = SpaceTags.biomeIn(level, pos, SpaceTags.DARK_BIOMES);   // convenience overload
```

- `isSpace(@Nullable ServerLevel)` — `true` when the level's dimension **type** is in
  `SPACE_DIMENSIONS`. `false` for `null` and for every vanilla dimension.
- `biomeIn(@Nullable Holder<Biome>, TagKey<Biome>)` — the shape spawn predicates want.
- `biomeIn(@Nullable LevelReader, BlockPos, TagKey<Biome>)` — the same test for the biome at a
  position.

### Empty tags are normal — never crash on one

On a Core-only server **every one of these tags is empty**. That is a supported state, not an
error. Consumers must read an empty tag as *"no such place exists here"* — no spawns, no
placement, no bonus — and must never assume a member exists (no
`getRandomElement(...).orElseThrow()`). Vanilla Earth stays untouched by design: a mod whose
content is gated on `PLANET_BIOMES` adds nothing to a world with no planet mod installed.

### Shipped membership

**Nerospace** (optional entries; theming read off each biome's own JSON plus its
`nerospace:gravity_*` tag membership):

| Biome | Tags | Why |
| ----- | ---- | --- |
| `nerospace:cindara` | dark, planet | volcanic ash world: temperature 2.0, no precipitation, brown/ember tint, on the `nerospace:space` dimension type (`ambient_light: 0.0`, fixed time, monster spawn block-light limit 0) |
| `nerospace:glacira` | moon, crystalline, planet | the only Nerospace biome in `nerospace:gravity_low`; glacite **gem** ore and an ice-lattice palette |
| `nerospace:greenxertz` | crystalline, planet | xertz-quartz ore fields; lush and lit, so not dark and not a moon |
| `nerospace:terraformed` | planet | terraformed states of a planet — off-Earth, but no longer hostile terrain |
| `nerospace:terraformed_meadow` | planet | as above |
| `nerospace:terraformed_savanna` | planet | as above |
| `nerospace:terraformed_tundra` | planet | as above |
| `nerospace:space` (dimension **type**) | dimensions | the shared type behind Cindara, Glacira and orbital stations |

Nerospace ships no asteroid field, so `ASTEROID_BIOMES` is empty on a Nerospace-only server —
a deliberate demonstration of the empty-tag contract.

Two consequences worth knowing:

- **Greenxertz is not in `SPACE_DIMENSIONS`.** Its dimension reuses the `minecraft:overworld`
  dimension **type**, which cannot be tagged without dragging the actual Overworld in with it.
  `SPACE_DIMENSIONS` is a coarse guard; the biome tags are the precise lever.
- **A biome may carry several tags.** Glacira is both a moon and a crystal field; that is
  intended, and consumers should treat the tags as independent questions.

**Ad Astra** entries ship too, on the same tag-mediated, no-hard-dependency footing as the rest
of Core's external interop — best-effort ids, `"required": false`, dormant until Ad Astra ports
to 26.1+. Moon and Mercury → moon + planet; Glacio → crystalline + planet; Orbit → dark,
asteroid and planet; Mars and Venus → planet; the corresponding dimension types →
`SPACE_DIMENSIONS`. If an id turns out to differ after the port, the fix is one JSON entry —
no code change and no crash in the meantime.

## 2. Entity registration seam — `za.co.neroland.nerolandcore.entity.EntityRegistrationSupport`

`RegistrationProvider` covers every vanilla registry, but two parts of mob setup are *not*
registry entries and diverge per loader:

| | NeoForge | Forge | Fabric |
| --- | --- | --- | --- |
| Default attributes | `EntityAttributeCreationEvent` (mod bus) | `EntityAttributeCreationEvent` | `FabricDefaultAttributeRegistry.register` (immediate) |
| Spawn placement | `RegisterSpawnPlacementsEvent` (mod bus) | `SpawnPlacementRegisterEvent` | `SpawnPlacements.register` (immediate) |

Core buffers both from common code and flushes them at each loader's correct moment.

```java
EntityRegistrationSupport entities = EntityRegistrationSupport.get("nerocreatures");

entities.registerAttributes(ModEntities.VOID_CRAWLER, VoidCrawler::createAttributes);

entities.registerSpawnPlacement(ModEntities.VOID_CRAWLER,
        SpawnPlacementTypes.ON_GROUND,
        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
        (type, level, reason, pos, random) ->
                level.getBlockState(pos.below()).isSolid()
                        && SpaceTags.biomeIn(level.getBiome(pos), SpaceTags.DARK_BIOMES));
```

### API

```java
static EntityRegistrationSupport get(String modId);

void registerAttributes(Supplier<? extends EntityType<? extends LivingEntity>> type,
                        Supplier<AttributeSupplier.Builder> attributes);

<T extends Mob> void registerSpawnPlacement(Supplier<? extends EntityType<T>> type,
                                            SpawnPlacementType placementType,
                                            Heightmap.Types heightmap,
                                            SpawnPlacements.SpawnPredicate<T> predicate);

static void attach(Object loaderEventBus);   // optional; see below
```

- **Suppliers, not values.** On the deferred-register loaders the `EntityType` does not exist
  yet while a mod is constructing, so both the type and the attribute builder are resolved only
  at flush time. A `RegistrationProvider.RegistryEntry` is already a `Supplier` — pass it
  straight in.
- **Attributes are mandatory** for every `LivingEntity` type: a type without them crashes the
  first time it spawns.
- **Placement is the placement half only** — *where* the game may consider a spawn. Biome and
  weight selection stay with your own spawn definitions or datapack biome modifiers, since
  `MobSpawnSettings` injection is itself loader-divergent.
- **Errors are contained.** A registration whose supplier throws is logged (mod id only, no
  player data) and skipped; one bad entry never aborts the batch for other mods.

### Zero loader wiring

Core installs the flush listeners during its own bootstrap, and on both event-driven loaders
the events reach every mod (NeoForge dispatches mod-bus events to all mod buses; Forge's two
events share one static bus). Every mod constructor has already run by the time they fire, so a
downstream mod that only calls the two register methods from **common** code is fully wired.

`EntityRegistrationSupport.attach(modEventBus /* or BusGroup */)` exists for parity with
`RegistrationProvider.attach(...)` and is safe to call from a loader entry point — it is
idempotent, and each buffered entry is applied at most once no matter how many buses flush.

### Ordering

Fabric applies registrations **eagerly**, so declare them after your entity types are
registered — i.e. late in your common `init()`, the same rule that already governs every other
Fabric-eager registration. On NeoForge/Forge order does not matter.

### Extending it

The seam is a ServiceLoader interface (`EntityRegistrationSupport.Plumbing`, one implementation
per loader in Core, registered through `META-INF/services/…EntityRegistrationSupport$Plumbing`).
It is Core-internal: downstream mods use the facade and ship no service file of their own.
