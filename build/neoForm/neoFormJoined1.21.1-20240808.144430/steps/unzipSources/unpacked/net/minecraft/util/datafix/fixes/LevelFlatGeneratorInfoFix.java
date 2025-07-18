package net.minecraft.util.datafix.fixes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.math.NumberUtils;

public class LevelFlatGeneratorInfoFix extends DataFix {
    private static final String GENERATOR_OPTIONS = "generatorOptions";
    @VisibleForTesting
    static final String DEFAULT = "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
    private static final Splitter SPLITTER = Splitter.on(';').limit(5);
    private static final Splitter LAYER_SPLITTER = Splitter.on(',');
    private static final Splitter OLD_AMOUNT_SPLITTER = Splitter.on('x').limit(2);
    private static final Splitter AMOUNT_SPLITTER = Splitter.on('*').limit(2);
    private static final Splitter BLOCK_SPLITTER = Splitter.on(':').limit(3);

    public LevelFlatGeneratorInfoFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "LevelFlatGeneratorInfoFix", this.getInputSchema().getType(References.LEVEL), p_16351_ -> p_16351_.update(DSL.remainderFinder(), this::fix)
        );
    }

    private Dynamic<?> fix(Dynamic<?> dynamic) {
        return dynamic.get("generatorName").asString("").equalsIgnoreCase("flat")
            ? dynamic.update(
                "generatorOptions",
                p_337644_ -> DataFixUtils.orElse(p_337644_.asString().map(this::fixString).map(p_337644_::createString).result(), p_337644_)
            )
            : dynamic;
    }

    @VisibleForTesting
    String fixString(String string) {
        if (string.isEmpty()) {
            return "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
        } else {
            Iterator<String> iterator = SPLITTER.split(string).iterator();
            String s = iterator.next();
            int i;
            String s1;
            if (iterator.hasNext()) {
                i = NumberUtils.toInt(s, 0);
                s1 = iterator.next();
            } else {
                i = 0;
                s1 = s;
            }

            if (i >= 0 && i <= 3) {
                StringBuilder stringbuilder = new StringBuilder();
                Splitter splitter = i < 3 ? OLD_AMOUNT_SPLITTER : AMOUNT_SPLITTER;
                stringbuilder.append(StreamSupport.stream(LAYER_SPLITTER.split(s1).spliterator(), false).map(p_16349_ -> {
                    List<String> list = splitter.splitToList(p_16349_);
                    int j;
                    String s2;
                    if (list.size() == 2) {
                        j = NumberUtils.toInt(list.get(0));
                        s2 = list.get(1);
                    } else {
                        j = 1;
                        s2 = list.get(0);
                    }

                    List<String> list1 = BLOCK_SPLITTER.splitToList(s2);
                    int k = list1.get(0).equals("minecraft") ? 1 : 0;
                    String s3 = list1.get(k);
                    int l = i == 3 ? EntityBlockStateFix.getBlockId("minecraft:" + s3) : NumberUtils.toInt(s3, 0);
                    int i1 = k + 1;
                    int j1 = list1.size() > i1 ? NumberUtils.toInt(list1.get(i1), 0) : 0;
                    return (j == 1 ? "" : j + "*") + BlockStateData.getTag(l << 4 | j1).get("Name").asString("");
                }).collect(Collectors.joining(",")));

                while (iterator.hasNext()) {
                    stringbuilder.append(';').append(iterator.next());
                }

                return stringbuilder.toString();
            } else {
                return "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
            }
        }
    }
}
