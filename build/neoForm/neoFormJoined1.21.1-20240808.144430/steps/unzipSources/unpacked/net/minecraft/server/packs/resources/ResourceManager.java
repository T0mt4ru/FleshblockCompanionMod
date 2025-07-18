package net.minecraft.server.packs.resources;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;

public interface ResourceManager extends ResourceProvider {
    Set<String> getNamespaces();

    List<Resource> getResourceStack(ResourceLocation location);

    Map<ResourceLocation, Resource> listResources(String path, Predicate<ResourceLocation> filter);

    Map<ResourceLocation, List<Resource>> listResourceStacks(String path, Predicate<ResourceLocation> filter);

    Stream<PackResources> listPacks();

    public static enum Empty implements ResourceManager {
        INSTANCE;

        @Override
        public Set<String> getNamespaces() {
            return Set.of();
        }

        @Override
        public Optional<Resource> getResource(ResourceLocation p_215576_) {
            return Optional.empty();
        }

        @Override
        public List<Resource> getResourceStack(ResourceLocation p_215568_) {
            return List.of();
        }

        @Override
        public Map<ResourceLocation, Resource> listResources(String p_215570_, Predicate<ResourceLocation> p_215571_) {
            return Map.of();
        }

        @Override
        public Map<ResourceLocation, List<Resource>> listResourceStacks(String p_215573_, Predicate<ResourceLocation> p_215574_) {
            return Map.of();
        }

        @Override
        public Stream<PackResources> listPacks() {
            return Stream.of();
        }
    }
}
