package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class OpCommand {
    private static final SimpleCommandExceptionType ERROR_ALREADY_OP = new SimpleCommandExceptionType(Component.translatable("commands.op.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("op")
                .requires(p_138087_ -> p_138087_.hasPermission(3))
                .then(
                    Commands.argument("targets", GameProfileArgument.gameProfile())
                        .suggests(
                            (p_138084_, p_138085_) -> {
                                PlayerList playerlist = p_138084_.getSource().getServer().getPlayerList();
                                return SharedSuggestionProvider.suggest(
                                    playerlist.getPlayers()
                                        .stream()
                                        .filter(p_352693_ -> !playerlist.isOp(p_352693_.getGameProfile()))
                                        .map(p_352691_ -> p_352691_.getGameProfile().getName()),
                                    p_138085_
                                );
                            }
                        )
                        .executes(p_138082_ -> opPlayers(p_138082_.getSource(), GameProfileArgument.getGameProfiles(p_138082_, "targets")))
                )
        );
    }

    private static int opPlayers(CommandSourceStack source, Collection<GameProfile> gameProfiles) throws CommandSyntaxException {
        PlayerList playerlist = source.getServer().getPlayerList();
        int i = 0;

        for (GameProfile gameprofile : gameProfiles) {
            if (!playerlist.isOp(gameprofile)) {
                playerlist.op(gameprofile);
                i++;
                source.sendSuccess(() -> Component.translatable("commands.op.success", gameProfiles.iterator().next().getName()), true);
            }
        }

        if (i == 0) {
            throw ERROR_ALREADY_OP.create();
        } else {
            return i;
        }
    }
}
