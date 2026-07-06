package za.co.neroland.nerolandcore.link;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.data.PlayerDataErasure;

/**
 * The per-player alert store behind NeroLink's {@code core/alerts} section. Modules
 * {@linkplain #raise(MinecraftServer, UUID, LinkAlert) raise} alerts; the app lists,
 * {@linkplain #ack(MinecraftServer, UUID, String) acks} and
 * {@linkplain #snooze(MinecraftServer, UUID, String, long) snoozes} them via the
 * {@code core/ack_alert} action. Persistent, server-authoritative state — unlike the
 * transient snapshots served by {@link LinkSnapshotProvider}s — so it survives restarts.
 *
 * <p><b>Persistence.</b> Stored as vanilla {@link SavedData} on the overworld, using the
 * exact {@link SavedDataType} + Codec pattern Core already uses for
 * {@code ProgressionState} and {@code PlayerActivity}. This keeps alert persistence in
 * {@code common} (no loader-specific storage), so a Core-only server persists alerts
 * without the bridge providing any storage of its own.
 *
 * <p><b>Erasure & privacy (POPIA/GDPR).</b> Registered with the shared
 * {@link PlayerDataErasure} hook (from {@code CoreData.init()}) via {@link #forget(UUID)}
 * — accessed as {@code LinkAlerts.get(server).forget(uuid)} — so one erasure request
 * purges a player's alerts alongside every other mod's data. Rows are keyed only by the
 * owning player's UUID and hold non-personal gameplay metadata; nothing here is logged
 * at info with player identity.
 *
 * <p>Raising and acking an alert publishes a {@link LinkEvent} on the shared
 * {@link LinkEventBus} (topic {@code alerts}) so the bridge can push it while the app is
 * closed.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class LinkAlerts extends SavedData {

    /** The section/topic name this store feeds ({@code core/alerts}). */
    public static final String SECTION = "alerts";

    public static final Identifier ID = Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "link_alerts");

    public static final SavedDataType<LinkAlerts> TYPE =
            new SavedDataType<>(ID, LinkAlerts::new, codec(), null);

    /** Per-player alerts, keyed by owning UUID, each ordered by insertion (id → alert). */
    private final Map<UUID, Map<String, LinkAlert>> byPlayer = new LinkedHashMap<>();

    public LinkAlerts() {
    }

    /** The one store, on the overworld so it is always loaded. */
    public static LinkAlerts get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    /**
     * Raise (or replace, if the id already exists) an alert for a player and publish an
     * {@code alerts} event. @return the stored alert.
     */
    public LinkAlert raise(MinecraftServer server, UUID player, LinkAlert alert) {
        byPlayer.computeIfAbsent(player, ignored -> new LinkedHashMap<>()).put(alert.id(), alert);
        setDirty();
        publish(player, alert, "raised");
        return alert;
    }

    /** All of a player's alerts, most-recently-created first (a defensive copy). */
    public List<LinkAlert> list(UUID player) {
        Map<String, LinkAlert> alerts = byPlayer.get(player);
        if (alerts == null || alerts.isEmpty()) {
            return List.of();
        }
        List<LinkAlert> out = new ArrayList<>(alerts.values());
        out.sort((a, b) -> Long.compare(b.createdAt(), a.createdAt()));
        return out;
    }

    /**
     * Mark a player's alert acknowledged and publish an {@code alerts} event.
     * @return {@code true} if the alert existed and was updated.
     */
    public boolean ack(MinecraftServer server, UUID player, String alertId) {
        return update(server, player, alertId, LinkAlert::withAcked, "acked");
    }

    /**
     * Snooze a player's alert until {@code untilEpochMillis} and publish an
     * {@code alerts} event. @return {@code true} if the alert existed and was updated.
     */
    public boolean snooze(MinecraftServer server, UUID player, String alertId, long untilEpochMillis) {
        return update(server, player, alertId, a -> a.withSnoozedUntil(untilEpochMillis), "snoozed");
    }

    /** Remove a player's alert. @return {@code true} if it existed. */
    public boolean dismiss(UUID player, String alertId) {
        Map<String, LinkAlert> alerts = byPlayer.get(player);
        if (alerts != null && alerts.remove(alertId) != null) {
            if (alerts.isEmpty()) {
                byPlayer.remove(player);
            }
            setDirty();
            return true;
        }
        return false;
    }

    /**
     * POPIA/GDPR erasure: drop every alert stored for a player. Wired into the shared
     * erasure hook from {@code CoreData.init()}.
     */
    public void forget(UUID player) {
        if (byPlayer.remove(player) != null) {
            setDirty();
        }
    }

    private boolean update(MinecraftServer server, UUID player, String alertId,
                           java.util.function.UnaryOperator<LinkAlert> op, String verb) {
        Map<String, LinkAlert> alerts = byPlayer.get(player);
        if (alerts == null) {
            return false;
        }
        LinkAlert current = alerts.get(alertId);
        if (current == null) {
            return false;
        }
        LinkAlert updated = op.apply(current);
        alerts.put(alertId, updated);
        setDirty();
        publish(player, updated, verb);
        return true;
    }

    private void publish(UUID player, LinkAlert alert, String verb) {
        JsonObject delta = new JsonObject();
        delta.addProperty("id", alert.id());
        delta.addProperty("module", alert.moduleId());
        delta.addProperty("severity", alert.severity().name());
        delta.addProperty("text", alert.text());
        delta.addProperty("at", alert.createdAt());
        delta.addProperty("acked", alert.acked());
        delta.addProperty("event", verb);
        NeroLinkRegistry.eventBus().publish(LinkEvent.forPlayer("core", SECTION, player, delta));
    }

    // --- persistence (same SavedDataType + Codec pattern as ProgressionState) --------

    private record Row(String player, List<AlertRow> alerts) {
        static final Codec<Row> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("player").forGetter(Row::player),
                AlertRow.CODEC.listOf().fieldOf("alerts").forGetter(Row::alerts)
        ).apply(inst, Row::new));
    }

    private record AlertRow(String id, String moduleId, String severity, String text,
                            long createdAt, boolean acked, long snoozedUntil) {
        static final Codec<AlertRow> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("id").forGetter(AlertRow::id),
                Codec.STRING.fieldOf("module").forGetter(AlertRow::moduleId),
                Codec.STRING.fieldOf("severity").forGetter(AlertRow::severity),
                Codec.STRING.fieldOf("text").forGetter(AlertRow::text),
                Codec.LONG.fieldOf("created_at").forGetter(AlertRow::createdAt),
                Codec.BOOL.optionalFieldOf("acked", false).forGetter(AlertRow::acked),
                Codec.LONG.optionalFieldOf("snoozed_until", 0L).forGetter(AlertRow::snoozedUntil)
        ).apply(inst, AlertRow::new));

        static AlertRow of(LinkAlert a) {
            return new AlertRow(a.id(), a.moduleId(), a.severity().name(), a.text(),
                    a.createdAt(), a.acked(), a.snoozedUntil());
        }

        LinkAlert toAlert() {
            LinkAlert.Severity sev;
            try {
                sev = LinkAlert.Severity.valueOf(severity);
            } catch (IllegalArgumentException ignored) {
                sev = LinkAlert.Severity.INFO;
            }
            return new LinkAlert(id, moduleId, sev, text, createdAt, acked, snoozedUntil);
        }
    }

    private static Codec<LinkAlerts> codec() {
        return RecordCodecBuilder.create(inst -> inst.group(
                Row.CODEC.listOf().optionalFieldOf("players", List.of()).forGetter(LinkAlerts::rows)
        ).apply(inst, LinkAlerts::fromRows));
    }

    private List<Row> rows() {
        List<Row> out = new ArrayList<>();
        byPlayer.forEach((uuid, alerts) -> {
            List<AlertRow> rows = new ArrayList<>();
            alerts.values().forEach(a -> rows.add(AlertRow.of(a)));
            out.add(new Row(uuid.toString(), rows));
        });
        return out;
    }

    private static LinkAlerts fromRows(List<Row> rows) {
        LinkAlerts store = new LinkAlerts();
        for (Row row : rows) {
            UUID uuid;
            try {
                uuid = UUID.fromString(row.player());
            } catch (IllegalArgumentException ignored) {
                continue; // skip malformed UUID rows
            }
            Map<String, LinkAlert> alerts = new LinkedHashMap<>();
            for (AlertRow alertRow : row.alerts()) {
                alerts.put(alertRow.id(), alertRow.toAlert());
            }
            store.byPlayer.put(uuid, alerts);
        }
        return store;
    }
}
