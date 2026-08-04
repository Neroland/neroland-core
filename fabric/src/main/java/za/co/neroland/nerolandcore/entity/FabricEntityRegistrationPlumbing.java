package za.co.neroland.nerolandcore.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Fabric {@link EntityRegistrationSupport.Plumbing}: Fabric has no registration event for
 * either surface, so both are applied the moment they are declared —
 * {@code FabricDefaultAttributeRegistry.register} for attributes and the vanilla
 * {@code SpawnPlacements.register} static for placements. Downstream mods must therefore
 * declare them after their entity types exist (the usual Fabric-eager ordering rule).
 *
 * <p>Registered via {@code META-INF/services/
 * za.co.neroland.nerolandcore.entity.EntityRegistrationSupport$Plumbing}.
 */
public final class FabricEntityRegistrationPlumbing implements EntityRegistrationSupport.Plumbing {

    private static final EntityRegistrationSupport.PlacementSink PLACEMENT_SINK =
            new EntityRegistrationSupport.PlacementSink() {
                @Override
                public <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placementType,
                        Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
                    SpawnPlacements.register(type, placementType, heightmap, predicate);
                }
            };

    @Override
    public void onRegistrationAdded() {
        EntityRegistrationSupport.flushAttributes(FabricDefaultAttributeRegistry::register);
        EntityRegistrationSupport.flushSpawnPlacements(PLACEMENT_SINK);
    }
}
