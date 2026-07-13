package za.co.neroland.nerolandcore.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.registry.RegistrationProvider.RegistryEntry;

/**
 * The shared "Neroland" creative tab that every Nero mod appends its items into,
 * so a server with ten Nero mods shows one organised tab instead of ten cluttered
 * ones. Registered cross-loader via {@link RegistrationProvider} over the vanilla
 * {@code CREATIVE_MODE_TAB} registry — a dedicated, plain-vanilla tab populated by
 * {@code displayItems}, which behaves identically on Fabric, Forge and NeoForge
 * (the per-loader tab-injection events do not).
 *
 * <p>Downstream mods (and Core itself, via {@link ModItems#addToCreativeTab()})
 * contribute through {@link #add(Supplier)} during {@code init()}, before the tab
 * is built. The icon is the Nero Alloy ingot — Core's first backbone material.
 */
public final class CoreCreativeTab {

    public static final RegistrationProvider<CreativeModeTab> TABS =
            RegistrationProvider.get(Registries.CREATIVE_MODE_TAB, NerolandCoreCommon.MOD_ID);

    /** Item suppliers contributed by Core systems and downstream mods, drained into the tab on build. */
    private static final List<Supplier<? extends ItemLike>> CONTENTS = new ArrayList<>();
    private static final List<Supplier<ItemStack>> STACKS = new ArrayList<>();

    /** Contents of the separate decor sub-group tab (see {@link #NEROLAND_DECOR}). */
    private static final List<Supplier<? extends ItemLike>> DECOR_CONTENTS = new ArrayList<>();

    // NOTE: vanilla CreativeModeTab.builder takes (Row, column); the no-arg overload and
    // withTabsBefore/After are NeoForge-only extensions, so they are avoided here (common = raw vanilla).
    public static final RegistryEntry<CreativeModeTab> NEROLAND = TABS.register("neroland",
            key -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.nerolandcore"))
                    .icon(() -> new ItemStack(ModItems.NERO_ALLOY_INGOT.get()))
                    .displayItems((params, output) -> {
                        CONTENTS.forEach(s -> output.accept(s.get()));
                        STACKS.forEach(s -> output.accept(s.get()));
                    })
                    .build());

    /**
     * The separate <b>Neroland Decor</b> sub-group tab (added in Core 1.9.0). Decorative
     * mods (NeroDecor first) register their many blocks here via {@link #addDecor(Supplier)}
     * so a large decor kit lives in its own organised tab instead of swamping the main
     * Neroland tab. Icon is Plasma Glass — a recognisably decorative Core block. If no mod
     * contributes decor, the tab is simply empty (harmless).
     */
    public static final RegistryEntry<CreativeModeTab> NEROLAND_DECOR = TABS.register("neroland_decor",
            key -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
                    .title(Component.translatable("itemGroup.nerolandcore.decor"))
                    .icon(() -> new ItemStack(ModItems.PLASMA_GLASS_BLOCK_ITEM.get()))
                    .displayItems((params, output) -> DECOR_CONTENTS.forEach(s -> output.accept(s.get())))
                    .build());

    private CoreCreativeTab() {
    }

    /**
     * Append an item to the shared Neroland creative tab. Call during mod init, before tabs are built.
     *
     * @param item supplier of the item to show (typically a {@link RegistryEntry})
     */
    public static void add(Supplier<? extends ItemLike> item) {
        CONTENTS.add(item);
    }

    /**
     * Append an item to the <b>Neroland Decor</b> sub-group tab ({@link #NEROLAND_DECOR}).
     * Call during mod init, before tabs are built. Decorative mods use this instead of
     * {@link #add(Supplier)} to keep the main tab uncluttered.
     *
     * @param item supplier of the item to show (typically a {@link RegistryEntry})
     */
    public static void addDecor(Supplier<? extends ItemLike> item) {
        DECOR_CONTENTS.add(item);
    }

    /** Append a configured example stack without registering another item id. */
    public static void addStack(Supplier<ItemStack> stack) {
        STACKS.add(stack);
    }

    /** Force class-load so the static tab registration runs. Called from {@link CoreRegistries#init()}. */
    public static void init() {
    }
}
