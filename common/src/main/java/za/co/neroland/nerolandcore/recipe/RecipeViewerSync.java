package za.co.neroland.nerolandcore.recipe;

import java.util.List;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * The vanilla recipe types Neroland Core sends to every client so recipe viewers can list them.
 *
 * <p>Since Minecraft 26.x the client keeps no full recipe list: the server sends only the recipe-book
 * entries a player has unlocked. JEI asks the server for these types itself, but only when JEI is also
 * installed on the server. On a server without it, JEI falls back to the vanilla recipes bundled with
 * the client, so every Nero crafting, smelting and stonecutting recipe disappears from it.
 *
 * <p>Core therefore asks the loader to send the same set JEI would (NeoForge:
 * {@code OnDatapackSyncEvent.sendRecipes}; Fabric: {@code RecipeSynchronization} for every
 * {@code minecraft}-namespace serializer). Asking twice is harmless on both loaders. Mods with their own
 * machine recipe types still send those themselves. These are recipe definitions from the server's
 * datapacks, not player data.
 */
public final class RecipeViewerSync {

    /** Exactly the list JEI sends when it is installed on the server. */
    public static final List<RecipeType<?>> VANILLA_TYPES = List.of(
            RecipeType.CRAFTING,
            RecipeType.STONECUTTING,
            RecipeType.SMELTING,
            RecipeType.SMOKING,
            RecipeType.BLASTING,
            RecipeType.CAMPFIRE_COOKING,
            RecipeType.SMITHING);

    /** Namespace of the serializers Fabric is told to sync (the vanilla ones). */
    public static final String VANILLA_NAMESPACE = "minecraft";

    private RecipeViewerSync() {
    }

    /** {@link #VANILLA_TYPES} as an array, for NeoForge's varargs {@code sendRecipes}. */
    public static RecipeType<?>[] vanillaTypes() {
        return VANILLA_TYPES.toArray(new RecipeType<?>[0]);
    }

    /** True for a serializer id Fabric should sync. */
    public static boolean isVanillaSerializer(Identifier id) {
        return VANILLA_NAMESPACE.equals(id.getNamespace());
    }
}
