package za.co.neroland.nerolandcore.network;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import za.co.neroland.nerolandcore.platform.Services;
import za.co.neroland.nerolandcore.sideconfig.Channel;
import za.co.neroland.nerolandcore.sideconfig.ClientSideConfig;
import za.co.neroland.nerolandcore.sideconfig.RelativeFace;
import za.co.neroland.nerolandcore.sideconfig.SideConfigComponent;
import za.co.neroland.nerolandcore.sideconfig.SideConfigured;
import za.co.neroland.nerolandcore.sideconfig.SideMode;

/**
 * Server-authoritative wiring for the universal side-config system. Registers the
 * serverbound {@link SideConfigIntentPayload} and clientbound
 * {@link SideConfigSyncPayload} on {@link CoreNetwork}, validates every intent
 * (range + the channel's {@code allow(...)} rules, enforced inside the component),
 * applies it, and syncs the authoritative snapshot back. Clients never mutate
 * routing directly.
 */
public final class SideConfigNetworking {

    private static final double REACH_SQR = 64.0;

    private SideConfigNetworking() {
    }

    /** Register both payloads. Called from {@link CoreNetwork#init()}. */
    public static void register() {
        CoreNetwork.serverbound(SideConfigIntentPayload.TYPE, SideConfigIntentPayload.STREAM_CODEC,
                SideConfigNetworking::handleIntent);
        CoreNetwork.clientbound(SideConfigSyncPayload.TYPE, SideConfigSyncPayload.STREAM_CODEC,
                payload -> ClientSideConfig.accept(payload.pos(), payload.packed()));
    }

    private static void handleIntent(SideConfigIntentPayload payload, ServerPlayer player) {
        ServerLevel level = player.level();
        BlockPos pos = payload.pos();
        if (!level.isLoaded(pos)) {
            return;
        }
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > REACH_SQR) {
            return;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SideConfigured configured)) {
            return;
        }
        SideConfigComponent comp = configured.sideConfig();
        if (comp == null) {
            return;
        }

        Channel channel = boundedChannel(payload.channel());
        switch (payload.op()) {
            case SideConfigIntentPayload.OP_SET_MODE -> {
                if (channel != null) {
                    comp.setMode(channel, boundedFace(payload.face()), boundedMode(payload.mode()));
                }
            }
            case SideConfigIntentPayload.OP_CYCLE -> {
                if (channel != null) {
                    comp.cycle(channel, boundedFace(payload.face()));
                }
            }
            case SideConfigIntentPayload.OP_AUTO_EJECT -> {
                if (channel != null) {
                    comp.setAutoEject(channel, payload.value());
                }
            }
            case SideConfigIntentPayload.OP_AUTO_INPUT -> {
                if (channel != null) {
                    comp.setAutoInput(channel, payload.value());
                }
            }
            case SideConfigIntentPayload.OP_RESET -> comp.resetToPreset();
            case SideConfigIntentPayload.OP_PASTE -> comp.paste(payload.packed());
            case SideConfigIntentPayload.OP_REQUEST -> {
                // no change; just answer with the current snapshot below
            }
            default -> {
                return;
            }
        }
        syncTo(player, pos, comp.config().packAll());
    }

    /** Push an authoritative snapshot of one machine's config to a player. */
    public static void syncTo(ServerPlayer player, BlockPos pos, Map<Channel, Integer> packed) {
        Services.NETWORK.sendToPlayer(player, new SideConfigSyncPayload(pos, packed));
    }

    private static Channel boundedChannel(int ordinal) {
        return ordinal >= 0 && ordinal < Channel.VALUES.length ? Channel.VALUES[ordinal] : null;
    }

    private static RelativeFace boundedFace(int ordinal) {
        return ordinal >= 0 && ordinal < RelativeFace.VALUES.length
                ? RelativeFace.VALUES[ordinal] : RelativeFace.FRONT;
    }

    private static SideMode boundedMode(int ordinal) {
        return ordinal >= 0 && ordinal < SideMode.VALUES.length
                ? SideMode.VALUES[ordinal] : SideMode.DISABLED;
    }
}
