package za.co.neroland.nerolandcore.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import za.co.neroland.nerolandcore.config.ConfigManager;
import za.co.neroland.nerolandcore.config.ConfigSchema;
import za.co.neroland.nerolandcore.config.ConfigValue;
import za.co.neroland.nerolandcore.network.CoreNetwork;

/**
 * The shared {@code /neroland} command tree, built once with vanilla Brigadier so
 * it is identical on every loader. Each loader entry point registers it from its
 * own command-registration event ({@code RegisterCommandsEvent} on NeoForge/Forge,
 * {@code CommandRegistrationCallback} on Fabric).
 *
 * <ul>
 *   <li>{@code /neroland config reload} — re-read every registered schema from disk
 *       (op level 2);</li>
 *   <li>{@code /neroland config list} — print each schema's values (current +
 *       whether server-authoritative).</li>
 * </ul>
 */
public final class CoreCommands {

    private CoreCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("neroland")
                .then(Commands.literal("config")
                        .then(Commands.literal("reload")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .executes(ctx -> {
                                    ConfigManager.reloadAll();
                                    if (ctx.getSource().getServer() != null) {
                                        CoreNetwork.syncAll(ctx.getSource().getServer());
                                    }
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("[Neroland] Config reloaded and re-synced to clients."), true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("list")
                                .executes(ctx -> {
                                    listConfig(ctx.getSource());
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }

    private static void listConfig(CommandSourceStack source) {
        for (ConfigSchema schema : ConfigManager.schemas()) {
            source.sendSuccess(() -> Component.literal("§e" + schema.modId() + "§r"), false);
            for (ConfigValue<?> value : schema.values()) {
                String line = "  " + value.key() + " = " + value.asString()
                        + (value.serverAuthoritative() ? " §8(server)§r" : "");
                source.sendSuccess(() -> Component.literal(line), false);
            }
        }
    }
}
