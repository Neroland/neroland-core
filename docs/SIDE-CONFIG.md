# Machine Side Configuration

Neroland Core 1.3.0 ships a universal, Mekanism-style side-configuration system in
`za.co.neroland.nerolandcore.sideconfig`. Every machine in the ecosystem gets one
consistent way to decide, per face, how it interacts with its neighbours — which
side pulls a resource in, which pushes it out, which connects to power, and which is
disabled. The behaviour lives in Core, so a mod author declares the surface once and
never hand-rolls face routing, and the player learns the control once.

This is the developer reference. The ecosystem design rationale lives in
`neroland-mc-ecosystem/neroland-core/SIDE-CONFIG.md`; the player-facing page is in
[`../wiki/Side-Config.md`](../wiki/Side-Config.md).

## The model

A machine declares which resource **channels** it has — `ITEM`, `FLUID`, `GAS`,
`ENERGY` — each mapped onto an existing Core layer (the item capability,
`NeroFluidStorage`, `NeroGasStorage`, and the power-type capability respectively;
the UI labels `ENERGY` as **Power**).

For each channel, every one of the six faces holds a `SideMode`:

- `DISABLED` — no connection, no capability exposed on that face.
- `INPUT` — accepts the resource (insert-only view); pulled if auto-input is on.
- `OUTPUT` — emits the resource (extract-only view); pushed if auto-eject is on.
- `IO` — both insert and extract on the same face, where the channel allows it.
- `PUSH` — an output that actively ejects even without a logistics network; behaves
  like `OUTPUT` as a capability view but is always auto-ejected.

Faces are **relative to the machine's facing** (`FRONT`, `BACK`, `LEFT`, `RIGHT`,
`TOP`, `BOTTOM`), so a configuration travels with the block when it is rotated.
`RelativeFace` holds the pure rotation math (unit-tested without the game) and
`FaceResolver` converts to and from a Minecraft `Direction` against the block's
`HORIZONTAL_FACING`. Vertically-facing machines clamp to a NORTH reference in v1.

Slotted channels group inventory slots into named `SlotGroup`s — conventionally
`input`, `output`, `battery`, `upgrade`, but any name is allowed. A face in `INPUT`
binds to the channel's input group(s); `OUTPUT` to the output group(s). A specific
face's input or output can be bound to a specific group (for the Item Sorter's
per-face filtered outputs) without per-slot face binding.

## Author API

Declare the surface once at block-entity construction:

```java
SideConfig config = SideConfig.builder()
    .channel(Channel.ITEM, SlotGroup.of("input", 0), SlotGroup.of("output", 1))
    .channel(Channel.ENERGY)
    .defaultPreset(SidePreset.PROCESSOR)
    .allow(Channel.ITEM, SideMode.IO, false) // this machine forbids item I/O
    .build();
```

Presets seed sensible starting faces: `GENERATOR`, `PROCESSOR`, `STORAGE`,
`ALL_INPUT`, `ALL_DISABLED`. A preset that would pick a forbidden mode is clamped to
the nearest permitted one.

### Two ways to adopt it

By **extension** — a machine that extends `AbstractMachineBlockEntity` calls
`installSideConfig(config)` in its constructor (pre-wired to the machine's energy
buffer) and chains `withItems(...)` / `withFluid(...)` / `withGas(...)`:

```java
installSideConfig(config).withItems(() -> this);
```

By **composition** — a machine with its own hierarchy holds a `SideConfigComponent`
and implements `SideConfigured`:

```java
public final class MyMachine extends MyBaseBE implements SideConfigured {
    private final SideConfigComponent sideConfig =
        new SideConfigComponent(config, this)
            .withEnergy(this::getEnergy)
            .withGas(this::getGas);

    @Override public SideConfigComponent sideConfig() { return sideConfig; }
}
```

Either way, persist it in `saveAdditional` / `loadAdditional`
(`sideConfig.save(output)` / `sideConfig.load(input)` — the base BE does this
automatically), and call `sideConfig.serverTick(level, pos, rate)` from the ticker if
the machine wants auto-transfer (the base BE does this too).

## Capability gating

Capability registration returns a handler for `(face, channel)` only when the
resolved mode permits the operation. Wire each loader's registration to the
component's gated views:

```java
// NeoForge
event.registerBlockEntity(ENERGY, MY_BE.get(), (be, side) -> be.sideConfig().energyView(side));
// Fabric
ENERGY.registerForBlockEntity((be, side) -> be.sideConfig().energyView(side), MY_BE.get());
```

`energyView` / `fluidView` / `gasView` return an insert-only, extract-only, full, or
`null` view depending on the face's mode, reading the mode live on every operation so
a held reference never goes stale. Items gate through the vanilla `WorldlyContainer`
hooks — delegate `getSlotsForFace`, `canPlaceItemThroughFace` and
`canTakeItemThroughFace` to `itemSlotsForFace` / `canInsertItem` / `canExtractItem`.
On any mode change the component fires a neighbour update and (on NeoForge)
invalidates cached capabilities so adjacent pipes reconnect.

## Auto-eject / auto-input

Optional per-channel toggles, off by default, push from `OUTPUT`/`PUSH` faces and
pull into `INPUT` faces against adjacent Core capabilities each tick, bounded by the
server-authoritative `sideConfigAutoTransferRate` config value (NE for energy, mB for
fluid/gas, item count for items; `0` disables). Item auto-transfer covers adjacent
vanilla containers; mod pipes still pull through the gated `WorldlyContainer`.

## Persistence & networking

Side config serialises as one packed `int` per channel (six 3-bit face modes plus the
two auto toggles) in the BE's NBT, saved with the chunk; it survives unload and
dimension travel. It is server-authoritative: the client sends a
`SideConfigIntentPayload` ("set face X channel C to mode M", or cycle / toggle / reset
/ paste / request), the server validates it against the channel's `allow(...)` rules
and a reach check, applies it, and streams the authoritative `SideConfigSyncPayload`
back. Clients never mutate routing directly.

## UI

`SideConfigWidget` is a reusable, self-contained tab a downstream
`AbstractContainerScreen` embeds: a flattened cube/net of the six faces (left-click
cycles a face, right-click disables it, middle-click opens a mode palette), one
sub-tab per channel, auto-eject/auto-input toggles, and copy/paste/reset controls,
colour-coded by `SideModeColors`. The downstream screen forwards `render` and
`mouseClicked`; the widget requests a snapshot on open and applies sync updates from
`ClientSideConfig`.

## Configurator

`Configurator` is the server-side cycle-mode API for an in-world wrench: `cycle`,
`read`, `snapshot`, and `apply`. Core ships the API; the item, its texture and recipe
ship in a content mod (Nerotech), which calls these from the item's `useOn` (and
stashes copy/paste snapshots in its own stack NBT).

## Privacy (POPIA / GDPR)

Side configuration is world/block data keyed by position — no player UUID, no
identity, nothing personal. It sits outside the `PlayerDataErasure` scope, is never
logged at info level, and carries no telemetry.
