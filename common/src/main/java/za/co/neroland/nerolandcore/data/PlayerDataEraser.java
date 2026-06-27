package za.co.neroland.nerolandcore.data;

import java.util.UUID;

import net.minecraft.server.MinecraftServer;

/**
 * One mod's contribution to the shared per-player erasure. Every Core-storing
 * system (and downstream mod) registers an eraser with {@link PlayerDataErasure};
 * a single erase request then purges the player across all of them.
 */
@FunctionalInterface
public interface PlayerDataEraser {

    /** Remove everything this system stores for {@code player}. */
    void erase(MinecraftServer server, UUID player);
}
