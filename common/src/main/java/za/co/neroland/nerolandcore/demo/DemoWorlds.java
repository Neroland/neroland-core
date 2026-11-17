package za.co.neroland.nerolandcore.demo;

//? if >=26.3 {
/*import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.registry.ModBlocks;

// Server side of the Nero Mods Demo (Core 1.20.0, Minecraft 26.3+). On the first start of a demo world it
// builds the hub, the path and one plot per selected module, in plot order, then records that the build
// finished. Singleplayer only: a dedicated server never builds. Logs carry counts and module ids only.
public final class DemoWorlds {

    // The name given to every demo world (the folder name is made unique by vanilla).
    public static final String LEVEL_NAME = "Nero Mods Demo";

    // The demo world's seed. The overworld is void, so this fixes the terrain of every other dimension.
    public static final String SEED = "4206461927";

    private DemoWorlds() {
    }

    // Called by each loader once the server has started (before any player joins).
    public static void onServerStarted(MinecraftServer server) {
        if (server.isDedicatedServer()) {
            return;
        }
        try {
            DemoPending.Request request = DemoPending.take(server.getWorldData().getLevelName());
            if (request == null && !markerOnDisk(server)) {
                return; // not a demo world: leave it completely untouched
            }
            DemoWorldState state = DemoWorldState.get(server);
            if (request != null) {
                state.begin(DemoRegistry.resolve(request.moduleIds()).stream().map(DemoModule::modId).toList());
            }
            if (!state.isDemo() || state.isBuilt()) {
                return;
            }
            int built = build(server, state.modules());
            state.markBuilt();
            DemoWorldState.backupNow(server, state);
            NerolandCoreCommon.LOGGER.info("[Neroland Core] Demo world built: {} exhibit(s).", built);
        } catch (RuntimeException e) {
            NerolandCoreCommon.LOGGER.error("[Neroland Core] The demo world could not be built.", e);
        }
    }

    // Whether this world already carries a demo marker (primary file or its recovery backup).
    private static boolean markerOnDisk(MinecraftServer server) {
        Path root = server.getWorldPath(LevelResource.ROOT);
        Path storage = DimensionType.getStorageFolder(Level.OVERWORLD, root);
        String stem = DemoWorldState.ID.getNamespace() + "/" + DemoWorldState.ID.getPath() + ".dat";
        String backup = DemoWorldState.ID.toString().replace(':', '_').replace('/', '_') + "_backup.dat";
        return Files.isRegularFile(storage.resolve("data").resolve(stem))
                || Files.isRegularFile(root.resolve("dimensions").resolve("minecraft").resolve("overworld")
                        .resolve("data").resolve(stem))
                || Files.isRegularFile(storage.resolve("data").resolve(backup));
    }

    // Builds the hub and every plot. Returns how many modules built without an error.
    private static int build(MinecraftServer server, List<String> moduleIds) {
        ServerLevel level = server.overworld();
        List<DemoModule> modules = DemoRegistry.resolve(moduleIds);
        List<DemoLayout.Plot> plots = DemoRegistry.layout(modules);
        BlockPos spawn = new BlockPos(0, DemoLayout.FLOOR_Y + 1, 0);

        buildHub(level, plots.size());
        buildPath(level, plots);
        level.setRespawnData(LevelData.RespawnData.of(Level.OVERWORLD, spawn, -90.0F, 0.0F));

        int ok = 0;
        for (int i = 0; i < plots.size(); i++) {
            DemoLayout.Plot plot = plots.get(i);
            DemoModule module = modules.get(i);
            buildPlotFloor(level, plot);
            DemoBuildContext ctx = new DemoBuildContext(level,
                    new BlockPos(plot.minX(), DemoLayout.FLOOR_Y, plot.minZ()), module.footprint(), spawn);
            List<Component> lines = new java.util.ArrayList<>();
            lines.add(module.displayName().copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            List<Component> guide = module.guide();
            if (guide != null) {
                lines.addAll(guide);
            }
            ctx.guide(new BlockPos(plot.minX() - 3, DemoLayout.FLOOR_Y + 4, 0), lines);
            try {
                module.build(ctx);
                ok++;
            } catch (RuntimeException e) {
                NerolandCoreCommon.LOGGER.error("[Neroland Core] Demo module '{}' failed to build.", module.modId(), e);
            }
        }

        // A showroom: a fixed bright sky. The game rules (no daylight or weather cycle, no natural spawns)
        // were set when the world was created.
        var source = server.createCommandSourceStack().withSuppressedOutput();
        server.getCommands().performPrefixedCommand(source, "time set noon");
        server.getCommands().performPrefixedCommand(source, "weather clear");
        return ok;
    }

    private static void buildHub(ServerLevel level, int exhibits) {
        int r = DemoLayout.HUB_RADIUS;
        int y = DemoLayout.FLOOR_Y;
        BlockState floor = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState rim = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                boolean edge = Math.abs(x) == r || Math.abs(z) == r;
                level.setBlockAndUpdate(new BlockPos(x, y, z), edge ? rim : floor);
            }
        }
        // Corner posts of Core's own alloy, capped with a light.
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                BlockPos base = new BlockPos(sx * r, y + 1, sz * r);
                level.setBlockAndUpdate(base, ModBlocks.NERO_ALLOY_BLOCK.get().defaultBlockState());
                level.setBlockAndUpdate(base.above(), ModBlocks.NERO_ALLOY_BLOCK.get().defaultBlockState());
                level.setBlockAndUpdate(base.above(2), Blocks.SEA_LANTERN.defaultBlockState());
            }
        }
        DemoBuildContext.label(level, new net.minecraft.world.phys.Vec3(4.5D, y + 3.0D, 0.5D),
                Component.translatable("demo.nerolandcore.hub.title").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        DemoBuildContext.label(level, new net.minecraft.world.phys.Vec3(4.5D, y + 2.7D, 0.5D),
                Component.translatable("demo.nerolandcore.hub.walk"));
        DemoBuildContext.label(level, new net.minecraft.world.phys.Vec3(4.5D, y + 2.4D, 0.5D),
                Component.translatable("demo.nerolandcore.hub.count", exhibits));
    }

    private static void buildPath(ServerLevel level, List<DemoLayout.Plot> plots) {
        int end = DemoLayout.pathEndX(plots);
        BlockState path = Blocks.POLISHED_ANDESITE.defaultBlockState();
        for (int x = DemoLayout.HUB_RADIUS + 1; x <= end; x++) {
            for (int z = -DemoLayout.PATH_HALF_WIDTH; z <= DemoLayout.PATH_HALF_WIDTH; z++) {
                level.setBlockAndUpdate(new BlockPos(x, DemoLayout.FLOOR_Y, z), path);
            }
        }
    }

    private static void buildPlotFloor(ServerLevel level, DemoLayout.Plot plot) {
        BlockState floor = Blocks.SMOOTH_STONE.defaultBlockState();
        BlockState rim = Blocks.STONE_BRICKS.defaultBlockState();
        for (int x = plot.minX(); x <= plot.maxX(); x++) {
            for (int z = plot.minZ(); z <= plot.maxZ(); z++) {
                boolean edge = x == plot.minX() || x == plot.maxX() || z == plot.minZ() || z == plot.maxZ();
                level.setBlockAndUpdate(new BlockPos(x, DemoLayout.FLOOR_Y, z), edge ? rim : floor);
            }
        }
    }
}
*///?} else {
// The Nero Mods Demo needs Minecraft 26.3 or newer; on older lines this class is an empty placeholder.
public final class DemoWorlds {

    private DemoWorlds() {
    }
}
//?}
