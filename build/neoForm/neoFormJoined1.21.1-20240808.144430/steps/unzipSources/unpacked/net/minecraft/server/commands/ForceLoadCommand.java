package net.minecraft.server.commands;

import com.google.common.base.Joiner;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ForceLoadCommand {
    private static final int MAX_CHUNK_LIMIT = 256;
    private static final Dynamic2CommandExceptionType ERROR_TOO_MANY_CHUNKS = new Dynamic2CommandExceptionType(
        (p_304237_, p_304238_) -> Component.translatableEscape("commands.forceload.toobig", p_304237_, p_304238_)
    );
    private static final Dynamic2CommandExceptionType ERROR_NOT_TICKING = new Dynamic2CommandExceptionType(
        (p_304235_, p_304236_) -> Component.translatableEscape("commands.forceload.query.failure", p_304235_, p_304236_)
    );
    private static final SimpleCommandExceptionType ERROR_ALL_ADDED = new SimpleCommandExceptionType(Component.translatable("commands.forceload.added.failure"));
    private static final SimpleCommandExceptionType ERROR_NONE_REMOVED = new SimpleCommandExceptionType(
        Component.translatable("commands.forceload.removed.failure")
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("forceload")
                .requires(p_137703_ -> p_137703_.hasPermission(2))
                .then(
                    Commands.literal("add")
                        .then(
                            Commands.argument("from", ColumnPosArgument.columnPos())
                                .executes(
                                    p_137711_ -> changeForceLoad(
                                            p_137711_.getSource(),
                                            ColumnPosArgument.getColumnPos(p_137711_, "from"),
                                            ColumnPosArgument.getColumnPos(p_137711_, "from"),
                                            true
                                        )
                                )
                                .then(
                                    Commands.argument("to", ColumnPosArgument.columnPos())
                                        .executes(
                                            p_137709_ -> changeForceLoad(
                                                    p_137709_.getSource(),
                                                    ColumnPosArgument.getColumnPos(p_137709_, "from"),
                                                    ColumnPosArgument.getColumnPos(p_137709_, "to"),
                                                    true
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("remove")
                        .then(
                            Commands.argument("from", ColumnPosArgument.columnPos())
                                .executes(
                                    p_137707_ -> changeForceLoad(
                                            p_137707_.getSource(),
                                            ColumnPosArgument.getColumnPos(p_137707_, "from"),
                                            ColumnPosArgument.getColumnPos(p_137707_, "from"),
                                            false
                                        )
                                )
                                .then(
                                    Commands.argument("to", ColumnPosArgument.columnPos())
                                        .executes(
                                            p_137705_ -> changeForceLoad(
                                                    p_137705_.getSource(),
                                                    ColumnPosArgument.getColumnPos(p_137705_, "from"),
                                                    ColumnPosArgument.getColumnPos(p_137705_, "to"),
                                                    false
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("all").executes(p_137701_ -> removeAll(p_137701_.getSource())))
                )
                .then(
                    Commands.literal("query")
                        .executes(p_137694_ -> listForceLoad(p_137694_.getSource()))
                        .then(
                            Commands.argument("pos", ColumnPosArgument.columnPos())
                                .executes(p_137679_ -> queryForceLoad(p_137679_.getSource(), ColumnPosArgument.getColumnPos(p_137679_, "pos")))
                        )
                )
        );
    }

    private static int queryForceLoad(CommandSourceStack source, ColumnPos pos) throws CommandSyntaxException {
        ChunkPos chunkpos = pos.toChunkPos();
        ServerLevel serverlevel = source.getLevel();
        ResourceKey<Level> resourcekey = serverlevel.dimension();
        boolean flag = serverlevel.getForcedChunks().contains(chunkpos.toLong());
        if (flag) {
            source.sendSuccess(
                () -> Component.translatable(
                        "commands.forceload.query.success", Component.translationArg(chunkpos), serverlevel.getDescription() // Neo: Use dimension translation, if one exists
                    ),
                false
            );
            return 1;
        } else {
            throw ERROR_NOT_TICKING.create(chunkpos, serverlevel.getDescription()); // Neo: Use dimension translation, if one exists
        }
    }

    private static int listForceLoad(CommandSourceStack source) {
        ServerLevel serverlevel = source.getLevel();
        ResourceKey<Level> resourcekey = serverlevel.dimension();
        LongSet longset = serverlevel.getForcedChunks();
        int i = longset.size();
        if (i > 0) {
            String s = Joiner.on(", ").join(longset.stream().sorted().map(ChunkPos::new).map(ChunkPos::toString).iterator());
            if (i == 1) {
                source.sendSuccess(
                    () -> Component.translatable("commands.forceload.list.single", serverlevel.getDescription(), s), false // Neo: Use dimension translation, if one exists
                );
            } else {
                source.sendSuccess(
                    () -> Component.translatable("commands.forceload.list.multiple", i, serverlevel.getDescription(), s), false // Neo: Use dimension translation, if one exists
                );
            }
        } else {
            source.sendFailure(Component.translatable("commands.forceload.added.none", serverlevel.getDescription())); // Neo: Use dimension translation, if one exists
        }

        return i;
    }

    private static int removeAll(CommandSourceStack source) {
        ServerLevel serverlevel = source.getLevel();
        ResourceKey<Level> resourcekey = serverlevel.dimension();
        LongSet longset = serverlevel.getForcedChunks();
        longset.forEach(p_137675_ -> serverlevel.setChunkForced(ChunkPos.getX(p_137675_), ChunkPos.getZ(p_137675_), false));
        source.sendSuccess(() -> Component.translatable("commands.forceload.removed.all", serverlevel.getDescription()), true); // Neo: Use dimension translation, if one exists
        return 0;
    }

    private static int changeForceLoad(CommandSourceStack source, ColumnPos from, ColumnPos to, boolean add) throws CommandSyntaxException {
        int i = Math.min(from.x(), to.x());
        int j = Math.min(from.z(), to.z());
        int k = Math.max(from.x(), to.x());
        int l = Math.max(from.z(), to.z());
        if (i >= -30000000 && j >= -30000000 && k < 30000000 && l < 30000000) {
            int i1 = SectionPos.blockToSectionCoord(i);
            int j1 = SectionPos.blockToSectionCoord(j);
            int k1 = SectionPos.blockToSectionCoord(k);
            int l1 = SectionPos.blockToSectionCoord(l);
            long i2 = ((long)(k1 - i1) + 1L) * ((long)(l1 - j1) + 1L);
            if (i2 > 256L) {
                throw ERROR_TOO_MANY_CHUNKS.create(256, i2);
            } else {
                ServerLevel serverlevel = source.getLevel();
                ResourceKey<Level> resourcekey = serverlevel.dimension();
                ChunkPos chunkpos = null;
                int j2 = 0;

                for (int k2 = i1; k2 <= k1; k2++) {
                    for (int l2 = j1; l2 <= l1; l2++) {
                        boolean flag = serverlevel.setChunkForced(k2, l2, add);
                        if (flag) {
                            j2++;
                            if (chunkpos == null) {
                                chunkpos = new ChunkPos(k2, l2);
                            }
                        }
                    }
                }

                ChunkPos chunkpos2 = chunkpos;
                int i3 = j2;
                if (i3 == 0) {
                    throw (add ? ERROR_ALL_ADDED : ERROR_NONE_REMOVED).create();
                } else {
                    if (i3 == 1) {
                        source.sendSuccess(
                            () -> Component.translatable(
                                    "commands.forceload." + (add ? "added" : "removed") + ".single",
                                    Component.translationArg(chunkpos2),
                                    serverlevel.getDescription() // Neo: Use dimension translation, if one exists
                                ),
                            true
                        );
                    } else {
                        ChunkPos chunkpos3 = new ChunkPos(i1, j1);
                        ChunkPos chunkpos1 = new ChunkPos(k1, l1);
                        source.sendSuccess(
                            () -> Component.translatable(
                                    "commands.forceload." + (add ? "added" : "removed") + ".multiple",
                                    i3,
                                    Component.translationArg(serverlevel.getDescription()), // Neo: Use dimension translation, if one exists
                                    Component.translationArg(chunkpos3),
                                    Component.translationArg(chunkpos1)
                                ),
                            true
                        );
                    }

                    return i3;
                }
            }
        } else {
            throw BlockPosArgument.ERROR_OUT_OF_WORLD.create();
        }
    }
}
