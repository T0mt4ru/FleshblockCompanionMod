package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class TeamDisplayNameFix extends DataFix {
    public TeamDisplayNameFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Dynamic<?>>> type = DSL.named(References.TEAM.typeName(), DSL.remainderType());
        if (!Objects.equals(type, this.getInputSchema().getType(References.TEAM))) {
            throw new IllegalStateException("Team type is not what was expected.");
        } else {
            return this.fixTypeEverywhere(
                "TeamDisplayNameFix",
                type,
                p_17011_ -> p_145726_ -> p_145726_.mapSecond(p_304340_ -> p_304340_.update("DisplayName", ComponentDataFixUtils::wrapLiteralStringAsComponent))
            );
        }
    }
}
