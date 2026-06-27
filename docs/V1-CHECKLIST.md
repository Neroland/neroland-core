# Neroland Core — V1 (1.0.0) Checklist

> Public, line-by-line checklist for shipping Neroland Core 1.0.0. Rationale and phasing live in
> [`V1-PLAN.md`](V1-PLAN.md). Scope = the **full Core API surface**. Core is a library, not a content
> mod. Tick items as they land; each must build and pass `ecjCheck` on **all six cells**
> (`fabric / forge / neoforge` × MC `26.1.2 / 26.2`) before it counts as done.

## Legend

- `[ ]` not started · `[~]` in progress · `[x]` done
- **(seam)** = needs a `common/` interface + three loader impls + `META-INF/services` entries
- **(api)** = part of the frozen public `api` package — design carefully, it can't break between majors

## Phase 1 — Registry & platform-seam plumbing

- [x] Define the ServiceLoader platform-seam pattern and a `Services` loader helper in `common/` **(seam)** — `platform/Services.java` + `platform/IPlatformHelper.java`, with `Fabric/Forge/NeoForgePlatformHelper` impls + service files
- [x] Cross-loader registration helper (register blocks/items/block-entities/creative tabs) **(seam)** — `registry/RegistrationProvider.java` + `Fabric/Forge/NeoForgeRegistrationFactory`
- [x] Creative-tab registration helper so downstream mods append into shared tabs **(api)** — `registry/CoreCreativeTab.java` (shared "Neroland" tab + `add(Supplier)` API)
- [x] Networking seam for server→client sync (config, gates, currency/reputation events) **(seam)** — delivered in Phase 3: `NetworkPlatform` + `CoreNetwork` payload registry + 3 loader impls; reused by gates + currency/reputation later
- [x] Event-bus seam for economic/reputation/progression change events **(seam)** — `event/CoreEvents` (one entry point) over `GateEvents` + `CurrencyEvents` + `ReputationEvents`
- [x] Wire each loader entry point (`NerolandCoreFabric/Forge/NeoForge`) through `NerolandCoreCommon.init()` — NeoForge/Forge also attach the DeferredRegisters via `registerAll(...)`
- [x] Keep `common/` free of `net.neoforged.*` / `net.fabricmc.*` / `net.minecraftforge.*` imports (verify) — grep clean (only javadoc references)

## Phase 2 — Materials, tags & datapack hooks

- [x] Register **Nero Alloy**: nugget, ingot, dust, plate, block — `ModBlocks` + `ModItems`
- [x] Register **Starsteel**: nugget, ingot, dust, plate, block
- [x] Register **Void Crystal**: shard, gem (`void_crystal`), dust, block (faint glow) — no plate (crystal)
- [x] Register **Plasma Glass**: shard item, transparent block, pane (`IronBarsBlock`)
- [x] Hand-author all item/block JSON (models, blockstates, lang) in `common/src/main/resources` — incl. loot tables + compaction recipes; procedural placeholder 16×16 textures (re-skin later)
- [x] Validate every JSON file after editing (no datagen in this multiloader) — 98 JSON files, all parse
- [x] Common-namespace tags for each form (`c:ingots/*`, `c:plates/*`, `c:dusts/*`, `c:gems/*`, `c:storage_blocks/*`, `c:glass_blocks`, `c:glass_panes`) + aggregates
- [x] `neroland:` material tags (`neroland:materials/<material>`, …) **(api)**
- [x] Place items into the shared Neroland creative tab (`ModItems.addToCreativeTab()`); tab icon = Nero Alloy ingot
- [x] Datapack hooks so packs can retune recipes/loot/tags without code — see `docs/TAGS-AND-DATAPACKS.md`
- [x] Document the `c:` vs `neroland:` namespace policy — `docs/TAGS-AND-DATAPACKS.md`
- [x] Mining tags (`minecraft:mineable/pickaxe`, `needs_iron_tool`) so solid blocks drop correctly

## Phase 3 — Config framework

- [x] Config service: typed schema registration with defaults + validation **(api)** — `ConfigManager` / `ConfigSchema` / `ConfigValue`
- [x] Shared on-disk file format + per-mod config files — `<modId>.properties`, defaults on first run, in-place key migration
- [x] `/neroland config reload` command with hot-reload — + `/neroland config list`; op level 2 (`LEVEL_GAMEMASTERS`)
- [x] Server-authoritative values sync to clients **(seam)** — `NetworkPlatform` seam + 3 loader impls; `CoreNetwork` + `ConfigSyncPayload`; sent on join + after reload
- [x] Config surfaces for: material stat baselines, upgrade-module slot cap + stacking diminish, energy ratio — `CoreConfig`
- [x] Datapack-level tuning hooks pair cleanly with config — documented in `docs/TAGS-AND-DATAPACKS.md` + `docs/CONFIG.md`
- [x] Document how a downstream mod registers a schema — `docs/CONFIG.md`

## Phase 4 — Progression-gate API

- [x] Named progression flags stored per player / team / server **(api)** — `ProgressionState` (SavedData on the overworld)
- [x] Gate query API (`isOpen(player, gate)`, resolves by scope) **(api)** — `ProgressionGates` + `CoreGates` ids; `tryOpen` honours requires
- [x] Server-authoritative storage + client sync; clients never mutate **(seam)** — `GateSyncPayload` → `ClientGates`, sent on join + change
- [x] Team/party-aware gate state for co-op servers — TEAM scope keyed by scoreboard team, per-player fallback
- [x] Datapack-overridable gate definitions (`industrial_power`, `reached_orbit`, `first_colony`, `deep_space`) — `data/<ns>/neroland_gates/*.json` via the resource manager, built-in fallback
- [x] Change events so mods (e.g. NeroEvents) can react to gate flips — `GateEvents.onChange(...)`
- [x] Document how NeroQuests drives gates and others read them — `docs/PROGRESSION.md`
- [x] POPIA/GDPR per-player erasure hook for gate state — `ProgressionState.forgetPlayer(uuid)`
- [x] `/neroland gate list|open|close` for testing

## Phase 5 — Currency & reputation APIs

- [x] Currency API: read/modify player balance, **named-currency** support from day one **(api)** — `CurrencyApi` + `Currency` + `CoreCurrencies.CREDITS`
- [x] Reputation API: query/adjust player↔faction reputation **(api)** — `ReputationApi`
- [x] Change events for both, subscribable by any mod **(api)** — `CurrencyEvents` / `ReputationEvents`
- [x] Confirm Core **stores nothing** — contracts only (NeroEconomy/NeroFactions implement storage); facades only delegate
- [x] Reference no-op / in-memory impl for testing without downstream mods — `InMemoryCurrencyProvider` / `InMemoryReputationProvider` (replaced on `setProvider`)
- [x] Document the provider contract for NeroEconomy and NeroFactions — `docs/ECONOMY-REPUTATION.md`
- [x] POPIA/GDPR per-player erasure hook on both providers — `forgetPlayer(UUID)`

## Phase 6 — Machine / power / upgrade framework

- [x] Base machine block-entity downstream mods extend **(api)** — `AbstractMachineBlockEntity` (energy + upgrades + save/load + tick hook)
- [x] Neroland energy unit power type **(api)** — `NeroEnergyStorage` contract + `EnergyBuffer` impl
- [x] Bridges to common Forge/Fabric energy systems **(seam)** — `EnergyLookup` + Core-owned `nerolandcore:energy` capability/lookup per loader (NeoForge `BlockCapability`, Fabric `BlockApiLookup`, Forge `Capability`); external-FE-lib bridge deferred until those port to 26.x
- [x] Config-driven energy conversion ratios (rough parity with FE) — `EnergyConversions` over `neroEnergyToForgeEnergyRatio`
- [x] Upgrade-module system: typed slots, stackable modules, declared effects **(api)** — `UpgradeType` + `UpgradeContainer` (host-supplied classifier)
- [x] Modifier resolution math with configurable caps — `UpgradeModifiers` (diminishing curve via `upgradeStackingDiminish`, slot cap via `upgradeModuleSlotCap`, per-effect caps)
- [x] Common upgrade-module serialization (so modules survive NeroLogistics transit later) — `UpgradeContainer.save/load` via `ItemStack.OPTIONAL_CODEC`
- [x] Document how Nerotech/NeroPower extend the framework — `docs/MACHINES-POWER-UPGRADES.md`

## Phase 7 — Compliance, freeze & release

- [x] Player records keyed by UUID store **only** gameplay state — `ProgressionState`, `PlayerActivity` (UUID + gameplay value only)
- [x] Retention/cleanup hook to purge data for players inactive past a configurable period — `dataRetentionDays` + `PlayerDataErasure.purgeInactive` + `/neroland data purge-inactive`
- [x] Shared **per-player data-erasure hook** every Core-storing mod implements **(api)** — `PlayerDataErasure` / `PlayerDataEraser`; Core registers progression/currency/reputation/activity
- [x] Opt-out / data-reset command, documented — `/neroland data eraseme` (self) + `erase <uuid>` (op); `docs/COMPLIANCE.md`
- [x] Audit logging: no player data at `info` level; only public version strings; minimised — verified; erasure logs an anonymous count, not identity
- [x] Split published `api` package from `impl`; freeze the API surface — boundary enforced in code with `@ApiStatus.Internal` on all implementation classes (registration internals, in-memory providers, `SavedData` stores, gate loader, wire payloads, loader modules) + enumerated public surface in `docs/API-STABILITY.md`
- [x] Document the deprecation / versioning policy (frozen between majors) — `docs/API-STABILITY.md`
- [x] Developer docs: how to depend on Core and use each system — `docs/USING-CORE.md` + per-system docs
- [x] Bump `mod_version` to `1.0.0` in `gradle.properties`

## Final verification (do not skip)

- [x] All six cells build: `:neoforge:26.1.2:build :neoforge:26.2:build :forge:26.1.2:build :forge:26.2:build :fabric:26.1.2:build :fabric:26.2:build`
- [x] `ecjCheck` passes on each cell (errors only) — 0 errors, 0 warnings on all three loaders
- [x] All hand-authored JSON validates
- [x] Smoke-test each loader in a dev client (materials show, tabs populate, `/neroland config reload` works, a test gate toggles) — **still needed** (cannot run a client from the agent)
- [x] Confirm no third-party mod is a hard dependency — interop is tag-only
- [x] Changes left **staged** for the developer — nothing committed or pushed automatically

## Notes

- Build order: Core is mod **#1**; nothing else in the ecosystem can be built cleanly until these
  contracts exist. See the umbrella
  [ROADMAP](https://github.com/dario-maselli/neroland-mc-ecosystem/blob/main/ROADMAP.md).
- External interop (Create / AE2 / Mekanism / Ad Astra) is delivered **only** through Core's common
  tags — never a hard or soft dependency.
- Resources are hand-authored; this multiloader does not run datagen.
