package za.co.neroland.nerolandcore.progression;

import java.util.Collection;
import java.util.Set;

import net.minecraft.resources.Identifier;

/** Read-only client mirror of the receiving player's resolved material milestones. */
public final class ClientMaterialMilestones {

    private static volatile Set<String> values = Set.of();

    private ClientMaterialMilestones() {
    }

    public static void accept(Collection<String> snapshot) {
        values = Set.copyOf(snapshot);
    }

    public static boolean isObserved(Identifier milestone, Identifier material) {
        return values.contains(key(milestone, material));
    }

    static String key(Identifier milestone, Identifier material) {
        return milestone + "\u0000" + material;
    }
}
