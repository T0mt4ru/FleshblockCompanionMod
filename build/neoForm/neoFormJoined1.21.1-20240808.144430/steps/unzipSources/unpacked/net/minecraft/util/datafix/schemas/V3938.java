package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3938 extends NamespacedSchema {
    public V3938(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    protected static TypeTemplate abstractArrow(Schema schema) {
        return DSL.optionalFields(
            "inBlockState", References.BLOCK_STATE.in(schema), "item", References.ITEM_STACK.in(schema), "weapon", References.ITEM_STACK.in(schema)
        );
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        schema.register(map, "minecraft:spectral_arrow", () -> abstractArrow(schema));
        schema.register(map, "minecraft:arrow", () -> abstractArrow(schema));
        return map;
    }
}
