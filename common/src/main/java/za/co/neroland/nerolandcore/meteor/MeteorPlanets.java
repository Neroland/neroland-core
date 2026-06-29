package za.co.neroland.nerolandcore.meteor;

import java.util.Objects;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import org.jetbrains.annotations.Nullable;

/**
 * The Nerospace-facing seam for the "which planet is this grind happening on?"
 * lookup the resolver needs for planet-bound materials and the planet-bias
 * multiplier. Core owns the contract; <b>it does not depend on Nerospace</b>.
 *
 * <p>This mirrors Core's other inversion points (currency, reputation): a default
 * provider returns {@code null} ("no planet context / off-world"), so planet-bound
 * entries simply drop out of the pool when Nerospace is absent. When present,
 * Nerospace installs its own provider once at init via {@link #setProvider}:
 *
 * <pre>{@code
 * MeteorPlanets.setProvider(player ->
 *     NerospacePlanets.dimensionToPlanetId(player.level().dimension()));
 * }</pre>
 */
public final class MeteorPlanets {

    /** Resolves the planet a player is currently grinding on, or {@code null} if none / unknown. */
    @FunctionalInterface
    public interface PlanetContext {
        @Nullable
        Identifier currentPlanet(ServerPlayer player);
    }

    /** Default: no planet context — planet-bound materials are off-world and excluded. */
    private static volatile PlanetContext provider = player -> null;

    private MeteorPlanets() {
    }

    /** Install the planet-lookup provider (Nerospace calls this once at init). */
    public static void setProvider(PlanetContext newProvider) {
        provider = Objects.requireNonNull(newProvider, "provider");
    }

    public static PlanetContext provider() {
        return provider;
    }

    /** The planet id the player is grinding on, or {@code null} when off-world / Nerospace absent. */
    @Nullable
    public static Identifier currentPlanet(ServerPlayer player) {
        return provider.currentPlanet(player);
    }
}
