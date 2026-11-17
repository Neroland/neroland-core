# Nero Mods Demo

The **Nero Mods Demo** builds a ready-made creative showroom of the Nero mods you have installed, so you can
see what each one does before starting a real world. Added in Neroland Core 1.20.0, for Minecraft 26.3.

## Making a demo world

1. Open **Singleplayer**. In the top-left corner of the world list, press **Nero Mods Demo**.
   (If you have no saved worlds yet, the game opens Create World straight away; the button is there instead.)
2. Tick the mods you want to see. Every installed mod with a demo exhibit is listed and ticked.
3. Press **Create Demo World**.

You get a new world called "Nero Mods Demo": creative mode, cheats on, a fixed seed and an empty sky world.
You start on the hub. Walk east along the path: each mod has its own plot with its name and a short guide
floating at the entrance. Some exhibits have **travel plates**; step on one to visit a place, and step on the
plate there to come back.

The world is a showroom: the time stays at noon, the weather stays clear, and no mobs spawn on their own.
Picking the same mods always gives the same world.

## What is stored

Only a small marker inside the demo world itself: that it is a demo world, which mods were picked, and
whether it has been built. Nothing about you is stored or logged.

## Hiding the button

Modpack makers can hide the button by setting `demoButtonEnabled=false` in Core's config. See
[Configuration](Configuration.md).

## For mod authors

See [`docs/DEMO-WORLDS.md`](../docs/DEMO-WORLDS.md) for how to add your mod's exhibit.
