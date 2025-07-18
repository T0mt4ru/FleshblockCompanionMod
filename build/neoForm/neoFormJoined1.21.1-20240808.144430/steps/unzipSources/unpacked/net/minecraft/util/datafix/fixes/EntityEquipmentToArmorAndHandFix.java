package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class EntityEquipmentToArmorAndHandFix extends DataFix {
    public EntityEquipmentToArmorAndHandFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.cap(this.getInputSchema().getTypeRaw(References.ITEM_STACK));
    }

    private <IS> TypeRewriteRule cap(Type<IS> itemStackType) {
        Type<Pair<Either<List<IS>, Unit>, Dynamic<?>>> type = DSL.and(DSL.optional(DSL.field("Equipment", DSL.list(itemStackType))), DSL.remainderType());
        Type<Pair<Either<List<IS>, Unit>, Pair<Either<List<IS>, Unit>, Pair<Either<IS, Unit>, Dynamic<?>>>>> type1 = DSL.and(
            DSL.optional(DSL.field("ArmorItems", DSL.list(itemStackType))),
            DSL.optional(DSL.field("HandItems", DSL.list(itemStackType))),
            DSL.optional(DSL.field("body_armor_item", itemStackType)),
            DSL.remainderType()
        );
        OpticFinder<Pair<Either<List<IS>, Unit>, Dynamic<?>>> opticfinder = DSL.typeFinder(type);
        OpticFinder<List<IS>> opticfinder1 = DSL.fieldFinder("Equipment", DSL.list(itemStackType));
        return this.fixTypeEverywhereTyped(
            "EntityEquipmentToArmorAndHandFix",
            this.getInputSchema().getType(References.ENTITY),
            this.getOutputSchema().getType(References.ENTITY),
            p_326763_ -> {
                Either<List<IS>, Unit> either = Either.right(DSL.unit());
                Either<List<IS>, Unit> either1 = Either.right(DSL.unit());
                Either<IS, Unit> either2 = Either.right(DSL.unit());
                Dynamic<?> dynamic = p_326763_.getOrCreate(DSL.remainderFinder());
                Optional<List<IS>> optional = p_326763_.getOptional(opticfinder1);
                if (optional.isPresent()) {
                    List<IS> list = optional.get();
                    IS is = itemStackType.read(dynamic.emptyMap())
                        .result()
                        .orElseThrow(() -> new IllegalStateException("Could not parse newly created empty itemstack."))
                        .getFirst();
                    if (!list.isEmpty()) {
                        either = Either.left(Lists.newArrayList(list.get(0), is));
                    }

                    if (list.size() > 1) {
                        List<IS> list1 = Lists.newArrayList(is, is, is, is);

                        for (int i = 1; i < Math.min(list.size(), 5); i++) {
                            list1.set(i - 1, list.get(i));
                        }

                        either1 = Either.left(list1);
                    }
                }

                Dynamic<?> dynamic2 = dynamic;
                Optional<? extends Stream<? extends Dynamic<?>>> optional1 = dynamic.get("DropChances").asStreamOpt().result();
                if (optional1.isPresent()) {
                    Iterator<? extends Dynamic<?>> iterator = Stream.concat(
                            (Stream<? extends Dynamic<?>>)optional1.get(), Stream.generate(() -> dynamic2.createInt(0))
                        )
                        .iterator();
                    float f = iterator.next().asFloat(0.0F);
                    if (dynamic.get("HandDropChances").result().isEmpty()) {
                        Dynamic<?> dynamic1 = dynamic.createList(Stream.of(f, 0.0F).map(dynamic::createFloat));
                        dynamic = dynamic.set("HandDropChances", dynamic1);
                    }

                    if (dynamic.get("ArmorDropChances").result().isEmpty()) {
                        Dynamic<?> dynamic3 = dynamic.createList(
                            Stream.of(
                                    iterator.next().asFloat(0.0F), iterator.next().asFloat(0.0F), iterator.next().asFloat(0.0F), iterator.next().asFloat(0.0F)
                                )
                                .map(dynamic::createFloat)
                        );
                        dynamic = dynamic.set("ArmorDropChances", dynamic3);
                    }

                    dynamic = dynamic.remove("DropChances");
                }

                return p_326763_.set(opticfinder, type1, Pair.of(either, Pair.of(either1, Pair.of(either2, dynamic))));
            }
        );
    }
}
