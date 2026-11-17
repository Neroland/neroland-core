package za.co.neroland.nerolandcore.demo;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.data.SavedDataCompat;
import za.co.neroland.nerolandcore.data.SavedDataRecovery;

/**
 * The build-once marker of a Nero Mods Demo world (Core 1.20.0), on the overworld, through
 * {@link SavedDataRecovery}. It holds exactly three things: that this world is a demo, which module ids were
 * selected (in plot order), and whether the build finished. <b>No player data</b>: no names, UUIDs,
 * positions or times.
 */
public final class DemoWorldState extends SavedData {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "demo");

    public static final SavedDataType<DemoWorldState> TYPE =
            SavedDataCompat.type(ID, DemoWorldState::new, codec());

    private boolean demo;
    private final List<String> modules = new ArrayList<>();
    private boolean built;

    public DemoWorldState() {
    }

    /** The world's marker. Only call this for a world known to be (or about to become) a demo world. */
    public static DemoWorldState get(MinecraftServer server) {
        return SavedDataRecovery.get(server.overworld(), TYPE, DemoWorldState::new, ID.toString());
    }

    /** Pushes the marker to the recovery backup at once (used right after the build finishes). */
    public static void backupNow(MinecraftServer server, DemoWorldState state) {
        SavedDataRecovery.backupNow(server.overworld(), TYPE, state, ID.toString());
    }

    public boolean isDemo() {
        return this.demo;
    }

    /** The selected module ids, in plot order. */
    public List<String> modules() {
        return List.copyOf(this.modules);
    }

    public boolean isBuilt() {
        return this.built;
    }

    /** Marks this world a demo of {@code moduleIds} (plot order). Does nothing if it already is one. */
    public void begin(List<String> moduleIds) {
        if (this.demo) {
            return;
        }
        this.demo = true;
        this.modules.clear();
        if (moduleIds != null) {
            this.modules.addAll(moduleIds);
        }
        setDirty();
    }

    /** Records that the build finished; later starts leave the world alone. */
    public void markBuilt() {
        if (!this.built) {
            this.built = true;
            setDirty();
        }
    }

    private static Codec<DemoWorldState> codec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("demo", false).forGetter(DemoWorldState::isDemo),
                Codec.STRING.listOf().optionalFieldOf("modules", List.of()).forGetter(DemoWorldState::modules),
                Codec.BOOL.optionalFieldOf("built", false).forGetter(DemoWorldState::isBuilt)
        ).apply(instance, DemoWorldState::decode));
    }

    private static DemoWorldState decode(boolean demo, List<String> modules, boolean built) {
        DemoWorldState state = new DemoWorldState();
        state.demo = demo;
        state.modules.addAll(modules);
        state.built = built;
        return state;
    }
}
