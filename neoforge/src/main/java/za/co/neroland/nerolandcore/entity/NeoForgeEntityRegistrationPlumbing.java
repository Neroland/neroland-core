package za.co.neroland.nerolandcore.entity;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * NeoForge {@link EntityRegistrationSupport.Plumbing}: buffers everything and flushes it from
 * the two mod-bus registration events. Core's own entry point attaches Core's bus during
 * bootstrap ({@link #attachBus(IEventBus)}); because NeoForge dispatches mod-bus events to
 * every mod's bus and every mod constructor has already run by then, that single attachment is
 * enough for downstream mods too. {@code attach(...)} from a downstream mod is still accepted
 * and harmless — each buffered entry is applied at most once.
 *
 * <p>Registered via {@code META-INF/services/
 * za.co.neroland.nerolandcore.entity.EntityRegistrationSupport$Plumbing}.
 */
public final class NeoForgeEntityRegistrationPlumbing implements EntityRegistrationSupport.Plumbing {

    private static final Set<IEventBus> ATTACHED =
            Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

    /** Attach the flush listeners to a mod event bus (idempotent per bus). */
    public static void attachBus(IEventBus modEventBus) {
        if (modEventBus == null || !ATTACHED.add(modEventBus)) {
            return;
        }
        modEventBus.addListener((EntityAttributeCreationEvent event) ->
                EntityRegistrationSupport.flushAttributes((type, builder) -> event.put(type, builder.build())));
        modEventBus.addListener((RegisterSpawnPlacementsEvent event) ->
                EntityRegistrationSupport.flushSpawnPlacements(new EntityRegistrationSupport.PlacementSink() {
                    @Override
                    public <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placementType,
                            Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
                        event.register(type, placementType, heightmap, predicate,
                                RegisterSpawnPlacementsEvent.Operation.REPLACE);
                    }
                }));
    }

    @Override
    public void onRegistrationAdded() {
        // Deferred: applied when the mod-bus registration events fire.
    }

    @Override
    public void attach(Object loaderEventBus) {
        attachBus((IEventBus) loaderEventBus);
    }
}
