package net.minecraft.world.entity.npc;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class VillagerType {
    public static final VillagerType DESERT = register("desert");
    public static final VillagerType JUNGLE = register("jungle");
    public static final VillagerType PLAINS = register("plains");
    public static final VillagerType SAVANNA = register("savanna");
    public static final VillagerType SNOW = register("snow");
    public static final VillagerType SWAMP = register("swamp");
    public static final VillagerType TAIGA = register("taiga");
    private final String name;
    /** Neo: use the {@link net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps#VILLAGER_TYPES data map} instead as this field will be ignored in future versions */
    @Deprecated
    private static final Map<ResourceKey<Biome>, VillagerType> BY_BIOME = Util.make(Maps.newHashMap(), p_35834_ -> {
        p_35834_.put(Biomes.BADLANDS, DESERT);
        p_35834_.put(Biomes.DESERT, DESERT);
        p_35834_.put(Biomes.ERODED_BADLANDS, DESERT);
        p_35834_.put(Biomes.WOODED_BADLANDS, DESERT);
        p_35834_.put(Biomes.BAMBOO_JUNGLE, JUNGLE);
        p_35834_.put(Biomes.JUNGLE, JUNGLE);
        p_35834_.put(Biomes.SPARSE_JUNGLE, JUNGLE);
        p_35834_.put(Biomes.SAVANNA_PLATEAU, SAVANNA);
        p_35834_.put(Biomes.SAVANNA, SAVANNA);
        p_35834_.put(Biomes.WINDSWEPT_SAVANNA, SAVANNA);
        p_35834_.put(Biomes.DEEP_FROZEN_OCEAN, SNOW);
        p_35834_.put(Biomes.FROZEN_OCEAN, SNOW);
        p_35834_.put(Biomes.FROZEN_RIVER, SNOW);
        p_35834_.put(Biomes.ICE_SPIKES, SNOW);
        p_35834_.put(Biomes.SNOWY_BEACH, SNOW);
        p_35834_.put(Biomes.SNOWY_TAIGA, SNOW);
        p_35834_.put(Biomes.SNOWY_PLAINS, SNOW);
        p_35834_.put(Biomes.GROVE, SNOW);
        p_35834_.put(Biomes.SNOWY_SLOPES, SNOW);
        p_35834_.put(Biomes.FROZEN_PEAKS, SNOW);
        p_35834_.put(Biomes.JAGGED_PEAKS, SNOW);
        p_35834_.put(Biomes.SWAMP, SWAMP);
        p_35834_.put(Biomes.MANGROVE_SWAMP, SWAMP);
        p_35834_.put(Biomes.OLD_GROWTH_SPRUCE_TAIGA, TAIGA);
        p_35834_.put(Biomes.OLD_GROWTH_PINE_TAIGA, TAIGA);
        p_35834_.put(Biomes.WINDSWEPT_GRAVELLY_HILLS, TAIGA);
        p_35834_.put(Biomes.WINDSWEPT_HILLS, TAIGA);
        p_35834_.put(Biomes.TAIGA, TAIGA);
        p_35834_.put(Biomes.WINDSWEPT_FOREST, TAIGA);
    });

    public VillagerType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    private static VillagerType register(String key) {
        return Registry.register(BuiltInRegistries.VILLAGER_TYPE, ResourceLocation.withDefaultNamespace(key), new VillagerType(key));
    }

    public static VillagerType byBiome(Holder<Biome> biome) {
        var fromDataMap = biome.getData(net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps.VILLAGER_TYPES);
        if (fromDataMap != null) return fromDataMap.type();
        // TODO - 1.22: remove fallback
        return biome.unwrapKey().map(BY_BIOME::get).orElse(PLAINS);
    }
}
