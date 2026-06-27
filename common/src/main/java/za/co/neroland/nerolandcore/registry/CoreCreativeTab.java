package za.co.neroland.nerolandcore.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
 * <p>Downstream mods (and later Core phases) contribute via {@link #add(Supplier)}
 * during their {@code init()}, before the tab is built. Until Core ships its own
 * materials (Phase 2), the icon is a vanilla placeholder — swap it to the Nero
 * Alloy ingot when materials land.
 */
public final class CoreCreativeTab {

    public static final RegistrationProvider<CreativeModeTab> TABS =
            RegistrationProvider.get(Registries.CREATIVE_MODE_TAB, NerolandCoreCommon.MOD_ID);

    /** Item suppliers contributed by Core systems and downstream mods, drained into the tab on build. */
    private static final List<Supplier<? extends ItemLike>> CONTENTS = new ArrayList<>();

    // NOTE: vanilla CreativeModeTab.builder takes (Row, column); the no-arg overload and
    // withTabsBefore/After are NeoForge-only extensions, so they are avoided here (common = raw vanilla).
    public static final RegistryEntry<CreativeModeTab> NEROLAND = TABS.register("neroland",
            key -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.nerolandcore"))
                    .icon(() -> new ItemStack(Items.NETHER_STAR))
                    .displayItems((params, output) -> CONTENTS.forEach(s -> output.accept(s.get())))
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

    /** Force class-load so the static tab registration runs. Called from {@link CoreRegistries#init()}. */
    public static void init() {
    }
}
