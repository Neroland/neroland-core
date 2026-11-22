package za.co.neroland.nerolandcore.data;

import java.util.UUID;

import com.google.gson.JsonElement;

import net.minecraft.server.MinecraftServer;

import org.jetbrains.annotations.Nullable;

/**
 * One system's part of a player's data export (POPIA s23 / GDPR Art. 15 and 20), the read-side twin of
 * {@link PlayerDataEraser} (Core 1.21.0). Registered with {@link PlayerDataExport#register}.
 *
 * <p>Return everything the mod stores about {@code player} as JSON, in plain field names a person can
 * read, or {@code null} when it stores nothing for them. Include the player's own records only: never
 * another player's UUID or name, never a salt, token or hash. Called on the server thread; it must not
 * change anything or log the player.</p>
 */
@FunctionalInterface
public interface PlayerDataExporter {

    @Nullable
    JsonElement export(MinecraftServer server, UUID player);
}
