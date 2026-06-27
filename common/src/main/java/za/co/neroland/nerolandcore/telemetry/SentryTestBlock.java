package za.co.neroland.nerolandcore.telemetry;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * A hidden developer diagnostic block: deliberately left OUT of the creative menu (not added to
 * {@code ModItems.addToCreativeTab()}) and uncraftable, so the only way to get one is
 * {@code /give @s nerolandcore:sentry_test}. Placing it fires a single synthetic Sentry event through
 * {@link NerolandCoreTelemetry#sendTestEvent(String)} — the easiest way to confirm end-to-end that error
 * reporting reaches the dashboard on a real (production) jar, on every loader and MC version.
 *
 * <p>Everything happens server-side; the placer gets a chat line saying whether the event was dispatched
 * or skipped because telemetry is opted out. The synthetic exception originates in Neroland Core code, so
 * it passes the package-only {@code beforeSend} filter; per-session de-duplication means repeat placements
 * in one session collapse to one event (restart to test again).</p>
 */
@org.jetbrains.annotations.ApiStatus.Internal
public class SentryTestBlock extends Block {

    public SentryTestBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) {
            return;
        }
        boolean dispatched = NerolandCoreTelemetry.sendTestEvent("placed at " + pos.toShortString());
        if (placer instanceof Player player) {
            player.sendSystemMessage(Component.translatable(dispatched
                    ? "message.nerolandcore.sentry_test.sent"
                    : "message.nerolandcore.sentry_test.disabled"));
        }
    }
}
