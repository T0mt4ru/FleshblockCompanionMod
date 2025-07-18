package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

public class PublishCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.publish.failed"));
    private static final DynamicCommandExceptionType ERROR_ALREADY_PUBLISHED = new DynamicCommandExceptionType(
        p_304281_ -> Component.translatableEscape("commands.publish.alreadyPublished", p_304281_)
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("publish")
                .requires(p_138189_ -> p_138189_.hasPermission(4))
                .executes(p_258235_ -> publish(p_258235_.getSource(), HttpUtil.getAvailablePort(), false, null))
                .then(
                    Commands.argument("allowCommands", BoolArgumentType.bool())
                        .executes(
                            p_258236_ -> publish(p_258236_.getSource(), HttpUtil.getAvailablePort(), BoolArgumentType.getBool(p_258236_, "allowCommands"), null)
                        )
                        .then(
                            Commands.argument("gamemode", GameModeArgument.gameMode())
                                .executes(
                                    p_258237_ -> publish(
                                            p_258237_.getSource(),
                                            HttpUtil.getAvailablePort(),
                                            BoolArgumentType.getBool(p_258237_, "allowCommands"),
                                            GameModeArgument.getGameMode(p_258237_, "gamemode")
                                        )
                                )
                                .then(
                                    Commands.argument("port", IntegerArgumentType.integer(0, 65535))
                                        .executes(
                                            p_258238_ -> publish(
                                                    p_258238_.getSource(),
                                                    IntegerArgumentType.getInteger(p_258238_, "port"),
                                                    BoolArgumentType.getBool(p_258238_, "allowCommands"),
                                                    GameModeArgument.getGameMode(p_258238_, "gamemode")
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int publish(CommandSourceStack source, int port, boolean cheats, @Nullable GameType gameMode) throws CommandSyntaxException {
        if (source.getServer().isPublished()) {
            throw ERROR_ALREADY_PUBLISHED.create(source.getServer().getPort());
        } else if (!source.getServer().publishServer(gameMode, cheats, port)) {
            throw ERROR_FAILED.create();
        } else {
            source.sendSuccess(() -> getSuccessMessage(port), true);
            return port;
        }
    }

    public static MutableComponent getSuccessMessage(int port) {
        Component component = ComponentUtils.copyOnClickText(String.valueOf(port));
        return Component.translatable("commands.publish.started", component);
    }
}
