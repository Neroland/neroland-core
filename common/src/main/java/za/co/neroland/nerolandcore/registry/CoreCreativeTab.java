package za.co.neroland.nerolandcore.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.registry.RegistrationProvider.RegistryEntry;

/**
 * Core's own "Neroland" creative tab. It lists <b>only Neroland Core's items</b>.
 *
 * <p>Since 1.16.0 every Nero mod registers its own creative tab (through
 * {@link RegistrationProvider} over the vanilla {@code CREATIVE_MODE_TAB} registry, the same way this
 * class does). The old contribution seam ({@link #add(Supplier)}, {@link #addDecor(Supplier)},
 * {@link #addStack(Supplier)}) is kept for binary compatibility with mods compiled against 1.15.0 and
 * earlier, but it is deprecated and anything it receives from another namespace is dropped when the tab
 * is built. Registered via {@link RegistrationProvider} as a plain-vanilla tab populated by
 * {@code displayItems}, which behaves identically on Fabric, Forge and NeoForge (the per-loader
 * tab-injection events do not). The icon is the Nero Alloy ingot, Core's first backbone material.
 */
public final class CoreCreativeTab {

    public static final RegistrationProvider<CreativeModeTab> TABS =
            RegistrationProvider.get(Registries.CREATIVE_MODE_TAB, NerolandCoreCommon.MOD_ID);

    /** Core's own items, in display order. Filled by {@link ModItems#addToCreativeTab()}. */
    private static final List<Supplier<? extends ItemLike>> CORE_CONTENTS = new ArrayList<>();

    /** Legacy contributions through the deprecated public seam; filtered to Core's namespace on build. */
    private static final List<Supplier<? extends ItemLike>> CONTENTS = new ArrayList<>();
    private static final List<Supplier<ItemStack>> STACKS = new ArrayList<>();
    private static final List<Supplier<? extends ItemLike>> DECOR_CONTENTS = new ArrayList<>();

    /** Logs the "foreign contribution ignored" notice once per session, not once per item. */
    private static final AtomicBoolean FOREIGN_LOGGED = new AtomicBoolean();

    // NOTE: vanilla CreativeModeTab.builder takes (Row, column); the no-arg overload and
    // withTabsBefore/After are NeoForge-only extensions, so they are avoided here (common = raw vanilla).
    public static final RegistryEntry<CreativeModeTab> NEROLAND = TABS.register("neroland",
            key -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.nerolandcore"))
                    .icon(() -> new ItemStack(ModItems.NERO_ALLOY_INGOT.get()))
                    .displayItems((params, output) -> {
                        CORE_CONTENTS.forEach(s -> output.accept(s.get()));
                        CONTENTS.forEach(s -> {
                            ItemLike item = s.get();
                            if (isCoreItem(item.asItem())) {
                                output.accept(item);
                            }
                        });
                        STACKS.forEach(s -> {
                            ItemStack stack = s.get();
                            if (isCoreItem(stack.getItem())) {
                                output.accept(stack);
                            }
                        });
                    })
                    .build());

    /**
     * The <b>Neroland Decor</b> tab (added in Core 1.9.0). Core has no decor items of its own, so since
     * 1.16.0 this tab is always empty and vanilla hides it; decorative mods list their blocks in their
     * own tab instead.
     *
     * @deprecated since 1.16.0: register a creative tab in your own mod. Scheduled for removal in 2.0.
     */
    @Deprecated(since = "1.16.0", forRemoval = true)
    public static final RegistryEntry<CreativeModeTab> NEROLAND_DECOR = TABS.register("neroland_decor",
            key -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
                    .title(Component.translatable("itemGroup.nerolandcore.decor"))
                    .icon(() -> new ItemStack(ModItems.PLASMA_GLASS_BLOCK_ITEM.get()))
                    .displayItems((params, output) -> DECOR_CONTENTS.forEach(s -> {
                        ItemLike item = s.get();
                        if (isCoreItem(item.asItem())) {
                            output.accept(item);
                        }
                    }))
                    .build());

    private CoreCreativeTab() {
    }

    /**
     * Formerly appended an item to the shared Neroland tab. Since 1.16.0 the Neroland tab lists only
     * Core's items: each Nero mod registers its own creative tab, and anything passed here from another
     * namespace is ignored.
     *
     * @param item supplier of the item (typically a {@link RegistryEntry})
     * @deprecated since 1.16.0: register a creative tab in your own mod. Scheduled for removal in 2.0.
     */
    @Deprecated(since = "1.16.0", forRemoval = true)
    public static void add(Supplier<? extends ItemLike> item) {
        CONTENTS.add(item);
    }

    /**
     * Formerly appended an item to the Neroland Decor tab. Since 1.16.0 anything passed here from
     * another namespace is ignored; decorative mods list their blocks in their own creative tab.
     *
     * @param item supplier of the item (typically a {@link RegistryEntry})
     * @deprecated since 1.16.0: register a creative tab in your own mod. Scheduled for removal in 2.0.
     */
    @Deprecated(since = "1.16.0", forRemoval = true)
    public static void addDecor(Supplier<? extends ItemLike> item) {
        DECOR_CONTENTS.add(item);
    }

    /**
     * Formerly appended a configured example stack to the Neroland tab. Since 1.16.0 stacks of items
     * from another namespace are ignored.
     *
     * @param stack supplier of the stack
     * @deprecated since 1.16.0: register a creative tab in your own mod. Scheduled for removal in 2.0.
     */
    @Deprecated(since = "1.16.0", forRemoval = true)
    public static void addStack(Supplier<ItemStack> stack) {
        STACKS.add(stack);
    }

    /** Core-internal: append one of Core's own items to the Neroland tab, in display order. */
    static void addCore(Supplier<? extends ItemLike> item) {
        CORE_CONTENTS.add(item);
    }

    private static boolean isCoreItem(Item item) {
        if (NerolandCoreCommon.MOD_ID.equals(BuiltInRegistries.ITEM.getKey(item).getNamespace())) {
            return true;
        }
        if (FOREIGN_LOGGED.compareAndSet(false, true)) {
            NerolandCoreCommon.LOGGER.debug("[Neroland Core] Ignoring creative-tab contributions from other mods:"
                    + " the Neroland tab lists only Core items since 1.16.0; each mod should register its own tab.");
        }
        return false;
    }

    /** Force class-load so the static tab registration runs. Called from {@link CoreRegistries#init()}. */
    public static void init() {
    }
}
