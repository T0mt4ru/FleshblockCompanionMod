package net.minecraft.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.placement.EndPlacements;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class EndBiomes {
    private static Biome baseEndBiome(BiomeGenerationSettings.Builder generationSettings) {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.endSpawns(mobspawnsettings$builder);
        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(0.5F)
            .downfall(0.5F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(10518688)
                    .skyColor(0)
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(mobspawnsettings$builder.build())
            .generationSettings(generationSettings.build())
            .build();
    }

    public static Biome endBarrens(HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> worldCarvers) {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers);
        return baseEndBiome(biomegenerationsettings$builder);
    }

    public static Biome theEnd(HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> worldCarvers) {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
            .addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, EndPlacements.END_SPIKE)
            .addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, EndPlacements.END_PLATFORM);
        return baseEndBiome(biomegenerationsettings$builder);
    }

    public static Biome endMidlands(HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> worldCarvers) {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers);
        return baseEndBiome(biomegenerationsettings$builder);
    }

    public static Biome endHighlands(HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> worldCarvers) {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
            .addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, EndPlacements.END_GATEWAY_RETURN)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, EndPlacements.CHORUS_PLANT);
        return baseEndBiome(biomegenerationsettings$builder);
    }

    public static Biome smallEndIslands(HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> worldCarvers) {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers)
            .addFeature(GenerationStep.Decoration.RAW_GENERATION, EndPlacements.END_ISLAND_DECORATED);
        return baseEndBiome(biomegenerationsettings$builder);
    }
}
