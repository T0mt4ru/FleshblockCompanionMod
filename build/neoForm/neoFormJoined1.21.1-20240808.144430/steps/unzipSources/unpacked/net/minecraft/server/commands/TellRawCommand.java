package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;

public class TellRawCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
            Commands.literal("tellraw")
                .requires(p_139068_ -> p_139068_.hasPermission(2))
                .then(
                    Commands.argument("targets", EntityArgument.players())
                        .then(
                            Commands.argument("message", ComponentArgument.textComponent(context))
                                .executes(
                                    p_139066_ -> {
                                        int i = 0;

                                        for (ServerPlayer serverplayer : EntityArgument.getPlayers(p_139066_, "targets")) {
                                            serverplayer.sendSystemMessage(
                                                ComponentUtils.updateForEntity(
                                                    p_139066_.getSource(), ComponentArgument.getComponent(p_139066_, "message"), serverplayer, 0
                                                ),
                                                false
                                            );
                                            i++;
                                        }

                                        return i;
                                    }
                                )
                        )
                )
        );
    }
}
