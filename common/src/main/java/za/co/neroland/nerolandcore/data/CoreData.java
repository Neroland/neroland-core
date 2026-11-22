package za.co.neroland.nerolandcore.data;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

import za.co.neroland.nerolandcore.link.LinkAlert;

import za.co.neroland.nerolandcore.economy.CurrencyApi;
import za.co.neroland.nerolandcore.link.LinkAlerts;
import za.co.neroland.nerolandcore.progression.ProgressionState;
import za.co.neroland.nerolandcore.progression.MaterialMilestoneState;
import za.co.neroland.nerolandcore.reputation.ReputationApi;

/**
 * Registers Neroland Core's own systems with the shared {@link PlayerDataErasure}
 * hook, so a single erase request purges a player's progression gates, currency,
 * reputation, activity record and NeroLink alerts together. Downstream mods register
 * their own erasers the same way. Called once from
 * {@link za.co.neroland.nerolandcore.NerolandCoreCommon#init()}.
 *
 * <p>Each of the four {@code SavedData}-backed stores is erased through its own
 * {@code eraseFor(server, uuid)} entry point rather than a bare {@code forget(uuid)}, because those
 * entry points also push the anonymised state to the {@link SavedDataRecovery} backup file in the
 * same request — the backup is a second copy of the same rows and erasure has to reach it.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class CoreData {

    /** Guards {@link #registerProviderErasers()} so the two provider erasers register exactly once. */
    private static final AtomicBoolean PROVIDER_ERASERS_REGISTERED = new AtomicBoolean();

    private CoreData() {
    }

    public static void init() {
        PlayerDataErasure.register(ProgressionState::eraseFor);
        PlayerDataErasure.register(MaterialMilestoneState::eraseFor);
        registerProviderErasers();
        PlayerDataErasure.register(PlayerActivity::eraseFor);
        PlayerDataErasure.register(LinkAlerts::eraseFor);
        PlayerDataExport.register("nerolandcore", CoreData::export);
    }

    /**
     * Core's own section of a player's data export: their per-player gates and material milestones,
     * when they were last seen (the retention clock) and their NeroLink alerts. Currency and reputation
     * are owned by their provider mods, which export them in their own sections.
     */
    static JsonElement export(MinecraftServer server, UUID player) {
        JsonObject out = new JsonObject();
        JsonArray gates = new JsonArray();
        for (String gate : ProgressionState.get(server).playerGates(player)) {
            gates.add(gate);
        }
        out.add("gates", gates);
        JsonObject milestones = new JsonObject();
        for (Map.Entry<Identifier, Set<Identifier>> entry
                : MaterialMilestoneState.get(server).playerValues(player).entrySet()) {
            JsonArray materials = new JsonArray();
            for (Identifier material : entry.getValue()) {
                materials.add(material.toString());
            }
            milestones.add(entry.getKey().toString(), materials);
        }
        out.add("materialMilestones", milestones);
        Long seen = PlayerActivity.get(server).lastSeen(player);
        if (seen != null) {
            out.addProperty("lastSeen", Instant.ofEpochMilli(seen).toString());
        }
        JsonArray alerts = new JsonArray();
        for (LinkAlert alert : LinkAlerts.get(server).list(player)) {
            JsonObject row = new JsonObject();
            row.addProperty("module", alert.moduleId());
            row.addProperty("severity", alert.severity().name().toLowerCase(java.util.Locale.ROOT));
            row.addProperty("text", alert.text());
            row.addProperty("createdAt", Instant.ofEpochMilli(alert.createdAt()).toString());
            row.addProperty("acknowledged", alert.acked());
            alerts.add(row);
        }
        out.add("linkAlerts", alerts);
        if (gates.isEmpty() && milestones.size() == 0 && seen == null && alerts.isEmpty()) {
            return null;
        }
        return out;
    }

    /**
     * Registers the currency and reputation erasers, at most once per process.
     *
     * <p>Split out and made idempotent so {@link ErasureConformance} can guarantee they are present
     * before it checks that {@code forgetPlayer} was actually reached — a plain-JVM downstream test
     * never calls {@link #init()} (the four store erasers need a live server), so without this the
     * conformance run would report a missing invocation that is really just missing wiring. Both
     * erasers ignore the {@code server} argument, so they are safe with a {@code null} server.
     */
    static void registerProviderErasers() {
        if (!PROVIDER_ERASERS_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        PlayerDataErasure.register((server, uuid) -> CurrencyApi.provider().forgetPlayer(uuid));
        PlayerDataErasure.register((server, uuid) -> ReputationApi.provider().forgetPlayer(uuid));
    }
}
