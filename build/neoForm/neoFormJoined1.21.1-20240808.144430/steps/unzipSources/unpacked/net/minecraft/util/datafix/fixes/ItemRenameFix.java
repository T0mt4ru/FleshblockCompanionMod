package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class ItemRenameFix extends DataFix {
    private final String name;

    public ItemRenameFix(Schema outputSchema, String name) {
        super(outputSchema, false);
        this.name = name;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<Pair<String, String>> type = DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString());
        if (!Objects.equals(this.getInputSchema().getType(References.ITEM_NAME), type)) {
            throw new IllegalStateException("item name type is not what was expected.");
        } else {
            return this.fixTypeEverywhere(this.name, type, p_16010_ -> p_145402_ -> p_145402_.mapSecond(this::fixItem));
        }
    }

    protected abstract String fixItem(String item);

    public static DataFix create(Schema p_outputSchema, String p_name, final Function<String, String> fixer) {
        return new ItemRenameFix(p_outputSchema, p_name) {
            @Override
            protected String fixItem(String p_16019_) {
                return fixer.apply(p_16019_);
            }
        };
    }
}
