package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3325 extends NamespacedSchema {
    public V3325(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        schema.register(map, "minecraft:item_display", p_270589_ -> DSL.optionalFields("item", References.ITEM_STACK.in(schema)));
        schema.register(map, "minecraft:block_display", p_270174_ -> DSL.optionalFields("block_state", References.BLOCK_STATE.in(schema)));
        schema.registerSimple(map, "minecraft:text_display");
        return map;
    }
}
