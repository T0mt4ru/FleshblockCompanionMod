package net.minecraft.data.advancements.packs;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.BeeNestDestroyedTrigger;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.BredAnimalsTrigger;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicates;
import net.minecraft.advancements.critereon.FilledBucketTrigger;
import net.minecraft.advancements.critereon.FishingRodHookedTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PickedUpItemTrigger;
import net.minecraft.advancements.critereon.PlayerInteractTrigger;
import net.minecraft.advancements.critereon.StartRidingTrigger;
import net.minecraft.advancements.critereon.TameAnimalTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;

public class VanillaHusbandryAdvancements implements AdvancementSubProvider {
    public static final List<EntityType<?>> BREEDABLE_ANIMALS = List.of(
        EntityType.HORSE,
        EntityType.DONKEY,
        EntityType.MULE,
        EntityType.SHEEP,
        EntityType.COW,
        EntityType.MOOSHROOM,
        EntityType.PIG,
        EntityType.CHICKEN,
        EntityType.WOLF,
        EntityType.OCELOT,
        EntityType.RABBIT,
        EntityType.LLAMA,
        EntityType.CAT,
        EntityType.PANDA,
        EntityType.FOX,
        EntityType.BEE,
        EntityType.HOGLIN,
        EntityType.STRIDER,
        EntityType.GOAT,
        EntityType.AXOLOTL,
        EntityType.CAMEL,
        EntityType.ARMADILLO
    );
    public static final List<EntityType<?>> INDIRECTLY_BREEDABLE_ANIMALS = List.of(EntityType.TURTLE, EntityType.FROG, EntityType.SNIFFER);
    private static final Item[] FISH = new Item[]{Items.COD, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.SALMON};
    private static final Item[] FISH_BUCKETS = new Item[]{Items.COD_BUCKET, Items.TROPICAL_FISH_BUCKET, Items.PUFFERFISH_BUCKET, Items.SALMON_BUCKET};
    private static final Item[] EDIBLE_ITEMS = new Item[]{
        Items.APPLE,
        Items.MUSHROOM_STEW,
        Items.BREAD,
        Items.PORKCHOP,
        Items.COOKED_PORKCHOP,
        Items.GOLDEN_APPLE,
        Items.ENCHANTED_GOLDEN_APPLE,
        Items.COD,
        Items.SALMON,
        Items.TROPICAL_FISH,
        Items.PUFFERFISH,
        Items.COOKED_COD,
        Items.COOKED_SALMON,
        Items.COOKIE,
        Items.MELON_SLICE,
        Items.BEEF,
        Items.COOKED_BEEF,
        Items.CHICKEN,
        Items.COOKED_CHICKEN,
        Items.ROTTEN_FLESH,
        Items.SPIDER_EYE,
        Items.CARROT,
        Items.POTATO,
        Items.BAKED_POTATO,
        Items.POISONOUS_POTATO,
        Items.GOLDEN_CARROT,
        Items.PUMPKIN_PIE,
        Items.RABBIT,
        Items.COOKED_RABBIT,
        Items.RABBIT_STEW,
        Items.MUTTON,
        Items.COOKED_MUTTON,
        Items.CHORUS_FRUIT,
        Items.BEETROOT,
        Items.BEETROOT_SOUP,
        Items.DRIED_KELP,
        Items.SUSPICIOUS_STEW,
        Items.SWEET_BERRIES,
        Items.HONEY_BOTTLE,
        Items.GLOW_BERRIES
    };
    public static final Item[] WAX_SCRAPING_TOOLS = new Item[]{
        Items.WOODEN_AXE, Items.GOLDEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE
    };

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> writer) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        AdvancementHolder advancementholder = Advancement.Builder.advancement()
            .display(
                Blocks.HAY_BLOCK,
                Component.translatable("advancements.husbandry.root.title"),
                Component.translatable("advancements.husbandry.root.description"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/husbandry.png"),
                AdvancementType.TASK,
                false,
                false,
                false
            )
            .addCriterion("consumed_item", ConsumeItemTrigger.TriggerInstance.usedItem())
            .save(writer, "husbandry/root");
        AdvancementHolder advancementholder1 = Advancement.Builder.advancement()
            .parent(advancementholder)
            .display(
                Items.WHEAT,
                Component.translatable("advancements.husbandry.plant_seed.title"),
                Component.translatable("advancements.husbandry.plant_seed.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .requirements(AdvancementRequirements.Strategy.OR)
            .addCriterion("wheat", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.WHEAT))
            .addCriterion("pumpkin_stem", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.PUMPKIN_STEM))
            .addCriterion("melon_stem", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.MELON_STEM))
            .addCriterion("beetroots", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.BEETROOTS))
            .addCriterion("nether_wart", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.NETHER_WART))
            .addCriterion("torchflower", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.TORCHFLOWER_CROP))
            .addCriterion("pitcher_pod", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.PITCHER_CROP))
            .save(writer, "husbandry/plant_seed");
        AdvancementHolder advancementholder2 = Advancement.Builder.advancement()
            .parent(advancementholder)
            .display(
                Items.WHEAT,
                Component.translatable("advancements.husbandry.breed_an_animal.title"),
                Component.translatable("advancements.husbandry.breed_an_animal.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .requirements(AdvancementRequirements.Strategy.OR)
            .addCriterion("bred", BredAnimalsTrigger.TriggerInstance.bredAnimals())
            .save(writer, "husbandry/breed_an_animal");
        createBreedAllAnimalsAdvancement(advancementholder2, writer, BREEDABLE_ANIMALS.stream(), INDIRECTLY_BREEDABLE_ANIMALS.stream());
        addFood(Advancement.Builder.advancement())
            .parent(advancementholder1)
            .display(
                Items.APPLE,
                Component.translatable("advancements.husbandry.balanced_diet.title"),
                Component.translatable("advancements.husbandry.balanced_diet.description"),
                null,
                AdvancementType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(100))
            .save(writer, "husbandry/balanced_diet");
        Advancement.Builder.advancement()
            .parent(advancementholder1)
            .display(
                Items.NETHERITE_HOE,
                Component.translatable("advancements.husbandry.netherite_hoe.title"),
                Component.translatable("advancements.husbandry.netherite_hoe.description"),
                null,
                AdvancementType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(100))
            .addCriterion("netherite_hoe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HOE))
            .save(writer, "husbandry/obtain_netherite_hoe");
        AdvancementHolder advancementholder3 = Advancement.Builder.advancement()
            .parent(advancementholder)
            .display(
                Items.LEAD,
                Component.translatable("advancements.husbandry.tame_an_animal.title"),
                Component.translatable("advancements.husbandry.tame_an_animal.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion("tamed_animal", TameAnimalTrigger.TriggerInstance.tamedAnimal())
            .save(writer, "husbandry/tame_an_animal");
        AdvancementHolder advancementholder4 = addFish(Advancement.Builder.advancement())
            .parent(advancementholder)
            .requirements(AdvancementRequirements.Strategy.OR)
            .display(
                Items.FISHING_ROD,
                Component.translatable("advancements.husbandry.fishy_business.title"),
                Component.translatable("advancements.husbandry.fishy_business.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .save(writer, "husbandry/fishy_business");
        AdvancementHolder advancementholder5 = addFishBuckets(Advancement.Builder.advancement())
            .parent(advancementholder4)
            .requirements(AdvancementRequirements.Strategy.OR)
            .display(
                Items.PUFFERFISH_BUCKET,
                Component.translatable("advancements.husbandry.tactical_fishing.title"),
                Component.translatable("advancements.husbandry.tactical_fishing.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .save(writer, "husbandry/tactical_fishing");
        AdvancementHolder advancementholder6 = Advancement.Builder.advancement()
            .parent(advancementholder5)
            .requirements(AdvancementRequirements.Strategy.OR)
            .addCriterion(
                BuiltInRegistries.ITEM.getKey(Items.AXOLOTL_BUCKET).getPath(),
                FilledBucketTrigger.TriggerInstance.filledBucket(ItemPredicate.Builder.item().of(Items.AXOLOTL_BUCKET))
            )
            .display(
                Items.AXOLOTL_BUCKET,
                Component.translatable("advancements.husbandry.axolotl_in_a_bucket.title"),
                Component.translatable("advancements.husbandry.axolotl_in_a_bucket.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .save(writer, "husbandry/axolotl_in_a_bucket");
        Advancement.Builder.advancement()
            .parent(advancementholder6)
            .addCriterion("kill_axolotl_target", EffectsChangedTrigger.TriggerInstance.gotEffectsFrom(EntityPredicate.Builder.entity().of(EntityType.AXOLOTL)))
            .display(
                Items.TROPICAL_FISH_BUCKET,
                Component.translatable("advancements.husbandry.kill_axolotl_target.title"),
                Component.translatable("advancements.husbandry.kill_axolotl_target.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .save(writer, "husbandry/kill_axolotl_target");
        addCatVariants(Advancement.Builder.advancement())
            .parent(advancementholder3)
            .display(
                Items.COD,
                Component.translatable("advancements.husbandry.complete_catalogue.title"),
                Component.translatable("advancements.husbandry.complete_catalogue.description"),
                null,
                AdvancementType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(50))
            .save(writer, "husbandry/complete_catalogue");
        addTamedWolfVariants(Advancement.Builder.advancement(), registries)
            .parent(advancementholder3)
            .display(
                Items.BONE,
                Component.translatable("advancements.husbandry.whole_pack.title"),
                Component.translatable("advancements.husbandry.whole_pack.description"),
                null,
                AdvancementType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(50))
            .save(writer, "husbandry/whole_pack");
        AdvancementHolder advancementholder7 = Advancement.Builder.advancement()
            .parent(advancementholder)
            .addCriterion(
                "safely_harvest_honey",
                ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                    LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BlockTags.BEEHIVES)).setSmokey(true),
                    ItemPredicate.Builder.item().of(Items.GLASS_BOTTLE)
                )
            )
            .display(
                Items.HONEY_BOTTLE,
                Component.translatable("advancements.husbandry.safely_harvest_honey.title"),
                Component.translatable("advancements.husbandry.safely_harvest_honey.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .save(writer, "husbandry/safely_harvest_honey");
        AdvancementHolder advancementholder8 = Advancement.Builder.advancement()
            .parent(advancementholder7)
            .display(
                Items.HONEYCOMB,
                Component.translatable("advancements.husbandry.wax_on.title"),
                Component.translatable("advancements.husbandry.wax_on.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "wax_on",
                ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                    LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(HoneycombItem.WAXABLES.get().keySet())),
                    ItemPredicate.Builder.item().of(Items.HONEYCOMB)
                )
            )
            .save(writer, "husbandry/wax_on");
        Advancement.Builder.advancement()
            .parent(advancementholder8)
            .display(
                Items.STONE_AXE,
                Component.translatable("advancements.husbandry.wax_off.title"),
                Component.translatable("advancements.husbandry.wax_off.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "wax_off",
                ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                    LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(HoneycombItem.WAX_OFF_BY_BLOCK.get().keySet())),
                    ItemPredicate.Builder.item().of(WAX_SCRAPING_TOOLS)
                )
            )
            .save(writer, "husbandry/wax_off");
        AdvancementHolder advancementholder9 = Advancement.Builder.advancement()
            .parent(advancementholder)
            .addCriterion(
                BuiltInRegistries.ITEM.getKey(Items.TADPOLE_BUCKET).getPath(),
                FilledBucketTrigger.TriggerInstance.filledBucket(ItemPredicate.Builder.item().of(Items.TADPOLE_BUCKET))
            )
            .display(
                Items.TADPOLE_BUCKET,
                Component.translatable("advancements.husbandry.tadpole_in_a_bucket.title"),
                Component.translatable("advancements.husbandry.tadpole_in_a_bucket.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .save(writer, "husbandry/tadpole_in_a_bucket");
        AdvancementHolder advancementholder10 = addLeashedFrogVariants(Advancement.Builder.advancement())
            .parent(advancementholder9)
            .display(
                Items.LEAD,
                Component.translatable("advancements.husbandry.leash_all_frog_variants.title"),
                Component.translatable("advancements.husbandry.leash_all_frog_variants.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .save(writer, "husbandry/leash_all_frog_variants");
        Advancement.Builder.advancement()
            .parent(advancementholder10)
            .display(
                Items.VERDANT_FROGLIGHT,
                Component.translatable("advancements.husbandry.froglights.title"),
                Component.translatable("advancements.husbandry.froglights.description"),
                null,
                AdvancementType.CHALLENGE,
                true,
                true,
                false
            )
            .addCriterion(
                "froglights", InventoryChangeTrigger.TriggerInstance.hasItems(Items.OCHRE_FROGLIGHT, Items.PEARLESCENT_FROGLIGHT, Items.VERDANT_FROGLIGHT)
            )
            .save(writer, "husbandry/froglights");
        Advancement.Builder.advancement()
            .parent(advancementholder)
            .addCriterion(
                "silk_touch_nest",
                BeeNestDestroyedTrigger.TriggerInstance.destroyedBeeNest(
                    Blocks.BEE_NEST,
                    ItemPredicate.Builder.item()
                        .withSubPredicate(
                            ItemSubPredicates.ENCHANTMENTS,
                            ItemEnchantmentsPredicate.enchantments(
                                List.of(new EnchantmentPredicate(registrylookup.getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1)))
                            )
                        ),
                    MinMaxBounds.Ints.exactly(3)
                )
            )
            .display(
                Blocks.BEE_NEST,
                Component.translatable("advancements.husbandry.silk_touch_nest.title"),
                Component.translatable("advancements.husbandry.silk_touch_nest.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .save(writer, "husbandry/silk_touch_nest");
        Advancement.Builder.advancement()
            .parent(advancementholder)
            .display(
                Items.OAK_BOAT,
                Component.translatable("advancements.husbandry.ride_a_boat_with_a_goat.title"),
                Component.translatable("advancements.husbandry.ride_a_boat_with_a_goat.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "ride_a_boat_with_a_goat",
                StartRidingTrigger.TriggerInstance.playerStartsRiding(
                    EntityPredicate.Builder.entity()
                        .vehicle(EntityPredicate.Builder.entity().of(EntityType.BOAT).passenger(EntityPredicate.Builder.entity().of(EntityType.GOAT)))
                )
            )
            .save(writer, "husbandry/ride_a_boat_with_a_goat");
        Advancement.Builder.advancement()
            .parent(advancementholder)
            .display(
                Items.GLOW_INK_SAC,
                Component.translatable("advancements.husbandry.make_a_sign_glow.title"),
                Component.translatable("advancements.husbandry.make_a_sign_glow.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "make_a_sign_glow",
                ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                    LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BlockTags.ALL_SIGNS)),
                    ItemPredicate.Builder.item().of(Items.GLOW_INK_SAC)
                )
            )
            .save(writer, "husbandry/make_a_sign_glow");
        AdvancementHolder advancementholder11 = Advancement.Builder.advancement()
            .parent(advancementholder)
            .display(
                Items.COOKIE,
                Component.translatable("advancements.husbandry.allay_deliver_item_to_player.title"),
                Component.translatable("advancements.husbandry.allay_deliver_item_to_player.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                true
            )
            .addCriterion(
                "allay_deliver_item_to_player",
                PickedUpItemTrigger.TriggerInstance.thrownItemPickedUpByPlayer(
                    Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.ALLAY)))
                )
            )
            .save(writer, "husbandry/allay_deliver_item_to_player");
        Advancement.Builder.advancement()
            .parent(advancementholder11)
            .display(
                Items.NOTE_BLOCK,
                Component.translatable("advancements.husbandry.allay_deliver_cake_to_note_block.title"),
                Component.translatable("advancements.husbandry.allay_deliver_cake_to_note_block.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                true
            )
            .addCriterion(
                "allay_deliver_cake_to_note_block",
                ItemUsedOnLocationTrigger.TriggerInstance.allayDropItemOnBlock(
                    LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(Blocks.NOTE_BLOCK)),
                    ItemPredicate.Builder.item().of(Items.CAKE)
                )
            )
            .save(writer, "husbandry/allay_deliver_cake_to_note_block");
        AdvancementHolder advancementholder12 = Advancement.Builder.advancement()
            .parent(advancementholder)
            .display(
                Items.SNIFFER_EGG,
                Component.translatable("advancements.husbandry.obtain_sniffer_egg.title"),
                Component.translatable("advancements.husbandry.obtain_sniffer_egg.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                true
            )
            .addCriterion("obtain_sniffer_egg", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SNIFFER_EGG))
            .save(writer, "husbandry/obtain_sniffer_egg");
        AdvancementHolder advancementholder13 = Advancement.Builder.advancement()
            .parent(advancementholder12)
            .display(
                Items.TORCHFLOWER_SEEDS,
                Component.translatable("advancements.husbandry.feed_snifflet.title"),
                Component.translatable("advancements.husbandry.feed_snifflet.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                true
            )
            .addCriterion(
                "feed_snifflet",
                PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                    ItemPredicate.Builder.item().of(ItemTags.SNIFFER_FOOD),
                    Optional.of(
                        EntityPredicate.wrap(
                            EntityPredicate.Builder.entity().of(EntityType.SNIFFER).flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true))
                        )
                    )
                )
            )
            .save(writer, "husbandry/feed_snifflet");
        Advancement.Builder.advancement()
            .parent(advancementholder13)
            .display(
                Items.PITCHER_POD,
                Component.translatable("advancements.husbandry.plant_any_sniffer_seed.title"),
                Component.translatable("advancements.husbandry.plant_any_sniffer_seed.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                true
            )
            .requirements(AdvancementRequirements.Strategy.OR)
            .addCriterion("torchflower", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.TORCHFLOWER_CROP))
            .addCriterion("pitcher_pod", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.PITCHER_CROP))
            .save(writer, "husbandry/plant_any_sniffer_seed");
        Advancement.Builder.advancement()
            .parent(advancementholder3)
            .display(
                Items.SHEARS,
                Component.translatable("advancements.husbandry.remove_wolf_armor.title"),
                Component.translatable("advancements.husbandry.remove_wolf_armor.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "remove_wolf_armor",
                PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                    ItemPredicate.Builder.item().of(Items.SHEARS), Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.WOLF)))
                )
            )
            .save(writer, "husbandry/remove_wolf_armor");
        Advancement.Builder.advancement()
            .parent(advancementholder3)
            .display(
                Items.WOLF_ARMOR,
                Component.translatable("advancements.husbandry.repair_wolf_armor.title"),
                Component.translatable("advancements.husbandry.repair_wolf_armor.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
            )
            .addCriterion(
                "repair_wolf_armor",
                PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                    ItemPredicate.Builder.item().of(Items.ARMADILLO_SCUTE),
                    Optional.of(
                        EntityPredicate.wrap(
                            EntityPredicate.Builder.entity()
                                .of(EntityType.WOLF)
                                .equipment(
                                    EntityEquipmentPredicate.Builder.equipment()
                                        .body(
                                            ItemPredicate.Builder.item()
                                                .of(Items.WOLF_ARMOR)
                                                .hasComponents(DataComponentPredicate.builder().expect(DataComponents.DAMAGE, 0).build())
                                        )
                                )
                        )
                    )
                )
            )
            .save(writer, "husbandry/repair_wolf_armor");
    }

    public static AdvancementHolder createBreedAllAnimalsAdvancement(
        AdvancementHolder parent, Consumer<AdvancementHolder> writer, Stream<EntityType<?>> breedableAnimals, Stream<EntityType<?>> indirectlyBreedableAnimals
    ) {
        return addBreedable(Advancement.Builder.advancement(), breedableAnimals, indirectlyBreedableAnimals)
            .parent(parent)
            .display(
                Items.GOLDEN_CARROT,
                Component.translatable("advancements.husbandry.breed_all_animals.title"),
                Component.translatable("advancements.husbandry.breed_all_animals.description"),
                null,
                AdvancementType.CHALLENGE,
                true,
                true,
                false
            )
            .rewards(AdvancementRewards.Builder.experience(100))
            .save(writer, "husbandry/bred_all_animals");
    }

    private static Advancement.Builder addLeashedFrogVariants(Advancement.Builder builder) {
        BuiltInRegistries.FROG_VARIANT
            .holders()
            .forEach(
                p_335189_ -> builder.addCriterion(
                        p_335189_.key().location().toString(),
                        PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                            ItemPredicate.Builder.item().of(Items.LEAD),
                            Optional.of(
                                EntityPredicate.wrap(
                                    EntityPredicate.Builder.entity().of(EntityType.FROG).subPredicate(EntitySubPredicates.frogVariant(p_335189_))
                                )
                            )
                        )
                    )
            );
        return builder;
    }

    /**
     * Adds all the items in {@link #EDIBLE_ITEMS} to the given advancement's criteria
     */
    private static Advancement.Builder addFood(Advancement.Builder builder) {
        for (Item item : EDIBLE_ITEMS) {
            builder.addCriterion(BuiltInRegistries.ITEM.getKey(item).getPath(), ConsumeItemTrigger.TriggerInstance.usedItem(item));
        }

        return builder;
    }

    private static Advancement.Builder addBreedable(Advancement.Builder builder, Stream<EntityType<?>> breedableAnimals, Stream<EntityType<?>> indirectlyBreedableAnimals) {
        breedableAnimals.forEach(
            p_300705_ -> builder.addCriterion(
                    EntityType.getKey((EntityType<?>)p_300705_).toString(),
                    BredAnimalsTrigger.TriggerInstance.bredAnimals(EntityPredicate.Builder.entity().of((EntityType<?>)p_300705_))
                )
        );
        indirectlyBreedableAnimals.forEach(
            p_300701_ -> builder.addCriterion(
                    EntityType.getKey((EntityType<?>)p_300701_).toString(),
                    BredAnimalsTrigger.TriggerInstance.bredAnimals(
                        Optional.of(EntityPredicate.Builder.entity().of((EntityType<?>)p_300701_).build()),
                        Optional.of(EntityPredicate.Builder.entity().of((EntityType<?>)p_300701_).build()),
                        Optional.empty()
                    )
                )
        );
        return builder;
    }

    private static Advancement.Builder addFishBuckets(Advancement.Builder builder) {
        for (Item item : FISH_BUCKETS) {
            builder.addCriterion(
                BuiltInRegistries.ITEM.getKey(item).getPath(), FilledBucketTrigger.TriggerInstance.filledBucket(ItemPredicate.Builder.item().of(item))
            );
        }

        return builder;
    }

    private static Advancement.Builder addFish(Advancement.Builder builder) {
        for (Item item : FISH) {
            builder.addCriterion(
                BuiltInRegistries.ITEM.getKey(item).getPath(),
                FishingRodHookedTrigger.TriggerInstance.fishedItem(
                    Optional.empty(), Optional.empty(), Optional.of(ItemPredicate.Builder.item().of(item).build())
                )
            );
        }

        return builder;
    }

    private static Advancement.Builder addCatVariants(Advancement.Builder builder) {
        BuiltInRegistries.CAT_VARIANT
            .holders()
            .sorted(Comparator.comparing(p_335187_ -> p_335187_.key().location()))
            .forEach(
                p_335186_ -> builder.addCriterion(
                        p_335186_.key().location().toString(),
                        TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().subPredicate(EntitySubPredicates.catVariant(p_335186_)))
                    )
            );
        return builder;
    }

    private static Advancement.Builder addTamedWolfVariants(Advancement.Builder builder, HolderLookup.Provider registries) {
        HolderLookup.RegistryLookup<WolfVariant> registrylookup = registries.lookupOrThrow(Registries.WOLF_VARIANT);
        registrylookup.listElementIds()
            .sorted(Comparator.comparing(ResourceKey::location))
            .forEach(
                p_340626_ -> {
                    Holder<WolfVariant> holder = registrylookup.getOrThrow((ResourceKey<WolfVariant>)p_340626_);
                    builder.addCriterion(
                        p_340626_.location().toString(),
                        TameAnimalTrigger.TriggerInstance.tamedAnimal(
                            EntityPredicate.Builder.entity().subPredicate(EntitySubPredicates.wolfVariant(HolderSet.direct(holder)))
                        )
                    );
                }
            );
        return builder;
    }
}
