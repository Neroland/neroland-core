# Privacy and Data

Neroland Core owns the data-protection contract for the whole ecosystem, built
to POPIA and GDPR principles. Core stores only the minimum gameplay state it
needs, keyed by player UUID, and provides a single shared erasure hook that
every Neroland system and downstream mod registers with.

## What Core stores

Keyed by player UUID, gameplay state only:

- progression gate flags
- last-login timestamp (for retention)

Core does **not** store balances or reputation itself — those are
[NeroEconomy and NeroFactions contracts](Economy-and-Reputation.md). Core never
stores names, IPs, chat, or location history beyond gameplay need.

## Data minimisation

- player records hold only a UUID plus the gameplay value
- no player data is logged at info level
- logs carry public version strings, config keys, gate ids, and anonymous counts
- erasure logs an anonymous acknowledgement, not who was erased

## Right to erasure

A single shared hook purges a player across every system. Each Core system and
every downstream mod that stores player data registers an eraser at init.

| Command | Who | Effect |
| --- | --- | --- |
| `/neroland data eraseme` | Any player | Erase your own Neroland data (opt-out / reset) |
| `/neroland data erase <uuid>` | Admin (op level 2) | Erase a specific player |

## Retention

The `dataRetentionDays` config (0 = never, opt-in) sets how long an inactive
player's data is kept. `/neroland data purge-inactive` erases everyone past the
threshold through the same shared hook — run it on a schedule for hands-off
retention. See [Configuration](Configuration.md).

## For downstream mods

If your mod stores player data:

- key by UUID
- store only what gameplay needs
- keep player data out of info logs
- register an eraser

## For developers

The full compliance guidance is documented in
[../docs/COMPLIANCE.md](../docs/COMPLIANCE.md).

## See also

- [Configuration](Configuration.md)
- [Progression Gates](Progression-Gates.md)
- [Economy and Reputation](Economy-and-Reputation.md)
- [Commands](Commands.md)
- [Home](Home.md)
