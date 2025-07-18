package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class CloneCommands {
    private static final SimpleCommandExceptionType ERROR_OVERLAP = new SimpleCommandExceptionType(Component.translatable("commands.clone.overlap"));
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
        (p_304194_, p_304195_) -> Component.translatableEscape("commands.clone.toobig", p_304194_, p_304195_)
    );
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.clone.failed"));
    public static final Predicate<BlockInWorld> FILTER_AIR = p_284652_ -> !p_284652_.getState().isAir();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
            Commands.literal("clone")
                .requires(p_136734_ -> p_136734_.hasPermission(2))
                .then(beginEndDestinationAndModeSuffix(context, p_264757_ -> p_264757_.getSource().getLevel()))
                .then(
                    Commands.literal("from")
                        .then(
                            Commands.argument("sourceDimension", DimensionArgument.dimension())
                                .then(beginEndDestinationAndModeSuffix(context, p_264743_ -> DimensionArgument.getDimension(p_264743_, "sourceDimension")))
                        )
                )
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> beginEndDestinationAndModeSuffix(
        CommandBuildContext buildContext, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> levelGetter
    ) {
        return Commands.argument("begin", BlockPosArgument.blockPos())
            .then(
                Commands.argument("end", BlockPosArgument.blockPos())
                    .then(destinationAndModeSuffix(buildContext, levelGetter, p_264751_ -> p_264751_.getSource().getLevel()))
                    .then(
                        Commands.literal("to")
                            .then(
                                Commands.argument("targetDimension", DimensionArgument.dimension())
                                    .then(
                                        destinationAndModeSuffix(
                                            buildContext, levelGetter, p_264756_ -> DimensionArgument.getDimension(p_264756_, "targetDimension")
                                        )
                                    )
                            )
                    )
            );
    }

    private static CloneCommands.DimensionAndPosition getLoadedDimensionAndPosition(
        CommandContext<CommandSourceStack> context, ServerLevel level, String name
    ) throws CommandSyntaxException {
        BlockPos blockpos = BlockPosArgument.getLoadedBlockPos(context, level, name);
        return new CloneCommands.DimensionAndPosition(level, blockpos);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> destinationAndModeSuffix(
        CommandBuildContext buildContext,
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> sourceLevelGetter,
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> destinationLevelGetter
    ) {
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> commandfunction = p_264737_ -> getLoadedDimensionAndPosition(
                p_264737_, sourceLevelGetter.apply(p_264737_), "begin"
            );
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> commandfunction1 = p_264735_ -> getLoadedDimensionAndPosition(
                p_264735_, sourceLevelGetter.apply(p_264735_), "end"
            );
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> commandfunction2 = p_264768_ -> getLoadedDimensionAndPosition(
                p_264768_, destinationLevelGetter.apply(p_264768_), "destination"
            );
        return Commands.argument("destination", BlockPosArgument.blockPos())
            .executes(
                p_264761_ -> clone(
                        p_264761_.getSource(),
                        commandfunction.apply(p_264761_),
                        commandfunction1.apply(p_264761_),
                        commandfunction2.apply(p_264761_),
                        p_180033_ -> true,
                        CloneCommands.Mode.NORMAL
                    )
            )
            .then(
                wrapWithCloneMode(
                    commandfunction,
                    commandfunction1,
                    commandfunction2,
                    p_264738_ -> p_180041_ -> true,
                    Commands.literal("replace")
                        .executes(
                            p_264755_ -> clone(
                                    p_264755_.getSource(),
                                    commandfunction.apply(p_264755_),
                                    commandfunction1.apply(p_264755_),
                                    commandfunction2.apply(p_264755_),
                                    p_180039_ -> true,
                                    CloneCommands.Mode.NORMAL
                                )
                        )
                )
            )
            .then(
                wrapWithCloneMode(
                    commandfunction,
                    commandfunction1,
                    commandfunction2,
                    p_264744_ -> FILTER_AIR,
                    Commands.literal("masked")
                        .executes(
                            p_264742_ -> clone(
                                    p_264742_.getSource(),
                                    commandfunction.apply(p_264742_),
                                    commandfunction1.apply(p_264742_),
                                    commandfunction2.apply(p_264742_),
                                    FILTER_AIR,
                                    CloneCommands.Mode.NORMAL
                                )
                        )
                )
            )
            .then(
                Commands.literal("filtered")
                    .then(
                        wrapWithCloneMode(
                            commandfunction,
                            commandfunction1,
                            commandfunction2,
                            p_264745_ -> BlockPredicateArgument.getBlockPredicate(p_264745_, "filter"),
                            Commands.argument("filter", BlockPredicateArgument.blockPredicate(buildContext))
                                .executes(
                                    p_264733_ -> clone(
                                            p_264733_.getSource(),
                                            commandfunction.apply(p_264733_),
                                            commandfunction1.apply(p_264733_),
                                            commandfunction2.apply(p_264733_),
                                            BlockPredicateArgument.getBlockPredicate(p_264733_, "filter"),
                                            CloneCommands.Mode.NORMAL
                                        )
                                )
                        )
                    )
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> wrapWithCloneMode(
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> beginGetter,
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> endGetter,
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> targetGetter,
        CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, Predicate<BlockInWorld>> filterGetter,
        ArgumentBuilder<CommandSourceStack, ?> argumentBuilder
    ) {
        return argumentBuilder.then(
                Commands.literal("force")
                    .executes(
                        p_264773_ -> clone(
                                p_264773_.getSource(),
                                beginGetter.apply(p_264773_),
                                endGetter.apply(p_264773_),
                                targetGetter.apply(p_264773_),
                                filterGetter.apply(p_264773_),
                                CloneCommands.Mode.FORCE
                            )
                    )
            )
            .then(
                Commands.literal("move")
                    .executes(
                        p_264766_ -> clone(
                                p_264766_.getSource(),
                                beginGetter.apply(p_264766_),
                                endGetter.apply(p_264766_),
                                targetGetter.apply(p_264766_),
                                filterGetter.apply(p_264766_),
                                CloneCommands.Mode.MOVE
                            )
                    )
            )
            .then(
                Commands.literal("normal")
                    .executes(
                        p_264750_ -> clone(
                                p_264750_.getSource(),
                                beginGetter.apply(p_264750_),
                                endGetter.apply(p_264750_),
                                targetGetter.apply(p_264750_),
                                filterGetter.apply(p_264750_),
                                CloneCommands.Mode.NORMAL
                            )
                    )
            );
    }

    private static int clone(
        CommandSourceStack source,
        CloneCommands.DimensionAndPosition begin,
        CloneCommands.DimensionAndPosition end,
        CloneCommands.DimensionAndPosition target,
        Predicate<BlockInWorld> filter,
        CloneCommands.Mode mode
    ) throws CommandSyntaxException {
        BlockPos blockpos = begin.position();
        BlockPos blockpos1 = end.position();
        BoundingBox boundingbox = BoundingBox.fromCorners(blockpos, blockpos1);
        BlockPos blockpos2 = target.position();
        BlockPos blockpos3 = blockpos2.offset(boundingbox.getLength());
        BoundingBox boundingbox1 = BoundingBox.fromCorners(blockpos2, blockpos3);
        ServerLevel serverlevel = begin.dimension();
        ServerLevel serverlevel1 = target.dimension();
        if (!mode.canOverlap() && serverlevel == serverlevel1 && boundingbox1.intersects(boundingbox)) {
            throw ERROR_OVERLAP.create();
        } else {
            int i = boundingbox.getXSpan() * boundingbox.getYSpan() * boundingbox.getZSpan();
            int j = source.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT);
            if (i > j) {
                throw ERROR_AREA_TOO_LARGE.create(j, i);
            } else if (serverlevel.hasChunksAt(blockpos, blockpos1) && serverlevel1.hasChunksAt(blockpos2, blockpos3)) {
                List<CloneCommands.CloneBlockInfo> list = Lists.newArrayList();
                List<CloneCommands.CloneBlockInfo> list1 = Lists.newArrayList();
                List<CloneCommands.CloneBlockInfo> list2 = Lists.newArrayList();
                Deque<BlockPos> deque = Lists.newLinkedList();
                BlockPos blockpos4 = new BlockPos(
                    boundingbox1.minX() - boundingbox.minX(), boundingbox1.minY() - boundingbox.minY(), boundingbox1.minZ() - boundingbox.minZ()
                );

                for (int k = boundingbox.minZ(); k <= boundingbox.maxZ(); k++) {
                    for (int l = boundingbox.minY(); l <= boundingbox.maxY(); l++) {
                        for (int i1 = boundingbox.minX(); i1 <= boundingbox.maxX(); i1++) {
                            BlockPos blockpos5 = new BlockPos(i1, l, k);
                            BlockPos blockpos6 = blockpos5.offset(blockpos4);
                            BlockInWorld blockinworld = new BlockInWorld(serverlevel, blockpos5, false);
                            BlockState blockstate = blockinworld.getState();
                            if (filter.test(blockinworld)) {
                                BlockEntity blockentity = serverlevel.getBlockEntity(blockpos5);
                                if (blockentity != null) {
                                    CloneCommands.CloneBlockEntityInfo clonecommands$cloneblockentityinfo = new CloneCommands.CloneBlockEntityInfo(
                                        blockentity.saveCustomOnly(source.registryAccess()), blockentity.components()
                                    );
                                    list1.add(new CloneCommands.CloneBlockInfo(blockpos6, blockstate, clonecommands$cloneblockentityinfo));
                                    deque.addLast(blockpos5);
                                } else if (!blockstate.isSolidRender(serverlevel, blockpos5) && !blockstate.isCollisionShapeFullBlock(serverlevel, blockpos5)) {
                                    list2.add(new CloneCommands.CloneBlockInfo(blockpos6, blockstate, null));
                                    deque.addFirst(blockpos5);
                                } else {
                                    list.add(new CloneCommands.CloneBlockInfo(blockpos6, blockstate, null));
                                    deque.addLast(blockpos5);
                                }
                            }
                        }
                    }
                }

                if (mode == CloneCommands.Mode.MOVE) {
                    for (BlockPos blockpos7 : deque) {
                        BlockEntity blockentity1 = serverlevel.getBlockEntity(blockpos7);
                        Clearable.tryClear(blockentity1);
                        serverlevel.setBlock(blockpos7, Blocks.BARRIER.defaultBlockState(), 2);
                    }

                    for (BlockPos blockpos8 : deque) {
                        serverlevel.setBlock(blockpos8, Blocks.AIR.defaultBlockState(), 3);
                    }
                }

                List<CloneCommands.CloneBlockInfo> list3 = Lists.newArrayList();
                list3.addAll(list);
                list3.addAll(list1);
                list3.addAll(list2);
                List<CloneCommands.CloneBlockInfo> list4 = Lists.reverse(list3);

                for (CloneCommands.CloneBlockInfo clonecommands$cloneblockinfo : list4) {
                    BlockEntity blockentity2 = serverlevel1.getBlockEntity(clonecommands$cloneblockinfo.pos);
                    Clearable.tryClear(blockentity2);
                    serverlevel1.setBlock(clonecommands$cloneblockinfo.pos, Blocks.BARRIER.defaultBlockState(), 2);
                }

                int j1 = 0;

                for (CloneCommands.CloneBlockInfo clonecommands$cloneblockinfo1 : list3) {
                    if (serverlevel1.setBlock(clonecommands$cloneblockinfo1.pos, clonecommands$cloneblockinfo1.state, 2)) {
                        j1++;
                    }
                }

                for (CloneCommands.CloneBlockInfo clonecommands$cloneblockinfo2 : list1) {
                    BlockEntity blockentity3 = serverlevel1.getBlockEntity(clonecommands$cloneblockinfo2.pos);
                    if (clonecommands$cloneblockinfo2.blockEntityInfo != null && blockentity3 != null) {
                        blockentity3.loadCustomOnly(clonecommands$cloneblockinfo2.blockEntityInfo.tag, serverlevel1.registryAccess());
                        blockentity3.setComponents(clonecommands$cloneblockinfo2.blockEntityInfo.components);
                        blockentity3.setChanged();
                    }

                    serverlevel1.setBlock(clonecommands$cloneblockinfo2.pos, clonecommands$cloneblockinfo2.state, 2);
                }

                for (CloneCommands.CloneBlockInfo clonecommands$cloneblockinfo3 : list4) {
                    serverlevel1.blockUpdated(clonecommands$cloneblockinfo3.pos, clonecommands$cloneblockinfo3.state.getBlock());
                }

                serverlevel1.getBlockTicks().copyAreaFrom(serverlevel.getBlockTicks(), boundingbox, blockpos4);
                if (j1 == 0) {
                    throw ERROR_FAILED.create();
                } else {
                    int k1 = j1;
                    source.sendSuccess(() -> Component.translatable("commands.clone.success", k1), true);
                    return j1;
                }
            } else {
                throw BlockPosArgument.ERROR_NOT_LOADED.create();
            }
        }
    }

    static record CloneBlockEntityInfo(CompoundTag tag, DataComponentMap components) {
    }

    static record CloneBlockInfo(BlockPos pos, BlockState state, @Nullable CloneCommands.CloneBlockEntityInfo blockEntityInfo) {
    }

    @FunctionalInterface
    interface CommandFunction<T, R> {
        R apply(T input) throws CommandSyntaxException;
    }

    static record DimensionAndPosition(ServerLevel dimension, BlockPos position) {
    }

    static enum Mode {
        FORCE(true),
        MOVE(true),
        NORMAL(false);

        private final boolean canOverlap;

        private Mode(boolean canOverlap) {
            this.canOverlap = canOverlap;
        }

        public boolean canOverlap() {
            return this.canOverlap;
        }
    }
}
