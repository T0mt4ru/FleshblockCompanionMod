package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityBlockStateFix extends DataFix {
    private static final Map<String, Integer> MAP = DataFixUtils.make(Maps.newHashMap(), p_15368_ -> {
        p_15368_.put("minecraft:air", 0);
        p_15368_.put("minecraft:stone", 1);
        p_15368_.put("minecraft:grass", 2);
        p_15368_.put("minecraft:dirt", 3);
        p_15368_.put("minecraft:cobblestone", 4);
        p_15368_.put("minecraft:planks", 5);
        p_15368_.put("minecraft:sapling", 6);
        p_15368_.put("minecraft:bedrock", 7);
        p_15368_.put("minecraft:flowing_water", 8);
        p_15368_.put("minecraft:water", 9);
        p_15368_.put("minecraft:flowing_lava", 10);
        p_15368_.put("minecraft:lava", 11);
        p_15368_.put("minecraft:sand", 12);
        p_15368_.put("minecraft:gravel", 13);
        p_15368_.put("minecraft:gold_ore", 14);
        p_15368_.put("minecraft:iron_ore", 15);
        p_15368_.put("minecraft:coal_ore", 16);
        p_15368_.put("minecraft:log", 17);
        p_15368_.put("minecraft:leaves", 18);
        p_15368_.put("minecraft:sponge", 19);
        p_15368_.put("minecraft:glass", 20);
        p_15368_.put("minecraft:lapis_ore", 21);
        p_15368_.put("minecraft:lapis_block", 22);
        p_15368_.put("minecraft:dispenser", 23);
        p_15368_.put("minecraft:sandstone", 24);
        p_15368_.put("minecraft:noteblock", 25);
        p_15368_.put("minecraft:bed", 26);
        p_15368_.put("minecraft:golden_rail", 27);
        p_15368_.put("minecraft:detector_rail", 28);
        p_15368_.put("minecraft:sticky_piston", 29);
        p_15368_.put("minecraft:web", 30);
        p_15368_.put("minecraft:tallgrass", 31);
        p_15368_.put("minecraft:deadbush", 32);
        p_15368_.put("minecraft:piston", 33);
        p_15368_.put("minecraft:piston_head", 34);
        p_15368_.put("minecraft:wool", 35);
        p_15368_.put("minecraft:piston_extension", 36);
        p_15368_.put("minecraft:yellow_flower", 37);
        p_15368_.put("minecraft:red_flower", 38);
        p_15368_.put("minecraft:brown_mushroom", 39);
        p_15368_.put("minecraft:red_mushroom", 40);
        p_15368_.put("minecraft:gold_block", 41);
        p_15368_.put("minecraft:iron_block", 42);
        p_15368_.put("minecraft:double_stone_slab", 43);
        p_15368_.put("minecraft:stone_slab", 44);
        p_15368_.put("minecraft:brick_block", 45);
        p_15368_.put("minecraft:tnt", 46);
        p_15368_.put("minecraft:bookshelf", 47);
        p_15368_.put("minecraft:mossy_cobblestone", 48);
        p_15368_.put("minecraft:obsidian", 49);
        p_15368_.put("minecraft:torch", 50);
        p_15368_.put("minecraft:fire", 51);
        p_15368_.put("minecraft:mob_spawner", 52);
        p_15368_.put("minecraft:oak_stairs", 53);
        p_15368_.put("minecraft:chest", 54);
        p_15368_.put("minecraft:redstone_wire", 55);
        p_15368_.put("minecraft:diamond_ore", 56);
        p_15368_.put("minecraft:diamond_block", 57);
        p_15368_.put("minecraft:crafting_table", 58);
        p_15368_.put("minecraft:wheat", 59);
        p_15368_.put("minecraft:farmland", 60);
        p_15368_.put("minecraft:furnace", 61);
        p_15368_.put("minecraft:lit_furnace", 62);
        p_15368_.put("minecraft:standing_sign", 63);
        p_15368_.put("minecraft:wooden_door", 64);
        p_15368_.put("minecraft:ladder", 65);
        p_15368_.put("minecraft:rail", 66);
        p_15368_.put("minecraft:stone_stairs", 67);
        p_15368_.put("minecraft:wall_sign", 68);
        p_15368_.put("minecraft:lever", 69);
        p_15368_.put("minecraft:stone_pressure_plate", 70);
        p_15368_.put("minecraft:iron_door", 71);
        p_15368_.put("minecraft:wooden_pressure_plate", 72);
        p_15368_.put("minecraft:redstone_ore", 73);
        p_15368_.put("minecraft:lit_redstone_ore", 74);
        p_15368_.put("minecraft:unlit_redstone_torch", 75);
        p_15368_.put("minecraft:redstone_torch", 76);
        p_15368_.put("minecraft:stone_button", 77);
        p_15368_.put("minecraft:snow_layer", 78);
        p_15368_.put("minecraft:ice", 79);
        p_15368_.put("minecraft:snow", 80);
        p_15368_.put("minecraft:cactus", 81);
        p_15368_.put("minecraft:clay", 82);
        p_15368_.put("minecraft:reeds", 83);
        p_15368_.put("minecraft:jukebox", 84);
        p_15368_.put("minecraft:fence", 85);
        p_15368_.put("minecraft:pumpkin", 86);
        p_15368_.put("minecraft:netherrack", 87);
        p_15368_.put("minecraft:soul_sand", 88);
        p_15368_.put("minecraft:glowstone", 89);
        p_15368_.put("minecraft:portal", 90);
        p_15368_.put("minecraft:lit_pumpkin", 91);
        p_15368_.put("minecraft:cake", 92);
        p_15368_.put("minecraft:unpowered_repeater", 93);
        p_15368_.put("minecraft:powered_repeater", 94);
        p_15368_.put("minecraft:stained_glass", 95);
        p_15368_.put("minecraft:trapdoor", 96);
        p_15368_.put("minecraft:monster_egg", 97);
        p_15368_.put("minecraft:stonebrick", 98);
        p_15368_.put("minecraft:brown_mushroom_block", 99);
        p_15368_.put("minecraft:red_mushroom_block", 100);
        p_15368_.put("minecraft:iron_bars", 101);
        p_15368_.put("minecraft:glass_pane", 102);
        p_15368_.put("minecraft:melon_block", 103);
        p_15368_.put("minecraft:pumpkin_stem", 104);
        p_15368_.put("minecraft:melon_stem", 105);
        p_15368_.put("minecraft:vine", 106);
        p_15368_.put("minecraft:fence_gate", 107);
        p_15368_.put("minecraft:brick_stairs", 108);
        p_15368_.put("minecraft:stone_brick_stairs", 109);
        p_15368_.put("minecraft:mycelium", 110);
        p_15368_.put("minecraft:waterlily", 111);
        p_15368_.put("minecraft:nether_brick", 112);
        p_15368_.put("minecraft:nether_brick_fence", 113);
        p_15368_.put("minecraft:nether_brick_stairs", 114);
        p_15368_.put("minecraft:nether_wart", 115);
        p_15368_.put("minecraft:enchanting_table", 116);
        p_15368_.put("minecraft:brewing_stand", 117);
        p_15368_.put("minecraft:cauldron", 118);
        p_15368_.put("minecraft:end_portal", 119);
        p_15368_.put("minecraft:end_portal_frame", 120);
        p_15368_.put("minecraft:end_stone", 121);
        p_15368_.put("minecraft:dragon_egg", 122);
        p_15368_.put("minecraft:redstone_lamp", 123);
        p_15368_.put("minecraft:lit_redstone_lamp", 124);
        p_15368_.put("minecraft:double_wooden_slab", 125);
        p_15368_.put("minecraft:wooden_slab", 126);
        p_15368_.put("minecraft:cocoa", 127);
        p_15368_.put("minecraft:sandstone_stairs", 128);
        p_15368_.put("minecraft:emerald_ore", 129);
        p_15368_.put("minecraft:ender_chest", 130);
        p_15368_.put("minecraft:tripwire_hook", 131);
        p_15368_.put("minecraft:tripwire", 132);
        p_15368_.put("minecraft:emerald_block", 133);
        p_15368_.put("minecraft:spruce_stairs", 134);
        p_15368_.put("minecraft:birch_stairs", 135);
        p_15368_.put("minecraft:jungle_stairs", 136);
        p_15368_.put("minecraft:command_block", 137);
        p_15368_.put("minecraft:beacon", 138);
        p_15368_.put("minecraft:cobblestone_wall", 139);
        p_15368_.put("minecraft:flower_pot", 140);
        p_15368_.put("minecraft:carrots", 141);
        p_15368_.put("minecraft:potatoes", 142);
        p_15368_.put("minecraft:wooden_button", 143);
        p_15368_.put("minecraft:skull", 144);
        p_15368_.put("minecraft:anvil", 145);
        p_15368_.put("minecraft:trapped_chest", 146);
        p_15368_.put("minecraft:light_weighted_pressure_plate", 147);
        p_15368_.put("minecraft:heavy_weighted_pressure_plate", 148);
        p_15368_.put("minecraft:unpowered_comparator", 149);
        p_15368_.put("minecraft:powered_comparator", 150);
        p_15368_.put("minecraft:daylight_detector", 151);
        p_15368_.put("minecraft:redstone_block", 152);
        p_15368_.put("minecraft:quartz_ore", 153);
        p_15368_.put("minecraft:hopper", 154);
        p_15368_.put("minecraft:quartz_block", 155);
        p_15368_.put("minecraft:quartz_stairs", 156);
        p_15368_.put("minecraft:activator_rail", 157);
        p_15368_.put("minecraft:dropper", 158);
        p_15368_.put("minecraft:stained_hardened_clay", 159);
        p_15368_.put("minecraft:stained_glass_pane", 160);
        p_15368_.put("minecraft:leaves2", 161);
        p_15368_.put("minecraft:log2", 162);
        p_15368_.put("minecraft:acacia_stairs", 163);
        p_15368_.put("minecraft:dark_oak_stairs", 164);
        p_15368_.put("minecraft:slime", 165);
        p_15368_.put("minecraft:barrier", 166);
        p_15368_.put("minecraft:iron_trapdoor", 167);
        p_15368_.put("minecraft:prismarine", 168);
        p_15368_.put("minecraft:sea_lantern", 169);
        p_15368_.put("minecraft:hay_block", 170);
        p_15368_.put("minecraft:carpet", 171);
        p_15368_.put("minecraft:hardened_clay", 172);
        p_15368_.put("minecraft:coal_block", 173);
        p_15368_.put("minecraft:packed_ice", 174);
        p_15368_.put("minecraft:double_plant", 175);
        p_15368_.put("minecraft:standing_banner", 176);
        p_15368_.put("minecraft:wall_banner", 177);
        p_15368_.put("minecraft:daylight_detector_inverted", 178);
        p_15368_.put("minecraft:red_sandstone", 179);
        p_15368_.put("minecraft:red_sandstone_stairs", 180);
        p_15368_.put("minecraft:double_stone_slab2", 181);
        p_15368_.put("minecraft:stone_slab2", 182);
        p_15368_.put("minecraft:spruce_fence_gate", 183);
        p_15368_.put("minecraft:birch_fence_gate", 184);
        p_15368_.put("minecraft:jungle_fence_gate", 185);
        p_15368_.put("minecraft:dark_oak_fence_gate", 186);
        p_15368_.put("minecraft:acacia_fence_gate", 187);
        p_15368_.put("minecraft:spruce_fence", 188);
        p_15368_.put("minecraft:birch_fence", 189);
        p_15368_.put("minecraft:jungle_fence", 190);
        p_15368_.put("minecraft:dark_oak_fence", 191);
        p_15368_.put("minecraft:acacia_fence", 192);
        p_15368_.put("minecraft:spruce_door", 193);
        p_15368_.put("minecraft:birch_door", 194);
        p_15368_.put("minecraft:jungle_door", 195);
        p_15368_.put("minecraft:acacia_door", 196);
        p_15368_.put("minecraft:dark_oak_door", 197);
        p_15368_.put("minecraft:end_rod", 198);
        p_15368_.put("minecraft:chorus_plant", 199);
        p_15368_.put("minecraft:chorus_flower", 200);
        p_15368_.put("minecraft:purpur_block", 201);
        p_15368_.put("minecraft:purpur_pillar", 202);
        p_15368_.put("minecraft:purpur_stairs", 203);
        p_15368_.put("minecraft:purpur_double_slab", 204);
        p_15368_.put("minecraft:purpur_slab", 205);
        p_15368_.put("minecraft:end_bricks", 206);
        p_15368_.put("minecraft:beetroots", 207);
        p_15368_.put("minecraft:grass_path", 208);
        p_15368_.put("minecraft:end_gateway", 209);
        p_15368_.put("minecraft:repeating_command_block", 210);
        p_15368_.put("minecraft:chain_command_block", 211);
        p_15368_.put("minecraft:frosted_ice", 212);
        p_15368_.put("minecraft:magma", 213);
        p_15368_.put("minecraft:nether_wart_block", 214);
        p_15368_.put("minecraft:red_nether_brick", 215);
        p_15368_.put("minecraft:bone_block", 216);
        p_15368_.put("minecraft:structure_void", 217);
        p_15368_.put("minecraft:observer", 218);
        p_15368_.put("minecraft:white_shulker_box", 219);
        p_15368_.put("minecraft:orange_shulker_box", 220);
        p_15368_.put("minecraft:magenta_shulker_box", 221);
        p_15368_.put("minecraft:light_blue_shulker_box", 222);
        p_15368_.put("minecraft:yellow_shulker_box", 223);
        p_15368_.put("minecraft:lime_shulker_box", 224);
        p_15368_.put("minecraft:pink_shulker_box", 225);
        p_15368_.put("minecraft:gray_shulker_box", 226);
        p_15368_.put("minecraft:silver_shulker_box", 227);
        p_15368_.put("minecraft:cyan_shulker_box", 228);
        p_15368_.put("minecraft:purple_shulker_box", 229);
        p_15368_.put("minecraft:blue_shulker_box", 230);
        p_15368_.put("minecraft:brown_shulker_box", 231);
        p_15368_.put("minecraft:green_shulker_box", 232);
        p_15368_.put("minecraft:red_shulker_box", 233);
        p_15368_.put("minecraft:black_shulker_box", 234);
        p_15368_.put("minecraft:white_glazed_terracotta", 235);
        p_15368_.put("minecraft:orange_glazed_terracotta", 236);
        p_15368_.put("minecraft:magenta_glazed_terracotta", 237);
        p_15368_.put("minecraft:light_blue_glazed_terracotta", 238);
        p_15368_.put("minecraft:yellow_glazed_terracotta", 239);
        p_15368_.put("minecraft:lime_glazed_terracotta", 240);
        p_15368_.put("minecraft:pink_glazed_terracotta", 241);
        p_15368_.put("minecraft:gray_glazed_terracotta", 242);
        p_15368_.put("minecraft:silver_glazed_terracotta", 243);
        p_15368_.put("minecraft:cyan_glazed_terracotta", 244);
        p_15368_.put("minecraft:purple_glazed_terracotta", 245);
        p_15368_.put("minecraft:blue_glazed_terracotta", 246);
        p_15368_.put("minecraft:brown_glazed_terracotta", 247);
        p_15368_.put("minecraft:green_glazed_terracotta", 248);
        p_15368_.put("minecraft:red_glazed_terracotta", 249);
        p_15368_.put("minecraft:black_glazed_terracotta", 250);
        p_15368_.put("minecraft:concrete", 251);
        p_15368_.put("minecraft:concrete_powder", 252);
        p_15368_.put("minecraft:structure_block", 255);
    });

    public EntityBlockStateFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    public static int getBlockId(String name) {
        Integer integer = MAP.get(name);
        return integer == null ? 0 : integer;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        Schema schema1 = this.getOutputSchema();
        Function<Typed<?>, Typed<?>> function = p_15381_ -> this.updateBlockToBlockState(p_15381_, "DisplayTile", "DisplayData", "DisplayState");
        Function<Typed<?>, Typed<?>> function1 = p_15379_ -> this.updateBlockToBlockState(p_15379_, "inTile", "inData", "inBlockState");
        Type<Pair<Either<Pair<String, Either<Integer, String>>, Unit>, Dynamic<?>>> type = DSL.and(
            DSL.optional(DSL.field("inTile", DSL.named(References.BLOCK_NAME.typeName(), DSL.or(DSL.intType(), NamespacedSchema.namespacedString())))),
            DSL.remainderType()
        );
        Function<Typed<?>, Typed<?>> function2 = p_15348_ -> p_15348_.update(type.finder(), DSL.remainderType(), Pair::getSecond);
        return this.fixTypeEverywhereTyped(
            "EntityBlockStateFix",
            schema.getType(References.ENTITY),
            schema1.getType(References.ENTITY),
            p_15373_ -> {
                p_15373_ = this.updateEntity(p_15373_, "minecraft:falling_block", this::updateFallingBlock);
                p_15373_ = this.updateEntity(
                    p_15373_, "minecraft:enderman", p_145271_ -> this.updateBlockToBlockState(p_145271_, "carried", "carriedData", "carriedBlockState")
                );
                p_15373_ = this.updateEntity(p_15373_, "minecraft:arrow", function1);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:spectral_arrow", function1);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:egg", function2);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:ender_pearl", function2);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:fireball", function2);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:potion", function2);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:small_fireball", function2);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:snowball", function2);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:wither_skull", function2);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:xp_bottle", function2);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:commandblock_minecart", function);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:minecart", function);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:chest_minecart", function);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:furnace_minecart", function);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:tnt_minecart", function);
                p_15373_ = this.updateEntity(p_15373_, "minecraft:hopper_minecart", function);
                return this.updateEntity(p_15373_, "minecraft:spawner_minecart", function);
            }
        );
    }

    private Typed<?> updateFallingBlock(Typed<?> typed) {
        Type<Either<Pair<String, Either<Integer, String>>, Unit>> type = DSL.optional(
            DSL.field("Block", DSL.named(References.BLOCK_NAME.typeName(), DSL.or(DSL.intType(), NamespacedSchema.namespacedString())))
        );
        Type<Either<Pair<String, Dynamic<?>>, Unit>> type1 = DSL.optional(
            DSL.field("BlockState", DSL.named(References.BLOCK_STATE.typeName(), DSL.remainderType()))
        );
        Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
        return typed.update(type.finder(), type1, p_15355_ -> {
            int i = p_15355_.map(p_145262_ -> p_145262_.getSecond().map(p_145273_ -> (Integer)p_145273_, EntityBlockStateFix::getBlockId), p_337617_ -> {
                Optional<Number> optional = dynamic.get("TileID").asNumber().result();
                return optional.map(Number::intValue).orElseGet(() -> dynamic.get("Tile").asByte((byte)0) & 0xFF);
            });
            int j = dynamic.get("Data").asInt(0) & 15;
            return Either.left(Pair.of(References.BLOCK_STATE.typeName(), BlockStateData.getTag(i << 4 | j)));
        }).set(DSL.remainderFinder(), dynamic.remove("Data").remove("TileID").remove("Tile"));
    }

    private Typed<?> updateBlockToBlockState(Typed<?> typed, String tileKey, String dataKey, String outputKey) {
        Type<Pair<String, Either<Integer, String>>> type = DSL.field(
            tileKey, DSL.named(References.BLOCK_NAME.typeName(), DSL.or(DSL.intType(), NamespacedSchema.namespacedString()))
        );
        Type<Pair<String, Dynamic<?>>> type1 = DSL.field(outputKey, DSL.named(References.BLOCK_STATE.typeName(), DSL.remainderType()));
        Dynamic<?> dynamic = typed.getOrCreate(DSL.remainderFinder());
        return typed.update(type.finder(), type1, p_15362_ -> {
            int i = p_15362_.getSecond().map(p_145269_ -> (Integer)p_145269_, EntityBlockStateFix::getBlockId);
            int j = dynamic.get(dataKey).asInt(0) & 15;
            return Pair.of(References.BLOCK_STATE.typeName(), BlockStateData.getTag(i << 4 | j));
        }).set(DSL.remainderFinder(), dynamic.remove(dataKey));
    }

    private Typed<?> updateEntity(Typed<?> typed, String choiceName, Function<Typed<?>, Typed<?>> updater) {
        Type<?> type = this.getInputSchema().getChoiceType(References.ENTITY, choiceName);
        Type<?> type1 = this.getOutputSchema().getChoiceType(References.ENTITY, choiceName);
        return typed.updateTyped(DSL.namedChoice(choiceName, type), type1, updater);
    }
}
