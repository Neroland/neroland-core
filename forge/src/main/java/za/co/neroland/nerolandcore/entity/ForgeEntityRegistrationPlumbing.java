package za.co.neroland.nerolandcore.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;

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

    /** Install the flush listeners on the shared event buses (idempotent). */
    public static synchronized void installListeners() {
        if (listenersInstalled) {
            return;
        }
        listenersInstalled = true;
        EntityAttributeCreationEvent.BUS.addListener(event ->
                EntityRegistrationSupport.flushAttributes((type, builder) -> event.put(type, builder.build())));
        SpawnPlacementRegisterEvent.BUS.addListener(event ->
                EntityRegistrationSupport.flushSpawnPlacements(new EntityRegistrationSupport.PlacementSink() {
                    @Override
                    public <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placementType,
                            Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
                        event.register(type, placementType, heightmap, predicate,
                                SpawnPlacementRegisterEvent.Operation.REPLACE);
                    }
                }));
    }

    @Override
    public void onRegistrationAdded() {
        installListeners();
    }

    @Override
    public void attach(Object loaderEventBus) {
        installListeners();
    }
}
