package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;

public class LevelLegacyWorldGenSettingsFix extends DataFix {
    private static final String WORLD_GEN_SETTINGS = "WorldGenSettings";
    private static final List<String> OLD_SETTINGS_KEYS = List.of(
        "RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest"
    );

    public LevelLegacyWorldGenSettingsFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "LevelLegacyWorldGenSettingsFix",
            this.getInputSchema().getType(References.LEVEL),
            p_307516_ -> p_307516_.update(DSL.remainderFinder(), p_307487_ -> {
                    Dynamic<?> dynamic = p_307487_.get("WorldGenSettings").orElseEmptyMap();

                    for (String s : OLD_SETTINGS_KEYS) {
                        Optional<? extends Dynamic<?>> optional = p_307487_.get(s).result();
                        if (optional.isPresent()) {
                            p_307487_ = p_307487_.remove(s);
                            dynamic = dynamic.set(s, (Dynamic<?>)optional.get());
                        }
                    }

                    return p_307487_.set("WorldGenSettings", dynamic);
                })
        );
    }
}
