package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class BlockEntityShulkerBoxColorFix extends NamedEntityFix {
    public BlockEntityShulkerBoxColorFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "BlockEntityShulkerBoxColorFix", References.BLOCK_ENTITY, "minecraft:shulker_box");
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), p_14860_ -> p_14860_.remove("Color"));
    }
}
