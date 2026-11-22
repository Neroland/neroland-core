package za.co.neroland.nerolandcore.energy;

import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * One mod's answer to "does this item stack hold Nero energy?" (Core 1.21.0). Registered with
 * {@link ItemEnergyLookup#register}; consulted in registration order by {@link ItemEnergyLookup#find}.
 *
 * <p>The returned storage reads and writes the stack itself (its data components), so a change made
 * through it is a change to that stack. Return {@code null} for stacks the provider does not own. A
 * provider must be cheap and side-effect free when it returns {@code null}; it is called on the server
 * and client thread alike and must never touch the world.</p>
 */
@FunctionalInterface
public interface ItemEnergyProvider {

    /** The stack's energy view, or {@code null} if this provider does not handle the stack. */
    @Nullable
    NeroEnergyStorage find(ItemStack stack);
}
