package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;

public class MappedRegistry<T> extends net.neoforged.neoforge.registries.BaseMappedRegistry<T> implements WritableRegistry<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    final ResourceKey<? extends Registry<T>> key;
    private final ObjectList<Holder.Reference<T>> byId = new ObjectArrayList<>(256);
    private final Reference2IntMap<T> toId = Util.make(new Reference2IntOpenHashMap<>(), p_304142_ -> p_304142_.defaultReturnValue(-1));
    private final Map<ResourceLocation, Holder.Reference<T>> byLocation = new HashMap<>();
    private final Map<ResourceKey<T>, Holder.Reference<T>> byKey = new HashMap<>();
    private final Map<T, Holder.Reference<T>> byValue = new IdentityHashMap<>();
    private final Map<ResourceKey<T>, RegistrationInfo> registrationInfos = new IdentityHashMap<>();
    private Lifecycle registryLifecycle;
    private volatile Map<TagKey<T>, HolderSet.Named<T>> tags = new IdentityHashMap<>();
    private boolean frozen;
    @Nullable
    private Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;
    private final HolderLookup.RegistryLookup<T> lookup = new HolderLookup.RegistryLookup<T>() {
        @Override
        public ResourceKey<? extends Registry<? extends T>> key() {
            return MappedRegistry.this.key;
        }

        @Override
        public Lifecycle registryLifecycle() {
            return MappedRegistry.this.registryLifecycle();
        }

        @Override
        public Optional<Holder.Reference<T>> get(ResourceKey<T> p_255624_) {
            return MappedRegistry.this.getHolder(p_255624_);
        }

        @Override
        public Stream<Holder.Reference<T>> listElements() {
            return MappedRegistry.this.holders();
        }

        @Override
        public Optional<HolderSet.Named<T>> get(TagKey<T> p_256277_) {
            return MappedRegistry.this.getTag(p_256277_);
        }

        @Override
        public Stream<HolderSet.Named<T>> listTags() {
            return MappedRegistry.this.getTags().map(Pair::getSecond);
        }

        @Override
        @org.jetbrains.annotations.Nullable
        public <A> A getData(net.neoforged.neoforge.registries.datamaps.DataMapType<T, A> type, ResourceKey<T> key) {
            return MappedRegistry.this.getData(type, key);
        }
    };
    private final Object tagAdditionLock = new Object();

    public MappedRegistry(ResourceKey<? extends Registry<T>> key, Lifecycle registryLifecycle) {
        this(key, registryLifecycle, false);
    }

    public MappedRegistry(ResourceKey<? extends Registry<T>> key, Lifecycle registryLifecycle, boolean hasIntrusiveHolders) {
        this.key = key;
        this.registryLifecycle = registryLifecycle;
        if (hasIntrusiveHolders) {
            this.unregisteredIntrusiveHolders = new IdentityHashMap<>();
        }
    }

    @Override
    public ResourceKey<? extends Registry<T>> key() {
        return this.key;
    }

    @Override
    public String toString() {
        return "Registry[" + this.key + " (" + this.registryLifecycle + ")]";
    }

    private void validateWrite() {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen");
        }
    }

    private void validateWrite(ResourceKey<T> key) {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen (trying to add key " + key + ")");
        }
    }

    @Override
    public Holder.Reference<T> register(ResourceKey<T> key, T value, RegistrationInfo registrationInfo) {
        return register(this.byId.size(), key, value, registrationInfo);
    }

    public Holder.Reference<T> register(int id, ResourceKey<T> p_256252_, T p_256591_, RegistrationInfo p_326235_) {
        this.validateWrite(p_256252_);
        Objects.requireNonNull(p_256252_);
        Objects.requireNonNull(p_256591_);
        int i = id;
        if (i > this.getMaxId())
            throw new IllegalStateException(String.format(java.util.Locale.ENGLISH, "Invalid id %d - maximum id range of %d exceeded.", i, this.getMaxId()));

        if (this.byLocation.containsKey(p_256252_.location())) {
            Util.pauseInIde(new IllegalStateException("Adding duplicate key '" + p_256252_ + "' to registry"));
        }

        if (this.byValue.containsKey(p_256591_)) {
            Util.pauseInIde(new IllegalStateException("Adding duplicate value '" + p_256591_ + "' to registry"));
        }

        Holder.Reference<T> reference;
        if (this.unregisteredIntrusiveHolders != null) {
            reference = this.unregisteredIntrusiveHolders.remove(p_256591_);
            if (reference == null) {
                throw new AssertionError("Missing intrusive holder for " + p_256252_ + ":" + p_256591_);
            }

            reference.bindKey(p_256252_);
        } else {
            reference = this.byKey.computeIfAbsent(p_256252_, p_258168_ -> Holder.Reference.createStandAlone(this.holderOwner(), (ResourceKey<T>)p_258168_));
            // Forge: Bind the value immediately so it can be queried while the registry is not frozen
            reference.bindValue(p_256591_);
        }

        this.byKey.put(p_256252_, reference);
        this.byLocation.put(p_256252_.location(), reference);
        this.byValue.put(p_256591_, reference);
        this.byId.add(reference);
        this.toId.put(p_256591_, i);
        this.registrationInfos.put(p_256252_, p_326235_);
        this.registryLifecycle = this.registryLifecycle.add(p_326235_.lifecycle());
        this.addCallbacks.forEach(addCallback -> addCallback.onAdd(this, i, p_256252_, p_256591_));
        return reference;
    }

    /**
     * @return the name used to identify the given object within this registry or {@code null} if the object is not within this registry
     */
    @Nullable
    @Override
    public ResourceLocation getKey(T value) {
        Holder.Reference<T> reference = this.byValue.get(value);
        return reference != null ? reference.key().location() : null;
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T value) {
        return Optional.ofNullable(this.byValue.get(value)).map(Holder.Reference::key);
    }

    /**
     * @return the integer ID used to identify the given object
     */
    @Override
    public int getId(@Nullable T value) {
        return this.toId.getInt(value);
    }

    @Nullable
    @Override
    public T get(@Nullable ResourceKey<T> key) {
        return getValueFromNullable(this.byKey.get(resolve(key)));
    }

    @Nullable
    @Override
    public T byId(int id) {
        return id >= 0 && id < this.byId.size() ? this.byId.get(id).value() : null;
    }

    @Override
    public Optional<Holder.Reference<T>> getHolder(int id) {
        return id >= 0 && id < this.byId.size() ? Optional.ofNullable(this.byId.get(id)) : Optional.empty();
    }

    @Override
    public Optional<Holder.Reference<T>> getHolder(ResourceLocation location) {
        return Optional.ofNullable(this.byLocation.get(resolve(location)));
    }

    @Override
    public Optional<Holder.Reference<T>> getHolder(ResourceKey<T> key) {
        return Optional.ofNullable(this.byKey.get(resolve(key)));
    }

    @Override
    public Optional<Holder.Reference<T>> getAny() {
        return this.byId.isEmpty() ? Optional.empty() : Optional.of(this.byId.getFirst());
    }

    @Override
    public Holder<T> wrapAsHolder(T value) {
        Holder.Reference<T> reference = this.byValue.get(value);
        return (Holder<T>)(reference != null ? reference : Holder.direct(value));
    }

    Holder.Reference<T> getOrCreateHolderOrThrow(ResourceKey<T> key) {
        return this.byKey.computeIfAbsent(resolve(key), p_258169_ -> {
            if (this.unregisteredIntrusiveHolders != null) {
                throw new IllegalStateException("This registry can't create new holders without value");
            } else {
                this.validateWrite((ResourceKey<T>)p_258169_);
                return Holder.Reference.createStandAlone(this.holderOwner(), (ResourceKey<T>)p_258169_);
            }
        });
    }

    @Override
    public int size() {
        return this.byKey.size();
    }

    @Override
    public Optional<RegistrationInfo> registrationInfo(ResourceKey<T> key) {
        return Optional.ofNullable(this.registrationInfos.get(key));
    }

    @Override
    public Lifecycle registryLifecycle() {
        return this.registryLifecycle;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.transform(this.byId.iterator(), Holder::value);
    }

    @Nullable
    @Override
    public T get(@Nullable ResourceLocation name) {
        Holder.Reference<T> reference = this.byLocation.get(name != null ? resolve(name) : null);
        return getValueFromNullable(reference);
    }

    @Nullable
    private static <T> T getValueFromNullable(@Nullable Holder.Reference<T> holder) {
        return holder != null ? holder.value() : null;
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return Collections.unmodifiableSet(this.byLocation.keySet());
    }

    @Override
    public Set<ResourceKey<T>> registryKeySet() {
        return Collections.unmodifiableSet(this.byKey.keySet());
    }

    @Override
    public Set<Entry<ResourceKey<T>, T>> entrySet() {
        return Collections.unmodifiableSet(Maps.transformValues(this.byKey, Holder::value).entrySet());
    }

    @Override
    public Stream<Holder.Reference<T>> holders() {
        return this.byId.stream();
    }

    @Override
    public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags() {
        return this.tags.entrySet().stream().map(p_211060_ -> Pair.of(p_211060_.getKey(), p_211060_.getValue()));
    }

    @Override
    public HolderSet.Named<T> getOrCreateTag(TagKey<T> key) {
        HolderSet.Named<T> named = this.tags.get(key);
        if (named != null) {
            return named;
        } else {
            synchronized (this.tagAdditionLock) {
                named = this.tags.get(key);
                if (named != null) {
                    return named;
                } else {
                    named = this.createTag(key);
                    Map<TagKey<T>, HolderSet.Named<T>> map = new IdentityHashMap<>(this.tags);
                    map.put(key, named);
                    this.tags = map;
                    return named;
                }
            }
        }
    }

    private HolderSet.Named<T> createTag(TagKey<T> key) {
        return new HolderSet.Named<>(this.holderOwner(), key);
    }

    @Override
    public Stream<TagKey<T>> getTagNames() {
        return this.tags.keySet().stream();
    }

    @Override
    public boolean isEmpty() {
        return this.byKey.isEmpty();
    }

    @Override
    public Optional<Holder.Reference<T>> getRandom(RandomSource random) {
        return Util.getRandomSafe(this.byId, random);
    }

    @Override
    public boolean containsKey(ResourceLocation name) {
        return this.byLocation.containsKey(name);
    }

    @Override
    public boolean containsKey(ResourceKey<T> key) {
        return this.byKey.containsKey(key);
    }

    /** @deprecated Forge: For internal use only. Use the Register events when registering values. */
    @Deprecated
    public void unfreeze() {
        this.frozen = false;
    }

    @Override
    public Registry<T> freeze() {
        if (this.frozen) {
            return this;
        } else {
            this.frozen = true;
            List<ResourceLocation> list = this.byKey
                .entrySet()
                .stream()
                .filter(p_211055_ -> !p_211055_.getValue().isBound())
                .map(p_211794_ -> p_211794_.getKey().location())
                .sorted()
                .toList();
            if (!list.isEmpty()) {
                throw new IllegalStateException("Unbound values in registry " + this.key() + ": " + list);
            } else {
                if (this.unregisteredIntrusiveHolders != null) {
                    if (!this.unregisteredIntrusiveHolders.isEmpty()) {
                        throw new IllegalStateException("Some intrusive holders were not registered: " + this.unregisteredIntrusiveHolders.values());
                    }

                    // Neo: We freeze/unfreeze vanilla registries more than once, so we need to keep the unregistered intrusive holders map around.
                    // this.unregisteredIntrusiveHolders = null;
                }
                this.bakeCallbacks.forEach(bakeCallback -> bakeCallback.onBake(this));

                return this;
            }
        }
    }

    @Override
    public Holder.Reference<T> createIntrusiveHolder(T value) {
        if (this.unregisteredIntrusiveHolders == null) {
            throw new IllegalStateException("This registry can't create intrusive holders");
        } else {
            this.validateWrite();
            return this.unregisteredIntrusiveHolders.computeIfAbsent(value, p_258166_ -> Holder.Reference.createIntrusive(this.asLookup(), (T)p_258166_));
        }
    }

    @Override
    public Optional<HolderSet.Named<T>> getTag(TagKey<T> key) {
        return Optional.ofNullable(this.tags.get(key));
    }

    @Override
    public void bindTags(Map<TagKey<T>, List<Holder<T>>> tagMap) {
        Map<Holder.Reference<T>, List<TagKey<T>>> map = new IdentityHashMap<>();
        this.byKey.values().forEach(p_211801_ -> map.put((Holder.Reference<T>)p_211801_, new ArrayList<>()));
        tagMap.forEach((p_339332_, p_339333_) -> {
            for (Holder<T> holder : p_339333_) {
                if (!holder.canSerializeIn(this.asLookup())) {
                    throw new IllegalStateException("Can't create named set " + p_339332_ + " containing value " + holder + " from outside registry " + this);
                }

                if (!(holder instanceof Holder.Reference<T> reference)) {
                    throw new IllegalStateException("Found direct holder " + holder + " value in tag " + p_339332_);
                }

                map.get(reference).add((TagKey<T>)p_339332_);
            }
        });
        Set<TagKey<T>> set = Sets.difference(this.tags.keySet(), tagMap.keySet());
        if (!set.isEmpty()) {
            LOGGER.warn(
                "Not all defined tags for registry {} are present in data pack: {}",
                this.key(),
                set.stream().map(p_211811_ -> p_211811_.location().toString()).sorted().collect(Collectors.joining(", "))
            );
        }

        synchronized (this.tagAdditionLock) {
            Map<TagKey<T>, HolderSet.Named<T>> map1 = new IdentityHashMap<>(this.tags);
            tagMap.forEach((p_211797_, p_211798_) -> map1.computeIfAbsent((TagKey<T>)p_211797_, this::createTag).bind((List<Holder<T>>)p_211798_));
            map.forEach(Holder.Reference::bindTags);
            this.tags = map1;
        }
    }

    @Override
    public void resetTags() {
        this.tags.values().forEach(p_211792_ -> p_211792_.bind(List.of()));
        this.byKey.values().forEach(p_211803_ -> p_211803_.bindTags(Set.of()));
    }

    @Override
    public HolderGetter<T> createRegistrationLookup() {
        this.validateWrite();
        return new HolderGetter<T>() {
            @Override
            public Optional<Holder.Reference<T>> get(ResourceKey<T> p_259097_) {
                return Optional.of(this.getOrThrow(p_259097_));
            }

            @Override
            public Holder.Reference<T> getOrThrow(ResourceKey<T> p_259750_) {
                return MappedRegistry.this.getOrCreateHolderOrThrow(p_259750_);
            }

            @Override
            public Optional<HolderSet.Named<T>> get(TagKey<T> p_259486_) {
                return Optional.of(this.getOrThrow(p_259486_));
            }

            @Override
            public HolderSet.Named<T> getOrThrow(TagKey<T> p_260298_) {
                return MappedRegistry.this.getOrCreateTag(p_260298_);
            }
        };
    }

    @Override
    public HolderOwner<T> holderOwner() {
        return this.lookup;
    }

    @Override
    public HolderLookup.RegistryLookup<T> asLookup() {
        return this.lookup;
    }

    @Override
    protected void clear(boolean full) {
        this.validateWrite();
        this.clearCallbacks.forEach(clearCallback -> clearCallback.onClear(this, full));
        super.clear(full);
        this.byId.clear();
        this.toId.clear();
        if (full) {
            this.byLocation.clear();
            this.byKey.clear();
            this.byValue.clear();
            this.tags.clear();
            if (unregisteredIntrusiveHolders != null) {
                unregisteredIntrusiveHolders.clear();
                unregisteredIntrusiveHolders = null;
            }
        }
    }

    @Override
    protected void registerIdMapping(ResourceKey<T> key, int id) {
        this.validateWrite(key);
        if (id > this.getMaxId())
            throw new IllegalStateException(String.format(java.util.Locale.ENGLISH, "Invalid id %d - maximum id range of %d exceeded.", id, this.getMaxId()));
        if (0 <= id && id < this.byId.size() && this.byId.get(id) != null) { // Don't use byId() method, it will return the default value if the entry is absent
            throw new IllegalStateException("Duplicate id " + id + " for " + key + " and " + this.getKey(this.byId.get(id).value()));
        }
        var holder = byKey.get(key);
        while (this.byId.size() < (id + 1)) this.byId.add(null);
        this.byId.set(id, holder);
        this.toId.put(holder.value(), id);
    }

    @Override
    public int getId(ResourceLocation name) {
        return getId(get(name));
    }

    @Override
    public int getId(ResourceKey<T> key) {
        return getId(get(key));
    }

    @Override
    public boolean containsValue(T value) {
        return byValue.containsKey(value);
    }
}
