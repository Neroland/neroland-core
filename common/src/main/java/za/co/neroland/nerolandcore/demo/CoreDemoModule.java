package za.co.neroland.nerolandcore.demo;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.registry.ModBlocks;

/**
 * Core's own small exhibit (Core 1.20.0): the four shared material blocks and the five shared storage
 * blocks, each on a plinth with its name above it. Registered by Core in common init; it is always first.
 */
public final class CoreDemoModule implements DemoModule {

    private static final DemoFootprint FOOTPRINT = DemoFootprint.of(13, 11);

    @Override
    public String modId() {
        return NerolandCoreCommon.MOD_ID;
    }

    @Override
    public Component displayName() {
        return Component.translatable("demo.nerolandcore.core.name");
    }

    @Override
    public Identifier icon() {
        return Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "textures/block/nero_alloy_block.png");
    }

    @Override
    public int sortOrder() {
        return 0;
    }

    @Override
    public DemoFootprint footprint() {
        return FOOTPRINT;
    }

    @Override
    public List<Component> guide() {
        return List.of(Component.translatable("demo.nerolandcore.core.guide.1"),
                Component.translatable("demo.nerolandcore.core.guide.2"));
    }

    @Override
    public void build(DemoBuildContext ctx) {
        // North row: the shared materials. South row: the shared storage blocks.
        List<Block> materials = List.of(ModBlocks.NERO_ALLOY_BLOCK.get(), ModBlocks.STARSTEEL_BLOCK.get(),
                ModBlocks.VOID_CRYSTAL_BLOCK.get(), ModBlocks.PLASMA_GLASS_BLOCK.get());
        List<Block> storage = List.of(ModBlocks.BATTERY.get(), ModBlocks.FLUID_TANK.get(), ModBlocks.GAS_TANK.get(),
                ModBlocks.ITEM_STORE.get(), ModBlocks.TRASH_CAN.get());
        row(ctx, materials, 3, 3, 2);
        row(ctx, storage, 2, 7, 2);
        ctx.label(ctx.at(6, 4, 1), Component.translatable("demo.nerolandcore.core.label.materials"));
        ctx.label(ctx.at(6, 4, 9), Component.translatable("demo.nerolandcore.core.label.storage"));
    }

    private static void row(DemoBuildContext ctx, List<Block> blocks, int startX, int z, int step) {
        for (int i = 0; i < blocks.size(); i++) {
            int x = startX + i * step;
            ctx.set(ctx.at(x, 1, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            ctx.set(ctx.at(x, 2, z), blocks.get(i).defaultBlockState());
            ctx.label(ctx.at(x, 3, z), blocks.get(i).getName());
        }
    }
}
