package za.co.neroland.nerolandcore.data.compat;

// Minecraft 1.21.1 - 1.21.5 only (empty on every other line). Read-side twin of data.compat.ValueOutput:
// wraps the CompoundTag + registry lookup a pre-1.21.6 block entity loads from, with the subset of the
// 1.21.6+ ValueInput API Core uses. The build renames the ValueInput import to this class on those lines.

//? if <1.21.6 {
/*import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public final class ValueInput {

    private final CompoundTag tag;
    private final HolderLookup.Provider registries;

    private ValueInput(CompoundTag tag, HolderLookup.Provider registries) {
        this.tag = tag;
        this.registries = registries;
    }

    public static ValueInput of(CompoundTag tag, HolderLookup.Provider registries) {
        return new ValueInput(tag, registries);
    }

    public CompoundTag tag() {
        return this.tag;
    }

    public HolderLookup.Provider registries() {
        return this.registries;
    }

*///?}
//? if >=1.21.5 && <1.21.6 {
/*    public int getIntOr(String key, int fallback) {
        return this.tag.getIntOr(key, fallback);
    }

    public long getLongOr(String key, long fallback) {
        return this.tag.getLongOr(key, fallback);
    }

    public boolean getBooleanOr(String key, boolean fallback) {
        return this.tag.getBooleanOr(key, fallback);
    }

    public String getStringOr(String key, String fallback) {
        return this.tag.getStringOr(key, fallback);
    }
*///?}
//? if <1.21.5 {
/*    public int getIntOr(String key, int fallback) {
        return this.tag.contains(key, Tag.TAG_ANY_NUMERIC) ? this.tag.getInt(key) : fallback;
    }

    public long getLongOr(String key, long fallback) {
        return this.tag.contains(key, Tag.TAG_ANY_NUMERIC) ? this.tag.getLong(key) : fallback;
    }

    public boolean getBooleanOr(String key, boolean fallback) {
        return this.tag.contains(key, Tag.TAG_ANY_NUMERIC) ? this.tag.getBoolean(key) : fallback;
    }

    public String getStringOr(String key, String fallback) {
        return this.tag.contains(key, Tag.TAG_STRING) ? this.tag.getString(key) : fallback;
    }
*///?}
//? if <1.21.6 {
/*    public <T> Optional<T> read(String key, Codec<T> codec) {
        Tag value = this.tag.get(key);
        if (value == null) {
            return Optional.empty();
        }
        return codec.parse(this.registries.createSerializationContext(NbtOps.INSTANCE), value).result();
    }
}
*///?}
