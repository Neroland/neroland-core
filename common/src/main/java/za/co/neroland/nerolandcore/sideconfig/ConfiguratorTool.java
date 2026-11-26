package za.co.neroland.nerolandcore.sideconfig;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

/** A handheld configurator that can interact with targets belonging to other mods. */
public interface ConfiguratorTool {

    interface PowerReadout {
        void showPower(ServerPlayer player, long tick);
    }

    interface Target {
        InteractionResult configure(UseOnContext context);
    }

    static InteractionResult useTarget(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var entity = level.getBlockEntity(pos);
        if (level instanceof ServerLevel serverLevel && context.getPlayer() instanceof ServerPlayer player
                && !player.mayInteract(serverLevel, pos)) {
            return InteractionResult.FAIL;
        }
        if (entity instanceof Target target) {
            InteractionResult result = target.configure(context);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        if (context.isSecondaryUseActive() && entity instanceof PowerReadout readout) {
            if (context.getPlayer() instanceof ServerPlayer player) {
                readout.showPower(player, level.getGameTime());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    static InteractionResult useMachine(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof SideConfigured machine)
                || machine.sideConfig().config().channels().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (context.getPlayer() instanceof ServerPlayer player) {
            var side = context.getClickedFace();
            var config = machine.sideConfig().config();
            Channel channel = config.has(Channel.ITEM) ? Channel.ITEM
                    : config.has(Channel.ENERGY) ? Channel.ENERGY : config.channels().keySet().iterator().next();
            SideMode mode = context.isSecondaryUseActive()
                    ? Configurator.read(level, pos, side, channel) : Configurator.cycle(level, pos, side, channel);
            if (mode != null) {
                //? if >=1.21.6 {
                player.sendSystemMessage(Component.translatable("item.nerolandcore.configurator.face",
                        channel.lowerName(), side.getName(), mode.lowerName()), true);
                //?} else {
                /*player.displayClientMessage(Component.translatable("item.nerolandcore.configurator.face",
                        channel.lowerName(), side.getName(), mode.lowerName()), true);
                *///?}
            }
        }
        return InteractionResult.SUCCESS;
    }
}
