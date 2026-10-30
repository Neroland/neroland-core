package za.co.neroland.nerolandcore.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
//? if >=1.21.6 && <1.21.9 {
/*import net.minecraftforge.eventbus.api.bus.BusGroup;
*///?}
//? if <1.21.6 {
/*import net.minecraftforge.eventbus.api.IEventBus;
*///?}

/**
 * Forge {@link EntityRegistrationSupport.Plumbing}: buffers everything and flushes it from
 * {@code EntityAttributeCreationEvent} / {@code SpawnPlacementRegisterEvent}. Both are
 * dispatched on their own static bus, so a single set of listeners — installed by Core's entry
 * point during bootstrap, and defensively on first use — covers every mod. The
 * {@code attach(...)} bus-group argument is accepted for parity with the other loaders and
 * ignored.
 *
 * <p>Registered via {@code META-INF/services/
 * za.co.neroland.nerolandcore.entity.EntityRegistrationSupport$Plumbing}.
 */
public final class ForgeEntityRegistrationPlumbing implements EntityRegistrationSupport.Plumbing {

    private static boolean listenersInstalled;

    //? if >=1.21.9 {
    /** Install the flush listeners on the shared event buses (idempotent). */
    public static synchronized void installListeners() {
        if (listenersInstalled) {
            return;
        }
        listenersInstalled = true;
        EntityAttributeCreationEvent.BUS.addListener(ForgeEntityRegistrationPlumbing::onAttributes);
        SpawnPlacementRegisterEvent.BUS.addListener(ForgeEntityRegistrationPlumbing::onSpawnPlacements);
    }
    //?}
    // Before 1.21.9 both events are mod-bus events: Core listens on its own mod bus (handed in by the entry
    // point); every mod's buffered registrations are flushed from there.
    //? if >=1.21.6 && <1.21.9 {
    /*public static synchronized void installListeners(Object modBus) {
        if (listenersInstalled) {
            return;
        }
        listenersInstalled = true;
        BusGroup modBusGroup = (BusGroup) modBus;
        EntityAttributeCreationEvent.getBus(modBusGroup).addListener(ForgeEntityRegistrationPlumbing::onAttributes);
        SpawnPlacementRegisterEvent.getBus(modBusGroup).addListener(ForgeEntityRegistrationPlumbing::onSpawnPlacements);
    }
    *///?}
    //? if <1.21.6 {
    /*public static synchronized void installListeners(Object modBus) {
        if (listenersInstalled) {
            return;
        }
        listenersInstalled = true;
        IEventBus modEventBus = (IEventBus) modBus;
        modEventBus.addListener(ForgeEntityRegistrationPlumbing::onAttributes);
        modEventBus.addListener(ForgeEntityRegistrationPlumbing::onSpawnPlacements);
    }
    *///?}

    private static void onAttributes(EntityAttributeCreationEvent event) {
        EntityRegistrationSupport.flushAttributes((type, builder) -> event.put(type, builder.build()));
    }

    private static void onSpawnPlacements(SpawnPlacementRegisterEvent event) {
        EntityRegistrationSupport.flushSpawnPlacements(new EntityRegistrationSupport.PlacementSink() {
                    @Override
                    public <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placementType,
                            Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
                        event.register(type, placementType, heightmap, predicate,
                                SpawnPlacementRegisterEvent.Operation.REPLACE);
                    }
                });
    }

    @Override
    public void onRegistrationAdded() {
        // (before 1.21.9 the listeners need a mod bus: installed by Core's entry point / attach(..))
        //? if >=1.21.9 {
        installListeners();
        //?}
    }

    @Override
    public void attach(Object loaderEventBus) {
        //? if >=1.21.9 {
        installListeners();
        //?} else {
        /*installListeners(loaderEventBus);
        *///?}
    }
}
