package net.minecraft.data.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ItemModelGenerators {
    public static final ResourceLocation TRIM_TYPE_PREDICATE_ID = ResourceLocation.withDefaultNamespace("trim_type");
    public static final List<ItemModelGenerators.TrimModelData> GENERATED_TRIM_MODELS = List.of(
        new ItemModelGenerators.TrimModelData("quartz", 0.1F, Map.of()),
        new ItemModelGenerators.TrimModelData("iron", 0.2F, Map.of(ArmorMaterials.IRON, "iron_darker")),
        new ItemModelGenerators.TrimModelData("netherite", 0.3F, Map.of(ArmorMaterials.NETHERITE, "netherite_darker")),
        new ItemModelGenerators.TrimModelData("redstone", 0.4F, Map.of()),
        new ItemModelGenerators.TrimModelData("copper", 0.5F, Map.of()),
        new ItemModelGenerators.TrimModelData("gold", 0.6F, Map.of(ArmorMaterials.GOLD, "gold_darker")),
        new ItemModelGenerators.TrimModelData("emerald", 0.7F, Map.of()),
        new ItemModelGenerators.TrimModelData("diamond", 0.8F, Map.of(ArmorMaterials.DIAMOND, "diamond_darker")),
        new ItemModelGenerators.TrimModelData("lapis", 0.9F, Map.of()),
        new ItemModelGenerators.TrimModelData("amethyst", 1.0F, Map.of())
    );
    private final BiConsumer<ResourceLocation, Supplier<JsonElement>> output;

    public ItemModelGenerators(BiConsumer<ResourceLocation, Supplier<JsonElement>> output) {
        this.output = output;
    }

    private void generateFlatItem(Item item, ModelTemplate modelTemplate) {
        modelTemplate.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(item), this.output);
    }

    private void generateFlatItem(Item item, String modelLocationSuffix, ModelTemplate modelTemplate) {
        modelTemplate.create(
            ModelLocationUtils.getModelLocation(item, modelLocationSuffix), TextureMapping.layer0(TextureMapping.getItemTexture(item, modelLocationSuffix)), this.output
        );
    }

    private void generateFlatItem(Item item, Item layerZeroItem, ModelTemplate modelTemplate) {
        modelTemplate.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(layerZeroItem), this.output);
    }

    private void generateItemWithOverlay(Item item) {
        this.generateLayeredItem(
            ModelLocationUtils.getModelLocation(item), TextureMapping.getItemTexture(item), TextureMapping.getItemTexture(item, "_overlay")
        );
    }

    private void generateCompassItem(Item item) {
        for (int i = 0; i < 32; i++) {
            if (i != 16) {
                this.generateFlatItem(item, String.format(Locale.ROOT, "_%02d", i), ModelTemplates.FLAT_ITEM);
            }
        }
    }

    private void generateClockItem(Item item) {
        for (int i = 1; i < 64; i++) {
            this.generateFlatItem(item, String.format(Locale.ROOT, "_%02d", i), ModelTemplates.FLAT_ITEM);
        }
    }

    private void generateLayeredItem(ResourceLocation modelLocation, ResourceLocation layer0, ResourceLocation layer1) {
        ModelTemplates.TWO_LAYERED_ITEM.create(modelLocation, TextureMapping.layered(layer0, layer1), this.output);
    }

    private void generateLayeredItem(ResourceLocation modelLocation, ResourceLocation layer0, ResourceLocation layer1, ResourceLocation layer2) {
        ModelTemplates.THREE_LAYERED_ITEM.create(modelLocation, TextureMapping.layered(layer0, layer1, layer2), this.output);
    }

    private ResourceLocation getItemModelForTrimMaterial(ResourceLocation modelLocation, String trimId) {
        return modelLocation.withSuffix("_" + trimId + "_trim");
    }

    private JsonObject generateBaseArmorTrimTemplate(ResourceLocation modelLocation, Map<TextureSlot, ResourceLocation> modelGetter, Holder<ArmorMaterial> armorMaterial) {
        JsonObject jsonobject = ModelTemplates.TWO_LAYERED_ITEM.createBaseTemplate(modelLocation, modelGetter);
        JsonArray jsonarray = new JsonArray();

        for (ItemModelGenerators.TrimModelData itemmodelgenerators$trimmodeldata : GENERATED_TRIM_MODELS) {
            JsonObject jsonobject1 = new JsonObject();
            JsonObject jsonobject2 = new JsonObject();
            jsonobject2.addProperty(TRIM_TYPE_PREDICATE_ID.getPath(), itemmodelgenerators$trimmodeldata.itemModelIndex());
            jsonobject1.add("predicate", jsonobject2);
            jsonobject1.addProperty("model", this.getItemModelForTrimMaterial(modelLocation, itemmodelgenerators$trimmodeldata.name(armorMaterial)).toString());
            jsonarray.add(jsonobject1);
        }

        jsonobject.add("overrides", jsonarray);
        return jsonobject;
    }

    private void generateArmorTrims(ArmorItem armorItem) {
        if (armorItem.getType().hasTrims()) {
            ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(armorItem);
            ResourceLocation resourcelocation1 = TextureMapping.getItemTexture(armorItem);
            ResourceLocation resourcelocation2 = TextureMapping.getItemTexture(armorItem, "_overlay");
            if (armorItem.getMaterial().is(ArmorMaterials.LEATHER)) {
                ModelTemplates.TWO_LAYERED_ITEM
                    .create(
                        resourcelocation,
                        TextureMapping.layered(resourcelocation1, resourcelocation2),
                        this.output,
                        (p_323130_, p_323131_) -> this.generateBaseArmorTrimTemplate(p_323130_, p_323131_, armorItem.getMaterial())
                    );
            } else {
                ModelTemplates.FLAT_ITEM
                    .create(
                        resourcelocation,
                        TextureMapping.layer0(resourcelocation1),
                        this.output,
                        (p_323127_, p_323128_) -> this.generateBaseArmorTrimTemplate(p_323127_, p_323128_, armorItem.getMaterial())
                    );
            }

            for (ItemModelGenerators.TrimModelData itemmodelgenerators$trimmodeldata : GENERATED_TRIM_MODELS) {
                String s = itemmodelgenerators$trimmodeldata.name(armorItem.getMaterial());
                ResourceLocation resourcelocation3 = this.getItemModelForTrimMaterial(resourcelocation, s);
                String s1 = armorItem.getType().getName() + "_trim_" + s;
                ResourceLocation resourcelocation4 = ResourceLocation.withDefaultNamespace(s1).withPrefix("trims/items/");
                if (armorItem.getMaterial().is(ArmorMaterials.LEATHER)) {
                    this.generateLayeredItem(resourcelocation3, resourcelocation1, resourcelocation2, resourcelocation4);
                } else {
                    this.generateLayeredItem(resourcelocation3, resourcelocation1, resourcelocation4);
                }
            }
        }
    }

    public void run() {
        this.generateFlatItem(Items.ACACIA_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.CHERRY_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.ACACIA_CHEST_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.CHERRY_CHEST_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.AMETHYST_SHARD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.APPLE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.ARMADILLO_SCUTE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.ARMOR_STAND, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.ARROW, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BAKED_POTATO, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BAMBOO, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.BEEF, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BEETROOT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BEETROOT_SOUP, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BIRCH_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BIRCH_CHEST_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BLACK_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BLAZE_POWDER, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BLAZE_ROD, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.BLUE_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BONE_MEAL, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BOOK, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BOWL, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BREAD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BRICK, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BREEZE_ROD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BROWN_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BUCKET, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.CARROT_ON_A_STICK, ModelTemplates.FLAT_HANDHELD_ROD_ITEM);
        this.generateFlatItem(Items.WARPED_FUNGUS_ON_A_STICK, ModelTemplates.FLAT_HANDHELD_ROD_ITEM);
        this.generateFlatItem(Items.CHARCOAL, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.CHEST_MINECART, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.CHICKEN, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.CHORUS_FRUIT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.CLAY_BALL, ModelTemplates.FLAT_ITEM);
        this.generateClockItem(Items.CLOCK);
        this.generateFlatItem(Items.COAL, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.COD_BUCKET, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.COMMAND_BLOCK_MINECART, ModelTemplates.FLAT_ITEM);
        this.generateCompassItem(Items.COMPASS);
        this.generateCompassItem(Items.RECOVERY_COMPASS);
        this.generateFlatItem(Items.COOKED_BEEF, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.COOKED_CHICKEN, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.COOKED_COD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.COOKED_MUTTON, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.COOKED_PORKCHOP, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.COOKED_RABBIT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.COOKED_SALMON, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.COOKIE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.RAW_COPPER, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.COPPER_INGOT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.CREEPER_BANNER_PATTERN, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.CYAN_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.DARK_OAK_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.DARK_OAK_CHEST_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.DIAMOND, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.DIAMOND_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.DIAMOND_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.DIAMOND_HORSE_ARMOR, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.DIAMOND_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.DIAMOND_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.DIAMOND_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.DRAGON_BREATH, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.DRIED_KELP, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.EGG, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.EMERALD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.ENCHANTED_BOOK, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.ENDER_EYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.ENDER_PEARL, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.END_CRYSTAL, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.EXPERIENCE_BOTTLE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.FERMENTED_SPIDER_EYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.FIREWORK_ROCKET, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.FIRE_CHARGE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.FLINT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.FLINT_AND_STEEL, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.FLOW_BANNER_PATTERN, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.FLOWER_BANNER_PATTERN, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.FURNACE_MINECART, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GHAST_TEAR, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GLASS_BOTTLE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GLISTERING_MELON_SLICE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GLOBE_BANNER_PATTERN, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GLOW_BERRIES, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GLOWSTONE_DUST, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GLOW_INK_SAC, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GLOW_ITEM_FRAME, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.RAW_GOLD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GOLDEN_APPLE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GOLDEN_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.GOLDEN_CARROT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GOLDEN_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.GOLDEN_HORSE_ARMOR, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GOLDEN_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.GOLDEN_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.GOLDEN_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.GOLD_INGOT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GOLD_NUGGET, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GRAY_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GREEN_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GUNPOWDER, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GUSTER_BANNER_PATTERN, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.HEART_OF_THE_SEA, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.HONEYCOMB, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.HONEY_BOTTLE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.HOPPER_MINECART, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.INK_SAC, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.RAW_IRON, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.IRON_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.IRON_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.IRON_HORSE_ARMOR, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.IRON_INGOT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.IRON_NUGGET, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.IRON_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.IRON_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.IRON_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.ITEM_FRAME, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.JUNGLE_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.JUNGLE_CHEST_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.KNOWLEDGE_BOOK, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.LAPIS_LAZULI, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.LAVA_BUCKET, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.LEATHER, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.LEATHER_HORSE_ARMOR, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.LIGHT_BLUE_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.LIGHT_GRAY_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.LIME_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.MAGENTA_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.MAGMA_CREAM, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.MANGROVE_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.MANGROVE_CHEST_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BAMBOO_RAFT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BAMBOO_CHEST_RAFT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.MAP, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.MELON_SLICE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.MILK_BUCKET, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.MINECART, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.MOJANG_BANNER_PATTERN, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.MUSHROOM_STEW, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.DISC_FRAGMENT_5, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.MUSIC_DISC_11, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_13, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_BLOCKS, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_CAT, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_CHIRP, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_CREATOR, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_CREATOR_MUSIC_BOX, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_FAR, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_MALL, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_MELLOHI, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_PIGSTEP, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_PRECIPICE, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_STAL, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_STRAD, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_WAIT, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_WARD, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_OTHERSIDE, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_RELIC, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUSIC_DISC_5, ModelTemplates.MUSIC_DISC);
        this.generateFlatItem(Items.MUTTON, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.NAME_TAG, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.NAUTILUS_SHELL, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.NETHERITE_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.NETHERITE_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.NETHERITE_INGOT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.NETHERITE_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.NETHERITE_SCRAP, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.NETHERITE_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.NETHERITE_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.NETHER_BRICK, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.NETHER_STAR, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.OAK_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.OAK_CHEST_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.ORANGE_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.PAINTING, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.PAPER, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.PHANTOM_MEMBRANE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.PIGLIN_BANNER_PATTERN, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.PINK_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.POISONOUS_POTATO, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.POPPED_CHORUS_FRUIT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.PORKCHOP, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.POWDER_SNOW_BUCKET, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.PRISMARINE_CRYSTALS, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.PRISMARINE_SHARD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.PUFFERFISH, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.PUFFERFISH_BUCKET, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.PUMPKIN_PIE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.PURPLE_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.QUARTZ, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.RABBIT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.RABBIT_FOOT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.RABBIT_HIDE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.RABBIT_STEW, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.RED_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.ROTTEN_FLESH, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SADDLE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SALMON, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SALMON_BUCKET, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.TURTLE_SCUTE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SHEARS, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SHULKER_SHELL, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SKULL_BANNER_PATTERN, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SLIME_BALL, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SNOWBALL, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.ECHO_SHARD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SPECTRAL_ARROW, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SPIDER_EYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SPRUCE_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SPRUCE_CHEST_BOAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SPYGLASS, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.STICK, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.STONE_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.STONE_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.STONE_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.STONE_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.STONE_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.SUGAR, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SUSPICIOUS_STEW, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.TNT_MINECART, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.TOTEM_OF_UNDYING, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.TRIDENT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.TROPICAL_FISH, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.TROPICAL_FISH_BUCKET, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.AXOLOTL_BUCKET, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.TADPOLE_BUCKET, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.WATER_BUCKET, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.WHEAT, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.WHITE_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.WIND_CHARGE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.MACE, ModelTemplates.FLAT_HANDHELD_MACE_ITEM);
        this.generateItemWithOverlay(Items.WOLF_ARMOR);
        this.generateFlatItem(Items.WOODEN_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.WOODEN_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.WOODEN_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.WOODEN_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.WOODEN_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.WRITABLE_BOOK, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.WRITTEN_BOOK, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.YELLOW_DYE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.DEBUG_STICK, Items.STICK, ModelTemplates.FLAT_HANDHELD_ITEM);
        this.generateFlatItem(Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE, ModelTemplates.FLAT_ITEM);

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ArmorItem armoritem) {
                this.generateArmorTrims(armoritem);
            }
        }

        this.generateFlatItem(Items.ANGLER_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.ARCHER_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.ARMS_UP_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BLADE_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BREWER_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.BURN_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.DANGER_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.EXPLORER_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.FLOW_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.FRIEND_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.GUSTER_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.HEART_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.HEARTBREAK_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.HOWL_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.MINER_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.MOURNER_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.PLENTY_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.PRIZE_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SCRAPE_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SHEAF_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SHELTER_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SKULL_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.SNORT_POTTERY_SHERD, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.TRIAL_KEY, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.OMINOUS_TRIAL_KEY, ModelTemplates.FLAT_ITEM);
        this.generateFlatItem(Items.OMINOUS_BOTTLE, ModelTemplates.FLAT_ITEM);
    }

    public static record TrimModelData(String name, float itemModelIndex, Map<Holder<ArmorMaterial>, String> overrideArmorMaterials) {
        public String name(Holder<ArmorMaterial> armorMaterial) {
            return this.overrideArmorMaterials.getOrDefault(armorMaterial, this.name);
        }
    }
}
