package net.minecraft.util.parsing.packrat;

import java.util.stream.Stream;

public interface SuggestionSupplier<S> {
    Stream<String> possibleValues(ParseState<S> parseState);

    static <S> SuggestionSupplier<S> empty() {
        return p_335687_ -> Stream.empty();
    }
}
