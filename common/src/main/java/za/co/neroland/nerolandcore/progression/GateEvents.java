package za.co.neroland.nerolandcore.progression;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import net.minecraft.resources.Identifier;

import za.co.neroland.nerolandcore.event.ListenerGuard;

/**
 * Change notifications for progression gates. Any mod (e.g. NeroEvents reacting to
 * {@code reached_orbit}) registers a listener; {@link ProgressionGates} fires one
 * whenever a gate opens or closes. Listeners run on the server thread.
 *
 * <p>Since Core 1.15.0 a listener that throws a {@link RuntimeException} is logged once per listener
 * class (without the change) and skipped; the remaining listeners still run.
 */
public final class GateEvents {

    /**
     * A gate state change.
     *
     * @param scope  the gate's scope
     * @param target the scope target — a player UUID string, a team name, or {@code ""} for server scope
     * @param gate   the gate id
     * @param open   {@code true} if it just opened, {@code false} if it closed
     */
    public record GateChange(GateScope scope, String target, Identifier gate, boolean open) {
    }

    private static final List<Consumer<GateChange>> LISTENERS = new CopyOnWriteArrayList<>();

    private GateEvents() {
    }

    /** Register a change listener (runs on the server thread). */
    public static void onChange(Consumer<GateChange> listener) {
        LISTENERS.add(listener);
    }

    static void fire(GateChange change) {
        ListenerGuard.dispatch("GateEvents", LISTENERS, change);
    }
}
