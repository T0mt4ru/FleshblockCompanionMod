package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemStackTheFlatteningFix extends DataFix {
    private static final Map<String, String> MAP = DataFixUtils.make(Maps.newHashMap(), p_16126_ -> {
        p_16126_.put("minecraft:stone.0", "minecraft:stone");
        p_16126_.put("minecraft:stone.1", "minecraft:granite");
        p_16126_.put("minecraft:stone.2", "minecraft:polished_granite");
        p_16126_.put("minecraft:stone.3", "minecraft:diorite");
        p_16126_.put("minecraft:stone.4", "minecraft:polished_diorite");
        p_16126_.put("minecraft:stone.5", "minecraft:andesite");
        p_16126_.put("minecraft:stone.6", "minecraft:polished_andesite");
        p_16126_.put("minecraft:dirt.0", "minecraft:dirt");
        p_16126_.put("minecraft:dirt.1", "minecraft:coarse_dirt");
        p_16126_.put("minecraft:dirt.2", "minecraft:podzol");
        p_16126_.put("minecraft:leaves.0", "minecraft:oak_leaves");
        p_16126_.put("minecraft:leaves.1", "minecraft:spruce_leaves");
        p_16126_.put("minecraft:leaves.2", "minecraft:birch_leaves");
        p_16126_.put("minecraft:leaves.3", "minecraft:jungle_leaves");
        p_16126_.put("minecraft:leaves2.0", "minecraft:acacia_leaves");
        p_16126_.put("minecraft:leaves2.1", "minecraft:dark_oak_leaves");
        p_16126_.put("minecraft:log.0", "minecraft:oak_log");
        p_16126_.put("minecraft:log.1", "minecraft:spruce_log");
        p_16126_.put("minecraft:log.2", "minecraft:birch_log");
        p_16126_.put("minecraft:log.3", "minecraft:jungle_log");
        p_16126_.put("minecraft:log2.0", "minecraft:acacia_log");
        p_16126_.put("minecraft:log2.1", "minecraft:dark_oak_log");
        p_16126_.put("minecraft:sapling.0", "minecraft:oak_sapling");
        p_16126_.put("minecraft:sapling.1", "minecraft:spruce_sapling");
        p_16126_.put("minecraft:sapling.2", "minecraft:birch_sapling");
        p_16126_.put("minecraft:sapling.3", "minecraft:jungle_sapling");
        p_16126_.put("minecraft:sapling.4", "minecraft:acacia_sapling");
        p_16126_.put("minecraft:sapling.5", "minecraft:dark_oak_sapling");
        p_16126_.put("minecraft:planks.0", "minecraft:oak_planks");
        p_16126_.put("minecraft:planks.1", "minecraft:spruce_planks");
        p_16126_.put("minecraft:planks.2", "minecraft:birch_planks");
        p_16126_.put("minecraft:planks.3", "minecraft:jungle_planks");
        p_16126_.put("minecraft:planks.4", "minecraft:acacia_planks");
        p_16126_.put("minecraft:planks.5", "minecraft:dark_oak_planks");
        p_16126_.put("minecraft:sand.0", "minecraft:sand");
        p_16126_.put("minecraft:sand.1", "minecraft:red_sand");
        p_16126_.put("minecraft:quartz_block.0", "minecraft:quartz_block");
        p_16126_.put("minecraft:quartz_block.1", "minecraft:chiseled_quartz_block");
        p_16126_.put("minecraft:quartz_block.2", "minecraft:quartz_pillar");
        p_16126_.put("minecraft:anvil.0", "minecraft:anvil");
        p_16126_.put("minecraft:anvil.1", "minecraft:chipped_anvil");
        p_16126_.put("minecraft:anvil.2", "minecraft:damaged_anvil");
        p_16126_.put("minecraft:wool.0", "minecraft:white_wool");
        p_16126_.put("minecraft:wool.1", "minecraft:orange_wool");
        p_16126_.put("minecraft:wool.2", "minecraft:magenta_wool");
        p_16126_.put("minecraft:wool.3", "minecraft:light_blue_wool");
        p_16126_.put("minecraft:wool.4", "minecraft:yellow_wool");
        p_16126_.put("minecraft:wool.5", "minecraft:lime_wool");
        p_16126_.put("minecraft:wool.6", "minecraft:pink_wool");
        p_16126_.put("minecraft:wool.7", "minecraft:gray_wool");
        p_16126_.put("minecraft:wool.8", "minecraft:light_gray_wool");
        p_16126_.put("minecraft:wool.9", "minecraft:cyan_wool");
        p_16126_.put("minecraft:wool.10", "minecraft:purple_wool");
        p_16126_.put("minecraft:wool.11", "minecraft:blue_wool");
        p_16126_.put("minecraft:wool.12", "minecraft:brown_wool");
        p_16126_.put("minecraft:wool.13", "minecraft:green_wool");
        p_16126_.put("minecraft:wool.14", "minecraft:red_wool");
        p_16126_.put("minecraft:wool.15", "minecraft:black_wool");
        p_16126_.put("minecraft:carpet.0", "minecraft:white_carpet");
        p_16126_.put("minecraft:carpet.1", "minecraft:orange_carpet");
        p_16126_.put("minecraft:carpet.2", "minecraft:magenta_carpet");
        p_16126_.put("minecraft:carpet.3", "minecraft:light_blue_carpet");
        p_16126_.put("minecraft:carpet.4", "minecraft:yellow_carpet");
        p_16126_.put("minecraft:carpet.5", "minecraft:lime_carpet");
        p_16126_.put("minecraft:carpet.6", "minecraft:pink_carpet");
        p_16126_.put("minecraft:carpet.7", "minecraft:gray_carpet");
        p_16126_.put("minecraft:carpet.8", "minecraft:light_gray_carpet");
        p_16126_.put("minecraft:carpet.9", "minecraft:cyan_carpet");
        p_16126_.put("minecraft:carpet.10", "minecraft:purple_carpet");
        p_16126_.put("minecraft:carpet.11", "minecraft:blue_carpet");
        p_16126_.put("minecraft:carpet.12", "minecraft:brown_carpet");
        p_16126_.put("minecraft:carpet.13", "minecraft:green_carpet");
        p_16126_.put("minecraft:carpet.14", "minecraft:red_carpet");
        p_16126_.put("minecraft:carpet.15", "minecraft:black_carpet");
        p_16126_.put("minecraft:hardened_clay.0", "minecraft:terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.0", "minecraft:white_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.1", "minecraft:orange_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.2", "minecraft:magenta_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.3", "minecraft:light_blue_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.4", "minecraft:yellow_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.5", "minecraft:lime_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.6", "minecraft:pink_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.7", "minecraft:gray_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.8", "minecraft:light_gray_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.9", "minecraft:cyan_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.10", "minecraft:purple_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.11", "minecraft:blue_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.12", "minecraft:brown_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.13", "minecraft:green_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.14", "minecraft:red_terracotta");
        p_16126_.put("minecraft:stained_hardened_clay.15", "minecraft:black_terracotta");
        p_16126_.put("minecraft:silver_glazed_terracotta.0", "minecraft:light_gray_glazed_terracotta");
        p_16126_.put("minecraft:stained_glass.0", "minecraft:white_stained_glass");
        p_16126_.put("minecraft:stained_glass.1", "minecraft:orange_stained_glass");
        p_16126_.put("minecraft:stained_glass.2", "minecraft:magenta_stained_glass");
        p_16126_.put("minecraft:stained_glass.3", "minecraft:light_blue_stained_glass");
        p_16126_.put("minecraft:stained_glass.4", "minecraft:yellow_stained_glass");
        p_16126_.put("minecraft:stained_glass.5", "minecraft:lime_stained_glass");
        p_16126_.put("minecraft:stained_glass.6", "minecraft:pink_stained_glass");
        p_16126_.put("minecraft:stained_glass.7", "minecraft:gray_stained_glass");
        p_16126_.put("minecraft:stained_glass.8", "minecraft:light_gray_stained_glass");
        p_16126_.put("minecraft:stained_glass.9", "minecraft:cyan_stained_glass");
        p_16126_.put("minecraft:stained_glass.10", "minecraft:purple_stained_glass");
        p_16126_.put("minecraft:stained_glass.11", "minecraft:blue_stained_glass");
        p_16126_.put("minecraft:stained_glass.12", "minecraft:brown_stained_glass");
        p_16126_.put("minecraft:stained_glass.13", "minecraft:green_stained_glass");
        p_16126_.put("minecraft:stained_glass.14", "minecraft:red_stained_glass");
        p_16126_.put("minecraft:stained_glass.15", "minecraft:black_stained_glass");
        p_16126_.put("minecraft:stained_glass_pane.0", "minecraft:white_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.1", "minecraft:orange_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.2", "minecraft:magenta_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.3", "minecraft:light_blue_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.4", "minecraft:yellow_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.5", "minecraft:lime_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.6", "minecraft:pink_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.7", "minecraft:gray_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.8", "minecraft:light_gray_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.9", "minecraft:cyan_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.10", "minecraft:purple_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.11", "minecraft:blue_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.12", "minecraft:brown_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.13", "minecraft:green_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.14", "minecraft:red_stained_glass_pane");
        p_16126_.put("minecraft:stained_glass_pane.15", "minecraft:black_stained_glass_pane");
        p_16126_.put("minecraft:prismarine.0", "minecraft:prismarine");
        p_16126_.put("minecraft:prismarine.1", "minecraft:prismarine_bricks");
        p_16126_.put("minecraft:prismarine.2", "minecraft:dark_prismarine");
        p_16126_.put("minecraft:concrete.0", "minecraft:white_concrete");
        p_16126_.put("minecraft:concrete.1", "minecraft:orange_concrete");
        p_16126_.put("minecraft:concrete.2", "minecraft:magenta_concrete");
        p_16126_.put("minecraft:concrete.3", "minecraft:light_blue_concrete");
        p_16126_.put("minecraft:concrete.4", "minecraft:yellow_concrete");
        p_16126_.put("minecraft:concrete.5", "minecraft:lime_concrete");
        p_16126_.put("minecraft:concrete.6", "minecraft:pink_concrete");
        p_16126_.put("minecraft:concrete.7", "minecraft:gray_concrete");
        p_16126_.put("minecraft:concrete.8", "minecraft:light_gray_concrete");
        p_16126_.put("minecraft:concrete.9", "minecraft:cyan_concrete");
        p_16126_.put("minecraft:concrete.10", "minecraft:purple_concrete");
        p_16126_.put("minecraft:concrete.11", "minecraft:blue_concrete");
        p_16126_.put("minecraft:concrete.12", "minecraft:brown_concrete");
        p_16126_.put("minecraft:concrete.13", "minecraft:green_concrete");
        p_16126_.put("minecraft:concrete.14", "minecraft:red_concrete");
        p_16126_.put("minecraft:concrete.15", "minecraft:black_concrete");
        p_16126_.put("minecraft:concrete_powder.0", "minecraft:white_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.1", "minecraft:orange_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.2", "minecraft:magenta_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.3", "minecraft:light_blue_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.4", "minecraft:yellow_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.5", "minecraft:lime_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.6", "minecraft:pink_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.7", "minecraft:gray_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.8", "minecraft:light_gray_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.9", "minecraft:cyan_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.10", "minecraft:purple_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.11", "minecraft:blue_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.12", "minecraft:brown_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.13", "minecraft:green_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.14", "minecraft:red_concrete_powder");
        p_16126_.put("minecraft:concrete_powder.15", "minecraft:black_concrete_powder");
        p_16126_.put("minecraft:cobblestone_wall.0", "minecraft:cobblestone_wall");
        p_16126_.put("minecraft:cobblestone_wall.1", "minecraft:mossy_cobblestone_wall");
        p_16126_.put("minecraft:sandstone.0", "minecraft:sandstone");
        p_16126_.put("minecraft:sandstone.1", "minecraft:chiseled_sandstone");
        p_16126_.put("minecraft:sandstone.2", "minecraft:cut_sandstone");
        p_16126_.put("minecraft:red_sandstone.0", "minecraft:red_sandstone");
        p_16126_.put("minecraft:red_sandstone.1", "minecraft:chiseled_red_sandstone");
        p_16126_.put("minecraft:red_sandstone.2", "minecraft:cut_red_sandstone");
        p_16126_.put("minecraft:stonebrick.0", "minecraft:stone_bricks");
        p_16126_.put("minecraft:stonebrick.1", "minecraft:mossy_stone_bricks");
        p_16126_.put("minecraft:stonebrick.2", "minecraft:cracked_stone_bricks");
        p_16126_.put("minecraft:stonebrick.3", "minecraft:chiseled_stone_bricks");
        p_16126_.put("minecraft:monster_egg.0", "minecraft:infested_stone");
        p_16126_.put("minecraft:monster_egg.1", "minecraft:infested_cobblestone");
        p_16126_.put("minecraft:monster_egg.2", "minecraft:infested_stone_bricks");
        p_16126_.put("minecraft:monster_egg.3", "minecraft:infested_mossy_stone_bricks");
        p_16126_.put("minecraft:monster_egg.4", "minecraft:infested_cracked_stone_bricks");
        p_16126_.put("minecraft:monster_egg.5", "minecraft:infested_chiseled_stone_bricks");
        p_16126_.put("minecraft:yellow_flower.0", "minecraft:dandelion");
        p_16126_.put("minecraft:red_flower.0", "minecraft:poppy");
        p_16126_.put("minecraft:red_flower.1", "minecraft:blue_orchid");
        p_16126_.put("minecraft:red_flower.2", "minecraft:allium");
        p_16126_.put("minecraft:red_flower.3", "minecraft:azure_bluet");
        p_16126_.put("minecraft:red_flower.4", "minecraft:red_tulip");
        p_16126_.put("minecraft:red_flower.5", "minecraft:orange_tulip");
        p_16126_.put("minecraft:red_flower.6", "minecraft:white_tulip");
        p_16126_.put("minecraft:red_flower.7", "minecraft:pink_tulip");
        p_16126_.put("minecraft:red_flower.8", "minecraft:oxeye_daisy");
        p_16126_.put("minecraft:double_plant.0", "minecraft:sunflower");
        p_16126_.put("minecraft:double_plant.1", "minecraft:lilac");
        p_16126_.put("minecraft:double_plant.2", "minecraft:tall_grass");
        p_16126_.put("minecraft:double_plant.3", "minecraft:large_fern");
        p_16126_.put("minecraft:double_plant.4", "minecraft:rose_bush");
        p_16126_.put("minecraft:double_plant.5", "minecraft:peony");
        p_16126_.put("minecraft:deadbush.0", "minecraft:dead_bush");
        p_16126_.put("minecraft:tallgrass.0", "minecraft:dead_bush");
        p_16126_.put("minecraft:tallgrass.1", "minecraft:grass");
        p_16126_.put("minecraft:tallgrass.2", "minecraft:fern");
        p_16126_.put("minecraft:sponge.0", "minecraft:sponge");
        p_16126_.put("minecraft:sponge.1", "minecraft:wet_sponge");
        p_16126_.put("minecraft:purpur_slab.0", "minecraft:purpur_slab");
        p_16126_.put("minecraft:stone_slab.0", "minecraft:stone_slab");
        p_16126_.put("minecraft:stone_slab.1", "minecraft:sandstone_slab");
        p_16126_.put("minecraft:stone_slab.2", "minecraft:petrified_oak_slab");
        p_16126_.put("minecraft:stone_slab.3", "minecraft:cobblestone_slab");
        p_16126_.put("minecraft:stone_slab.4", "minecraft:brick_slab");
        p_16126_.put("minecraft:stone_slab.5", "minecraft:stone_brick_slab");
        p_16126_.put("minecraft:stone_slab.6", "minecraft:nether_brick_slab");
        p_16126_.put("minecraft:stone_slab.7", "minecraft:quartz_slab");
        p_16126_.put("minecraft:stone_slab2.0", "minecraft:red_sandstone_slab");
        p_16126_.put("minecraft:wooden_slab.0", "minecraft:oak_slab");
        p_16126_.put("minecraft:wooden_slab.1", "minecraft:spruce_slab");
        p_16126_.put("minecraft:wooden_slab.2", "minecraft:birch_slab");
        p_16126_.put("minecraft:wooden_slab.3", "minecraft:jungle_slab");
        p_16126_.put("minecraft:wooden_slab.4", "minecraft:acacia_slab");
        p_16126_.put("minecraft:wooden_slab.5", "minecraft:dark_oak_slab");
        p_16126_.put("minecraft:coal.0", "minecraft:coal");
        p_16126_.put("minecraft:coal.1", "minecraft:charcoal");
        p_16126_.put("minecraft:fish.0", "minecraft:cod");
        p_16126_.put("minecraft:fish.1", "minecraft:salmon");
        p_16126_.put("minecraft:fish.2", "minecraft:clownfish");
        p_16126_.put("minecraft:fish.3", "minecraft:pufferfish");
        p_16126_.put("minecraft:cooked_fish.0", "minecraft:cooked_cod");
        p_16126_.put("minecraft:cooked_fish.1", "minecraft:cooked_salmon");
        p_16126_.put("minecraft:skull.0", "minecraft:skeleton_skull");
        p_16126_.put("minecraft:skull.1", "minecraft:wither_skeleton_skull");
        p_16126_.put("minecraft:skull.2", "minecraft:zombie_head");
        p_16126_.put("minecraft:skull.3", "minecraft:player_head");
        p_16126_.put("minecraft:skull.4", "minecraft:creeper_head");
        p_16126_.put("minecraft:skull.5", "minecraft:dragon_head");
        p_16126_.put("minecraft:golden_apple.0", "minecraft:golden_apple");
        p_16126_.put("minecraft:golden_apple.1", "minecraft:enchanted_golden_apple");
        p_16126_.put("minecraft:fireworks.0", "minecraft:firework_rocket");
        p_16126_.put("minecraft:firework_charge.0", "minecraft:firework_star");
        p_16126_.put("minecraft:dye.0", "minecraft:ink_sac");
        p_16126_.put("minecraft:dye.1", "minecraft:rose_red");
        p_16126_.put("minecraft:dye.2", "minecraft:cactus_green");
        p_16126_.put("minecraft:dye.3", "minecraft:cocoa_beans");
        p_16126_.put("minecraft:dye.4", "minecraft:lapis_lazuli");
        p_16126_.put("minecraft:dye.5", "minecraft:purple_dye");
        p_16126_.put("minecraft:dye.6", "minecraft:cyan_dye");
        p_16126_.put("minecraft:dye.7", "minecraft:light_gray_dye");
        p_16126_.put("minecraft:dye.8", "minecraft:gray_dye");
        p_16126_.put("minecraft:dye.9", "minecraft:pink_dye");
        p_16126_.put("minecraft:dye.10", "minecraft:lime_dye");
        p_16126_.put("minecraft:dye.11", "minecraft:dandelion_yellow");
        p_16126_.put("minecraft:dye.12", "minecraft:light_blue_dye");
        p_16126_.put("minecraft:dye.13", "minecraft:magenta_dye");
        p_16126_.put("minecraft:dye.14", "minecraft:orange_dye");
        p_16126_.put("minecraft:dye.15", "minecraft:bone_meal");
        p_16126_.put("minecraft:silver_shulker_box.0", "minecraft:light_gray_shulker_box");
        p_16126_.put("minecraft:fence.0", "minecraft:oak_fence");
        p_16126_.put("minecraft:fence_gate.0", "minecraft:oak_fence_gate");
        p_16126_.put("minecraft:wooden_door.0", "minecraft:oak_door");
        p_16126_.put("minecraft:boat.0", "minecraft:oak_boat");
        p_16126_.put("minecraft:lit_pumpkin.0", "minecraft:jack_o_lantern");
        p_16126_.put("minecraft:pumpkin.0", "minecraft:carved_pumpkin");
        p_16126_.put("minecraft:trapdoor.0", "minecraft:oak_trapdoor");
        p_16126_.put("minecraft:nether_brick.0", "minecraft:nether_bricks");
        p_16126_.put("minecraft:red_nether_brick.0", "minecraft:red_nether_bricks");
        p_16126_.put("minecraft:netherbrick.0", "minecraft:nether_brick");
        p_16126_.put("minecraft:wooden_button.0", "minecraft:oak_button");
        p_16126_.put("minecraft:wooden_pressure_plate.0", "minecraft:oak_pressure_plate");
        p_16126_.put("minecraft:noteblock.0", "minecraft:note_block");
        p_16126_.put("minecraft:bed.0", "minecraft:white_bed");
        p_16126_.put("minecraft:bed.1", "minecraft:orange_bed");
        p_16126_.put("minecraft:bed.2", "minecraft:magenta_bed");
        p_16126_.put("minecraft:bed.3", "minecraft:light_blue_bed");
        p_16126_.put("minecraft:bed.4", "minecraft:yellow_bed");
        p_16126_.put("minecraft:bed.5", "minecraft:lime_bed");
        p_16126_.put("minecraft:bed.6", "minecraft:pink_bed");
        p_16126_.put("minecraft:bed.7", "minecraft:gray_bed");
        p_16126_.put("minecraft:bed.8", "minecraft:light_gray_bed");
        p_16126_.put("minecraft:bed.9", "minecraft:cyan_bed");
        p_16126_.put("minecraft:bed.10", "minecraft:purple_bed");
        p_16126_.put("minecraft:bed.11", "minecraft:blue_bed");
        p_16126_.put("minecraft:bed.12", "minecraft:brown_bed");
        p_16126_.put("minecraft:bed.13", "minecraft:green_bed");
        p_16126_.put("minecraft:bed.14", "minecraft:red_bed");
        p_16126_.put("minecraft:bed.15", "minecraft:black_bed");
        p_16126_.put("minecraft:banner.15", "minecraft:white_banner");
        p_16126_.put("minecraft:banner.14", "minecraft:orange_banner");
        p_16126_.put("minecraft:banner.13", "minecraft:magenta_banner");
        p_16126_.put("minecraft:banner.12", "minecraft:light_blue_banner");
        p_16126_.put("minecraft:banner.11", "minecraft:yellow_banner");
        p_16126_.put("minecraft:banner.10", "minecraft:lime_banner");
        p_16126_.put("minecraft:banner.9", "minecraft:pink_banner");
        p_16126_.put("minecraft:banner.8", "minecraft:gray_banner");
        p_16126_.put("minecraft:banner.7", "minecraft:light_gray_banner");
        p_16126_.put("minecraft:banner.6", "minecraft:cyan_banner");
        p_16126_.put("minecraft:banner.5", "minecraft:purple_banner");
        p_16126_.put("minecraft:banner.4", "minecraft:blue_banner");
        p_16126_.put("minecraft:banner.3", "minecraft:brown_banner");
        p_16126_.put("minecraft:banner.2", "minecraft:green_banner");
        p_16126_.put("minecraft:banner.1", "minecraft:red_banner");
        p_16126_.put("minecraft:banner.0", "minecraft:black_banner");
        p_16126_.put("minecraft:grass.0", "minecraft:grass_block");
        p_16126_.put("minecraft:brick_block.0", "minecraft:bricks");
        p_16126_.put("minecraft:end_bricks.0", "minecraft:end_stone_bricks");
        p_16126_.put("minecraft:golden_rail.0", "minecraft:powered_rail");
        p_16126_.put("minecraft:magma.0", "minecraft:magma_block");
        p_16126_.put("minecraft:quartz_ore.0", "minecraft:nether_quartz_ore");
        p_16126_.put("minecraft:reeds.0", "minecraft:sugar_cane");
        p_16126_.put("minecraft:slime.0", "minecraft:slime_block");
        p_16126_.put("minecraft:stone_stairs.0", "minecraft:cobblestone_stairs");
        p_16126_.put("minecraft:waterlily.0", "minecraft:lily_pad");
        p_16126_.put("minecraft:web.0", "minecraft:cobweb");
        p_16126_.put("minecraft:snow.0", "minecraft:snow_block");
        p_16126_.put("minecraft:snow_layer.0", "minecraft:snow");
        p_16126_.put("minecraft:record_11.0", "minecraft:music_disc_11");
        p_16126_.put("minecraft:record_13.0", "minecraft:music_disc_13");
        p_16126_.put("minecraft:record_blocks.0", "minecraft:music_disc_blocks");
        p_16126_.put("minecraft:record_cat.0", "minecraft:music_disc_cat");
        p_16126_.put("minecraft:record_chirp.0", "minecraft:music_disc_chirp");
        p_16126_.put("minecraft:record_far.0", "minecraft:music_disc_far");
        p_16126_.put("minecraft:record_mall.0", "minecraft:music_disc_mall");
        p_16126_.put("minecraft:record_mellohi.0", "minecraft:music_disc_mellohi");
        p_16126_.put("minecraft:record_stal.0", "minecraft:music_disc_stal");
        p_16126_.put("minecraft:record_strad.0", "minecraft:music_disc_strad");
        p_16126_.put("minecraft:record_wait.0", "minecraft:music_disc_wait");
        p_16126_.put("minecraft:record_ward.0", "minecraft:music_disc_ward");
    });
    private static final Set<String> IDS = MAP.keySet().stream().map(p_16121_ -> p_16121_.substring(0, p_16121_.indexOf(46))).collect(Collectors.toSet());
    private static final Set<String> DAMAGE_IDS = Sets.newHashSet(
        "minecraft:bow",
        "minecraft:carrot_on_a_stick",
        "minecraft:chainmail_boots",
        "minecraft:chainmail_chestplate",
        "minecraft:chainmail_helmet",
        "minecraft:chainmail_leggings",
        "minecraft:diamond_axe",
        "minecraft:diamond_boots",
        "minecraft:diamond_chestplate",
        "minecraft:diamond_helmet",
        "minecraft:diamond_hoe",
        "minecraft:diamond_leggings",
        "minecraft:diamond_pickaxe",
        "minecraft:diamond_shovel",
        "minecraft:diamond_sword",
        "minecraft:elytra",
        "minecraft:fishing_rod",
        "minecraft:flint_and_steel",
        "minecraft:golden_axe",
        "minecraft:golden_boots",
        "minecraft:golden_chestplate",
        "minecraft:golden_helmet",
        "minecraft:golden_hoe",
        "minecraft:golden_leggings",
        "minecraft:golden_pickaxe",
        "minecraft:golden_shovel",
        "minecraft:golden_sword",
        "minecraft:iron_axe",
        "minecraft:iron_boots",
        "minecraft:iron_chestplate",
        "minecraft:iron_helmet",
        "minecraft:iron_hoe",
        "minecraft:iron_leggings",
        "minecraft:iron_pickaxe",
        "minecraft:iron_shovel",
        "minecraft:iron_sword",
        "minecraft:leather_boots",
        "minecraft:leather_chestplate",
        "minecraft:leather_helmet",
        "minecraft:leather_leggings",
        "minecraft:shears",
        "minecraft:shield",
        "minecraft:stone_axe",
        "minecraft:stone_hoe",
        "minecraft:stone_pickaxe",
        "minecraft:stone_shovel",
        "minecraft:stone_sword",
        "minecraft:wooden_axe",
        "minecraft:wooden_hoe",
        "minecraft:wooden_pickaxe",
        "minecraft:wooden_shovel",
        "minecraft:wooden_sword"
    );

    public ItemStackTheFlatteningFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> opticfinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<?> opticfinder1 = type.findField("tag");
        return this.fixTypeEverywhereTyped("ItemInstanceTheFlatteningFix", type, p_16119_ -> {
            Optional<Pair<String, String>> optional = p_16119_.getOptional(opticfinder);
            if (optional.isEmpty()) {
                return p_16119_;
            } else {
                Typed<?> typed = p_16119_;
                Dynamic<?> dynamic = p_16119_.get(DSL.remainderFinder());
                int i = dynamic.get("Damage").asInt(0);
                String s = updateItem(optional.get().getSecond(), i);
                if (s != null) {
                    typed = p_16119_.set(opticfinder, Pair.of(References.ITEM_NAME.typeName(), s));
                }

                if (DAMAGE_IDS.contains(optional.get().getSecond())) {
                    Typed<?> typed1 = p_16119_.getOrCreateTyped(opticfinder1);
                    Dynamic<?> dynamic1 = typed1.get(DSL.remainderFinder());
                    dynamic1 = dynamic1.set("Damage", dynamic1.createInt(i));
                    typed = typed.set(opticfinder1, typed1.set(DSL.remainderFinder(), dynamic1));
                }

                return typed.set(DSL.remainderFinder(), dynamic.remove("Damage"));
            }
        });
    }

    @Nullable
    public static String updateItem(@Nullable String item, int dataValue) {
        if (IDS.contains(item)) {
            String s = MAP.get(item + "." + dataValue);
            return s == null ? MAP.get(item + ".0") : s;
        } else {
            return null;
        }
    }
}
