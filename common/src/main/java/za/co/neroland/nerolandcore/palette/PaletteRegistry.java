package za.co.neroland.nerolandcore.palette;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.Identifier;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * The shared, read-only palette export (added in Core 1.9.0). Any mod or tooling can
 * ask Core "what colour/finish is {@code neroland:starsteel}?" and get back a
 * {@link Finish} whose sRGB colour + emissive metadata exactly match the tone Core uses
 * on that material — so downstream trims, decor blocks and GUIs line up without copying
 * hex constants around. <b>NeroDecor is the first consumer</b> (its paint gun and
 * gen_textures palette both read this).
 *
 * <p><b>Contract.</b> This is a registry of cosmetic metadata only — colours and
 * emissive flags. It stores no behaviour and no player data. Core registers its own
 * built-in finishes ({@link CoreFinishes}) during init; other mods may
 * {@link #register(Finish) contribute} their own finishes (namespaced to themselves)
 * so a single palette spans the ecosystem. Reads are safe from any thread.
 *
 * <p><b>Stability.</b> Part of Core's public API from 1.9.0; the method signatures and
 * the {@code neroland:} finish ids Core ships are a frozen contract within the 1.x major
 * (see {@code docs/API-STABILITY.md}). {@link #API_VERSION} is bumped only if the
 * <i>shape</i> of {@link Finish} changes.
 */
public final class PaletteRegistry {

    /** The palette-export schema version; bump only when {@link Finish}'s shape changes. */
    public static final int API_VERSION = 1;

    private static final Map<Identifier, Finish> FINISHES = new ConcurrentHashMap<>();

    private PaletteRegistry() {
    }

    /**
     * Register (or replace) a finish. A second registration for the same id replaces the
     * first and logs a warning — mods should namespace finish ids to themselves to avoid
     * clashing with Core's {@code neroland:} set.
     *
     * @param finish the finish to expose
     * @return the same finish, for convenient static-field assignment
     */
    public static Finish register(Finish finish) {
        if (FINISHES.put(finish.id(), finish) != null) {
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Palette finish '{}' replaced.", finish.id());
        }
        return finish;
    }

    /** The finish for {@code id}, if one is registered. */
    public static Optional<Finish> getFinish(Identifier id) {
        return Optional.ofNullable(FINISHES.get(id));
    }

    /** Whether a finish is registered for {@code id}. */
    public static boolean contains(Identifier id) {
        return FINISHES.containsKey(id);
    }

    /**
     * An immutable, insertion-independent snapshot of every registered finish — safe to
     * iterate while other threads read. Mutating the returned collection is unsupported.
     */
    public static Collection<Finish> all() {
        return Collections.unmodifiableCollection(new LinkedHashMap<>(FINISHES).values());
    }

    /** How many finishes are currently registered. */
    public static int size() {
        return FINISHES.size();
    }

    /** Force class-load of the built-in finishes. Called once from Core init. */
    public static void init() {
        CoreFinishes.registerAll();
        NerolandCoreCommon.LOGGER.info("[Neroland Core] Palette export ready: {} finish(es) (schema v{}).",
                FINISHES.size(), API_VERSION);
    }
}
