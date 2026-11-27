package za.co.neroland.nerolandcore.reclaim;

import java.io.BufferedReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

/**
 * The Ore Reclaimer's conversions, read from every datapack's {@code neroland/ore_reclaiming/*.json}
 * (see {@link ReclaimRecipe}). Re-read whenever the server's resources change, so {@code /reload} applies
 * edits. A datapack replaces a file by shipping one with the same id.
 *
 * @since 1.25.0
 */
public final class ReclaimRecipes {

    public static final String DIRECTORY = "neroland/ore_reclaiming";
    private static final String SUFFIX = ".json";

    private static WeakReference<ResourceManager> loadedFrom = new WeakReference<>(null);
    private static List<ReclaimRecipe> recipes = List.of();

    private ReclaimRecipes() {
    }

    /** Every conversion for {@code server}, active or not, in file-id order. */
    public static synchronized List<ReclaimRecipe> forServer(MinecraftServer server) {
        ResourceManager resources = server.getResourceManager();
        if (loadedFrom.get() != resources) {
            recipes = load(resources);
            loadedFrom = new WeakReference<>(resources);
        }
        return recipes;
    }

    /**
     * The conversion for {@code stack}: the first active one that matches, else the first inactive one
     * that matches (so the machine can say why it refuses), else {@code null}.
     */
    @Nullable
    public static ReclaimRecipe find(MinecraftServer server, ItemStack stack) {
        ReclaimRecipe inactive = null;
        for (ReclaimRecipe recipe : forServer(server)) {
            if (recipe.matches(stack)) {
                if (recipe.active()) {
                    return recipe;
                }
                if (inactive == null) {
                    inactive = recipe;
                }
            }
        }
        return inactive;
    }

    private static List<ReclaimRecipe> load(ResourceManager resources) {
        List<ReclaimRecipe> loaded = new ArrayList<>();
        try {
            Map<Identifier, Resource> files =
                    resources.listResources(DIRECTORY, file -> file.getPath().endsWith(SUFFIX));
            List<Identifier> ids = new ArrayList<>(files.keySet());
            ids.sort(Identifier::compareTo);
            for (Identifier fileId : ids) {
                Identifier id = Identifier.fromNamespaceAndPath(fileId.getNamespace(),
                        fileId.getPath().substring(DIRECTORY.length() + 1, fileId.getPath().length() - SUFFIX.length()));
                try (BufferedReader reader = files.get(fileId).openAsReader()) {
                    JsonElement json = JsonParser.parseReader(reader);
                    ReclaimRecipe recipe = json != null && json.isJsonObject() ? parse(id, json.getAsJsonObject()) : null;
                    if (recipe != null) {
                        loaded.add(recipe);
                    } else {
                        NerolandCoreCommon.LOGGER.warn("[Neroland Core] Skipping invalid ore reclaiming file {}", id);
                    }
                } catch (Exception e) {
                    NerolandCoreCommon.LOGGER.warn("[Neroland Core] Could not read ore reclaiming file {}", id, e);
                }
            }
        } catch (RuntimeException | LinkageError e) {
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Ore reclaiming data failed to load.", e);
            return List.of();
        }
        return List.copyOf(loaded);
    }

    @Nullable
    static ReclaimRecipe parse(Identifier id, JsonObject json) {
        if (!json.has("input") || !json.has("result")) {
            return null;
        }
        String input = json.get("input").getAsString();
        Identifier item = null;
        Identifier tag = null;
        if (input.startsWith("#")) {
            tag = Identifier.tryParse(input.substring(1));
        } else {
            item = Identifier.tryParse(input);
        }
        if (item == null && tag == null) {
            return null;
        }
        JsonElement resultJson = json.get("result");
        Identifier result;
        int count = 1;
        if (resultJson.isJsonObject()) {
            JsonObject r = resultJson.getAsJsonObject();
            result = r.has("id") ? Identifier.tryParse(r.get("id").getAsString()) : null;
            if (r.has("count")) {
                count = r.get("count").getAsInt();
            }
        } else {
            result = Identifier.tryParse(resultJson.getAsString());
        }
        if (result == null) {
            return null;
        }
        int time = json.has("time") ? json.get("time").getAsInt() : ReclaimRecipe.DEFAULT_TIME;
        List<String> whenAbsent = new ArrayList<>();
        if (json.has("when_absent")) {
            JsonArray mods = json.getAsJsonArray("when_absent");
            for (JsonElement mod : mods) {
                whenAbsent.add(mod.getAsString());
            }
        }
        return new ReclaimRecipe(id, item, tag == null ? null : ReclaimRecipe.itemTag(tag), result,
                Math.max(1, Math.min(64, count)), Math.max(1, time), whenAbsent);
    }
}
