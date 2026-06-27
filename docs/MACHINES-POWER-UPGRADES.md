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

## What Core does not ship

Core provides the framework, not content: no concrete machines, generators or
upgrade-module items. Nerotech and NeroPower add those, extending
`AbstractMachineBlockEntity`, registering against the energy capability, and defining
their upgrade items + classifier.
