package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
import org.slf4j.Logger;

public class TagLoader<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    final Function<ResourceLocation, Optional<? extends T>> idToValue;
    private final String directory;

    public TagLoader(Function<ResourceLocation, Optional<? extends T>> idToValue, String directory) {
        this.idToValue = idToValue;
        this.directory = directory;
    }

    public Map<ResourceLocation, List<TagLoader.EntryWithSource>> load(ResourceManager resourceManager) {
        Map<ResourceLocation, List<TagLoader.EntryWithSource>> map = Maps.newHashMap();
        FileToIdConverter filetoidconverter = FileToIdConverter.json(this.directory);

        for (Entry<ResourceLocation, List<Resource>> entry : filetoidconverter.listMatchingResourceStacks(resourceManager).entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            ResourceLocation resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

            for (Resource resource : entry.getValue()) {
                try (Reader reader = resource.openAsReader()) {
                    JsonElement jsonelement = JsonParser.parseReader(reader);
                    List<TagLoader.EntryWithSource> list = map.computeIfAbsent(resourcelocation1, p_215974_ -> new ArrayList<>());
                    TagFile tagfile = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, jsonelement)).getOrThrow();
                    if (tagfile.replace()) {
                        list.clear();
                    }

                    String s = resource.sourcePackId();
                    tagfile.entries().forEach(p_215997_ -> list.add(new TagLoader.EntryWithSource(p_215997_, s)));
                    // Make all removal entries optional at runtime to avoid them creating intrusive holders - see NeoForge#2319
                    tagfile.remove().forEach(e -> list.add(new TagLoader.EntryWithSource(e.withRequired(false), s, true)));
                } catch (Exception exception) {
                    LOGGER.error("Couldn't read tag list {} from {} in data pack {}", resourcelocation1, resourcelocation, resource.sourcePackId(), exception);
                }
            }
        }

        return map;
    }

    private Either<Collection<TagLoader.EntryWithSource>, Collection<T>> build(TagEntry.Lookup<T> lookup, List<TagLoader.EntryWithSource> entries) {
        var builder = new java.util.LinkedHashSet<T>(); // Set must retain insertion order, some tag consumers rely on this being the case (see NeoForge#256)
        List<TagLoader.EntryWithSource> list = new ArrayList<>();

        for (TagLoader.EntryWithSource tagloader$entrywithsource : entries) {
            if (!tagloader$entrywithsource.entry().build(lookup, tagloader$entrywithsource.remove() ? builder::remove : builder::add)) {
                list.add(tagloader$entrywithsource);
            }
        }

        return list.isEmpty() ? Either.right(List.copyOf(builder)) : Either.left(list);
    }

    public Map<ResourceLocation, Collection<T>> build(Map<ResourceLocation, List<TagLoader.EntryWithSource>> builders) {
        final Map<ResourceLocation, Collection<T>> map = Maps.newHashMap();
        TagEntry.Lookup<T> lookup = new TagEntry.Lookup<T>() {
            @Nullable
            @Override
            public T element(ResourceLocation p_216039_) {
                return (T)TagLoader.this.idToValue.apply(p_216039_).orElse(null);
            }

            @Nullable
            @Override
            public Collection<T> tag(ResourceLocation p_216041_) {
                return map.get(p_216041_);
            }
        };
        DependencySorter<ResourceLocation, TagLoader.SortingEntry> dependencysorter = new DependencySorter<>();
        builders.forEach(
            (p_284685_, p_284686_) -> dependencysorter.addEntry(p_284685_, new TagLoader.SortingEntry((List<TagLoader.EntryWithSource>)p_284686_))
        );
        dependencysorter.orderByDependencies(
            (p_284682_, p_284683_) -> this.build(lookup, p_284683_.entries)
                    .ifLeft(
                        p_215977_ -> LOGGER.error(
                                "Couldn't load tag {} as it is missing following references: {}",
                                p_284682_,
                                p_215977_.stream().map(Objects::toString).collect(Collectors.joining("\n\t", "\n\t", ""))
                            )
                    )
                    .ifRight(p_216001_ -> map.put(p_284682_, (Collection<T>)p_216001_))
        );
        return map;
    }

    public Map<ResourceLocation, Collection<T>> loadAndBuild(ResourceManager resourceManager) {
        return this.build(this.load(resourceManager));
    }

    public static record EntryWithSource(TagEntry entry, String source, boolean remove) {
        public EntryWithSource(TagEntry entry, String source) { this(entry, source, false); }
        @Override
        public String toString() {
            return this.entry + " (from " + this.source + ")";
        }
    }

    static record SortingEntry(List<TagLoader.EntryWithSource> entries) implements DependencySorter.Entry<ResourceLocation> {
        @Override
        public void visitRequiredDependencies(Consumer<ResourceLocation> visitor) {
            this.entries.forEach(p_285236_ -> p_285236_.entry.visitRequiredDependencies(visitor));
        }

        @Override
        public void visitOptionalDependencies(Consumer<ResourceLocation> visitor) {
            this.entries.forEach(p_284943_ -> p_284943_.entry.visitOptionalDependencies(visitor));
        }
    }
}
