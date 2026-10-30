package za.co.neroland.nerolandcore.data;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Version seam for {@link SavedData}. Minecraft changed how saved data is declared and stored three times
 * across the lines Core builds for, so every store and {@link SavedDataRecovery} go through here instead of
 * touching {@code SavedDataType} or the data storage directly:
 *
 * <ul>
 * <li><b>26.x</b> — {@code SavedDataType(Identifier, Supplier, Codec, DataFixTypes)}; files at
 * {@code data/<namespace>/<path>.dat}.</li>
 * <li><b>1.21.5 – 1.21.11</b> — the same record keyed by a plain {@code String}; files at
 * {@code data/<id>.dat}. 1.21.5 – 1.21.10 also wrap the codec in a {@code SavedData.Context} function.</li>
 * <li><b>1.21.1 – 1.21.4</b> — no {@code SavedDataType} at all ({@code SavedData.Factory} + an abstract
 * NBT {@code save}). Core supplies a look-alike {@code data.compat.SavedDataType} and
 * {@code data.compat.CodecSavedData} for these lines; the build renames the imports (see
 * {@code legacyRenames} in the build script), so store source stays identical.</li>
 * </ul>
 *
 * <p>Pre-26.x ids are {@code <namespace>_<path>}, so they never collide with vanilla or other mods.
 */
public final class SavedDataCompat {

    private SavedDataCompat() {
    }

    /** Declares a codec-backed store. Use this instead of {@code new SavedDataType<>(...)}. */
    public static <T extends SavedData> SavedDataType<T> type(Identifier id, Supplier<T> constructor, Codec<T> codec) {
        //? if >=26.1 {
        return new SavedDataType<>(id, constructor, codec, null);
        //?} else {
        /*// Before 26.1 vanilla's DimensionDataStorage.readTagFromDisk runs dataFixType.update(..) with no null
        // check, so a null type throws on every reload of an existing file (vanilla then logs it and hands back
        // an empty store). RANDOM_SEQUENCES is a saved-data type with no data fixes, and the update is a no-op
        // when the file's DataVersion matches the running game, so it only satisfies the call.
        return new SavedDataType<>(legacyId(id), constructor, codec,
                net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
        *///?}
    }

    /** The flat id a store uses before 26.1 ({@code nerolandcore:progression -> nerolandcore_progression}). */
    public static String legacyId(Identifier id) {
        return id.getNamespace() + "_" + id.getPath().replace('/', '_');
    }

    /** The store's file name under a dimension's {@code data/} folder, without {@code .dat}. */
    public static String fileStem(SavedDataType<?> type) {
        //? if >=26.1 {
        return type.id().getNamespace() + "/" + type.id().getPath();
        //?} else {
        /*return type.id();
        *///?}
    }

    /** The store's codec, resolved for {@code level} where Minecraft wants a context. */
    public static <T extends SavedData> Codec<T> codec(SavedDataType<T> type, ServerLevel level) {
        //? if >=1.21.5 && <1.21.11 {
        /*return type.codec().apply(new SavedData.Context(level));
        *///?} else {
        return type.codec();
        //?}
    }

    /** Loads (or creates) the store in {@code level}'s data storage. */
    public static <T extends SavedData> T computeIfAbsent(ServerLevel level, SavedDataType<T> type) {
        //? if >=1.21.5 {
        return level.getDataStorage().computeIfAbsent(type);
        //?} else {
        /*return level.getDataStorage().computeIfAbsent(type.factory(level), type.id());
        *///?}
    }

    /** Replaces the store in {@code level}'s data storage (used when recovery installs a restored copy). */
    public static <T extends SavedData> void set(ServerLevel level, SavedDataType<T> type, T instance) {
        //? if >=1.21.5 {
        level.getDataStorage().set(type, instance);
        //?} else {
        /*level.getDataStorage().set(type.id(), type.attach(instance));
        *///?}
    }
}
