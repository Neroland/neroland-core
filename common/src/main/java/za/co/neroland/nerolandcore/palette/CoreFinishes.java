package za.co.neroland.nerolandcore.palette;

import net.minecraft.resources.Identifier;

/**
 * Core's built-in {@link Finish}es: the four backbone materials plus a curated 16-colour
 * accent set (aligned to the vanilla dye names so a paint/pigment cost model maps
 * cleanly). These are the palette every Nero mod and NeroDecor match against.
 *
 * <p>Ids are namespaced {@code neroland:…} and are a frozen contract within the 1.x
 * major. Material finishes carry an emissive flag + light level matching Core's block
 * definitions (Void Crystal and Plasma Glass glow); accents are flat cosmetic colours.
 */
public final class CoreFinishes {

    // --- Material finishes (colours/emissive match ModBlocks) ----------------
    public static final Finish NERO_ALLOY = Finish.material(
            id("nero_alloy"), "Nero Alloy", 0x2E8B9E, false, 0, tag("c", "ingots/nero_alloy"));
    public static final Finish STARSTEEL = Finish.material(
            id("starsteel"), "Starsteel", 0x9FC4E0, false, 0, tag("c", "ingots/starsteel"));
    public static final Finish VOID_CRYSTAL = Finish.material(
            id("void_crystal"), "Void Crystal", 0x7A3FB0, true, 6, tag("c", "gems/void_crystal"));
    public static final Finish PLASMA_GLASS = Finish.material(
            id("plasma_glass"), "Plasma Glass", 0x3FD0E0, true, 4, tag("c", "glass_blocks"));

    // --- Accent set (16, dye-aligned) ---------------------------------------
    public static final Finish WHITE = accent("white", "White", 0xF9FFFE);
    public static final Finish LIGHT_GRAY = accent("light_gray", "Light Gray", 0x9D9D97);
    public static final Finish GRAY = accent("gray", "Gray", 0x474F52);
    public static final Finish BLACK = accent("black", "Black", 0x1D1D21);
    public static final Finish RED = accent("red", "Red", 0xB02E26);
    public static final Finish ORANGE = accent("orange", "Orange", 0xF9801D);
    public static final Finish YELLOW = accent("yellow", "Yellow", 0xFED83D);
    public static final Finish LIME = accent("lime", "Lime", 0x80C71F);
    public static final Finish GREEN = accent("green", "Green", 0x5E7C16);
    public static final Finish CYAN = accent("cyan", "Cyan", 0x169C9C);
    public static final Finish LIGHT_BLUE = accent("light_blue", "Light Blue", 0x3AB3DA);
    public static final Finish BLUE = accent("blue", "Blue", 0x3C44AA);
    public static final Finish PURPLE = accent("purple", "Purple", 0x8932B8);
    public static final Finish MAGENTA = accent("magenta", "Magenta", 0xC74EBD);
    public static final Finish PINK = accent("pink", "Pink", 0xF38BAA);
    public static final Finish BROWN = accent("brown", "Brown", 0x835432);

    private CoreFinishes() {
    }

    /** Register every built-in finish into the {@link PaletteRegistry}. Idempotent. */
    public static void registerAll() {
        // Materials
        PaletteRegistry.register(NERO_ALLOY);
        PaletteRegistry.register(STARSTEEL);
        PaletteRegistry.register(VOID_CRYSTAL);
        PaletteRegistry.register(PLASMA_GLASS);
        // Accents
        PaletteRegistry.register(WHITE);
        PaletteRegistry.register(LIGHT_GRAY);
        PaletteRegistry.register(GRAY);
        PaletteRegistry.register(BLACK);
        PaletteRegistry.register(RED);
        PaletteRegistry.register(ORANGE);
        PaletteRegistry.register(YELLOW);
        PaletteRegistry.register(LIME);
        PaletteRegistry.register(GREEN);
        PaletteRegistry.register(CYAN);
        PaletteRegistry.register(LIGHT_BLUE);
        PaletteRegistry.register(BLUE);
        PaletteRegistry.register(PURPLE);
        PaletteRegistry.register(MAGENTA);
        PaletteRegistry.register(PINK);
        PaletteRegistry.register(BROWN);
    }

    private static Finish accent(String path, String name, int rgb) {
        return Finish.accent(id("accent/" + path), name, rgb);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("neroland", path);
    }

    private static Identifier tag(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}
