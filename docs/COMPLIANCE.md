# Neroland Core — Data, Privacy & Compliance (POPIA / GDPR)

Core stores some player-keyed gameplay state, so it owns the ecosystem's
data-protection contract. Every Core-storing mod inherits these rules. Part of
[V1](V1-PLAN.md) Phase 7.

## What Core stores

Keyed by player **UUID**, gameplay state only:

- progression gate flags (`ProgressionState`),
- typed material milestones (`MaterialMilestoneState`),
- last-login timestamp for retention (`PlayerActivity`),
- erasure tombstones (`ErasureTombstones`, since 1.18.0) — see
  [Erasure tombstones](#erasure-tombstones--entities-in-unloaded-chunks) below,
- NeroLink alerts (`LinkAlerts`) — per-player gameplay notifications (module id,
  severity, short non-personal text, timestamps), keyed by owning UUID.
  Capped per player (`linkAlertsMaxPerPlayer`, default 50) and per module
  (`linkAlertsMaxPerModule`, default 25) since 1.15.0. Over a cap, the oldest acknowledged alert
  is evicted first, then the oldest.

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

When a primary `.dat` cannot be read at all (truncated or corrupt), `SavedDataRecovery` moves it
aside as `<file>.dat.corrupt` before vanilla reads it, so the store loads from the backup (or
starts fresh) and the broken file does not trip recovery again on every load. Only one
quarantined copy is kept per store. It can still hold player-keyed rows, so the same forced backup
refresh (`SavedDataRecovery.backupNow`) also deletes it, and fails the erasure if it cannot.

### Erasure reaches the disk before the request returns (1.18.0)

Stores marked changed are written to their main `.dat` file only at the next autosave, so a crash
between an erasure and that save would load the old main file and bring the erased rows back. Since
1.18.0 `PlayerDataErasure.eraseAndReport` (which `erase`, both erase commands and the retention sweep
use) ends by refreshing the tombstone backup and **saving the overworld's data storage** — every
changed store there, which is where Core and every Nero mod keep their `SavedData` — before it
returns. Called from another thread, the save is queued on the server thread. A failed save is logged
(exception class only) and makes the request report "incomplete"; the next world save writes it. The
retention sweep saves once at the end of the sweep, not once per player.

### Erasure tombstones — entities in unloaded chunks

Vanilla keeps an owner UUID on the entity (tamed animals, owned drones, companions). An erasure cannot
reach an entity in an unloaded chunk, so when it next loads it still names the erased player. Core
keeps one shared record so each mod's load-time check can release it (`data/nerolandcore/erasure_tombstones.dat`
on 26.x, `data/nerolandcore_erasure_tombstones.dat` before; plus its `SavedDataRecovery` backup):

| Kind | Content | Written by | Kept for |
| --- | --- | --- | --- |
| Owner tombstone | 128-bit truncation of SHA-256 over a random per-world salt and the erased UUID, plus the time | Core, inside `eraseAndReport`, before any mod eraser runs | `max(dataRetentionDays, 365)` days; the salt is deleted with the last one |
| Entity tombstone | the UUID of an entity the mod could not free (names no player), plus the time | the mod's eraser (`markEntity`) | until the mod reports it freed (`entityFreed`) |

**Necessity.** Without the record an erased player's pet, sitting in a chunk nobody has loaded since,
would load later and write the erased UUID back into the mod's index. The record is what finishes the
erasure the player asked for (GDPR art. 6(1)(c) with art. 17; POPIA s 11(1)(c) with s 24).

**Is it personal data?** The owner tombstone is **pseudonymised** personal data: it cannot be
reversed, but an operator holding the salt (same file) and a list of UUIDs could test each one. It is
minimised: never the UUID or a name; no listing API; never logged (only `count()` for diagnostics and
the number expired); a player's own export may carry it only as a yes/no (`isOwnerErased`). Entity
tombstones name only a game object. A hard cap of 65,536 per kind bounds the file; reaching it drops
the oldest and logs a WARN with the count — never silently.

**Retention.** Owner tombstones are expired at server start and daily (see [Retention](#retention));
`isOwnerErased` already ignores an expired one. A year minimum, because a tombstone that expired with
a short retention period would defeat itself. Entity tombstones do not expire: the entity loading and
being freed is what ends them.

**API** (`za.co.neroland.nerolandcore.data.ErasureTombstones`):

```java
ErasureTombstones t = ErasureTombstones.get(server);
t.markEntity(entityUuid);           // in your PlayerDataEraser, for each owned entity you could not free
t.shouldRelease(owner, entityUuid); // on load, only for an entity your own index does not hold
t.entityFreed(entityUuid);          // after releasing it
t.ownerReturned(owner);             // when recording a NEW ownership for a player (fresh taming)
t.isOwnerErased(owner);             // yes/no, e.g. in that player's own export
t.count();                          // diagnostics
```

Release silently: nothing may be sent to or about the erased player. Conformance:
`ErasureConformance.create().tombstoneReleases("mymod:unloaded_pet", store, entityUuid)`.

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
through the same shared hook.

Since 1.18.0 Core runs the sweep itself: once when the server starts and once a day while it runs,
when `dataRetentionDays > 0` and `dataRetentionScheduledPurge` is on (the default; set it to `false`
to purge only by command). The same daily pass expires owner tombstones older than
`max(dataRetentionDays, 365)` days, whatever `dataRetentionDays` is. Both log counts only.

## For downstream mods

If your mod stores player data, you **must**: key by UUID, store only what gameplay
needs, keep player data out of `info` logs, and register a `PlayerDataEraser`. That's
the whole contract — Core handles the command, retention sweep, and erasure fan-out.
