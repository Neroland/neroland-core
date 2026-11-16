# Consuming Neroland Core

Every Nero mod hard-depends on **Neroland Core** and resolves it as a normal Maven
artifact, one per loader × Minecraft version:

```
za.co.neroland.nerolandcore:nerolandcore-<loader>-<mc>:<version>
# e.g. za.co.neroland.nerolandcore:nerolandcore-fabric-26.1.2:1.2.0
```

The version a mod wants is set once in its `gradle.properties` (`nerolandcore_version=…`)
and used by all six of its loader cells.

## Where Core comes from

Core is published to two places:

- **Maven Local (`~/.m2`)** — for local development, via `./gradlew publishToMavenLocal`
  in the Core repo. Listed first in each consumer's `repositories {}`, so a locally built
  Core always wins on a dev machine.
- **GitHub Packages** — `https://maven.pkg.github.com/Neroland/neroland-core`. This is the
  remote that CI runners and fresh clones (which have an empty `~/.m2`) resolve from. Core's
  `publish.yml` pushes the six per-loader artifacts here whenever a new `mod_version` is
  released — a publish step in the `build` job, reusing the jars that job just built (no second
  build). CurseForge / Modrinth / GitHub Releases carry the player-facing
  jars only — they are **not** Gradle-consumable, which is why GitHub Packages exists.

> Before this was wired up, Core had no remote Maven at all. Consumer CI checked Core out and
> ran `publishToMavenLocal` against a hard-coded `ref:` — which silently drifted from
> `nerolandcore_version` and broke every dependent build with
> `Could not find …:nerolandcore-fabric-…`. That checkout step has been removed everywhere.

## Authentication

Gradle reads credentials for the GitHub Packages repo from, in order:

1. Gradle properties `gpr.user` / `gpr.key` (put these in `~/.gradle/gradle.properties`), else
2. environment variables `GITHUB_ACTOR` / `GITHUB_TOKEN`.

Credentials are evaluated lazily, so a local build that resolves Core from Maven Local never
needs a token. You only need one when actually fetching Core from GitHub Packages.

### Local developers

Easiest: build Core once and forget the token —

```
cd ../neroland-core
./gradlew publishToMavenLocal
```

Or, to pull Core from GitHub Packages without building it, add to `~/.gradle/gradle.properties`:

```
gpr.user=<your-github-username>
gpr.key=<a personal access token with read:packages>
```

### CI

Each consumer's `publish.yml` grants `packages: read` and passes
`GITHUB_ACTOR` / `GITHUB_TOKEN` to Gradle. For the automatic `GITHUB_TOKEN` to read a package
that lives in **another** repo of the org, the package must be reachable from the consumer
repo. Pick one:

- **Recommended:** set each Core package's visibility to **Internal** (GitHub → the
  `neroland-core` packages → Package settings → Change visibility → Internal). All org repos
  can then read it with their own `GITHUB_TOKEN`, no extra secret.
- **Or:** create an organisation secret (e.g. `PACKAGES_READ_TOKEN`) holding a PAT with
  `read:packages`, and swap it in for `GITHUB_TOKEN` in the consumer build steps.

## Required one-time setup

1. **Publish-side token.** Core's `build` job publishes with the workflow's own `GITHUB_TOKEN`
   (`packages: write`) — no secret to create.
2. **Read access** for consumers — choose Internal visibility or the org PAT secret above.
3. **Backfill the versions consumers already pin.** The `maven` job only runs for a *new*
   `mod_version` (one whose `v<version>` tag does not exist yet). Versions that were tagged
   before GitHub Packages existed are not there yet, so publish them once from their tags:

   ```
   cd neroland-core
   git checkout v1.2.0          # the version nerospace pins
   ./gradlew \
     :fabric:26.1.2:publish :fabric:26.2:publish \
     :neoforge:26.1.2:publish :neoforge:26.2:publish \
     :forge:26.1.2:publish :forge:26.2:publish \
     -Pgpr.user=<user> -Pgpr.key=<token-with-write:packages>
   ```

   Repeat for any other pinned version (currently **nerotech pins `1.0.1`**). Alternatively,
   bump the lagging consumer's `nerolandcore_version` to a version that is already published.

## Keep versions aligned

A consumer's `nerolandcore_version` must name a Core version that exists in GitHub Packages
(or Maven Local for local builds). If you bump Core, either publish that version and point the
consumer at it, or leave the consumer on the older pinned version (Core accepts the 1.0–2.0
range at runtime; the build-time pin just has to resolve).

### Minimum versions for the newer APIs

Core's API is additive within 1.x, so a consumer must pin at least the version that introduced
the API it calls (and floor its manifest `versionRange` at the same number — a `[1.0,2.0)`
floor loads happily against a Core that is too old and then fails at runtime):

| API | Since |
| --- | ----- |
| Palette export, dashboard surfaces, `neroland:decor/*` tags | `1.9.0` |
| `SpaceTags` (`neroland:space/*`) and `EntityRegistrationSupport` | `1.10.0` |
| Map layer API (`za.co.neroland.nerolandcore.map`) | `1.19.0` |

## Map layers

Since **1.19.0** any Nero mod can put its things on a map (NeroMaps, the companion app) by depending only
on Core. Pin `nerolandcore_version=1.19.0` or later. Core renders nothing, stores nothing and never calls
your provider itself; the map mod calls it through `MapLayerRegistry.query`.

**1. Declare layers and implement the provider** (common code, in a `map` package of your mod):

```java
public final class MyMapLayers implements MapLayerProvider {
    static final Identifier BASES = Identifier.fromNamespaceAndPath("mymod", "bases");
    static final Identifier PADS = Identifier.fromNamespaceAndPath("mymod", "pads_nearby");

    @Override public String modId() { return "mymod"; }

    @Override public List<MapLayerInfo> layers() {
        return List.of(
                MapLayerInfo.of(BASES, "map.layer.mymod.bases",
                        Identifier.fromNamespaceAndPath("mymod", "base"), MapScope.OWNER),
                MapLayerInfo.proximityBuilt(PADS, "map.layer.mymod.pads_nearby",
                        Identifier.fromNamespaceAndPath("mymod", "pad")));   // 64 blocks, minimap off
    }

    @Override public void query(MapViewer viewer, ResourceKey<Level> dim, MapBounds bounds, MapLayerSink sink) {
        for (Base b : store().basesOwnedBy(viewer.id(), dim)) {            // owner-scoped: the whole rule
            if (sink.isFull(BASES)) break;
            sink.marker(MapMarker.builder("base." + b.number(), BASES, dim, b.pos())
                    .label(MapLabel.literal(b.name())).build());          // the viewer's own base name
        }
        for (Pad p : store().unownedPads(dim)) {                         // no owner field at all
            if (viewer.within(dim, p.x(), p.z(), MapLayerInfo.BUILT_PROXIMITY)) {
                sink.marker(MapMarker.builder("pad." + p.number(), PADS, dim, p.pos()).build());
            }
        }
    }
}
```

**2. Register it once** during common init: `MapLayerRegistry.register(new MyMapLayers());`

**3. Fire a hint when something changes** (not for things that merely move):
`MapEvents.fire(BASES, dim, MapBounds.around(pos, 16));`, or `MapEvents.fireFor(playerId, ...)` when
only one player's view changed (for example they were removed from a member list).

**4. Prove the scoping** from your test suite with the fixture that ships in Core's main source set:

```java
MapLayerConformance.create(new MyMapLayers())
        .viewers(MapViewer.forTest(owner, null, overworld, new BlockPos(0, 64, 0)),
                 MapViewer.forTest(stranger, null, overworld, new BlockPos(4, 64, 0)))
        .dimensions(overworld, someDimensionYouKnowNothingAbout)
        .bounds(MapBounds.around(0, 0, 512))
        .farBounds(MapBounds.around(20_000, 20_000, 256))
        .forbiddenText("ThirdPlayerName").forbiddenIds(thirdPlayer)   // seed a third player's base
        .expectEntitledSees(MyMapLayers.BASES, 1)
        .expectEntitledSees(MyMapLayers.PADS, 1)
        .verify();
```

**The rule** (it is on `MapLayerProvider`'s javadoc and the fixture enforces it): return only what *that
viewer* may see. Owner UUID or member list where you record one, and that is the whole rule. Proximity
(`viewer.within`, from the viewer's own server-side position, never from `bounds`) only for things with
no owner at all. World features only once that viewer has discovered them. One `MapScope` per layer:
split a layer that would mix owned and unowned things. Never a server-wide roster, never
`getNearestPlayer`, and no other player's name, UUID or online state in any id, label, detail or
colour. Read in-memory state only: never load a chunk or touch disk in `query`.

**Limits.** At most 16 layers per provider; per query each layer holds at most `maxPerQuery` items
(default 128, hard cap 512) and 8 192 chunks / 2 048 polygon vertices across its areas; areas span at
most 16 384 blocks per axis. Programmer errors (bad ids, keys, vertex or chunk counts) throw
`IllegalArgumentException`; data-derived values (label length, extra details, circle radius) clamp.

**Icon ids.** Name an icon id; NeroMaps draws it from its shared atlas and falls back to
`neromaps:generic`, so your mod ships no map art. Ids the atlas carries for 0.1.0: `neromaps:generic`,
`neromaps:area`, `neromaps:star_chart`; `nerospace:station`, `nerospace:launch_pad`, `nerospace:rocket`,
`nerospace:terraform_region`; `nerotech:pollution`; `neropower:reactor`, `neropower:beam_link`,
`neropower:scorch_zone`; `nerologistics:drone_port`, `nerologistics:train_station`;
`nerocolonies:colony_border`, `nerocolonies:colony_building`, `nerocolonies:colony_storage`,
`nerocolonies:outpost`; `nerofactions:outpost` (with the badges `neromaps:badge_hostile`,
`neromaps:badge_neutral`, `neromaps:badge_friendly`); `neroeconomy:my_shop`;
`nerosecurity:installation`, `nerosecurity:field`; `neroevents:event`, `neroevents:storm`,
`neroevents:war_zone`; `neroruins:ruin`, `neroruins:vault_claimed`, `neroruins:expedition_target`;
`nerocreatures:my_creature`; `nerocompanion:companion`, `nerocompanion:companion_lost`,
`nerocompanion:home_bay`.

## What to call once it resolves

This page is about **getting** Core on the classpath. For the APIs themselves start at
[`docs/USING-CORE.md`](docs/USING-CORE.md), which indexes every system and its deep doc —
including [`docs/SPACE-TAGS-AND-ENTITIES.md`](docs/SPACE-TAGS-AND-ENTITIES.md) for the
`neroland:space/*` biome vocabulary and the cross-loader mob-registration seam.

## Privacy

Publishing and resolving Core handles only build artifacts and version strings — no personal
data — so it stays POPIA/GDPR-clean.
