package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class EntityRenderers {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<EntityType<?>, EntityRendererProvider<?>> PROVIDERS = new Object2ObjectOpenHashMap<>();
    private static final Map<PlayerSkin.Model, EntityRendererProvider<AbstractClientPlayer>> PLAYER_PROVIDERS = Map.of(
        PlayerSkin.Model.WIDE, p_174098_ -> new PlayerRenderer(p_174098_, false), PlayerSkin.Model.SLIM, p_174096_ -> new PlayerRenderer(p_174096_, true)
    );

    public static <T extends Entity> void register(EntityType<? extends T> entityType, EntityRendererProvider<T> provider) {
        PROVIDERS.put(entityType, provider);
    }

    public static Map<EntityType<?>, EntityRenderer<?>> createEntityRenderers(EntityRendererProvider.Context context) {
        Builder<EntityType<?>, EntityRenderer<?>> builder = ImmutableMap.builder();
        PROVIDERS.forEach((p_339310_, p_339311_) -> {
            try {
                builder.put((EntityType<?>)p_339310_, p_339311_.create(context));
            } catch (Exception exception) {
                throw new IllegalArgumentException("Failed to create model for " + BuiltInRegistries.ENTITY_TYPE.getKey((EntityType<?>)p_339310_), exception);
            }
        });
        return builder.build();
    }

    public static Map<PlayerSkin.Model, EntityRenderer<? extends Player>> createPlayerRenderers(EntityRendererProvider.Context context) {
        Builder<PlayerSkin.Model, EntityRenderer<? extends Player>> builder = ImmutableMap.builder();
        PLAYER_PROVIDERS.forEach((p_339306_, p_339307_) -> {
            try {
                builder.put(p_339306_, p_339307_.create(context));
            } catch (Exception exception) {
                throw new IllegalArgumentException("Failed to create player model for " + p_339306_, exception);
            }
        });
        return builder.build();
    }

    public static boolean validateRegistrations() {
        boolean flag = true;

        for (EntityType<?> entitytype : BuiltInRegistries.ENTITY_TYPE) {
            if (entitytype != EntityType.PLAYER && !PROVIDERS.containsKey(entitytype)) {
                LOGGER.warn("No renderer registered for {}", BuiltInRegistries.ENTITY_TYPE.getKey(entitytype));
                flag = false;
            }
        }

        return !flag;
    }

    static {
        register(EntityType.ALLAY, AllayRenderer::new);
        register(EntityType.AREA_EFFECT_CLOUD, NoopRenderer::new);
        register(EntityType.ARMADILLO, ArmadilloRenderer::new);
        register(EntityType.ARMOR_STAND, ArmorStandRenderer::new);
        register(EntityType.ARROW, TippableArrowRenderer::new);
        register(EntityType.AXOLOTL, AxolotlRenderer::new);
        register(EntityType.BAT, BatRenderer::new);
        register(EntityType.BEE, BeeRenderer::new);
        register(EntityType.BLAZE, BlazeRenderer::new);
        register(EntityType.BLOCK_DISPLAY, DisplayRenderer.BlockDisplayRenderer::new);
        register(EntityType.BOAT, p_174094_ -> new BoatRenderer(p_174094_, false));
        register(EntityType.BOGGED, BoggedRenderer::new);
        register(EntityType.BREEZE, BreezeRenderer::new);
        register(EntityType.BREEZE_WIND_CHARGE, WindChargeRenderer::new);
        register(EntityType.CAT, CatRenderer::new);
        register(EntityType.CAMEL, p_247942_ -> new CamelRenderer(p_247942_, ModelLayers.CAMEL));
        register(EntityType.CAVE_SPIDER, CaveSpiderRenderer::new);
        register(EntityType.CHEST_BOAT, p_174092_ -> new BoatRenderer(p_174092_, true));
        register(EntityType.CHEST_MINECART, p_174090_ -> new MinecartRenderer<>(p_174090_, ModelLayers.CHEST_MINECART));
        register(EntityType.CHICKEN, ChickenRenderer::new);
        register(EntityType.COD, CodRenderer::new);
        register(EntityType.COMMAND_BLOCK_MINECART, p_174088_ -> new MinecartRenderer<>(p_174088_, ModelLayers.COMMAND_BLOCK_MINECART));
        register(EntityType.COW, CowRenderer::new);
        register(EntityType.CREEPER, CreeperRenderer::new);
        register(EntityType.DOLPHIN, DolphinRenderer::new);
        register(EntityType.DONKEY, p_174086_ -> new ChestedHorseRenderer<>(p_174086_, 0.87F, ModelLayers.DONKEY));
        register(EntityType.DRAGON_FIREBALL, DragonFireballRenderer::new);
        register(EntityType.DROWNED, DrownedRenderer::new);
        register(EntityType.EGG, ThrownItemRenderer::new);
        register(EntityType.ELDER_GUARDIAN, ElderGuardianRenderer::new);
        register(EntityType.ENDERMAN, EndermanRenderer::new);
        register(EntityType.ENDERMITE, EndermiteRenderer::new);
        register(EntityType.ENDER_DRAGON, EnderDragonRenderer::new);
        register(EntityType.ENDER_PEARL, ThrownItemRenderer::new);
        register(EntityType.END_CRYSTAL, EndCrystalRenderer::new);
        register(EntityType.EVOKER, EvokerRenderer::new);
        register(EntityType.EVOKER_FANGS, EvokerFangsRenderer::new);
        register(EntityType.EXPERIENCE_BOTTLE, ThrownItemRenderer::new);
        register(EntityType.EXPERIENCE_ORB, ExperienceOrbRenderer::new);
        register(EntityType.EYE_OF_ENDER, p_174084_ -> new ThrownItemRenderer<>(p_174084_, 1.0F, true));
        register(EntityType.FALLING_BLOCK, FallingBlockRenderer::new);
        register(EntityType.FIREBALL, p_174082_ -> new ThrownItemRenderer<>(p_174082_, 3.0F, true));
        register(EntityType.FIREWORK_ROCKET, FireworkEntityRenderer::new);
        register(EntityType.FISHING_BOBBER, FishingHookRenderer::new);
        register(EntityType.FOX, FoxRenderer::new);
        register(EntityType.FROG, FrogRenderer::new);
        register(EntityType.FURNACE_MINECART, p_174080_ -> new MinecartRenderer<>(p_174080_, ModelLayers.FURNACE_MINECART));
        register(EntityType.GHAST, GhastRenderer::new);
        register(EntityType.GIANT, p_174078_ -> new GiantMobRenderer(p_174078_, 6.0F));
        register(EntityType.GLOW_ITEM_FRAME, ItemFrameRenderer::new);
        register(EntityType.GLOW_SQUID, p_174076_ -> new GlowSquidRenderer(p_174076_, new SquidModel<>(p_174076_.bakeLayer(ModelLayers.GLOW_SQUID))));
        register(EntityType.GOAT, GoatRenderer::new);
        register(EntityType.GUARDIAN, GuardianRenderer::new);
        register(EntityType.HOGLIN, HoglinRenderer::new);
        register(EntityType.HOPPER_MINECART, p_174074_ -> new MinecartRenderer<>(p_174074_, ModelLayers.HOPPER_MINECART));
        register(EntityType.HORSE, HorseRenderer::new);
        register(EntityType.HUSK, HuskRenderer::new);
        register(EntityType.ILLUSIONER, IllusionerRenderer::new);
        register(EntityType.INTERACTION, NoopRenderer::new);
        register(EntityType.IRON_GOLEM, IronGolemRenderer::new);
        register(EntityType.ITEM, ItemEntityRenderer::new);
        register(EntityType.ITEM_DISPLAY, DisplayRenderer.ItemDisplayRenderer::new);
        register(EntityType.ITEM_FRAME, ItemFrameRenderer::new);
        register(EntityType.OMINOUS_ITEM_SPAWNER, OminousItemSpawnerRenderer::new);
        register(EntityType.LEASH_KNOT, LeashKnotRenderer::new);
        register(EntityType.LIGHTNING_BOLT, LightningBoltRenderer::new);
        register(EntityType.LLAMA, p_174072_ -> new LlamaRenderer(p_174072_, ModelLayers.LLAMA));
        register(EntityType.LLAMA_SPIT, LlamaSpitRenderer::new);
        register(EntityType.MAGMA_CUBE, MagmaCubeRenderer::new);
        register(EntityType.MARKER, NoopRenderer::new);
        register(EntityType.MINECART, p_174070_ -> new MinecartRenderer<>(p_174070_, ModelLayers.MINECART));
        register(EntityType.MOOSHROOM, MushroomCowRenderer::new);
        register(EntityType.MULE, p_174068_ -> new ChestedHorseRenderer<>(p_174068_, 0.92F, ModelLayers.MULE));
        register(EntityType.OCELOT, OcelotRenderer::new);
        register(EntityType.PAINTING, PaintingRenderer::new);
        register(EntityType.PANDA, PandaRenderer::new);
        register(EntityType.PARROT, ParrotRenderer::new);
        register(EntityType.PHANTOM, PhantomRenderer::new);
        register(EntityType.PIG, PigRenderer::new);
        register(
            EntityType.PIGLIN,
            p_174066_ -> new PiglinRenderer(p_174066_, ModelLayers.PIGLIN, ModelLayers.PIGLIN_INNER_ARMOR, ModelLayers.PIGLIN_OUTER_ARMOR, false)
        );
        register(
            EntityType.PIGLIN_BRUTE,
            p_174064_ -> new PiglinRenderer(
                    p_174064_, ModelLayers.PIGLIN_BRUTE, ModelLayers.PIGLIN_BRUTE_INNER_ARMOR, ModelLayers.PIGLIN_BRUTE_OUTER_ARMOR, false
                )
        );
        register(EntityType.PILLAGER, PillagerRenderer::new);
        register(EntityType.POLAR_BEAR, PolarBearRenderer::new);
        register(EntityType.POTION, ThrownItemRenderer::new);
        register(EntityType.PUFFERFISH, PufferfishRenderer::new);
        register(EntityType.RABBIT, RabbitRenderer::new);
        register(EntityType.RAVAGER, RavagerRenderer::new);
        register(EntityType.SALMON, SalmonRenderer::new);
        register(EntityType.SHEEP, SheepRenderer::new);
        register(EntityType.SHULKER, ShulkerRenderer::new);
        register(EntityType.SHULKER_BULLET, ShulkerBulletRenderer::new);
        register(EntityType.SILVERFISH, SilverfishRenderer::new);
        register(EntityType.SKELETON, SkeletonRenderer::new);
        register(EntityType.SKELETON_HORSE, p_174062_ -> new UndeadHorseRenderer(p_174062_, ModelLayers.SKELETON_HORSE));
        register(EntityType.SLIME, SlimeRenderer::new);
        register(EntityType.SMALL_FIREBALL, p_174060_ -> new ThrownItemRenderer<>(p_174060_, 0.75F, true));
        register(EntityType.SNIFFER, SnifferRenderer::new);
        register(EntityType.SNOWBALL, ThrownItemRenderer::new);
        register(EntityType.SNOW_GOLEM, SnowGolemRenderer::new);
        register(EntityType.SPAWNER_MINECART, p_174058_ -> new MinecartRenderer<>(p_174058_, ModelLayers.SPAWNER_MINECART));
        register(EntityType.SPECTRAL_ARROW, SpectralArrowRenderer::new);
        register(EntityType.SPIDER, SpiderRenderer::new);
        register(EntityType.SQUID, p_174056_ -> new SquidRenderer<>(p_174056_, new SquidModel<>(p_174056_.bakeLayer(ModelLayers.SQUID))));
        register(EntityType.STRAY, StrayRenderer::new);
        register(EntityType.STRIDER, StriderRenderer::new);
        register(EntityType.TADPOLE, TadpoleRenderer::new);
        register(EntityType.TEXT_DISPLAY, DisplayRenderer.TextDisplayRenderer::new);
        register(EntityType.TNT, TntRenderer::new);
        register(EntityType.TNT_MINECART, TntMinecartRenderer::new);
        register(EntityType.TRADER_LLAMA, p_174054_ -> new LlamaRenderer(p_174054_, ModelLayers.TRADER_LLAMA));
        register(EntityType.TRIDENT, ThrownTridentRenderer::new);
        register(EntityType.TROPICAL_FISH, TropicalFishRenderer::new);
        register(EntityType.TURTLE, TurtleRenderer::new);
        register(EntityType.VEX, VexRenderer::new);
        register(EntityType.VILLAGER, VillagerRenderer::new);
        register(EntityType.VINDICATOR, VindicatorRenderer::new);
        register(EntityType.WARDEN, WardenRenderer::new);
        register(EntityType.WANDERING_TRADER, WanderingTraderRenderer::new);
        register(EntityType.WIND_CHARGE, WindChargeRenderer::new);
        register(EntityType.WITCH, WitchRenderer::new);
        register(EntityType.WITHER, WitherBossRenderer::new);
        register(EntityType.WITHER_SKELETON, WitherSkeletonRenderer::new);
        register(EntityType.WITHER_SKULL, WitherSkullRenderer::new);
        register(EntityType.WOLF, WolfRenderer::new);
        register(EntityType.ZOGLIN, ZoglinRenderer::new);
        register(EntityType.ZOMBIE, ZombieRenderer::new);
        register(EntityType.ZOMBIE_HORSE, p_234612_ -> new UndeadHorseRenderer(p_234612_, ModelLayers.ZOMBIE_HORSE));
        register(EntityType.ZOMBIE_VILLAGER, ZombieVillagerRenderer::new);
        register(
            EntityType.ZOMBIFIED_PIGLIN,
            p_234610_ -> new PiglinRenderer(
                    p_234610_, ModelLayers.ZOMBIFIED_PIGLIN, ModelLayers.ZOMBIFIED_PIGLIN_INNER_ARMOR, ModelLayers.ZOMBIFIED_PIGLIN_OUTER_ARMOR, true
                )
        );
    }
}
