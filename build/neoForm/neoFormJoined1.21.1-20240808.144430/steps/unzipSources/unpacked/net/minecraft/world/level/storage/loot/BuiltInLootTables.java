package net.minecraft.world.level.storage.loot;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Stores IDs for built in loot tables, i.e. loot tables which are not based directly on a block or entity ID.
 */
public class BuiltInLootTables {
    private static final Set<ResourceKey<LootTable>> LOCATIONS = new HashSet<>();
    private static final Set<ResourceKey<LootTable>> IMMUTABLE_LOCATIONS = Collections.unmodifiableSet(LOCATIONS);
    public static final ResourceKey<LootTable> EMPTY = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("empty"));
    public static final ResourceKey<LootTable> SPAWN_BONUS_CHEST = register("chests/spawn_bonus_chest");
    public static final ResourceKey<LootTable> END_CITY_TREASURE = register("chests/end_city_treasure");
    public static final ResourceKey<LootTable> SIMPLE_DUNGEON = register("chests/simple_dungeon");
    public static final ResourceKey<LootTable> VILLAGE_WEAPONSMITH = register("chests/village/village_weaponsmith");
    public static final ResourceKey<LootTable> VILLAGE_TOOLSMITH = register("chests/village/village_toolsmith");
    public static final ResourceKey<LootTable> VILLAGE_ARMORER = register("chests/village/village_armorer");
    public static final ResourceKey<LootTable> VILLAGE_CARTOGRAPHER = register("chests/village/village_cartographer");
    public static final ResourceKey<LootTable> VILLAGE_MASON = register("chests/village/village_mason");
    public static final ResourceKey<LootTable> VILLAGE_SHEPHERD = register("chests/village/village_shepherd");
    public static final ResourceKey<LootTable> VILLAGE_BUTCHER = register("chests/village/village_butcher");
    public static final ResourceKey<LootTable> VILLAGE_FLETCHER = register("chests/village/village_fletcher");
    public static final ResourceKey<LootTable> VILLAGE_FISHER = register("chests/village/village_fisher");
    public static final ResourceKey<LootTable> VILLAGE_TANNERY = register("chests/village/village_tannery");
    public static final ResourceKey<LootTable> VILLAGE_TEMPLE = register("chests/village/village_temple");
    public static final ResourceKey<LootTable> VILLAGE_DESERT_HOUSE = register("chests/village/village_desert_house");
    public static final ResourceKey<LootTable> VILLAGE_PLAINS_HOUSE = register("chests/village/village_plains_house");
    public static final ResourceKey<LootTable> VILLAGE_TAIGA_HOUSE = register("chests/village/village_taiga_house");
    public static final ResourceKey<LootTable> VILLAGE_SNOWY_HOUSE = register("chests/village/village_snowy_house");
    public static final ResourceKey<LootTable> VILLAGE_SAVANNA_HOUSE = register("chests/village/village_savanna_house");
    public static final ResourceKey<LootTable> ABANDONED_MINESHAFT = register("chests/abandoned_mineshaft");
    public static final ResourceKey<LootTable> NETHER_BRIDGE = register("chests/nether_bridge");
    public static final ResourceKey<LootTable> STRONGHOLD_LIBRARY = register("chests/stronghold_library");
    public static final ResourceKey<LootTable> STRONGHOLD_CROSSING = register("chests/stronghold_crossing");
    public static final ResourceKey<LootTable> STRONGHOLD_CORRIDOR = register("chests/stronghold_corridor");
    public static final ResourceKey<LootTable> DESERT_PYRAMID = register("chests/desert_pyramid");
    public static final ResourceKey<LootTable> JUNGLE_TEMPLE = register("chests/jungle_temple");
    public static final ResourceKey<LootTable> JUNGLE_TEMPLE_DISPENSER = register("chests/jungle_temple_dispenser");
    public static final ResourceKey<LootTable> IGLOO_CHEST = register("chests/igloo_chest");
    public static final ResourceKey<LootTable> WOODLAND_MANSION = register("chests/woodland_mansion");
    public static final ResourceKey<LootTable> UNDERWATER_RUIN_SMALL = register("chests/underwater_ruin_small");
    public static final ResourceKey<LootTable> UNDERWATER_RUIN_BIG = register("chests/underwater_ruin_big");
    public static final ResourceKey<LootTable> BURIED_TREASURE = register("chests/buried_treasure");
    public static final ResourceKey<LootTable> SHIPWRECK_MAP = register("chests/shipwreck_map");
    public static final ResourceKey<LootTable> SHIPWRECK_SUPPLY = register("chests/shipwreck_supply");
    public static final ResourceKey<LootTable> SHIPWRECK_TREASURE = register("chests/shipwreck_treasure");
    public static final ResourceKey<LootTable> PILLAGER_OUTPOST = register("chests/pillager_outpost");
    public static final ResourceKey<LootTable> BASTION_TREASURE = register("chests/bastion_treasure");
    public static final ResourceKey<LootTable> BASTION_OTHER = register("chests/bastion_other");
    public static final ResourceKey<LootTable> BASTION_BRIDGE = register("chests/bastion_bridge");
    public static final ResourceKey<LootTable> BASTION_HOGLIN_STABLE = register("chests/bastion_hoglin_stable");
    public static final ResourceKey<LootTable> ANCIENT_CITY = register("chests/ancient_city");
    public static final ResourceKey<LootTable> ANCIENT_CITY_ICE_BOX = register("chests/ancient_city_ice_box");
    public static final ResourceKey<LootTable> RUINED_PORTAL = register("chests/ruined_portal");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_REWARD = register("chests/trial_chambers/reward");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_REWARD_COMMON = register("chests/trial_chambers/reward_common");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_REWARD_RARE = register("chests/trial_chambers/reward_rare");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_REWARD_UNIQUE = register("chests/trial_chambers/reward_unique");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_REWARD_OMINOUS = register("chests/trial_chambers/reward_ominous");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_REWARD_OMINOUS_COMMON = register("chests/trial_chambers/reward_ominous_common");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_REWARD_OMINOUS_RARE = register("chests/trial_chambers/reward_ominous_rare");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE = register("chests/trial_chambers/reward_ominous_unique");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_SUPPLY = register("chests/trial_chambers/supply");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_CORRIDOR = register("chests/trial_chambers/corridor");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_INTERSECTION = register("chests/trial_chambers/intersection");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_INTERSECTION_BARREL = register("chests/trial_chambers/intersection_barrel");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_ENTRANCE = register("chests/trial_chambers/entrance");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_CORRIDOR_DISPENSER = register("dispensers/trial_chambers/corridor");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_CHAMBER_DISPENSER = register("dispensers/trial_chambers/chamber");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_WATER_DISPENSER = register("dispensers/trial_chambers/water");
    public static final ResourceKey<LootTable> TRIAL_CHAMBERS_CORRIDOR_POT = register("pots/trial_chambers/corridor");
    public static final ResourceKey<LootTable> EQUIPMENT_TRIAL_CHAMBER = register("equipment/trial_chamber");
    public static final ResourceKey<LootTable> EQUIPMENT_TRIAL_CHAMBER_RANGED = register("equipment/trial_chamber_ranged");
    public static final ResourceKey<LootTable> EQUIPMENT_TRIAL_CHAMBER_MELEE = register("equipment/trial_chamber_melee");
    public static final ResourceKey<LootTable> SHEEP_WHITE = register("entities/sheep/white");
    public static final ResourceKey<LootTable> SHEEP_ORANGE = register("entities/sheep/orange");
    public static final ResourceKey<LootTable> SHEEP_MAGENTA = register("entities/sheep/magenta");
    public static final ResourceKey<LootTable> SHEEP_LIGHT_BLUE = register("entities/sheep/light_blue");
    public static final ResourceKey<LootTable> SHEEP_YELLOW = register("entities/sheep/yellow");
    public static final ResourceKey<LootTable> SHEEP_LIME = register("entities/sheep/lime");
    public static final ResourceKey<LootTable> SHEEP_PINK = register("entities/sheep/pink");
    public static final ResourceKey<LootTable> SHEEP_GRAY = register("entities/sheep/gray");
    public static final ResourceKey<LootTable> SHEEP_LIGHT_GRAY = register("entities/sheep/light_gray");
    public static final ResourceKey<LootTable> SHEEP_CYAN = register("entities/sheep/cyan");
    public static final ResourceKey<LootTable> SHEEP_PURPLE = register("entities/sheep/purple");
    public static final ResourceKey<LootTable> SHEEP_BLUE = register("entities/sheep/blue");
    public static final ResourceKey<LootTable> SHEEP_BROWN = register("entities/sheep/brown");
    public static final ResourceKey<LootTable> SHEEP_GREEN = register("entities/sheep/green");
    public static final ResourceKey<LootTable> SHEEP_RED = register("entities/sheep/red");
    public static final ResourceKey<LootTable> SHEEP_BLACK = register("entities/sheep/black");
    public static final ResourceKey<LootTable> FISHING = register("gameplay/fishing");
    public static final ResourceKey<LootTable> FISHING_JUNK = register("gameplay/fishing/junk");
    public static final ResourceKey<LootTable> FISHING_TREASURE = register("gameplay/fishing/treasure");
    public static final ResourceKey<LootTable> FISHING_FISH = register("gameplay/fishing/fish");
    public static final ResourceKey<LootTable> CAT_MORNING_GIFT = register("gameplay/cat_morning_gift");
    public static final ResourceKey<LootTable> ARMORER_GIFT = register("gameplay/hero_of_the_village/armorer_gift");
    public static final ResourceKey<LootTable> BUTCHER_GIFT = register("gameplay/hero_of_the_village/butcher_gift");
    public static final ResourceKey<LootTable> CARTOGRAPHER_GIFT = register("gameplay/hero_of_the_village/cartographer_gift");
    public static final ResourceKey<LootTable> CLERIC_GIFT = register("gameplay/hero_of_the_village/cleric_gift");
    public static final ResourceKey<LootTable> FARMER_GIFT = register("gameplay/hero_of_the_village/farmer_gift");
    public static final ResourceKey<LootTable> FISHERMAN_GIFT = register("gameplay/hero_of_the_village/fisherman_gift");
    public static final ResourceKey<LootTable> FLETCHER_GIFT = register("gameplay/hero_of_the_village/fletcher_gift");
    public static final ResourceKey<LootTable> LEATHERWORKER_GIFT = register("gameplay/hero_of_the_village/leatherworker_gift");
    public static final ResourceKey<LootTable> LIBRARIAN_GIFT = register("gameplay/hero_of_the_village/librarian_gift");
    public static final ResourceKey<LootTable> MASON_GIFT = register("gameplay/hero_of_the_village/mason_gift");
    public static final ResourceKey<LootTable> SHEPHERD_GIFT = register("gameplay/hero_of_the_village/shepherd_gift");
    public static final ResourceKey<LootTable> TOOLSMITH_GIFT = register("gameplay/hero_of_the_village/toolsmith_gift");
    public static final ResourceKey<LootTable> WEAPONSMITH_GIFT = register("gameplay/hero_of_the_village/weaponsmith_gift");
    public static final ResourceKey<LootTable> SNIFFER_DIGGING = register("gameplay/sniffer_digging");
    public static final ResourceKey<LootTable> PANDA_SNEEZE = register("gameplay/panda_sneeze");
    public static final ResourceKey<LootTable> PIGLIN_BARTERING = register("gameplay/piglin_bartering");
    public static final ResourceKey<LootTable> SPAWNER_TRIAL_CHAMBER_KEY = register("spawners/trial_chamber/key");
    public static final ResourceKey<LootTable> SPAWNER_TRIAL_CHAMBER_CONSUMABLES = register("spawners/trial_chamber/consumables");
    public static final ResourceKey<LootTable> SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY = register("spawners/ominous/trial_chamber/key");
    public static final ResourceKey<LootTable> SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES = register("spawners/ominous/trial_chamber/consumables");
    public static final ResourceKey<LootTable> SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS = register("spawners/trial_chamber/items_to_drop_when_ominous");
    public static final ResourceKey<LootTable> BOGGED_SHEAR = register("shearing/bogged");
    public static final ResourceKey<LootTable> DESERT_WELL_ARCHAEOLOGY = register("archaeology/desert_well");
    public static final ResourceKey<LootTable> DESERT_PYRAMID_ARCHAEOLOGY = register("archaeology/desert_pyramid");
    public static final ResourceKey<LootTable> TRAIL_RUINS_ARCHAEOLOGY_COMMON = register("archaeology/trail_ruins_common");
    public static final ResourceKey<LootTable> TRAIL_RUINS_ARCHAEOLOGY_RARE = register("archaeology/trail_ruins_rare");
    public static final ResourceKey<LootTable> OCEAN_RUIN_WARM_ARCHAEOLOGY = register("archaeology/ocean_ruin_warm");
    public static final ResourceKey<LootTable> OCEAN_RUIN_COLD_ARCHAEOLOGY = register("archaeology/ocean_ruin_cold");

    private static ResourceKey<LootTable> register(String name) {
        return register(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace(name)));
    }

    private static ResourceKey<LootTable> register(ResourceKey<LootTable> name) {
        if (LOCATIONS.add(name)) {
            return name;
        } else {
            throw new IllegalArgumentException(name.location() + " is already a registered built-in loot table");
        }
    }

    public static Set<ResourceKey<LootTable>> all() {
        return IMMUTABLE_LOCATIONS;
    }
}
