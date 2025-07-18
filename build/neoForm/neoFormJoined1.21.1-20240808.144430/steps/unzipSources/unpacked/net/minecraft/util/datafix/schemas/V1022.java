package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1022 extends Schema {
    public V1022(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        schema.registerType(false, References.RECIPE, () -> DSL.constType(NamespacedSchema.namespacedString()));
        schema.registerType(
            false,
            References.PLAYER,
            () -> DSL.optionalFields(
                    Pair.of("RootVehicle", DSL.optionalFields("Entity", References.ENTITY_TREE.in(schema))),
                    Pair.of("Inventory", DSL.list(References.ITEM_STACK.in(schema))),
                    Pair.of("EnderItems", DSL.list(References.ITEM_STACK.in(schema))),
                    Pair.of("ShoulderEntityLeft", References.ENTITY_TREE.in(schema)),
                    Pair.of("ShoulderEntityRight", References.ENTITY_TREE.in(schema)),
                    Pair.of(
                        "recipeBook",
                        DSL.optionalFields("recipes", DSL.list(References.RECIPE.in(schema)), "toBeDisplayed", DSL.list(References.RECIPE.in(schema)))
                    )
                )
        );
        schema.registerType(false, References.HOTBAR, () -> DSL.compoundList(DSL.list(References.ITEM_STACK.in(schema))));
    }
}
