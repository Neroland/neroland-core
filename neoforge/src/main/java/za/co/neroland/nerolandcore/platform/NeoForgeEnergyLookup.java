package za.co.neroland.nerolandcore.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
//? if >=1.21.9 {
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
//?} else {
/*import net.neoforged.neoforge.energy.IEnergyStorage;
*///?}

import org.jetbrains.annotations.Nullable;

import za.co.neroland.nerolandcore.NerolandCoreCommon;
import za.co.neroland.nerolandcore.energy.EnergyConversions;
import za.co.neroland.nerolandcore.energy.NeroEnergyStorage;

/**
 * NeoForge energy bridge. Owns the shared {@code nerolandcore:energy}
 * {@link BlockCapability} over {@link NeroEnergyStorage}; downstream mods register
 * their machine block-entities against {@link #ENERGY} during
 * {@code RegisterCapabilitiesEvent}. Registered via {@code META-INF/services}.
 *
 * <p>When a block exposes no Nero energy, the lookup falls back to the standard
 * NeoForge energy capability ({@code Capabilities.Energy.BLOCK}, Forge Energy) and
 * adapts it to {@link NeroEnergyStorage} with the config-driven NE↔FE conversion.
 * This is what lets Universal Pipes connect to — and batteries push into —
 * third-party FE blocks such as Energized Power cables and machines.</p>
 */
public final class NeoForgeEnergyLookup implements EnergyLookup {

    /** The cross-mod Nero energy capability. Downstream machines register providers for it. */
    public static final BlockCapability<NeroEnergyStorage, Direction> ENERGY =
            BlockCapability.createSided(
                    Identifier.fromNamespaceAndPath(NerolandCoreCommon.MOD_ID, "energy"),
                    NeroEnergyStorage.class);

    @Nullable
    @Override
    public NeroEnergyStorage find(Level level, BlockPos pos, @Nullable Direction side) {
        NeroEnergyStorage nero = level.getCapability(ENERGY, pos, side);
        if (nero != null) {
            return nero;
        }
        // Standard-FE fallback: adapt any third-party Forge Energy block to the Nero surface.
        //? if >=1.21.9 {
        EnergyHandler forgeEnergy = Capabilities.Energy.BLOCK.getCapability(level, pos, null, null, side);
        //?} else {
        /*IEnergyStorage forgeEnergy = Capabilities.EnergyStorage.BLOCK.getCapability(level, pos, null, null, side);
        *///?}
        return forgeEnergy == null ? null : new EnergyHandlerAdapter(forgeEnergy);
    }

    //? if >=1.21.9 {
    // Adapts a standard NeoForge EnergyHandler (FE) to NeroEnergyStorage (NE).
    // Amounts convert via EnergyConversions; simulation maps to an uncommitted transaction.
    private static final class EnergyHandlerAdapter implements NeroEnergyStorage {

        private final EnergyHandler handler;

        private EnergyHandlerAdapter(EnergyHandler handler) {
            this.handler = handler;
        }

        @Override
        public long getAmount() {
            return EnergyConversions.forgeToNero(this.handler.getAmountAsLong());
        }

        @Override
        public long getCapacity() {
            return EnergyConversions.forgeToNero(this.handler.getCapacityAsInt());
        }

        @Override
        public long insert(long maxAmount, boolean simulate) {
            int fe = clampToInt(EnergyConversions.neroToForge(maxAmount));
            if (fe <= 0) {
                return 0;
            }
            try (Transaction tx = Transaction.openRoot()) {
                long inserted = this.handler.insert(fe, tx);
                if (!simulate) {
                    tx.commit();
                }
                return EnergyConversions.forgeToNero(inserted);
            }
        }

        @Override
        public long extract(long maxAmount, boolean simulate) {
            int fe = clampToInt(EnergyConversions.neroToForge(maxAmount));
            if (fe <= 0) {
                return 0;
            }
            try (Transaction tx = Transaction.openRoot()) {
                long extracted = this.handler.extract(fe, tx);
                if (!simulate) {
                    tx.commit();
                }
                return EnergyConversions.forgeToNero(extracted);
            }
        }

        private static int clampToInt(long value) {
            return (int) Math.max(0, Math.min(Integer.MAX_VALUE, value));
        }
    }
    //?} else {
    /*// Adapts a standard (pre-1.21.9) NeoForge IEnergyStorage (FE) to NeroEnergyStorage (NE). Amounts
    // convert via EnergyConversions; simulation maps straight onto IEnergyStorage's simulate flag.
    private static final class EnergyHandlerAdapter implements NeroEnergyStorage {

        private final IEnergyStorage handler;

        private EnergyHandlerAdapter(IEnergyStorage handler) {
            this.handler = handler;
        }

        @Override
        public long getAmount() {
            return EnergyConversions.forgeToNero(this.handler.getEnergyStored());
        }

        @Override
        public long getCapacity() {
            return EnergyConversions.forgeToNero(this.handler.getMaxEnergyStored());
        }

        @Override
        public long insert(long maxAmount, boolean simulate) {
            int fe = clampToInt(EnergyConversions.neroToForge(maxAmount));
            if (fe <= 0) {
                return 0;
            }
            return EnergyConversions.forgeToNero(this.handler.receiveEnergy(fe, simulate));
        }

        @Override
        public long extract(long maxAmount, boolean simulate) {
            int fe = clampToInt(EnergyConversions.neroToForge(maxAmount));
            if (fe <= 0) {
                return 0;
            }
            return EnergyConversions.forgeToNero(this.handler.extractEnergy(fe, simulate));
        }

        private static int clampToInt(long value) {
            return (int) Math.max(0, Math.min(Integer.MAX_VALUE, value));
        }
    }
    *///?}
}
