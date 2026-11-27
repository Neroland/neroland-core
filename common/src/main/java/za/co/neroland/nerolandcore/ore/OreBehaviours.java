package za.co.neroland.nerolandcore.ore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

/**
 * Registry of {@link OreBehaviour}s keyed by ore block id (for example {@code nerolandcore:hematite_ore}).
 * One behaviour per ore: a later registration replaces an earlier one.
 *
 * @since 1.25.0
 */
public final class OreBehaviours {

    private static final Map<Identifier, OreBehaviour> BEHAVIOURS = new ConcurrentHashMap<>();

    private OreBehaviours() {
    }

    /** Attach {@code behaviour} to the Core ore block {@code oreId}. Call once during mod init. */
    public static void register(Identifier oreId, OreBehaviour behaviour) {
        BEHAVIOURS.put(oreId, behaviour);
    }

    /** The behaviour attached to {@code oreId}, or {@code null}. */
    @Nullable
    public static OreBehaviour get(Identifier oreId) {
        return BEHAVIOURS.get(oreId);
    }
}
