# Progression Gates

A progression gate is a named milestone (for example
`nerolandcore:reached_orbit`). NeroQuests drives gates open as players complete
objectives, and every other Neroland mod simply asks "is this gate open?"
before unlocking content. Gate state is **server-owned** and synced to clients;
clients never mutate it.

## Scopes

Every gate declares one scope. Resolution is automatic.

| Scope | Meaning |
| --- | --- |
| player (default) | Per-player arc |
| team | Shared by a vanilla scoreboard team (co-op) |
| server | One shared flag for the whole world |

A team gate falls back to per-player if the player has no team.

## Gates are data

Each gate is one JSON file under `data/<namespace>/neroland_gates/<path>.json`.
The gate id is the file's namespace plus path.

| Field | Meaning |
| --- | --- |
| `scope` | player, team, or server |
| `requires` | List of gate ids that must already be open |
| `title` | Display title |

A pack overrides a gate by shipping the same id, or adds its own gate with a new
id. Later packs win.

## The canonical arc

Core ships this default chain:

- `industrial_power`
- `reached_orbit`
- `first_colony`
- `deep_space`

`tryOpen` only opens a gate when **every** gate in its `requires` list is
already open. The explicit open/close operations force the state regardless.
Every change fires a change event and re-syncs the affected players.

## Privacy

Gate storage is POPIA/GDPR-minimal: a player row holds only a UUID plus the gate
ids. Per-player erasure is supported through Core's shared erasure hook. See
[Privacy and Data](Privacy-and-Data.md).

## Commands

These are mainly for testing (op level 2 for open/close):

| Command | Effect |
| --- | --- |
| `/neroland gate list` | List gates |
| `/neroland gate open <id>` | Force a gate open |
| `/neroland gate close <id>` | Force a gate closed |

A bare id like `reached_orbit` is treated as `nerolandcore:reached_orbit`.

## For developers

The full progression API is documented in
[../docs/PROGRESSION.md](../docs/PROGRESSION.md).

## See also

- [Economy and Reputation](Economy-and-Reputation.md)
- [Commands](Commands.md)
- [Privacy and Data](Privacy-and-Data.md)
- [Home](Home.md)
