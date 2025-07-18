package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class StainedGlassPaneBlock extends IronBarsBlock implements BeaconBeamBlock {
    public static final MapCodec<StainedGlassPaneBlock> CODEC = RecordCodecBuilder.mapCodec(
        p_308838_ -> p_308838_.group(DyeColor.CODEC.fieldOf("color").forGetter(StainedGlassPaneBlock::getColor), propertiesCodec())
                .apply(p_308838_, StainedGlassPaneBlock::new)
    );
    private final DyeColor color;

    @Override
    public MapCodec<StainedGlassPaneBlock> codec() {
        return CODEC;
    }

    public StainedGlassPaneBlock(DyeColor color, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = color;
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(NORTH, Boolean.valueOf(false))
                .setValue(EAST, Boolean.valueOf(false))
                .setValue(SOUTH, Boolean.valueOf(false))
                .setValue(WEST, Boolean.valueOf(false))
                .setValue(WATERLOGGED, Boolean.valueOf(false))
        );
    }

    @Override
    public DyeColor getColor() {
        return this.color;
    }
}
