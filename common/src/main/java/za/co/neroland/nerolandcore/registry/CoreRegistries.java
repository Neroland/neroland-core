package za.co.neroland.nerolandcore.registry;

/**
 * Aggregates Neroland Core's cross-loader registrations. Called once from
 * {@link za.co.neroland.nerolandcore.NerolandCoreCommon#init()}.
 *
 * <p>On the eager (Fabric) loader, registration order matters where one entry
 * references another (e.g. block items after blocks); add future systems
 * (materials, blocks, items, block-entities, menus) in dependency order. On
 * NeoForge / Forge each provider wraps a {@code DeferredRegister} that the loader
 * entry point attaches to the mod bus after this runs.
 */
@org.jetbrains.annotations.ApiStatus.Internal
public final class CoreRegistries {

    private CoreRegistries() {
    }

    public static void init() {
        // Order matters on the eager (Fabric) loader: blocks before items (block
        // items reference their block), then the creative tab, then its contents.
        ModBlocks.init();
        ModBlockEntities.init();
        ModMenuTypes.init();
        ModItems.init();
        CoreCreativeTab.init();
        ModItems.addToCreativeTab();
    }
}
