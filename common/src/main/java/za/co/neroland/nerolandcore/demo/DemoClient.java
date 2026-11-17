package za.co.neroland.nerolandcore.demo;

//? if >=26.3 {
/*import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.config.CoreConfig;

// Client entry point of the Nero Mods Demo (Core 1.20.0, Minecraft 26.3+). Each loader calls
// afterScreenInit from its own screen-init event; this adds the "Nero Mods Demo" button to the
// singleplayer world list. With no saved worlds vanilla skips that list and opens Create World straight
// away, so in that one case the button is added there instead. Hidden when demoButtonEnabled is false.
public final class DemoClient {

    // Translation key of the button label; also how the gametest finds the button.
    public static final String BUTTON_KEY = "demo.nerolandcore.button";

    public static final int BUTTON_WIDTH = 100;
    public static final int BUTTON_HEIGHT = 20;

    private DemoClient() {
    }

    // Adds the button to `screen` through `add` when it is the singleplayer world list (or the first
    // Create World screen of a game with no saved worlds) and the config allows it.
    public static void afterScreenInit(Screen screen, Consumer<Button> add) {
        try {
            if (!CoreConfig.DEMO_BUTTON_ENABLED.get() || !wantsButton(screen)) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            Button button = Button.builder(Component.translatable(BUTTON_KEY),
                            b -> mc.gui.setScreen(new DemoPickerScreen(screen)))
                    .bounds(4, 4, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .tooltip(Tooltip.create(Component.translatable("demo.nerolandcore.button.tooltip")))
                    .build();
            add.accept(button);
        } catch (RuntimeException e) {
            // Never break the world list over an optional button.
            NerolandCoreCommon.LOGGER.warn("[Neroland Core] Could not add the Nero Mods Demo button.", e);
        }
    }

    private static boolean wantsButton(Screen screen) {
        if (screen instanceof SelectWorldScreen) {
            return true;
        }
        if (screen instanceof CreateWorldScreen) {
            try {
                return Minecraft.getInstance().getLevelSource().findLevelCandidates().isEmpty();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    // The demo modules the picker offers: registered, with every required mod loaded.
    static List<DemoModule> offered() {
        return DemoRegistry.available();
    }
}
*///?} else {
// The Nero Mods Demo needs Minecraft 26.3 or newer; on older lines this class is an empty placeholder.
public final class DemoClient {

    private DemoClient() {
    }
}
//?}
