package za.co.neroland.nerolandcore.demo;

//? if >=26.3 {
/*import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import za.co.neroland.nerolandcore.NerolandCoreCommon;

// Creates a Nero Mods Demo world (Core 1.20.0, Minecraft 26.3+) through vanilla's own Create World screen,
// so every loader's data-pack and feature handling applies exactly as for a hand-made world. The screen is
// opened, its settings are filled in (name, creative, cheats on, fixed seed, void overworld, showroom game
// rules) and its Create button is pressed; the player never has to touch it.
//
// Why a void overworld: a one-layer air superflat generator with the_void biome and no structures or
// features is plain vanilla data, works identically on every loader and needs no Core dimension or data
// pack in every other world. The hub and plots are then placed by code at fixed coordinates, so the same
// selection always yields the same world. Other dimensions (a mod's planets) keep their own generators and
// are fixed by the seed.
public final class DemoWorldCreator {

    // GLFW_KEY_ENTER; only used to satisfy Button.onPress.
    private static final int KEY_ENTER = 257;
    private static final SystemToast.SystemToastId CREATE_FAILED = new SystemToast.SystemToastId();

    private DemoWorldCreator() {
    }

    // Starts creating a demo world for `moduleIds`; on failure returns to `back`.
    public static void create(Minecraft mc, List<String> moduleIds, Screen back) {
        DemoPending.clear();
        try {
            CreateWorldScreen.openFresh(mc, () -> {
                DemoPending.clear();
                mc.gui.setScreen(back);
            });
            if (!(mc.gui.screen() instanceof CreateWorldScreen screen)) {
                // Forge lets screen-opening listeners cancel or replace the requested screen.
                NerolandCoreCommon.LOGGER.warn("[Neroland Core] Demo world creation was interrupted while opening Create World.");
                showFailure(mc);
                return;
            }
            configure(screen.getUiState());
            DemoPending.offer(DemoWorlds.LEVEL_NAME, moduleIds);
            if (!pressCreate(screen)) {
                throw new IllegalStateException("Create button not found");
            }
        } catch (RuntimeException e) {
            DemoPending.clear();
            NerolandCoreCommon.LOGGER.error("[Neroland Core] Could not create the demo world.", e);
            showFailure(mc);
            mc.gui.setScreen(back);
        }
    }

    private static void showFailure(Minecraft mc) {
        SystemToast.add(mc.gui.toastManager(), CREATE_FAILED,
                Component.translatable("demo.nerolandcore.picker.create_failed"), null);
    }

    static void configure(WorldCreationUiState ui) {
        ui.setName(DemoWorlds.LEVEL_NAME);
        ui.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
        ui.setAllowCommands(true);
        ui.setSeed(DemoWorlds.SEED);
        ui.setGenerateStructures(false);
        ui.setBonusChest(false);
        ui.updateDimensions((registries, dimensions) ->
                dimensions.replaceOverworldGenerator(registries, voidGenerator(registries)));
        GameRules rules = ui.getGameRules();
        rules.set(GameRules.ADVANCE_TIME, false, null);
        rules.set(GameRules.ADVANCE_WEATHER, false, null);
        rules.set(GameRules.SPAWN_MOBS, false, null);
        rules.set(GameRules.SPAWN_MONSTERS, false, null);
        rules.set(GameRules.SPAWN_PHANTOMS, false, null);
        rules.set(GameRules.SPAWN_PATROLS, false, null);
        rules.set(GameRules.SPAWN_WANDERING_TRADERS, false, null);
        rules.set(GameRules.MOB_GRIEFING, false, null);
        rules.set(GameRules.KEEP_INVENTORY, true, null);
        rules.set(GameRules.RESPAWN_RADIUS, 0, null);
        rules.set(GameRules.COMMAND_BLOCK_OUTPUT, false, null);
        ui.setGameRules(rules);
    }

    // One layer of air, the_void biome, no structures, no lakes, no decoration: an empty overworld.
    static FlatLevelSource voidGenerator(RegistryAccess.Frozen registries) {
        HolderGetter<Biome> biomes = registries.lookupOrThrow(Registries.BIOME);
        Holder<Biome> theVoid = biomes.getOrThrow(Biomes.THE_VOID);
        Optional<HolderSet<StructureSet>> noStructures = Optional.of(HolderSet.<StructureSet>direct(List.of()));
        FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(noStructures, theVoid, List.of())
                .withBiomeAndLayers(List.of(new FlatLayerInfo(1, Blocks.AIR)), noStructures, theVoid);
        return new FlatLevelSource(settings);
    }

    private static boolean pressCreate(CreateWorldScreen screen) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof Button button
                    && button.getMessage().getContents() instanceof TranslatableContents key
                    && "selectWorld.create".equals(key.getKey())) {
                button.onPress(new KeyEvent(KEY_ENTER, 0, 0));
                return true;
            }
        }
        return false;
    }
}
*///?} else {
// The Nero Mods Demo needs Minecraft 26.3 or newer; on older lines this class is an empty placeholder.
public final class DemoWorldCreator {

    private DemoWorldCreator() {
    }
}
//?}
