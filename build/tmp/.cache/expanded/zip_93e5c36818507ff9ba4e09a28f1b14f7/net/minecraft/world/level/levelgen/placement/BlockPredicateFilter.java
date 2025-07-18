package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public class BlockPredicateFilter extends PlacementFilter {
    public static final MapCodec<BlockPredicateFilter> CODEC = RecordCodecBuilder.mapCodec(
        p_191575_ -> p_191575_.group(BlockPredicate.CODEC.fieldOf("predicate").forGetter(p_191579_ -> p_191579_.predicate))
                .apply(p_191575_, BlockPredicateFilter::new)
    );
    private final BlockPredicate predicate;

    private BlockPredicateFilter(BlockPredicate predicate) {
        this.predicate = predicate;
    }

    public static BlockPredicateFilter forPredicate(BlockPredicate predicate) {
        return new BlockPredicateFilter(predicate);
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return this.predicate.test(context.getLevel(), pos);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.BLOCK_PREDICATE_FILTER;
    }
}
