package net.minecraft.world.level.block;

import com.google.common.base.Predicates;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EndPortalFrameBlock extends Block {
    public static final MapCodec<EndPortalFrameBlock> CODEC = simpleCodec(EndPortalFrameBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty HAS_EYE = BlockStateProperties.EYE;
    protected static final VoxelShape BASE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 13.0, 16.0);
    protected static final VoxelShape EYE_SHAPE = Block.box(4.0, 13.0, 4.0, 12.0, 16.0, 12.0);
    protected static final VoxelShape FULL_SHAPE = Shapes.or(BASE_SHAPE, EYE_SHAPE);
    private static BlockPattern portalShape;

    @Override
    public MapCodec<EndPortalFrameBlock> codec() {
        return CODEC;
    }

    public EndPortalFrameBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HAS_EYE, Boolean.valueOf(false)));
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HAS_EYE) ? FULL_SHAPE : BASE_SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(HAS_EYE, Boolean.valueOf(false));
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /**
     * Returns the analog signal this block emits. This is the signal a comparator can read from it.
     *
     */
    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return blockState.getValue(HAS_EYE) ? 15 : 0;
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed blockstate.
     */
    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed blockstate.
     */
    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_EYE);
    }

    public static BlockPattern getOrCreatePortalShape() {
        if (portalShape == null) {
            portalShape = BlockPatternBuilder.start()
                .aisle("?vvv?", ">???<", ">???<", ">???<", "?^^^?")
                .where('?', BlockInWorld.hasState(BlockStatePredicate.ANY))
                .where(
                    '^',
                    BlockInWorld.hasState(
                        BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME)
                            .where(HAS_EYE, Predicates.equalTo(true))
                            .where(FACING, Predicates.equalTo(Direction.SOUTH))
                    )
                )
                .where(
                    '>',
                    BlockInWorld.hasState(
                        BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME)
                            .where(HAS_EYE, Predicates.equalTo(true))
                            .where(FACING, Predicates.equalTo(Direction.WEST))
                    )
                )
                .where(
                    'v',
                    BlockInWorld.hasState(
                        BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME)
                            .where(HAS_EYE, Predicates.equalTo(true))
                            .where(FACING, Predicates.equalTo(Direction.NORTH))
                    )
                )
                .where(
                    '<',
                    BlockInWorld.hasState(
                        BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME)
                            .where(HAS_EYE, Predicates.equalTo(true))
                            .where(FACING, Predicates.equalTo(Direction.EAST))
                    )
                )
                .build();
        }

        return portalShape;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
