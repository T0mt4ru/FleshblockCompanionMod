package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Map;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class BannerEntityCustomNameToOverrideComponentFix extends DataFix {
    public BannerEntityCustomNameToOverrideComponentFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.BLOCK_ENTITY);
        TaggedChoiceType<?> taggedchoicetype = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
        OpticFinder<?> opticfinder = type.findField("components");
        return this.fixTypeEverywhereTyped("Banner entity custom_name to item_name component fix", type, p_338407_ -> {
            Object object = p_338407_.get(taggedchoicetype.finder()).getFirst();
            return object.equals("minecraft:banner") ? this.fix(p_338407_, opticfinder) : p_338407_;
        });
    }

    private Typed<?> fix(Typed<?> data, OpticFinder<?> finder) {
        Dynamic<?> dynamic = data.getOptional(DSL.remainderFinder()).orElseThrow();
        OptionalDynamic<?> optionaldynamic = dynamic.get("CustomName");
        boolean flag = optionaldynamic.asString()
            .result()
            .flatMap(ComponentDataFixUtils::extractTranslationString)
            .filter(p_338664_ -> p_338664_.equals("block.minecraft.ominous_banner"))
            .isPresent();
        if (flag) {
            Typed<?> typed = data.getOrCreateTyped(finder)
                .update(
                    DSL.remainderFinder(),
                    p_338230_ -> p_338230_.set("minecraft:item_name", optionaldynamic.result().get())
                            .set("minecraft:hide_additional_tooltip", p_338230_.createMap(Map.of()))
                );
            return data.set(finder, typed).set(DSL.remainderFinder(), dynamic.remove("CustomName"));
        } else {
            return data;
        }
    }
}
