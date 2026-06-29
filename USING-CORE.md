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
  released (the `maven` job). CurseForge / Modrinth / GitHub Releases carry the player-facing
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

Each consumer's `multiloader.yml` and `publish.yml` grant `packages: read` and pass
`GITHUB_ACTOR` / `GITHUB_TOKEN` to Gradle. For the automatic `GITHUB_TOKEN` to read a package
that lives in **another** repo of the org, the package must be reachable from the consumer
repo. Pick one:

- **Recommended:** set each Core package's visibility to **Internal** (GitHub → the
  `neroland-core` packages → Package settings → Change visibility → Internal). All org repos
  can then read it with their own `GITHUB_TOKEN`, no extra secret.
- **Or:** create an organisation secret (e.g. `PACKAGES_READ_TOKEN`) holding a PAT with
  `read:packages`, and swap it in for `GITHUB_TOKEN` in the consumer build steps.

## Required one-time setup

1. **Publish-side token.** Core's `maven` job uses the workflow's own `GITHUB_TOKEN`
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

## Privacy

Publishing and resolving Core handles only build artifacts and version strings — no personal
data — so it stays POPIA/GDPR-clean.
