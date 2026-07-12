package za.co.neroland.nerolandcore.registry;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import za.co.neroland.nerolandcore.platform.Services;

/**
 * Cross-loader registration seam — the MultiLoader-Template alternative to
 * Architectury's {@code DeferredRegister} (which has not ported to 26.x).
 *
 * <p>Common code obtains a provider for a vanilla registry and registers
 * factories that receive the entry's {@link ResourceKey} — so the value can set
 * its own id ({@code Properties.setId(key)}, mandatory since 1.21.2). Each loader
 * ships one {@link Factory} implementation, resolved via {@link Services}
 * ({@link java.util.ServiceLoader}):
 * <ul>
 *   <li>NeoForge / Forge wrap a {@code DeferredRegister} (attached to the mod bus
 *       by the loader entry point);</li>
 *   <li>Fabric calls {@code Registry.register} eagerly.</li>
 * </ul>
 *
 * <p>This is the single registration entry point every Neroland Core system
 * (materials, blocks, items, block-entities, menus, creative tabs, …) and every
 * downstream Nero mod registers through, so registration stays loader-agnostic.
 */
public interface RegistrationProvider<T> {

    static <T> RegistrationProvider<T> get(ResourceKey<? extends Registry<T>> registryKey, String modId) {
        return Factory.INSTANCE.create(registryKey, modId);
    }

    /** Attach registrations created by the calling downstream mod to its loader event bus. */
    static void attach(Object loaderEventBus) {
        Factory.INSTANCE.attach(loaderEventBus);
    }

    <I extends T> RegistryEntry<I> register(String name, Function<ResourceKey<T>, I> factory);

    /** A registered entry: a {@link Supplier} of the value plus its id. */
    interface RegistryEntry<R> extends Supplier<R> {
        @Override
        R get();

        Identifier id();
    }

    /** Loader-provided bridge to the platform registry. */
    interface Factory {
        Factory INSTANCE = Services.load(Factory.class);

        <T> RegistrationProvider<T> create(ResourceKey<? extends Registry<T>> registryKey, String modId);

        /** Fabric registers eagerly; deferred-register loaders override this hook. */
        default void attach(Object loaderEventBus) {
        }
    }
}
