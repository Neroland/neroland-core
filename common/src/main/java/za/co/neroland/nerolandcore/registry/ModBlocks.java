package za.co.neroland.nerolandcore.registry;

import java.util.function.UnaryOperator;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.registry.RegistrationProvider.RegistryEntry;

/**
 * Neroland Core's shared material blocks, registered cross-loader through
 * {@link RegistrationProvider}. Properties are configured via a
 * {@link UnaryOperator} so per-block variations stay inline.
 *
 * <p>Core ships only the four backbone materials' storage forms (plus Plasma
 * Glass's transparent block + pane). Ores live in the mods that introduce them
 * (e.g. Nerospace planet ores smelt into these). This is the single source of
 * truth for Nero material blocks — referenced everywhere by tag, never by class.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class ModBlocks {

    public static final RegistrationProvider<Block> BLOCKS =
            RegistrationProvider.get(Registries.BLOCK, NerolandCoreCommon.MOD_ID);

    // --- Metal storage blocks ----------------------------------------------
    public static final RegistryEntry<Block> NERO_ALLOY_BLOCK = block("nero_alloy_block",
            p -> p.mapColor(MapColor.COLOR_CYAN).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));
    public static final RegistryEntry<Block> STARSTEEL_BLOCK = block("starsteel_block",
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).strength(6.0F, 7.0F).requiresCorrectToolForDrops().sound(SoundType.METAL));

    // --- Crystal storage block (faint alien glow) --------------------------
    public static final RegistryEntry<Block> VOID_CRYSTAL_BLOCK = block("void_crystal_block",
            p -> p.mapColor(MapColor.COLOR_PURPLE).strength(5.0F, 6.0F).requiresCorrectToolForDrops()
                    .lightLevel(s -> 6).sound(SoundType.AMETHYST));

    // --- Plasma Glass (transparent block + pane) ---------------------------
    public static final RegistryEntry<Block> PLASMA_GLASS_BLOCK = block("plasma_glass_block",
            p -> p.mapColor(MapColor.COLOR_CYAN).strength(0.3F).sound(SoundType.GLASS).lightLevel(s -> 4).noOcclusion());
    public static final RegistryEntry<IronBarsBlock> PLASMA_GLASS_PANE = BLOCKS.register("plasma_glass_pane",
            key -> new IronBarsBlock(BlockBehaviour.Properties.of()
                    .setId(key).mapColor(MapColor.COLOR_CYAN).strength(0.3F)
                    .sound(SoundType.GLASS).lightLevel(s -> 4).noOcclusion()));

    private static RegistryEntry<Block> block(String name, UnaryOperator<BlockBehaviour.Properties> props) {
        return BLOCKS.register(name, key -> new Block(props.apply(BlockBehaviour.Properties.of().setId(key))));
    }

    private ModBlocks() {
    }

    /** Force class-load so the static block registrations run. */
    public static void init() {
    }
}
