package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ChunkBedBlockEntityInjecterFix extends DataFix {
    public ChunkBedBlockEntityInjecterFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getOutputSchema().getType(References.CHUNK);
        Type<?> type1 = type.findFieldType("Level");
        if (!(type1.findFieldType("TileEntities") instanceof ListType<?> listtype)) {
            throw new IllegalStateException("Tile entity type is not a list type.");
        } else {
            return this.cap(type1, listtype);
        }
    }

    private <TE> TypeRewriteRule cap(Type<?> levelType, ListType<TE> tileEntityTypes) {
        Type<TE> type = tileEntityTypes.getElement();
        OpticFinder<?> opticfinder = DSL.fieldFinder("Level", levelType);
        OpticFinder<List<TE>> opticfinder1 = DSL.fieldFinder("TileEntities", tileEntityTypes);
        int i = 416;
        return TypeRewriteRule.seq(
            this.fixTypeEverywhere(
                "InjectBedBlockEntityType",
                (com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType<String>)this.getInputSchema().findChoiceType(References.BLOCK_ENTITY),
                (com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType<String>)this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY),
                p_184841_ -> p_184837_ -> p_184837_
            ),
            this.fixTypeEverywhereTyped(
                "BedBlockEntityInjecter",
                this.getOutputSchema().getType(References.CHUNK),
                p_297967_ -> {
                    Typed<?> typed = p_297967_.getTyped(opticfinder);
                    Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
                    int j = dynamic.get("xPos").asInt(0);
                    int k = dynamic.get("zPos").asInt(0);
                    List<TE> list = Lists.newArrayList(typed.getOrCreate(opticfinder1));

                    for (Dynamic<?> dynamic1 : dynamic.get("Sections").asList(Function.identity())) {
                        int l = dynamic1.get("Y").asInt(0);
                        Streams.mapWithIndex(dynamic1.get("Blocks").asIntStream(), (p_274917_, p_274918_) -> {
                                if (416 == (p_274917_ & 0xFF) << 4) {
                                    int i1 = (int)p_274918_;
                                    int j1 = i1 & 15;
                                    int k1 = i1 >> 8 & 15;
                                    int l1 = i1 >> 4 & 15;
                                    Map<Dynamic<?>, Dynamic<?>> map = Maps.newHashMap();
                                    map.put(dynamic1.createString("id"), dynamic1.createString("minecraft:bed"));
                                    map.put(dynamic1.createString("x"), dynamic1.createInt(j1 + (j << 4)));
                                    map.put(dynamic1.createString("y"), dynamic1.createInt(k1 + (l << 4)));
                                    map.put(dynamic1.createString("z"), dynamic1.createInt(l1 + (k << 4)));
                                    map.put(dynamic1.createString("color"), dynamic1.createShort((short)14));
                                    return map;
                                } else {
                                    return null;
                                }
                            })
                            .forEachOrdered(
                                p_337607_ -> {
                                    if (p_337607_ != null) {
                                        list.add(
                                            type.read(dynamic1.createMap((Map<? extends Dynamic<?>, ? extends Dynamic<?>>)p_337607_))
                                                .result()
                                                .orElseThrow(() -> new IllegalStateException("Could not parse newly created bed block entity."))
                                                .getFirst()
                                        );
                                    }
                                }
                            );
                    }

                    return !list.isEmpty() ? p_297967_.set(opticfinder, typed.set(opticfinder1, list)) : p_297967_;
                }
            )
        );
    }
}
