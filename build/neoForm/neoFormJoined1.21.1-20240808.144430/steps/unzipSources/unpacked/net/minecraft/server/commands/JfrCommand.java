package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;

public class JfrCommand {
    private static final SimpleCommandExceptionType START_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.jfr.start.failed"));
    private static final DynamicCommandExceptionType DUMP_FAILED = new DynamicCommandExceptionType(
        p_304256_ -> Component.translatableEscape("commands.jfr.dump.failed", p_304256_)
    );

    private JfrCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("jfr")
                .requires(p_183661_ -> p_183661_.hasPermission(4))
                .then(Commands.literal("start").executes(p_183657_ -> startJfr(p_183657_.getSource())))
                .then(Commands.literal("stop").executes(p_183648_ -> stopJfr(p_183648_.getSource())))
        );
    }

    private static int startJfr(CommandSourceStack source) throws CommandSyntaxException {
        Environment environment = Environment.from(source.getServer());
        if (!JvmProfiler.INSTANCE.start(environment)) {
            throw START_FAILED.create();
        } else {
            source.sendSuccess(() -> Component.translatable("commands.jfr.started"), false);
            return 1;
        }
    }

    private static int stopJfr(CommandSourceStack source) throws CommandSyntaxException {
        try {
            Path path = Paths.get(".").relativize(JvmProfiler.INSTANCE.stop().normalize());
            Path path1 = source.getServer().isPublished() && !SharedConstants.IS_RUNNING_IN_IDE ? path : path.toAbsolutePath();
            Component component = Component.literal(path.toString())
                .withStyle(ChatFormatting.UNDERLINE)
                .withStyle(
                    p_183655_ -> p_183655_.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, path1.toString()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")))
                );
            source.sendSuccess(() -> Component.translatable("commands.jfr.stopped", component), false);
            return 1;
        } catch (Throwable throwable) {
            throw DUMP_FAILED.create(throwable.getMessage());
        }
    }
}
