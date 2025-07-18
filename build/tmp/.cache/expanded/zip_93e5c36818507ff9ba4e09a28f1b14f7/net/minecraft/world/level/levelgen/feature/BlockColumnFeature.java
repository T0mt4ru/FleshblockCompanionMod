package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;

public class BlockColumnFeature extends Feature<BlockColumnConfiguration> {
    public BlockColumnFeature(Codec<BlockColumnConfiguration> codec) {
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
    public boolean place(FeaturePlaceContext<BlockColumnConfiguration> context) {
        WorldGenLevel worldgenlevel = context.level();
        BlockColumnConfiguration blockcolumnconfiguration = context.config();
        RandomSource randomsource = context.random();
        int i = blockcolumnconfiguration.layers().size();
        int[] aint = new int[i];
        int j = 0;

        for (int k = 0; k < i; k++) {
            aint[k] = blockcolumnconfiguration.layers().get(k).height().sample(randomsource);
            j += aint[k];
        }

        if (j == 0) {
            return false;
        } else {
            BlockPos.MutableBlockPos blockpos$mutableblockpos1 = context.origin().mutable();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = blockpos$mutableblockpos1.mutable().move(blockcolumnconfiguration.direction());

            for (int l = 0; l < j; l++) {
                if (!blockcolumnconfiguration.allowedPlacement().test(worldgenlevel, blockpos$mutableblockpos)) {
                    truncate(aint, j, l, blockcolumnconfiguration.prioritizeTip());
                    break;
                }

                blockpos$mutableblockpos.move(blockcolumnconfiguration.direction());
            }

            for (int k1 = 0; k1 < i; k1++) {
                int i1 = aint[k1];
                if (i1 != 0) {
                    BlockColumnConfiguration.Layer blockcolumnconfiguration$layer = blockcolumnconfiguration.layers().get(k1);

                    for (int j1 = 0; j1 < i1; j1++) {
                        worldgenlevel.setBlock(
                            blockpos$mutableblockpos1, blockcolumnconfiguration$layer.state().getState(randomsource, blockpos$mutableblockpos1), 2
                        );
                        blockpos$mutableblockpos1.move(blockcolumnconfiguration.direction());
                    }
                }
            }

            return true;
        }
    }

    private static void truncate(int[] layerHeights, int totalHeight, int currentHeight, boolean prioritizeTip) {
        int i = totalHeight - currentHeight;
        int j = prioritizeTip ? 1 : -1;
        int k = prioritizeTip ? 0 : layerHeights.length - 1;
        int l = prioritizeTip ? layerHeights.length : -1;

        for (int i1 = k; i1 != l && i > 0; i1 += j) {
            int j1 = layerHeights[i1];
            int k1 = Math.min(j1, i);
            i -= k1;
            layerHeights[i1] -= k1;
        }
    }
}
