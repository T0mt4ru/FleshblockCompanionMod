package net.minecraft.network.chat.contents;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

public class KeybindResolver {
    static Function<String, Supplier<Component>> keyResolver = p_237363_ -> () -> Component.literal(p_237363_);

    public static void setKeyResolver(Function<String, Supplier<Component>> p_keyResolver) {
        keyResolver = p_keyResolver;
    }
}
