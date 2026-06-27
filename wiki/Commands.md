# Commands

Every Neroland Core command lives under the `/neroland` root. Commands that change server state or
other players require **op level 2** (gamemaster); the personal data command is available to any
player for their own data.

## Config

| Command | Permission | Effect |
| ------- | ---------- | ------ |
| `/neroland config reload` | op 2 | Re-reads every registered config schema from disk and re-syncs server-authoritative values to online clients. |
| `/neroland config list` | op 2 | Prints each schema's values, flagging the server-authoritative ones. |

See [Configuration](Configuration.md).

## Progression gates

| Command | Permission | Effect |
| ------- | ---------- | ------ |
| `/neroland gate list` | any | Lists every defined gate, its scope, and whether it's open for you. |
| `/neroland gate open <id>` | op 2 | Forces a gate open for you. |
| `/neroland gate close <id>` | op 2 | Forces a gate closed for you. |

A bare id such as `reached_orbit` is treated as `nerolandcore:reached_orbit`. See
[Progression Gates](Progression-Gates.md).

## Data & privacy

| Command | Permission | Effect |
| ------- | ---------- | ------ |
| `/neroland data eraseme` | any | Erases **your own** Neroland data across every system (opt-out / reset). |
| `/neroland data erase <uuid>` | op 2 | Erases a specific player's Neroland data. |
| `/neroland data purge-inactive` | op 2 | Erases data for every player inactive past the `dataRetentionDays` threshold. |

See [Privacy & Data](Privacy-and-Data.md).

## See also

- [Configuration](Configuration.md)
- [Progression Gates](Progression-Gates.md)
- [Privacy & Data](Privacy-and-Data.md)
