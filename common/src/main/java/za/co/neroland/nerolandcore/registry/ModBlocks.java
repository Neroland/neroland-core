package za.co.neroland.nerolandcore.registry;

import java.util.function.UnaryOperator;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.registry.RegistrationProvider.RegistryEntry;
import za.co.neroland.nerolandcore.storage.BatteryBlock;
import za.co.neroland.nerolandcore.storage.CreativeBatteryBlock;
import za.co.neroland.nerolandcore.storage.CreativeFluidTankBlock;
import za.co.neroland.nerolandcore.storage.CreativeGasTankBlock;
import za.co.neroland.nerolandcore.storage.CreativeItemStoreBlock;
import za.co.neroland.nerolandcore.storage.FluidTankBlock;
import za.co.neroland.nerolandcore.storage.GasTankBlock;
import za.co.neroland.nerolandcore.storage.ItemStoreBlock;
import za.co.neroland.nerolandcore.storage.TrashCanBlock;

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

    // --- Hidden Sentry diagnostic block (give-only: /give @s nerolandcore:sentry_test) ---
    public static final RegistryEntry<za.co.neroland.nerolandcore.telemetry.SentryTestBlock> SENTRY_TEST =
            BLOCKS.register("sentry_test",
                    key -> new za.co.neroland.nerolandcore.telemetry.SentryTestBlock(BlockBehaviour.Properties.of()
                            .setId(key).mapColor(MapColor.COLOR_ORANGE).strength(1.0F, 1.0F).sound(SoundType.METAL)));

    // --- Storage blocks (moved from Nerospace; usable by every Nero mod) -----
    // Passive endpoints that buffer a single resource and expose it via Core's
    // capability seams: Battery (energy), Fluid Tank (fluid), Gas Tank (gas),
    // Item Store (vanilla container + chest GUI). No tickers. The Creative
    // variants are unbreakable endless sources/sinks for testing logistics.
    // Block-entity types live in ModBlockEntities; per-loader capability wiring
    // in each loader's CoreCapabilities.
    public static final RegistryEntry<BatteryBlock> BATTERY =
            BLOCKS.register("battery", key -> new BatteryBlock(storageProps(key)));
    public static final RegistryEntry<FluidTankBlock> FLUID_TANK =
            BLOCKS.register("fluid_tank", key -> new FluidTankBlock(storageProps(key)));
    public static final RegistryEntry<GasTankBlock> GAS_TANK =
            BLOCKS.register("gas_tank", key -> new GasTankBlock(storageProps(key)));
    public static final RegistryEntry<ItemStoreBlock> ITEM_STORE =
            BLOCKS.register("item_store", key -> new ItemStoreBlock(storageProps(key)));
    public static final RegistryEntry<CreativeBatteryBlock> CREATIVE_BATTERY =
            BLOCKS.register("creative_battery", key -> new CreativeBatteryBlock(creativeStorageProps(key)));
    public static final RegistryEntry<CreativeFluidTankBlock> CREATIVE_FLUID_TANK =
            BLOCKS.register("creative_fluid_tank", key -> new CreativeFluidTankBlock(creativeStorageProps(key)));
    public static final RegistryEntry<CreativeGasTankBlock> CREATIVE_GAS_TANK =
            BLOCKS.register("creative_gas_tank", key -> new CreativeGasTankBlock(creativeStorageProps(key)));
    public static final RegistryEntry<CreativeItemStoreBlock> CREATIVE_ITEM_STORE =
            BLOCKS.register("creative_item_store", key -> new CreativeItemStoreBlock(creativeStorageProps(key)));
    public static final RegistryEntry<TrashCanBlock> TRASH_CAN =
            BLOCKS.register("trash_can", key -> new TrashCanBlock(BlockBehaviour.Properties.of().setId(key)
                    .mapColor(MapColor.COLOR_GRAY).strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()));

    /** Properties for the regular (mineable) storage blocks. */
    private static BlockBehaviour.Properties storageProps(ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of().setId(key)
                .mapColor(MapColor.METAL).strength(3.0F, 6.0F)
                .requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion();
    }

    /** Properties for the unbreakable Creative storage variants. */
    private static BlockBehaviour.Properties creativeStorageProps(ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of().setId(key)
                .mapColor(MapColor.COLOR_PINK).strength(-1.0F, 3_600_000.0F)
                .sound(SoundType.METAL).noOcclusion();
    }

    private static RegistryEntry<Block> block(String name, UnaryOperator<BlockBehaviour.Properties> props) {
        return BLOCKS.register(name, key -> new Block(props.apply(BlockBehaviour.Properties.of().setId(key))));
    }

    private ModBlocks() {
    }

    /** Force class-load so the static block registrations run. */
    public static void init() {
    }
}
