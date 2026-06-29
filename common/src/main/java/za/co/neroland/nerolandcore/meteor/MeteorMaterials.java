package za.co.neroland.nerolandcore.meteor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.config.CoreConfig;
import za.co.neroland.nerolandcore.meteor.MeteorResolution.Candidate;
import za.co.neroland.nerolandcore.meteor.MeteorResolution.Context;
import za.co.neroland.nerolandcore.meteor.MeteorResolution.Tuning;
import za.co.neroland.nerolandcore.progression.ProgressionGates;

/**
 * The public entry point for the Meteor Material Registry — the Core-owned
 * aggregation point any Nero mod declares grindable materials into and that random
 * output machines (the Nerospace meteor grinder first) read from. See
 * {@code docs/METEOR-MATERIAL-REGISTRY.md}.
 *
 * <p>Server-authoritative: aggregation, gate checks, weighting and rolls all run
 * here on the server; the consumer receives only the resulting item(s). The registry
 * holds <b>item metadata only</b> — no player data, nothing to export or erase
 * (POPIA/GDPR).
 */
public final class MeteorMaterials {

    private MeteorMaterials() {
    }

    /** Called once from common init; eagerly warms the annotation scan so failures surface early. */
    public static void init() {
        try {
            int annotated = MeteorAnnotationScanner.INSTANCE.scan().size();
            NerolandCoreCommon.LOGGER.info(
                    "[Neroland Core] Meteor Material Registry ready ({} annotation entr{}; data files load per world).",
                    annotated, annotated == 1 ? "y" : "ies");
        } catch (RuntimeException | LinkageError e) {
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Meteor annotation scan unavailable at init.", e);
        }
    }

    // --- registry access ----------------------------------------------------

    /** Every registered material for {@code server} (loads + caches on first use). */
    public static Collection<MeteorMaterialEntry> all(MinecraftServer server) {
        return MeteorMaterialRegistry.forServer(server).values();
    }

    /** Re-read the data files for {@code server} (call from a datapack reload listener). */
    public static void reload(MinecraftServer server) {
        MeteorMaterialRegistry.reload(server);
    }

    /** Whether {@code item} is a registered grindable material on {@code server} (registry-truth). */
    public static boolean isGrindable(MinecraftServer server, Item item) {
        Identifier id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
        for (MeteorMaterialEntry e : all(server)) {
            if (e.enabled() && e.item().equals(id)) {
                return true;
            }
        }
        return false;
    }

    // --- resolution ---------------------------------------------------------

    /** The current tuning from Core config (read live so {@code /neroland config reload} takes effect). */
    public static Tuning tuning() {
        return new Tuning(
                CoreConfig.METEOR_TIER_WEIGHT_COMMON.get(),
                CoreConfig.METEOR_TIER_WEIGHT_UNCOMMON.get(),
                CoreConfig.METEOR_TIER_WEIGHT_RARE.get(),
                CoreConfig.METEOR_PLANET_BIAS.get(),
                CoreConfig.METEOR_EXOTIC_CHANCE.get());
    }

    /** Build the per-roll context for {@code player}: their satisfied gates and current planet. */
    public static Context contextFor(ServerPlayer player) {
        Identifier planet = MeteorPlanets.currentPlanet(player);
        return new Context(
                planet == null ? null : planet.toString(),
                gate -> ProgressionGates.isOpen(player, Identifier.parse(gate)));
    }

    /**
     * Resolve a grind's full output for {@code player}: one primary dust, plus — with probability
     * {@code exoticChance} — one exotic bonus item. Returns the produced item ids (possibly empty if no
     * material is eligible). This is the algorithm in {@code METEOR-MATERIAL-REGISTRY.md}.
     */
    public static List<Item> resolve(ServerPlayer player, RandomSource random) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return List.of();
        }
        Map<Identifier, MeteorMaterialEntry> aggregate = MeteorMaterialRegistry.forServer(server);
        List<Candidate> candidates = new ArrayList<>(aggregate.size());
        for (MeteorMaterialEntry e : aggregate.values()) {
            candidates.add(e.toCandidate());
        }
        Context ctx = contextFor(player);
        Tuning tuning = tuning();

        List<Item> output = new ArrayList<>(2);
        Candidate primary = MeteorResolution.pickPrimary(candidates, ctx, tuning, random.nextDouble());
        addItem(output, aggregate, primary);

        if (MeteorResolution.rollExotic(tuning, random.nextDouble())) {
            Candidate exotic = MeteorResolution.pickExotic(candidates, ctx, random.nextDouble());
            addItem(output, aggregate, exotic);
        }
        return output;
    }

    private static void addItem(List<Item> out, Map<Identifier, MeteorMaterialEntry> aggregate,
            @Nullable Candidate chosen) {
        if (chosen == null) {
            return;
        }
        MeteorMaterialEntry entry = aggregate.get(Identifier.parse(chosen.id()));
        if (entry == null) {
            return;
        }
        Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(entry.item());
        if (item != null && item != net.minecraft.world.item.Items.AIR) {
            out.add(item);
        }
    }
}
