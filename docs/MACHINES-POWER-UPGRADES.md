# Neroland Core — Machine / Power / Upgrade Framework

The shared base Nerotech, NeroPower and any other machine mod build on, so energy,
upgrade modules and machine boilerplate behave identically across the ecosystem.
Part of [V1](V1-PLAN.md) Phase 6.

## Power type: Nero energy (NE)

`NeroEnergyStorage` is the loader-neutral energy contract every Nero machine,
generator and cable exposes. `EnergyBuffer` is the ready-made bounded
implementation:

```java
EnergyBuffer energy = new EnergyBuffer(capacity, maxIO, maxIO, this::setChanged);
energy.generate(8);          // a generator producing NE (bypasses the input limit)
energy.consume(work * cost); // a machine spending NE
energy.insert(amount, false);// external input (cables), bounded by maxIO
```

Energy is measured in **Nero energy units (NE)**. `EnergyConversions` converts to/from
Forge Energy using the config ratio `neroEnergyToForgeEnergyRatio` (`CoreConfig`),
which the per-loader bridges use when adapting to a loader's native FE energy.

## Base machine block-entity

Extend `AbstractMachineBlockEntity` instead of re-implementing the boilerplate — it
bundles an `EnergyBuffer` + an `UpgradeContainer` and persists both:

```java
public final class GrinderBlockEntity extends AbstractMachineBlockEntity {
    public GrinderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRINDER.get(), pos, state,
              /*capacity*/ 100_000, /*maxIO*/ 1_000, /*upgradeSlots*/ 4,
              GrinderBlockEntity::classifyUpgrade);
    }

    @Override
    protected void serverTick(Level level, BlockPos pos, BlockState state) {
        UpgradeModifiers mods = modifiers();
        int cost = (int) Math.round(BASE_COST * mods.energyMultiplier());
        if (energyBuffer().has(cost)) { energyBuffer().consume(cost); /* do work */ }
    }
}
```

Register a ticker that calls `AbstractMachineBlockEntity.tick(level, pos, state, be)`,
and override `saveAdditional`/`loadAdditional` for your own extra state (call
`super` — the base saves energy + upgrades).

## Upgrade modules

`UpgradeType` (SPEED, EFFICIENCY, RANGE, CAPACITY) describes a module's effect. A
machine embeds an `UpgradeContainer` of slots and supplies a `Classifier` mapping a
stack to its type — so **Core doesn't ship the module items**; a downstream mod
defines the items and the mapping (often a tag check):

```java
static UpgradeType classifyUpgrade(ItemStack stack) {
    if (stack.is(MyTags.SPEED_MODULE)) return UpgradeType.SPEED;
    if (stack.is(MyTags.EFFICIENCY_MODULE)) return UpgradeType.EFFICIENCY;
    return null; // not a module
}
```

`UpgradeModifiers` turns module counts into multipliers — `speedMultiplier()`,
`energyMultiplier()`, `capacityMultiplier()`, `rangeBonus()` — applying a
configurable diminishing curve (`upgradeStackingDiminish`) and hard caps. Slot count
is clamped to `upgradeModuleSlotCap`. Because every machine across every mod uses this
one resolver, modules are interchangeable and balance is tuned in one place
(`CoreConfig`).

## Cross-loader energy interop

Core owns the shared `nerolandcore:energy` capability/lookup on each loader, exposed
through the `EnergyLookup` seam:

```java
NeroEnergyStorage neighbour = EnergyLookup.INSTANCE.find(level, pos, side);
if (neighbour != null && neighbour.canReceive()) {
    long moved = neighbour.insert(energyBuffer().extract(budget, true), false);
    energyBuffer().extract(moved, false);
}
```

A downstream machine **registers its block-entity against Core's capability** so other
Nero machines (in any mod) can find it:

- **NeoForge** — in `RegisterCapabilitiesEvent`:
  `event.registerBlockEntity(NeoForgeEnergyLookup.ENERGY, MY_BE.get(), (be, side) -> be.getEnergy());`
- **Fabric** — `FabricEnergyLookup.ENERGY.registerForBlockEntity((be, side) -> be.getEnergy(), MY_BE.get());`
- **Forge** — attach a provider for `ForgeEnergyLookup.ENERGY` in `AttachCapabilitiesEvent`.

Bridging to external Forge Energy / Fabric energy libraries is deferred until those
libraries port to MC 26.x (Nerospace reached the same conclusion); `EnergyConversions`
is ready for when they do.

## Generic fluid and gas storage

Core mirrors the energy seam for two more resource types, so a downstream mod can pipe
fluids and gases the same way it pipes NE. These are **generic**: Core ships no specific
fluids or gases, only the storage contracts and their cross-loader capabilities.

### Fluid

`NeroFluidStorage` is the loader-neutral fluid contract; `FluidBuffer` is the ready-made
bounded implementation (single-fluid, mB-measured), shaped like `EnergyBuffer`:

```java
FluidBuffer fluid = new FluidBuffer(capacity, this::setChanged);
fluid.fill(stack, false);     // external input (pipes/buckets), bounded by capacity
fluid.drain(amount, false);   // external output
```

Core owns the shared `nerolandcore:fluid` capability/lookup on each loader, exactly like
`nerolandcore:energy`.

### Gas

`NeroGasStorage` is the gas contract; `GasBuffer` is its bounded implementation. A gas is
identified by an `Identifier` (Core ships no concrete gases — `NeroGases` is the helper for
naming and resolving them), so a buffer learns which gas it holds from the first gas inserted:

```java
GasBuffer gas = new GasBuffer(capacity, this::setChanged);
gas.insert(NeroGases.of(myGasId), amount, false); // claims the gas until drained empty
gas.extract(amount, false);
```

Core owns the shared `nerolandcore:gas` capability/lookup on each loader.

## Storage blocks Core ships

Core ships four **passive storage endpoints** — the foundation library's first
block-entities — plus a Creative variant of each. They hold a resource and expose it on a
capability for pipes/hoppers; they are not *machines* (no processing, no generation, no
upgrade slots):

| Block | Holds | Capability exposed |
| --- | --- | --- |
| **Battery** (`nerolandcore:battery`) | Nero energy (NE) | `nerolandcore:energy` |
| **Fluid Tank** (`nerolandcore:fluid_tank`) | one fluid | `nerolandcore:fluid` |
| **Gas Tank** (`nerolandcore:gas_tank`) | one gas | `nerolandcore:gas` |
| **Item Store** (`nerolandcore:item_store`) | items (vanilla `Container`) | the standard item handler |

The Battery rides Core's energy API (`EnergyBuffer`); the Fluid Tank and Gas Tank ride the
new `FluidBuffer` / `GasBuffer`. The Item Store is a plain vanilla `Container` — it opens the
vanilla chest GUI and interoperates with hoppers and pipes through the standard item
capability, so Core registers nothing extra for it. All four are pickaxe / iron-tier
mineable, drop themselves, and ship crafting recipes that reference the existing `#c:`
material tags.

The Creative variants (`nerolandcore:creative_*`, creative-tab only, unbreakable, no recipe)
are endless sources and sinks for testing networks. Because Core is generic, two of them are
configured in-world rather than hard-wired:

- **Creative Fluid Tank** starts **empty**; right-click it with a filled bucket to set the
  endless fluid it provides.
- **Creative Gas Tank** **learns its gas from the first gas piped into it** (it is not tied
  to any particular gas).
- **Creative Battery** is an endless energy source/sink as shipped.
- **Creative Item Store** provides an endless stream of one configured item.

### Per-loader capability exposure

Each storage block registers its block-entity against Core's matching capability on every
loader — energy/fluid/gas through Core's `*Lookup` seams, plus the vanilla item handler for
the Item Store — using the same registration as any downstream machine. Downstream mods
register **their own** block-entities against `nerolandcore:fluid` / `nerolandcore:gas`
exactly as they already do against `nerolandcore:energy` (see the energy examples above),
so a downstream tank and a Core tank are interchangeable on the same pipe network.

## What Core does not ship

Core provides the framework plus these passive storage endpoints — but still **no concrete
machines, generators, or upgrade-module items**, and no specific fluids or gases. Nerotech
and NeroPower add those, extending `AbstractMachineBlockEntity`, registering against the
energy/fluid/gas capabilities, defining their upgrade items + classifier, and naming their
own fluids/gases.
