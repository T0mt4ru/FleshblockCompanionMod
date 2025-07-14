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
import net.minecraft.commands.ParserUtils;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;

public class SlotArgument implements ArgumentType<Integer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("container.5", "weapon");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_SLOT = new DynamicCommandExceptionType(
        p_304113_ -> Component.translatableEscape("slot.unknown", p_304113_)
    );
    private static final DynamicCommandExceptionType ERROR_ONLY_SINGLE_SLOT_ALLOWED = new DynamicCommandExceptionType(
        p_332551_ -> Component.translatableEscape("slot.only_single_allowed", p_332551_)
    );

    public static SlotArgument slot() {
        return new SlotArgument();
    }

    public static int getSlot(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    public Integer parse(StringReader reader) throws CommandSyntaxException {
        String s = ParserUtils.readWhile(reader, p_332550_ -> p_332550_ != ' ');
        SlotRange slotrange = SlotRanges.nameToIds(s);
        if (slotrange == null) {
            throw ERROR_UNKNOWN_SLOT.createWithContext(reader, s);
        } else if (slotrange.size() != 1) {
            throw ERROR_ONLY_SINGLE_SLOT_ALLOWED.createWithContext(reader, s);
        } else {
            return slotrange.slots().getInt(0);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(SlotRanges.singleSlotNames(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
