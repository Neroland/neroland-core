# Side Configuration

Every machine built on Neroland Core shares one way to control how each of its six
sides talks to neighbours. You set, per face, whether that side takes a resource
**in**, pushes it **out**, connects to **power**, or is **disabled** — and the
control works the same on a Nero Generator, an Ore Processor, an Oxygen Generator, or
anything else built on Core.

This page is for players and pack makers. Developers should read
[`../docs/SIDE-CONFIG.md`](../docs/SIDE-CONFIG.md).

## The faces

A machine's sides are named relative to the way it faces — front, back, left, right,
top, bottom — so your setup stays correct if you break and replace the machine facing
a different way. The side that was an output is still the output.

Each side shows one of these modes, colour-coded so you can read it at a glance:

- **Disabled** (grey) — nothing connects on that side.
- **Input** (blue) — neighbours can put the resource in.
- **Output** (orange) — neighbours can pull the resource out.
- **I/O** (green) — both in and out on the same side.
- **Push** (yellow) — an output that actively sprays the resource to whatever is next
  to it, even without pipes.

## Channels

A machine can route more than one kind of resource: **items**, **fluid**, **gas**,
and **power** (energy). Each kind is its own "channel" with its own per-face setup. A
generator that only handles power shows a single page; a processor that handles items
and power shows a tab for each.

## Using the Side Config tab

Open the machine and click the Side Config tab. On the flattened cube:

- **Left-click** a side to cycle its mode.
- **Right-click** a side to disable it quickly.
- **Middle-click** a side to pick a specific mode from a palette.

You also get per-channel **auto-eject** and **auto-input** toggles (the machine
pushes out of / pulls into those sides on its own, up to the pack's transfer-rate
setting), a **reset** button to return to the machine's default layout, and
**copy/paste** to apply one machine's layout to another of the same type.

## The Configurator

Some content mods (such as Nerotech) add a wrench-style **Configurator** item: right-
click a machine face in the world to cycle that side's mode without opening the GUI,
sneak-right-click to read the current mode, and copy/paste a whole layout between
machines.

## For pack makers

The auto-eject / auto-input speed is a single server-side value,
`sideConfigAutoTransferRate`, in Core's config — see
[Configuration](Configuration.md). It is hot-reloadable with `/neroland config
reload`. Set it to `0` to disable automatic transfer entirely and rely on pipes.

## Privacy

Side configuration is saved with the machine (block) in the world. It records only
how a machine routes resources — never anything about you — so it falls outside
Core's player-data handling. See [Privacy & Data](Privacy-and-Data.md).
