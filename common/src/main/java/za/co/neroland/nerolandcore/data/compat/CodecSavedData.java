package za.co.neroland.nerolandcore.data.compat;

// Minecraft 1.21.1 - 1.21.4 only (empty on every other line). On those versions SavedData has an abstract
// NBT save(CompoundTag, Provider); this base implements it with the store's codec, so stores written for the
// codec-only API (1.21.5+) work unchanged. The build rewrites `class X extends SavedData` to extend this class
// on those lines. The codec is bound by data.compat.SavedDataType when vanilla creates or loads the store.

//? if <1.21.5 {
/*import com.mojang.serialization.Codec;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import za.co.neroland.nerolandcore.data.SavedDataCompat;

public abstract class CodecSavedData extends net.minecraft.world.level.saveddata.SavedData {

    private Codec<Object> codec;

    @SuppressWarnings("unchecked")
    void bindCodec(Codec<?> storeCodec) {
        this.codec = (Codec<Object>) storeCodec;
    }

*///?}
//? if >=1.20.5 && <1.21.5 {
/*    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return encode(tag, registries);
    }
*///?} else if <1.20.5 {
/*    private HolderLookup.Provider registries;

    void bindRegistries(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return encode(tag, registries);
    }
*///?}
//? if <1.21.5 {
/*    private CompoundTag encode(CompoundTag tag, HolderLookup.Provider registries) {
        if (codec == null) {
            throw new IllegalStateException(getClass().getName()
                    + " was not created through SavedDataCompat, so it has no codec to save with");
        }
        Tag encoded = codec.encodeStart(SavedDataCompat.nbtOps(registries), this).result()
                .orElseThrow(() -> new IllegalStateException("Saved data could not be encoded"));
        if (!(encoded instanceof CompoundTag compound)) {
            throw new IllegalStateException(getClass().getName() + " codec must encode to a compound tag");
        }
        tag.merge(compound);
        return tag;
    }
}
*///?}
