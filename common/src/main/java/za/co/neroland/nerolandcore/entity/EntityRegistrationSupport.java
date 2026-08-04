package za.co.neroland.nerolandcore.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.levelgen.Heightmap;

import org.jetbrains.annotations.ApiStatus;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.platform.Services;

/**
 * Cross-loader entity registration seam (added in Core 1.10.0) — the counterpart to
 * {@link za.co.neroland.nerolandcore.registry.RegistrationProvider} for the two pieces of
 * mob setup that have <b>no vanilla cross-loader home</b>: default attributes and natural
 * spawn placements.
 *
 * <p>Each loader solves them differently — NeoForge with {@code EntityAttributeCreationEvent}
 * / {@code RegisterSpawnPlacementsEvent} on the mod event bus, Forge with
 * {@code EntityAttributeCreationEvent} / {@code SpawnPlacementRegisterEvent}, Fabric with
 * {@code FabricDefaultAttributeRegistry} plus the vanilla {@code SpawnPlacements.register}
 * static, applied immediately. Downstream common code should not care. It declares both
 * through this facade and Core's per-loader plumbing flushes the buffer at the right moment:
 *
 * <pre>{@code
 * EntityRegistrationSupport entities = EntityRegistrationSupport.get("nerocreatures");
 * entities.registerAttributes(ModEntities.VOID_CRAWLER, VoidCrawler::createAttributes);
 * entities.registerSpawnPlacement(ModEntities.VOID_CRAWLER,
 *         SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
 *         (type, level, reason, pos, random) -> level.getBlockState(pos).isAir());
 * }</pre>
 *
 * <p><b>Suppliers, not values.</b> On the deferred-register loaders the {@link EntityType}
 * does not exist yet while a mod is constructing, so both the type and the attribute builder
 * are taken as {@link Supplier}s and resolved only at flush time. Pass a
 * {@code RegistrationProvider.RegistryEntry} straight in — it is already a {@code Supplier}.
 *
 * <p><b>Zero loader wiring required.</b> Core attaches its own listeners during its bootstrap,
 * and the loader events that carry these registrations are global, so a downstream mod that
 * only calls the two register methods from common code is fully wired. {@link #attach(Object)}
 * is offered for parity with {@code RegistrationProvider.attach(...)} and is safe to call
 * (idempotent — every buffered entry is applied at most once, whichever bus flushes first).
 *
 * <p><b>Ordering.</b> Fabric applies registrations eagerly, so call these <i>after</i> your
 * entity types are registered (i.e. late in your common {@code init()}), exactly as with any
 * other Fabric-eager registration.
 *
 * <p><b>No player data crosses this API.</b> It carries entity types, attribute values and
 * spawn predicates only, so nothing routes through Core's per-player erasure hook.
 */
public final class EntityRegistrationSupport {

    private static final List<AttributeRegistration> ATTRIBUTES = new ArrayList<>();
    private static final List<PlacementRegistration<?>> PLACEMENTS = new ArrayList<>();

    private final String modId;

    private EntityRegistrationSupport(String modId) {
        this.modId = modId;
    }

    /** A handle for {@code modId}'s registrations (kept for diagnostics; buffers are shared). */
    public static EntityRegistrationSupport get(String modId) {
        return new EntityRegistrationSupport(Objects.requireNonNull(modId, "modId"));
    }

    /**
     * Declare the default {@link net.minecraft.world.entity.ai.attributes.Attribute} values for
     * a living entity type. Mandatory for every {@code LivingEntity} — a type without them
     * crashes the first time it spawns.
     */
    public void registerAttributes(Supplier<? extends EntityType<? extends LivingEntity>> type,
            Supplier<AttributeSupplier.Builder> attributes) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(attributes, "attributes");
        synchronized (ATTRIBUTES) {
            ATTRIBUTES.add(new AttributeRegistration(this.modId, type, attributes));
        }
        Plumbing.INSTANCE.onRegistrationAdded();
    }

    /**
     * Declare a natural-spawn placement rule (where the game is allowed to consider spawning
     * this mob at all). This is the placement half only — biome/weight selection stays with
     * the owning mod's own spawn definitions or datapack biome modifiers.
     */
    public <T extends Mob> void registerSpawnPlacement(Supplier<? extends EntityType<T>> type,
            SpawnPlacementType placementType, Heightmap.Types heightmap,
            SpawnPlacements.SpawnPredicate<T> predicate) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(placementType, "placementType");
        Objects.requireNonNull(heightmap, "heightmap");
        Objects.requireNonNull(predicate, "predicate");
        synchronized (PLACEMENTS) {
            PLACEMENTS.add(new PlacementRegistration<>(this.modId, type, placementType, heightmap, predicate));
        }
        Plumbing.INSTANCE.onRegistrationAdded();
    }

    /**
     * Optional: attach Core's flush listeners to the calling mod's own loader event bus
     * (NeoForge {@code IEventBus}, Forge {@code BusGroup}; ignored on Fabric). Idempotent and
     * unnecessary in the common case — see the class notes.
     */
    public static void attach(Object loaderEventBus) {
        Plumbing.INSTANCE.attach(loaderEventBus);
    }

    /** Apply every not-yet-applied attribute registration. Called by Core's loader plumbing. */
    @ApiStatus.Internal
    public static void flushAttributes(AttributeSink sink) {
        List<AttributeRegistration> pending;
        synchronized (ATTRIBUTES) {
            pending = List.copyOf(ATTRIBUTES);
        }
        for (AttributeRegistration registration : pending) {
            registration.apply(sink);
        }
    }

    /** Apply every not-yet-applied spawn placement. Called by Core's loader plumbing. */
    @ApiStatus.Internal
    public static void flushSpawnPlacements(PlacementSink sink) {
        List<PlacementRegistration<?>> pending;
        synchronized (PLACEMENTS) {
            pending = List.copyOf(PLACEMENTS);
        }
        for (PlacementRegistration<?> registration : pending) {
            registration.apply(sink);
        }
    }

    /** Receives one resolved (entity type, attribute builder) pair. */
    public interface AttributeSink {
        void accept(EntityType<? extends LivingEntity> type, AttributeSupplier.Builder builder);
    }

    /** Receives one resolved placement rule. */
    public interface PlacementSink {
        <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placementType,
                Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate);
    }

    /**
     * Loader-provided flush plumbing, resolved via {@link Services}
     * ({@link java.util.ServiceLoader}). Not part of the downstream surface — mods call the
     * static/instance methods above.
     */
    public interface Plumbing {

        Plumbing INSTANCE = Services.load(Plumbing.class);

        /**
         * Called after every buffered registration. Fabric applies it immediately; the
         * event-driven loaders no-op and flush inside their registration events.
         */
        void onRegistrationAdded();

        /** Attach flush listeners to a mod's loader event bus. Fabric needs none. */
        default void attach(Object loaderEventBus) {
        }
    }

    private static final class AttributeRegistration {

        private final String modId;
        private final Supplier<? extends EntityType<? extends LivingEntity>> type;
        private final Supplier<AttributeSupplier.Builder> attributes;
        private boolean applied;

        AttributeRegistration(String modId, Supplier<? extends EntityType<? extends LivingEntity>> type,
                Supplier<AttributeSupplier.Builder> attributes) {
            this.modId = modId;
            this.type = type;
            this.attributes = attributes;
        }

        synchronized void apply(AttributeSink sink) {
            if (this.applied) {
                return;
            }
            this.applied = true;
            try {
                sink.accept(this.type.get(), this.attributes.get());
            } catch (RuntimeException e) {
                // Never let one mod's broken entry abort the whole batch. Mod id only — no player data.
                NerolandCoreCommon.LOGGER.error(
                        "[Neroland Core] Failed to apply default attributes registered by mod '{}'",
                        this.modId, e);
            }
        }
    }

    private static final class PlacementRegistration<T extends Mob> {

        private final String modId;
        private final Supplier<? extends EntityType<T>> type;
        private final SpawnPlacementType placementType;
        private final Heightmap.Types heightmap;
        private final SpawnPlacements.SpawnPredicate<T> predicate;
        private boolean applied;

        PlacementRegistration(String modId, Supplier<? extends EntityType<T>> type,
                SpawnPlacementType placementType, Heightmap.Types heightmap,
                SpawnPlacements.SpawnPredicate<T> predicate) {
            this.modId = modId;
            this.type = type;
            this.placementType = placementType;
            this.heightmap = heightmap;
            this.predicate = predicate;
        }

        synchronized void apply(PlacementSink sink) {
            if (this.applied) {
                return;
            }
            this.applied = true;
            try {
                sink.register(this.type.get(), this.placementType, this.heightmap, this.predicate);
            } catch (RuntimeException e) {
                // Never let one mod's broken entry abort the whole batch. Mod id only — no player data.
                NerolandCoreCommon.LOGGER.error(
                        "[Neroland Core] Failed to apply spawn placement registered by mod '{}'",
                        this.modId, e);
            }
        }
    }
}
