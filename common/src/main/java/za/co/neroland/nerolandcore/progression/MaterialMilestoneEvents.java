package za.co.neroland.nerolandcore.progression;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import net.minecraft.resources.Identifier;

/** Server-thread notifications for typed material milestone changes. */
public final class MaterialMilestoneEvents {

    public record Change(GateScope scope, String target, Identifier milestone, Identifier material,
            MaterialObservation observation, boolean present) {
    }

    private static final List<Consumer<Change>> LISTENERS = new CopyOnWriteArrayList<>();

    private MaterialMilestoneEvents() {
    }

    public static void onChange(Consumer<Change> listener) {
        LISTENERS.add(listener);
    }

    static void fire(Change change) {
        LISTENERS.forEach(listener -> listener.accept(change));
    }
}
