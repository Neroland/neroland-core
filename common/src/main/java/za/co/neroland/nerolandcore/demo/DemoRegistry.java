package za.co.neroland.nerolandcore.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.event.SourceIds;
import za.co.neroland.nerolandcore.platform.Services;

/**
 * The static registry of {@link DemoModule}s (Core 1.20.0). Mirrors {@code MapLayerRegistry}: keyed by mod
 * id, a second registration replaces the first with a WARN, one INFO line per registration, thread-safe.
 *
 * <p>{@link #register} snapshots the module's footprint and required mods once; later calls use the
 * snapshot. Modules are always listed in plot order: {@link DemoModule#sortOrder()} ascending, then mod id.
 * There is deliberately no {@code unregister}: one mod must not be able to drop another's exhibit.
 */
public final class DemoRegistry {

    /** Most modules the registry accepts (one demo world lays out at most {@link DemoLayout#MAX_PLOTS}). */
    public static final int MAX_MODULES = DemoLayout.MAX_PLOTS;

    /** Most other mods one module may require. */
    public static final int MAX_REQUIRED_MODS = 16;

    private static final Map<String, Registered> MODULES = new ConcurrentHashMap<>();

    /** Plot order. */
    private static final Comparator<Registered> ORDER =
            Comparator.comparingInt(Registered::sortOrder).thenComparing(Registered::modId);

    private DemoRegistry() {
    }

    private record Registered(DemoModule module, String modId, int sortOrder, DemoFootprint footprint,
            List<String> requiredMods) {
    }

    /**
     * Register {@code module} under its {@link DemoModule#modId()}. Throws {@link IllegalArgumentException}
     * when the mod id is invalid, the footprint or display name is missing, a required mod id is invalid,
     * more than {@link #MAX_REQUIRED_MODS} mods are required, or the registry is full. Re-registering a mod
     * id replaces the earlier module (WARN).
     */
    public static void register(DemoModule module) {
        Registered r = validate(module);
        if (!MODULES.containsKey(r.modId()) && MODULES.size() >= MAX_MODULES) {
            throw new IllegalArgumentException("The demo registry is full (" + MAX_MODULES + " modules)");
        }
        if (MODULES.put(r.modId(), r) != null) {
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Demo module '{}' replaced.", r.modId());
        }
        NerolandCoreCommon.LOGGER.info("[Neroland Core] Demo module '{}' registered.", r.modId());
    }

    /** The module registered for {@code modId}, if any. */
    public static Optional<DemoModule> module(String modId) {
        Registered r = modId == null ? null : MODULES.get(modId);
        return r == null ? Optional.empty() : Optional.of(r.module());
    }

    /** Every registered module, in plot order; a snapshot copy. */
    public static List<DemoModule> modules() {
        return sorted(MODULES.values()).stream().map(Registered::module).toList();
    }

    /** The registered modules whose required mods are all loaded, in plot order. */
    public static List<DemoModule> available() {
        return available(Services.PLATFORM::isModLoaded);
    }

    /** The registered modules whose required mods all pass {@code isLoaded}, in plot order. */
    public static List<DemoModule> available(Predicate<String> isLoaded) {
        List<DemoModule> out = new ArrayList<>();
        for (Registered r : sorted(MODULES.values())) {
            if (r.requiredMods().stream().allMatch(isLoaded)) {
                out.add(r.module());
            }
        }
        return out;
    }

    /**
     * The modules named in {@code ids}, in plot order (not the order given), skipping unknown ids and
     * duplicates. This is what a demo world builds, so a selection always yields the same world.
     */
    public static List<DemoModule> resolve(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<String> wanted = new HashSet<>(ids);
        List<DemoModule> out = new ArrayList<>();
        for (Registered r : sorted(MODULES.values())) {
            if (wanted.contains(r.modId())) {
                out.add(r.module());
            }
        }
        return out;
    }

    /** The plot layout for {@code modules} (in the order given), from the registration snapshots. */
    public static List<DemoLayout.Plot> layout(List<DemoModule> modules) {
        List<DemoLayout.Request> requests = new ArrayList<>();
        for (DemoModule m : modules) {
            Registered r = MODULES.get(m.modId());
            requests.add(new DemoLayout.Request(m.modId(), r != null ? r.footprint() : m.footprint()));
        }
        return DemoLayout.plan(requests);
    }

    private static List<Registered> sorted(Collection<Registered> values) {
        List<Registered> list = new ArrayList<>(values);
        list.sort(ORDER);
        return list;
    }

    static Registered validate(DemoModule module) {
        if (module == null) {
            throw new IllegalArgumentException("Cannot register a null demo module");
        }
        String modId = module.modId();
        if (!SourceIds.isValidSourceModId(modId)) {
            throw new IllegalArgumentException("A demo module's mod id must match [a-z0-9_.-]{1,64}");
        }
        if (module.displayName() == null) {
            throw new IllegalArgumentException("Demo module '" + modId + "' has no display name");
        }
        DemoFootprint footprint = module.footprint();
        if (footprint == null) {
            throw new IllegalArgumentException("Demo module '" + modId + "' has no footprint");
        }
        List<String> declared = module.requiredMods();
        List<String> required = new ArrayList<>();
        if (declared != null) {
            for (String id : declared) {
                if (!SourceIds.isValidSourceModId(id)) {
                    throw new IllegalArgumentException("Demo module '" + modId + "' requires an invalid mod id");
                }
                if (!id.equals(modId) && !required.contains(id)) {
                    required.add(id);
                }
            }
        }
        if (required.size() > MAX_REQUIRED_MODS) {
            throw new IllegalArgumentException("Demo module '" + modId + "' requires more than "
                    + MAX_REQUIRED_MODS + " mods");
        }
        return new Registered(module, modId, module.sortOrder(), footprint, List.copyOf(required));
    }

    @ApiStatus.Internal
    static void clearForTest() {
        MODULES.clear();
    }
}
