package za.co.neroland.nerolandcore.fluid;

import java.util.function.BooleanSupplier;

/**
 * One tank as it is offered to a loader's standard fluid capability: the <b>raw</b> storage plus the
 * two permissions that face allows right now.
 *
 * <p>The split matters for transactions. A loader's fluid handler is transactional, and Core's tanks
 * are not, so the adapters apply a mutation immediately and undo it if the transaction aborts. If the
 * storage handed to an adapter were already wrapped in a side-config gate, that undo would go through
 * the gate too — and on an input-only face the undo of an insert is a drain, which the gate refuses.
 * The mutation would then stand, creating fluid out of a rolled-back transaction (and destroying it in
 * the mirror case). So gating belongs here, checked per operation, while snapshot and rollback speak
 * to the raw storage.
 *
 * <p>The suppliers are read on every operation, so a player changing a face takes effect immediately
 * without the capability being re-resolved.
 */
public record GatedFluidView(NeroFluidStorage storage, BooleanSupplier canInsert, BooleanSupplier canExtract) {

    /** An ungated view — for storage with no side configuration, such as Core's own tanks. */
    public static GatedFluidView open(NeroFluidStorage storage) {
        return new GatedFluidView(storage, () -> true, () -> true);
    }

    public boolean insertable() {
        return this.canInsert.getAsBoolean();
    }

    public boolean extractable() {
        return this.canExtract.getAsBoolean();
    }
}
