package za.co.neroland.nerolandcore.data.compat;

// Minecraft 1.21.1 - 1.21.4 only (empty on every other line). On those versions SavedData has an abstract
// NBT save(CompoundTag, Provider); this base implements it with the store's codec, so stores written for the
// codec-only API (1.21.5+) work unchanged. The build rewrites `class X extends SavedData` to extend this class
// on those lines. The codec is bound by data.compat.SavedDataType when vanilla creates or loads the store.

//? if <1.21.5 {
/*import com.mojang.serialization.Codec;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public abstract class CodecSavedData extends net.minecraft.world.level.saveddata.SavedData {

    private Codec<Object> codec;

    @SuppressWarnings("unchecked")
    void bindCodec(Codec<?> storeCodec) {
        this.codec = (Codec<Object>) storeCodec;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        if (codec == null) {
            throw new IllegalStateException(getClass().getName()
                    + " was not created through SavedDataCompat, so it has no codec to save with");
        }
        Tag encoded = codec.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        if (!(encoded instanceof CompoundTag compound)) {
            throw new IllegalStateException(getClass().getName() + " codec must encode to a compound tag");
        }
        tag.merge(compound);
        return tag;
    }
}
*///?}
