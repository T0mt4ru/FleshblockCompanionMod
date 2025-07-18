package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class DesertWellFeature extends Feature<NoneFeatureConfiguration> {
    private static final BlockStatePredicate IS_SAND = BlockStatePredicate.forBlock(Blocks.SAND);
    private final BlockState sand = Blocks.SAND.defaultBlockState();
    private final BlockState sandSlab = Blocks.SANDSTONE_SLAB.defaultBlockState();
    private final BlockState sandstone = Blocks.SANDSTONE.defaultBlockState();
    private final BlockState water = Blocks.WATER.defaultBlockState();

    public DesertWellFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    /**
     * Places the given feature at the given location.
     * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated, that they can safely generate into.
     *
     * @param context A context object with a reference to the level and the position
     *                the feature is being placed at
     */
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel worldgenlevel = context.level();
        BlockPos blockpos = context.origin();
        blockpos = blockpos.above();

        while (worldgenlevel.isEmptyBlock(blockpos) && blockpos.getY() > worldgenlevel.getMinBuildHeight() + 2) {
            blockpos = blockpos.below();
        }

        if (!IS_SAND.test(worldgenlevel.getBlockState(blockpos))) {
            return false;
        } else {
            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    if (worldgenlevel.isEmptyBlock(blockpos.offset(i, -1, j)) && worldgenlevel.isEmptyBlock(blockpos.offset(i, -2, j))) {
                        return false;
                    }
                }
            }

            for (int l = -2; l <= 0; l++) {
                for (int i1 = -2; i1 <= 2; i1++) {
                    for (int k = -2; k <= 2; k++) {
                        worldgenlevel.setBlock(blockpos.offset(i1, l, k), this.sandstone, 2);
                    }
                }
            }

            worldgenlevel.setBlock(blockpos, this.water, 2);

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                worldgenlevel.setBlock(blockpos.relative(direction), this.water, 2);
            }

            BlockPos blockpos1 = blockpos.below();
            worldgenlevel.setBlock(blockpos1, this.sand, 2);

            for (Direction direction1 : Direction.Plane.HORIZONTAL) {
                worldgenlevel.setBlock(blockpos1.relative(direction1), this.sand, 2);
            }

            for (int j1 = -2; j1 <= 2; j1++) {
                for (int i2 = -2; i2 <= 2; i2++) {
                    if (j1 == -2 || j1 == 2 || i2 == -2 || i2 == 2) {
                        worldgenlevel.setBlock(blockpos.offset(j1, 1, i2), this.sandstone, 2);
                    }
                }
            }

            worldgenlevel.setBlock(blockpos.offset(2, 1, 0), this.sandSlab, 2);
            worldgenlevel.setBlock(blockpos.offset(-2, 1, 0), this.sandSlab, 2);
            worldgenlevel.setBlock(blockpos.offset(0, 1, 2), this.sandSlab, 2);
            worldgenlevel.setBlock(blockpos.offset(0, 1, -2), this.sandSlab, 2);

            for (int k1 = -1; k1 <= 1; k1++) {
                for (int j2 = -1; j2 <= 1; j2++) {
                    if (k1 == 0 && j2 == 0) {
                        worldgenlevel.setBlock(blockpos.offset(k1, 4, j2), this.sandstone, 2);
                    } else {
                        worldgenlevel.setBlock(blockpos.offset(k1, 4, j2), this.sandSlab, 2);
                    }
                }
            }

            for (int l1 = 1; l1 <= 3; l1++) {
                worldgenlevel.setBlock(blockpos.offset(-1, l1, -1), this.sandstone, 2);
                worldgenlevel.setBlock(blockpos.offset(-1, l1, 1), this.sandstone, 2);
                worldgenlevel.setBlock(blockpos.offset(1, l1, -1), this.sandstone, 2);
                worldgenlevel.setBlock(blockpos.offset(1, l1, 1), this.sandstone, 2);
            }

            List<BlockPos> list = List.of(blockpos, blockpos.east(), blockpos.south(), blockpos.west(), blockpos.north());
            RandomSource randomsource = context.random();
            placeSusSand(worldgenlevel, Util.getRandom(list, randomsource).below(1));
            placeSusSand(worldgenlevel, Util.getRandom(list, randomsource).below(2));
            return true;
        }
    }

    private static void placeSusSand(WorldGenLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 3);
        level.getBlockEntity(pos, BlockEntityType.BRUSHABLE_BLOCK)
            .ifPresent(p_335307_ -> p_335307_.setLootTable(BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY, pos.asLong()));
    }
}
