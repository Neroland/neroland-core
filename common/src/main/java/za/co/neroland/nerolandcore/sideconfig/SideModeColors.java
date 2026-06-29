package za.co.neroland.nerolandcore.sideconfig;

/**
 * The colour-coding shared by every Side Config surface (the GUI widget, and any
 * HUD a content mod adds): disabled grey, input blue, output orange, I/O green,
 * push yellow — so a face's state reads at a glance and means the same thing
 * everywhere. ARGB ints.
 */
public final class SideModeColors {

    public static final int DISABLED = 0xFF555555;
    public static final int INPUT = 0xFF3C78F0;
    public static final int OUTPUT = 0xFFE0852A;
    public static final int IO = 0xFF3CB043;
    public static final int PUSH = 0xFFE0C020;

    private SideModeColors() {
    }

    public static int of(SideMode mode) {
        return switch (mode) {
            case DISABLED -> DISABLED;
            case INPUT -> INPUT;
            case OUTPUT -> OUTPUT;
            case IO -> IO;
            case PUSH -> PUSH;
        };
    }
}
