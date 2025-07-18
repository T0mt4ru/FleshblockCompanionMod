package net.minecraft.world.level.levelgen.material;

import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseChunk;

public interface WorldGenMaterialRule {
    @Nullable
    BlockState apply(NoiseChunk chunk, int x, int y, int z);
}
