package net.minecraft.data.worldgen;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasureStructure;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidStructure;
import net.minecraft.world.level.levelgen.structure.structures.EndCityStructure;
import net.minecraft.world.level.levelgen.structure.structures.IglooStructure;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.structures.JungleTempleStructure;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftStructure;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure;
import net.minecraft.world.level.levelgen.structure.structures.NetherFossilStructure;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import net.minecraft.world.level.levelgen.structure.structures.OceanRuinStructure;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalStructure;
import net.minecraft.world.level.levelgen.structure.structures.ShipwreckStructure;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdStructure;
import net.minecraft.world.level.levelgen.structure.structures.SwampHutStructure;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

public class Structures {
    public static void bootstrap(BootstrapContext<Structure> context) {
        HolderGetter<Biome> holdergetter = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> holdergetter1 = context.lookup(Registries.TEMPLATE_POOL);
        context.register(
            BuiltinStructures.PILLAGER_OUTPOST,
            new JigsawStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_PILLAGER_OUTPOST))
                    .spawnOverrides(
                        Map.of(
                            MobCategory.MONSTER,
                            new StructureSpawnOverride(
                                StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                                WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1))
                            )
                        )
                    )
                    .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                    .build(),
                holdergetter1.getOrThrow(PillagerOutpostPools.START),
                7,
                ConstantHeight.of(VerticalAnchor.absolute(0)),
                true,
                Heightmap.Types.WORLD_SURFACE_WG
            )
        );
        context.register(
            BuiltinStructures.MINESHAFT,
            new MineshaftStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_MINESHAFT))
                    .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                    .build(),
                MineshaftStructure.Type.NORMAL
            )
        );
        context.register(
            BuiltinStructures.MINESHAFT_MESA,
            new MineshaftStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_MINESHAFT_MESA))
                    .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                    .build(),
                MineshaftStructure.Type.MESA
            )
        );
        context.register(
            BuiltinStructures.WOODLAND_MANSION,
            new WoodlandMansionStructure(new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_WOODLAND_MANSION)))
        );
        context.register(
            BuiltinStructures.JUNGLE_TEMPLE, new JungleTempleStructure(new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_JUNGLE_TEMPLE)))
        );
        context.register(
            BuiltinStructures.DESERT_PYRAMID,
            new DesertPyramidStructure(new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_DESERT_PYRAMID)))
        );
        context.register(BuiltinStructures.IGLOO, new IglooStructure(new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_IGLOO))));
        context.register(
            BuiltinStructures.SHIPWRECK, new ShipwreckStructure(new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_SHIPWRECK)), false)
        );
        context.register(
            BuiltinStructures.SHIPWRECK_BEACHED,
            new ShipwreckStructure(new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_SHIPWRECK_BEACHED)), true)
        );
        context.register(
            BuiltinStructures.SWAMP_HUT,
            new SwampHutStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_SWAMP_HUT))
                    .spawnOverrides(
                        Map.of(
                            MobCategory.MONSTER,
                            new StructureSpawnOverride(
                                StructureSpawnOverride.BoundingBoxType.PIECE,
                                WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1, 1))
                            ),
                            MobCategory.CREATURE,
                            new StructureSpawnOverride(
                                StructureSpawnOverride.BoundingBoxType.PIECE,
                                WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.CAT, 1, 1, 1))
                            )
                        )
                    )
                    .build()
            )
        );
        context.register(
            BuiltinStructures.STRONGHOLD,
            new StrongholdStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_STRONGHOLD)).terrainAdapation(TerrainAdjustment.BURY).build()
            )
        );
        context.register(
            BuiltinStructures.OCEAN_MONUMENT,
            new OceanMonumentStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_OCEAN_MONUMENT))
                    .spawnOverrides(
                        Map.of(
                            MobCategory.MONSTER,
                            new StructureSpawnOverride(
                                StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                                WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.GUARDIAN, 1, 2, 4))
                            ),
                            MobCategory.UNDERGROUND_WATER_CREATURE,
                            new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, MobSpawnSettings.EMPTY_MOB_LIST),
                            MobCategory.AXOLOTLS,
                            new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, MobSpawnSettings.EMPTY_MOB_LIST)
                        )
                    )
                    .build()
            )
        );
        context.register(
            BuiltinStructures.OCEAN_RUIN_COLD,
            new OceanRuinStructure(
                new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_OCEAN_RUIN_COLD)), OceanRuinStructure.Type.COLD, 0.3F, 0.9F
            )
        );
        context.register(
            BuiltinStructures.OCEAN_RUIN_WARM,
            new OceanRuinStructure(
                new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_OCEAN_RUIN_WARM)), OceanRuinStructure.Type.WARM, 0.3F, 0.9F
            )
        );
        context.register(
            BuiltinStructures.FORTRESS,
            new NetherFortressStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_NETHER_FORTRESS))
                    .spawnOverrides(
                        Map.of(
                            MobCategory.MONSTER,
                            new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, NetherFortressStructure.FORTRESS_ENEMIES)
                        )
                    )
                    .generationStep(GenerationStep.Decoration.UNDERGROUND_DECORATION)
                    .build()
            )
        );
        context.register(
            BuiltinStructures.NETHER_FOSSIL,
            new NetherFossilStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_NETHER_FOSSIL))
                    .generationStep(GenerationStep.Decoration.UNDERGROUND_DECORATION)
                    .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                    .build(),
                UniformHeight.of(VerticalAnchor.absolute(32), VerticalAnchor.belowTop(2))
            )
        );
        context.register(BuiltinStructures.END_CITY, new EndCityStructure(new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_END_CITY))));
        context.register(
            BuiltinStructures.BURIED_TREASURE,
            new BuriedTreasureStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_BURIED_TREASURE))
                    .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                    .build()
            )
        );
        context.register(
            BuiltinStructures.BASTION_REMNANT,
            new JigsawStructure(
                new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_BASTION_REMNANT)),
                holdergetter1.getOrThrow(BastionPieces.START),
                6,
                ConstantHeight.of(VerticalAnchor.absolute(33)),
                false
            )
        );
        context.register(
            BuiltinStructures.VILLAGE_PLAINS,
            new JigsawStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_VILLAGE_PLAINS))
                    .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                    .build(),
                holdergetter1.getOrThrow(PlainVillagePools.START),
                6,
                ConstantHeight.of(VerticalAnchor.absolute(0)),
                true,
                Heightmap.Types.WORLD_SURFACE_WG
            )
        );
        context.register(
            BuiltinStructures.VILLAGE_DESERT,
            new JigsawStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_VILLAGE_DESERT))
                    .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                    .build(),
                holdergetter1.getOrThrow(DesertVillagePools.START),
                6,
                ConstantHeight.of(VerticalAnchor.absolute(0)),
                true,
                Heightmap.Types.WORLD_SURFACE_WG
            )
        );
        context.register(
            BuiltinStructures.VILLAGE_SAVANNA,
            new JigsawStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_VILLAGE_SAVANNA))
                    .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                    .build(),
                holdergetter1.getOrThrow(SavannaVillagePools.START),
                6,
                ConstantHeight.of(VerticalAnchor.absolute(0)),
                true,
                Heightmap.Types.WORLD_SURFACE_WG
            )
        );
        context.register(
            BuiltinStructures.VILLAGE_SNOWY,
            new JigsawStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_VILLAGE_SNOWY))
                    .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                    .build(),
                holdergetter1.getOrThrow(SnowyVillagePools.START),
                6,
                ConstantHeight.of(VerticalAnchor.absolute(0)),
                true,
                Heightmap.Types.WORLD_SURFACE_WG
            )
        );
        context.register(
            BuiltinStructures.VILLAGE_TAIGA,
            new JigsawStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_VILLAGE_TAIGA))
                    .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                    .build(),
                holdergetter1.getOrThrow(TaigaVillagePools.START),
                6,
                ConstantHeight.of(VerticalAnchor.absolute(0)),
                true,
                Heightmap.Types.WORLD_SURFACE_WG
            )
        );
        context.register(
            BuiltinStructures.RUINED_PORTAL_STANDARD,
            new RuinedPortalStructure(
                new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_STANDARD)),
                List.of(
                    new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.UNDERGROUND, 1.0F, 0.2F, false, false, true, false, 0.5F),
                    new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.2F, false, false, true, false, 0.5F)
                )
            )
        );
        context.register(
            BuiltinStructures.RUINED_PORTAL_DESERT,
            new RuinedPortalStructure(
                new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_DESERT)),
                new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED, 0.0F, 0.0F, false, false, false, false, 1.0F)
            )
        );
        context.register(
            BuiltinStructures.RUINED_PORTAL_JUNGLE,
            new RuinedPortalStructure(
                new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_JUNGLE)),
                new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.8F, true, true, false, false, 1.0F)
            )
        );
        context.register(
            BuiltinStructures.RUINED_PORTAL_SWAMP,
            new RuinedPortalStructure(
                new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_SWAMP)),
                new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR, 0.0F, 0.5F, false, true, false, false, 1.0F)
            )
        );
        context.register(
            BuiltinStructures.RUINED_PORTAL_MOUNTAIN,
            new RuinedPortalStructure(
                new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_MOUNTAIN)),
                List.of(
                    new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN, 1.0F, 0.2F, false, false, true, false, 0.5F),
                    new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.2F, false, false, true, false, 0.5F)
                )
            )
        );
        context.register(
            BuiltinStructures.RUINED_PORTAL_OCEAN,
            new RuinedPortalStructure(
                new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_OCEAN)),
                new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR, 0.0F, 0.8F, false, false, true, false, 1.0F)
            )
        );
        context.register(
            BuiltinStructures.RUINED_PORTAL_NETHER,
            new RuinedPortalStructure(
                new Structure.StructureSettings(holdergetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_NETHER)),
                new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.IN_NETHER, 0.5F, 0.0F, false, false, false, true, 1.0F)
            )
        );
        context.register(
            BuiltinStructures.ANCIENT_CITY,
            new JigsawStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_ANCIENT_CITY))
                    .spawnOverrides(
                        Arrays.stream(MobCategory.values())
                            .collect(
                                Collectors.toMap(
                                    p_236555_ -> (MobCategory)p_236555_,
                                    p_236551_ -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create())
                                )
                            )
                    )
                    .generationStep(GenerationStep.Decoration.UNDERGROUND_DECORATION)
                    .terrainAdapation(TerrainAdjustment.BEARD_BOX)
                    .build(),
                holdergetter1.getOrThrow(AncientCityStructurePieces.START),
                Optional.of(ResourceLocation.withDefaultNamespace("city_anchor")),
                7,
                ConstantHeight.of(VerticalAnchor.absolute(-27)),
                false,
                Optional.empty(),
                116,
                List.of(),
                JigsawStructure.DEFAULT_DIMENSION_PADDING,
                JigsawStructure.DEFAULT_LIQUID_SETTINGS
            )
        );
        context.register(
            BuiltinStructures.TRAIL_RUINS,
            new JigsawStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_TRAIL_RUINS))
                    .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                    .terrainAdapation(TerrainAdjustment.BURY)
                    .build(),
                holdergetter1.getOrThrow(TrailRuinsStructurePools.START),
                7,
                ConstantHeight.of(VerticalAnchor.absolute(-15)),
                false,
                Heightmap.Types.WORLD_SURFACE_WG
            )
        );
        context.register(
            BuiltinStructures.TRIAL_CHAMBERS,
            new JigsawStructure(
                new Structure.StructureSettings.Builder(holdergetter.getOrThrow(BiomeTags.HAS_TRIAL_CHAMBERS))
                    .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                    .terrainAdapation(TerrainAdjustment.ENCAPSULATE)
                    .spawnOverrides(
                        Arrays.stream(MobCategory.values())
                            .collect(
                                Collectors.toMap(
                                    p_344252_ -> (MobCategory)p_344252_,
                                    p_344253_ -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, WeightedRandomList.create())
                                )
                            )
                    )
                    .build(),
                holdergetter1.getOrThrow(TrialChambersStructurePools.START),
                Optional.empty(),
                20,
                UniformHeight.of(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(-20)),
                false,
                Optional.empty(),
                116,
                TrialChambersStructurePools.ALIAS_BINDINGS,
                new DimensionPadding(10),
                LiquidSettings.IGNORE_WATERLOGGING
            )
        );
    }
}
