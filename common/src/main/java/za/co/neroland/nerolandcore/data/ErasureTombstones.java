package za.co.neroland.nerolandcore.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.config.CoreConfig;

/**
 * The shared <b>erasure tombstone</b> store (Core 1.18.0): the minimal record that a player's data was
 * erased, so an entity that was in an unloaded chunk at erasure time can be released when it next loads.
 *
 * <p><b>Why it exists.</b> Vanilla keeps an owner UUID on the entity itself (tamed animals, owned drones,
 * companions). An erasure request cannot reach an entity whose chunk is not loaded, so the next time that
 * entity loads it still names the erased player. Each mod's load-time check asks this store instead of
 * keeping its own copy, so the ecosystem has one salt, one retention policy and one place an operator can
 * see. Erasure and the {@code dataRetentionDays} sweep both write here (through
 * {@link PlayerDataErasure#eraseAndReport}).
 *
 * <p><b>What it holds</b> (one per world, on the overworld, through {@link SavedDataRecovery}):
 * <ul>
 *   <li><b>Owner tombstones</b> — a 128-bit truncation of SHA-256 over a random per-world salt and the
 *       erased player's UUID, with the time it was written. Core writes them itself, before any mod's
 *       eraser runs; mods never see the salt. Never the UUID, never a name. Kept for
 *       {@link #ownerLifetimeDays(int) max(dataRetentionDays, 365)} days; the salt is deleted with the
 *       last one.</li>
 *   <li><b>Entity tombstones</b> — the random UUID of a game entity a mod's eraser could not free because
 *       it was unloaded ({@link #markEntity}). Names no player. Kept until the mod reports the entity
 *       freed ({@link #entityFreed}); they do not expire, because an expired marker would let the entity
 *       load and write the erased owner back.</li>
 * </ul>
 *
 * <p><b>How a mod uses it</b> when one of its owned entities loads:
 * <pre>{@code
 * if (myIndex.contains(owner, entity)) return;                 // known and live: nothing to do
 * ErasureTombstones tombstones = ErasureTombstones.get(server);
 * if (tombstones.shouldRelease(owner, entity)) {
 *     releaseSilently(entity);                                 // clear the owner tag, recall, ...
 *     tombstones.entityFreed(entity);
 * } else {
 *     myIndex.add(owner, entity);                              // an index gap, not an erasure
 * }
 * }</pre>
 * In its {@link PlayerDataEraser}, a mod calls {@link #markEntity} for each owned entity it could not
 * free. When it records a <em>new</em> ownership for a player (a fresh taming), it calls
 * {@link #ownerReturned} so the player's old owner tombstone does not release the new pet.
 *
 * <p><b>Privacy (POPIA/GDPR).</b> An owner tombstone is pseudonymised personal data: it cannot be reversed,
 * but someone holding the salt and a list of UUIDs could test each one. It is kept only to finish the
 * erasure the player asked for, there is no listing API, it is never logged (only {@link #count()} is),
 * and a player's own export may say only "yes/no" ({@link #isOwnerErased}). A hard cap of
 * {@value #MAX_TOMBSTONES} per kind bounds the file; reaching it drops the oldest and is logged at WARN
 * with a count, never silently. See {@code PRIVACY.md} and {@code docs/COMPLIANCE.md}.
 *
 * <p>Server thread only, like every other {@link SavedData}.
 */
public final class ErasureTombstones extends SavedData {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "erasure_tombstones");

    public static final SavedDataType<ErasureTombstones> TYPE =
            SavedDataCompat.type(ID, ErasureTombstones::new, codec());

    /** The shortest life an owner tombstone gets, in days; the real lifetime is {@link #ownerLifetimeDays}. */
    public static final int MIN_OWNER_DAYS = 365;

    /** Upper bound on each tombstone set (owner hashes, entity UUIDs). About 2.4 MB of entity UUIDs. */
    public static final int MAX_TOMBSTONES = 65_536;

    private static final long MILLIS_PER_DAY = 86_400_000L;

    /** Wall clock for tombstone stamps; tests swap it. */
    static LongSupplier clock = System::currentTimeMillis;

    private String salt = "";
    private final Map<String, Long> owners = new LinkedHashMap<>();
    private final Map<UUID, Long> entities = new LinkedHashMap<>();
    /** Tombstones dropped at {@link #MAX_TOMBSTONES} and not yet reported. */
    private int droppedAtCap;

    public ErasureTombstones() {
    }

    /** The one store, on the overworld so it is always loaded. */
    public static ErasureTombstones get(MinecraftServer server) {
        ErasureTombstones store = SavedDataRecovery.get(server.overworld(), TYPE, ErasureTombstones::new, ID.toString());
        store.reportDroppedAtCap();
        return store;
    }

    /**
     * Owner-tombstone lifetime for a {@code dataRetentionDays} value: {@code max(retention, 365)}. Never
     * shorter than a year, because a tombstone that expired with a short retention period would defeat
     * itself (an erased pet in a chunk nobody visits for a month).
     */
    public static int ownerLifetimeDays(int retentionDays) {
        return Math.max(retentionDays, MIN_OWNER_DAYS);
    }

    // --- queries --------------------------------------------------------------

    /**
     * Whether this player's data was erased (an owner tombstone exists and has not expired). The UUID is
     * hashed with the world's salt; it is never stored. A player's own data export may report this as a
     * yes/no; it must never be logged.
     */
    public boolean isOwnerErased(UUID owner) {
        if (owner == null || this.salt.isEmpty()) {
            return false;
        }
        Long at = this.owners.get(hash(this.salt, owner));
        return at != null && at >= threshold(ownerLifetimeDays(CoreConfig.DATA_RETENTION_DAYS.get()));
    }

    /** Whether a mod marked this entity as erased-but-unreachable and has not yet freed it. */
    public boolean isEntityMarked(UUID entity) {
        return entity != null && this.entities.containsKey(entity);
    }

    /**
     * The load-time check in one call: {@code true} when this entity was marked, or its owner was erased.
     * Consult it only for an entity the mod's own index does not already hold for that owner.
     *
     * @param owner the owner UUID read from the entity, or {@code null} if it has none
     */
    public boolean shouldRelease(UUID owner, UUID entity) {
        return this.isEntityMarked(entity) || this.isOwnerErased(owner);
    }

    /** How many tombstones (owner + entity) the store holds. For operator diagnostics; never lists them. */
    public int count() {
        return this.owners.size() + this.entities.size();
    }

    // --- writes from mods -----------------------------------------------------

    /** Records an entity an eraser could not free because it was not loaded. Entity UUIDs only. */
    public void markEntity(UUID entity) {
        if (entity == null) {
            return;
        }
        this.droppedAtCap += addBounded(this.entities, entity, clock.getAsLong());
        this.setDirty();
    }

    /** The entity has been released; its tombstone is no longer needed and is deleted. */
    public void entityFreed(UUID entity) {
        if (entity != null && this.entities.remove(entity) != null) {
            this.setDirty();
        }
    }

    /**
     * The player is back and a mod is recording a <em>new</em> ownership for them (a fresh taming, a new
     * drone). Deletes their owner tombstone so the new entity is not released when it next loads. Entity
     * tombstones are kept: entities erased earlier are still released.
     */
    public void ownerReturned(UUID owner) {
        if (owner == null || this.salt.isEmpty()) {
            return;
        }
        if (this.owners.remove(hash(this.salt, owner)) != null) {
            this.dropSaltIfUnused();
            this.setDirty();
        }
    }

    /**
     * Refreshes the last-known-good backup of this store now (hash-compared). Core calls it at the end of
     * every erasure; a mod only needs it after a bulk change outside an erasure request.
     */
    public void backupNow(MinecraftServer server) {
        SavedDataRecovery.backupNow(server.overworld(), TYPE, this, ID.toString());
    }

    // --- Core-internal --------------------------------------------------------

    /** Writes the owner tombstone. Called by {@link PlayerDataErasure} before any mod eraser runs. */
    @org.jetbrains.annotations.ApiStatus.Internal
    public void markOwner(UUID owner) {
        if (owner == null) {
            return;
        }
        if (this.salt.isEmpty()) {
            this.salt = newSalt();
        }
        this.droppedAtCap += addBounded(this.owners, hash(this.salt, owner), clock.getAsLong());
        this.setDirty();
    }

    /**
     * Deletes owner tombstones older than {@code days} days, and the salt with the last one. Entity
     * tombstones never expire (see the class comment). Run at server start and daily by
     * {@link RetentionSchedule}.
     *
     * @return how many owner tombstones were deleted
     */
    @org.jetbrains.annotations.ApiStatus.Internal
    public int pruneExpiredOwners(int days) {
        if (days <= 0) {
            return 0;
        }
        long threshold = threshold(days);
        int before = this.owners.size();
        this.owners.values().removeIf(at -> at < threshold);
        int dropped = before - this.owners.size();
        if (dropped > 0) {
            this.setDirty();
        }
        this.dropSaltIfUnused();
        return dropped;
    }

    /** Logs (count only, at WARN) tombstones dropped at the cap since the last report. */
    void reportDroppedAtCap() {
        if (this.droppedAtCap > 0) {
            int dropped = this.droppedAtCap;
            this.droppedAtCap = 0;
            NerolandCoreCommon.LOGGER.warn(
                    "[Neroland Core] {} erasure tombstone(s) dropped at the {} cap; entities they covered will "
                            + "not be released when they next load.", dropped, MAX_TOMBSTONES);
        }
    }

    /** Tombstones dropped at the cap and not yet reported (tests). */
    int droppedAtCap() {
        return this.droppedAtCap;
    }

    /** Whether a salt is held (tests: it must be deleted with the last owner tombstone). */
    boolean hasSalt() {
        return !this.salt.isEmpty();
    }

    // --- internals ------------------------------------------------------------

    private static long threshold(int days) {
        return clock.getAsLong() - days * MILLIS_PER_DAY;
    }

    /** Salted SHA-256 of the UUID, first 16 bytes, hex. One-way; never logged or exported. */
    static String hash(String salt, UUID owner) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            digest.update(owner.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest.digest(), 0, 16);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is a required JVM algorithm", impossible);
        }
    }

    private static String newSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /** Adds one tombstone, dropping the oldest while the set is over the cap. @return how many were dropped */
    static <T> int addBounded(Map<T, Long> set, T value, long at) {
        set.remove(value);
        set.put(value, at);
        int dropped = 0;
        Iterator<T> oldest = set.keySet().iterator();
        while (set.size() > MAX_TOMBSTONES && oldest.hasNext()) {
            oldest.next();
            oldest.remove();
            dropped++;
        }
        return dropped;
    }

    private void dropSaltIfUnused() {
        if (this.owners.isEmpty() && !this.salt.isEmpty()) {
            this.salt = "";
            this.setDirty();
        }
    }

    // --- persistence ----------------------------------------------------------

    private record Row(String id, long at) {
        static final Codec<Row> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("id").forGetter(Row::id),
                Codec.LONG.optionalFieldOf("at", 0L).forGetter(Row::at)
        ).apply(inst, Row::new));
    }

    private static Codec<ErasureTombstones> codec() {
        return RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.optionalFieldOf("salt", "").forGetter(store -> store.salt),
                Row.CODEC.listOf().optionalFieldOf("owners", List.of()).forGetter(store -> rows(store.owners, id -> id)),
                Row.CODEC.listOf().optionalFieldOf("entities", List.of())
                        .forGetter(store -> rows(store.entities, UUID::toString))
        ).apply(inst, ErasureTombstones::fromRows));
    }

    /** The store codec, for the package tests. */
    static Codec<ErasureTombstones> storeCodec() {
        return codec();
    }

    private static <T> List<Row> rows(Map<T, Long> set, java.util.function.Function<T, String> id) {
        List<Row> out = new ArrayList<>(set.size());
        set.forEach((key, at) -> out.add(new Row(id.apply(key), at)));
        return out;
    }

    private static ErasureTombstones fromRows(String salt, List<Row> owners, List<Row> entities) {
        ErasureTombstones store = new ErasureTombstones();
        long now = clock.getAsLong();
        if (!salt.isEmpty()) {
            store.salt = salt;
            for (Row row : owners) {
                store.droppedAtCap += addBounded(store.owners, row.id(), row.at() > 0L ? row.at() : now);
            }
        }
        for (Row row : entities) {
            try {
                store.droppedAtCap += addBounded(store.entities, UUID.fromString(row.id()), row.at() > 0L ? row.at() : now);
            } catch (IllegalArgumentException ignored) {
                // a malformed row is dropped; the entity is then treated as unknown, never lost
            }
        }
        store.dropSaltIfUnused();
        return store;
    }
}
