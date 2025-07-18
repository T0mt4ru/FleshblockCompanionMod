package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;

public class ReplaceBlockConfiguration implements FeatureConfiguration {
    public static final Codec<ReplaceBlockConfiguration> CODEC = RecordCodecBuilder.create(
        p_161087_ -> p_161087_.group(Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter(p_161089_ -> p_161089_.targetStates))
                .apply(p_161087_, ReplaceBlockConfiguration::new)
    );
    public final List<OreConfiguration.TargetBlockState> targetStates;

    public ReplaceBlockConfiguration(BlockState targetState, BlockState state) {
        this(ImmutableList.of(OreConfiguration.target(new BlockStateMatchTest(targetState), state)));
    }

    public ReplaceBlockConfiguration(List<OreConfiguration.TargetBlockState> targetStates) {
        this.targetStates = targetStates;
    }
}
