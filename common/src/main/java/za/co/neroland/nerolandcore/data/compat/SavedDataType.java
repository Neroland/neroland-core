package za.co.neroland.nerolandcore.data.compat;

// Minecraft 1.21.1 - 1.21.4 only (empty on every other line). Those versions have no
// net.minecraft.world.level.saveddata.SavedDataType; this record has the same shape as the 1.21.11 one so
// store code is written once. The build rewrites `import ...saveddata.SavedDataType;` to this class on
// those lines. Stores are created through za.co.neroland.nerolandcore.data.SavedDataCompat.

//? if <1.21.5 {
/*import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import za.co.neroland.nerolandcore.data.SavedDataCompat;

public record SavedDataType<T extends SavedData>(String id, Supplier<T> constructor, Codec<T> codec,
        DataFixTypes dataFixType) {

    // The vanilla factory for this store: new instances and loaded ones both get the codec bound, so
    // CodecSavedData.save can write them. A decode failure throws; vanilla logs it and hands back a fresh
    // store, which SavedDataRecovery's first-access check then catches and restores from backup.
*///?}
//? if >=1.20.5 && <1.21.5 {
/*    public SavedData.Factory<T> factory(net.minecraft.server.level.ServerLevel level) {
        return new SavedData.Factory<>(
                () -> attach(constructor.get()),
                (tag, registries) -> attach(decode(tag, registries)),
                dataFixType);
    }
*///?} else if <1.20.5 {
/*    public T read(CompoundTag tag, HolderLookup.Provider registries) {
        return attach(decode(tag, registries), registries);
    }

    public T attach(T instance, HolderLookup.Provider registries) {
        if (instance instanceof CodecSavedData bound) {
            bound.bindRegistries(registries);
        }
        return attach(instance);
    }
*///?}
//? if <1.21.5 {
/*    private T decode(CompoundTag tag, HolderLookup.Provider registries) {
        return codec.parse(SavedDataCompat.nbtOps(registries), tag).result()
                .orElseThrow(() -> new IllegalStateException("Saved data could not be decoded"));
    }

    public T attach(T instance) {
        if (instance instanceof CodecSavedData bound) {
            bound.bindCodec(codec);
        }
        return instance;
    }
}
*///?}
