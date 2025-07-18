package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface StructurePlacementType<SP extends StructurePlacement> {
    StructurePlacementType<RandomSpreadStructurePlacement> RANDOM_SPREAD = register("random_spread", RandomSpreadStructurePlacement.CODEC);
    StructurePlacementType<ConcentricRingsStructurePlacement> CONCENTRIC_RINGS = register("concentric_rings", ConcentricRingsStructurePlacement.CODEC);

    MapCodec<SP> codec();

    private static <SP extends StructurePlacement> StructurePlacementType<SP> register(String name, MapCodec<SP> codec) {
        return Registry.register(BuiltInRegistries.STRUCTURE_PLACEMENT, name, () -> codec);
    }
}
