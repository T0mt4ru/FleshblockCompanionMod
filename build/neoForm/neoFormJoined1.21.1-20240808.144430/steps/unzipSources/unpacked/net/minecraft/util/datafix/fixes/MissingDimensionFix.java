package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FieldFinder;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList.CompoundListType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class MissingDimensionFix extends DataFix {
    public MissingDimensionFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    protected static <A> Type<Pair<A, Dynamic<?>>> fields(String name, Type<A> element) {
        return DSL.and(DSL.field(name, element), DSL.remainderType());
    }

    protected static <A> Type<Pair<Either<A, Unit>, Dynamic<?>>> optionalFields(String name, Type<A> element) {
        return DSL.and(DSL.optional(DSL.field(name, element)), DSL.remainderType());
    }

    protected static <A1, A2> Type<Pair<Either<A1, Unit>, Pair<Either<A2, Unit>, Dynamic<?>>>> optionalFields(
        String name1, Type<A1> element1, String name2, Type<A2> element2
    ) {
        return DSL.and(DSL.optional(DSL.field(name1, element1)), DSL.optional(DSL.field(name2, element2)), DSL.remainderType());
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        Type<?> type = DSL.taggedChoiceType(
            "type",
            DSL.string(),
            ImmutableMap.of(
                "minecraft:debug",
                DSL.remainderType(),
                "minecraft:flat",
                flatType(schema),
                "minecraft:noise",
                optionalFields(
                    "biome_source",
                    DSL.taggedChoiceType(
                        "type",
                        DSL.string(),
                        ImmutableMap.of(
                            "minecraft:fixed",
                            fields("biome", schema.getType(References.BIOME)),
                            "minecraft:multi_noise",
                            DSL.list(fields("biome", schema.getType(References.BIOME))),
                            "minecraft:checkerboard",
                            fields("biomes", DSL.list(schema.getType(References.BIOME))),
                            "minecraft:vanilla_layered",
                            DSL.remainderType(),
                            "minecraft:the_end",
                            DSL.remainderType()
                        )
                    ),
                    "settings",
                    DSL.or(
                        DSL.string(),
                        optionalFields("default_block", schema.getType(References.BLOCK_NAME), "default_fluid", schema.getType(References.BLOCK_NAME))
                    )
                )
            )
        );
        CompoundListType<String, ?> compoundlisttype = DSL.compoundList(NamespacedSchema.namespacedString(), fields("generator", type));
        Type<?> type1 = DSL.and(compoundlisttype, DSL.remainderType());
        Type<?> type2 = schema.getType(References.WORLD_GEN_SETTINGS);
        FieldFinder<?> fieldfinder = new FieldFinder<>("dimensions", type1);
        if (!type2.findFieldType("dimensions").equals(type1)) {
            throw new IllegalStateException();
        } else {
            OpticFinder<? extends List<? extends Pair<String, ?>>> opticfinder = compoundlisttype.finder();
            return this.fixTypeEverywhereTyped(
                "MissingDimensionFix", type2, p_16426_ -> p_16426_.updateTyped(fieldfinder, p_145517_ -> p_145517_.updateTyped(opticfinder, p_337647_ -> {
                            if (!(p_337647_.getValue() instanceof List)) {
                                throw new IllegalStateException("List exptected");
                            } else if (((List)p_337647_.getValue()).isEmpty()) {
                                Dynamic<?> dynamic = p_16426_.get(DSL.remainderFinder());
                                Dynamic<?> dynamic1 = this.recreateSettings(dynamic);
                                return DataFixUtils.orElse(compoundlisttype.readTyped(dynamic1).result().map(Pair::getFirst), p_337647_);
                            } else {
                                return p_337647_;
                            }
                        }))
            );
        }
    }

    protected static Type<? extends Pair<? extends Either<? extends Pair<? extends Either<?, Unit>, ? extends Pair<? extends Either<? extends List<? extends Pair<? extends Either<?, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>> flatType(
        Schema schema
    ) {
        return optionalFields(
            "settings",
            optionalFields("biome", schema.getType(References.BIOME), "layers", DSL.list(optionalFields("block", schema.getType(References.BLOCK_NAME))))
        );
    }

    private <T> Dynamic<T> recreateSettings(Dynamic<T> dynamic) {
        long i = dynamic.get("seed").asLong(0L);
        return new Dynamic<>(dynamic.getOps(), WorldGenSettingsFix.vanillaLevels(dynamic, i, WorldGenSettingsFix.defaultOverworld(dynamic, i), false));
    }
}
