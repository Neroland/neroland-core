package za.co.neroland.nerolandcore.ore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
//? if >=1.21.2 {
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
//?}
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import za.co.neroland.nerolandcore.registry.ModBlocks;
import za.co.neroland.nerolandcore.registry.ModItems;
import za.co.neroland.nerolandcore.registry.PropertiesCompat;
import za.co.neroland.nerolandcore.registry.RegistrationProvider.RegistryEntry;

/**
 * Every Neroland ore, and the raw material each one drops, registered under the {@code nerolandcore}
 * namespace. Core owns the blocks and items only; it generates none of them. The mod each ore comes from
 * (its <em>home mod</em>) keeps placing it in the world: Nerospace on its planets, NeroEvents in meteor
 * craters. NeroPower's Pallurite has no ore block, so only its dust lives here.
 *
 * <p>Before 1.25.0 these ids lived in their home mods. {@link za.co.neroland.nerolandcore.registry.LegacyIds}
 * maps the old ids onto these, so existing worlds keep their ores and stored items. Without its home mod an
 * ore can be turned into a vanilla material in the Ore Reclaimer.
 *
 * <p>Reference these entries (or the {@code c:ores/*}, {@code c:raw_materials/*} and {@code c:gems/*} tags)
 * rather than the registry ids.
 *
 * @since 1.25.0
 */
public final class CoreOres {

    /** Home mod id of every ore and raw material here, keyed by registry path. */
    private static final Map<String, String> HOME_MODS = new LinkedHashMap<>();
    private static final List<RegistryEntry<? extends ItemLike>> CREATIVE = new ArrayList<>();

    // --- Nerospace: Overworld and Greenxertz -----------------------------------------------------------
    public static final RegistryEntry<NeroOreBlock> NEROSIUM_ORE = ore("nerosium_ore", "nerospace",
            p -> p.mapColor(MapColor.STONE).strength(3.0F, 3.0F).sound(SoundType.STONE));
    public static final RegistryEntry<NeroOreBlock> DEEPSLATE_NEROSIUM_ORE = ore("deepslate_nerosium_ore", "nerospace",
            p -> p.mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE));
    public static final RegistryEntry<Block> RAW_NEROSIUM_BLOCK = block("raw_nerosium_block", "nerospace", Block::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).strength(5.0F, 6.0F).requiresCorrectToolForDrops()
                    .sound(SoundType.METAL));
    public static final RegistryEntry<NeroOreBlock> NEROSTEEL_ORE = ore("nerosteel_ore", "nerospace",
            p -> p.mapColor(MapColor.STONE).strength(3.0F, 3.0F).sound(SoundType.STONE));
    public static final RegistryEntry<NeroOreBlock> XERTZ_QUARTZ_ORE = ore("xertz_quartz_ore", "nerospace",
            p -> p.mapColor(MapColor.STONE).strength(3.0F, 3.0F).sound(SoundType.NETHER_ORE));
    public static final RegistryEntry<DrippingOreBlock> AMBER_SAP_ORE = block("amber_sap_ore", "nerospace",
            DrippingOreBlock::new, p -> p.mapColor(MapColor.COLOR_ORANGE).strength(2.5F, 2.5F)
                    .requiresCorrectToolForDrops().sound(SoundType.WOOD));

    // --- Nerospace: Luna ---------------------------------------------------------------------------------
    public static final RegistryEntry<NeroOreBlock> ILMENITE_ORE = ore("ilmenite_ore", "nerospace",
            p -> p.mapColor(MapColor.STONE).strength(3.0F, 3.0F).sound(SoundType.STONE));
    public static final RegistryEntry<NeroOreBlock> CHEESE_ORE = ore("cheese_ore", "nerospace",
            p -> p.mapColor(MapColor.STONE).strength(3.0F, 3.0F).sound(SoundType.STONE));

    // --- Nerospace: Mars ---------------------------------------------------------------------------------
    public static final RegistryEntry<NeroOreBlock> HEMATITE_ORE = ore("hematite_ore", "nerospace",
            p -> p.mapColor(MapColor.TERRACOTTA_RED).strength(3.0F, 3.0F).sound(SoundType.STONE));

    // --- Nerospace: Cindara, Glacira, Nyxara --------------------------------------------------------------
    public static final RegistryEntry<NeroOreBlock> CINDRITE_ORE = ore("cindrite_ore", "nerospace",
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(3.5F, 3.0F).sound(SoundType.STONE));
    public static final RegistryEntry<NeroOreBlock> GLACITE_ORE = ore("glacite_ore", "nerospace",
            p -> p.mapColor(MapColor.ICE).strength(3.5F, 3.0F).sound(SoundType.STONE));
    public static final RegistryEntry<NeroOreBlock> VOIDGLASS_ORE = ore("voidglass_ore", "nerospace",
            p -> p.mapColor(MapColor.COLOR_PURPLE).strength(3.5F, 3.5F).sound(SoundType.STONE).lightLevel(s -> 1));

    // --- Nerospace: Europa -------------------------------------------------------------------------------
    public static final RegistryEntry<NeroOreBlock> BRINE_SALT_ORE = ore("brine_salt_ore", "nerospace",
            p -> p.mapColor(MapColor.LAPIS).strength(3.0F, 3.0F).sound(SoundType.GLASS));
    public static final RegistryEntry<NeroOreBlock> VENT_SULFIDE_ORE = ore("vent_sulfide_ore", "nerospace",
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(3.5F, 3.5F).sound(SoundType.BASALT));
    public static final RegistryEntry<NeroOreBlock> EUROPIUM_ORE = ore("europium_ore", "nerospace",
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(3.0F, 3.0F).sound(SoundType.BASALT).lightLevel(s -> 2));

    // --- NeroEvents: meteor craters ----------------------------------------------------------------------
    public static final RegistryEntry<NeroOreBlock> EMBEDDED_METEOR_ORE = ore("embedded_meteor_ore", "neroevents",
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(3.0F, 6.0F).sound(SoundType.BASALT));

    // --- Block items (creative order follows the blocks above) ------------------------------------------
    public static final RegistryEntry<BlockItem> NEROSIUM_ORE_ITEM = blockItem("nerosium_ore", NEROSIUM_ORE);
    public static final RegistryEntry<BlockItem> DEEPSLATE_NEROSIUM_ORE_ITEM =
            blockItem("deepslate_nerosium_ore", DEEPSLATE_NEROSIUM_ORE);
    public static final RegistryEntry<BlockItem> RAW_NEROSIUM_BLOCK_ITEM = blockItem("raw_nerosium_block", RAW_NEROSIUM_BLOCK);
    public static final RegistryEntry<BlockItem> NEROSTEEL_ORE_ITEM = blockItem("nerosteel_ore", NEROSTEEL_ORE);
    public static final RegistryEntry<BlockItem> XERTZ_QUARTZ_ORE_ITEM = blockItem("xertz_quartz_ore", XERTZ_QUARTZ_ORE);
    public static final RegistryEntry<BlockItem> AMBER_SAP_ORE_ITEM = blockItem("amber_sap_ore", AMBER_SAP_ORE);
    public static final RegistryEntry<BlockItem> ILMENITE_ORE_ITEM = blockItem("ilmenite_ore", ILMENITE_ORE);
    public static final RegistryEntry<BlockItem> CHEESE_ORE_ITEM = blockItem("cheese_ore", CHEESE_ORE);
    public static final RegistryEntry<BlockItem> HEMATITE_ORE_ITEM = blockItem("hematite_ore", HEMATITE_ORE);
    public static final RegistryEntry<BlockItem> CINDRITE_ORE_ITEM = blockItem("cindrite_ore", CINDRITE_ORE);
    public static final RegistryEntry<BlockItem> GLACITE_ORE_ITEM = blockItem("glacite_ore", GLACITE_ORE);
    public static final RegistryEntry<BlockItem> VOIDGLASS_ORE_ITEM = blockItem("voidglass_ore", VOIDGLASS_ORE);
    public static final RegistryEntry<BlockItem> BRINE_SALT_ORE_ITEM = blockItem("brine_salt_ore", BRINE_SALT_ORE);
    public static final RegistryEntry<BlockItem> VENT_SULFIDE_ORE_ITEM = blockItem("vent_sulfide_ore", VENT_SULFIDE_ORE);
    public static final RegistryEntry<BlockItem> EUROPIUM_ORE_ITEM = blockItem("europium_ore", EUROPIUM_ORE);
    public static final RegistryEntry<BlockItem> EMBEDDED_METEOR_ORE_ITEM =
            blockItem("embedded_meteor_ore", EMBEDDED_METEOR_ORE);

    // --- Raw materials -----------------------------------------------------------------------------------
    public static final RegistryEntry<Item> RAW_NEROSIUM = item("raw_nerosium", "nerospace");
    public static final RegistryEntry<Item> RAW_NEROSTEEL = item("raw_nerosteel", "nerospace");
    public static final RegistryEntry<Item> XERTZ_QUARTZ = item("xertz_quartz", "nerospace");
    public static final RegistryEntry<Item> AMBER_SAP = item("amber_sap", "nerospace");
    public static final RegistryEntry<Item> RAW_ILMENITE = item("raw_ilmenite", "nerospace");
    /** Moon Cheese: food (4 hunger, Regeneration I for 20 s) with one line of tooltip. */
    public static final RegistryEntry<TooltipItem> MOON_CHEESE = ModItems.ITEMS.register("moon_cheese",
            //? if >=1.21.2 {
            key -> new TooltipItem(PropertiesCompat.item(key).food(cheeseFood(), cheeseConsumable()),
            //?} else {
            /*key -> new TooltipItem(PropertiesCompat.item(key).food(cheeseFood()),
            *///?}
                    "item.nerolandcore.moon_cheese.tooltip", ChatFormatting.GOLD, ChatFormatting.ITALIC));
    public static final RegistryEntry<Item> RAW_HEMATITE = item("raw_hematite", "nerospace");
    public static final RegistryEntry<Item> CINDRITE = item("cindrite", "nerospace");
    public static final RegistryEntry<Item> GLACITE = item("glacite", "nerospace");
    public static final RegistryEntry<Item> VOIDGLASS = item("voidglass", "nerospace");
    public static final RegistryEntry<Item> BRINE_SALT = item("brine_salt", "nerospace");
    public static final RegistryEntry<Item> RAW_VENT_SULFIDE = item("raw_vent_sulfide", "nerospace");
    /** Raw Europium: a decorative gem with one line of tooltip. */
    public static final RegistryEntry<TooltipItem> RAW_EUROPIUM = ModItems.ITEMS.register("raw_europium",
            key -> new TooltipItem(PropertiesCompat.item(key), "item.nerolandcore.raw_europium.tooltip",
                    ChatFormatting.RED, ChatFormatting.ITALIC));
    /** Pallurite Dust: NeroPower's fertile fuel, centrifuged from tuff and fertile sources. */
    public static final RegistryEntry<Item> PALLURITE_DUST = item("pallurite_dust", "neropower");

    static {
        HOME_MODS.put("moon_cheese", "nerospace");
        HOME_MODS.put("raw_europium", "nerospace");
        CREATIVE.add(RAW_NEROSIUM);
        CREATIVE.add(RAW_NEROSTEEL);
        CREATIVE.add(XERTZ_QUARTZ);
        CREATIVE.add(AMBER_SAP);
        CREATIVE.add(RAW_ILMENITE);
        CREATIVE.add(MOON_CHEESE);
        CREATIVE.add(RAW_HEMATITE);
        CREATIVE.add(CINDRITE);
        CREATIVE.add(GLACITE);
        CREATIVE.add(VOIDGLASS);
        CREATIVE.add(BRINE_SALT);
        CREATIVE.add(RAW_VENT_SULFIDE);
        CREATIVE.add(RAW_EUROPIUM);
        CREATIVE.add(PALLURITE_DUST);
    }

    private CoreOres() {
    }

    /** Force class-load so the static registrations run. Blocks register before their items. */
    public static void init() {
    }

    /** Registry path → home mod id for every ore and raw material here. */
    public static Map<String, String> homeMods() {
        return Collections.unmodifiableMap(HOME_MODS);
    }

    /** Every ore item, in creative-tab order. */
    public static List<RegistryEntry<? extends ItemLike>> creativeOrder() {
        return Collections.unmodifiableList(CREATIVE);
    }

    static FoodProperties cheeseFood() {
        //? if >=1.21.2 {
        return new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).alwaysEdible().build();
        //?} else if >=1.20.5 {
        /*return new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).alwaysEdible().fast()
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 400, 0), 1.0F).build();
        *///?} else {
        /*return new FoodProperties.Builder().nutrition(4).saturationMod(0.6F).alwaysEat().fast()
                .effect(new MobEffectInstance(MobEffects.REGENERATION, 400, 0), 1.0F).build();
        *///?}
    }

    //? if >=1.21.2 {
    static Consumable cheeseConsumable() {
        return Consumables.defaultFood().consumeSeconds(0.8F)
                .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 0)))
                .build();
    }
    //?}

    private static RegistryEntry<NeroOreBlock> ore(String name, String homeMod,
            UnaryOperator<BlockBehaviour.Properties> props) {
        return block(name, homeMod, NeroOreBlock::new, p -> props.apply(p).requiresCorrectToolForDrops());
    }

    private static <B extends Block> RegistryEntry<B> block(String name, String homeMod,
            Function<BlockBehaviour.Properties, B> factory, UnaryOperator<BlockBehaviour.Properties> props) {
        HOME_MODS.put(name, homeMod);
        return ModBlocks.BLOCKS.register(name, key -> factory.apply(props.apply(PropertiesCompat.block(key))));
    }

    private static RegistryEntry<BlockItem> blockItem(String name, RegistryEntry<? extends Block> block) {
        // 26.x: each block item needs a mirrored item.nerolandcore.<id> lang entry (see ModItems).
        RegistryEntry<BlockItem> entry = ModItems.ITEMS.register(name,
                key -> new BlockItem(block.get(), PropertiesCompat.item(key)));
        CREATIVE.add(entry);
        return entry;
    }

    private static RegistryEntry<Item> item(String name, String homeMod) {
        HOME_MODS.put(name, homeMod);
        return ModItems.ITEMS.register(name, key -> new Item(PropertiesCompat.item(key)));
    }
}
