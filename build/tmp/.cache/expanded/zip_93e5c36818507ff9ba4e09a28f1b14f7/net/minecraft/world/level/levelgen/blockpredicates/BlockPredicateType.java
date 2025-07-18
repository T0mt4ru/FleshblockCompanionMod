package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface BlockPredicateType<P extends BlockPredicate> {
    BlockPredicateType<MatchingBlocksPredicate> MATCHING_BLOCKS = register("matching_blocks", MatchingBlocksPredicate.CODEC);
    BlockPredicateType<MatchingBlockTagPredicate> MATCHING_BLOCK_TAG = register("matching_block_tag", MatchingBlockTagPredicate.CODEC);
    BlockPredicateType<MatchingFluidsPredicate> MATCHING_FLUIDS = register("matching_fluids", MatchingFluidsPredicate.CODEC);
    BlockPredicateType<HasSturdyFacePredicate> HAS_STURDY_FACE = register("has_sturdy_face", HasSturdyFacePredicate.CODEC);
    BlockPredicateType<SolidPredicate> SOLID = register("solid", SolidPredicate.CODEC);
    BlockPredicateType<ReplaceablePredicate> REPLACEABLE = register("replaceable", ReplaceablePredicate.CODEC);
    BlockPredicateType<WouldSurvivePredicate> WOULD_SURVIVE = register("would_survive", WouldSurvivePredicate.CODEC);
    BlockPredicateType<InsideWorldBoundsPredicate> INSIDE_WORLD_BOUNDS = register("inside_world_bounds", InsideWorldBoundsPredicate.CODEC);
    BlockPredicateType<AnyOfPredicate> ANY_OF = register("any_of", AnyOfPredicate.CODEC);
    BlockPredicateType<AllOfPredicate> ALL_OF = register("all_of", AllOfPredicate.CODEC);
    BlockPredicateType<NotPredicate> NOT = register("not", NotPredicate.CODEC);
    BlockPredicateType<TrueBlockPredicate> TRUE = register("true", TrueBlockPredicate.CODEC);
    BlockPredicateType<UnobstructedPredicate> UNOBSTRUCTED = register("unobstructed", UnobstructedPredicate.CODEC);

    MapCodec<P> codec();

    private static <P extends BlockPredicate> BlockPredicateType<P> register(String name, MapCodec<P> codec) {
        return Registry.register(BuiltInRegistries.BLOCK_PREDICATE_TYPE, name, () -> codec);
    }
}
