# Neroland Core — Data, Privacy & Compliance (POPIA / GDPR)

Core stores some player-keyed gameplay state, so it owns the ecosystem's
data-protection contract. Every Core-storing mod inherits these rules. Part of
[V1](V1-PLAN.md) Phase 7.

## What Core stores

Keyed by player **UUID**, gameplay state only:

- progression gate flags (`ProgressionState`),
- last-login timestamp for retention (`PlayerActivity`).

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
Each Core system registers an eraser at init (progression, currency, reputation,
activity), and so must every downstream mod that stores player data
(NeroEconomy, NeroFactions, NeroSecurity, NeroQuests, NeroEvents):

```java
PlayerDataErasure.register((server, uuid) -> myStore.forget(uuid));
```

One call then clears them all:

```java
PlayerDataErasure.erase(server, playerUuid);
```

Players and admins drive it via command:

- `/neroland data eraseme` — a player erases their own Neroland data (opt-out / reset).
- `/neroland data erase <uuid>` — an admin (op level 2) erases a specific player.

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
