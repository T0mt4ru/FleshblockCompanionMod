package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class OptionsKeyTranslationFix extends DataFix {
    public OptionsKeyTranslationFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "OptionsKeyTranslationFix",
            this.getInputSchema().getType(References.OPTIONS),
            p_16648_ -> p_16648_.update(
                    DSL.remainderFinder(),
                    p_337655_ -> p_337655_.getMapValues().map(p_145588_ -> p_337655_.createMap(p_145588_.entrySet().stream().map(p_145585_ -> {
                                if (p_145585_.getKey().asString("").startsWith("key_")) {
                                    String s = p_145585_.getValue().asString("");
                                    if (!s.startsWith("key.mouse") && !s.startsWith("scancode.")) {
                                        return Pair.of(p_145585_.getKey(), p_337655_.createString("key.keyboard." + s.substring("key.".length())));
                                    }
                                }

                                return Pair.of(p_145585_.getKey(), p_145585_.getValue());
                            }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)))).result().orElse((Dynamic)p_337655_)
                )
        );
    }
}
