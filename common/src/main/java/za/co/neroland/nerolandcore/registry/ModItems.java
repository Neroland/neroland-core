package za.co.neroland.nerolandcore.registry;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.registry.RegistrationProvider.RegistryEntry;

/**
 * Neroland Core's shared material items + block items, registered cross-loader
 * through {@link RegistrationProvider}. The four backbone materials and their
 * derived forms live here — the single source of truth other mods reference by
 * tag (see the {@code c:} and {@code neroland:} tag set).
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class ModItems {

    public static final RegistrationProvider<Item> ITEMS =
            RegistrationProvider.get(Registries.ITEM, NerolandCoreCommon.MOD_ID);

    // --- Block items --------------------------------------------------------
    public static final RegistryEntry<BlockItem> NERO_ALLOY_BLOCK_ITEM = blockItem("nero_alloy_block", ModBlocks.NERO_ALLOY_BLOCK);
    public static final RegistryEntry<BlockItem> STARSTEEL_BLOCK_ITEM = blockItem("starsteel_block", ModBlocks.STARSTEEL_BLOCK);
    public static final RegistryEntry<BlockItem> VOID_CRYSTAL_BLOCK_ITEM = blockItem("void_crystal_block", ModBlocks.VOID_CRYSTAL_BLOCK);
    public static final RegistryEntry<BlockItem> PLASMA_GLASS_BLOCK_ITEM = blockItem("plasma_glass_block", ModBlocks.PLASMA_GLASS_BLOCK);
    public static final RegistryEntry<BlockItem> PLASMA_GLASS_PANE_ITEM = blockItem("plasma_glass_pane", ModBlocks.PLASMA_GLASS_PANE);

    // --- Nero Alloy (industrial metal) -------------------------------------
    public static final RegistryEntry<Item> NERO_ALLOY_NUGGET = item("nero_alloy_nugget");
    public static final RegistryEntry<Item> NERO_ALLOY_INGOT = item("nero_alloy_ingot");
    public static final RegistryEntry<Item> NERO_ALLOY_DUST = item("nero_alloy_dust");
    public static final RegistryEntry<Item> NERO_ALLOY_PLATE = item("nero_alloy_plate");

    // --- Starsteel (space-era metal) ---------------------------------------
    public static final RegistryEntry<Item> STARSTEEL_NUGGET = item("starsteel_nugget");
    public static final RegistryEntry<Item> STARSTEEL_INGOT = item("starsteel_ingot");
    public static final RegistryEntry<Item> STARSTEEL_DUST = item("starsteel_dust");
    public static final RegistryEntry<Item> STARSTEEL_PLATE = item("starsteel_plate");

    // --- Void Crystal (late/alien gem) -------------------------------------
    public static final RegistryEntry<Item> VOID_CRYSTAL_SHARD = item("void_crystal_shard");
    public static final RegistryEntry<Item> VOID_CRYSTAL = item("void_crystal");
    public static final RegistryEntry<Item> VOID_CRYSTAL_DUST = item("void_crystal_dust");

    // --- Plasma Glass (ingredient form; block + pane registered in ModBlocks) ----
    public static final RegistryEntry<Item> PLASMA_GLASS = item("plasma_glass");

    private static RegistryEntry<Item> item(String name) {
        return ITEMS.register(name, key -> new Item(new Item.Properties().setId(key)));
    }

    private static RegistryEntry<BlockItem> blockItem(String name, RegistryEntry<? extends Block> block) {
        // 26.x: Item.getDescriptionId() is final and resolves to item.nerolandcore.<id>; BlockItem no
        // longer delegates to the block's key, so each block item needs a mirrored item.nerolandcore.<id>
        // lang entry alongside block.nerolandcore.<id> (see en_us.json).
        return ITEMS.register(name, key -> new BlockItem(block.get(), new Item.Properties().setId(key)));
    }

    /** Every Core item entry, in display order, for the shared {@link CoreCreativeTab}. */
    private static List<RegistryEntry<? extends ItemLike>> creativeOrder() {
        return List.<RegistryEntry<? extends ItemLike>>of(
                // blocks
                NERO_ALLOY_BLOCK_ITEM, STARSTEEL_BLOCK_ITEM, VOID_CRYSTAL_BLOCK_ITEM,
                PLASMA_GLASS_BLOCK_ITEM, PLASMA_GLASS_PANE_ITEM,
                // Nero Alloy
                NERO_ALLOY_NUGGET, NERO_ALLOY_INGOT, NERO_ALLOY_DUST, NERO_ALLOY_PLATE,
                // Starsteel
                STARSTEEL_NUGGET, STARSTEEL_INGOT, STARSTEEL_DUST, STARSTEEL_PLATE,
                // Void Crystal
                VOID_CRYSTAL_SHARD, VOID_CRYSTAL, VOID_CRYSTAL_DUST,
                // Plasma Glass ingredient
                PLASMA_GLASS);
    }

    /**
     * Adds every Core item to the shared Neroland creative tab as <b>lazy</b> suppliers: they resolve at
     * tab-build time (after registration), so this is safe to call during init on the deferred-registration
     * (NeoForge / Forge) loaders. Called from {@link CoreRegistries#init()}.
     */
    public static void addToCreativeTab() {
        creativeOrder().forEach(entry -> CoreCreativeTab.add(entry::get));
    }

    private ModItems() {
    }

    /** Force class-load so the static item registrations run. */
    public static void init() {
    }
}
