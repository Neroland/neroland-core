package za.co.neroland.nerolandcore.progression;

import java.util.Locale;

import com.mojang.serialization.Codec;

/** Server-observed evidence that can satisfy a typed per-material milestone. */
public enum MaterialObservation {
    OWNER_MOD,
    PLANET_VISIT,
    PLAYER_PICKUP,
    ADMIN;

    public static final Codec<MaterialObservation> CODEC = Codec.STRING.xmap(
            value -> valueOf(value.trim().toUpperCase(Locale.ROOT)),
            value -> value.name().toLowerCase(Locale.ROOT));
}
