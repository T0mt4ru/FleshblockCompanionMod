package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class HeightmapRenamingFix extends DataFix {
    public HeightmapRenamingFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> opticfinder = type.findField("Level");
        return this.fixTypeEverywhereTyped(
            "HeightmapRenamingFix", type, p_15895_ -> p_15895_.updateTyped(opticfinder, p_145380_ -> p_145380_.update(DSL.remainderFinder(), this::fix))
        );
    }

    private Dynamic<?> fix(Dynamic<?> p_dynamic) {
        Optional<? extends Dynamic<?>> optional = p_dynamic.get("Heightmaps").result();
        if (optional.isEmpty()) {
            return p_dynamic;
        } else {
            Dynamic<?> dynamic = (Dynamic<?>)optional.get();
            Optional<? extends Dynamic<?>> optional1 = dynamic.get("LIQUID").result();
            if (optional1.isPresent()) {
                dynamic = dynamic.remove("LIQUID");
                dynamic = dynamic.set("WORLD_SURFACE_WG", (Dynamic<?>)optional1.get());
            }

            Optional<? extends Dynamic<?>> optional2 = dynamic.get("SOLID").result();
            if (optional2.isPresent()) {
                dynamic = dynamic.remove("SOLID");
                dynamic = dynamic.set("OCEAN_FLOOR_WG", (Dynamic<?>)optional2.get());
                dynamic = dynamic.set("OCEAN_FLOOR", (Dynamic<?>)optional2.get());
            }

            Optional<? extends Dynamic<?>> optional3 = dynamic.get("LIGHT").result();
            if (optional3.isPresent()) {
                dynamic = dynamic.remove("LIGHT");
                dynamic = dynamic.set("LIGHT_BLOCKING", (Dynamic<?>)optional3.get());
            }

            Optional<? extends Dynamic<?>> optional4 = dynamic.get("RAIN").result();
            if (optional4.isPresent()) {
                dynamic = dynamic.remove("RAIN");
                dynamic = dynamic.set("MOTION_BLOCKING", (Dynamic<?>)optional4.get());
                dynamic = dynamic.set("MOTION_BLOCKING_NO_LEAVES", (Dynamic<?>)optional4.get());
            }

            return p_dynamic.set("Heightmaps", dynamic);
        }
    }
}
