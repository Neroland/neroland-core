package za.co.neroland.nerolandcore.protection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ClaimChecksTest {

    @AfterEach
    void reset() {
        ClaimChecks.clearForTests();
    }

    private static ClaimCheck fixed(boolean protectedLand, boolean allow) {
        return new ClaimCheck() {
            @Override
            public boolean isProtected(ServerLevel level, BlockPos pos) {
                return protectedLand;
            }

            @Override
            public boolean mayInteract(ServerPlayer player, ServerLevel level, BlockPos pos) {
                return allow;
            }
        };
    }

    @Test
    void noChecksMeansOpenLand() {
        assertFalse(ClaimChecks.active());
        assertFalse(ClaimChecks.isProtected(null, null));
        assertTrue(ClaimChecks.mayInteract(null, null, null));
    }

    @Test
    void anyProtectingCheckWinsAndAnyDenialWins() {
        ClaimChecks.register(fixed(false, true));
        ClaimChecks.register(fixed(true, false));
        assertTrue(ClaimChecks.active());
        assertTrue(ClaimChecks.isProtected(null, null));
        assertFalse(ClaimChecks.mayInteract(null, null, null));
    }

    @Test
    void permissiveChecksStayPermissive() {
        ClaimChecks.register(fixed(false, true));
        ClaimChecks.register(fixed(false, true));
        assertFalse(ClaimChecks.isProtected(null, null));
        assertTrue(ClaimChecks.mayInteract(null, null, null));
    }

    @Test
    void throwingCheckFailsClosed() {
        ClaimChecks.register(new ClaimCheck() {
            @Override
            public boolean isProtected(ServerLevel level, BlockPos pos) {
                throw new IllegalStateException("boom");
            }

            @Override
            public boolean mayInteract(ServerPlayer player, ServerLevel level, BlockPos pos) {
                throw new IllegalStateException("boom");
            }
        });
        assertTrue(ClaimChecks.isProtected(null, null));
        assertFalse(ClaimChecks.mayInteract(null, null, null));
    }
}
