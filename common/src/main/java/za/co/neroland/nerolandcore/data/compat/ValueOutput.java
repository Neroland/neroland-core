package za.co.neroland.nerolandcore.data.compat;

// Minecraft 1.21.1 - 1.21.5 only (empty on every other line). Those versions have no
// net.minecraft.world.level.storage.ValueOutput: block entities save into a CompoundTag with a registry
// lookup. This look-alike wraps that pair with the subset of the 1.21.6+ API Core's persistence uses, so
// save code (SideConfig, UpgradeContainer, machine/storage block entities) stays identical. The build
// renames the ValueOutput import to this class on those lines (see legacyRenames).

//? if <1.21.6 {
/*import com.mojang.serialization.Codec;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public final class ValueOutput {

    private final CompoundTag tag;
    private final HolderLookup.Provider registries;

    private ValueOutput(CompoundTag tag, HolderLookup.Provider registries) {
        this.tag = tag;
        this.registries = registries;
    }

    public static ValueOutput of(CompoundTag tag, HolderLookup.Provider registries) {
        return new ValueOutput(tag, registries);
    }

    public CompoundTag tag() {
        return this.tag;
    }

    public HolderLookup.Provider registries() {
        return this.registries;
    }

    public void putInt(String key, int value) {
        this.tag.putInt(key, value);
    }

    public void putLong(String key, long value) {
        this.tag.putLong(key, value);
    }

    public void putBoolean(String key, boolean value) {
        this.tag.putBoolean(key, value);
    }

    public void putString(String key, String value) {
        this.tag.putString(key, value);
    }

    public <T> void store(String key, Codec<T> codec, T value) {
        codec.encodeStart(this.registries.createSerializationContext(NbtOps.INSTANCE), value)
                .result()
                .ifPresent(encoded -> this.tag.put(key, encoded));
    }
}
*///?}
