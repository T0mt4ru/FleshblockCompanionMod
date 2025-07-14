package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;

public class ScoreboardSlotArgument implements ArgumentType<DisplaySlot> {
    private static final Collection<String> EXAMPLES = Arrays.asList("sidebar", "foo.bar");
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
        p_304112_ -> Component.translatableEscape("argument.scoreboardDisplaySlot.invalid", p_304112_)
    );

    private ScoreboardSlotArgument() {
    }

    public static ScoreboardSlotArgument displaySlot() {
        return new ScoreboardSlotArgument();
    }

    public static DisplaySlot getDisplaySlot(CommandContext<CommandSourceStack> context, String slot) {
        return context.getArgument(slot, DisplaySlot.class);
    }

    public DisplaySlot parse(StringReader reader) throws CommandSyntaxException {
        String s = reader.readUnquotedString();
        DisplaySlot displayslot = DisplaySlot.CODEC.byName(s);
        if (displayslot == null) {
            throw ERROR_INVALID_VALUE.createWithContext(reader, s);
        } else {
            return displayslot;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Arrays.stream(DisplaySlot.values()).map(DisplaySlot::getSerializedName), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
