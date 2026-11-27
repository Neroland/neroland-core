package za.co.neroland.nerolandcore.registry;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

/**
 * Block, item and block-entity ids that moved into Neroland Core from another Nero mod, so a world saved
 * before the move still loads them. Every moved entry keeps its path and only changes namespace:
 * {@code nerospace:hematite_ore} becomes {@code nerolandcore:hematite_ore}.
 *
 * <ul>
 *   <li>Nerospace storage blocks (Core 1.0): Battery, Fluid Tank, Gas Tank, Item Store, their Creative
 *       variants and the Trash Can. Blocks, items and block-entity types.</li>
 *   <li>Every Nero ore and the raw material it drops (Core 1.25.0): Nerospace's planet ores, NeroEvents'
 *       Embedded Meteor Ore and NeroPower's Pallurite Dust. Blocks and items.</li>
 * </ul>
 *
 * <p>The table is consulted by {@code MappedRegistryLegacyAliasMixin} on every loader, by NeoForge's
 * registry aliases and by Forge's missing-mappings event. An old id is only redirected while nothing is
 * registered under it, so an older home mod that still registers its own copy is left alone.
 *
 * <p>Plain strings only, so the table can be unit-tested without Minecraft.
 *
 * @since 1.25.0
 */
public final class LegacyIds {

    public static final String CORE = "nerolandcore";

    private static final List<String> STORAGE = List.of(
            "battery", "fluid_tank", "gas_tank", "item_store",
            "creative_battery", "creative_fluid_tank", "creative_gas_tank", "creative_item_store",
            "trash_can");

    private static final List<String> NEROSPACE_ORES = List.of(
            "nerosium_ore", "deepslate_nerosium_ore", "raw_nerosium_block", "nerosteel_ore",
            "xertz_quartz_ore", "amber_sap_ore", "ilmenite_ore", "cheese_ore", "hematite_ore",
            "cindrite_ore", "glacite_ore", "voidglass_ore", "brine_salt_ore", "vent_sulfide_ore",
            "europium_ore",
            "raw_nerosium", "raw_nerosteel", "xertz_quartz", "amber_sap", "raw_ilmenite", "moon_cheese",
            "raw_hematite", "cindrite", "glacite", "voidglass", "brine_salt", "raw_vent_sulfide",
            "raw_europium");

    private static final Map<String, Set<String>> BLOCKS_AND_ITEMS = new LinkedHashMap<>();
    private static final Map<String, Set<String>> BLOCK_ENTITY_TYPES = new LinkedHashMap<>();

    static {
        Set<String> nerospace = new LinkedHashSet<>(STORAGE);
        nerospace.addAll(NEROSPACE_ORES);
        BLOCKS_AND_ITEMS.put("nerospace", Set.copyOf(nerospace));
        BLOCKS_AND_ITEMS.put("neroevents", Set.of("embedded_meteor_ore"));
        BLOCKS_AND_ITEMS.put("neropower", Set.of("pallurite_dust"));
        BLOCK_ENTITY_TYPES.put("nerospace", Set.copyOf(STORAGE));
    }

    private LegacyIds() {
    }

    /**
     * The Core path a moved block or item id now has, or {@code null} if {@code namespace:path} did not
     * move.
     */
    @Nullable
    public static String blockOrItem(String namespace, String path) {
        Set<String> moved = BLOCKS_AND_ITEMS.get(namespace);
        return moved != null && moved.contains(path) ? path : null;
    }

    /**
     * Whether {@code namespace:path} is an ore or ore drop that moved into Core in 1.25.0 (not one of the older
     * storage blocks). Core registers these but never generates them; their home mod does.
     */
    public static boolean movedOre(String namespace, String path) {
        return switch (namespace) {
            case "nerospace" -> NEROSPACE_ORES.contains(path);
            case "neroevents" -> "embedded_meteor_ore".equals(path);
            case "neropower" -> "pallurite_dust".equals(path);
            default -> false;
        };
    }

    /** As {@link #blockOrItem} for block-entity types. */
    @Nullable
    public static String blockEntityType(String namespace, String path) {
        Set<String> moved = BLOCK_ENTITY_TYPES.get(namespace);
        return moved != null && moved.contains(path) ? path : null;
    }

    /** Old namespace → moved block and item paths. */
    public static Map<String, Set<String>> blocksAndItems() {
        return java.util.Collections.unmodifiableMap(BLOCKS_AND_ITEMS);
    }

    /** Old namespace → moved block-entity type paths. */
    public static Map<String, Set<String>> blockEntityTypes() {
        return java.util.Collections.unmodifiableMap(BLOCK_ENTITY_TYPES);
    }
}
