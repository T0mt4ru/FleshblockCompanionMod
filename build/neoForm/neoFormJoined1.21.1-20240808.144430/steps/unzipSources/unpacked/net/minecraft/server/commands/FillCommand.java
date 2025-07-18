package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FillCommand {
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
        (p_304218_, p_304219_) -> Component.translatableEscape("commands.fill.toobig", p_304218_, p_304219_)
    );
    static final BlockInput HOLLOW_CORE = new BlockInput(Blocks.AIR.defaultBlockState(), Collections.emptySet(), null);
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.fill.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
            Commands.literal("fill")
                .requires(p_137384_ -> p_137384_.hasPermission(2))
                .then(
                    Commands.argument("from", BlockPosArgument.blockPos())
                        .then(
                            Commands.argument("to", BlockPosArgument.blockPos())
                                .then(
                                    Commands.argument("block", BlockStateArgument.block(context))
                                        .executes(
                                            p_137405_ -> fillBlocks(
                                                    p_137405_.getSource(),
                                                    BoundingBox.fromCorners(
                                                        BlockPosArgument.getLoadedBlockPos(p_137405_, "from"),
                                                        BlockPosArgument.getLoadedBlockPos(p_137405_, "to")
                                                    ),
                                                    BlockStateArgument.getBlock(p_137405_, "block"),
                                                    FillCommand.Mode.REPLACE,
                                                    null
                                                )
                                        )
                                        .then(
                                            Commands.literal("replace")
                                                .executes(
                                                    p_137403_ -> fillBlocks(
                                                            p_137403_.getSource(),
                                                            BoundingBox.fromCorners(
                                                                BlockPosArgument.getLoadedBlockPos(p_137403_, "from"),
                                                                BlockPosArgument.getLoadedBlockPos(p_137403_, "to")
                                                            ),
                                                            BlockStateArgument.getBlock(p_137403_, "block"),
                                                            FillCommand.Mode.REPLACE,
                                                            null
                                                        )
                                                )
                                                .then(
                                                    Commands.argument("filter", BlockPredicateArgument.blockPredicate(context))
                                                        .executes(
                                                            p_137401_ -> fillBlocks(
                                                                    p_137401_.getSource(),
                                                                    BoundingBox.fromCorners(
                                                                        BlockPosArgument.getLoadedBlockPos(p_137401_, "from"),
                                                                        BlockPosArgument.getLoadedBlockPos(p_137401_, "to")
                                                                    ),
                                                                    BlockStateArgument.getBlock(p_137401_, "block"),
                                                                    FillCommand.Mode.REPLACE,
                                                                    BlockPredicateArgument.getBlockPredicate(p_137401_, "filter")
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("keep")
                                                .executes(
                                                    p_137399_ -> fillBlocks(
                                                            p_137399_.getSource(),
                                                            BoundingBox.fromCorners(
                                                                BlockPosArgument.getLoadedBlockPos(p_137399_, "from"),
                                                                BlockPosArgument.getLoadedBlockPos(p_137399_, "to")
                                                            ),
                                                            BlockStateArgument.getBlock(p_137399_, "block"),
                                                            FillCommand.Mode.REPLACE,
                                                            p_180225_ -> p_180225_.getLevel().isEmptyBlock(p_180225_.getPos())
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("outline")
                                                .executes(
                                                    p_137397_ -> fillBlocks(
                                                            p_137397_.getSource(),
                                                            BoundingBox.fromCorners(
                                                                BlockPosArgument.getLoadedBlockPos(p_137397_, "from"),
                                                                BlockPosArgument.getLoadedBlockPos(p_137397_, "to")
                                                            ),
                                                            BlockStateArgument.getBlock(p_137397_, "block"),
                                                            FillCommand.Mode.OUTLINE,
                                                            null
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("hollow")
                                                .executes(
                                                    p_137395_ -> fillBlocks(
                                                            p_137395_.getSource(),
                                                            BoundingBox.fromCorners(
                                                                BlockPosArgument.getLoadedBlockPos(p_137395_, "from"),
                                                                BlockPosArgument.getLoadedBlockPos(p_137395_, "to")
                                                            ),
                                                            BlockStateArgument.getBlock(p_137395_, "block"),
                                                            FillCommand.Mode.HOLLOW,
                                                            null
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.literal("destroy")
                                                .executes(
                                                    p_137382_ -> fillBlocks(
                                                            p_137382_.getSource(),
                                                            BoundingBox.fromCorners(
                                                                BlockPosArgument.getLoadedBlockPos(p_137382_, "from"),
                                                                BlockPosArgument.getLoadedBlockPos(p_137382_, "to")
                                                            ),
                                                            BlockStateArgument.getBlock(p_137382_, "block"),
                                                            FillCommand.Mode.DESTROY,
                                                            null
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int fillBlocks(
        CommandSourceStack source, BoundingBox area, BlockInput newBlock, FillCommand.Mode mode, @Nullable Predicate<BlockInWorld> replacingPredicate
    ) throws CommandSyntaxException {
        int i = area.getXSpan() * area.getYSpan() * area.getZSpan();
        int j = source.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT);
        if (i > j) {
            throw ERROR_AREA_TOO_LARGE.create(j, i);
        } else {
            List<BlockPos> list = Lists.newArrayList();
            ServerLevel serverlevel = source.getLevel();
            int k = 0;

            for (BlockPos blockpos : BlockPos.betweenClosed(
                area.minX(), area.minY(), area.minZ(), area.maxX(), area.maxY(), area.maxZ()
            )) {
                if (replacingPredicate == null || replacingPredicate.test(new BlockInWorld(serverlevel, blockpos, true))) {
                    BlockInput blockinput = mode.filter.filter(area, blockpos, newBlock, serverlevel);
                    if (blockinput != null) {
                        BlockEntity blockentity = serverlevel.getBlockEntity(blockpos);
                        Clearable.tryClear(blockentity);
                        if (blockinput.place(serverlevel, blockpos, 2)) {
                            list.add(blockpos.immutable());
                            k++;
                        }
                    }
                }
            }

            for (BlockPos blockpos1 : list) {
                Block block = serverlevel.getBlockState(blockpos1).getBlock();
                serverlevel.blockUpdated(blockpos1, block);
            }

            if (k == 0) {
                throw ERROR_FAILED.create();
            } else {
                int l = k;
                source.sendSuccess(() -> Component.translatable("commands.fill.success", l), true);
                return k;
            }
        }
    }

    static enum Mode {
        REPLACE((p_137433_, p_137434_, p_137435_, p_137436_) -> p_137435_),
        OUTLINE(
            (p_137428_, p_137429_, p_137430_, p_137431_) -> p_137429_.getX() != p_137428_.minX()
                        && p_137429_.getX() != p_137428_.maxX()
                        && p_137429_.getY() != p_137428_.minY()
                        && p_137429_.getY() != p_137428_.maxY()
                        && p_137429_.getZ() != p_137428_.minZ()
                        && p_137429_.getZ() != p_137428_.maxZ()
                    ? null
                    : p_137430_
        ),
        HOLLOW(
            (p_137423_, p_137424_, p_137425_, p_137426_) -> p_137424_.getX() != p_137423_.minX()
                        && p_137424_.getX() != p_137423_.maxX()
                        && p_137424_.getY() != p_137423_.minY()
                        && p_137424_.getY() != p_137423_.maxY()
                        && p_137424_.getZ() != p_137423_.minZ()
                        && p_137424_.getZ() != p_137423_.maxZ()
                    ? FillCommand.HOLLOW_CORE
                    : p_137425_
        ),
        DESTROY((p_137418_, p_137419_, p_137420_, p_137421_) -> {
            p_137421_.destroyBlock(p_137419_, true);
            return p_137420_;
        });

        public final SetBlockCommand.Filter filter;

        private Mode(SetBlockCommand.Filter filter) {
            this.filter = filter;
        }
    }
}
