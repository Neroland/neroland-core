package za.co.neroland.nerolandcore.demo;

//? if >=26.3 {
/*import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

// The Nero Mods Demo picker (Core 1.20.0, Minecraft 26.3+): every demo module whose required mods are
// loaded, each with its icon, name and a checkbox (all ticked), and a button that creates the world.
public final class DemoPickerScreen extends Screen {

    private static final int ROW = 22;
    private static final int COLUMN_WIDTH = 190;
    private static final int COLUMN_GAP = 12;
    private static final int ICON = 20;
    private static final int TOP = 52;

    private final Screen parent;
    private final List<DemoModule> modules;
    private final List<Identifier> icons = new ArrayList<>();
    private final List<Checkbox> boxes = new ArrayList<>();
    private final List<DemoModule> shown = new ArrayList<>();
    private Button create;

    public DemoPickerScreen(Screen parent) {
        super(Component.translatable("demo.nerolandcore.picker.title"));
        this.parent = parent;
        this.modules = DemoClient.offered();
    }

    @Override
    protected void init() {
        this.boxes.clear();
        this.shown.clear();
        this.icons.clear();
        int bottom = this.height - 40;
        int rowsFit = Math.max(1, (bottom - TOP) / ROW);
        int columns = this.modules.size() > rowsFit ? 2 : 1;
        int perColumn = Math.max(1, (this.modules.size() + columns - 1) / columns);
        int total = columns * COLUMN_WIDTH + (columns - 1) * COLUMN_GAP;
        int left = this.width / 2 - total / 2;
        for (int i = 0; i < this.modules.size(); i++) {
            int col = i / perColumn;
            int row = i % perColumn;
            if (col >= columns || row >= rowsFit) {
                continue; // more modules than fit: the rest are left out (and not created)
            }
            DemoModule module = this.modules.get(i);
            int x = left + col * (COLUMN_WIDTH + COLUMN_GAP) + ICON;
            int y = TOP + row * ROW;
            Checkbox box = Checkbox.builder(module.displayName(), this.font)
                    .pos(x, y)
                    .maxWidth(COLUMN_WIDTH - ICON)
                    .selected(true)
                    .onValueChange((c, value) -> updateCreate())
                    .build();
            this.addRenderableWidget(box);
            this.boxes.add(box);
            this.shown.add(module);
            this.icons.add(icon(module));
        }
        this.create = this.addRenderableWidget(Button.builder(Component.translatable("demo.nerolandcore.picker.create"),
                b -> createWorld()).bounds(this.width / 2 - 154, this.height - 28, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> this.onClose())
                .bounds(this.width / 2 + 4, this.height - 28, 150, 20).build());
        updateCreate();
    }

    private static Identifier icon(DemoModule module) {
        try {
            return module.icon();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void updateCreate() {
        if (this.create != null) {
            this.create.active = this.boxes.stream().anyMatch(Checkbox::selected);
        }
    }

    private void createWorld() {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < this.boxes.size(); i++) {
            if (this.boxes.get(i).selected()) {
                ids.add(this.shown.get(i).modId());
            }
        }
        if (!ids.isEmpty()) {
            DemoWorldCreator.create(this.minecraft, ids, this);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.centeredText(this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.translatable("demo.nerolandcore.picker.subtitle"),
                this.width / 2, 30, 0xFFA8A8A8);
        if (this.modules.isEmpty()) {
            graphics.centeredText(this.font, Component.translatable("demo.nerolandcore.picker.empty"),
                    this.width / 2, this.height / 2 - 4, 0xFFFFFFFF);
        }
        for (int i = 0; i < this.boxes.size(); i++) {
            Checkbox box = this.boxes.get(i);
            Identifier icon = this.icons.get(i);
            if (icon != null) {
                int x = box.getX() - ICON;
                int y = box.getY() + (box.getHeight() - 16) / 2;
                graphics.blit(icon, x, y, x + 16, y + 16, 0.0F, 1.0F, 0.0F, 1.0F);
            }
        }
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
*///?} else {
// The Nero Mods Demo needs Minecraft 26.3 or newer; on older lines this class is an empty placeholder.
public final class DemoPickerScreen {

    private DemoPickerScreen() {
    }
}
//?}
