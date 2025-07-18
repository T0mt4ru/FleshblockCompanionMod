package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.slf4j.Logger;

public class ReloadableServerResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
    private final ReloadableServerRegistries.Holder fullRegistryHolder;
    private final ReloadableServerResources.ConfigurableRegistryLookup registryLookup;
    private final Commands commands;
    private final RecipeManager recipes;
    private final TagManager tagManager;
    private final ServerAdvancementManager advancements;
    private final ServerFunctionLibrary functionLibrary;

    private ReloadableServerResources(RegistryAccess.Frozen registryAccess, FeatureFlagSet enabledFeatures, Commands.CommandSelection commandSelection, int functionCompilationLevel) {
        this.fullRegistryHolder = new ReloadableServerRegistries.Holder(registryAccess);
        this.registryLookup = new ReloadableServerResources.ConfigurableRegistryLookup(registryAccess);
        this.registryLookup.missingTagAccessPolicy(ReloadableServerResources.MissingTagAccessPolicy.CREATE_NEW);
        this.recipes = new RecipeManager(this.registryLookup);
        this.tagManager = new TagManager(registryAccess);
        this.commands = new Commands(commandSelection, CommandBuildContext.simple(this.registryLookup, enabledFeatures));
        this.advancements = new ServerAdvancementManager(this.registryLookup);
        this.functionLibrary = new ServerFunctionLibrary(functionCompilationLevel, this.commands.getDispatcher());
        // Neo: Create context object
        this.context = new net.neoforged.neoforge.common.conditions.ConditionContext(this.tagManager);
    }

    public ServerFunctionLibrary getFunctionLibrary() {
        return this.functionLibrary;
    }

    public ReloadableServerRegistries.Holder fullRegistries() {
        return this.fullRegistryHolder;
    }

    public RecipeManager getRecipeManager() {
        return this.recipes;
    }

    public Commands getCommands() {
        return this.commands;
    }

    public ServerAdvancementManager getAdvancements() {
        return this.advancements;
    }

    public List<PreparableReloadListener> listeners() {
        return List.of(this.tagManager, this.recipes, this.functionLibrary, this.advancements);
    }

    private final net.neoforged.neoforge.common.conditions.ICondition.IContext context;

    /**
     * Exposes the current condition context for usage in other reload listeners.<br>
     * This is not useful outside the reloading stage.
     * @return The condition context for the currently active reload.
     */
    public net.neoforged.neoforge.common.conditions.ICondition.IContext getConditionContext() {
        return this.context;
    }

    /**
      * {@return the lookup provider access for the currently active reload}
      */
    public HolderLookup.Provider getRegistryLookup() {
        return this.registryLookup;
    }

    public static CompletableFuture<ReloadableServerResources> loadResources(
        ResourceManager resourceManager,
        LayeredRegistryAccess<RegistryLayer> registries,
        FeatureFlagSet enabledFeatures,
        Commands.CommandSelection commandSelection,
        int functionCompilationLevel,
        Executor backgroundExecutor,
        Executor gameExecutor
    ) {
        return ReloadableServerRegistries.reload(registries, resourceManager, backgroundExecutor)
            .thenCompose(
                p_335211_ -> {
                    ReloadableServerResources reloadableserverresources = new ReloadableServerResources(
                        p_335211_.compositeAccess(), enabledFeatures, commandSelection, functionCompilationLevel
                    );
                    List<PreparableReloadListener> listeners = new java.util.ArrayList<>(reloadableserverresources.listeners());
                    listeners.addAll(net.neoforged.neoforge.event.EventHooks.onResourceReload(reloadableserverresources, p_335211_.compositeAccess()));
                    listeners.forEach(rl -> {
                        if (rl instanceof net.neoforged.neoforge.resource.ContextAwareReloadListener srl) srl.injectContext(reloadableserverresources.context, reloadableserverresources.registryLookup);
                    });
                    return SimpleReloadInstance.create(
                            resourceManager, listeners, backgroundExecutor, gameExecutor, DATA_RELOAD_INITIAL_TASK, LOGGER.isDebugEnabled()
                        )
                        .done()
                        .whenComplete(
                            (p_323178_, p_323179_) -> reloadableserverresources.registryLookup
                                    .missingTagAccessPolicy(ReloadableServerResources.MissingTagAccessPolicy.FAIL)
                        )
                        .thenRun(() -> {
                            // Clear context after reload completes
                            listeners.forEach(rl -> {
                                if (rl instanceof net.neoforged.neoforge.resource.ContextAwareReloadListener srl) {
                                    srl.injectContext(net.neoforged.neoforge.common.conditions.ICondition.IContext.EMPTY, RegistryAccess.EMPTY);
                                }
                            });
                        })
                        .thenApply(p_214306_ -> reloadableserverresources);
                }
            );
    }

    public void updateRegistryTags() {
        this.tagManager.getResult().forEach(p_335204_ -> updateRegistryTags(this.fullRegistryHolder.get(), (TagManager.LoadResult<?>)p_335204_));
        AbstractFurnaceBlockEntity.invalidateCache();
        Blocks.rebuildCache();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.TagsUpdatedEvent(this.fullRegistryHolder.get(), false, false));
    }

    private static <T> void updateRegistryTags(RegistryAccess registryAccess, TagManager.LoadResult<T> loadResult) {
        ResourceKey<? extends Registry<T>> resourcekey = loadResult.key();
        Map<TagKey<T>, List<Holder<T>>> map = loadResult.tags()
            .entrySet()
            .stream()
            .collect(Collectors.toUnmodifiableMap(p_214303_ -> TagKey.create(resourcekey, p_214303_.getKey()), p_214312_ -> List.copyOf(p_214312_.getValue())));
        registryAccess.registryOrThrow(resourcekey).bindTags(map);
    }

    static class ConfigurableRegistryLookup implements HolderLookup.Provider {
        private final RegistryAccess registryAccess;
        ReloadableServerResources.MissingTagAccessPolicy missingTagAccessPolicy = ReloadableServerResources.MissingTagAccessPolicy.FAIL;

        ConfigurableRegistryLookup(RegistryAccess registryAccess) {
            this.registryAccess = registryAccess;
        }

        public void missingTagAccessPolicy(ReloadableServerResources.MissingTagAccessPolicy missingTagAccessPolicy) {
            this.missingTagAccessPolicy = missingTagAccessPolicy;
        }

        @Override
        public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
            return this.registryAccess.listRegistries();
        }

        @Override
        public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> registryKey) {
            return this.registryAccess.registry(registryKey).map(p_324050_ -> this.createDispatchedLookup(p_324050_.asLookup(), p_324050_.asTagAddingLookup()));
        }

        private <T> HolderLookup.RegistryLookup<T> createDispatchedLookup(
            final HolderLookup.RegistryLookup<T> lookup, final HolderLookup.RegistryLookup<T> tagAddingLookup
        ) {
            return new HolderLookup.RegistryLookup.Delegate<T>() {
                @Override
                public HolderLookup.RegistryLookup<T> parent() {
                    return switch (ConfigurableRegistryLookup.this.missingTagAccessPolicy) {
                        case CREATE_NEW -> tagAddingLookup;
                        case FAIL -> lookup;
                    };
                }
            };
        }
    }

    static enum MissingTagAccessPolicy {
        CREATE_NEW,
        FAIL;
    }
}
