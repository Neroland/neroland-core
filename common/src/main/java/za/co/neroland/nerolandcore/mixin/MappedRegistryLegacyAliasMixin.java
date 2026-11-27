package za.co.neroland.nerolandcore.mixin;

import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import za.co.neroland.nerolandcore.registry.LegacyIds;

/**
 * World compatibility for ids that moved into Core ({@link LegacyIds}): a lookup of an old id such as
 * {@code nerospace:hematite_ore} in the BLOCK, ITEM or BLOCK_ENTITY_TYPE registry answers with the
 * {@code nerolandcore:} entry. Chunk block states, item stacks in containers and inventories, block
 * entities, and recipes, loot tables, tags and advancements that name the old id all resolve through these
 * lookups, on every loader and Minecraft line. NeoForge's registry aliases and Forge's missing-mappings
 * event are added on top by those loaders.
 *
 * <p>An old id is only redirected while nothing is registered under it. Every other lookup is untouched.
 * {@code require = 0}: if a signature does not match on some line the injector is skipped, and the old
 * behaviour (the block is lost) applies there.
 */
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryLegacyAliasMixin<T> implements Registry<T> {

    //? if >=1.21.2 {
    @Inject(method = "get(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void nerolandcore$aliasHolderByKey(ResourceKey<T> key, CallbackInfoReturnable<Optional<Holder.Reference<T>>> cir) {
        Identifier coreId = nerolandcore$coreIdFor(key.identifier());
        if (coreId != null) {
            cir.setReturnValue(this.get(ResourceKey.create(this.key(), coreId)));
        }
    }

    @Inject(method = "get(Lnet/minecraft/resources/Identifier;)Ljava/util/Optional;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void nerolandcore$aliasHolderById(Identifier id, CallbackInfoReturnable<Optional<Holder.Reference<T>>> cir) {
        Identifier coreId = nerolandcore$coreIdFor(id);
        if (coreId != null) {
            cir.setReturnValue(this.get(ResourceKey.create(this.key(), coreId)));
        }
    }

    @Inject(method = "getValue(Lnet/minecraft/resources/Identifier;)Ljava/lang/Object;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void nerolandcore$aliasValueById(Identifier id, CallbackInfoReturnable<T> cir) {
        Identifier coreId = nerolandcore$coreIdFor(id);
        if (coreId != null) {
            cir.setReturnValue(this.getValue(coreId));
        }
    }

    @Inject(method = "getValue(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void nerolandcore$aliasValueByKey(ResourceKey<T> key, CallbackInfoReturnable<T> cir) {
        Identifier coreId = nerolandcore$coreIdFor(key.identifier());
        if (coreId != null) {
            cir.setReturnValue(this.getValue(coreId));
        }
    }

    //?} else {
    /*@Inject(method = "getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void nerolandcore$aliasHolderByKey(ResourceKey<T> key, CallbackInfoReturnable<Optional<Holder.Reference<T>>> cir) {
        Identifier coreId = nerolandcore$coreIdFor(key.identifier());
        if (coreId != null) {
            cir.setReturnValue(this.getHolder(ResourceKey.create(this.key(), coreId)));
        }
    }

    //? if >=1.20.5 {
    @Inject(method = "getHolder(Lnet/minecraft/resources/Identifier;)Ljava/util/Optional;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void nerolandcore$aliasHolderById(Identifier id, CallbackInfoReturnable<Optional<Holder.Reference<T>>> cir) {
        Identifier coreId = nerolandcore$coreIdFor(id);
        if (coreId != null) {
            cir.setReturnValue(this.getHolder(ResourceKey.create(this.key(), coreId)));
        }
    }
    //?}

    @Inject(method = "get(Lnet/minecraft/resources/Identifier;)Ljava/lang/Object;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void nerolandcore$aliasValueById(Identifier id, CallbackInfoReturnable<T> cir) {
        Identifier coreId = nerolandcore$coreIdFor(id);
        if (coreId != null) {
            cir.setReturnValue(this.get(coreId));
        }
    }

    @Inject(method = "get(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void nerolandcore$aliasValueByKey(ResourceKey<T> key, CallbackInfoReturnable<T> cir) {
        Identifier coreId = nerolandcore$coreIdFor(key.identifier());
        if (coreId != null) {
            cir.setReturnValue(this.get(coreId));
        }
    }

    *///?}
    /**
     * The {@code nerolandcore:} id for {@code id} if it is a moved id in this registry and nothing is
     * registered under it; otherwise {@code null}.
     */
    private Identifier nerolandcore$coreIdFor(Identifier id) {
        if (id == null || LegacyIds.CORE.equals(id.getNamespace())) {
            return null;
        }
        ResourceKey<?> registryKey = this.key();
        String path;
        if (registryKey == Registries.BLOCK || registryKey == Registries.ITEM) {
            path = LegacyIds.blockOrItem(id.getNamespace(), id.getPath());
        } else if (registryKey == Registries.BLOCK_ENTITY_TYPE) {
            path = LegacyIds.blockEntityType(id.getNamespace(), id.getPath());
        } else {
            return null;
        }
        if (path == null || this.containsKey(id)) {
            return null;
        }
        return Identifier.fromNamespaceAndPath(LegacyIds.CORE, path);
    }
}
