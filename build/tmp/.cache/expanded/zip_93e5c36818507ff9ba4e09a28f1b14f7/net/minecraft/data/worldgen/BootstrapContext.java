package net.minecraft.data.worldgen;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface BootstrapContext<T> {
    Holder.Reference<T> register(ResourceKey<T> key, T value, Lifecycle registryLifecycle);

    default Holder.Reference<T> register(ResourceKey<T> key, T value) {
        return this.register(key, value, Lifecycle.stable());
    }

    <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> registryKey);

    default <S> java.util.Optional<net.minecraft.core.HolderLookup.RegistryLookup<S>> registryLookup(ResourceKey<? extends Registry<? extends S>> registry) { return java.util.Optional.empty(); }
}
