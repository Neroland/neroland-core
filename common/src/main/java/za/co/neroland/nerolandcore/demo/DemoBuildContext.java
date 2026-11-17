package za.co.neroland.nerolandcore.demo;

import java.util.List;
import java.util.Locale;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * What a {@link DemoModule} gets when Core asks it to build its exhibit (Core 1.20.0): the level, the plot
 * and a few helpers for the things most exhibits need (floating labels, a guide, travel pads). Everything
 * here places blocks or entities only; nothing records the player.
 */
public final class DemoBuildContext {

    /** Most guide lines shown at a plot entrance. */
    public static final int MAX_GUIDE_LINES = 6;

    /** Vertical spacing between stacked label lines, in blocks. */
    public static final double LINE_SPACING = 0.3D;

    /** How far above an armor stand's feet its name is drawn (its height plus vanilla's 0.5 offset). */
    private static final double NAME_OFFSET = 2.475D;

    private final ServerLevel level;
    private final BlockPos origin;
    private final DemoFootprint footprint;
    private final BlockPos hubSpawn;

    public DemoBuildContext(ServerLevel level, BlockPos origin, DemoFootprint footprint, BlockPos hubSpawn) {
        this.level = level;
        this.origin = origin.immutable();
        this.footprint = footprint;
        this.hubSpawn = hubSpawn.immutable();
    }

    /** The demo world's overworld, where the plot is. */
    public ServerLevel level() {
        return this.level;
    }

    public MinecraftServer server() {
        return this.level.getServer();
    }

    /** North-west corner of the plot floor. Build at {@code y + 1} and above. */
    public BlockPos origin() {
        return this.origin;
    }

    public DemoFootprint footprint() {
        return this.footprint;
    }

    /** Where the player stands at the hub (feet position, overworld). */
    public BlockPos hubSpawn() {
        return this.hubSpawn;
    }

    /** {@link #origin()} offset by the given amounts. */
    public BlockPos at(int dx, int dy, int dz) {
        return this.origin.offset(dx, dy, dz);
    }

    /** Sets a block in the plot's level (with neighbour updates). */
    public void set(BlockPos pos, BlockState state) {
        this.level.setBlockAndUpdate(pos, state);
    }

    /** A floating one-line label whose text appears at the bottom centre of {@code pos}, in this plot's level. */
    public void label(BlockPos pos, Component text) {
        label(this.level, Vec3.atBottomCenterOf(pos), text);
    }

    /**
     * Floating lines, top line at {@code top} (block centre) and each next line {@value #LINE_SPACING}
     * blocks lower. At most {@link #MAX_GUIDE_LINES} lines are placed.
     */
    public void guide(ServerLevel in, BlockPos top, List<Component> lines) {
        if (lines == null) {
            return;
        }
        Vec3 at = Vec3.atBottomCenterOf(top);
        int count = Math.min(lines.size(), MAX_GUIDE_LINES);
        for (int i = 0; i < count; i++) {
            Component line = lines.get(i);
            if (line != null) {
                label(in, at.add(0.0D, -i * LINE_SPACING, 0.0D), line);
            }
        }
    }

    /** {@link #guide(ServerLevel, BlockPos, List)} in this plot's level. */
    public void guide(BlockPos top, List<Component> lines) {
        guide(this.level, top, lines);
    }

    /**
     * A walk-on travel pad at {@code plate} in {@code in}: a pressure plate on a command block that moves the
     * player standing on it to {@code destination} in {@code dimension}, facing {@code yaw}/{@code pitch}.
     * Uses only vanilla blocks; the command names no player (it targets whoever stands on the pad). Keep labels at
     * least two blocks above the plate: a label is an armor stand, and one touching the plate holds it down.
     */
    public void travelPad(ServerLevel in, BlockPos plate, ResourceKey<Level> dimension, Vec3 destination,
            float yaw, float pitch) {
        String command = String.format(Locale.ROOT, "execute as @p[distance=..3] in %s run tp @s %.2f %.2f %.2f %.1f %.1f",
                dimension.identifier(), destination.x, destination.y, destination.z, yaw, pitch);
        BlockPos below = plate.below();
        in.setBlockAndUpdate(below, Blocks.COMMAND_BLOCK.defaultBlockState());
        if (in.getBlockEntity(below) instanceof CommandBlockEntity block) {
            block.getCommandBlock().setCommand(command);
            block.getCommandBlock().setTrackOutput(false);
            block.setChanged();
        }
        in.setBlockAndUpdate(plate, Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE.defaultBlockState());
    }

    /** A {@link #travelPad} in this plot's level. */
    public void travelPad(BlockPos plate, ResourceKey<Level> dimension, Vec3 destination, float yaw, float pitch) {
        travelPad(this.level, plate, dimension, destination, yaw, pitch);
    }

    /** A travel pad in {@code in} that returns the player to the hub, facing east. */
    public void returnPad(ServerLevel in, BlockPos plate) {
        travelPad(in, plate, Level.OVERWORLD, Vec3.atBottomCenterOf(this.hubSpawn), -90.0F, 0.0F);
    }

    /**
     * A floating, invulnerable, gravity-free label in {@code in} whose text appears at about {@code at}
     * (an invisible armor stand, placed below that point, carrying the text as its visible name).
     */
    public static void label(ServerLevel in, Vec3 at, Component text) {
        ArmorStand stand = new ArmorStand(in, at.x, at.y - NAME_OFFSET, at.z);
        stand.setCustomName(text);
        stand.setCustomNameVisible(true);
        stand.setInvisible(true);
        stand.setNoGravity(true);
        //? if >=26.3 {
        /*stand.setPermanentlyInvulnerable(true);
        *///?} else {
        stand.setInvulnerable(true);
        //?}
        in.addFreshEntity(stand);
    }
}
