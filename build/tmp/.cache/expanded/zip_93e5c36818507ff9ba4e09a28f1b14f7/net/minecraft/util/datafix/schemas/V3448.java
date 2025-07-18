package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3448 extends NamespacedSchema {
    public V3448(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        schema.register(
            map,
            "minecraft:decorated_pot",
            () -> DSL.optionalFields("sherds", DSL.list(References.ITEM_NAME.in(schema)), "item", References.ITEM_STACK.in(schema))
        );
        return map;
    }
}
