# Neroland Core — V1 (1.0.0) Plan

> Planning doc for the first stable release of **Neroland Core**, the foundation library every
> Neroland mod builds on. Design source of truth lives in the umbrella repo under
> [`../neroland-mc-ecosystem/neroland-core`](https://github.com/dario-maselli/neroland-mc-ecosystem)
> (`README` / `DESIGN` / `FEATURES` / `DEPENDENCIES` / `IDEAS`). This doc turns that design into a
> shippable V1; [`V1-CHECKLIST.md`](V1-CHECKLIST.md) is the line-by-line task list.

## Goal of V1

Ship the **full Core API surface** so that Nerospace (the flagship, already in development) and every
other Neroland mod can be built against a single, stable foundation. Core is a **library, not a content
mod**: it ships APIs, registries, tags, and a small set of base implementations — not gameplay.

The bar for 1.0.0: a downstream mod can declare a hard dependency on Core and use **materials, config,
currency/reputation, the machine/power/upgrade framework, progression gates, and compat tags** without
Core needing a breaking change to support it.

## Where V1 starts from

The `nerolandcore` repo is currently a **barebones multiloader skeleton**: a single
`NerolandCoreCommon.init()` stub, the three loader entry points (`NerolandCoreFabric` +
`...FabricClient`, `NerolandCoreForge`, `NerolandCoreNeoForge`), and the Stonecutter "6 cells" build
(`fabric / forge / neoforge` × MC `26.1.2 / 26.2`). No registries, no resources, no platform-seam
services yet. V1 is the first content-bearing release.

## Scope — the six Core systems

V1 delivers all six systems described in the design docs. Each is a public contract that downstream
mods code against.

### 1. Material & registry layer

The single source of truth for Neroland materials. Register the four backbone materials — **Nero Alloy,
Starsteel, Void Crystal, Plasma Glass** — and their derived forms (ingot, nugget, dust, plate, block;
Plasma Glass also a transparent block + pane). Every form is tagged into common namespaces
(`c:ingots/nero_alloy`, `neroland:materials/starsteel`, …) so downstream mods require items **by tag**,
never by concrete class. Nerospace planet ores smelt into these; Nerotech recipes consume them.

### 2. Config framework

One config service: typed schema registration, defaults, validation, hot-reload via
`/neroland config reload`, and **server-authoritative** values that sync to clients. Every Neroland mod
registers its schema here instead of hand-rolling config I/O. Pairs with datapack hooks for pack-level
tuning. Material stat baselines, the upgrade-module modifier curve, and energy conversion ratios are all
config-exposed.

### 3. Currency & reputation APIs

Capability-style contracts for player balances and faction/player reputation, plus change events. Core
**defines and exposes** them but stores nothing — NeroEconomy implements the currency store, NeroFactions
implements reputation, and any mod (shops, quest rewards, faction perks) reads/writes through the API.
The API supports **named currencies** from day one (global + per-faction/per-planet scrip) even though
NeroEconomy may ship with one.

### 4. Machine / power / upgrade framework

A base machine block-entity, a set of **power types** (a Neroland energy unit plus bridges to common
Forge/Fabric energy), and an **upgrade-module system**: machines expose typed upgrade slots, modules
declare effects (speed, efficiency, range, capacity), and Core resolves stacked modifiers with
configurable caps. Nerotech and NeroPower extend this rather than reinventing it, so upgrade modules are
interchangeable across mods.

### 5. Progression-gate API

Named progression flags per player / team / server with a gate API (`is gate reached_orbit open?`).
Server-authoritative and synced; clients never mutate. NeroQuests drives the gates; everything else just
reads them. Gate definitions (`industrial_power`, `reached_orbit`, `first_colony`, …) are
**datapack-overridable** so servers can reorder or skip the arc.

### 6. Common tags & datapack hooks

The canonical cross-mod tag set (materials, plates, gears, energy-bearing items, machine-I/O categories)
mapped onto the conventions Create, AE2, Mekanism, and Ad Astra-style mods use. This is how the ecosystem
gets external interop **without any Neroland mod hard-depending** on a third-party mod. Plus datapack
hooks so packs retune progression and recipes without code, and a documented `c:` vs `neroland:`
namespace policy.

## Cross-cutting requirements

### Platform seams (no Architectury)

Loader-agnostic code lives in `common/`; each loader ships one impl plus a `META-INF/services` entry via
**ServiceLoader**. `common/` stays free of `net.neoforged.*` / `net.fabricmc.*` / `net.minecraftforge.*`
imports. Every system above that touches registries, networking, energy, or events needs a platform-seam
interface in `common/` and three impls.

### Data, privacy & compliance (POPIA / GDPR)

Currency, reputation, and progression records are keyed by player UUID — personal data. Core must:

- store only UUID + gameplay state (never IP, chat, or location history beyond gameplay needs);
- expose retention/cleanup hooks so owners can purge data for players inactive for a configurable period;
- provide a **shared per-player data-erasure hook** every Core-storing mod implements, so one request
  purges a player across the whole ecosystem;
- ship an opt-out / data-reset command and document it;
- avoid writing player data to logs at `info` level — only public version strings, minimised and
  time-limited.

### API stability

Once downstream mods ship against Core, breaking changes cascade across the lineup. Version the public
API and treat it as **frozen between majors**. Separate published `api` packages (interfaces, tags,
records) from `impl` (internals free to change). Document the deprecation policy in V1.

## Phasing within V1

Each phase should compile and pass `ecjCheck` on all six cells before the next begins.

| Phase | Deliverable | Notes |
| ----- | ----------- | ----- |
| 1 | Registry + platform-seam plumbing | ServiceLoader seam, `DeferredRegister`-style helper, creative-tab helper. Nothing user-facing yet. |
| 2 | Materials + tags + datapack hooks | The four materials and all derived forms; common + `neroland:` tags; hand-authored JSON in `common/src/main/resources`. |
| 3 | Config framework | Schema registration, sync, hot-reload command, datapack tuning hooks. |
| 4 | Progression-gate API | Flags, gate queries, server sync, datapack-overridable definitions, team scoping. |
| 5 | Currency & reputation APIs | Contracts + events + named-currency support. Contracts only — Core stores nothing. |
| 6 | Machine / power / upgrade framework | Base block-entity, power types + bridges, upgrade modules + modifier math. Largest phase. |
| 7 | Compliance + API freeze + docs | Erasure hook, retention config, opt-out command; freeze `api` package; write developer docs; tag `1.0.0`. |

Ordering rationale: registries and tags unblock everything; config and gates are what Nerospace needs
soonest; currency/reputation are pure contracts (low risk); the machine framework is the heaviest and
benefits from the config + upgrade-curve surface already existing.

## Build & verify

- Targets: **MC 26.1.2 and 26.2** on **NeoForge, Forge, Fabric** — the six cells. Java 25, official
  Mojang mappings (26.x ships de-obfuscated; no Parchment).
- Resources are **hand-authored** in `common/src/main/resources` — the multiloader does not run datagen;
  validate JSON after every edit.
- Build a cell: `./gradlew :fabric:26.2:build`. All six:
  `:neoforge:26.1.2:build :neoforge:26.2:build :forge:26.1.2:build :forge:26.2:build :fabric:26.1.2:build :fabric:26.2:build`.
- Static analysis: `./gradlew :fabric:26.2:ecjCheck` (fails only on errors).
- **A Cowork sandbox cannot decompile Minecraft** — builds run natively on the developer's machine (or
  the local Gradle MCP). Verify the cells build before marking any task done; never sign off on an
  uncompiled change.

## Out of scope for V1

- Actual gameplay content (belongs in Nerospace, Nerotech, etc. — Core stays a library).
- The shared GUI/widget toolkit, capability-discovery service, KubeJS/CraftTweaker bindings, and dev
  overlay — all **stretch goals** tracked for 1.1+.
- The NeroPower split decision (deferred until Nerotech's energy system reveals whether power deserves a
  standalone mod).

## Definition of done for 1.0.0

- All six systems present with published `api` packages and at least one reference impl/base where
  applicable.
- All six cells build and pass `ecjCheck`; all hand-authored JSON validates.
- POPIA/GDPR erasure hook, retention config, and opt-out command implemented and documented.
- Public API documented and frozen; `mod_version` bumped to `1.0.0`.
- Changes left **staged** for the developer to review and commit — never committed automatically.
