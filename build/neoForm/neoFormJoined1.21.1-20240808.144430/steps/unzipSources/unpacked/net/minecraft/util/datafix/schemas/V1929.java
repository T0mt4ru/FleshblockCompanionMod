package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1929 extends NamespacedSchema {
    public V1929(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        schema.register(
            map,
            "minecraft:wandering_trader",
            p_340691_ -> DSL.optionalFields(
                    "Inventory",
                    DSL.list(References.ITEM_STACK.in(schema)),
                    "Offers",
                    DSL.optionalFields("Recipes", DSL.list(References.VILLAGER_TRADE.in(schema))),
                    V100.equipment(schema)
                )
        );
        schema.register(
            map,
            "minecraft:trader_llama",
            p_17815_ -> DSL.optionalFields(
                    "Items",
                    DSL.list(References.ITEM_STACK.in(schema)),
                    "SaddleItem",
                    References.ITEM_STACK.in(schema),
                    "DecorItem",
                    References.ITEM_STACK.in(schema),
                    V100.equipment(schema)
                )
        );
        return map;
    }
}
