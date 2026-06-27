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
public final class CoreRegistries {

    private CoreRegistries() {
    }

    public static void init() {
        // Phase 1: the shared creative tab. Materials, blocks, items, block-entities
        // and menus are added here as later phases land.
        CoreCreativeTab.init();
    }
}
