package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class SwizzleArgument implements ArgumentType<EnumSet<Direction.Axis>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("xyz", "x");
    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("arguments.swizzle.invalid"));

    public static SwizzleArgument swizzle() {
        return new SwizzleArgument();
    }

    public static EnumSet<Direction.Axis> getSwizzle(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, EnumSet.class);
    }

    public EnumSet<Direction.Axis> parse(StringReader reader) throws CommandSyntaxException {
        EnumSet<Direction.Axis> enumset = EnumSet.noneOf(Direction.Axis.class);

        while (reader.canRead() && reader.peek() != ' ') {
            char c0 = reader.read();

            Direction.Axis $$6 = switch (c0) {
                case 'x' -> Direction.Axis.X;
                case 'y' -> Direction.Axis.Y;
                case 'z' -> Direction.Axis.Z;
                default -> throw ERROR_INVALID.createWithContext(reader);
            };
            if (enumset.contains($$6)) {
                throw ERROR_INVALID.createWithContext(reader);
            }

            enumset.add($$6);
        }

        return enumset;
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
