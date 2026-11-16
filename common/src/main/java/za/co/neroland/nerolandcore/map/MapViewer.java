package za.co.neroland.nerolandcore.map;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Who a map query is for (Core 1.19.0): the viewer's UUID, the server, and — while the viewer is online —
 * the dimension and block position captured when the viewer was made. It never holds the player object.
 *
 * <p>{@link #within} is the <b>one</b> proximity test every provider uses, so the rule "same dimension,
 * horizontal distance, nothing while offline" is written once.
 *
 * <p><b>Privacy.</b> {@link #toString()} prints only {@code MapViewer[online]} or {@code MapViewer[offline]}:
 * no UUID, name or position, so logging a viewer by accident leaks nothing. Equality is identity.
 */
public final class MapViewer {

    private final UUID id;
    private final @Nullable MinecraftServer server;
    private final @Nullable ResourceKey<Level> dimension;
    private final @Nullable BlockPos position;

    private MapViewer(UUID id, @Nullable MinecraftServer server, @Nullable ResourceKey<Level> dimension,
            @Nullable BlockPos position) {
        if (id == null) {
            throw new IllegalArgumentException("A map viewer needs an id");
        }
        this.id = id;
        this.server = server;
        if (dimension != null && position != null) {
            this.dimension = dimension;
            this.position = position.immutable();
        } else {
            this.dimension = null;
            this.position = null;
        }
    }

    /** An online viewer: captures the player's id, server, dimension and block position now. */
    public static MapViewer of(ServerPlayer player) {
        if (player == null) {
            throw new IllegalArgumentException("A map viewer needs a player");
        }
        return new MapViewer(player.getUUID(), player.level().getServer(), player.level().dimension(),
                player.blockPosition());
    }

    /** An offline viewer (for example a companion-app request while the player is not in game). */
    public static MapViewer offline(MinecraftServer server, UUID id) {
        if (server == null) {
            throw new IllegalArgumentException("An offline map viewer needs the server");
        }
        return new MapViewer(id, server, null, null);
    }

    /**
     * Test support: a viewer with any combination of server, dimension and position, so pure-store
     * providers can run {@link MapLayerConformance} in a plain JVM test. The viewer is online only when
     * both {@code dimension} and {@code position} are given. Not for production code.
     */
    @ApiStatus.Internal
    public static MapViewer forTest(UUID id, @Nullable MinecraftServer server, @Nullable ResourceKey<Level> dimension,
            @Nullable BlockPos position) {
        return new MapViewer(id, server, dimension, position);
    }

    /** The viewer's player UUID. Never put it in a marker, label or log line. */
    public UUID id() {
        return id;
    }

    /** The server; non-null for every viewer made outside {@link #forTest}. */
    public @Nullable MinecraftServer server() {
        return server;
    }

    /** {@code true} when the viewer was online (dimension and position known) when this viewer was made. */
    public boolean online() {
        return position != null;
    }

    /** The viewer's dimension; empty when offline. */
    public Optional<ResourceKey<Level>> dimension() {
        return Optional.ofNullable(dimension);
    }

    /** The viewer's block position; empty when offline. */
    public Optional<BlockPos> position() {
        return Optional.ofNullable(position);
    }

    /**
     * The one proximity test: {@code false} when the viewer is offline or in another dimension; otherwise
     * whether the column {@code (x, z)} is within {@code radius} blocks horizontally (Euclidean) of the
     * viewer's server-side position.
     */
    public boolean within(ResourceKey<Level> inDimension, int x, int z, int radius) {
        if (position == null || inDimension == null || radius < 0 || !inDimension.equals(dimension)) {
            return false;
        }
        long dx = (long) x - position.getX();
        long dz = (long) z - position.getZ();
        long r = radius;
        return dx * dx + dz * dz <= r * r;
    }

    /**
     * The area form of {@link #within(ResourceKey, int, int, int)}: {@code true} when the point of
     * {@code box} nearest the viewer is within {@code radius}. Used for areas on {@link MapScope#PROXIMITY}
     * layers, with the area's {@link MapArea#bounds()}.
     */
    public boolean within(ResourceKey<Level> inDimension, MapBounds box, int radius) {
        if (position == null || box == null) {
            return false;
        }
        return within(inDimension, box.nearestX(position.getX()), box.nearestZ(position.getZ()), radius);
    }

    @Override
    public String toString() {
        return online() ? "MapViewer[online]" : "MapViewer[offline]";
    }
}
