package net.minecraft.world.item;

import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.material.Fluids;

public class Items {
    public static final Item AIR = registerBlock(Blocks.AIR, new AirItem(Blocks.AIR, new Item.Properties()));
    public static final Item STONE = registerBlock(Blocks.STONE);
    public static final Item GRANITE = registerBlock(Blocks.GRANITE);
    public static final Item POLISHED_GRANITE = registerBlock(Blocks.POLISHED_GRANITE);
    public static final Item DIORITE = registerBlock(Blocks.DIORITE);
    public static final Item POLISHED_DIORITE = registerBlock(Blocks.POLISHED_DIORITE);
    public static final Item ANDESITE = registerBlock(Blocks.ANDESITE);
    public static final Item POLISHED_ANDESITE = registerBlock(Blocks.POLISHED_ANDESITE);
    public static final Item DEEPSLATE = registerBlock(Blocks.DEEPSLATE);
    public static final Item COBBLED_DEEPSLATE = registerBlock(Blocks.COBBLED_DEEPSLATE);
    public static final Item POLISHED_DEEPSLATE = registerBlock(Blocks.POLISHED_DEEPSLATE);
    public static final Item CALCITE = registerBlock(Blocks.CALCITE);
    public static final Item TUFF = registerBlock(Blocks.TUFF);
    public static final Item TUFF_SLAB = registerBlock(Blocks.TUFF_SLAB);
    public static final Item TUFF_STAIRS = registerBlock(Blocks.TUFF_STAIRS);
    public static final Item TUFF_WALL = registerBlock(Blocks.TUFF_WALL);
    public static final Item CHISELED_TUFF = registerBlock(Blocks.CHISELED_TUFF);
    public static final Item POLISHED_TUFF = registerBlock(Blocks.POLISHED_TUFF);
    public static final Item POLISHED_TUFF_SLAB = registerBlock(Blocks.POLISHED_TUFF_SLAB);
    public static final Item POLISHED_TUFF_STAIRS = registerBlock(Blocks.POLISHED_TUFF_STAIRS);
    public static final Item POLISHED_TUFF_WALL = registerBlock(Blocks.POLISHED_TUFF_WALL);
    public static final Item TUFF_BRICKS = registerBlock(Blocks.TUFF_BRICKS);
    public static final Item TUFF_BRICK_SLAB = registerBlock(Blocks.TUFF_BRICK_SLAB);
    public static final Item TUFF_BRICK_STAIRS = registerBlock(Blocks.TUFF_BRICK_STAIRS);
    public static final Item TUFF_BRICK_WALL = registerBlock(Blocks.TUFF_BRICK_WALL);
    public static final Item CHISELED_TUFF_BRICKS = registerBlock(Blocks.CHISELED_TUFF_BRICKS);
    public static final Item DRIPSTONE_BLOCK = registerBlock(Blocks.DRIPSTONE_BLOCK);
    public static final Item GRASS_BLOCK = registerBlock(Blocks.GRASS_BLOCK);
    public static final Item DIRT = registerBlock(Blocks.DIRT);
    public static final Item COARSE_DIRT = registerBlock(Blocks.COARSE_DIRT);
    public static final Item PODZOL = registerBlock(Blocks.PODZOL);
    public static final Item ROOTED_DIRT = registerBlock(Blocks.ROOTED_DIRT);
    public static final Item MUD = registerBlock(Blocks.MUD);
    public static final Item CRIMSON_NYLIUM = registerBlock(Blocks.CRIMSON_NYLIUM);
    public static final Item WARPED_NYLIUM = registerBlock(Blocks.WARPED_NYLIUM);
    public static final Item COBBLESTONE = registerBlock(Blocks.COBBLESTONE);
    public static final Item OAK_PLANKS = registerBlock(Blocks.OAK_PLANKS);
    public static final Item SPRUCE_PLANKS = registerBlock(Blocks.SPRUCE_PLANKS);
    public static final Item BIRCH_PLANKS = registerBlock(Blocks.BIRCH_PLANKS);
    public static final Item JUNGLE_PLANKS = registerBlock(Blocks.JUNGLE_PLANKS);
    public static final Item ACACIA_PLANKS = registerBlock(Blocks.ACACIA_PLANKS);
    public static final Item CHERRY_PLANKS = registerBlock(Blocks.CHERRY_PLANKS);
    public static final Item DARK_OAK_PLANKS = registerBlock(Blocks.DARK_OAK_PLANKS);
    public static final Item MANGROVE_PLANKS = registerBlock(Blocks.MANGROVE_PLANKS);
    public static final Item BAMBOO_PLANKS = registerBlock(Blocks.BAMBOO_PLANKS);
    public static final Item CRIMSON_PLANKS = registerBlock(Blocks.CRIMSON_PLANKS);
    public static final Item WARPED_PLANKS = registerBlock(Blocks.WARPED_PLANKS);
    public static final Item BAMBOO_MOSAIC = registerBlock(Blocks.BAMBOO_MOSAIC);
    public static final Item OAK_SAPLING = registerBlock(Blocks.OAK_SAPLING);
    public static final Item SPRUCE_SAPLING = registerBlock(Blocks.SPRUCE_SAPLING);
    public static final Item BIRCH_SAPLING = registerBlock(Blocks.BIRCH_SAPLING);
    public static final Item JUNGLE_SAPLING = registerBlock(Blocks.JUNGLE_SAPLING);
    public static final Item ACACIA_SAPLING = registerBlock(Blocks.ACACIA_SAPLING);
    public static final Item CHERRY_SAPLING = registerBlock(Blocks.CHERRY_SAPLING);
    public static final Item DARK_OAK_SAPLING = registerBlock(Blocks.DARK_OAK_SAPLING);
    public static final Item MANGROVE_PROPAGULE = registerBlock(Blocks.MANGROVE_PROPAGULE);
    public static final Item BEDROCK = registerBlock(Blocks.BEDROCK);
    public static final Item SAND = registerBlock(Blocks.SAND);
    public static final Item SUSPICIOUS_SAND = registerBlock(new BlockItem(Blocks.SUSPICIOUS_SAND, new Item.Properties()));
    public static final Item SUSPICIOUS_GRAVEL = registerBlock(new BlockItem(Blocks.SUSPICIOUS_GRAVEL, new Item.Properties()));
    public static final Item RED_SAND = registerBlock(Blocks.RED_SAND);
    public static final Item GRAVEL = registerBlock(Blocks.GRAVEL);
    public static final Item COAL_ORE = registerBlock(Blocks.COAL_ORE);
    public static final Item DEEPSLATE_COAL_ORE = registerBlock(Blocks.DEEPSLATE_COAL_ORE);
    public static final Item IRON_ORE = registerBlock(Blocks.IRON_ORE);
    public static final Item DEEPSLATE_IRON_ORE = registerBlock(Blocks.DEEPSLATE_IRON_ORE);
    public static final Item COPPER_ORE = registerBlock(Blocks.COPPER_ORE);
    public static final Item DEEPSLATE_COPPER_ORE = registerBlock(Blocks.DEEPSLATE_COPPER_ORE);
    public static final Item GOLD_ORE = registerBlock(Blocks.GOLD_ORE);
    public static final Item DEEPSLATE_GOLD_ORE = registerBlock(Blocks.DEEPSLATE_GOLD_ORE);
    public static final Item REDSTONE_ORE = registerBlock(Blocks.REDSTONE_ORE);
    public static final Item DEEPSLATE_REDSTONE_ORE = registerBlock(Blocks.DEEPSLATE_REDSTONE_ORE);
    public static final Item EMERALD_ORE = registerBlock(Blocks.EMERALD_ORE);
    public static final Item DEEPSLATE_EMERALD_ORE = registerBlock(Blocks.DEEPSLATE_EMERALD_ORE);
    public static final Item LAPIS_ORE = registerBlock(Blocks.LAPIS_ORE);
    public static final Item DEEPSLATE_LAPIS_ORE = registerBlock(Blocks.DEEPSLATE_LAPIS_ORE);
    public static final Item DIAMOND_ORE = registerBlock(Blocks.DIAMOND_ORE);
    public static final Item DEEPSLATE_DIAMOND_ORE = registerBlock(Blocks.DEEPSLATE_DIAMOND_ORE);
    public static final Item NETHER_GOLD_ORE = registerBlock(Blocks.NETHER_GOLD_ORE);
    public static final Item NETHER_QUARTZ_ORE = registerBlock(Blocks.NETHER_QUARTZ_ORE);
    public static final Item ANCIENT_DEBRIS = registerBlock(new BlockItem(Blocks.ANCIENT_DEBRIS, new Item.Properties().fireResistant()));
    public static final Item COAL_BLOCK = registerBlock(Blocks.COAL_BLOCK);
    public static final Item RAW_IRON_BLOCK = registerBlock(Blocks.RAW_IRON_BLOCK);
    public static final Item RAW_COPPER_BLOCK = registerBlock(Blocks.RAW_COPPER_BLOCK);
    public static final Item RAW_GOLD_BLOCK = registerBlock(Blocks.RAW_GOLD_BLOCK);
    public static final Item HEAVY_CORE = registerBlock(new BlockItem(Blocks.HEAVY_CORE, new Item.Properties().rarity(Rarity.EPIC)));
    public static final Item AMETHYST_BLOCK = registerBlock(Blocks.AMETHYST_BLOCK);
    public static final Item BUDDING_AMETHYST = registerBlock(Blocks.BUDDING_AMETHYST);
    public static final Item IRON_BLOCK = registerBlock(Blocks.IRON_BLOCK);
    public static final Item COPPER_BLOCK = registerBlock(Blocks.COPPER_BLOCK);
    public static final Item GOLD_BLOCK = registerBlock(Blocks.GOLD_BLOCK);
    public static final Item DIAMOND_BLOCK = registerBlock(Blocks.DIAMOND_BLOCK);
    public static final Item NETHERITE_BLOCK = registerBlock(new BlockItem(Blocks.NETHERITE_BLOCK, new Item.Properties().fireResistant()));
    public static final Item EXPOSED_COPPER = registerBlock(Blocks.EXPOSED_COPPER);
    public static final Item WEATHERED_COPPER = registerBlock(Blocks.WEATHERED_COPPER);
    public static final Item OXIDIZED_COPPER = registerBlock(Blocks.OXIDIZED_COPPER);
    public static final Item CHISELED_COPPER = registerBlock(Blocks.CHISELED_COPPER);
    public static final Item EXPOSED_CHISELED_COPPER = registerBlock(Blocks.EXPOSED_CHISELED_COPPER);
    public static final Item WEATHERED_CHISELED_COPPER = registerBlock(Blocks.WEATHERED_CHISELED_COPPER);
    public static final Item OXIDIZED_CHISELED_COPPER = registerBlock(Blocks.OXIDIZED_CHISELED_COPPER);
    public static final Item CUT_COPPER = registerBlock(Blocks.CUT_COPPER);
    public static final Item EXPOSED_CUT_COPPER = registerBlock(Blocks.EXPOSED_CUT_COPPER);
    public static final Item WEATHERED_CUT_COPPER = registerBlock(Blocks.WEATHERED_CUT_COPPER);
    public static final Item OXIDIZED_CUT_COPPER = registerBlock(Blocks.OXIDIZED_CUT_COPPER);
    public static final Item CUT_COPPER_STAIRS = registerBlock(Blocks.CUT_COPPER_STAIRS);
    public static final Item EXPOSED_CUT_COPPER_STAIRS = registerBlock(Blocks.EXPOSED_CUT_COPPER_STAIRS);
    public static final Item WEATHERED_CUT_COPPER_STAIRS = registerBlock(Blocks.WEATHERED_CUT_COPPER_STAIRS);
    public static final Item OXIDIZED_CUT_COPPER_STAIRS = registerBlock(Blocks.OXIDIZED_CUT_COPPER_STAIRS);
    public static final Item CUT_COPPER_SLAB = registerBlock(Blocks.CUT_COPPER_SLAB);
    public static final Item EXPOSED_CUT_COPPER_SLAB = registerBlock(Blocks.EXPOSED_CUT_COPPER_SLAB);
    public static final Item WEATHERED_CUT_COPPER_SLAB = registerBlock(Blocks.WEATHERED_CUT_COPPER_SLAB);
    public static final Item OXIDIZED_CUT_COPPER_SLAB = registerBlock(Blocks.OXIDIZED_CUT_COPPER_SLAB);
    public static final Item WAXED_COPPER_BLOCK = registerBlock(Blocks.WAXED_COPPER_BLOCK);
    public static final Item WAXED_EXPOSED_COPPER = registerBlock(Blocks.WAXED_EXPOSED_COPPER);
    public static final Item WAXED_WEATHERED_COPPER = registerBlock(Blocks.WAXED_WEATHERED_COPPER);
    public static final Item WAXED_OXIDIZED_COPPER = registerBlock(Blocks.WAXED_OXIDIZED_COPPER);
    public static final Item WAXED_CHISELED_COPPER = registerBlock(Blocks.WAXED_CHISELED_COPPER);
    public static final Item WAXED_EXPOSED_CHISELED_COPPER = registerBlock(Blocks.WAXED_EXPOSED_CHISELED_COPPER);
    public static final Item WAXED_WEATHERED_CHISELED_COPPER = registerBlock(Blocks.WAXED_WEATHERED_CHISELED_COPPER);
    public static final Item WAXED_OXIDIZED_CHISELED_COPPER = registerBlock(Blocks.WAXED_OXIDIZED_CHISELED_COPPER);
    public static final Item WAXED_CUT_COPPER = registerBlock(Blocks.WAXED_CUT_COPPER);
    public static final Item WAXED_EXPOSED_CUT_COPPER = registerBlock(Blocks.WAXED_EXPOSED_CUT_COPPER);
    public static final Item WAXED_WEATHERED_CUT_COPPER = registerBlock(Blocks.WAXED_WEATHERED_CUT_COPPER);
    public static final Item WAXED_OXIDIZED_CUT_COPPER = registerBlock(Blocks.WAXED_OXIDIZED_CUT_COPPER);
    public static final Item WAXED_CUT_COPPER_STAIRS = registerBlock(Blocks.WAXED_CUT_COPPER_STAIRS);
    public static final Item WAXED_EXPOSED_CUT_COPPER_STAIRS = registerBlock(Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS);
    public static final Item WAXED_WEATHERED_CUT_COPPER_STAIRS = registerBlock(Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS);
    public static final Item WAXED_OXIDIZED_CUT_COPPER_STAIRS = registerBlock(Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS);
    public static final Item WAXED_CUT_COPPER_SLAB = registerBlock(Blocks.WAXED_CUT_COPPER_SLAB);
    public static final Item WAXED_EXPOSED_CUT_COPPER_SLAB = registerBlock(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB);
    public static final Item WAXED_WEATHERED_CUT_COPPER_SLAB = registerBlock(Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB);
    public static final Item WAXED_OXIDIZED_CUT_COPPER_SLAB = registerBlock(Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB);
    public static final Item OAK_LOG = registerBlock(Blocks.OAK_LOG);
    public static final Item SPRUCE_LOG = registerBlock(Blocks.SPRUCE_LOG);
    public static final Item BIRCH_LOG = registerBlock(Blocks.BIRCH_LOG);
    public static final Item JUNGLE_LOG = registerBlock(Blocks.JUNGLE_LOG);
    public static final Item ACACIA_LOG = registerBlock(Blocks.ACACIA_LOG);
    public static final Item CHERRY_LOG = registerBlock(Blocks.CHERRY_LOG);
    public static final Item DARK_OAK_LOG = registerBlock(Blocks.DARK_OAK_LOG);
    public static final Item MANGROVE_LOG = registerBlock(Blocks.MANGROVE_LOG);
    public static final Item MANGROVE_ROOTS = registerBlock(Blocks.MANGROVE_ROOTS);
    public static final Item MUDDY_MANGROVE_ROOTS = registerBlock(Blocks.MUDDY_MANGROVE_ROOTS);
    public static final Item CRIMSON_STEM = registerBlock(Blocks.CRIMSON_STEM);
    public static final Item WARPED_STEM = registerBlock(Blocks.WARPED_STEM);
    public static final Item BAMBOO_BLOCK = registerBlock(Blocks.BAMBOO_BLOCK);
    public static final Item STRIPPED_OAK_LOG = registerBlock(Blocks.STRIPPED_OAK_LOG);
    public static final Item STRIPPED_SPRUCE_LOG = registerBlock(Blocks.STRIPPED_SPRUCE_LOG);
    public static final Item STRIPPED_BIRCH_LOG = registerBlock(Blocks.STRIPPED_BIRCH_LOG);
    public static final Item STRIPPED_JUNGLE_LOG = registerBlock(Blocks.STRIPPED_JUNGLE_LOG);
    public static final Item STRIPPED_ACACIA_LOG = registerBlock(Blocks.STRIPPED_ACACIA_LOG);
    public static final Item STRIPPED_CHERRY_LOG = registerBlock(Blocks.STRIPPED_CHERRY_LOG);
    public static final Item STRIPPED_DARK_OAK_LOG = registerBlock(Blocks.STRIPPED_DARK_OAK_LOG);
    public static final Item STRIPPED_MANGROVE_LOG = registerBlock(Blocks.STRIPPED_MANGROVE_LOG);
    public static final Item STRIPPED_CRIMSON_STEM = registerBlock(Blocks.STRIPPED_CRIMSON_STEM);
    public static final Item STRIPPED_WARPED_STEM = registerBlock(Blocks.STRIPPED_WARPED_STEM);
    public static final Item STRIPPED_OAK_WOOD = registerBlock(Blocks.STRIPPED_OAK_WOOD);
    public static final Item STRIPPED_SPRUCE_WOOD = registerBlock(Blocks.STRIPPED_SPRUCE_WOOD);
    public static final Item STRIPPED_BIRCH_WOOD = registerBlock(Blocks.STRIPPED_BIRCH_WOOD);
    public static final Item STRIPPED_JUNGLE_WOOD = registerBlock(Blocks.STRIPPED_JUNGLE_WOOD);
    public static final Item STRIPPED_ACACIA_WOOD = registerBlock(Blocks.STRIPPED_ACACIA_WOOD);
    public static final Item STRIPPED_CHERRY_WOOD = registerBlock(Blocks.STRIPPED_CHERRY_WOOD);
    public static final Item STRIPPED_DARK_OAK_WOOD = registerBlock(Blocks.STRIPPED_DARK_OAK_WOOD);
    public static final Item STRIPPED_MANGROVE_WOOD = registerBlock(Blocks.STRIPPED_MANGROVE_WOOD);
    public static final Item STRIPPED_CRIMSON_HYPHAE = registerBlock(Blocks.STRIPPED_CRIMSON_HYPHAE);
    public static final Item STRIPPED_WARPED_HYPHAE = registerBlock(Blocks.STRIPPED_WARPED_HYPHAE);
    public static final Item STRIPPED_BAMBOO_BLOCK = registerBlock(Blocks.STRIPPED_BAMBOO_BLOCK);
    public static final Item OAK_WOOD = registerBlock(Blocks.OAK_WOOD);
    public static final Item SPRUCE_WOOD = registerBlock(Blocks.SPRUCE_WOOD);
    public static final Item BIRCH_WOOD = registerBlock(Blocks.BIRCH_WOOD);
    public static final Item JUNGLE_WOOD = registerBlock(Blocks.JUNGLE_WOOD);
    public static final Item ACACIA_WOOD = registerBlock(Blocks.ACACIA_WOOD);
    public static final Item CHERRY_WOOD = registerBlock(Blocks.CHERRY_WOOD);
    public static final Item DARK_OAK_WOOD = registerBlock(Blocks.DARK_OAK_WOOD);
    public static final Item MANGROVE_WOOD = registerBlock(Blocks.MANGROVE_WOOD);
    public static final Item CRIMSON_HYPHAE = registerBlock(Blocks.CRIMSON_HYPHAE);
    public static final Item WARPED_HYPHAE = registerBlock(Blocks.WARPED_HYPHAE);
    public static final Item OAK_LEAVES = registerBlock(Blocks.OAK_LEAVES);
    public static final Item SPRUCE_LEAVES = registerBlock(Blocks.SPRUCE_LEAVES);
    public static final Item BIRCH_LEAVES = registerBlock(Blocks.BIRCH_LEAVES);
    public static final Item JUNGLE_LEAVES = registerBlock(Blocks.JUNGLE_LEAVES);
    public static final Item ACACIA_LEAVES = registerBlock(Blocks.ACACIA_LEAVES);
    public static final Item CHERRY_LEAVES = registerBlock(Blocks.CHERRY_LEAVES);
    public static final Item DARK_OAK_LEAVES = registerBlock(Blocks.DARK_OAK_LEAVES);
    public static final Item MANGROVE_LEAVES = registerBlock(Blocks.MANGROVE_LEAVES);
    public static final Item AZALEA_LEAVES = registerBlock(Blocks.AZALEA_LEAVES);
    public static final Item FLOWERING_AZALEA_LEAVES = registerBlock(Blocks.FLOWERING_AZALEA_LEAVES);
    public static final Item SPONGE = registerBlock(Blocks.SPONGE);
    public static final Item WET_SPONGE = registerBlock(Blocks.WET_SPONGE);
    public static final Item GLASS = registerBlock(Blocks.GLASS);
    public static final Item TINTED_GLASS = registerBlock(Blocks.TINTED_GLASS);
    public static final Item LAPIS_BLOCK = registerBlock(Blocks.LAPIS_BLOCK);
    public static final Item SANDSTONE = registerBlock(Blocks.SANDSTONE);
    public static final Item CHISELED_SANDSTONE = registerBlock(Blocks.CHISELED_SANDSTONE);
    public static final Item CUT_SANDSTONE = registerBlock(Blocks.CUT_SANDSTONE);
    public static final Item COBWEB = registerBlock(Blocks.COBWEB);
    public static final Item SHORT_GRASS = registerBlock(Blocks.SHORT_GRASS);
    public static final Item FERN = registerBlock(Blocks.FERN);
    public static final Item AZALEA = registerBlock(Blocks.AZALEA);
    public static final Item FLOWERING_AZALEA = registerBlock(Blocks.FLOWERING_AZALEA);
    public static final Item DEAD_BUSH = registerBlock(Blocks.DEAD_BUSH);
    public static final Item SEAGRASS = registerBlock(Blocks.SEAGRASS);
    public static final Item SEA_PICKLE = registerBlock(Blocks.SEA_PICKLE);
    public static final Item WHITE_WOOL = registerBlock(Blocks.WHITE_WOOL);
    public static final Item ORANGE_WOOL = registerBlock(Blocks.ORANGE_WOOL);
    public static final Item MAGENTA_WOOL = registerBlock(Blocks.MAGENTA_WOOL);
    public static final Item LIGHT_BLUE_WOOL = registerBlock(Blocks.LIGHT_BLUE_WOOL);
    public static final Item YELLOW_WOOL = registerBlock(Blocks.YELLOW_WOOL);
    public static final Item LIME_WOOL = registerBlock(Blocks.LIME_WOOL);
    public static final Item PINK_WOOL = registerBlock(Blocks.PINK_WOOL);
    public static final Item GRAY_WOOL = registerBlock(Blocks.GRAY_WOOL);
    public static final Item LIGHT_GRAY_WOOL = registerBlock(Blocks.LIGHT_GRAY_WOOL);
    public static final Item CYAN_WOOL = registerBlock(Blocks.CYAN_WOOL);
    public static final Item PURPLE_WOOL = registerBlock(Blocks.PURPLE_WOOL);
    public static final Item BLUE_WOOL = registerBlock(Blocks.BLUE_WOOL);
    public static final Item BROWN_WOOL = registerBlock(Blocks.BROWN_WOOL);
    public static final Item GREEN_WOOL = registerBlock(Blocks.GREEN_WOOL);
    public static final Item RED_WOOL = registerBlock(Blocks.RED_WOOL);
    public static final Item BLACK_WOOL = registerBlock(Blocks.BLACK_WOOL);
    public static final Item DANDELION = registerBlock(Blocks.DANDELION);
    public static final Item POPPY = registerBlock(Blocks.POPPY);
    public static final Item BLUE_ORCHID = registerBlock(Blocks.BLUE_ORCHID);
    public static final Item ALLIUM = registerBlock(Blocks.ALLIUM);
    public static final Item AZURE_BLUET = registerBlock(Blocks.AZURE_BLUET);
    public static final Item RED_TULIP = registerBlock(Blocks.RED_TULIP);
    public static final Item ORANGE_TULIP = registerBlock(Blocks.ORANGE_TULIP);
    public static final Item WHITE_TULIP = registerBlock(Blocks.WHITE_TULIP);
    public static final Item PINK_TULIP = registerBlock(Blocks.PINK_TULIP);
    public static final Item OXEYE_DAISY = registerBlock(Blocks.OXEYE_DAISY);
    public static final Item CORNFLOWER = registerBlock(Blocks.CORNFLOWER);
    public static final Item LILY_OF_THE_VALLEY = registerBlock(Blocks.LILY_OF_THE_VALLEY);
    public static final Item WITHER_ROSE = registerBlock(Blocks.WITHER_ROSE);
    public static final Item TORCHFLOWER = registerBlock(Blocks.TORCHFLOWER);
    public static final Item PITCHER_PLANT = registerBlock(Blocks.PITCHER_PLANT);
    public static final Item SPORE_BLOSSOM = registerBlock(Blocks.SPORE_BLOSSOM);
    public static final Item BROWN_MUSHROOM = registerBlock(Blocks.BROWN_MUSHROOM);
    public static final Item RED_MUSHROOM = registerBlock(Blocks.RED_MUSHROOM);
    public static final Item CRIMSON_FUNGUS = registerBlock(Blocks.CRIMSON_FUNGUS);
    public static final Item WARPED_FUNGUS = registerBlock(Blocks.WARPED_FUNGUS);
    public static final Item CRIMSON_ROOTS = registerBlock(Blocks.CRIMSON_ROOTS);
    public static final Item WARPED_ROOTS = registerBlock(Blocks.WARPED_ROOTS);
    public static final Item NETHER_SPROUTS = registerBlock(Blocks.NETHER_SPROUTS);
    public static final Item WEEPING_VINES = registerBlock(Blocks.WEEPING_VINES);
    public static final Item TWISTING_VINES = registerBlock(Blocks.TWISTING_VINES);
    public static final Item SUGAR_CANE = registerBlock(Blocks.SUGAR_CANE);
    public static final Item KELP = registerBlock(Blocks.KELP);
    public static final Item MOSS_CARPET = registerBlock(Blocks.MOSS_CARPET);
    public static final Item PINK_PETALS = registerBlock(Blocks.PINK_PETALS);
    public static final Item MOSS_BLOCK = registerBlock(Blocks.MOSS_BLOCK);
    public static final Item HANGING_ROOTS = registerBlock(Blocks.HANGING_ROOTS);
    public static final Item BIG_DRIPLEAF = registerBlock(Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM);
    public static final Item SMALL_DRIPLEAF = registerBlock(new DoubleHighBlockItem(Blocks.SMALL_DRIPLEAF, new Item.Properties()));
    public static final Item BAMBOO = registerBlock(Blocks.BAMBOO);
    public static final Item OAK_SLAB = registerBlock(Blocks.OAK_SLAB);
    public static final Item SPRUCE_SLAB = registerBlock(Blocks.SPRUCE_SLAB);
    public static final Item BIRCH_SLAB = registerBlock(Blocks.BIRCH_SLAB);
    public static final Item JUNGLE_SLAB = registerBlock(Blocks.JUNGLE_SLAB);
    public static final Item ACACIA_SLAB = registerBlock(Blocks.ACACIA_SLAB);
    public static final Item CHERRY_SLAB = registerBlock(Blocks.CHERRY_SLAB);
    public static final Item DARK_OAK_SLAB = registerBlock(Blocks.DARK_OAK_SLAB);
    public static final Item MANGROVE_SLAB = registerBlock(Blocks.MANGROVE_SLAB);
    public static final Item BAMBOO_SLAB = registerBlock(Blocks.BAMBOO_SLAB);
    public static final Item BAMBOO_MOSAIC_SLAB = registerBlock(Blocks.BAMBOO_MOSAIC_SLAB);
    public static final Item CRIMSON_SLAB = registerBlock(Blocks.CRIMSON_SLAB);
    public static final Item WARPED_SLAB = registerBlock(Blocks.WARPED_SLAB);
    public static final Item STONE_SLAB = registerBlock(Blocks.STONE_SLAB);
    public static final Item SMOOTH_STONE_SLAB = registerBlock(Blocks.SMOOTH_STONE_SLAB);
    public static final Item SANDSTONE_SLAB = registerBlock(Blocks.SANDSTONE_SLAB);
    public static final Item CUT_STANDSTONE_SLAB = registerBlock(Blocks.CUT_SANDSTONE_SLAB);
    public static final Item PETRIFIED_OAK_SLAB = registerBlock(Blocks.PETRIFIED_OAK_SLAB);
    public static final Item COBBLESTONE_SLAB = registerBlock(Blocks.COBBLESTONE_SLAB);
    public static final Item BRICK_SLAB = registerBlock(Blocks.BRICK_SLAB);
    public static final Item STONE_BRICK_SLAB = registerBlock(Blocks.STONE_BRICK_SLAB);
    public static final Item MUD_BRICK_SLAB = registerBlock(Blocks.MUD_BRICK_SLAB);
    public static final Item NETHER_BRICK_SLAB = registerBlock(Blocks.NETHER_BRICK_SLAB);
    public static final Item QUARTZ_SLAB = registerBlock(Blocks.QUARTZ_SLAB);
    public static final Item RED_SANDSTONE_SLAB = registerBlock(Blocks.RED_SANDSTONE_SLAB);
    public static final Item CUT_RED_SANDSTONE_SLAB = registerBlock(Blocks.CUT_RED_SANDSTONE_SLAB);
    public static final Item PURPUR_SLAB = registerBlock(Blocks.PURPUR_SLAB);
    public static final Item PRISMARINE_SLAB = registerBlock(Blocks.PRISMARINE_SLAB);
    public static final Item PRISMARINE_BRICK_SLAB = registerBlock(Blocks.PRISMARINE_BRICK_SLAB);
    public static final Item DARK_PRISMARINE_SLAB = registerBlock(Blocks.DARK_PRISMARINE_SLAB);
    public static final Item SMOOTH_QUARTZ = registerBlock(Blocks.SMOOTH_QUARTZ);
    public static final Item SMOOTH_RED_SANDSTONE = registerBlock(Blocks.SMOOTH_RED_SANDSTONE);
    public static final Item SMOOTH_SANDSTONE = registerBlock(Blocks.SMOOTH_SANDSTONE);
    public static final Item SMOOTH_STONE = registerBlock(Blocks.SMOOTH_STONE);
    public static final Item BRICKS = registerBlock(Blocks.BRICKS);
    public static final Item BOOKSHELF = registerBlock(Blocks.BOOKSHELF);
    public static final Item CHISELED_BOOKSHELF = registerBlock(
        Blocks.CHISELED_BOOKSHELF, p_332742_ -> p_332742_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    );
    public static final Item DECORATED_POT = registerBlock(
        new BlockItem(Blocks.DECORATED_POT, new Item.Properties().component(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY))
    );
    public static final Item MOSSY_COBBLESTONE = registerBlock(Blocks.MOSSY_COBBLESTONE);
    public static final Item OBSIDIAN = registerBlock(Blocks.OBSIDIAN);
    public static final Item TORCH = registerBlock(new StandingAndWallBlockItem(Blocks.TORCH, Blocks.WALL_TORCH, new Item.Properties(), Direction.DOWN));
    public static final Item END_ROD = registerBlock(Blocks.END_ROD);
    public static final Item CHORUS_PLANT = registerBlock(Blocks.CHORUS_PLANT);
    public static final Item CHORUS_FLOWER = registerBlock(Blocks.CHORUS_FLOWER);
    public static final Item PURPUR_BLOCK = registerBlock(Blocks.PURPUR_BLOCK);
    public static final Item PURPUR_PILLAR = registerBlock(Blocks.PURPUR_PILLAR);
    public static final Item PURPUR_STAIRS = registerBlock(Blocks.PURPUR_STAIRS);
    public static final Item SPAWNER = registerBlock(Blocks.SPAWNER);
    public static final Item CHEST = registerBlock(Blocks.CHEST, p_332827_ -> p_332827_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
    public static final Item CRAFTING_TABLE = registerBlock(Blocks.CRAFTING_TABLE);
    public static final Item FARMLAND = registerBlock(Blocks.FARMLAND);
    public static final Item FURNACE = registerBlock(Blocks.FURNACE, p_332745_ -> p_332745_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
    public static final Item LADDER = registerBlock(Blocks.LADDER);
    public static final Item COBBLESTONE_STAIRS = registerBlock(Blocks.COBBLESTONE_STAIRS);
    public static final Item SNOW = registerBlock(Blocks.SNOW);
    public static final Item ICE = registerBlock(Blocks.ICE);
    public static final Item SNOW_BLOCK = registerBlock(Blocks.SNOW_BLOCK);
    public static final Item CACTUS = registerBlock(Blocks.CACTUS);
    public static final Item CLAY = registerBlock(Blocks.CLAY);
    public static final Item JUKEBOX = registerBlock(Blocks.JUKEBOX);
    public static final Item OAK_FENCE = registerBlock(Blocks.OAK_FENCE);
    public static final Item SPRUCE_FENCE = registerBlock(Blocks.SPRUCE_FENCE);
    public static final Item BIRCH_FENCE = registerBlock(Blocks.BIRCH_FENCE);
    public static final Item JUNGLE_FENCE = registerBlock(Blocks.JUNGLE_FENCE);
    public static final Item ACACIA_FENCE = registerBlock(Blocks.ACACIA_FENCE);
    public static final Item CHERRY_FENCE = registerBlock(Blocks.CHERRY_FENCE);
    public static final Item DARK_OAK_FENCE = registerBlock(Blocks.DARK_OAK_FENCE);
    public static final Item MANGROVE_FENCE = registerBlock(Blocks.MANGROVE_FENCE);
    public static final Item BAMBOO_FENCE = registerBlock(Blocks.BAMBOO_FENCE);
    public static final Item CRIMSON_FENCE = registerBlock(Blocks.CRIMSON_FENCE);
    public static final Item WARPED_FENCE = registerBlock(Blocks.WARPED_FENCE);
    public static final Item PUMPKIN = registerBlock(Blocks.PUMPKIN);
    public static final Item CARVED_PUMPKIN = registerBlock(Blocks.CARVED_PUMPKIN);
    public static final Item JACK_O_LANTERN = registerBlock(Blocks.JACK_O_LANTERN);
    public static final Item NETHERRACK = registerBlock(Blocks.NETHERRACK);
    public static final Item SOUL_SAND = registerBlock(Blocks.SOUL_SAND);
    public static final Item SOUL_SOIL = registerBlock(Blocks.SOUL_SOIL);
    public static final Item BASALT = registerBlock(Blocks.BASALT);
    public static final Item POLISHED_BASALT = registerBlock(Blocks.POLISHED_BASALT);
    public static final Item SMOOTH_BASALT = registerBlock(Blocks.SMOOTH_BASALT);
    public static final Item SOUL_TORCH = registerBlock(
        new StandingAndWallBlockItem(Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH, new Item.Properties(), Direction.DOWN)
    );
    public static final Item GLOWSTONE = registerBlock(Blocks.GLOWSTONE);
    public static final Item INFESTED_STONE = registerBlock(Blocks.INFESTED_STONE);
    public static final Item INFESTED_COBBLESTONE = registerBlock(Blocks.INFESTED_COBBLESTONE);
    public static final Item INFESTED_STONE_BRICKS = registerBlock(Blocks.INFESTED_STONE_BRICKS);
    public static final Item INFESTED_MOSSY_STONE_BRICKS = registerBlock(Blocks.INFESTED_MOSSY_STONE_BRICKS);
    public static final Item INFESTED_CRACKED_STONE_BRICKS = registerBlock(Blocks.INFESTED_CRACKED_STONE_BRICKS);
    public static final Item INFESTED_CHISELED_STONE_BRICKS = registerBlock(Blocks.INFESTED_CHISELED_STONE_BRICKS);
    public static final Item INFESTED_DEEPSLATE = registerBlock(Blocks.INFESTED_DEEPSLATE);
    public static final Item STONE_BRICKS = registerBlock(Blocks.STONE_BRICKS);
    public static final Item MOSSY_STONE_BRICKS = registerBlock(Blocks.MOSSY_STONE_BRICKS);
    public static final Item CRACKED_STONE_BRICKS = registerBlock(Blocks.CRACKED_STONE_BRICKS);
    public static final Item CHISELED_STONE_BRICKS = registerBlock(Blocks.CHISELED_STONE_BRICKS);
    public static final Item PACKED_MUD = registerBlock(Blocks.PACKED_MUD);
    public static final Item MUD_BRICKS = registerBlock(Blocks.MUD_BRICKS);
    public static final Item DEEPSLATE_BRICKS = registerBlock(Blocks.DEEPSLATE_BRICKS);
    public static final Item CRACKED_DEEPSLATE_BRICKS = registerBlock(Blocks.CRACKED_DEEPSLATE_BRICKS);
    public static final Item DEEPSLATE_TILES = registerBlock(Blocks.DEEPSLATE_TILES);
    public static final Item CRACKED_DEEPSLATE_TILES = registerBlock(Blocks.CRACKED_DEEPSLATE_TILES);
    public static final Item CHISELED_DEEPSLATE = registerBlock(Blocks.CHISELED_DEEPSLATE);
    public static final Item REINFORCED_DEEPSLATE = registerBlock(Blocks.REINFORCED_DEEPSLATE);
    public static final Item BROWN_MUSHROOM_BLOCK = registerBlock(Blocks.BROWN_MUSHROOM_BLOCK);
    public static final Item RED_MUSHROOM_BLOCK = registerBlock(Blocks.RED_MUSHROOM_BLOCK);
    public static final Item MUSHROOM_STEM = registerBlock(Blocks.MUSHROOM_STEM);
    public static final Item IRON_BARS = registerBlock(Blocks.IRON_BARS);
    public static final Item CHAIN = registerBlock(Blocks.CHAIN);
    public static final Item GLASS_PANE = registerBlock(Blocks.GLASS_PANE);
    public static final Item MELON = registerBlock(Blocks.MELON);
    public static final Item VINE = registerBlock(Blocks.VINE);
    public static final Item GLOW_LICHEN = registerBlock(Blocks.GLOW_LICHEN);
    public static final Item BRICK_STAIRS = registerBlock(Blocks.BRICK_STAIRS);
    public static final Item STONE_BRICK_STAIRS = registerBlock(Blocks.STONE_BRICK_STAIRS);
    public static final Item MUD_BRICK_STAIRS = registerBlock(Blocks.MUD_BRICK_STAIRS);
    public static final Item MYCELIUM = registerBlock(Blocks.MYCELIUM);
    public static final Item LILY_PAD = registerBlock(new PlaceOnWaterBlockItem(Blocks.LILY_PAD, new Item.Properties()));
    public static final Item NETHER_BRICKS = registerBlock(Blocks.NETHER_BRICKS);
    public static final Item CRACKED_NETHER_BRICKS = registerBlock(Blocks.CRACKED_NETHER_BRICKS);
    public static final Item CHISELED_NETHER_BRICKS = registerBlock(Blocks.CHISELED_NETHER_BRICKS);
    public static final Item NETHER_BRICK_FENCE = registerBlock(Blocks.NETHER_BRICK_FENCE);
    public static final Item NETHER_BRICK_STAIRS = registerBlock(Blocks.NETHER_BRICK_STAIRS);
    public static final Item SCULK = registerBlock(Blocks.SCULK);
    public static final Item SCULK_VEIN = registerBlock(Blocks.SCULK_VEIN);
    public static final Item SCULK_CATALYST = registerBlock(Blocks.SCULK_CATALYST);
    public static final Item SCULK_SHRIEKER = registerBlock(Blocks.SCULK_SHRIEKER);
    public static final Item ENCHANTING_TABLE = registerBlock(Blocks.ENCHANTING_TABLE);
    public static final Item END_PORTAL_FRAME = registerBlock(Blocks.END_PORTAL_FRAME);
    public static final Item END_STONE = registerBlock(Blocks.END_STONE);
    public static final Item END_STONE_BRICKS = registerBlock(Blocks.END_STONE_BRICKS);
    public static final Item DRAGON_EGG = registerBlock(new BlockItem(Blocks.DRAGON_EGG, new Item.Properties().rarity(Rarity.EPIC)));
    public static final Item SANDSTONE_STAIRS = registerBlock(Blocks.SANDSTONE_STAIRS);
    public static final Item ENDER_CHEST = registerBlock(
        Blocks.ENDER_CHEST, p_332753_ -> p_332753_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    );
    public static final Item EMERALD_BLOCK = registerBlock(Blocks.EMERALD_BLOCK);
    public static final Item OAK_STAIRS = registerBlock(Blocks.OAK_STAIRS);
    public static final Item SPRUCE_STAIRS = registerBlock(Blocks.SPRUCE_STAIRS);
    public static final Item BIRCH_STAIRS = registerBlock(Blocks.BIRCH_STAIRS);
    public static final Item JUNGLE_STAIRS = registerBlock(Blocks.JUNGLE_STAIRS);
    public static final Item ACACIA_STAIRS = registerBlock(Blocks.ACACIA_STAIRS);
    public static final Item CHERRY_STAIRS = registerBlock(Blocks.CHERRY_STAIRS);
    public static final Item DARK_OAK_STAIRS = registerBlock(Blocks.DARK_OAK_STAIRS);
    public static final Item MANGROVE_STAIRS = registerBlock(Blocks.MANGROVE_STAIRS);
    public static final Item BAMBOO_STAIRS = registerBlock(Blocks.BAMBOO_STAIRS);
    public static final Item BAMBOO_MOSAIC_STAIRS = registerBlock(Blocks.BAMBOO_MOSAIC_STAIRS);
    public static final Item CRIMSON_STAIRS = registerBlock(Blocks.CRIMSON_STAIRS);
    public static final Item WARPED_STAIRS = registerBlock(Blocks.WARPED_STAIRS);
    public static final Item COMMAND_BLOCK = registerBlock(new GameMasterBlockItem(Blocks.COMMAND_BLOCK, new Item.Properties().rarity(Rarity.EPIC)));
    public static final Item BEACON = registerBlock(new BlockItem(Blocks.BEACON, new Item.Properties().rarity(Rarity.RARE)));
    public static final Item COBBLESTONE_WALL = registerBlock(Blocks.COBBLESTONE_WALL);
    public static final Item MOSSY_COBBLESTONE_WALL = registerBlock(Blocks.MOSSY_COBBLESTONE_WALL);
    public static final Item BRICK_WALL = registerBlock(Blocks.BRICK_WALL);
    public static final Item PRISMARINE_WALL = registerBlock(Blocks.PRISMARINE_WALL);
    public static final Item RED_SANDSTONE_WALL = registerBlock(Blocks.RED_SANDSTONE_WALL);
    public static final Item MOSSY_STONE_BRICK_WALL = registerBlock(Blocks.MOSSY_STONE_BRICK_WALL);
    public static final Item GRANITE_WALL = registerBlock(Blocks.GRANITE_WALL);
    public static final Item STONE_BRICK_WALL = registerBlock(Blocks.STONE_BRICK_WALL);
    public static final Item MUD_BRICK_WALL = registerBlock(Blocks.MUD_BRICK_WALL);
    public static final Item NETHER_BRICK_WALL = registerBlock(Blocks.NETHER_BRICK_WALL);
    public static final Item ANDESITE_WALL = registerBlock(Blocks.ANDESITE_WALL);
    public static final Item RED_NETHER_BRICK_WALL = registerBlock(Blocks.RED_NETHER_BRICK_WALL);
    public static final Item SANDSTONE_WALL = registerBlock(Blocks.SANDSTONE_WALL);
    public static final Item END_STONE_BRICK_WALL = registerBlock(Blocks.END_STONE_BRICK_WALL);
    public static final Item DIORITE_WALL = registerBlock(Blocks.DIORITE_WALL);
    public static final Item BLACKSTONE_WALL = registerBlock(Blocks.BLACKSTONE_WALL);
    public static final Item POLISHED_BLACKSTONE_WALL = registerBlock(Blocks.POLISHED_BLACKSTONE_WALL);
    public static final Item POLISHED_BLACKSTONE_BRICK_WALL = registerBlock(Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
    public static final Item COBBLED_DEEPSLATE_WALL = registerBlock(Blocks.COBBLED_DEEPSLATE_WALL);
    public static final Item POLISHED_DEEPSLATE_WALL = registerBlock(Blocks.POLISHED_DEEPSLATE_WALL);
    public static final Item DEEPSLATE_BRICK_WALL = registerBlock(Blocks.DEEPSLATE_BRICK_WALL);
    public static final Item DEEPSLATE_TILE_WALL = registerBlock(Blocks.DEEPSLATE_TILE_WALL);
    public static final Item ANVIL = registerBlock(Blocks.ANVIL);
    public static final Item CHIPPED_ANVIL = registerBlock(Blocks.CHIPPED_ANVIL);
    public static final Item DAMAGED_ANVIL = registerBlock(Blocks.DAMAGED_ANVIL);
    public static final Item CHISELED_QUARTZ_BLOCK = registerBlock(Blocks.CHISELED_QUARTZ_BLOCK);
    public static final Item QUARTZ_BLOCK = registerBlock(Blocks.QUARTZ_BLOCK);
    public static final Item QUARTZ_BRICKS = registerBlock(Blocks.QUARTZ_BRICKS);
    public static final Item QUARTZ_PILLAR = registerBlock(Blocks.QUARTZ_PILLAR);
    public static final Item QUARTZ_STAIRS = registerBlock(Blocks.QUARTZ_STAIRS);
    public static final Item WHITE_TERRACOTTA = registerBlock(Blocks.WHITE_TERRACOTTA);
    public static final Item ORANGE_TERRACOTTA = registerBlock(Blocks.ORANGE_TERRACOTTA);
    public static final Item MAGENTA_TERRACOTTA = registerBlock(Blocks.MAGENTA_TERRACOTTA);
    public static final Item LIGHT_BLUE_TERRACOTTA = registerBlock(Blocks.LIGHT_BLUE_TERRACOTTA);
    public static final Item YELLOW_TERRACOTTA = registerBlock(Blocks.YELLOW_TERRACOTTA);
    public static final Item LIME_TERRACOTTA = registerBlock(Blocks.LIME_TERRACOTTA);
    public static final Item PINK_TERRACOTTA = registerBlock(Blocks.PINK_TERRACOTTA);
    public static final Item GRAY_TERRACOTTA = registerBlock(Blocks.GRAY_TERRACOTTA);
    public static final Item LIGHT_GRAY_TERRACOTTA = registerBlock(Blocks.LIGHT_GRAY_TERRACOTTA);
    public static final Item CYAN_TERRACOTTA = registerBlock(Blocks.CYAN_TERRACOTTA);
    public static final Item PURPLE_TERRACOTTA = registerBlock(Blocks.PURPLE_TERRACOTTA);
    public static final Item BLUE_TERRACOTTA = registerBlock(Blocks.BLUE_TERRACOTTA);
    public static final Item BROWN_TERRACOTTA = registerBlock(Blocks.BROWN_TERRACOTTA);
    public static final Item GREEN_TERRACOTTA = registerBlock(Blocks.GREEN_TERRACOTTA);
    public static final Item RED_TERRACOTTA = registerBlock(Blocks.RED_TERRACOTTA);
    public static final Item BLACK_TERRACOTTA = registerBlock(Blocks.BLACK_TERRACOTTA);
    public static final Item BARRIER = registerBlock(new BlockItem(Blocks.BARRIER, new Item.Properties().rarity(Rarity.EPIC)));
    public static final Item LIGHT = registerBlock(new BlockItem(Blocks.LIGHT, new Item.Properties().rarity(Rarity.EPIC)));
    public static final Item HAY_BLOCK = registerBlock(Blocks.HAY_BLOCK);
    public static final Item WHITE_CARPET = registerBlock(Blocks.WHITE_CARPET);
    public static final Item ORANGE_CARPET = registerBlock(Blocks.ORANGE_CARPET);
    public static final Item MAGENTA_CARPET = registerBlock(Blocks.MAGENTA_CARPET);
    public static final Item LIGHT_BLUE_CARPET = registerBlock(Blocks.LIGHT_BLUE_CARPET);
    public static final Item YELLOW_CARPET = registerBlock(Blocks.YELLOW_CARPET);
    public static final Item LIME_CARPET = registerBlock(Blocks.LIME_CARPET);
    public static final Item PINK_CARPET = registerBlock(Blocks.PINK_CARPET);
    public static final Item GRAY_CARPET = registerBlock(Blocks.GRAY_CARPET);
    public static final Item LIGHT_GRAY_CARPET = registerBlock(Blocks.LIGHT_GRAY_CARPET);
    public static final Item CYAN_CARPET = registerBlock(Blocks.CYAN_CARPET);
    public static final Item PURPLE_CARPET = registerBlock(Blocks.PURPLE_CARPET);
    public static final Item BLUE_CARPET = registerBlock(Blocks.BLUE_CARPET);
    public static final Item BROWN_CARPET = registerBlock(Blocks.BROWN_CARPET);
    public static final Item GREEN_CARPET = registerBlock(Blocks.GREEN_CARPET);
    public static final Item RED_CARPET = registerBlock(Blocks.RED_CARPET);
    public static final Item BLACK_CARPET = registerBlock(Blocks.BLACK_CARPET);
    public static final Item TERRACOTTA = registerBlock(Blocks.TERRACOTTA);
    public static final Item PACKED_ICE = registerBlock(Blocks.PACKED_ICE);
    public static final Item DIRT_PATH = registerBlock(Blocks.DIRT_PATH);
    public static final Item SUNFLOWER = registerBlock(new DoubleHighBlockItem(Blocks.SUNFLOWER, new Item.Properties()));
    public static final Item LILAC = registerBlock(new DoubleHighBlockItem(Blocks.LILAC, new Item.Properties()));
    public static final Item ROSE_BUSH = registerBlock(new DoubleHighBlockItem(Blocks.ROSE_BUSH, new Item.Properties()));
    public static final Item PEONY = registerBlock(new DoubleHighBlockItem(Blocks.PEONY, new Item.Properties()));
    public static final Item TALL_GRASS = registerBlock(new DoubleHighBlockItem(Blocks.TALL_GRASS, new Item.Properties()));
    public static final Item LARGE_FERN = registerBlock(new DoubleHighBlockItem(Blocks.LARGE_FERN, new Item.Properties()));
    public static final Item WHITE_STAINED_GLASS = registerBlock(Blocks.WHITE_STAINED_GLASS);
    public static final Item ORANGE_STAINED_GLASS = registerBlock(Blocks.ORANGE_STAINED_GLASS);
    public static final Item MAGENTA_STAINED_GLASS = registerBlock(Blocks.MAGENTA_STAINED_GLASS);
    public static final Item LIGHT_BLUE_STAINED_GLASS = registerBlock(Blocks.LIGHT_BLUE_STAINED_GLASS);
    public static final Item YELLOW_STAINED_GLASS = registerBlock(Blocks.YELLOW_STAINED_GLASS);
    public static final Item LIME_STAINED_GLASS = registerBlock(Blocks.LIME_STAINED_GLASS);
    public static final Item PINK_STAINED_GLASS = registerBlock(Blocks.PINK_STAINED_GLASS);
    public static final Item GRAY_STAINED_GLASS = registerBlock(Blocks.GRAY_STAINED_GLASS);
    public static final Item LIGHT_GRAY_STAINED_GLASS = registerBlock(Blocks.LIGHT_GRAY_STAINED_GLASS);
    public static final Item CYAN_STAINED_GLASS = registerBlock(Blocks.CYAN_STAINED_GLASS);
    public static final Item PURPLE_STAINED_GLASS = registerBlock(Blocks.PURPLE_STAINED_GLASS);
    public static final Item BLUE_STAINED_GLASS = registerBlock(Blocks.BLUE_STAINED_GLASS);
    public static final Item BROWN_STAINED_GLASS = registerBlock(Blocks.BROWN_STAINED_GLASS);
    public static final Item GREEN_STAINED_GLASS = registerBlock(Blocks.GREEN_STAINED_GLASS);
    public static final Item RED_STAINED_GLASS = registerBlock(Blocks.RED_STAINED_GLASS);
    public static final Item BLACK_STAINED_GLASS = registerBlock(Blocks.BLACK_STAINED_GLASS);
    public static final Item WHITE_STAINED_GLASS_PANE = registerBlock(Blocks.WHITE_STAINED_GLASS_PANE);
    public static final Item ORANGE_STAINED_GLASS_PANE = registerBlock(Blocks.ORANGE_STAINED_GLASS_PANE);
    public static final Item MAGENTA_STAINED_GLASS_PANE = registerBlock(Blocks.MAGENTA_STAINED_GLASS_PANE);
    public static final Item LIGHT_BLUE_STAINED_GLASS_PANE = registerBlock(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
    public static final Item YELLOW_STAINED_GLASS_PANE = registerBlock(Blocks.YELLOW_STAINED_GLASS_PANE);
    public static final Item LIME_STAINED_GLASS_PANE = registerBlock(Blocks.LIME_STAINED_GLASS_PANE);
    public static final Item PINK_STAINED_GLASS_PANE = registerBlock(Blocks.PINK_STAINED_GLASS_PANE);
    public static final Item GRAY_STAINED_GLASS_PANE = registerBlock(Blocks.GRAY_STAINED_GLASS_PANE);
    public static final Item LIGHT_GRAY_STAINED_GLASS_PANE = registerBlock(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
    public static final Item CYAN_STAINED_GLASS_PANE = registerBlock(Blocks.CYAN_STAINED_GLASS_PANE);
    public static final Item PURPLE_STAINED_GLASS_PANE = registerBlock(Blocks.PURPLE_STAINED_GLASS_PANE);
    public static final Item BLUE_STAINED_GLASS_PANE = registerBlock(Blocks.BLUE_STAINED_GLASS_PANE);
    public static final Item BROWN_STAINED_GLASS_PANE = registerBlock(Blocks.BROWN_STAINED_GLASS_PANE);
    public static final Item GREEN_STAINED_GLASS_PANE = registerBlock(Blocks.GREEN_STAINED_GLASS_PANE);
    public static final Item RED_STAINED_GLASS_PANE = registerBlock(Blocks.RED_STAINED_GLASS_PANE);
    public static final Item BLACK_STAINED_GLASS_PANE = registerBlock(Blocks.BLACK_STAINED_GLASS_PANE);
    public static final Item PRISMARINE = registerBlock(Blocks.PRISMARINE);
    public static final Item PRISMARINE_BRICKS = registerBlock(Blocks.PRISMARINE_BRICKS);
    public static final Item DARK_PRISMARINE = registerBlock(Blocks.DARK_PRISMARINE);
    public static final Item PRISMARINE_STAIRS = registerBlock(Blocks.PRISMARINE_STAIRS);
    public static final Item PRISMARINE_BRICK_STAIRS = registerBlock(Blocks.PRISMARINE_BRICK_STAIRS);
    public static final Item DARK_PRISMARINE_STAIRS = registerBlock(Blocks.DARK_PRISMARINE_STAIRS);
    public static final Item SEA_LANTERN = registerBlock(Blocks.SEA_LANTERN);
    public static final Item RED_SANDSTONE = registerBlock(Blocks.RED_SANDSTONE);
    public static final Item CHISELED_RED_SANDSTONE = registerBlock(Blocks.CHISELED_RED_SANDSTONE);
    public static final Item CUT_RED_SANDSTONE = registerBlock(Blocks.CUT_RED_SANDSTONE);
    public static final Item RED_SANDSTONE_STAIRS = registerBlock(Blocks.RED_SANDSTONE_STAIRS);
    public static final Item REPEATING_COMMAND_BLOCK = registerBlock(
        new GameMasterBlockItem(Blocks.REPEATING_COMMAND_BLOCK, new Item.Properties().rarity(Rarity.EPIC))
    );
    public static final Item CHAIN_COMMAND_BLOCK = registerBlock(new GameMasterBlockItem(Blocks.CHAIN_COMMAND_BLOCK, new Item.Properties().rarity(Rarity.EPIC)));
    public static final Item MAGMA_BLOCK = registerBlock(Blocks.MAGMA_BLOCK);
    public static final Item NETHER_WART_BLOCK = registerBlock(Blocks.NETHER_WART_BLOCK);
    public static final Item WARPED_WART_BLOCK = registerBlock(Blocks.WARPED_WART_BLOCK);
    public static final Item RED_NETHER_BRICKS = registerBlock(Blocks.RED_NETHER_BRICKS);
    public static final Item BONE_BLOCK = registerBlock(Blocks.BONE_BLOCK);
    public static final Item STRUCTURE_VOID = registerBlock(new BlockItem(Blocks.STRUCTURE_VOID, new Item.Properties().rarity(Rarity.EPIC)));
    public static final Item SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item WHITE_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.WHITE_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item ORANGE_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.ORANGE_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item MAGENTA_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.MAGENTA_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item LIGHT_BLUE_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.LIGHT_BLUE_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item YELLOW_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.YELLOW_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item LIME_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.LIME_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item PINK_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.PINK_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item GRAY_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.GRAY_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item LIGHT_GRAY_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.LIGHT_GRAY_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item CYAN_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.CYAN_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item PURPLE_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.PURPLE_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item BLUE_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.BLUE_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item BROWN_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.BROWN_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item GREEN_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.GREEN_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item RED_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.RED_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item BLACK_SHULKER_BOX = registerBlock(
        new BlockItem(Blocks.BLACK_SHULKER_BOX, new Item.Properties().stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY))
    );
    public static final Item WHITE_GLAZED_TERRACOTTA = registerBlock(Blocks.WHITE_GLAZED_TERRACOTTA);
    public static final Item ORANGE_GLAZED_TERRACOTTA = registerBlock(Blocks.ORANGE_GLAZED_TERRACOTTA);
    public static final Item MAGENTA_GLAZED_TERRACOTTA = registerBlock(Blocks.MAGENTA_GLAZED_TERRACOTTA);
    public static final Item LIGHT_BLUE_GLAZED_TERRACOTTA = registerBlock(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA);
    public static final Item YELLOW_GLAZED_TERRACOTTA = registerBlock(Blocks.YELLOW_GLAZED_TERRACOTTA);
    public static final Item LIME_GLAZED_TERRACOTTA = registerBlock(Blocks.LIME_GLAZED_TERRACOTTA);
    public static final Item PINK_GLAZED_TERRACOTTA = registerBlock(Blocks.PINK_GLAZED_TERRACOTTA);
    public static final Item GRAY_GLAZED_TERRACOTTA = registerBlock(Blocks.GRAY_GLAZED_TERRACOTTA);
    public static final Item LIGHT_GRAY_GLAZED_TERRACOTTA = registerBlock(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA);
    public static final Item CYAN_GLAZED_TERRACOTTA = registerBlock(Blocks.CYAN_GLAZED_TERRACOTTA);
    public static final Item PURPLE_GLAZED_TERRACOTTA = registerBlock(Blocks.PURPLE_GLAZED_TERRACOTTA);
    public static final Item BLUE_GLAZED_TERRACOTTA = registerBlock(Blocks.BLUE_GLAZED_TERRACOTTA);
    public static final Item BROWN_GLAZED_TERRACOTTA = registerBlock(Blocks.BROWN_GLAZED_TERRACOTTA);
    public static final Item GREEN_GLAZED_TERRACOTTA = registerBlock(Blocks.GREEN_GLAZED_TERRACOTTA);
    public static final Item RED_GLAZED_TERRACOTTA = registerBlock(Blocks.RED_GLAZED_TERRACOTTA);
    public static final Item BLACK_GLAZED_TERRACOTTA = registerBlock(Blocks.BLACK_GLAZED_TERRACOTTA);
    public static final Item WHITE_CONCRETE = registerBlock(Blocks.WHITE_CONCRETE);
    public static final Item ORANGE_CONCRETE = registerBlock(Blocks.ORANGE_CONCRETE);
    public static final Item MAGENTA_CONCRETE = registerBlock(Blocks.MAGENTA_CONCRETE);
    public static final Item LIGHT_BLUE_CONCRETE = registerBlock(Blocks.LIGHT_BLUE_CONCRETE);
    public static final Item YELLOW_CONCRETE = registerBlock(Blocks.YELLOW_CONCRETE);
    public static final Item LIME_CONCRETE = registerBlock(Blocks.LIME_CONCRETE);
    public static final Item PINK_CONCRETE = registerBlock(Blocks.PINK_CONCRETE);
    public static final Item GRAY_CONCRETE = registerBlock(Blocks.GRAY_CONCRETE);
    public static final Item LIGHT_GRAY_CONCRETE = registerBlock(Blocks.LIGHT_GRAY_CONCRETE);
    public static final Item CYAN_CONCRETE = registerBlock(Blocks.CYAN_CONCRETE);
    public static final Item PURPLE_CONCRETE = registerBlock(Blocks.PURPLE_CONCRETE);
    public static final Item BLUE_CONCRETE = registerBlock(Blocks.BLUE_CONCRETE);
    public static final Item BROWN_CONCRETE = registerBlock(Blocks.BROWN_CONCRETE);
    public static final Item GREEN_CONCRETE = registerBlock(Blocks.GREEN_CONCRETE);
    public static final Item RED_CONCRETE = registerBlock(Blocks.RED_CONCRETE);
    public static final Item BLACK_CONCRETE = registerBlock(Blocks.BLACK_CONCRETE);
    public static final Item WHITE_CONCRETE_POWDER = registerBlock(Blocks.WHITE_CONCRETE_POWDER);
    public static final Item ORANGE_CONCRETE_POWDER = registerBlock(Blocks.ORANGE_CONCRETE_POWDER);
    public static final Item MAGENTA_CONCRETE_POWDER = registerBlock(Blocks.MAGENTA_CONCRETE_POWDER);
    public static final Item LIGHT_BLUE_CONCRETE_POWDER = registerBlock(Blocks.LIGHT_BLUE_CONCRETE_POWDER);
    public static final Item YELLOW_CONCRETE_POWDER = registerBlock(Blocks.YELLOW_CONCRETE_POWDER);
    public static final Item LIME_CONCRETE_POWDER = registerBlock(Blocks.LIME_CONCRETE_POWDER);
    public static final Item PINK_CONCRETE_POWDER = registerBlock(Blocks.PINK_CONCRETE_POWDER);
    public static final Item GRAY_CONCRETE_POWDER = registerBlock(Blocks.GRAY_CONCRETE_POWDER);
    public static final Item LIGHT_GRAY_CONCRETE_POWDER = registerBlock(Blocks.LIGHT_GRAY_CONCRETE_POWDER);
    public static final Item CYAN_CONCRETE_POWDER = registerBlock(Blocks.CYAN_CONCRETE_POWDER);
    public static final Item PURPLE_CONCRETE_POWDER = registerBlock(Blocks.PURPLE_CONCRETE_POWDER);
    public static final Item BLUE_CONCRETE_POWDER = registerBlock(Blocks.BLUE_CONCRETE_POWDER);
    public static final Item BROWN_CONCRETE_POWDER = registerBlock(Blocks.BROWN_CONCRETE_POWDER);
    public static final Item GREEN_CONCRETE_POWDER = registerBlock(Blocks.GREEN_CONCRETE_POWDER);
    public static final Item RED_CONCRETE_POWDER = registerBlock(Blocks.RED_CONCRETE_POWDER);
    public static final Item BLACK_CONCRETE_POWDER = registerBlock(Blocks.BLACK_CONCRETE_POWDER);
    public static final Item TURTLE_EGG = registerBlock(Blocks.TURTLE_EGG);
    public static final Item SNIFFER_EGG = registerBlock(Blocks.SNIFFER_EGG);
    public static final Item DEAD_TUBE_CORAL_BLOCK = registerBlock(Blocks.DEAD_TUBE_CORAL_BLOCK);
    public static final Item DEAD_BRAIN_CORAL_BLOCK = registerBlock(Blocks.DEAD_BRAIN_CORAL_BLOCK);
    public static final Item DEAD_BUBBLE_CORAL_BLOCK = registerBlock(Blocks.DEAD_BUBBLE_CORAL_BLOCK);
    public static final Item DEAD_FIRE_CORAL_BLOCK = registerBlock(Blocks.DEAD_FIRE_CORAL_BLOCK);
    public static final Item DEAD_HORN_CORAL_BLOCK = registerBlock(Blocks.DEAD_HORN_CORAL_BLOCK);
    public static final Item TUBE_CORAL_BLOCK = registerBlock(Blocks.TUBE_CORAL_BLOCK);
    public static final Item BRAIN_CORAL_BLOCK = registerBlock(Blocks.BRAIN_CORAL_BLOCK);
    public static final Item BUBBLE_CORAL_BLOCK = registerBlock(Blocks.BUBBLE_CORAL_BLOCK);
    public static final Item FIRE_CORAL_BLOCK = registerBlock(Blocks.FIRE_CORAL_BLOCK);
    public static final Item HORN_CORAL_BLOCK = registerBlock(Blocks.HORN_CORAL_BLOCK);
    public static final Item TUBE_CORAL = registerBlock(Blocks.TUBE_CORAL);
    public static final Item BRAIN_CORAL = registerBlock(Blocks.BRAIN_CORAL);
    public static final Item BUBBLE_CORAL = registerBlock(Blocks.BUBBLE_CORAL);
    public static final Item FIRE_CORAL = registerBlock(Blocks.FIRE_CORAL);
    public static final Item HORN_CORAL = registerBlock(Blocks.HORN_CORAL);
    public static final Item DEAD_BRAIN_CORAL = registerBlock(Blocks.DEAD_BRAIN_CORAL);
    public static final Item DEAD_BUBBLE_CORAL = registerBlock(Blocks.DEAD_BUBBLE_CORAL);
    public static final Item DEAD_FIRE_CORAL = registerBlock(Blocks.DEAD_FIRE_CORAL);
    public static final Item DEAD_HORN_CORAL = registerBlock(Blocks.DEAD_HORN_CORAL);
    public static final Item DEAD_TUBE_CORAL = registerBlock(Blocks.DEAD_TUBE_CORAL);
    public static final Item TUBE_CORAL_FAN = registerBlock(
        new StandingAndWallBlockItem(Blocks.TUBE_CORAL_FAN, Blocks.TUBE_CORAL_WALL_FAN, new Item.Properties(), Direction.DOWN)
    );
    public static final Item BRAIN_CORAL_FAN = registerBlock(
        new StandingAndWallBlockItem(Blocks.BRAIN_CORAL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, new Item.Properties(), Direction.DOWN)
    );
    public static final Item BUBBLE_CORAL_FAN = registerBlock(
        new StandingAndWallBlockItem(Blocks.BUBBLE_CORAL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN, new Item.Properties(), Direction.DOWN)
    );
    public static final Item FIRE_CORAL_FAN = registerBlock(
        new StandingAndWallBlockItem(Blocks.FIRE_CORAL_FAN, Blocks.FIRE_CORAL_WALL_FAN, new Item.Properties(), Direction.DOWN)
    );
    public static final Item HORN_CORAL_FAN = registerBlock(
        new StandingAndWallBlockItem(Blocks.HORN_CORAL_FAN, Blocks.HORN_CORAL_WALL_FAN, new Item.Properties(), Direction.DOWN)
    );
    public static final Item DEAD_TUBE_CORAL_FAN = registerBlock(
        new StandingAndWallBlockItem(Blocks.DEAD_TUBE_CORAL_FAN, Blocks.DEAD_TUBE_CORAL_WALL_FAN, new Item.Properties(), Direction.DOWN)
    );
    public static final Item DEAD_BRAIN_CORAL_FAN = registerBlock(
        new StandingAndWallBlockItem(Blocks.DEAD_BRAIN_CORAL_FAN, Blocks.DEAD_BRAIN_CORAL_WALL_FAN, new Item.Properties(), Direction.DOWN)
    );
    public static final Item DEAD_BUBBLE_CORAL_FAN = registerBlock(
        new StandingAndWallBlockItem(Blocks.DEAD_BUBBLE_CORAL_FAN, Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, new Item.Properties(), Direction.DOWN)
    );
    public static final Item DEAD_FIRE_CORAL_FAN = registerBlock(
        new StandingAndWallBlockItem(Blocks.DEAD_FIRE_CORAL_FAN, Blocks.DEAD_FIRE_CORAL_WALL_FAN, new Item.Properties(), Direction.DOWN)
    );
    public static final Item DEAD_HORN_CORAL_FAN = registerBlock(
        new StandingAndWallBlockItem(Blocks.DEAD_HORN_CORAL_FAN, Blocks.DEAD_HORN_CORAL_WALL_FAN, new Item.Properties(), Direction.DOWN)
    );
    public static final Item BLUE_ICE = registerBlock(Blocks.BLUE_ICE);
    public static final Item CONDUIT = registerBlock(new BlockItem(Blocks.CONDUIT, new Item.Properties().rarity(Rarity.RARE)));
    public static final Item POLISHED_GRANITE_STAIRS = registerBlock(Blocks.POLISHED_GRANITE_STAIRS);
    public static final Item SMOOTH_RED_SANDSTONE_STAIRS = registerBlock(Blocks.SMOOTH_RED_SANDSTONE_STAIRS);
    public static final Item MOSSY_STONE_BRICK_STAIRS = registerBlock(Blocks.MOSSY_STONE_BRICK_STAIRS);
    public static final Item POLISHED_DIORITE_STAIRS = registerBlock(Blocks.POLISHED_DIORITE_STAIRS);
    public static final Item MOSSY_COBBLESTONE_STAIRS = registerBlock(Blocks.MOSSY_COBBLESTONE_STAIRS);
    public static final Item END_STONE_BRICK_STAIRS = registerBlock(Blocks.END_STONE_BRICK_STAIRS);
    public static final Item STONE_STAIRS = registerBlock(Blocks.STONE_STAIRS);
    public static final Item SMOOTH_SANDSTONE_STAIRS = registerBlock(Blocks.SMOOTH_SANDSTONE_STAIRS);
    public static final Item SMOOTH_QUARTZ_STAIRS = registerBlock(Blocks.SMOOTH_QUARTZ_STAIRS);
    public static final Item GRANITE_STAIRS = registerBlock(Blocks.GRANITE_STAIRS);
    public static final Item ANDESITE_STAIRS = registerBlock(Blocks.ANDESITE_STAIRS);
    public static final Item RED_NETHER_BRICK_STAIRS = registerBlock(Blocks.RED_NETHER_BRICK_STAIRS);
    public static final Item POLISHED_ANDESITE_STAIRS = registerBlock(Blocks.POLISHED_ANDESITE_STAIRS);
    public static final Item DIORITE_STAIRS = registerBlock(Blocks.DIORITE_STAIRS);
    public static final Item COBBLED_DEEPSLATE_STAIRS = registerBlock(Blocks.COBBLED_DEEPSLATE_STAIRS);
    public static final Item POLISHED_DEEPSLATE_STAIRS = registerBlock(Blocks.POLISHED_DEEPSLATE_STAIRS);
    public static final Item DEEPSLATE_BRICK_STAIRS = registerBlock(Blocks.DEEPSLATE_BRICK_STAIRS);
    public static final Item DEEPSLATE_TILE_STAIRS = registerBlock(Blocks.DEEPSLATE_TILE_STAIRS);
    public static final Item POLISHED_GRANITE_SLAB = registerBlock(Blocks.POLISHED_GRANITE_SLAB);
    public static final Item SMOOTH_RED_SANDSTONE_SLAB = registerBlock(Blocks.SMOOTH_RED_SANDSTONE_SLAB);
    public static final Item MOSSY_STONE_BRICK_SLAB = registerBlock(Blocks.MOSSY_STONE_BRICK_SLAB);
    public static final Item POLISHED_DIORITE_SLAB = registerBlock(Blocks.POLISHED_DIORITE_SLAB);
    public static final Item MOSSY_COBBLESTONE_SLAB = registerBlock(Blocks.MOSSY_COBBLESTONE_SLAB);
    public static final Item END_STONE_BRICK_SLAB = registerBlock(Blocks.END_STONE_BRICK_SLAB);
    public static final Item SMOOTH_SANDSTONE_SLAB = registerBlock(Blocks.SMOOTH_SANDSTONE_SLAB);
    public static final Item SMOOTH_QUARTZ_SLAB = registerBlock(Blocks.SMOOTH_QUARTZ_SLAB);
    public static final Item GRANITE_SLAB = registerBlock(Blocks.GRANITE_SLAB);
    public static final Item ANDESITE_SLAB = registerBlock(Blocks.ANDESITE_SLAB);
    public static final Item RED_NETHER_BRICK_SLAB = registerBlock(Blocks.RED_NETHER_BRICK_SLAB);
    public static final Item POLISHED_ANDESITE_SLAB = registerBlock(Blocks.POLISHED_ANDESITE_SLAB);
    public static final Item DIORITE_SLAB = registerBlock(Blocks.DIORITE_SLAB);
    public static final Item COBBLED_DEEPSLATE_SLAB = registerBlock(Blocks.COBBLED_DEEPSLATE_SLAB);
    public static final Item POLISHED_DEEPSLATE_SLAB = registerBlock(Blocks.POLISHED_DEEPSLATE_SLAB);
    public static final Item DEEPSLATE_BRICK_SLAB = registerBlock(Blocks.DEEPSLATE_BRICK_SLAB);
    public static final Item DEEPSLATE_TILE_SLAB = registerBlock(Blocks.DEEPSLATE_TILE_SLAB);
    public static final Item SCAFFOLDING = registerBlock(new ScaffoldingBlockItem(Blocks.SCAFFOLDING, new Item.Properties()));
    public static final Item REDSTONE = registerItem("redstone", new ItemNameBlockItem(Blocks.REDSTONE_WIRE, new Item.Properties()));
    public static final Item REDSTONE_TORCH = registerBlock(
        new StandingAndWallBlockItem(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH, new Item.Properties(), Direction.DOWN)
    );
    public static final Item REDSTONE_BLOCK = registerBlock(Blocks.REDSTONE_BLOCK);
    public static final Item REPEATER = registerBlock(Blocks.REPEATER);
    public static final Item COMPARATOR = registerBlock(Blocks.COMPARATOR);
    public static final Item PISTON = registerBlock(Blocks.PISTON);
    public static final Item STICKY_PISTON = registerBlock(Blocks.STICKY_PISTON);
    public static final Item SLIME_BLOCK = registerBlock(Blocks.SLIME_BLOCK);
    public static final Item HONEY_BLOCK = registerBlock(Blocks.HONEY_BLOCK);
    public static final Item OBSERVER = registerBlock(Blocks.OBSERVER);
    public static final Item HOPPER = registerBlock(Blocks.HOPPER, p_332794_ -> p_332794_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
    public static final Item DISPENSER = registerBlock(
        Blocks.DISPENSER, p_332746_ -> p_332746_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    );
    public static final Item DROPPER = registerBlock(Blocks.DROPPER, p_332667_ -> p_332667_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
    public static final Item LECTERN = registerBlock(Blocks.LECTERN);
    public static final Item TARGET = registerBlock(Blocks.TARGET);
    public static final Item LEVER = registerBlock(Blocks.LEVER);
    public static final Item LIGHTNING_ROD = registerBlock(Blocks.LIGHTNING_ROD);
    public static final Item DAYLIGHT_DETECTOR = registerBlock(Blocks.DAYLIGHT_DETECTOR);
    public static final Item SCULK_SENSOR = registerBlock(Blocks.SCULK_SENSOR);
    public static final Item CALIBRATED_SCULK_SENSOR = registerBlock(Blocks.CALIBRATED_SCULK_SENSOR);
    public static final Item TRIPWIRE_HOOK = registerBlock(Blocks.TRIPWIRE_HOOK);
    public static final Item TRAPPED_CHEST = registerBlock(
        Blocks.TRAPPED_CHEST, p_332799_ -> p_332799_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    );
    public static final Item TNT = registerBlock(Blocks.TNT);
    public static final Item REDSTONE_LAMP = registerBlock(Blocks.REDSTONE_LAMP);
    public static final Item NOTE_BLOCK = registerBlock(Blocks.NOTE_BLOCK);
    public static final Item STONE_BUTTON = registerBlock(Blocks.STONE_BUTTON);
    public static final Item POLISHED_BLACKSTONE_BUTTON = registerBlock(Blocks.POLISHED_BLACKSTONE_BUTTON);
    public static final Item OAK_BUTTON = registerBlock(Blocks.OAK_BUTTON);
    public static final Item SPRUCE_BUTTON = registerBlock(Blocks.SPRUCE_BUTTON);
    public static final Item BIRCH_BUTTON = registerBlock(Blocks.BIRCH_BUTTON);
    public static final Item JUNGLE_BUTTON = registerBlock(Blocks.JUNGLE_BUTTON);
    public static final Item ACACIA_BUTTON = registerBlock(Blocks.ACACIA_BUTTON);
    public static final Item CHERRY_BUTTON = registerBlock(Blocks.CHERRY_BUTTON);
    public static final Item DARK_OAK_BUTTON = registerBlock(Blocks.DARK_OAK_BUTTON);
    public static final Item MANGROVE_BUTTON = registerBlock(Blocks.MANGROVE_BUTTON);
    public static final Item BAMBOO_BUTTON = registerBlock(Blocks.BAMBOO_BUTTON);
    public static final Item CRIMSON_BUTTON = registerBlock(Blocks.CRIMSON_BUTTON);
    public static final Item WARPED_BUTTON = registerBlock(Blocks.WARPED_BUTTON);
    public static final Item STONE_PRESSURE_PLATE = registerBlock(Blocks.STONE_PRESSURE_PLATE);
    public static final Item POLISHED_BLACKSTONE_PRESSURE_PLATE = registerBlock(Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE);
    public static final Item LIGHT_WEIGHTED_PRESSURE_PLATE = registerBlock(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
    public static final Item HEAVY_WEIGHTED_PRESSURE_PLATE = registerBlock(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
    public static final Item OAK_PRESSURE_PLATE = registerBlock(Blocks.OAK_PRESSURE_PLATE);
    public static final Item SPRUCE_PRESSURE_PLATE = registerBlock(Blocks.SPRUCE_PRESSURE_PLATE);
    public static final Item BIRCH_PRESSURE_PLATE = registerBlock(Blocks.BIRCH_PRESSURE_PLATE);
    public static final Item JUNGLE_PRESSURE_PLATE = registerBlock(Blocks.JUNGLE_PRESSURE_PLATE);
    public static final Item ACACIA_PRESSURE_PLATE = registerBlock(Blocks.ACACIA_PRESSURE_PLATE);
    public static final Item CHERRY_PRESSURE_PLATE = registerBlock(Blocks.CHERRY_PRESSURE_PLATE);
    public static final Item DARK_OAK_PRESSURE_PLATE = registerBlock(Blocks.DARK_OAK_PRESSURE_PLATE);
    public static final Item MANGROVE_PRESSURE_PLATE = registerBlock(Blocks.MANGROVE_PRESSURE_PLATE);
    public static final Item BAMBOO_PRESSURE_PLATE = registerBlock(Blocks.BAMBOO_PRESSURE_PLATE);
    public static final Item CRIMSON_PRESSURE_PLATE = registerBlock(Blocks.CRIMSON_PRESSURE_PLATE);
    public static final Item WARPED_PRESSURE_PLATE = registerBlock(Blocks.WARPED_PRESSURE_PLATE);
    public static final Item IRON_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.IRON_DOOR, new Item.Properties()));
    public static final Item OAK_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.OAK_DOOR, new Item.Properties()));
    public static final Item SPRUCE_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.SPRUCE_DOOR, new Item.Properties()));
    public static final Item BIRCH_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.BIRCH_DOOR, new Item.Properties()));
    public static final Item JUNGLE_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.JUNGLE_DOOR, new Item.Properties()));
    public static final Item ACACIA_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.ACACIA_DOOR, new Item.Properties()));
    public static final Item CHERRY_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.CHERRY_DOOR, new Item.Properties()));
    public static final Item DARK_OAK_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.DARK_OAK_DOOR, new Item.Properties()));
    public static final Item MANGROVE_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.MANGROVE_DOOR, new Item.Properties()));
    public static final Item BAMBOO_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.BAMBOO_DOOR, new Item.Properties()));
    public static final Item CRIMSON_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.CRIMSON_DOOR, new Item.Properties()));
    public static final Item WARPED_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.WARPED_DOOR, new Item.Properties()));
    public static final Item COPPER_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.COPPER_DOOR, new Item.Properties()));
    public static final Item EXPOSED_COPPER_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.EXPOSED_COPPER_DOOR, new Item.Properties()));
    public static final Item WEATHERED_COPPER_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.WEATHERED_COPPER_DOOR, new Item.Properties()));
    public static final Item OXIDIZED_COPPER_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.OXIDIZED_COPPER_DOOR, new Item.Properties()));
    public static final Item WAXED_COPPER_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.WAXED_COPPER_DOOR, new Item.Properties()));
    public static final Item WAXED_EXPOSED_COPPER_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.WAXED_EXPOSED_COPPER_DOOR, new Item.Properties()));
    public static final Item WAXED_WEATHERED_COPPER_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.WAXED_WEATHERED_COPPER_DOOR, new Item.Properties()));
    public static final Item WAXED_OXIDIZED_COPPER_DOOR = registerBlock(new DoubleHighBlockItem(Blocks.WAXED_OXIDIZED_COPPER_DOOR, new Item.Properties()));
    public static final Item IRON_TRAPDOOR = registerBlock(Blocks.IRON_TRAPDOOR);
    public static final Item OAK_TRAPDOOR = registerBlock(Blocks.OAK_TRAPDOOR);
    public static final Item SPRUCE_TRAPDOOR = registerBlock(Blocks.SPRUCE_TRAPDOOR);
    public static final Item BIRCH_TRAPDOOR = registerBlock(Blocks.BIRCH_TRAPDOOR);
    public static final Item JUNGLE_TRAPDOOR = registerBlock(Blocks.JUNGLE_TRAPDOOR);
    public static final Item ACACIA_TRAPDOOR = registerBlock(Blocks.ACACIA_TRAPDOOR);
    public static final Item CHERRY_TRAPDOOR = registerBlock(Blocks.CHERRY_TRAPDOOR);
    public static final Item DARK_OAK_TRAPDOOR = registerBlock(Blocks.DARK_OAK_TRAPDOOR);
    public static final Item MANGROVE_TRAPDOOR = registerBlock(Blocks.MANGROVE_TRAPDOOR);
    public static final Item BAMBOO_TRAPDOOR = registerBlock(Blocks.BAMBOO_TRAPDOOR);
    public static final Item CRIMSON_TRAPDOOR = registerBlock(Blocks.CRIMSON_TRAPDOOR);
    public static final Item WARPED_TRAPDOOR = registerBlock(Blocks.WARPED_TRAPDOOR);
    public static final Item COPPER_TRAPDOOR = registerBlock(Blocks.COPPER_TRAPDOOR);
    public static final Item EXPOSED_COPPER_TRAPDOOR = registerBlock(Blocks.EXPOSED_COPPER_TRAPDOOR);
    public static final Item WEATHERED_COPPER_TRAPDOOR = registerBlock(Blocks.WEATHERED_COPPER_TRAPDOOR);
    public static final Item OXIDIZED_COPPER_TRAPDOOR = registerBlock(Blocks.OXIDIZED_COPPER_TRAPDOOR);
    public static final Item WAXED_COPPER_TRAPDOOR = registerBlock(Blocks.WAXED_COPPER_TRAPDOOR);
    public static final Item WAXED_EXPOSED_COPPER_TRAPDOOR = registerBlock(Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR);
    public static final Item WAXED_WEATHERED_COPPER_TRAPDOOR = registerBlock(Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR);
    public static final Item WAXED_OXIDIZED_COPPER_TRAPDOOR = registerBlock(Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR);
    public static final Item OAK_FENCE_GATE = registerBlock(Blocks.OAK_FENCE_GATE);
    public static final Item SPRUCE_FENCE_GATE = registerBlock(Blocks.SPRUCE_FENCE_GATE);
    public static final Item BIRCH_FENCE_GATE = registerBlock(Blocks.BIRCH_FENCE_GATE);
    public static final Item JUNGLE_FENCE_GATE = registerBlock(Blocks.JUNGLE_FENCE_GATE);
    public static final Item ACACIA_FENCE_GATE = registerBlock(Blocks.ACACIA_FENCE_GATE);
    public static final Item CHERRY_FENCE_GATE = registerBlock(Blocks.CHERRY_FENCE_GATE);
    public static final Item DARK_OAK_FENCE_GATE = registerBlock(Blocks.DARK_OAK_FENCE_GATE);
    public static final Item MANGROVE_FENCE_GATE = registerBlock(Blocks.MANGROVE_FENCE_GATE);
    public static final Item BAMBOO_FENCE_GATE = registerBlock(Blocks.BAMBOO_FENCE_GATE);
    public static final Item CRIMSON_FENCE_GATE = registerBlock(Blocks.CRIMSON_FENCE_GATE);
    public static final Item WARPED_FENCE_GATE = registerBlock(Blocks.WARPED_FENCE_GATE);
    public static final Item POWERED_RAIL = registerBlock(Blocks.POWERED_RAIL);
    public static final Item DETECTOR_RAIL = registerBlock(Blocks.DETECTOR_RAIL);
    public static final Item RAIL = registerBlock(Blocks.RAIL);
    public static final Item ACTIVATOR_RAIL = registerBlock(Blocks.ACTIVATOR_RAIL);
    public static final Item SADDLE = registerItem("saddle", new SaddleItem(new Item.Properties().stacksTo(1)));
    public static final Item MINECART = registerItem("minecart", new MinecartItem(AbstractMinecart.Type.RIDEABLE, new Item.Properties().stacksTo(1)));
    public static final Item CHEST_MINECART = registerItem("chest_minecart", new MinecartItem(AbstractMinecart.Type.CHEST, new Item.Properties().stacksTo(1)));
    public static final Item FURNACE_MINECART = registerItem(
        "furnace_minecart", new MinecartItem(AbstractMinecart.Type.FURNACE, new Item.Properties().stacksTo(1))
    );
    public static final Item TNT_MINECART = registerItem("tnt_minecart", new MinecartItem(AbstractMinecart.Type.TNT, new Item.Properties().stacksTo(1)));
    public static final Item HOPPER_MINECART = registerItem(
        "hopper_minecart", new MinecartItem(AbstractMinecart.Type.HOPPER, new Item.Properties().stacksTo(1))
    );
    public static final Item CARROT_ON_A_STICK = registerItem(
        "carrot_on_a_stick", new FoodOnAStickItem<>(new Item.Properties().durability(25), EntityType.PIG, 7)
    );
    public static final Item WARPED_FUNGUS_ON_A_STICK = registerItem(
        "warped_fungus_on_a_stick", new FoodOnAStickItem<>(new Item.Properties().durability(100), EntityType.STRIDER, 1)
    );
    public static final Item ELYTRA = registerItem("elytra", new ElytraItem(new Item.Properties().durability(432).rarity(Rarity.UNCOMMON)));
    public static final Item OAK_BOAT = registerItem("oak_boat", new BoatItem(false, Boat.Type.OAK, new Item.Properties().stacksTo(1)));
    public static final Item OAK_CHEST_BOAT = registerItem("oak_chest_boat", new BoatItem(true, Boat.Type.OAK, new Item.Properties().stacksTo(1)));
    public static final Item SPRUCE_BOAT = registerItem("spruce_boat", new BoatItem(false, Boat.Type.SPRUCE, new Item.Properties().stacksTo(1)));
    public static final Item SPRUCE_CHEST_BOAT = registerItem("spruce_chest_boat", new BoatItem(true, Boat.Type.SPRUCE, new Item.Properties().stacksTo(1)));
    public static final Item BIRCH_BOAT = registerItem("birch_boat", new BoatItem(false, Boat.Type.BIRCH, new Item.Properties().stacksTo(1)));
    public static final Item BIRCH_CHEST_BOAT = registerItem("birch_chest_boat", new BoatItem(true, Boat.Type.BIRCH, new Item.Properties().stacksTo(1)));
    public static final Item JUNGLE_BOAT = registerItem("jungle_boat", new BoatItem(false, Boat.Type.JUNGLE, new Item.Properties().stacksTo(1)));
    public static final Item JUNGLE_CHEST_BOAT = registerItem("jungle_chest_boat", new BoatItem(true, Boat.Type.JUNGLE, new Item.Properties().stacksTo(1)));
    public static final Item ACACIA_BOAT = registerItem("acacia_boat", new BoatItem(false, Boat.Type.ACACIA, new Item.Properties().stacksTo(1)));
    public static final Item ACACIA_CHEST_BOAT = registerItem("acacia_chest_boat", new BoatItem(true, Boat.Type.ACACIA, new Item.Properties().stacksTo(1)));
    public static final Item CHERRY_BOAT = registerItem("cherry_boat", new BoatItem(false, Boat.Type.CHERRY, new Item.Properties().stacksTo(1)));
    public static final Item CHERRY_CHEST_BOAT = registerItem("cherry_chest_boat", new BoatItem(true, Boat.Type.CHERRY, new Item.Properties().stacksTo(1)));
    public static final Item DARK_OAK_BOAT = registerItem("dark_oak_boat", new BoatItem(false, Boat.Type.DARK_OAK, new Item.Properties().stacksTo(1)));
    public static final Item DARK_OAK_CHEST_BOAT = registerItem(
        "dark_oak_chest_boat", new BoatItem(true, Boat.Type.DARK_OAK, new Item.Properties().stacksTo(1))
    );
    public static final Item MANGROVE_BOAT = registerItem("mangrove_boat", new BoatItem(false, Boat.Type.MANGROVE, new Item.Properties().stacksTo(1)));
    public static final Item MANGROVE_CHEST_BOAT = registerItem(
        "mangrove_chest_boat", new BoatItem(true, Boat.Type.MANGROVE, new Item.Properties().stacksTo(1))
    );
    public static final Item BAMBOO_RAFT = registerItem("bamboo_raft", new BoatItem(false, Boat.Type.BAMBOO, new Item.Properties().stacksTo(1)));
    public static final Item BAMBOO_CHEST_RAFT = registerItem("bamboo_chest_raft", new BoatItem(true, Boat.Type.BAMBOO, new Item.Properties().stacksTo(1)));
    public static final Item STRUCTURE_BLOCK = registerBlock(new GameMasterBlockItem(Blocks.STRUCTURE_BLOCK, new Item.Properties().rarity(Rarity.EPIC)));
    public static final Item JIGSAW = registerBlock(new GameMasterBlockItem(Blocks.JIGSAW, new Item.Properties().rarity(Rarity.EPIC)));
    public static final Item TURTLE_HELMET = registerItem(
        "turtle_helmet", new ArmorItem(ArmorMaterials.TURTLE, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(25)))
    );
    public static final Item TURTLE_SCUTE = registerItem("turtle_scute", new Item(new Item.Properties()));
    public static final Item ARMADILLO_SCUTE = registerItem("armadillo_scute", new Item(new Item.Properties()));
    public static final Item WOLF_ARMOR = registerItem(
        "wolf_armor",
        new AnimalArmorItem(
            ArmorMaterials.ARMADILLO, AnimalArmorItem.BodyType.CANINE, true, new Item.Properties().durability(ArmorItem.Type.BODY.getDurability(4))
        )
    );
    public static final Item FLINT_AND_STEEL = registerItem("flint_and_steel", new FlintAndSteelItem(new Item.Properties().durability(64)));
    public static final Item BOWL = registerItem("bowl", new Item(new Item.Properties()));
    public static final Item APPLE = registerItem("apple", new Item(new Item.Properties().food(Foods.APPLE)));
    public static final Item BOW = registerItem("bow", new BowItem(new Item.Properties().durability(384)));
    public static final Item ARROW = registerItem("arrow", new ArrowItem(new Item.Properties()));
    public static final Item COAL = registerItem("coal", new Item(new Item.Properties()));
    public static final Item CHARCOAL = registerItem("charcoal", new Item(new Item.Properties()));
    public static final Item DIAMOND = registerItem("diamond", new Item(new Item.Properties()));
    public static final Item EMERALD = registerItem("emerald", new Item(new Item.Properties()));
    public static final Item LAPIS_LAZULI = registerItem("lapis_lazuli", new Item(new Item.Properties()));
    public static final Item QUARTZ = registerItem("quartz", new Item(new Item.Properties()));
    public static final Item AMETHYST_SHARD = registerItem("amethyst_shard", new Item(new Item.Properties()));
    public static final Item RAW_IRON = registerItem("raw_iron", new Item(new Item.Properties()));
    public static final Item IRON_INGOT = registerItem("iron_ingot", new Item(new Item.Properties()));
    public static final Item RAW_COPPER = registerItem("raw_copper", new Item(new Item.Properties()));
    public static final Item COPPER_INGOT = registerItem("copper_ingot", new Item(new Item.Properties()));
    public static final Item RAW_GOLD = registerItem("raw_gold", new Item(new Item.Properties()));
    public static final Item GOLD_INGOT = registerItem("gold_ingot", new Item(new Item.Properties()));
    public static final Item NETHERITE_INGOT = registerItem("netherite_ingot", new Item(new Item.Properties().fireResistant()));
    public static final Item NETHERITE_SCRAP = registerItem("netherite_scrap", new Item(new Item.Properties().fireResistant()));
    public static final Item WOODEN_SWORD = registerItem(
        "wooden_sword", new SwordItem(Tiers.WOOD, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.WOOD, 3, -2.4F)))
    );
    public static final Item WOODEN_SHOVEL = registerItem(
        "wooden_shovel", new ShovelItem(Tiers.WOOD, new Item.Properties().attributes(ShovelItem.createAttributes(Tiers.WOOD, 1.5F, -3.0F)))
    );
    public static final Item WOODEN_PICKAXE = registerItem(
        "wooden_pickaxe", new PickaxeItem(Tiers.WOOD, new Item.Properties().attributes(PickaxeItem.createAttributes(Tiers.WOOD, 1.0F, -2.8F)))
    );
    public static final Item WOODEN_AXE = registerItem(
        "wooden_axe", new AxeItem(Tiers.WOOD, new Item.Properties().attributes(AxeItem.createAttributes(Tiers.WOOD, 6.0F, -3.2F)))
    );
    public static final Item WOODEN_HOE = registerItem(
        "wooden_hoe", new HoeItem(Tiers.WOOD, new Item.Properties().attributes(HoeItem.createAttributes(Tiers.WOOD, 0.0F, -3.0F)))
    );
    public static final Item STONE_SWORD = registerItem(
        "stone_sword", new SwordItem(Tiers.STONE, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.STONE, 3, -2.4F)))
    );
    public static final Item STONE_SHOVEL = registerItem(
        "stone_shovel", new ShovelItem(Tiers.STONE, new Item.Properties().attributes(ShovelItem.createAttributes(Tiers.STONE, 1.5F, -3.0F)))
    );
    public static final Item STONE_PICKAXE = registerItem(
        "stone_pickaxe", new PickaxeItem(Tiers.STONE, new Item.Properties().attributes(PickaxeItem.createAttributes(Tiers.STONE, 1.0F, -2.8F)))
    );
    public static final Item STONE_AXE = registerItem(
        "stone_axe", new AxeItem(Tiers.STONE, new Item.Properties().attributes(AxeItem.createAttributes(Tiers.STONE, 7.0F, -3.2F)))
    );
    public static final Item STONE_HOE = registerItem(
        "stone_hoe", new HoeItem(Tiers.STONE, new Item.Properties().attributes(HoeItem.createAttributes(Tiers.STONE, -1.0F, -2.0F)))
    );
    public static final Item GOLDEN_SWORD = registerItem(
        "golden_sword", new SwordItem(Tiers.GOLD, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.GOLD, 3, -2.4F)))
    );
    public static final Item GOLDEN_SHOVEL = registerItem(
        "golden_shovel", new ShovelItem(Tiers.GOLD, new Item.Properties().attributes(ShovelItem.createAttributes(Tiers.GOLD, 1.5F, -3.0F)))
    );
    public static final Item GOLDEN_PICKAXE = registerItem(
        "golden_pickaxe", new PickaxeItem(Tiers.GOLD, new Item.Properties().attributes(PickaxeItem.createAttributes(Tiers.GOLD, 1.0F, -2.8F)))
    );
    public static final Item GOLDEN_AXE = registerItem(
        "golden_axe", new AxeItem(Tiers.GOLD, new Item.Properties().attributes(AxeItem.createAttributes(Tiers.GOLD, 6.0F, -3.0F)))
    );
    public static final Item GOLDEN_HOE = registerItem(
        "golden_hoe", new HoeItem(Tiers.GOLD, new Item.Properties().attributes(HoeItem.createAttributes(Tiers.GOLD, 0.0F, -3.0F)))
    );
    public static final Item IRON_SWORD = registerItem(
        "iron_sword", new SwordItem(Tiers.IRON, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4F)))
    );
    public static final Item IRON_SHOVEL = registerItem(
        "iron_shovel", new ShovelItem(Tiers.IRON, new Item.Properties().attributes(ShovelItem.createAttributes(Tiers.IRON, 1.5F, -3.0F)))
    );
    public static final Item IRON_PICKAXE = registerItem(
        "iron_pickaxe", new PickaxeItem(Tiers.IRON, new Item.Properties().attributes(PickaxeItem.createAttributes(Tiers.IRON, 1.0F, -2.8F)))
    );
    public static final Item IRON_AXE = registerItem(
        "iron_axe", new AxeItem(Tiers.IRON, new Item.Properties().attributes(AxeItem.createAttributes(Tiers.IRON, 6.0F, -3.1F)))
    );
    public static final Item IRON_HOE = registerItem(
        "iron_hoe", new HoeItem(Tiers.IRON, new Item.Properties().attributes(HoeItem.createAttributes(Tiers.IRON, -2.0F, -1.0F)))
    );
    public static final Item DIAMOND_SWORD = registerItem(
        "diamond_sword", new SwordItem(Tiers.DIAMOND, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4F)))
    );
    public static final Item DIAMOND_SHOVEL = registerItem(
        "diamond_shovel", new ShovelItem(Tiers.DIAMOND, new Item.Properties().attributes(ShovelItem.createAttributes(Tiers.DIAMOND, 1.5F, -3.0F)))
    );
    public static final Item DIAMOND_PICKAXE = registerItem(
        "diamond_pickaxe", new PickaxeItem(Tiers.DIAMOND, new Item.Properties().attributes(PickaxeItem.createAttributes(Tiers.DIAMOND, 1.0F, -2.8F)))
    );
    public static final Item DIAMOND_AXE = registerItem(
        "diamond_axe", new AxeItem(Tiers.DIAMOND, new Item.Properties().attributes(AxeItem.createAttributes(Tiers.DIAMOND, 5.0F, -3.0F)))
    );
    public static final Item DIAMOND_HOE = registerItem(
        "diamond_hoe", new HoeItem(Tiers.DIAMOND, new Item.Properties().attributes(HoeItem.createAttributes(Tiers.DIAMOND, -3.0F, 0.0F)))
    );
    public static final Item NETHERITE_SWORD = registerItem(
        "netherite_sword",
        new SwordItem(Tiers.NETHERITE, new Item.Properties().fireResistant().attributes(SwordItem.createAttributes(Tiers.NETHERITE, 3, -2.4F)))
    );
    public static final Item NETHERITE_SHOVEL = registerItem(
        "netherite_shovel",
        new ShovelItem(Tiers.NETHERITE, new Item.Properties().fireResistant().attributes(ShovelItem.createAttributes(Tiers.NETHERITE, 1.5F, -3.0F)))
    );
    public static final Item NETHERITE_PICKAXE = registerItem(
        "netherite_pickaxe",
        new PickaxeItem(Tiers.NETHERITE, new Item.Properties().fireResistant().attributes(PickaxeItem.createAttributes(Tiers.NETHERITE, 1.0F, -2.8F)))
    );
    public static final Item NETHERITE_AXE = registerItem(
        "netherite_axe", new AxeItem(Tiers.NETHERITE, new Item.Properties().fireResistant().attributes(AxeItem.createAttributes(Tiers.NETHERITE, 5.0F, -3.0F)))
    );
    public static final Item NETHERITE_HOE = registerItem(
        "netherite_hoe", new HoeItem(Tiers.NETHERITE, new Item.Properties().fireResistant().attributes(HoeItem.createAttributes(Tiers.NETHERITE, -4.0F, 0.0F)))
    );
    public static final Item STICK = registerItem("stick", new Item(new Item.Properties()));
    public static final Item MUSHROOM_STEW = registerItem("mushroom_stew", new Item(new Item.Properties().stacksTo(1).food(Foods.MUSHROOM_STEW)));
    public static final Item STRING = registerItem("string", new ItemNameBlockItem(Blocks.TRIPWIRE, new Item.Properties()));
    public static final Item FEATHER = registerItem("feather", new Item(new Item.Properties()));
    public static final Item GUNPOWDER = registerItem("gunpowder", new Item(new Item.Properties()));
    public static final Item WHEAT_SEEDS = registerItem("wheat_seeds", new ItemNameBlockItem(Blocks.WHEAT, new Item.Properties()));
    public static final Item WHEAT = registerItem("wheat", new Item(new Item.Properties()));
    public static final Item BREAD = registerItem("bread", new Item(new Item.Properties().food(Foods.BREAD)));
    public static final Item LEATHER_HELMET = registerItem(
        "leather_helmet",
        new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(5)))
    );
    public static final Item LEATHER_CHESTPLATE = registerItem(
        "leather_chestplate",
        new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(5)))
    );
    public static final Item LEATHER_LEGGINGS = registerItem(
        "leather_leggings",
        new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(5)))
    );
    public static final Item LEATHER_BOOTS = registerItem(
        "leather_boots", new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(5)))
    );
    public static final Item CHAINMAIL_HELMET = registerItem(
        "chainmail_helmet",
        new ArmorItem(ArmorMaterials.CHAIN, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(15)))
    );
    public static final Item CHAINMAIL_CHESTPLATE = registerItem(
        "chainmail_chestplate",
        new ArmorItem(ArmorMaterials.CHAIN, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(15)))
    );
    public static final Item CHAINMAIL_LEGGINGS = registerItem(
        "chainmail_leggings",
        new ArmorItem(ArmorMaterials.CHAIN, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(15)))
    );
    public static final Item CHAINMAIL_BOOTS = registerItem(
        "chainmail_boots", new ArmorItem(ArmorMaterials.CHAIN, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(15)))
    );
    public static final Item IRON_HELMET = registerItem(
        "iron_helmet", new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(15)))
    );
    public static final Item IRON_CHESTPLATE = registerItem(
        "iron_chestplate",
        new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(15)))
    );
    public static final Item IRON_LEGGINGS = registerItem(
        "iron_leggings",
        new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(15)))
    );
    public static final Item IRON_BOOTS = registerItem(
        "iron_boots", new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(15)))
    );
    public static final Item DIAMOND_HELMET = registerItem(
        "diamond_helmet",
        new ArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(33)))
    );
    public static final Item DIAMOND_CHESTPLATE = registerItem(
        "diamond_chestplate",
        new ArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(33)))
    );
    public static final Item DIAMOND_LEGGINGS = registerItem(
        "diamond_leggings",
        new ArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(33)))
    );
    public static final Item DIAMOND_BOOTS = registerItem(
        "diamond_boots", new ArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(33)))
    );
    public static final Item GOLDEN_HELMET = registerItem(
        "golden_helmet", new ArmorItem(ArmorMaterials.GOLD, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(7)))
    );
    public static final Item GOLDEN_CHESTPLATE = registerItem(
        "golden_chestplate",
        new ArmorItem(ArmorMaterials.GOLD, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(7)))
    );
    public static final Item GOLDEN_LEGGINGS = registerItem(
        "golden_leggings",
        new ArmorItem(ArmorMaterials.GOLD, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(7)))
    );
    public static final Item GOLDEN_BOOTS = registerItem(
        "golden_boots", new ArmorItem(ArmorMaterials.GOLD, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(7)))
    );
    public static final Item NETHERITE_HELMET = registerItem(
        "netherite_helmet",
        new ArmorItem(
            ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, new Item.Properties().fireResistant().durability(ArmorItem.Type.HELMET.getDurability(37))
        )
    );
    public static final Item NETHERITE_CHESTPLATE = registerItem(
        "netherite_chestplate",
        new ArmorItem(
            ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, new Item.Properties().fireResistant().durability(ArmorItem.Type.CHESTPLATE.getDurability(37))
        )
    );
    public static final Item NETHERITE_LEGGINGS = registerItem(
        "netherite_leggings",
        new ArmorItem(
            ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, new Item.Properties().fireResistant().durability(ArmorItem.Type.LEGGINGS.getDurability(37))
        )
    );
    public static final Item NETHERITE_BOOTS = registerItem(
        "netherite_boots",
        new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, new Item.Properties().fireResistant().durability(ArmorItem.Type.BOOTS.getDurability(37)))
    );
    public static final Item FLINT = registerItem("flint", new Item(new Item.Properties()));
    public static final Item PORKCHOP = registerItem("porkchop", new Item(new Item.Properties().food(Foods.PORKCHOP)));
    public static final Item COOKED_PORKCHOP = registerItem("cooked_porkchop", new Item(new Item.Properties().food(Foods.COOKED_PORKCHOP)));
    public static final Item PAINTING = registerItem("painting", new HangingEntityItem(EntityType.PAINTING, new Item.Properties()));
    public static final Item GOLDEN_APPLE = registerItem("golden_apple", new Item(new Item.Properties().rarity(Rarity.RARE).food(Foods.GOLDEN_APPLE)));
    public static final Item ENCHANTED_GOLDEN_APPLE = registerItem(
        "enchanted_golden_apple",
        new Item(new Item.Properties().rarity(Rarity.EPIC).food(Foods.ENCHANTED_GOLDEN_APPLE).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
    );
    public static final Item OAK_SIGN = registerItem("oak_sign", new SignItem(new Item.Properties().stacksTo(16), Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN));
    public static final Item SPRUCE_SIGN = registerItem(
        "spruce_sign", new SignItem(new Item.Properties().stacksTo(16), Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN)
    );
    public static final Item BIRCH_SIGN = registerItem(
        "birch_sign", new SignItem(new Item.Properties().stacksTo(16), Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN)
    );
    public static final Item JUNGLE_SIGN = registerItem(
        "jungle_sign", new SignItem(new Item.Properties().stacksTo(16), Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN)
    );
    public static final Item ACACIA_SIGN = registerItem(
        "acacia_sign", new SignItem(new Item.Properties().stacksTo(16), Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN)
    );
    public static final Item CHERRY_SIGN = registerItem(
        "cherry_sign", new SignItem(new Item.Properties().stacksTo(16), Blocks.CHERRY_SIGN, Blocks.CHERRY_WALL_SIGN)
    );
    public static final Item DARK_OAK_SIGN = registerItem(
        "dark_oak_sign", new SignItem(new Item.Properties().stacksTo(16), Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN)
    );
    public static final Item MANGROVE_SIGN = registerItem(
        "mangrove_sign", new SignItem(new Item.Properties().stacksTo(16), Blocks.MANGROVE_SIGN, Blocks.MANGROVE_WALL_SIGN)
    );
    public static final Item BAMBOO_SIGN = registerItem(
        "bamboo_sign", new SignItem(new Item.Properties().stacksTo(16), Blocks.BAMBOO_SIGN, Blocks.BAMBOO_WALL_SIGN)
    );
    public static final Item CRIMSON_SIGN = registerItem(
        "crimson_sign", new SignItem(new Item.Properties().stacksTo(16), Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN)
    );
    public static final Item WARPED_SIGN = registerItem(
        "warped_sign", new SignItem(new Item.Properties().stacksTo(16), Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN)
    );
    public static final Item OAK_HANGING_SIGN = registerItem(
        "oak_hanging_sign", new HangingSignItem(Blocks.OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))
    );
    public static final Item SPRUCE_HANGING_SIGN = registerItem(
        "spruce_hanging_sign", new HangingSignItem(Blocks.SPRUCE_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))
    );
    public static final Item BIRCH_HANGING_SIGN = registerItem(
        "birch_hanging_sign", new HangingSignItem(Blocks.BIRCH_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))
    );
    public static final Item JUNGLE_HANGING_SIGN = registerItem(
        "jungle_hanging_sign", new HangingSignItem(Blocks.JUNGLE_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))
    );
    public static final Item ACACIA_HANGING_SIGN = registerItem(
        "acacia_hanging_sign", new HangingSignItem(Blocks.ACACIA_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))
    );
    public static final Item CHERRY_HANGING_SIGN = registerItem(
        "cherry_hanging_sign", new HangingSignItem(Blocks.CHERRY_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))
    );
    public static final Item DARK_OAK_HANGING_SIGN = registerItem(
        "dark_oak_hanging_sign", new HangingSignItem(Blocks.DARK_OAK_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))
    );
    public static final Item MANGROVE_HANGING_SIGN = registerItem(
        "mangrove_hanging_sign", new HangingSignItem(Blocks.MANGROVE_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))
    );
    public static final Item BAMBOO_HANGING_SIGN = registerItem(
        "bamboo_hanging_sign", new HangingSignItem(Blocks.BAMBOO_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))
    );
    public static final Item CRIMSON_HANGING_SIGN = registerItem(
        "crimson_hanging_sign", new HangingSignItem(Blocks.CRIMSON_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))
    );
    public static final Item WARPED_HANGING_SIGN = registerItem(
        "warped_hanging_sign", new HangingSignItem(Blocks.WARPED_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN, new Item.Properties().stacksTo(16))
    );
    public static final Item BUCKET = registerItem("bucket", new BucketItem(Fluids.EMPTY, new Item.Properties().stacksTo(16)));
    public static final Item WATER_BUCKET = registerItem("water_bucket", new BucketItem(Fluids.WATER, new Item.Properties().craftRemainder(BUCKET).stacksTo(1)));
    public static final Item LAVA_BUCKET = registerItem("lava_bucket", new BucketItem(Fluids.LAVA, new Item.Properties().craftRemainder(BUCKET).stacksTo(1)));
    public static final Item POWDER_SNOW_BUCKET = registerItem(
        "powder_snow_bucket", new SolidBucketItem(Blocks.POWDER_SNOW, SoundEvents.BUCKET_EMPTY_POWDER_SNOW, new Item.Properties().stacksTo(1))
    );
    public static final Item SNOWBALL = registerItem("snowball", new SnowballItem(new Item.Properties().stacksTo(16)));
    public static final Item LEATHER = registerItem("leather", new Item(new Item.Properties()));
    public static final Item MILK_BUCKET = registerItem("milk_bucket", new MilkBucketItem(new Item.Properties().craftRemainder(BUCKET).stacksTo(1)));
    public static final Item PUFFERFISH_BUCKET = registerItem(
        "pufferfish_bucket",
        new MobBucketItem(
            EntityType.PUFFERFISH,
            Fluids.WATER,
            SoundEvents.BUCKET_EMPTY_FISH,
            new Item.Properties().stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)
        )
    );
    public static final Item SALMON_BUCKET = registerItem(
        "salmon_bucket",
        new MobBucketItem(
            EntityType.SALMON,
            Fluids.WATER,
            SoundEvents.BUCKET_EMPTY_FISH,
            new Item.Properties().stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)
        )
    );
    public static final Item COD_BUCKET = registerItem(
        "cod_bucket",
        new MobBucketItem(
            EntityType.COD,
            Fluids.WATER,
            SoundEvents.BUCKET_EMPTY_FISH,
            new Item.Properties().stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)
        )
    );
    public static final Item TROPICAL_FISH_BUCKET = registerItem(
        "tropical_fish_bucket",
        new MobBucketItem(
            EntityType.TROPICAL_FISH,
            Fluids.WATER,
            SoundEvents.BUCKET_EMPTY_FISH,
            new Item.Properties().stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)
        )
    );
    public static final Item AXOLOTL_BUCKET = registerItem(
        "axolotl_bucket",
        new MobBucketItem(
            EntityType.AXOLOTL,
            Fluids.WATER,
            SoundEvents.BUCKET_EMPTY_AXOLOTL,
            new Item.Properties().stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)
        )
    );
    public static final Item TADPOLE_BUCKET = registerItem(
        "tadpole_bucket",
        new MobBucketItem(
            EntityType.TADPOLE,
            Fluids.WATER,
            SoundEvents.BUCKET_EMPTY_TADPOLE,
            new Item.Properties().stacksTo(1).component(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY)
        )
    );
    public static final Item BRICK = registerItem("brick", new Item(new Item.Properties()));
    public static final Item CLAY_BALL = registerItem("clay_ball", new Item(new Item.Properties()));
    public static final Item DRIED_KELP_BLOCK = registerBlock(Blocks.DRIED_KELP_BLOCK);
    public static final Item PAPER = registerItem("paper", new Item(new Item.Properties()));
    public static final Item BOOK = registerItem("book", new BookItem(new Item.Properties()));
    public static final Item SLIME_BALL = registerItem("slime_ball", new Item(new Item.Properties()));
    public static final Item EGG = registerItem("egg", new EggItem(new Item.Properties().stacksTo(16)));
    public static final Item COMPASS = registerItem("compass", new CompassItem(new Item.Properties()));
    public static final Item RECOVERY_COMPASS = registerItem("recovery_compass", new Item(new Item.Properties()));
    public static final Item BUNDLE = registerItem(
        "bundle", new BundleItem(new Item.Properties().stacksTo(1).component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY))
    );
    public static final Item FISHING_ROD = registerItem("fishing_rod", new FishingRodItem(new Item.Properties().durability(64)));
    public static final Item CLOCK = registerItem("clock", new Item(new Item.Properties()));
    public static final Item SPYGLASS = registerItem("spyglass", new SpyglassItem(new Item.Properties().stacksTo(1)));
    public static final Item GLOWSTONE_DUST = registerItem("glowstone_dust", new Item(new Item.Properties()));
    public static final Item COD = registerItem("cod", new Item(new Item.Properties().food(Foods.COD)));
    public static final Item SALMON = registerItem("salmon", new Item(new Item.Properties().food(Foods.SALMON)));
    public static final Item TROPICAL_FISH = registerItem("tropical_fish", new Item(new Item.Properties().food(Foods.TROPICAL_FISH)));
    public static final Item PUFFERFISH = registerItem("pufferfish", new Item(new Item.Properties().food(Foods.PUFFERFISH)));
    public static final Item COOKED_COD = registerItem("cooked_cod", new Item(new Item.Properties().food(Foods.COOKED_COD)));
    public static final Item COOKED_SALMON = registerItem("cooked_salmon", new Item(new Item.Properties().food(Foods.COOKED_SALMON)));
    public static final Item INK_SAC = registerItem("ink_sac", new InkSacItem(new Item.Properties()));
    public static final Item GLOW_INK_SAC = registerItem("glow_ink_sac", new GlowInkSacItem(new Item.Properties()));
    public static final Item COCOA_BEANS = registerItem("cocoa_beans", new ItemNameBlockItem(Blocks.COCOA, new Item.Properties()));
    public static final Item WHITE_DYE = registerItem("white_dye", new DyeItem(DyeColor.WHITE, new Item.Properties()));
    public static final Item ORANGE_DYE = registerItem("orange_dye", new DyeItem(DyeColor.ORANGE, new Item.Properties()));
    public static final Item MAGENTA_DYE = registerItem("magenta_dye", new DyeItem(DyeColor.MAGENTA, new Item.Properties()));
    public static final Item LIGHT_BLUE_DYE = registerItem("light_blue_dye", new DyeItem(DyeColor.LIGHT_BLUE, new Item.Properties()));
    public static final Item YELLOW_DYE = registerItem("yellow_dye", new DyeItem(DyeColor.YELLOW, new Item.Properties()));
    public static final Item LIME_DYE = registerItem("lime_dye", new DyeItem(DyeColor.LIME, new Item.Properties()));
    public static final Item PINK_DYE = registerItem("pink_dye", new DyeItem(DyeColor.PINK, new Item.Properties()));
    public static final Item GRAY_DYE = registerItem("gray_dye", new DyeItem(DyeColor.GRAY, new Item.Properties()));
    public static final Item LIGHT_GRAY_DYE = registerItem("light_gray_dye", new DyeItem(DyeColor.LIGHT_GRAY, new Item.Properties()));
    public static final Item CYAN_DYE = registerItem("cyan_dye", new DyeItem(DyeColor.CYAN, new Item.Properties()));
    public static final Item PURPLE_DYE = registerItem("purple_dye", new DyeItem(DyeColor.PURPLE, new Item.Properties()));
    public static final Item BLUE_DYE = registerItem("blue_dye", new DyeItem(DyeColor.BLUE, new Item.Properties()));
    public static final Item BROWN_DYE = registerItem("brown_dye", new DyeItem(DyeColor.BROWN, new Item.Properties()));
    public static final Item GREEN_DYE = registerItem("green_dye", new DyeItem(DyeColor.GREEN, new Item.Properties()));
    public static final Item RED_DYE = registerItem("red_dye", new DyeItem(DyeColor.RED, new Item.Properties()));
    public static final Item BLACK_DYE = registerItem("black_dye", new DyeItem(DyeColor.BLACK, new Item.Properties()));
    public static final Item BONE_MEAL = registerItem("bone_meal", new BoneMealItem(new Item.Properties()));
    public static final Item BONE = registerItem("bone", new Item(new Item.Properties()));
    public static final Item SUGAR = registerItem("sugar", new Item(new Item.Properties()));
    public static final Item CAKE = registerBlock(new BlockItem(Blocks.CAKE, new Item.Properties().stacksTo(1)));
    public static final Item WHITE_BED = registerBlock(new BedItem(Blocks.WHITE_BED, new Item.Properties().stacksTo(1)));
    public static final Item ORANGE_BED = registerBlock(new BedItem(Blocks.ORANGE_BED, new Item.Properties().stacksTo(1)));
    public static final Item MAGENTA_BED = registerBlock(new BedItem(Blocks.MAGENTA_BED, new Item.Properties().stacksTo(1)));
    public static final Item LIGHT_BLUE_BED = registerBlock(new BedItem(Blocks.LIGHT_BLUE_BED, new Item.Properties().stacksTo(1)));
    public static final Item YELLOW_BED = registerBlock(new BedItem(Blocks.YELLOW_BED, new Item.Properties().stacksTo(1)));
    public static final Item LIME_BED = registerBlock(new BedItem(Blocks.LIME_BED, new Item.Properties().stacksTo(1)));
    public static final Item PINK_BED = registerBlock(new BedItem(Blocks.PINK_BED, new Item.Properties().stacksTo(1)));
    public static final Item GRAY_BED = registerBlock(new BedItem(Blocks.GRAY_BED, new Item.Properties().stacksTo(1)));
    public static final Item LIGHT_GRAY_BED = registerBlock(new BedItem(Blocks.LIGHT_GRAY_BED, new Item.Properties().stacksTo(1)));
    public static final Item CYAN_BED = registerBlock(new BedItem(Blocks.CYAN_BED, new Item.Properties().stacksTo(1)));
    public static final Item PURPLE_BED = registerBlock(new BedItem(Blocks.PURPLE_BED, new Item.Properties().stacksTo(1)));
    public static final Item BLUE_BED = registerBlock(new BedItem(Blocks.BLUE_BED, new Item.Properties().stacksTo(1)));
    public static final Item BROWN_BED = registerBlock(new BedItem(Blocks.BROWN_BED, new Item.Properties().stacksTo(1)));
    public static final Item GREEN_BED = registerBlock(new BedItem(Blocks.GREEN_BED, new Item.Properties().stacksTo(1)));
    public static final Item RED_BED = registerBlock(new BedItem(Blocks.RED_BED, new Item.Properties().stacksTo(1)));
    public static final Item BLACK_BED = registerBlock(new BedItem(Blocks.BLACK_BED, new Item.Properties().stacksTo(1)));
    public static final Item COOKIE = registerItem("cookie", new Item(new Item.Properties().food(Foods.COOKIE)));
    public static final Item CRAFTER = registerBlock(Blocks.CRAFTER, p_332815_ -> p_332815_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
    public static final Item FILLED_MAP = registerItem(
        "filled_map",
        new MapItem(
            new Item.Properties().component(DataComponents.MAP_COLOR, MapItemColor.DEFAULT).component(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY)
        )
    );
    public static final Item SHEARS = registerItem(
        "shears", new ShearsItem(new Item.Properties().durability(238).component(DataComponents.TOOL, ShearsItem.createToolProperties()))
    );
    public static final Item MELON_SLICE = registerItem("melon_slice", new Item(new Item.Properties().food(Foods.MELON_SLICE)));
    public static final Item DRIED_KELP = registerItem("dried_kelp", new Item(new Item.Properties().food(Foods.DRIED_KELP)));
    public static final Item PUMPKIN_SEEDS = registerItem(
        net.minecraft.references.Items.PUMPKIN_SEEDS, new ItemNameBlockItem(Blocks.PUMPKIN_STEM, new Item.Properties())
    );
    public static final Item MELON_SEEDS = registerItem(
        net.minecraft.references.Items.MELON_SEEDS, new ItemNameBlockItem(Blocks.MELON_STEM, new Item.Properties())
    );
    public static final Item BEEF = registerItem("beef", new Item(new Item.Properties().food(Foods.BEEF)));
    public static final Item COOKED_BEEF = registerItem("cooked_beef", new Item(new Item.Properties().food(Foods.COOKED_BEEF)));
    public static final Item CHICKEN = registerItem("chicken", new Item(new Item.Properties().food(Foods.CHICKEN)));
    public static final Item COOKED_CHICKEN = registerItem("cooked_chicken", new Item(new Item.Properties().food(Foods.COOKED_CHICKEN)));
    public static final Item ROTTEN_FLESH = registerItem("rotten_flesh", new Item(new Item.Properties().food(Foods.ROTTEN_FLESH)));
    public static final Item ENDER_PEARL = registerItem("ender_pearl", new EnderpearlItem(new Item.Properties().stacksTo(16)));
    public static final Item BLAZE_ROD = registerItem("blaze_rod", new Item(new Item.Properties()));
    public static final Item GHAST_TEAR = registerItem("ghast_tear", new Item(new Item.Properties()));
    public static final Item GOLD_NUGGET = registerItem("gold_nugget", new Item(new Item.Properties()));
    public static final Item NETHER_WART = registerItem("nether_wart", new ItemNameBlockItem(Blocks.NETHER_WART, new Item.Properties()));
    public static final Item POTION = registerItem(
        "potion", new PotionItem(new Item.Properties().stacksTo(1).component(DataComponents.POTION_CONTENTS, PotionContents.EMPTY))
    );
    public static final Item GLASS_BOTTLE = registerItem("glass_bottle", new BottleItem(new Item.Properties()));
    public static final Item SPIDER_EYE = registerItem("spider_eye", new Item(new Item.Properties().food(Foods.SPIDER_EYE)));
    public static final Item FERMENTED_SPIDER_EYE = registerItem("fermented_spider_eye", new Item(new Item.Properties()));
    public static final Item BLAZE_POWDER = registerItem("blaze_powder", new Item(new Item.Properties()));
    public static final Item MAGMA_CREAM = registerItem("magma_cream", new Item(new Item.Properties()));
    public static final Item BREWING_STAND = registerBlock(
        Blocks.BREWING_STAND, p_332688_ -> p_332688_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    );
    public static final Item CAULDRON = registerBlock(Blocks.CAULDRON, Blocks.WATER_CAULDRON, Blocks.LAVA_CAULDRON, Blocks.POWDER_SNOW_CAULDRON);
    public static final Item ENDER_EYE = registerItem("ender_eye", new EnderEyeItem(new Item.Properties()));
    public static final Item GLISTERING_MELON_SLICE = registerItem("glistering_melon_slice", new Item(new Item.Properties()));
    public static final Item ARMADILLO_SPAWN_EGG = registerItem(
        "armadillo_spawn_egg", new SpawnEggItem(EntityType.ARMADILLO, 11366765, 8538184, new Item.Properties())
    );
    public static final Item ALLAY_SPAWN_EGG = registerItem("allay_spawn_egg", new SpawnEggItem(EntityType.ALLAY, 56063, 44543, new Item.Properties()));
    public static final Item AXOLOTL_SPAWN_EGG = registerItem(
        "axolotl_spawn_egg", new SpawnEggItem(EntityType.AXOLOTL, 16499171, 10890612, new Item.Properties())
    );
    public static final Item BAT_SPAWN_EGG = registerItem("bat_spawn_egg", new SpawnEggItem(EntityType.BAT, 4996656, 986895, new Item.Properties()));
    public static final Item BEE_SPAWN_EGG = registerItem("bee_spawn_egg", new SpawnEggItem(EntityType.BEE, 15582019, 4400155, new Item.Properties()));
    public static final Item BLAZE_SPAWN_EGG = registerItem("blaze_spawn_egg", new SpawnEggItem(EntityType.BLAZE, 16167425, 16775294, new Item.Properties()));
    public static final Item BOGGED_SPAWN_EGG = registerItem("bogged_spawn_egg", new SpawnEggItem(EntityType.BOGGED, 9084018, 3231003, new Item.Properties()));
    public static final Item BREEZE_SPAWN_EGG = registerItem("breeze_spawn_egg", new SpawnEggItem(EntityType.BREEZE, 11506911, 9529055, new Item.Properties()));
    public static final Item CAT_SPAWN_EGG = registerItem("cat_spawn_egg", new SpawnEggItem(EntityType.CAT, 15714446, 9794134, new Item.Properties()));
    public static final Item CAMEL_SPAWN_EGG = registerItem("camel_spawn_egg", new SpawnEggItem(EntityType.CAMEL, 16565097, 13341495, new Item.Properties()));
    public static final Item CAVE_SPIDER_SPAWN_EGG = registerItem(
        "cave_spider_spawn_egg", new SpawnEggItem(EntityType.CAVE_SPIDER, 803406, 11013646, new Item.Properties())
    );
    public static final Item CHICKEN_SPAWN_EGG = registerItem(
        "chicken_spawn_egg", new SpawnEggItem(EntityType.CHICKEN, 10592673, 16711680, new Item.Properties())
    );
    public static final Item COD_SPAWN_EGG = registerItem("cod_spawn_egg", new SpawnEggItem(EntityType.COD, 12691306, 15058059, new Item.Properties()));
    public static final Item COW_SPAWN_EGG = registerItem("cow_spawn_egg", new SpawnEggItem(EntityType.COW, 4470310, 10592673, new Item.Properties()));
    public static final Item CREEPER_SPAWN_EGG = registerItem("creeper_spawn_egg", new SpawnEggItem(EntityType.CREEPER, 894731, 0, new Item.Properties()));
    public static final Item DOLPHIN_SPAWN_EGG = registerItem(
        "dolphin_spawn_egg", new SpawnEggItem(EntityType.DOLPHIN, 2243405, 16382457, new Item.Properties())
    );
    public static final Item DONKEY_SPAWN_EGG = registerItem("donkey_spawn_egg", new SpawnEggItem(EntityType.DONKEY, 5457209, 8811878, new Item.Properties()));
    public static final Item DROWNED_SPAWN_EGG = registerItem(
        "drowned_spawn_egg", new SpawnEggItem(EntityType.DROWNED, 9433559, 7969893, new Item.Properties())
    );
    public static final Item ELDER_GUARDIAN_SPAWN_EGG = registerItem(
        "elder_guardian_spawn_egg", new SpawnEggItem(EntityType.ELDER_GUARDIAN, 13552826, 7632531, new Item.Properties())
    );
    public static final Item ENDER_DRAGON_SPAWN_EGG = registerItem(
        "ender_dragon_spawn_egg", new SpawnEggItem(EntityType.ENDER_DRAGON, 1842204, 14711290, new Item.Properties())
    );
    public static final Item ENDERMAN_SPAWN_EGG = registerItem("enderman_spawn_egg", new SpawnEggItem(EntityType.ENDERMAN, 1447446, 0, new Item.Properties()));
    public static final Item ENDERMITE_SPAWN_EGG = registerItem(
        "endermite_spawn_egg", new SpawnEggItem(EntityType.ENDERMITE, 1447446, 7237230, new Item.Properties())
    );
    public static final Item EVOKER_SPAWN_EGG = registerItem("evoker_spawn_egg", new SpawnEggItem(EntityType.EVOKER, 9804699, 1973274, new Item.Properties()));
    public static final Item FOX_SPAWN_EGG = registerItem("fox_spawn_egg", new SpawnEggItem(EntityType.FOX, 14005919, 13396256, new Item.Properties()));
    public static final Item FROG_SPAWN_EGG = registerItem("frog_spawn_egg", new SpawnEggItem(EntityType.FROG, 13661252, 16762748, new Item.Properties()));
    public static final Item GHAST_SPAWN_EGG = registerItem("ghast_spawn_egg", new SpawnEggItem(EntityType.GHAST, 16382457, 12369084, new Item.Properties()));
    public static final Item GLOW_SQUID_SPAWN_EGG = registerItem(
        "glow_squid_spawn_egg", new SpawnEggItem(EntityType.GLOW_SQUID, 611926, 8778172, new Item.Properties())
    );
    public static final Item GOAT_SPAWN_EGG = registerItem("goat_spawn_egg", new SpawnEggItem(EntityType.GOAT, 10851452, 5589310, new Item.Properties()));
    public static final Item GUARDIAN_SPAWN_EGG = registerItem(
        "guardian_spawn_egg", new SpawnEggItem(EntityType.GUARDIAN, 5931634, 15826224, new Item.Properties())
    );
    public static final Item HOGLIN_SPAWN_EGG = registerItem("hoglin_spawn_egg", new SpawnEggItem(EntityType.HOGLIN, 13004373, 6251620, new Item.Properties()));
    public static final Item HORSE_SPAWN_EGG = registerItem("horse_spawn_egg", new SpawnEggItem(EntityType.HORSE, 12623485, 15656192, new Item.Properties()));
    public static final Item HUSK_SPAWN_EGG = registerItem("husk_spawn_egg", new SpawnEggItem(EntityType.HUSK, 7958625, 15125652, new Item.Properties()));
    public static final Item IRON_GOLEM_SPAWN_EGG = registerItem(
        "iron_golem_spawn_egg", new SpawnEggItem(EntityType.IRON_GOLEM, 14405058, 7643954, new Item.Properties())
    );
    public static final Item LLAMA_SPAWN_EGG = registerItem("llama_spawn_egg", new SpawnEggItem(EntityType.LLAMA, 12623485, 10051392, new Item.Properties()));
    public static final Item MAGMA_CUBE_SPAWN_EGG = registerItem(
        "magma_cube_spawn_egg", new SpawnEggItem(EntityType.MAGMA_CUBE, 3407872, 16579584, new Item.Properties())
    );
    public static final Item MOOSHROOM_SPAWN_EGG = registerItem(
        "mooshroom_spawn_egg", new SpawnEggItem(EntityType.MOOSHROOM, 10489616, 12040119, new Item.Properties())
    );
    public static final Item MULE_SPAWN_EGG = registerItem("mule_spawn_egg", new SpawnEggItem(EntityType.MULE, 1769984, 5321501, new Item.Properties()));
    public static final Item OCELOT_SPAWN_EGG = registerItem("ocelot_spawn_egg", new SpawnEggItem(EntityType.OCELOT, 15720061, 5653556, new Item.Properties()));
    public static final Item PANDA_SPAWN_EGG = registerItem("panda_spawn_egg", new SpawnEggItem(EntityType.PANDA, 15198183, 1776418, new Item.Properties()));
    public static final Item PARROT_SPAWN_EGG = registerItem("parrot_spawn_egg", new SpawnEggItem(EntityType.PARROT, 894731, 16711680, new Item.Properties()));
    public static final Item PHANTOM_SPAWN_EGG = registerItem(
        "phantom_spawn_egg", new SpawnEggItem(EntityType.PHANTOM, 4411786, 8978176, new Item.Properties())
    );
    public static final Item PIG_SPAWN_EGG = registerItem("pig_spawn_egg", new SpawnEggItem(EntityType.PIG, 15771042, 14377823, new Item.Properties()));
    public static final Item PIGLIN_SPAWN_EGG = registerItem("piglin_spawn_egg", new SpawnEggItem(EntityType.PIGLIN, 10051392, 16380836, new Item.Properties()));
    public static final Item PIGLIN_BRUTE_SPAWN_EGG = registerItem(
        "piglin_brute_spawn_egg", new SpawnEggItem(EntityType.PIGLIN_BRUTE, 5843472, 16380836, new Item.Properties())
    );
    public static final Item PILLAGER_SPAWN_EGG = registerItem(
        "pillager_spawn_egg", new SpawnEggItem(EntityType.PILLAGER, 5451574, 9804699, new Item.Properties())
    );
    public static final Item POLAR_BEAR_SPAWN_EGG = registerItem(
        "polar_bear_spawn_egg", new SpawnEggItem(EntityType.POLAR_BEAR, 15658718, 14014157, new Item.Properties())
    );
    public static final Item PUFFERFISH_SPAWN_EGG = registerItem(
        "pufferfish_spawn_egg", new SpawnEggItem(EntityType.PUFFERFISH, 16167425, 3654642, new Item.Properties())
    );
    public static final Item RABBIT_SPAWN_EGG = registerItem("rabbit_spawn_egg", new SpawnEggItem(EntityType.RABBIT, 10051392, 7555121, new Item.Properties()));
    public static final Item RAVAGER_SPAWN_EGG = registerItem(
        "ravager_spawn_egg", new SpawnEggItem(EntityType.RAVAGER, 7697520, 5984329, new Item.Properties())
    );
    public static final Item SALMON_SPAWN_EGG = registerItem("salmon_spawn_egg", new SpawnEggItem(EntityType.SALMON, 10489616, 951412, new Item.Properties()));
    public static final Item SHEEP_SPAWN_EGG = registerItem("sheep_spawn_egg", new SpawnEggItem(EntityType.SHEEP, 15198183, 16758197, new Item.Properties()));
    public static final Item SHULKER_SPAWN_EGG = registerItem(
        "shulker_spawn_egg", new SpawnEggItem(EntityType.SHULKER, 9725844, 5060690, new Item.Properties())
    );
    public static final Item SILVERFISH_SPAWN_EGG = registerItem(
        "silverfish_spawn_egg", new SpawnEggItem(EntityType.SILVERFISH, 7237230, 3158064, new Item.Properties())
    );
    public static final Item SKELETON_SPAWN_EGG = registerItem(
        "skeleton_spawn_egg", new SpawnEggItem(EntityType.SKELETON, 12698049, 4802889, new Item.Properties())
    );
    public static final Item SKELETON_HORSE_SPAWN_EGG = registerItem(
        "skeleton_horse_spawn_egg", new SpawnEggItem(EntityType.SKELETON_HORSE, 6842447, 15066584, new Item.Properties())
    );
    public static final Item SLIME_SPAWN_EGG = registerItem("slime_spawn_egg", new SpawnEggItem(EntityType.SLIME, 5349438, 8306542, new Item.Properties()));
    public static final Item SNIFFER_SPAWN_EGG = registerItem(
        "sniffer_spawn_egg", new SpawnEggItem(EntityType.SNIFFER, 8855049, 2468720, new Item.Properties())
    );
    public static final Item SNOW_GOLEM_SPAWN_EGG = registerItem(
        "snow_golem_spawn_egg", new SpawnEggItem(EntityType.SNOW_GOLEM, 14283506, 8496292, new Item.Properties())
    );
    public static final Item SPIDER_SPAWN_EGG = registerItem("spider_spawn_egg", new SpawnEggItem(EntityType.SPIDER, 3419431, 11013646, new Item.Properties()));
    public static final Item SQUID_SPAWN_EGG = registerItem("squid_spawn_egg", new SpawnEggItem(EntityType.SQUID, 2243405, 7375001, new Item.Properties()));
    public static final Item STRAY_SPAWN_EGG = registerItem("stray_spawn_egg", new SpawnEggItem(EntityType.STRAY, 6387319, 14543594, new Item.Properties()));
    public static final Item STRIDER_SPAWN_EGG = registerItem(
        "strider_spawn_egg", new SpawnEggItem(EntityType.STRIDER, 10236982, 5065037, new Item.Properties())
    );
    public static final Item TADPOLE_SPAWN_EGG = registerItem(
        "tadpole_spawn_egg", new SpawnEggItem(EntityType.TADPOLE, 7164733, 1444352, new Item.Properties())
    );
    public static final Item TRADER_LLAMA_SPAWN_EGG = registerItem(
        "trader_llama_spawn_egg", new SpawnEggItem(EntityType.TRADER_LLAMA, 15377456, 4547222, new Item.Properties())
    );
    public static final Item TROPICAL_FISH_SPAWN_EGG = registerItem(
        "tropical_fish_spawn_egg", new SpawnEggItem(EntityType.TROPICAL_FISH, 15690005, 16775663, new Item.Properties())
    );
    public static final Item TURTLE_SPAWN_EGG = registerItem("turtle_spawn_egg", new SpawnEggItem(EntityType.TURTLE, 15198183, 44975, new Item.Properties()));
    public static final Item VEX_SPAWN_EGG = registerItem("vex_spawn_egg", new SpawnEggItem(EntityType.VEX, 8032420, 15265265, new Item.Properties()));
    public static final Item VILLAGER_SPAWN_EGG = registerItem(
        "villager_spawn_egg", new SpawnEggItem(EntityType.VILLAGER, 5651507, 12422002, new Item.Properties())
    );
    public static final Item VINDICATOR_SPAWN_EGG = registerItem(
        "vindicator_spawn_egg", new SpawnEggItem(EntityType.VINDICATOR, 9804699, 2580065, new Item.Properties())
    );
    public static final Item WANDERING_TRADER_SPAWN_EGG = registerItem(
        "wandering_trader_spawn_egg", new SpawnEggItem(EntityType.WANDERING_TRADER, 4547222, 15377456, new Item.Properties())
    );
    public static final Item WARDEN_SPAWN_EGG = registerItem("warden_spawn_egg", new SpawnEggItem(EntityType.WARDEN, 1001033, 3790560, new Item.Properties()));
    public static final Item WITCH_SPAWN_EGG = registerItem("witch_spawn_egg", new SpawnEggItem(EntityType.WITCH, 3407872, 5349438, new Item.Properties()));
    public static final Item WITHER_SPAWN_EGG = registerItem("wither_spawn_egg", new SpawnEggItem(EntityType.WITHER, 1315860, 5075616, new Item.Properties()));
    public static final Item WITHER_SKELETON_SPAWN_EGG = registerItem(
        "wither_skeleton_spawn_egg", new SpawnEggItem(EntityType.WITHER_SKELETON, 1315860, 4672845, new Item.Properties())
    );
    public static final Item WOLF_SPAWN_EGG = registerItem("wolf_spawn_egg", new SpawnEggItem(EntityType.WOLF, 14144467, 13545366, new Item.Properties()));
    public static final Item ZOGLIN_SPAWN_EGG = registerItem("zoglin_spawn_egg", new SpawnEggItem(EntityType.ZOGLIN, 13004373, 15132390, new Item.Properties()));
    public static final Item ZOMBIE_SPAWN_EGG = registerItem("zombie_spawn_egg", new SpawnEggItem(EntityType.ZOMBIE, 44975, 7969893, new Item.Properties()));
    public static final Item ZOMBIE_HORSE_SPAWN_EGG = registerItem(
        "zombie_horse_spawn_egg", new SpawnEggItem(EntityType.ZOMBIE_HORSE, 3232308, 9945732, new Item.Properties())
    );
    public static final Item ZOMBIE_VILLAGER_SPAWN_EGG = registerItem(
        "zombie_villager_spawn_egg", new SpawnEggItem(EntityType.ZOMBIE_VILLAGER, 5651507, 7969893, new Item.Properties())
    );
    public static final Item ZOMBIFIED_PIGLIN_SPAWN_EGG = registerItem(
        "zombified_piglin_spawn_egg", new SpawnEggItem(EntityType.ZOMBIFIED_PIGLIN, 15373203, 5009705, new Item.Properties())
    );
    public static final Item EXPERIENCE_BOTTLE = registerItem(
        "experience_bottle", new ExperienceBottleItem(new Item.Properties().rarity(Rarity.UNCOMMON).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
    );
    public static final Item FIRE_CHARGE = registerItem("fire_charge", new FireChargeItem(new Item.Properties()));
    public static final Item WIND_CHARGE = registerItem("wind_charge", new WindChargeItem(new Item.Properties()));
    public static final Item WRITABLE_BOOK = registerItem(
        "writable_book", new WritableBookItem(new Item.Properties().stacksTo(1).component(DataComponents.WRITABLE_BOOK_CONTENT, WritableBookContent.EMPTY))
    );
    public static final Item WRITTEN_BOOK = registerItem(
        "written_book", new WrittenBookItem(new Item.Properties().stacksTo(16).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
    );
    public static final Item MACE = registerItem(
        "mace",
        new MaceItem(
            new Item.Properties()
                .rarity(Rarity.EPIC)
                .durability(500)
                .component(DataComponents.TOOL, MaceItem.createToolProperties())
                .attributes(MaceItem.createAttributes())
        )
    );
    public static final Item ITEM_FRAME = registerItem("item_frame", new ItemFrameItem(EntityType.ITEM_FRAME, new Item.Properties()));
    public static final Item GLOW_ITEM_FRAME = registerItem("glow_item_frame", new ItemFrameItem(EntityType.GLOW_ITEM_FRAME, new Item.Properties()));
    public static final Item FLOWER_POT = registerBlock(Blocks.FLOWER_POT);
    public static final Item CARROT = registerItem("carrot", new ItemNameBlockItem(Blocks.CARROTS, new Item.Properties().food(Foods.CARROT)));
    public static final Item POTATO = registerItem("potato", new ItemNameBlockItem(Blocks.POTATOES, new Item.Properties().food(Foods.POTATO)));
    public static final Item BAKED_POTATO = registerItem("baked_potato", new Item(new Item.Properties().food(Foods.BAKED_POTATO)));
    public static final Item POISONOUS_POTATO = registerItem("poisonous_potato", new Item(new Item.Properties().food(Foods.POISONOUS_POTATO)));
    public static final Item MAP = registerItem("map", new EmptyMapItem(new Item.Properties()));
    public static final Item GOLDEN_CARROT = registerItem("golden_carrot", new Item(new Item.Properties().food(Foods.GOLDEN_CARROT)));
    public static final Item SKELETON_SKULL = registerBlock(
        new StandingAndWallBlockItem(Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN)
    );
    public static final Item WITHER_SKELETON_SKULL = registerBlock(
        new StandingAndWallBlockItem(
            Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN
        )
    );
    public static final Item PLAYER_HEAD = registerBlock(
        new PlayerHeadItem(Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, new Item.Properties().rarity(Rarity.UNCOMMON))
    );
    public static final Item ZOMBIE_HEAD = registerBlock(
        new StandingAndWallBlockItem(Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN)
    );
    public static final Item CREEPER_HEAD = registerBlock(
        new StandingAndWallBlockItem(Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN)
    );
    public static final Item DRAGON_HEAD = registerBlock(
        new StandingAndWallBlockItem(Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD, new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN)
    );
    public static final Item PIGLIN_HEAD = registerBlock(
        new StandingAndWallBlockItem(Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD, new Item.Properties().rarity(Rarity.UNCOMMON), Direction.DOWN)
    );
    public static final Item NETHER_STAR = registerItem(
        "nether_star", new Item(new Item.Properties().rarity(Rarity.UNCOMMON).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
    );
    public static final Item PUMPKIN_PIE = registerItem("pumpkin_pie", new Item(new Item.Properties().food(Foods.PUMPKIN_PIE)));
    public static final Item FIREWORK_ROCKET = registerItem(
        "firework_rocket", new FireworkRocketItem(new Item.Properties().component(DataComponents.FIREWORKS, new Fireworks(1, List.of())))
    );
    public static final Item FIREWORK_STAR = registerItem("firework_star", new FireworkStarItem(new Item.Properties()));
    public static final Item ENCHANTED_BOOK = registerItem(
        "enchanted_book",
        new EnchantedBookItem(
            new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON)
                .component(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
        )
    );
    public static final Item NETHER_BRICK = registerItem("nether_brick", new Item(new Item.Properties()));
    public static final Item PRISMARINE_SHARD = registerItem("prismarine_shard", new Item(new Item.Properties()));
    public static final Item PRISMARINE_CRYSTALS = registerItem("prismarine_crystals", new Item(new Item.Properties()));
    public static final Item RABBIT = registerItem("rabbit", new Item(new Item.Properties().food(Foods.RABBIT)));
    public static final Item COOKED_RABBIT = registerItem("cooked_rabbit", new Item(new Item.Properties().food(Foods.COOKED_RABBIT)));
    public static final Item RABBIT_STEW = registerItem("rabbit_stew", new Item(new Item.Properties().stacksTo(1).food(Foods.RABBIT_STEW)));
    public static final Item RABBIT_FOOT = registerItem("rabbit_foot", new Item(new Item.Properties()));
    public static final Item RABBIT_HIDE = registerItem("rabbit_hide", new Item(new Item.Properties()));
    public static final Item ARMOR_STAND = registerItem("armor_stand", new ArmorStandItem(new Item.Properties().stacksTo(16)));
    public static final Item IRON_HORSE_ARMOR = registerItem(
        "iron_horse_armor", new AnimalArmorItem(ArmorMaterials.IRON, AnimalArmorItem.BodyType.EQUESTRIAN, false, new Item.Properties().stacksTo(1))
    );
    public static final Item GOLDEN_HORSE_ARMOR = registerItem(
        "golden_horse_armor", new AnimalArmorItem(ArmorMaterials.GOLD, AnimalArmorItem.BodyType.EQUESTRIAN, false, new Item.Properties().stacksTo(1))
    );
    public static final Item DIAMOND_HORSE_ARMOR = registerItem(
        "diamond_horse_armor", new AnimalArmorItem(ArmorMaterials.DIAMOND, AnimalArmorItem.BodyType.EQUESTRIAN, false, new Item.Properties().stacksTo(1))
    );
    public static final Item LEATHER_HORSE_ARMOR = registerItem(
        "leather_horse_armor", new AnimalArmorItem(ArmorMaterials.LEATHER, AnimalArmorItem.BodyType.EQUESTRIAN, false, new Item.Properties().stacksTo(1))
    );
    public static final Item LEAD = registerItem("lead", new LeadItem(new Item.Properties()));
    public static final Item NAME_TAG = registerItem("name_tag", new NameTagItem(new Item.Properties()));
    public static final Item COMMAND_BLOCK_MINECART = registerItem(
        "command_block_minecart", new MinecartItem(AbstractMinecart.Type.COMMAND_BLOCK, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    );
    public static final Item MUTTON = registerItem("mutton", new Item(new Item.Properties().food(Foods.MUTTON)));
    public static final Item COOKED_MUTTON = registerItem("cooked_mutton", new Item(new Item.Properties().food(Foods.COOKED_MUTTON)));
    public static final Item WHITE_BANNER = registerItem(
        "white_banner",
        new BannerItem(
            Blocks.WHITE_BANNER,
            Blocks.WHITE_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item ORANGE_BANNER = registerItem(
        "orange_banner",
        new BannerItem(
            Blocks.ORANGE_BANNER,
            Blocks.ORANGE_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item MAGENTA_BANNER = registerItem(
        "magenta_banner",
        new BannerItem(
            Blocks.MAGENTA_BANNER,
            Blocks.MAGENTA_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item LIGHT_BLUE_BANNER = registerItem(
        "light_blue_banner",
        new BannerItem(
            Blocks.LIGHT_BLUE_BANNER,
            Blocks.LIGHT_BLUE_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item YELLOW_BANNER = registerItem(
        "yellow_banner",
        new BannerItem(
            Blocks.YELLOW_BANNER,
            Blocks.YELLOW_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item LIME_BANNER = registerItem(
        "lime_banner",
        new BannerItem(
            Blocks.LIME_BANNER,
            Blocks.LIME_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item PINK_BANNER = registerItem(
        "pink_banner",
        new BannerItem(
            Blocks.PINK_BANNER,
            Blocks.PINK_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item GRAY_BANNER = registerItem(
        "gray_banner",
        new BannerItem(
            Blocks.GRAY_BANNER,
            Blocks.GRAY_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item LIGHT_GRAY_BANNER = registerItem(
        "light_gray_banner",
        new BannerItem(
            Blocks.LIGHT_GRAY_BANNER,
            Blocks.LIGHT_GRAY_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item CYAN_BANNER = registerItem(
        "cyan_banner",
        new BannerItem(
            Blocks.CYAN_BANNER,
            Blocks.CYAN_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item PURPLE_BANNER = registerItem(
        "purple_banner",
        new BannerItem(
            Blocks.PURPLE_BANNER,
            Blocks.PURPLE_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item BLUE_BANNER = registerItem(
        "blue_banner",
        new BannerItem(
            Blocks.BLUE_BANNER,
            Blocks.BLUE_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item BROWN_BANNER = registerItem(
        "brown_banner",
        new BannerItem(
            Blocks.BROWN_BANNER,
            Blocks.BROWN_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item GREEN_BANNER = registerItem(
        "green_banner",
        new BannerItem(
            Blocks.GREEN_BANNER,
            Blocks.GREEN_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item RED_BANNER = registerItem(
        "red_banner",
        new BannerItem(
            Blocks.RED_BANNER, Blocks.RED_WALL_BANNER, new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item BLACK_BANNER = registerItem(
        "black_banner",
        new BannerItem(
            Blocks.BLACK_BANNER,
            Blocks.BLACK_WALL_BANNER,
            new Item.Properties().stacksTo(16).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
        )
    );
    public static final Item END_CRYSTAL = registerItem(
        "end_crystal", new EndCrystalItem(new Item.Properties().rarity(Rarity.RARE).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
    );
    public static final Item CHORUS_FRUIT = registerItem("chorus_fruit", new ChorusFruitItem(new Item.Properties().food(Foods.CHORUS_FRUIT)));
    public static final Item POPPED_CHORUS_FRUIT = registerItem("popped_chorus_fruit", new Item(new Item.Properties()));
    public static final Item TORCHFLOWER_SEEDS = registerItem("torchflower_seeds", new ItemNameBlockItem(Blocks.TORCHFLOWER_CROP, new Item.Properties()));
    public static final Item PITCHER_POD = registerItem("pitcher_pod", new ItemNameBlockItem(Blocks.PITCHER_CROP, new Item.Properties()));
    public static final Item BEETROOT = registerItem("beetroot", new Item(new Item.Properties().food(Foods.BEETROOT)));
    public static final Item BEETROOT_SEEDS = registerItem("beetroot_seeds", new ItemNameBlockItem(Blocks.BEETROOTS, new Item.Properties()));
    public static final Item BEETROOT_SOUP = registerItem("beetroot_soup", new Item(new Item.Properties().stacksTo(1).food(Foods.BEETROOT_SOUP)));
    public static final Item DRAGON_BREATH = registerItem("dragon_breath", new Item(new Item.Properties().craftRemainder(GLASS_BOTTLE).rarity(Rarity.UNCOMMON)));
    public static final Item SPLASH_POTION = registerItem(
        "splash_potion", new SplashPotionItem(new Item.Properties().stacksTo(1).component(DataComponents.POTION_CONTENTS, PotionContents.EMPTY))
    );
    public static final Item SPECTRAL_ARROW = registerItem("spectral_arrow", new SpectralArrowItem(new Item.Properties()));
    public static final Item TIPPED_ARROW = registerItem(
        "tipped_arrow", new TippedArrowItem(new Item.Properties().component(DataComponents.POTION_CONTENTS, PotionContents.EMPTY))
    );
    public static final Item LINGERING_POTION = registerItem(
        "lingering_potion", new LingeringPotionItem(new Item.Properties().stacksTo(1).component(DataComponents.POTION_CONTENTS, PotionContents.EMPTY))
    );
    public static final Item SHIELD = registerItem(
        "shield", new ShieldItem(new Item.Properties().durability(336).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY))
    );
    public static final Item TOTEM_OF_UNDYING = registerItem("totem_of_undying", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final Item SHULKER_SHELL = registerItem("shulker_shell", new Item(new Item.Properties()));
    public static final Item IRON_NUGGET = registerItem("iron_nugget", new Item(new Item.Properties()));
    public static final Item KNOWLEDGE_BOOK = registerItem(
        "knowledge_book", new KnowledgeBookItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).component(DataComponents.RECIPES, List.of()))
    );
    public static final Item DEBUG_STICK = registerItem(
        "debug_stick",
        new DebugStickItem(
            new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .component(DataComponents.DEBUG_STICK_STATE, DebugStickState.EMPTY)
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
        )
    );
    public static final Item MUSIC_DISC_13 = registerItem(
        "music_disc_13", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.THIRTEEN))
    );
    public static final Item MUSIC_DISC_CAT = registerItem(
        "music_disc_cat", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.CAT))
    );
    public static final Item MUSIC_DISC_BLOCKS = registerItem(
        "music_disc_blocks", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.BLOCKS))
    );
    public static final Item MUSIC_DISC_CHIRP = registerItem(
        "music_disc_chirp", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.CHIRP))
    );
    public static final Item MUSIC_DISC_CREATOR = registerItem(
        "music_disc_creator", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.CREATOR))
    );
    public static final Item MUSIC_DISC_CREATOR_MUSIC_BOX = registerItem(
        "music_disc_creator_music_box", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.CREATOR_MUSIC_BOX))
    );
    public static final Item MUSIC_DISC_FAR = registerItem(
        "music_disc_far", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.FAR))
    );
    public static final Item MUSIC_DISC_MALL = registerItem(
        "music_disc_mall", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.MALL))
    );
    public static final Item MUSIC_DISC_MELLOHI = registerItem(
        "music_disc_mellohi", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.MELLOHI))
    );
    public static final Item MUSIC_DISC_STAL = registerItem(
        "music_disc_stal", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.STAL))
    );
    public static final Item MUSIC_DISC_STRAD = registerItem(
        "music_disc_strad", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.STRAD))
    );
    public static final Item MUSIC_DISC_WARD = registerItem(
        "music_disc_ward", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.WARD))
    );
    public static final Item MUSIC_DISC_11 = registerItem(
        "music_disc_11", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.ELEVEN))
    );
    public static final Item MUSIC_DISC_WAIT = registerItem(
        "music_disc_wait", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.WAIT))
    );
    public static final Item MUSIC_DISC_OTHERSIDE = registerItem(
        "music_disc_otherside", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.OTHERSIDE))
    );
    public static final Item MUSIC_DISC_RELIC = registerItem(
        "music_disc_relic", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.RELIC))
    );
    public static final Item MUSIC_DISC_5 = registerItem(
        "music_disc_5", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.FIVE))
    );
    public static final Item MUSIC_DISC_PIGSTEP = registerItem(
        "music_disc_pigstep", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.PIGSTEP))
    );
    public static final Item MUSIC_DISC_PRECIPICE = registerItem(
        "music_disc_precipice", new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(JukeboxSongs.PRECIPICE))
    );
    public static final Item DISC_FRAGMENT_5 = registerItem("disc_fragment_5", new DiscFragmentItem(new Item.Properties()));
    public static final Item TRIDENT = registerItem(
        "trident",
        new TridentItem(
            new Item.Properties()
                .rarity(Rarity.EPIC)
                .durability(250)
                .attributes(TridentItem.createAttributes())
                .component(DataComponents.TOOL, TridentItem.createToolProperties())
        )
    );
    public static final Item PHANTOM_MEMBRANE = registerItem("phantom_membrane", new Item(new Item.Properties()));
    public static final Item NAUTILUS_SHELL = registerItem("nautilus_shell", new Item(new Item.Properties()));
    public static final Item HEART_OF_THE_SEA = registerItem("heart_of_the_sea", new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final Item CROSSBOW = registerItem(
        "crossbow", new CrossbowItem(new Item.Properties().stacksTo(1).durability(465).component(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY))
    );
    public static final Item SUSPICIOUS_STEW = registerItem(
        "suspicious_stew",
        new SuspiciousStewItem(
            new Item.Properties().stacksTo(1).food(Foods.SUSPICIOUS_STEW).component(DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY)
        )
    );
    public static final Item LOOM = registerBlock(Blocks.LOOM);
    public static final Item FLOWER_BANNER_PATTERN = registerItem(
        "flower_banner_pattern", new BannerPatternItem(BannerPatternTags.PATTERN_ITEM_FLOWER, new Item.Properties().stacksTo(1))
    );
    public static final Item CREEPER_BANNER_PATTERN = registerItem(
        "creeper_banner_pattern", new BannerPatternItem(BannerPatternTags.PATTERN_ITEM_CREEPER, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    );
    public static final Item SKULL_BANNER_PATTERN = registerItem(
        "skull_banner_pattern", new BannerPatternItem(BannerPatternTags.PATTERN_ITEM_SKULL, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    );
    public static final Item MOJANG_BANNER_PATTERN = registerItem(
        "mojang_banner_pattern", new BannerPatternItem(BannerPatternTags.PATTERN_ITEM_MOJANG, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    );
    public static final Item GLOBE_BANNER_PATTERN = registerItem(
        "globe_banner_pattern", new BannerPatternItem(BannerPatternTags.PATTERN_ITEM_GLOBE, new Item.Properties().stacksTo(1))
    );
    public static final Item PIGLIN_BANNER_PATTERN = registerItem(
        "piglin_banner_pattern", new BannerPatternItem(BannerPatternTags.PATTERN_ITEM_PIGLIN, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    );
    public static final Item FLOW_BANNER_PATTERN = registerItem(
        "flow_banner_pattern", new BannerPatternItem(BannerPatternTags.PATTERN_ITEM_FLOW, new Item.Properties().stacksTo(1).rarity(Rarity.RARE))
    );
    public static final Item GUSTER_BANNER_PATTERN = registerItem(
        "guster_banner_pattern", new BannerPatternItem(BannerPatternTags.PATTERN_ITEM_GUSTER, new Item.Properties().stacksTo(1).rarity(Rarity.RARE))
    );
    public static final Item GOAT_HORN = registerItem("goat_horn", new InstrumentItem(new Item.Properties().stacksTo(1), InstrumentTags.GOAT_HORNS));
    public static final Item COMPOSTER = registerBlock(Blocks.COMPOSTER);
    public static final Item BARREL = registerBlock(Blocks.BARREL, p_332734_ -> p_332734_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
    public static final Item SMOKER = registerBlock(Blocks.SMOKER, p_332711_ -> p_332711_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
    public static final Item BLAST_FURNACE = registerBlock(
        Blocks.BLAST_FURNACE, p_332802_ -> p_332802_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    );
    public static final Item CARTOGRAPHY_TABLE = registerBlock(Blocks.CARTOGRAPHY_TABLE);
    public static final Item FLETCHING_TABLE = registerBlock(Blocks.FLETCHING_TABLE);
    public static final Item GRINDSTONE = registerBlock(Blocks.GRINDSTONE);
    public static final Item SMITHING_TABLE = registerBlock(Blocks.SMITHING_TABLE);
    public static final Item STONECUTTER = registerBlock(Blocks.STONECUTTER);
    public static final Item BELL = registerBlock(Blocks.BELL);
    public static final Item LANTERN = registerBlock(Blocks.LANTERN);
    public static final Item SOUL_LANTERN = registerBlock(Blocks.SOUL_LANTERN);
    public static final Item SWEET_BERRIES = registerItem(
        "sweet_berries", new ItemNameBlockItem(Blocks.SWEET_BERRY_BUSH, new Item.Properties().food(Foods.SWEET_BERRIES))
    );
    public static final Item GLOW_BERRIES = registerItem(
        "glow_berries", new ItemNameBlockItem(Blocks.CAVE_VINES, new Item.Properties().food(Foods.GLOW_BERRIES))
    );
    public static final Item CAMPFIRE = registerBlock(Blocks.CAMPFIRE, p_332697_ -> p_332697_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
    public static final Item SOUL_CAMPFIRE = registerBlock(
        Blocks.SOUL_CAMPFIRE, p_332832_ -> p_332832_.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    );
    public static final Item SHROOMLIGHT = registerBlock(Blocks.SHROOMLIGHT);
    public static final Item HONEYCOMB = registerItem("honeycomb", new HoneycombItem(new Item.Properties()));
    public static final Item BEE_NEST = registerBlock(new BlockItem(Blocks.BEE_NEST, new Item.Properties().component(DataComponents.BEES, List.of())));
    public static final Item BEEHIVE = registerBlock(new BlockItem(Blocks.BEEHIVE, new Item.Properties().component(DataComponents.BEES, List.of())));
    public static final Item HONEY_BOTTLE = registerItem(
        "honey_bottle", new HoneyBottleItem(new Item.Properties().craftRemainder(GLASS_BOTTLE).food(Foods.HONEY_BOTTLE).stacksTo(16))
    );
    public static final Item HONEYCOMB_BLOCK = registerBlock(Blocks.HONEYCOMB_BLOCK);
    public static final Item LODESTONE = registerBlock(Blocks.LODESTONE);
    public static final Item CRYING_OBSIDIAN = registerBlock(Blocks.CRYING_OBSIDIAN);
    public static final Item BLACKSTONE = registerBlock(Blocks.BLACKSTONE);
    public static final Item BLACKSTONE_SLAB = registerBlock(Blocks.BLACKSTONE_SLAB);
    public static final Item BLACKSTONE_STAIRS = registerBlock(Blocks.BLACKSTONE_STAIRS);
    public static final Item GILDED_BLACKSTONE = registerBlock(Blocks.GILDED_BLACKSTONE);
    public static final Item POLISHED_BLACKSTONE = registerBlock(Blocks.POLISHED_BLACKSTONE);
    public static final Item POLISHED_BLACKSTONE_SLAB = registerBlock(Blocks.POLISHED_BLACKSTONE_SLAB);
    public static final Item POLISHED_BLACKSTONE_STAIRS = registerBlock(Blocks.POLISHED_BLACKSTONE_STAIRS);
    public static final Item CHISELED_POLISHED_BLACKSTONE = registerBlock(Blocks.CHISELED_POLISHED_BLACKSTONE);
    public static final Item POLISHED_BLACKSTONE_BRICKS = registerBlock(Blocks.POLISHED_BLACKSTONE_BRICKS);
    public static final Item POLISHED_BLACKSTONE_BRICK_SLAB = registerBlock(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB);
    public static final Item POLISHED_BLACKSTONE_BRICK_STAIRS = registerBlock(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS);
    public static final Item CRACKED_POLISHED_BLACKSTONE_BRICKS = registerBlock(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
    public static final Item RESPAWN_ANCHOR = registerBlock(Blocks.RESPAWN_ANCHOR);
    public static final Item CANDLE = registerBlock(Blocks.CANDLE);
    public static final Item WHITE_CANDLE = registerBlock(Blocks.WHITE_CANDLE);
    public static final Item ORANGE_CANDLE = registerBlock(Blocks.ORANGE_CANDLE);
    public static final Item MAGENTA_CANDLE = registerBlock(Blocks.MAGENTA_CANDLE);
    public static final Item LIGHT_BLUE_CANDLE = registerBlock(Blocks.LIGHT_BLUE_CANDLE);
    public static final Item YELLOW_CANDLE = registerBlock(Blocks.YELLOW_CANDLE);
    public static final Item LIME_CANDLE = registerBlock(Blocks.LIME_CANDLE);
    public static final Item PINK_CANDLE = registerBlock(Blocks.PINK_CANDLE);
    public static final Item GRAY_CANDLE = registerBlock(Blocks.GRAY_CANDLE);
    public static final Item LIGHT_GRAY_CANDLE = registerBlock(Blocks.LIGHT_GRAY_CANDLE);
    public static final Item CYAN_CANDLE = registerBlock(Blocks.CYAN_CANDLE);
    public static final Item PURPLE_CANDLE = registerBlock(Blocks.PURPLE_CANDLE);
    public static final Item BLUE_CANDLE = registerBlock(Blocks.BLUE_CANDLE);
    public static final Item BROWN_CANDLE = registerBlock(Blocks.BROWN_CANDLE);
    public static final Item GREEN_CANDLE = registerBlock(Blocks.GREEN_CANDLE);
    public static final Item RED_CANDLE = registerBlock(Blocks.RED_CANDLE);
    public static final Item BLACK_CANDLE = registerBlock(Blocks.BLACK_CANDLE);
    public static final Item SMALL_AMETHYST_BUD = registerBlock(Blocks.SMALL_AMETHYST_BUD);
    public static final Item MEDIUM_AMETHYST_BUD = registerBlock(Blocks.MEDIUM_AMETHYST_BUD);
    public static final Item LARGE_AMETHYST_BUD = registerBlock(Blocks.LARGE_AMETHYST_BUD);
    public static final Item AMETHYST_CLUSTER = registerBlock(Blocks.AMETHYST_CLUSTER);
    public static final Item POINTED_DRIPSTONE = registerBlock(Blocks.POINTED_DRIPSTONE);
    public static final Item OCHRE_FROGLIGHT = registerBlock(Blocks.OCHRE_FROGLIGHT);
    public static final Item VERDANT_FROGLIGHT = registerBlock(Blocks.VERDANT_FROGLIGHT);
    public static final Item PEARLESCENT_FROGLIGHT = registerBlock(Blocks.PEARLESCENT_FROGLIGHT);
    public static final Item FROGSPAWN = registerBlock(new PlaceOnWaterBlockItem(Blocks.FROGSPAWN, new Item.Properties()));
    public static final Item ECHO_SHARD = registerItem("echo_shard", new Item(new Item.Properties()));
    public static final Item BRUSH = registerItem("brush", new BrushItem(new Item.Properties().durability(64)));
    public static final Item NETHERITE_UPGRADE_SMITHING_TEMPLATE = registerItem(
        "netherite_upgrade_smithing_template", SmithingTemplateItem.createNetheriteUpgradeTemplate()
    );
    public static final Item SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "sentry_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.SENTRY)
    );
    public static final Item DUNE_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "dune_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.DUNE)
    );
    public static final Item COAST_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "coast_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.COAST)
    );
    public static final Item WILD_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "wild_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.WILD)
    );
    public static final Item WARD_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "ward_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.WARD)
    );
    public static final Item EYE_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "eye_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.EYE)
    );
    public static final Item VEX_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "vex_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.VEX)
    );
    public static final Item TIDE_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "tide_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.TIDE)
    );
    public static final Item SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "snout_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.SNOUT)
    );
    public static final Item RIB_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "rib_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.RIB)
    );
    public static final Item SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "spire_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.SPIRE)
    );
    public static final Item WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "wayfinder_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.WAYFINDER)
    );
    public static final Item SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "shaper_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.SHAPER)
    );
    public static final Item SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "silence_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.SILENCE)
    );
    public static final Item RAISER_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "raiser_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.RAISER)
    );
    public static final Item HOST_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "host_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.HOST)
    );
    public static final Item FLOW_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "flow_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.FLOW)
    );
    public static final Item BOLT_ARMOR_TRIM_SMITHING_TEMPLATE = registerItem(
        "bolt_armor_trim_smithing_template", SmithingTemplateItem.createArmorTrimTemplate(TrimPatterns.BOLT)
    );
    public static final Item ANGLER_POTTERY_SHERD = registerItem("angler_pottery_sherd", new Item(new Item.Properties()));
    public static final Item ARCHER_POTTERY_SHERD = registerItem("archer_pottery_sherd", new Item(new Item.Properties()));
    public static final Item ARMS_UP_POTTERY_SHERD = registerItem("arms_up_pottery_sherd", new Item(new Item.Properties()));
    public static final Item BLADE_POTTERY_SHERD = registerItem("blade_pottery_sherd", new Item(new Item.Properties()));
    public static final Item BREWER_POTTERY_SHERD = registerItem("brewer_pottery_sherd", new Item(new Item.Properties()));
    public static final Item BURN_POTTERY_SHERD = registerItem("burn_pottery_sherd", new Item(new Item.Properties()));
    public static final Item DANGER_POTTERY_SHERD = registerItem("danger_pottery_sherd", new Item(new Item.Properties()));
    public static final Item EXPLORER_POTTERY_SHERD = registerItem("explorer_pottery_sherd", new Item(new Item.Properties()));
    public static final Item FLOW_POTTERY_SHERD = registerItem("flow_pottery_sherd", new Item(new Item.Properties()));
    public static final Item FRIEND_POTTERY_SHERD = registerItem("friend_pottery_sherd", new Item(new Item.Properties()));
    public static final Item GUSTER_POTTERY_SHERD = registerItem("guster_pottery_sherd", new Item(new Item.Properties()));
    public static final Item HEART_POTTERY_SHERD = registerItem("heart_pottery_sherd", new Item(new Item.Properties()));
    public static final Item HEARTBREAK_POTTERY_SHERD = registerItem("heartbreak_pottery_sherd", new Item(new Item.Properties()));
    public static final Item HOWL_POTTERY_SHERD = registerItem("howl_pottery_sherd", new Item(new Item.Properties()));
    public static final Item MINER_POTTERY_SHERD = registerItem("miner_pottery_sherd", new Item(new Item.Properties()));
    public static final Item MOURNER_POTTERY_SHERD = registerItem("mourner_pottery_sherd", new Item(new Item.Properties()));
    public static final Item PLENTY_POTTERY_SHERD = registerItem("plenty_pottery_sherd", new Item(new Item.Properties()));
    public static final Item PRIZE_POTTERY_SHERD = registerItem("prize_pottery_sherd", new Item(new Item.Properties()));
    public static final Item SCRAPE_POTTERY_SHERD = registerItem("scrape_pottery_sherd", new Item(new Item.Properties()));
    public static final Item SHEAF_POTTERY_SHERD = registerItem("sheaf_pottery_sherd", new Item(new Item.Properties()));
    public static final Item SHELTER_POTTERY_SHERD = registerItem("shelter_pottery_sherd", new Item(new Item.Properties()));
    public static final Item SKULL_POTTERY_SHERD = registerItem("skull_pottery_sherd", new Item(new Item.Properties()));
    public static final Item SNORT_POTTERY_SHERD = registerItem("snort_pottery_sherd", new Item(new Item.Properties()));
    public static final Item COPPER_GRATE = registerBlock(Blocks.COPPER_GRATE);
    public static final Item EXPOSED_COPPER_GRATE = registerBlock(Blocks.EXPOSED_COPPER_GRATE);
    public static final Item WEATHERED_COPPER_GRATE = registerBlock(Blocks.WEATHERED_COPPER_GRATE);
    public static final Item OXIDIZED_COPPER_GRATE = registerBlock(Blocks.OXIDIZED_COPPER_GRATE);
    public static final Item WAXED_COPPER_GRATE = registerBlock(Blocks.WAXED_COPPER_GRATE);
    public static final Item WAXED_EXPOSED_COPPER_GRATE = registerBlock(Blocks.WAXED_EXPOSED_COPPER_GRATE);
    public static final Item WAXED_WEATHERED_COPPER_GRATE = registerBlock(Blocks.WAXED_WEATHERED_COPPER_GRATE);
    public static final Item WAXED_OXIDIZED_COPPER_GRATE = registerBlock(Blocks.WAXED_OXIDIZED_COPPER_GRATE);
    public static final Item COPPER_BULB = registerBlock(Blocks.COPPER_BULB);
    public static final Item EXPOSED_COPPER_BULB = registerBlock(Blocks.EXPOSED_COPPER_BULB);
    public static final Item WEATHERED_COPPER_BULB = registerBlock(Blocks.WEATHERED_COPPER_BULB);
    public static final Item OXIDIZED_COPPER_BULB = registerBlock(Blocks.OXIDIZED_COPPER_BULB);
    public static final Item WAXED_COPPER_BULB = registerBlock(Blocks.WAXED_COPPER_BULB);
    public static final Item WAXED_EXPOSED_COPPER_BULB = registerBlock(Blocks.WAXED_EXPOSED_COPPER_BULB);
    public static final Item WAXED_WEATHERED_COPPER_BULB = registerBlock(Blocks.WAXED_WEATHERED_COPPER_BULB);
    public static final Item WAXED_OXIDIZED_COPPER_BULB = registerBlock(Blocks.WAXED_OXIDIZED_COPPER_BULB);
    public static final Item TRIAL_SPAWNER = registerBlock(Blocks.TRIAL_SPAWNER);
    public static final Item TRIAL_KEY = registerItem("trial_key", new Item(new Item.Properties()));
    public static final Item OMINOUS_TRIAL_KEY = registerItem("ominous_trial_key", new Item(new Item.Properties()));
    public static final Item VAULT = registerBlock(Blocks.VAULT);
    public static final Item OMINOUS_BOTTLE = registerItem(
        "ominous_bottle", new OminousBottleItem(new Item.Properties().food(Foods.OMINOUS_BOTTLE).component(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, 0))
    );
    public static final Item BREEZE_ROD = registerItem("breeze_rod", new Item(new Item.Properties()));

    public static Item registerBlock(Block block) {
        return registerBlock(new BlockItem(block, new Item.Properties()));
    }

    public static Item registerBlock(Block block, UnaryOperator<Item.Properties> propertiesModifier) {
        return registerBlock(new BlockItem(block, propertiesModifier.apply(new Item.Properties())));
    }

    public static Item registerBlock(Block p_block, Block... others) {
        BlockItem blockitem = new BlockItem(p_block, new Item.Properties()) {
            @Override
            public void registerBlocks(java.util.Map<Block, Item> map, Item self) {
                super.registerBlocks(map, self);
                for (Block b : others) {
                    map.put(b, self);
                }
            }

            /** @deprecated Neo: To be removed without replacement since registry replacement is not a feature anymore. */
            @Deprecated(forRemoval = true, since = "1.21.1")
            @Override
            public void removeFromBlockToItemMap(java.util.Map<Block, Item> map, Item self) {
                super.removeFromBlockToItemMap(map, self);
                for (Block b : others) {
                    map.remove(b);
                }
            }
        };

        return registerBlock(blockitem);
    }

    public static Item registerBlock(BlockItem item) {
        return registerBlock(item.getBlock(), item);
    }

    public static Item registerBlock(Block block, Item item) {
        return registerItem(BuiltInRegistries.BLOCK.getKey(block), item);
    }

    public static Item registerItem(String key, Item item) {
        return registerItem(ResourceLocation.withDefaultNamespace(key), item);
    }

    public static Item registerItem(ResourceLocation key, Item item) {
        return registerItem(ResourceKey.create(BuiltInRegistries.ITEM.key(), key), item);
    }

    public static Item registerItem(ResourceKey<Item> key, Item item) {
        if (item instanceof BlockItem) {
            ((BlockItem)item).registerBlocks(Item.BY_BLOCK, item);
        }

        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }
}
