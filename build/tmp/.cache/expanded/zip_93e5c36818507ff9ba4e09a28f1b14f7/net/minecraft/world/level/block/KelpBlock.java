package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public class KelpBlock extends GrowingPlantHeadBlock implements LiquidBlockContainer {
    public static final MapCodec<KelpBlock> CODEC = simpleCodec(KelpBlock::new);
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);
    private static final double GROW_PER_TICK_PROBABILITY = 0.14;

    @Override
    public MapCodec<KelpBlock> codec() {
        return CODEC;
    }

    public KelpBlock(BlockBehaviour.Properties p_54300_) {
        super(p_54300_, Direction.UP, SHAPE, true, 0.14);
    }

    @Override
    protected boolean canGrowInto(BlockState state) {
        return state.is(Blocks.WATER);
    }

    @Override
    protected Block getBodyBlock() {
        return Blocks.KELP_PLANT;
    }

    @Override
    protected boolean canAttachTo(BlockState state) {
        return !state.is(Blocks.MAGMA_BLOCK);
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource random) {
        return 1;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8 ? super.getStateForPlacement(context) : null;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getSource(false);
    }
}
