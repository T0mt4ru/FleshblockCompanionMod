package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BeehiveDecorator extends TreeDecorator {
    public static final MapCodec<BeehiveDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
        .fieldOf("probability")
        .xmap(BeehiveDecorator::new, p_69971_ -> p_69971_.probability);
    private static final Direction WORLDGEN_FACING = Direction.SOUTH;
    private static final Direction[] SPAWN_DIRECTIONS = Direction.Plane.HORIZONTAL
        .stream()
        .filter(p_202307_ -> p_202307_ != WORLDGEN_FACING.getOpposite())
        .toArray(Direction[]::new);
    /**
     * Probability to generate a beehive
     */
    private final float probability;

    public BeehiveDecorator(float probability) {
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.BEEHIVE;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource randomsource = context.random();
        if (!(randomsource.nextFloat() >= this.probability)) {
            List<BlockPos> list = context.leaves();
            List<BlockPos> list1 = context.logs();
            int i = !list.isEmpty()
                ? Math.max(list.get(0).getY() - 1, list1.get(0).getY() + 1)
                : Math.min(list1.get(0).getY() + 1 + randomsource.nextInt(3), list1.get(list1.size() - 1).getY());
            List<BlockPos> list2 = list1.stream()
                .filter(p_202300_ -> p_202300_.getY() == i)
                .flatMap(p_202305_ -> Stream.of(SPAWN_DIRECTIONS).map(p_202305_::relative))
                .collect(Collectors.toList());
            if (!list2.isEmpty()) {
                Collections.shuffle(list2);
                Optional<BlockPos> optional = list2.stream()
                    .filter(p_226022_ -> context.isAir(p_226022_) && context.isAir(p_226022_.relative(WORLDGEN_FACING)))
                    .findFirst();
                if (!optional.isEmpty()) {
                    context.setBlock(optional.get(), Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, WORLDGEN_FACING));
                    context.level().getBlockEntity(optional.get(), BlockEntityType.BEEHIVE).ifPresent(p_330149_ -> {
                        int j = 2 + randomsource.nextInt(2);

                        for (int k = 0; k < j; k++) {
                            p_330149_.storeBee(BeehiveBlockEntity.Occupant.create(randomsource.nextInt(599)));
                        }
                    });
                }
            }
        }
    }
}
