package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class RedstoneWireConnectionsFix extends DataFix {
    public RedstoneWireConnectionsFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        return this.fixTypeEverywhereTyped(
            "RedstoneConnectionsFix",
            schema.getType(References.BLOCK_STATE),
            p_16751_ -> p_16751_.update(DSL.remainderFinder(), this::updateRedstoneConnections)
        );
    }

    private <T> Dynamic<T> updateRedstoneConnections(Dynamic<T> dynamic) {
        boolean flag = dynamic.get("Name").asString().result().filter("minecraft:redstone_wire"::equals).isPresent();
        return !flag
            ? dynamic
            : dynamic.update(
                "Properties",
                p_16760_ -> {
                    String s = p_16760_.get("east").asString("none");
                    String s1 = p_16760_.get("west").asString("none");
                    String s2 = p_16760_.get("north").asString("none");
                    String s3 = p_16760_.get("south").asString("none");
                    boolean flag1 = isConnected(s) || isConnected(s1);
                    boolean flag2 = isConnected(s2) || isConnected(s3);
                    String s4 = !isConnected(s) && !flag2 ? "side" : s;
                    String s5 = !isConnected(s1) && !flag2 ? "side" : s1;
                    String s6 = !isConnected(s2) && !flag1 ? "side" : s2;
                    String s7 = !isConnected(s3) && !flag1 ? "side" : s3;
                    return p_16760_.update("east", p_145627_ -> p_145627_.createString(s4))
                        .update("west", p_145624_ -> p_145624_.createString(s5))
                        .update("north", p_145621_ -> p_145621_.createString(s6))
                        .update("south", p_145618_ -> p_145618_.createString(s7));
                }
            );
    }

    private static boolean isConnected(String state) {
        return !"none".equals(state);
    }
}
