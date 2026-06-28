package za.co.neroland.nerolandcore.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.gas.NeroGasStorage;

/**
 * Forge gas bridge. Owns the shared {@link NeroGasStorage} capability; downstream
 * mods attach providers to their tank/machine block-entities via
 * {@code AttachCapabilitiesEvent}. Registered via {@code META-INF/services}.
 */
public final class ForgeGasLookup implements GasLookup {

    /** The cross-mod Nero gas capability. Downstream tanks attach providers for it. */
    public static final Capability<NeroGasStorage> GAS =
            CapabilityManager.get(new CapabilityToken<>() { });

    @Nullable
    @Override
    public NeroGasStorage find(Level level, BlockPos pos, @Nullable Direction side) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return null;
        }
        return be.getCapability(GAS, side).orElse(null);
    }
}
