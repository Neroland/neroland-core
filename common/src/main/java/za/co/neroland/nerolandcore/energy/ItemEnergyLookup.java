package za.co.neroland.nerolandcore.energy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * Item side of the energy seam (Core 1.21.0): find the {@link NeroEnergyStorage} held by an item stack
 * (a battery, a power cell, a charged tool), on every loader including Fabric.
 *
 * <p>Mods that make energy-holding items register an {@link ItemEnergyProvider} once at init. Tools,
 * chargers and machines that want to fill or drain "any Nero energy item" call {@link #find(ItemStack)}
 * instead of knowing each mod's item classes. This is a plain registry with no loader capability behind
 * it, so it behaves the same on Fabric, Forge and NeoForge; a mod that also wants third-party
 * Forge-Energy items to see its items keeps registering its loader item capability as before.</p>
 *
 * <p>Lookup is in registration order and the first non-null answer wins. A provider that throws is
 * skipped (logged once per provider, with its class name only) so one broken mod cannot break every
 * charger.</p>
 */
public final class ItemEnergyLookup {

    private static final List<ItemEnergyProvider> PROVIDERS = new CopyOnWriteArrayList<>();
    private static final List<ItemEnergyProvider> REPORTED = new CopyOnWriteArrayList<>();

    private ItemEnergyLookup() {
    }

    /** Register a provider. Call once at init; registering the same instance twice is a no-op. */
    public static void register(ItemEnergyProvider provider) {
        if (provider != null && !PROVIDERS.contains(provider)) {
            PROVIDERS.add(provider);
        }
    }

    /** The energy view of {@code stack}, or {@code null} if it is empty or no provider handles it. */
    @Nullable
    public static NeroEnergyStorage find(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return firstMatch(stack);
    }

    /** Whether any provider exposes energy for {@code stack}. */
    public static boolean holdsEnergy(@Nullable ItemStack stack) {
        return find(stack) != null;
    }

    /** Provider resolution without the empty-stack check; package-private for plain-JVM tests. */
    @Nullable
    static NeroEnergyStorage firstMatch(ItemStack stack) {
        for (ItemEnergyProvider provider : PROVIDERS) {
            try {
                NeroEnergyStorage storage = provider.find(stack);
                if (storage != null) {
                    return storage;
                }
            } catch (RuntimeException failure) {
                if (!REPORTED.contains(provider)) {
                    REPORTED.add(provider);
                    NerolandCoreCommon.LOGGER.warn("[Neroland Core] Item energy provider {} failed ({}); skipping it",
                            provider.getClass().getName(), failure.getClass().getName());
                }
            }
        }
        return null;
    }

    /** Test teardown only. */
    @org.jetbrains.annotations.ApiStatus.Internal
    public static void clearForTests() {
        PROVIDERS.clear();
        REPORTED.clear();
    }
}
