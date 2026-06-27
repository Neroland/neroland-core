package za.co.neroland.nerolandcore.progression;

import java.util.Locale;

import com.mojang.serialization.Codec;

/**
 * The ownership scope of a progression gate.
 *
 * <ul>
 *   <li>{@link #SERVER} — one shared flag for the whole world (e.g. a server-wide milestone);</li>
 *   <li>{@link #TEAM} — shared by a vanilla scoreboard team (co-op progression);</li>
 *   <li>{@link #PLAYER} — owned per player.</li>
 * </ul>
 */
public enum GateScope {
    PLAYER,
    TEAM,
    SERVER;

    public static final Codec<GateScope> CODEC = Codec.STRING.xmap(
            s -> GateScope.valueOf(s.trim().toUpperCase(Locale.ROOT)),
            scope -> scope.name().toLowerCase(Locale.ROOT));
}
