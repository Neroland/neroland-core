# Neroland Core — Progression Gates

The server-authoritative milestone system the whole ecosystem paces against. Part of
[V1](V1-PLAN.md) Phase 4.

## The idea

A **gate** is a named milestone (`nerolandcore:reached_orbit`). NeroQuests drives
gates open as players complete objectives; every other mod just asks "is this gate
open?" before unlocking content, so the Earth → space → colonies arc stays in step
across mods. Gate state is owned by the server and synced to clients — clients never
mutate it.

## Scopes

Each gate declares a `GateScope`:

- **player** — owned per player (default; each player advances their own arc);
- **team** — shared by a vanilla scoreboard team (co-op progression);
- **server** — one shared flag for the whole world.

`ProgressionGates.isOpen(player, gate)` resolves automatically to the gate's scope
(a team-scoped gate falls back to per-player if the player has no team).

## Defining gates (datapack-overridable)

Gates are data: one JSON per gate under
`data/<namespace>/neroland_gates/<path>.json`, where the id is the file's namespace
+ path:

```json
{
  "scope": "player",
  "requires": ["nerolandcore:industrial_power"],
  "title": "Reached Orbit"
}
```

Core ships the canonical arc — `industrial_power → reached_orbit → first_colony →
deep_space` — and a pack overrides a gate by shipping the same id, or adds its own.
`CoreGates` mirrors the canonical ids in code (with built-in defaults as a fallback
when no datapack copy is present), so Core code can reference
`CoreGates.REACHED_ORBIT` directly.

## Reading and driving gates

```java
// read (any mod gating content)
if (ProgressionGates.isOpen(player, CoreGates.REACHED_ORBIT)) { /* unlock */ }

// drive open (NeroQuests on objective complete) — respects requires
ProgressionGates.tryOpen(player, CoreGates.REACHED_ORBIT);

// server-wide milestone
ProgressionGates.setServerGate(server, someGate, true);

// client side (e.g. a HUD), reads the synced mirror
ClientGates.isOpen(CoreGates.REACHED_ORBIT);
```

`tryOpen` only opens when every `requires` gate is already open; `open`/`close`
force it. Every change fires a `GateEvents` notification and re-syncs affected
players.

## Reacting to changes

```java
GateEvents.onChange(change -> {
    if (change.open() && change.gate().equals(CoreGates.REACHED_ORBIT)) {
        // e.g. NeroEvents triggers an orbital event
    }
});
```

## Storage, sync & privacy

State persists in a single overworld `SavedData` (`ProgressionState`): a server set,
a per-player map (keyed by UUID), and a per-team map (keyed by team name). On join
and after any change, the server pushes each player their resolved open-gate set
(`GateSyncPayload` → `ClientGates`).

**POPIA/GDPR:** player rows hold only a UUID and gate ids — never names, IPs, chat,
or location. `ProgressionState.forgetPlayer(uuid)` purges a player; this is the
hook Core's shared per-player erasure will call so one request clears a player
across every Core-storing system.

## Command (testing)

- `/neroland gate list` — every defined gate, its scope, and whether it's open for you.
- `/neroland gate open|close <id>` — force a gate for yourself (op level 2). A bare
  `reached_orbit` is treated as `nerolandcore:reached_orbit`.
