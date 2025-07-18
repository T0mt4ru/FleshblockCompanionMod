package net.minecraft.client.particle;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ParticleEngine implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter PARTICLE_LISTER = FileToIdConverter.json("particles");
    private static final ResourceLocation PARTICLES_ATLAS_INFO = ResourceLocation.withDefaultNamespace("particles");
    private static final int MAX_PARTICLES_PER_LAYER = 16384;
    private static final List<ParticleRenderType> RENDER_ORDER = ImmutableList.of(
        ParticleRenderType.TERRAIN_SHEET,
        ParticleRenderType.PARTICLE_SHEET_OPAQUE,
        ParticleRenderType.PARTICLE_SHEET_LIT,
        ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT,
        ParticleRenderType.CUSTOM
    );
    protected ClientLevel level;
    private final Map<ParticleRenderType, Queue<Particle>> particles = Maps.newTreeMap(net.neoforged.neoforge.client.ClientHooks.makeParticleRenderTypeComparator(RENDER_ORDER));
    private final Queue<TrackingEmitter> trackingEmitters = Queues.newArrayDeque();
    private final TextureManager textureManager;
    private final RandomSource random = RandomSource.create();
    private final Map<ResourceLocation, ParticleProvider<?>> providers = new java.util.HashMap<>();
    private final Queue<Particle> particlesToAdd = Queues.newArrayDeque();
    private final Map<ResourceLocation, ParticleEngine.MutableSpriteSet> spriteSets = Maps.newHashMap();
    private final TextureAtlas textureAtlas;
    private final Object2IntOpenHashMap<ParticleGroup> trackedParticleCounts = new Object2IntOpenHashMap<>();

    public ParticleEngine(ClientLevel level, TextureManager textureManager) {
        this.textureAtlas = new TextureAtlas(TextureAtlas.LOCATION_PARTICLES);
        textureManager.register(this.textureAtlas.location(), this.textureAtlas);
        this.level = level;
        this.textureManager = textureManager;
        this.registerProviders();
    }

    private void registerProviders() {
        this.register(ParticleTypes.ANGRY_VILLAGER, HeartParticle.AngryVillagerProvider::new);
        this.register(ParticleTypes.BLOCK_MARKER, new BlockMarker.Provider());
        this.register(ParticleTypes.BLOCK, new TerrainParticle.Provider());
        this.register(ParticleTypes.BUBBLE, BubbleParticle.Provider::new);
        this.register(ParticleTypes.BUBBLE_COLUMN_UP, BubbleColumnUpParticle.Provider::new);
        this.register(ParticleTypes.BUBBLE_POP, BubblePopParticle.Provider::new);
        this.register(ParticleTypes.CAMPFIRE_COSY_SMOKE, CampfireSmokeParticle.CosyProvider::new);
        this.register(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, CampfireSmokeParticle.SignalProvider::new);
        this.register(ParticleTypes.CLOUD, PlayerCloudParticle.Provider::new);
        this.register(ParticleTypes.COMPOSTER, SuspendedTownParticle.ComposterFillProvider::new);
        this.register(ParticleTypes.CRIT, CritParticle.Provider::new);
        this.register(ParticleTypes.CURRENT_DOWN, WaterCurrentDownParticle.Provider::new);
        this.register(ParticleTypes.DAMAGE_INDICATOR, CritParticle.DamageIndicatorProvider::new);
        this.register(ParticleTypes.DRAGON_BREATH, DragonBreathParticle.Provider::new);
        this.register(ParticleTypes.DOLPHIN, SuspendedTownParticle.DolphinSpeedProvider::new);
        this.register(ParticleTypes.DRIPPING_LAVA, DripParticle::createLavaHangParticle);
        this.register(ParticleTypes.FALLING_LAVA, DripParticle::createLavaFallParticle);
        this.register(ParticleTypes.LANDING_LAVA, DripParticle::createLavaLandParticle);
        this.register(ParticleTypes.DRIPPING_WATER, DripParticle::createWaterHangParticle);
        this.register(ParticleTypes.FALLING_WATER, DripParticle::createWaterFallParticle);
        this.register(ParticleTypes.DUST, DustParticle.Provider::new);
        this.register(ParticleTypes.DUST_COLOR_TRANSITION, DustColorTransitionParticle.Provider::new);
        this.register(ParticleTypes.EFFECT, SpellParticle.Provider::new);
        this.register(ParticleTypes.ELDER_GUARDIAN, new MobAppearanceParticle.Provider());
        this.register(ParticleTypes.ENCHANTED_HIT, CritParticle.MagicProvider::new);
        this.register(ParticleTypes.ENCHANT, FlyTowardsPositionParticle.EnchantProvider::new);
        this.register(ParticleTypes.END_ROD, EndRodParticle.Provider::new);
        this.register(ParticleTypes.ENTITY_EFFECT, SpellParticle.MobEffectProvider::new);
        this.register(ParticleTypes.EXPLOSION_EMITTER, new HugeExplosionSeedParticle.Provider());
        this.register(ParticleTypes.EXPLOSION, HugeExplosionParticle.Provider::new);
        this.register(ParticleTypes.SONIC_BOOM, SonicBoomParticle.Provider::new);
        this.register(ParticleTypes.FALLING_DUST, FallingDustParticle.Provider::new);
        this.register(ParticleTypes.GUST, GustParticle.Provider::new);
        this.register(ParticleTypes.SMALL_GUST, GustParticle.SmallProvider::new);
        this.register(ParticleTypes.GUST_EMITTER_LARGE, new GustSeedParticle.Provider(3.0, 7, 0));
        this.register(ParticleTypes.GUST_EMITTER_SMALL, new GustSeedParticle.Provider(1.0, 3, 2));
        this.register(ParticleTypes.FIREWORK, FireworkParticles.SparkProvider::new);
        this.register(ParticleTypes.FISHING, WakeParticle.Provider::new);
        this.register(ParticleTypes.FLAME, FlameParticle.Provider::new);
        this.register(ParticleTypes.INFESTED, SpellParticle.Provider::new);
        this.register(ParticleTypes.SCULK_SOUL, SoulParticle.EmissiveProvider::new);
        this.register(ParticleTypes.SCULK_CHARGE, SculkChargeParticle.Provider::new);
        this.register(ParticleTypes.SCULK_CHARGE_POP, SculkChargePopParticle.Provider::new);
        this.register(ParticleTypes.SOUL, SoulParticle.Provider::new);
        this.register(ParticleTypes.SOUL_FIRE_FLAME, FlameParticle.Provider::new);
        this.register(ParticleTypes.FLASH, FireworkParticles.FlashProvider::new);
        this.register(ParticleTypes.HAPPY_VILLAGER, SuspendedTownParticle.HappyVillagerProvider::new);
        this.register(ParticleTypes.HEART, HeartParticle.Provider::new);
        this.register(ParticleTypes.INSTANT_EFFECT, SpellParticle.InstantProvider::new);
        this.register(ParticleTypes.ITEM, new BreakingItemParticle.Provider());
        this.register(ParticleTypes.ITEM_SLIME, new BreakingItemParticle.SlimeProvider());
        this.register(ParticleTypes.ITEM_COBWEB, new BreakingItemParticle.CobwebProvider());
        this.register(ParticleTypes.ITEM_SNOWBALL, new BreakingItemParticle.SnowballProvider());
        this.register(ParticleTypes.LARGE_SMOKE, LargeSmokeParticle.Provider::new);
        this.register(ParticleTypes.LAVA, LavaParticle.Provider::new);
        this.register(ParticleTypes.MYCELIUM, SuspendedTownParticle.Provider::new);
        this.register(ParticleTypes.NAUTILUS, FlyTowardsPositionParticle.NautilusProvider::new);
        this.register(ParticleTypes.NOTE, NoteParticle.Provider::new);
        this.register(ParticleTypes.POOF, ExplodeParticle.Provider::new);
        this.register(ParticleTypes.PORTAL, PortalParticle.Provider::new);
        this.register(ParticleTypes.RAIN, WaterDropParticle.Provider::new);
        this.register(ParticleTypes.SMOKE, SmokeParticle.Provider::new);
        this.register(ParticleTypes.WHITE_SMOKE, WhiteSmokeParticle.Provider::new);
        this.register(ParticleTypes.SNEEZE, PlayerCloudParticle.SneezeProvider::new);
        this.register(ParticleTypes.SNOWFLAKE, SnowflakeParticle.Provider::new);
        this.register(ParticleTypes.SPIT, SpitParticle.Provider::new);
        this.register(ParticleTypes.SWEEP_ATTACK, AttackSweepParticle.Provider::new);
        this.register(ParticleTypes.TOTEM_OF_UNDYING, TotemParticle.Provider::new);
        this.register(ParticleTypes.SQUID_INK, SquidInkParticle.Provider::new);
        this.register(ParticleTypes.UNDERWATER, SuspendedParticle.UnderwaterProvider::new);
        this.register(ParticleTypes.SPLASH, SplashParticle.Provider::new);
        this.register(ParticleTypes.WITCH, SpellParticle.WitchProvider::new);
        this.register(ParticleTypes.DRIPPING_HONEY, DripParticle::createHoneyHangParticle);
        this.register(ParticleTypes.FALLING_HONEY, DripParticle::createHoneyFallParticle);
        this.register(ParticleTypes.LANDING_HONEY, DripParticle::createHoneyLandParticle);
        this.register(ParticleTypes.FALLING_NECTAR, DripParticle::createNectarFallParticle);
        this.register(ParticleTypes.FALLING_SPORE_BLOSSOM, DripParticle::createSporeBlossomFallParticle);
        this.register(ParticleTypes.SPORE_BLOSSOM_AIR, SuspendedParticle.SporeBlossomAirProvider::new);
        this.register(ParticleTypes.ASH, AshParticle.Provider::new);
        this.register(ParticleTypes.CRIMSON_SPORE, SuspendedParticle.CrimsonSporeProvider::new);
        this.register(ParticleTypes.WARPED_SPORE, SuspendedParticle.WarpedSporeProvider::new);
        this.register(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, DripParticle::createObsidianTearHangParticle);
        this.register(ParticleTypes.FALLING_OBSIDIAN_TEAR, DripParticle::createObsidianTearFallParticle);
        this.register(ParticleTypes.LANDING_OBSIDIAN_TEAR, DripParticle::createObsidianTearLandParticle);
        this.register(ParticleTypes.REVERSE_PORTAL, ReversePortalParticle.ReversePortalProvider::new);
        this.register(ParticleTypes.WHITE_ASH, WhiteAshParticle.Provider::new);
        this.register(ParticleTypes.SMALL_FLAME, FlameParticle.SmallFlameProvider::new);
        this.register(ParticleTypes.DRIPPING_DRIPSTONE_WATER, DripParticle::createDripstoneWaterHangParticle);
        this.register(ParticleTypes.FALLING_DRIPSTONE_WATER, DripParticle::createDripstoneWaterFallParticle);
        this.register(
            ParticleTypes.CHERRY_LEAVES,
            p_277215_ -> (p_277217_, p_277218_, p_277219_, p_277220_, p_277221_, p_277222_, p_277223_, p_277224_) -> new CherryParticle(
                        p_277218_, p_277219_, p_277220_, p_277221_, p_277215_
                    )
        );
        this.register(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, DripParticle::createDripstoneLavaHangParticle);
        this.register(ParticleTypes.FALLING_DRIPSTONE_LAVA, DripParticle::createDripstoneLavaFallParticle);
        this.register(ParticleTypes.VIBRATION, VibrationSignalParticle.Provider::new);
        this.register(ParticleTypes.GLOW_SQUID_INK, SquidInkParticle.GlowInkProvider::new);
        this.register(ParticleTypes.GLOW, GlowParticle.GlowSquidProvider::new);
        this.register(ParticleTypes.WAX_ON, GlowParticle.WaxOnProvider::new);
        this.register(ParticleTypes.WAX_OFF, GlowParticle.WaxOffProvider::new);
        this.register(ParticleTypes.ELECTRIC_SPARK, GlowParticle.ElectricSparkProvider::new);
        this.register(ParticleTypes.SCRAPE, GlowParticle.ScrapeProvider::new);
        this.register(ParticleTypes.SHRIEK, ShriekParticle.Provider::new);
        this.register(ParticleTypes.EGG_CRACK, SuspendedTownParticle.EggCrackProvider::new);
        this.register(ParticleTypes.DUST_PLUME, DustPlumeParticle.Provider::new);
        this.register(ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER, TrialSpawnerDetectionParticle.Provider::new);
        this.register(ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS, TrialSpawnerDetectionParticle.Provider::new);
        this.register(ParticleTypes.VAULT_CONNECTION, FlyTowardsPositionParticle.VaultConnectionProvider::new);
        this.register(ParticleTypes.DUST_PILLAR, new TerrainParticle.DustPillarProvider());
        this.register(ParticleTypes.RAID_OMEN, SpellParticle.Provider::new);
        this.register(ParticleTypes.TRIAL_OMEN, SpellParticle.Provider::new);
        this.register(ParticleTypes.OMINOUS_SPAWNING, FlyStraightTowardsParticle.OminousSpawnProvider::new);
    }

    /**
 * @deprecated Register via {@link
 *             net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent}
 */
    @Deprecated
    public <T extends ParticleOptions> void register(ParticleType<T> particleType, ParticleProvider<T> particleFactory) {
        this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getKey(particleType), particleFactory);
    }

    /**
 * @deprecated Register via {@link
 *             net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent}
 */
    @Deprecated
    public <T extends ParticleOptions> void register(ParticleType<T> particleType, ParticleProvider.Sprite<T> sprite) {
        this.register(
            particleType,
            p_272320_ -> (p_272323_, p_272324_, p_272325_, p_272326_, p_272327_, p_272328_, p_272329_, p_272330_) -> {
                    TextureSheetParticle texturesheetparticle = sprite.createParticle(
                        p_272323_, p_272324_, p_272325_, p_272326_, p_272327_, p_272328_, p_272329_, p_272330_
                    );
                    if (texturesheetparticle != null) {
                        texturesheetparticle.pickSprite(p_272320_);
                    }

                    return texturesheetparticle;
                }
        );
    }

    /**
 * @deprecated Register via {@link
 *             net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent}
 */
    @Deprecated
    public <T extends ParticleOptions> void register(ParticleType<T> particleType, ParticleEngine.SpriteParticleRegistration<T> particleMetaFactory) {
        ParticleEngine.MutableSpriteSet particleengine$mutablespriteset = new ParticleEngine.MutableSpriteSet();
        this.spriteSets.put(BuiltInRegistries.PARTICLE_TYPE.getKey(particleType), particleengine$mutablespriteset);
        this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getKey(particleType), particleMetaFactory.create(particleengine$mutablespriteset));
    }

    @Override
    public CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier stage,
        ResourceManager resourceManager,
        ProfilerFiller preparationsProfiler,
        ProfilerFiller reloadProfiler,
        Executor backgroundExecutor,
        Executor gameExecutor
    ) {
        @OnlyIn(Dist.CLIENT)
        record ParticleDefinition(ResourceLocation id, Optional<List<ResourceLocation>> sprites) {
        }

        CompletableFuture<List<ParticleDefinition>> completablefuture = CompletableFuture.<Map<ResourceLocation, Resource>>supplyAsync(
                () -> PARTICLE_LISTER.listMatchingResources(resourceManager), backgroundExecutor
            )
            .thenCompose(
                p_247914_ -> {
                    List<CompletableFuture<ParticleDefinition>> list = new ArrayList<>(p_247914_.size());
                    p_247914_.forEach(
                        (p_247903_, p_247904_) -> {
                            ResourceLocation resourcelocation = PARTICLE_LISTER.fileToId(p_247903_);
                            list.add(
                                CompletableFuture.supplyAsync(
                                    () -> new ParticleDefinition(resourcelocation, this.loadParticleDescription(resourcelocation, p_247904_)), backgroundExecutor
                                )
                            );
                        }
                    );
                    return Util.sequence(list);
                }
            );
        CompletableFuture<SpriteLoader.Preparations> completablefuture1 = SpriteLoader.create(this.textureAtlas)
            .loadAndStitch(resourceManager, PARTICLES_ATLAS_INFO, 0, backgroundExecutor)
            .thenCompose(SpriteLoader.Preparations::waitForUpload);
        return CompletableFuture.allOf(completablefuture1, completablefuture).thenCompose(stage::wait).thenAcceptAsync(p_247900_ -> {
            this.clearParticles();
            reloadProfiler.startTick();
            reloadProfiler.push("upload");
            SpriteLoader.Preparations spriteloader$preparations = completablefuture1.join();
            this.textureAtlas.upload(spriteloader$preparations);
            reloadProfiler.popPush("bindSpriteSets");
            Set<ResourceLocation> set = new HashSet<>();
            TextureAtlasSprite textureatlassprite = spriteloader$preparations.missing();
            completablefuture.join().forEach(p_247911_ -> {
                Optional<List<ResourceLocation>> optional = p_247911_.sprites();
                if (!optional.isEmpty()) {
                    List<TextureAtlasSprite> list = new ArrayList<>();

                    for (ResourceLocation resourcelocation : optional.get()) {
                        TextureAtlasSprite textureatlassprite1 = spriteloader$preparations.regions().get(resourcelocation);
                        if (textureatlassprite1 == null) {
                            set.add(resourcelocation);
                            list.add(textureatlassprite);
                        } else {
                            list.add(textureatlassprite1);
                        }
                    }

                    if (list.isEmpty()) {
                        list.add(textureatlassprite);
                    }

                    this.spriteSets.get(p_247911_.id()).rebind(list);
                }
            });
            if (!set.isEmpty()) {
                LOGGER.warn("Missing particle sprites: {}", set.stream().sorted().map(ResourceLocation::toString).collect(Collectors.joining(",")));
            }

            reloadProfiler.pop();
            reloadProfiler.endTick();
        }, gameExecutor);
    }

    public void close() {
        this.textureAtlas.clearTextureData();
    }

    private Optional<List<ResourceLocation>> loadParticleDescription(ResourceLocation registryName, Resource resource) {
        if (!this.spriteSets.containsKey(registryName)) {
            LOGGER.debug("Redundant texture list for particle: {}", registryName);
            return Optional.empty();
        } else {
            try {
                Optional optional;
                try (Reader reader = resource.openAsReader()) {
                    ParticleDescription particledescription = ParticleDescription.fromJson(GsonHelper.parse(reader));
                    optional = Optional.of(particledescription.getTextures());
                }

                return optional;
            } catch (IOException ioexception) {
                throw new IllegalStateException("Failed to load description for particle " + registryName, ioexception);
            }
        }
    }

    public void createTrackingEmitter(Entity entity, ParticleOptions particleData) {
        this.trackingEmitters.add(new TrackingEmitter(this.level, entity, particleData));
    }

    public void createTrackingEmitter(Entity entity, ParticleOptions data, int lifetime) {
        this.trackingEmitters.add(new TrackingEmitter(this.level, entity, data, lifetime));
    }

    @Nullable
    public Particle createParticle(
        ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed
    ) {
        Particle particle = this.makeParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
        if (particle != null) {
            this.add(particle);
            return particle;
        } else {
            return null;
        }
    }

    @Nullable
    private <T extends ParticleOptions> Particle makeParticle(
        T particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed
    ) {
        ParticleProvider<T> particleprovider = (ParticleProvider<T>) this.providers.get(BuiltInRegistries.PARTICLE_TYPE.getKey(particleData.getType()));
        return particleprovider == null
            ? null
            : particleprovider.createParticle(particleData, this.level, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    public void add(Particle effect) {
        Optional<ParticleGroup> optional = effect.getParticleGroup();
        if (optional.isPresent()) {
            if (this.hasSpaceInParticleLimit(optional.get())) {
                this.particlesToAdd.add(effect);
                this.updateCount(optional.get(), 1);
            }
        } else {
            this.particlesToAdd.add(effect);
        }
    }

    public void tick() {
        this.particles.forEach((p_340611_, p_340612_) -> {
            this.level.getProfiler().push(p_340611_.toString());
            this.tickParticleList(p_340612_);
            this.level.getProfiler().pop();
        });
        if (!this.trackingEmitters.isEmpty()) {
            List<TrackingEmitter> list = Lists.newArrayList();

            for (TrackingEmitter trackingemitter : this.trackingEmitters) {
                trackingemitter.tick();
                if (!trackingemitter.isAlive()) {
                    list.add(trackingemitter);
                }
            }

            this.trackingEmitters.removeAll(list);
        }

        Particle particle;
        if (!this.particlesToAdd.isEmpty()) {
            while ((particle = this.particlesToAdd.poll()) != null) {
                this.particles.computeIfAbsent(particle.getRenderType(), p_107347_ -> EvictingQueue.create(16384)).add(particle);
            }
        }
    }

    private void tickParticleList(Collection<Particle> particles) {
        if (!particles.isEmpty()) {
            Iterator<Particle> iterator = particles.iterator();

            while (iterator.hasNext()) {
                Particle particle = iterator.next();
                this.tickParticle(particle);
                if (!particle.isAlive()) {
                    particle.getParticleGroup().ifPresent(p_172289_ -> this.updateCount(p_172289_, -1));
                    iterator.remove();
                }
            }
        }
    }

    private void updateCount(ParticleGroup group, int count) {
        this.trackedParticleCounts.addTo(group, count);
    }

    private void tickParticle(Particle particle) {
        try {
            particle.tick();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking Particle");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being ticked");
            crashreportcategory.setDetail("Particle", particle::toString);
            crashreportcategory.setDetail("Particle Type", particle.getRenderType()::toString);
            throw new ReportedException(crashreport);
        }
    }

    @Deprecated
    public void render(LightTexture lightTexture, Camera camera, float partialTick) {
        render(lightTexture, camera, partialTick, null, type -> true);
    }

    public void render(LightTexture lightTexture, Camera camera, float partialTick, @Nullable net.minecraft.client.renderer.culling.Frustum frustum, java.util.function.Predicate<ParticleRenderType> renderTypePredicate) {
        lightTexture.turnOnLightLayer();
        RenderSystem.enableDepthTest();
        //TODO porting: is this even needed with the particle render order fix???
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE2);
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);

        for (ParticleRenderType particlerendertype : this.particles.keySet()) { // Neo: allow custom IParticleRenderType's
            if (particlerendertype == ParticleRenderType.NO_RENDER || !renderTypePredicate.test(particlerendertype)) continue;
            Queue<Particle> queue = this.particles.get(particlerendertype);
            if (queue != null && !queue.isEmpty()) {
                RenderSystem.setShader(GameRenderer::getParticleShader);
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = particlerendertype.begin(tesselator, this.textureManager);
                if (bufferbuilder != null) {
                    for (Particle particle : queue) {
                        if (frustum != null && !frustum.isVisible(particle.getRenderBoundingBox(partialTick))) continue;
                        try {
                            particle.render(bufferbuilder, camera, partialTick);
                        } catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
                            CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
                            crashreportcategory.setDetail("Particle", particle::toString);
                            crashreportcategory.setDetail("Particle Type", particlerendertype::toString);
                            throw new ReportedException(crashreport);
                        }
                    }

                    MeshData meshdata = bufferbuilder.build();
                    if (meshdata != null) {
                        BufferUploader.drawWithShader(meshdata);
                    }
                }
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        lightTexture.turnOffLightLayer();
    }

    public void setLevel(@Nullable ClientLevel level) {
        this.level = level;
        this.clearParticles();
        this.trackingEmitters.clear();
    }

    public void destroy(BlockPos pos, BlockState state) {
        if (!state.isAir() && !net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions.of(state).addDestroyEffects(state, this.level, pos, this)) {
            VoxelShape voxelshape = state.getShape(this.level, pos);
            double d0 = 0.25;
            voxelshape.forAllBoxes(
                (p_172273_, p_172274_, p_172275_, p_172276_, p_172277_, p_172278_) -> {
                    double d1 = Math.min(1.0, p_172276_ - p_172273_);
                    double d2 = Math.min(1.0, p_172277_ - p_172274_);
                    double d3 = Math.min(1.0, p_172278_ - p_172275_);
                    int i = Math.max(2, Mth.ceil(d1 / 0.25));
                    int j = Math.max(2, Mth.ceil(d2 / 0.25));
                    int k = Math.max(2, Mth.ceil(d3 / 0.25));

                    for (int l = 0; l < i; l++) {
                        for (int i1 = 0; i1 < j; i1++) {
                            for (int j1 = 0; j1 < k; j1++) {
                                double d4 = ((double)l + 0.5) / (double)i;
                                double d5 = ((double)i1 + 0.5) / (double)j;
                                double d6 = ((double)j1 + 0.5) / (double)k;
                                double d7 = d4 * d1 + p_172273_;
                                double d8 = d5 * d2 + p_172274_;
                                double d9 = d6 * d3 + p_172275_;
                                this.add(
                                    new TerrainParticle(
                                        this.level,
                                        (double)pos.getX() + d7,
                                        (double)pos.getY() + d8,
                                        (double)pos.getZ() + d9,
                                        d4 - 0.5,
                                        d5 - 0.5,
                                        d6 - 0.5,
                                        state,
                                        pos
                                    ).updateSprite(state, pos)
                                );
                            }
                        }
                    }
                }
            );
        }
    }

    /**
     * Adds block hit particles for the specified block
     */
    public void crack(BlockPos pos, Direction side) {
        BlockState blockstate = this.level.getBlockState(pos);
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE && blockstate.shouldSpawnTerrainParticles()) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            float f = 0.1F;
            AABB aabb = blockstate.getShape(this.level, pos).bounds();
            double d0 = (double)i + this.random.nextDouble() * (aabb.maxX - aabb.minX - 0.2F) + 0.1F + aabb.minX;
            double d1 = (double)j + this.random.nextDouble() * (aabb.maxY - aabb.minY - 0.2F) + 0.1F + aabb.minY;
            double d2 = (double)k + this.random.nextDouble() * (aabb.maxZ - aabb.minZ - 0.2F) + 0.1F + aabb.minZ;
            if (side == Direction.DOWN) {
                d1 = (double)j + aabb.minY - 0.1F;
            }

            if (side == Direction.UP) {
                d1 = (double)j + aabb.maxY + 0.1F;
            }

            if (side == Direction.NORTH) {
                d2 = (double)k + aabb.minZ - 0.1F;
            }

            if (side == Direction.SOUTH) {
                d2 = (double)k + aabb.maxZ + 0.1F;
            }

            if (side == Direction.WEST) {
                d0 = (double)i + aabb.minX - 0.1F;
            }

            if (side == Direction.EAST) {
                d0 = (double)i + aabb.maxX + 0.1F;
            }

            this.add((new TerrainParticle(this.level, d0, d1, d2, 0.0D, 0.0D, 0.0D, blockstate, pos).updateSprite(blockstate, pos)).setPower(0.2F).scale(0.6F));
        }
    }

    public String countParticles() {
        return String.valueOf(this.particles.values().stream().mapToInt(Collection::size).sum());
    }

    public void iterateParticles(java.util.function.Consumer<Particle> consumer) {
        for (ParticleRenderType particlerendertype : this.particles.keySet()) {
            if (particlerendertype == ParticleRenderType.NO_RENDER) continue;
            Iterable<Particle> iterable = this.particles.get(particlerendertype);
            if (iterable != null) {
                iterable.forEach(consumer);
            }
        }
    }

    public void addBlockHitEffects(BlockPos pos, net.minecraft.world.phys.BlockHitResult target) {
        BlockState state = level.getBlockState(pos);
        if (!net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions.of(state).addHitEffects(state, level, target, this))
            crack(pos, target.getDirection());
    }

    private boolean hasSpaceInParticleLimit(ParticleGroup group) {
        return this.trackedParticleCounts.getInt(group) < group.getLimit();
    }

    private void clearParticles() {
        this.particles.clear();
        this.particlesToAdd.clear();
        this.trackingEmitters.clear();
        this.trackedParticleCounts.clear();
    }

    @OnlyIn(Dist.CLIENT)
    static class MutableSpriteSet implements SpriteSet {
        private List<TextureAtlasSprite> sprites;

        @Override
        public TextureAtlasSprite get(int particleAge, int particleMaxAge) {
            return this.sprites.get(particleAge * (this.sprites.size() - 1) / particleMaxAge);
        }

        @Override
        public TextureAtlasSprite get(RandomSource random) {
            return this.sprites.get(random.nextInt(this.sprites.size()));
        }

        public void rebind(List<TextureAtlasSprite> sprites) {
            this.sprites = ImmutableList.copyOf(sprites);
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface SpriteParticleRegistration<T extends ParticleOptions> {
        ParticleProvider<T> create(SpriteSet sprites);
    }
}
