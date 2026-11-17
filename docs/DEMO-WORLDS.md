# Nero Mods Demo worlds

Since **1.20.0** Core can create a **Nero Mods Demo** world: a fixed-seed creative showroom with a hub and
one exhibit (a "plot") per selected Nero mod. A mod takes part by registering a `DemoModule` with Core; it
needs no other dependency. Pin `nerolandcore_version=1.20.0` or later.

The runtime (button, picker, world creation, building) is available on **Minecraft 26.3**. The API types
compile on every Minecraft line Core builds for, so a module can be registered from shared code; on older
lines it is simply never asked to build.

## What the player sees

1. A **Nero Mods Demo** button in the top-left corner of the singleplayer world list. (With no saved worlds
   at all, vanilla skips that list and opens Create World, so the button appears there instead.)
2. A picker listing every registered module whose required mods are loaded, each with its icon, name and a
   checkbox (all ticked), and a **Create Demo World** button. The icon is a texture, not an item: the picker
   runs on the main menu, before any world has loaded, when item stacks cannot be made.
3. A new singleplayer world named "Nero Mods Demo": creative, cheats on, a fixed seed, an empty (void)
   overworld, and showroom game rules (no daylight or weather cycle, no natural mob spawning, keep inventory,
   respawn radius 0, quiet command blocks). Time is set to noon and the weather cleared.
4. On the world's first start Core builds the hub at `(0, 64, 0)`, a path running east, and one plot per
   selected module in plot order, then asks each module to build its exhibit. The player spawns on the hub.
   The build runs once; later starts leave the world alone.

The same selection always produces the same world.

## Registering a module

```java
public final class MyDemo implements DemoModule {
    @Override public String modId() { return "mymod"; }
    @Override public Component displayName() { return Component.translatable("demo.mymod.name"); }
    @Override public Identifier icon() {                         // a square texture; client only
        return Identifier.fromNamespaceAndPath("mymod", "textures/item/hero.png");
    }
    @Override public int sortOrder() { return 500; }                // lower = nearer the hub; Core is 0
    @Override public List<String> requiredMods() { return List.of(); } // other mods a cross-mod exhibit needs
    @Override public DemoFootprint footprint() { return DemoFootprint.of(24, 16); } // X by Z, 5..96
    @Override public List<Component> guide() {                     // floats at the plot entrance
        return List.of(Component.translatable("demo.mymod.guide.1"));
    }
    @Override public void build(DemoBuildContext ctx) {
        ctx.set(ctx.at(4, 1, 4), MyBlocks.HERO_BLOCK.get().defaultBlockState());
        ctx.label(ctx.at(4, 3, 4), Component.translatable("demo.mymod.label.hero"));
    }
}
```

Register it once from common init, and let a failure never take your mod down:

```java
try {
    DemoRegistry.register(new MyDemo());
} catch (RuntimeException e) {
    LOGGER.warn("demo module not registered: {}", e.getClass().getSimpleName());
}
```

## Rules for `build`

- **Where:** `ctx.origin()` is the north-west corner of the plot floor (y 64). Build at `y + 1` and above,
  inside your footprint. You may also build in **your own** dimensions (for example a viewing outpost on a
  planet), never in another mod's.
- **Deterministic:** place the same things in the same places every time. No randomness, no clock reads, no
  dependence on which other modules were selected.
- **Entities are fine:** natural spawning is off, but anything you add with `addFreshEntity` stays.
- **Failures are contained:** an exception is caught and logged by Core, and the other modules still build.
- **No player data:** do not read, store or log anything about the player. Place machines and entities
  unowned.

`DemoBuildContext` helpers:

| Helper | What it does |
| ------ | ------------ |
| `at(dx, dy, dz)` / `set(pos, state)` | Plot-relative positions; set a block with updates |
| `label(pos, text)` | A floating one-line label (an invisible, invulnerable armor stand's name) |
| `guide(top, lines)` / `guide(level, top, lines)` | Up to six stacked label lines |
| `travelPad(plate, dimension, destination, yaw, pitch)` | A walk-on pressure plate over a command block that moves whoever stands on it |
| `returnPad(level, plate)` | A travel pad back to the hub, for use on an outpost |

## What Core stores

One marker per demo world (`nerolandcore:demo`, through `SavedDataRecovery`): that the world is a demo,
the selected module ids, and whether the build finished. Nothing about the player. Worlds that are not demo
worlds are never touched.

## Hiding the button

Set `demoButtonEnabled=false` in Core's config (client-side) to hide the button, for example in a modpack
with its own menus.
