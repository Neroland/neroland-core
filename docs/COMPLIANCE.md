# Neroland Core — Data, Privacy & Compliance (POPIA / GDPR)

Core stores some player-keyed gameplay state, so it owns the ecosystem's
data-protection contract. Every Core-storing mod inherits these rules. Part of
[V1](V1-PLAN.md) Phase 7.

## What Core stores

Keyed by player **UUID**, gameplay state only:

- progression gate flags (`ProgressionState`),
- typed material milestones (`MaterialMilestoneState`),
- last-login timestamp for retention (`PlayerActivity`),
- NeroLink alerts (`LinkAlerts`) — per-player gameplay notifications (module id,
  severity, short non-personal text, timestamps), keyed by owning UUID.

Core does **not** store balances or reputation itself — those are contracts
NeroEconomy / NeroFactions implement. Core never stores names, IPs, chat, or location
history beyond gameplay need.

## Data minimisation & logging

- Player records hold only a UUID plus the gameplay value.
- **No player data is logged at `info` level.** Logs carry public version strings,
  config keys, gate ids and anonymous counts — never a UUID, name, or world data.
  Erasure logs an anonymous acknowledgement ("Player data erased on request"), not who.

## Right to erasure — the shared hook

`PlayerDataErasure` is the single hook that purges a player across **every** system.
Each Core system registers an eraser at init (progression gates and material milestones, currency, reputation,
activity, NeroLink alerts), and so must every downstream mod that stores player data
(NeroEconomy, NeroFactions, NeroSecurity, NeroQuests, NeroEvents):

```java
PlayerDataErasure.register((server, uuid) -> myStore.forget(uuid));
```

One call then clears them all:

```java
PlayerDataErasure.erase(server, playerUuid);
```

Material milestone subject exports use
`MaterialMilestones.exportPlayer(server, playerUuid)`. It returns only the player's UUID-keyed rows;
shared team/server progression is not personal data and is therefore not included.

Players and admins drive it via command:

- `/neroland data eraseme` — a player erases their own Neroland data (opt-out / reset).
- `/neroland data erase <uuid>` — an admin (op level 2) erases a specific player.

Erasure also reaches the **recovery backups**. Every Core store is loaded through
`SavedDataRecovery`, which keeps a last-known-good copy of each `.dat` beside it
(`data/nerolandcore_<store>_backup.dat`). That backup holds the same player-keyed rows as the
primary file, so each store's `eraseFor(server, uuid)` entry point forces an immediate backup
refresh right after purging the player, instead of leaving the erased rows there until the next
periodic pass. **Any downstream mod that adopts `SavedDataRecovery` must do the same in its
`PlayerDataEraser`.**

### Proving it — the erasure conformance harness

`data.ErasureConformance` is a reusable harness in Core's **main** source set, so any mod can run
it from its own test suite against its own stores:

```java
UUID player = UUID.randomUUID();
myState.record(player, ...);                                  // seed, so the probe is not vacuous
PlayerDataErasure.register((server, uuid) -> myState.forgetPlayer(uuid));

ErasureConformance.create()
        .probe("mymod:player_rows", uuid -> myState.has(uuid))
        .verify(null, player);   // real MinecraftServer instead of null in a game test
```

A run asserts that (1) every probe held data before the request and none after, (2) an eraser that
throws — `RuntimeException` **or** `Error` — does not stop the erasers registered after it, and
(3) `CurrencyProvider.forgetPlayer` and `ReputationProvider.forgetPlayer` were actually reached, and
that the bound provider does not merely inherit Core's default no-op body. On failure it names the
subsystem that retained data; it never carries the player UUID.

## Known gap — team-scoped progression rows survive individual erasure

`ProgressionState` and `MaterialMilestoneState` both keep a **team-scoped** map alongside their
UUID-keyed one, used when a gate or milestone is declared with `scope: team`. Those rows are keyed
by **scoreboard team name**, not by player UUID, and `forgetPlayer(UUID)` does not touch them. After
`/neroland data eraseme`, a team-scoped gate the player helped open is still recorded.

**Why it is tolerated.** A scoreboard team name is not in itself a personal identifier — it is a
shared, admin-created label like `builders` or `red`, and the row records what *the team* achieved,
not what a person did. There is also no safe automatic fix: a team row is shared, so clearing it on
one member's erasure request would revoke progression the remaining members legitimately earned —
trading one person's erasure right against several others' game state. For the same reason,
`MaterialMilestones.exportPlayer(server, uuid)` returns only UUID-keyed rows; shared team and server
progression is not treated as the requester's personal data and is not included in a subject-access
export.

**Residual risk.** The edge case this reasoning does not cover is a **single-member team named after
its player** — `Dario`, `Steve123`. There the team name *is* effectively personal data, the row
identifies exactly one person, and it outlives the erasure request. Servers where players create
their own one-person teams are precisely where this happens.

**Recommended mitigation** (not implemented — documented so operators can act on it):

1. **Operating policy, effective today.** Do not let team names carry player names. Create teams
   with role/colour names (`builders`, `red`, `alpha`) and treat a one-person team named after its
   player as a data-protection defect to be renamed. `/team` names are admin-controlled on most
   servers, so this is enforceable without a code change.
2. **On an erasure request, check for a matching team.** If the erased player was the only member of
   a team named after them, that team's rows must be removed to complete the request. Core exposes
   no command or public API for team-scope rows today, so this is currently an operator action
   against the world save (`<dimension>/data/nerolandcore_progression.dat`,
   `nerolandcore_material_milestones.dat`, **and the matching `_backup.dat` files**) with the server
   stopped.
3. **The proper fix, deferred to the next major.** Record team *membership* alongside the gates and
   milestones, so a purge can tell a single-member team from a shared one and clear only the former.
   That is a saved-data format change, which the
   [API stability policy](API-STABILITY.md) keeps out of a minor release. An admin command for
   team-scope rows (`/neroland gate team <team> <gate> close`, and an equivalent purge) should land
   with it so step 2 stops being a file edit.

## Retention

`dataRetentionDays` (config; `0` = never, opt-in) sets how long an inactive player's
data is kept. `PlayerActivity` records last login; `/neroland data purge-inactive`
(or `PlayerDataErasure.purgeInactive(server)`) erases everyone past the threshold
through the same shared hook. Run it on a schedule (e.g. a scheduled command) for
hands-off retention.

## For downstream mods

If your mod stores player data, you **must**: key by UUID, store only what gameplay
needs, keep player data out of `info` logs, and register a `PlayerDataEraser`. That's
the whole contract — Core handles the command, retention sweep, and erasure fan-out.
