# Privacy and Data

Neroland Core owns the data-protection contract for the whole ecosystem, built
to POPIA and GDPR principles. Core stores only the minimum gameplay state it
needs, keyed by player UUID, and provides a single shared erasure hook that
every Neroland system and downstream mod registers with.

## What Core stores

Keyed by player UUID, gameplay state only:

- progression gate flags
- material milestones (which materials you have discovered)
- last-login timestamp (for retention)
- NeroLink alerts (module id, severity, a short non-personal message)

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

Erasure also reaches Core's save-file safety copies: the `_backup.dat` last-known-good
copy of each store, and any `.dat.corrupt` file. A `.dat.corrupt` file is a save file
that could not be read and was moved aside so the world could load from the backup;
only one is kept per store, and it is deleted when a player's data is erased.

Since 1.18.0 an erasure saves the affected data files before it reports success, so a
crash straight afterwards cannot bring the erased data back.

### Pets and drones in unloaded chunks

A tamed creature or owned drone in a chunk that is not loaded cannot be reached during
an erasure. Core keeps a small **erasure tombstone** record so mods can release it
when it next loads: a salted one-way hash of the erased player (kept for
`max(dataRetentionDays, 365)` days, then deleted with its salt) and the UUIDs of the
creatures a mod could not reach (kept until each is released). It is never logged or
listed; a player's own export shows it only as yes/no. Mods use the
`ErasureTombstones` API — see `docs/COMPLIANCE.md` in the Core repository.

## Known gap: team-scoped progression

Gates and material milestones declared with `scope: team` are stored against the
**scoreboard team name**, not against a player UUID, so erasing a player does not
remove them. This is deliberate: a team row records what the *team* achieved and is
shared, so clearing it for one member would revoke progression the other members
earned. A team name like `builders` or `red` is not personal data.

The exception is a **one-person team named after its player** (`Dario`, `Steve123`) —
there the team name effectively identifies someone, and the row outlives their erasure
request.

**What server admins should do:**

- Name teams after roles or colours, never after individual players.
- Treat a one-person team named after its player as something to rename.
- If such a team already exists, completing that player's erasure request means
  removing the team's rows as well. There is no command for this yet; it currently
  needs an offline edit of the world save's
  `data/nerolandcore_progression.dat`, `data/nerolandcore_material_milestones.dat`
  and their `_backup.dat` counterparts.

A future major version will record team membership alongside the rows so a purge can
tell single-member teams from shared ones and clear only the former, together with an
admin command for team-scoped rows.

## Retention

The `dataRetentionDays` config (0 = never, opt-in) sets how long an inactive
player's data is kept. `/neroland data purge-inactive` erases everyone past the
threshold through the same shared hook. Since 1.18.0 Core also runs it by itself at
server start and once a day while `dataRetentionDays` is above 0
(`dataRetentionScheduledPurge`, on by default). The same daily pass deletes expired
erasure tombstones. See [Configuration](Configuration.md).

## Crash reporting (Sentry)

Crash reporting is **on by default** and is **opt-out**. When an error caused by
Neroland Core's own code occurs, an anonymous report is sent to Sentry, hosted in
the EU (`ingest.de.sentry.io`). It contains the stack trace and error message (file
paths are scrubbed of your account name), the mod / Minecraft / loader / OS / Java
versions, Core's config values, the ids and versions of your other loaded mods, a
short trail of generic action names, and anonymous per-session stability and timing
data.

It never contains your IP address, username, player UUID, machine name, world data,
chat, coordinates or inventories, and there is no persistent identifier linking
reports. Errors from other mods or Minecraft itself are filtered out, and reports
are capped at 10 per session and deleted after 90 days.

To opt out, set this in `config/nerolandcore.properties` (created on first launch)
and restart:

```properties
telemetryEnabled=false
```

This one switch covers everything Core sends. It is a local setting — the server
does not sync it to clients — so each player and each server decides for itself.
Questions or data requests: [info@neroland.co.za](mailto:info@neroland.co.za).

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
