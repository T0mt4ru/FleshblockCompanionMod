package net.minecraft.commands;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;

public interface CommandBuildContext extends HolderLookup.Provider {
    static CommandBuildContext simple(final HolderLookup.Provider provider, final FeatureFlagSet enabledFeatures) {
        return new CommandBuildContext() {
            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
                return provider.listRegistries();
            }

            @Override
            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> p_323477_) {
                return provider.lookup(p_323477_).map(p_323674_ -> p_323674_.filterFeatures(enabledFeatures));
            }
        };
    }
}
