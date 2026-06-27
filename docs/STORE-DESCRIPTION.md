# Neroland Core — CurseForge / Modrinth listing

Copy-paste source for the project pages. Logo: `art/logo/nerolandcore_logo_400.png`.

## Summary (one line)

> The shared foundation library for the Neroland sci-fi mod ecosystem — required by Nerospace and every other Nero mod.

## Categories / tags

Library & API · Forge · NeoForge · Fabric · MC 26.1.2 / 26.2

## Full description (Markdown)

---

# Neroland Core

**Neroland Core is the foundation every Neroland mod is built on.** It's a library
mod: on its own it adds only a handful of shared materials, but it provides the
common backbone — materials, config, progression, economy, machines and energy —
that lets the whole **Neroland** sci-fi ecosystem (starting with **Nerospace**) work
together as one coherent universe.

> ℹ️ **You usually don't install this by itself.** Add it because a mod you want —
> like **Nerospace** — requires it. With Core installed, every Neroland mod shares
> the same materials, energy, economy and progression instead of each reinventing
> them.

## What it gives players

- **Shared materials** — Nero Alloy, Starsteel, Void Crystal and Plasma Glass (ingots,
  nuggets, dusts, plates, blocks, and Plasma Glass panes), all in one organised
  **Neroland** creative tab. Other Nero mods' ores smelt into these, so materials are
  consistent everywhere.
- **One config + one command** — every Neroland mod's settings live in the same place,
  reloadable in-game with `/neroland config reload`.
- **A coherent ecosystem** — shared progression milestones, a single economy/reputation
  layer and one energy standard mean Nero mods feel like one game, not a pile of
  unrelated add-ons.

## What it gives modders

A stable, multiloader API to build a Nero mod on: cross-loader registration + platform
seams, a typed config service with server→client sync, a datapack-driven
progression-gate system, currency & reputation contracts, a base machine block-entity
with a Nero energy power type + upgrade-module framework, and a shared per-player
data-erasure hook. Interop with Create / AE2 / Mekanism / Ad Astra is routed through
common `c:` tags — no hard dependency on any third-party mod. See the GitHub repo for
full developer docs.

## Compatibility

- **Minecraft:** 26.1.2 and 26.2
- **Loaders:** NeoForge, Forge, and Fabric
- **Requires:** Fabric API on Fabric

## Telemetry notice

Neroland Core sends **anonymous error reports** (stack trace + mod/game versions only —
never IPs, usernames, UUIDs, or world data) to the developers via Sentry (EU servers)
so crashes can be fixed. Opt out any time by setting `telemetryEnabled = false` in
`config/nerolandcore.properties`. Full details:
[PRIVACY.md](https://github.com/Neroland/neroland-core/blob/main/PRIVACY.md).

## Links

- Source & developer docs: https://github.com/Neroland/neroland-core
- The flagship mod that uses it: **Nerospace**
- License: All Rights Reserved (modpacks allowed — see LICENSE)
