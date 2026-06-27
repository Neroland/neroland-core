# Neroland Core

> Part of the [Neroland](../neroland-mc-ecosystem) sci-fi Minecraft mod ecosystem, built on **Neroland Core**.

**Status:** V1 foundation complete — version `1.0.0`. The full Core API surface is in
place (materials, config, progression gates, currency/reputation contracts, and the
machine/power/upgrade framework) on the platform, registration and networking seams.
Core is a **library**, not a content mod. All six cells build; runtime client/server
testing is the recommended next step before tagging a release.

## Build targets

- **Minecraft:** 26.1.2 and 26.2
- **Loaders:** NeoForge, MinecraftForge/Forge, Fabric (the "6 cells")
- **Java:** 25
- Mod id: `nerolandcore` · package `za.co.neroland.nerolandcore`

## Layout

The build is the repo root, with a flattened cross-loader structure driven by Stonecutter:

- `common/` — shared, loader-agnostic source spliced into every loader node
- `fabric/` — Fabric Loom
- `forge/` — ForgeGradle
- `neoforge/` — ModDevGradle
- `stonecutter.gradle` — the real root build script; `build.gradle` is intentionally inert

## Building

```sh
./gradlew :fabric:26.2:build          # one cell
./gradlew :neoforge:26.1.2:build :neoforge:26.2:build \
          :forge:26.1.2:build :forge:26.2:build \
          :fabric:26.1.2:build :fabric:26.2:build   # all six
```

See [`AGENTS.md`](AGENTS.md) / [`CLAUDE.md`](CLAUDE.md) for agent and contributor context.

## Documentation

- [docs/V1-PLAN.md](docs/V1-PLAN.md) · [docs/V1-CHECKLIST.md](docs/V1-CHECKLIST.md) — the V1 plan and progress
- [docs/USING-CORE.md](docs/USING-CORE.md) — how to depend on Core and use each system
- [docs/API-STABILITY.md](docs/API-STABILITY.md) — public API surface + versioning policy
- [docs/TAGS-AND-DATAPACKS.md](docs/TAGS-AND-DATAPACKS.md) — materials, `c:`/`neroland:` tags, datapack hooks
- [docs/CONFIG.md](docs/CONFIG.md) — the config framework
- [docs/PROGRESSION.md](docs/PROGRESSION.md) — progression gates
- [docs/ECONOMY-REPUTATION.md](docs/ECONOMY-REPUTATION.md) — currency & reputation APIs
- [docs/MACHINES-POWER-UPGRADES.md](docs/MACHINES-POWER-UPGRADES.md) — machine / power / upgrade framework
- [docs/COMPLIANCE.md](docs/COMPLIANCE.md) — POPIA/GDPR: erasure, retention, logging

## Planning docs

Design, feature and dependency docs for this mod live in the umbrella repo under
[`../neroland-mc-ecosystem/neroland-core`](../neroland-mc-ecosystem/neroland-core).
