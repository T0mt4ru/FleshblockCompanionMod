package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

public class ChunkLightRemoveFix extends DataFix {
    public ChunkLightRemoveFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.CHUNK);
        Type<?> type1 = type.findFieldType("Level");
        OpticFinder<?> opticfinder = DSL.fieldFinder("Level", type1);
        return this.fixTypeEverywhereTyped(
            "ChunkLightRemoveFix",
            type,
            this.getOutputSchema().getType(References.CHUNK),
            p_15029_ -> p_15029_.updateTyped(opticfinder, p_145208_ -> p_145208_.update(DSL.remainderFinder(), p_145210_ -> p_145210_.remove("isLightOn")))
        );
    }
}
