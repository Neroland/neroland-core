package za.co.neroland.nerolandcore.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.config.ConfigManager;
import za.co.neroland.nerolandcore.config.ConfigSchema;
import za.co.neroland.nerolandcore.config.ConfigValue;
import za.co.neroland.nerolandcore.network.CoreNetwork;
import za.co.neroland.nerolandcore.progression.Gate;
import za.co.neroland.nerolandcore.progression.GateDefinitions;
import za.co.neroland.nerolandcore.progression.ProgressionGates;

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
                                })))
                .then(Commands.literal("gate")
                        .then(Commands.literal("list")
                                .executes(ctx -> {
                                    listGates(ctx.getSource());
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("open")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.argument("gate", StringArgumentType.string())
                                        .executes(ctx -> setGate(ctx, true))))
                        .then(Commands.literal("close")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.argument("gate", StringArgumentType.string())
                                        .executes(ctx -> setGate(ctx, false))))));
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

    private static void listGates(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        for (Gate gate : GateDefinitions.current().values()) {
            String state = player != null
                    ? (ProgressionGates.isOpen(player, gate.id()) ? " §a[open]§r" : " §8[closed]§r")
                    : "";
            String line = "  " + gate.id() + " §7(" + gate.scope().name().toLowerCase() + ")§r" + state;
            source.sendSuccess(() -> Component.literal(line), false);
        }
    }

    private static int setGate(CommandContext<CommandSourceStack> ctx, boolean open) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("Run this as a player."));
            return 0;
        }
        Identifier gate = parseGateId(StringArgumentType.getString(ctx, "gate"));
        boolean changed = open ? ProgressionGates.open(player, gate) : ProgressionGates.close(player, gate);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "[Neroland] Gate " + gate + (open ? " opened" : " closed")
                        + (changed ? "." : " (no change).")), true);
        return Command.SINGLE_SUCCESS;
    }

    /** Accept a bare path ("reached_orbit") as nerolandcore-namespaced, or a full "ns:path". */
    private static Identifier parseGateId(String raw) {
        if (raw.indexOf(':') < 0) {
            return Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, raw);
        }
        return Identifier.parse(raw);
    }
}
