package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EntityRidingToPassengersFix extends DataFix {
    public EntityRidingToPassengersFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        Schema schema1 = this.getOutputSchema();
        Type<?> type = schema.getTypeRaw(References.ENTITY_TREE);
        Type<?> type1 = schema1.getTypeRaw(References.ENTITY_TREE);
        Type<?> type2 = schema.getTypeRaw(References.ENTITY);
        return this.cap(schema, schema1, type, type1, type2);
    }

    private <OldEntityTree, NewEntityTree, Entity> TypeRewriteRule cap(
        Schema inputSchema, Schema outputSchema, Type<OldEntityTree> oldEntityTreeType, Type<NewEntityTree> newEntityTreeType, Type<Entity> entityType
    ) {
        Type<Pair<String, Pair<Either<OldEntityTree, Unit>, Entity>>> type = DSL.named(
            References.ENTITY_TREE.typeName(), DSL.and(DSL.optional(DSL.field("Riding", oldEntityTreeType)), entityType)
        );
        Type<Pair<String, Pair<Either<List<NewEntityTree>, Unit>, Entity>>> type1 = DSL.named(
            References.ENTITY_TREE.typeName(), DSL.and(DSL.optional(DSL.field("Passengers", DSL.list(newEntityTreeType))), entityType)
        );
        Type<?> type2 = inputSchema.getType(References.ENTITY_TREE);
        Type<?> type3 = outputSchema.getType(References.ENTITY_TREE);
        if (!Objects.equals(type2, type)) {
            throw new IllegalStateException("Old entity type is not what was expected.");
        } else if (!type3.equals(type1, true, true)) {
            throw new IllegalStateException("New entity type is not what was expected.");
        } else {
            OpticFinder<Pair<String, Pair<Either<OldEntityTree, Unit>, Entity>>> opticfinder = DSL.typeFinder(type);
            OpticFinder<Pair<String, Pair<Either<List<NewEntityTree>, Unit>, Entity>>> opticfinder1 = DSL.typeFinder(type1);
            OpticFinder<NewEntityTree> opticfinder2 = DSL.typeFinder(newEntityTreeType);
            Type<?> type4 = inputSchema.getType(References.PLAYER);
            Type<?> type5 = outputSchema.getType(References.PLAYER);
            return TypeRewriteRule.seq(
                this.fixTypeEverywhere(
                    "EntityRidingToPassengerFix",
                    type,
                    type1,
                    p_15653_ -> p_145320_ -> {
                            Optional<Pair<String, Pair<Either<List<NewEntityTree>, Unit>, Entity>>> optional = Optional.empty();
                            Pair<String, Pair<Either<OldEntityTree, Unit>, Entity>> pair = p_145320_;

                            while (true) {
                                Either<List<NewEntityTree>, Unit> either = DataFixUtils.orElse(
                                    optional.map(
                                        p_145326_ -> {
                                            Typed<NewEntityTree> typed = newEntityTreeType.pointTyped(p_15653_)
                                                .orElseThrow(() -> new IllegalStateException("Could not create new entity tree"));
                                            NewEntityTree newentitytree = typed.set(
                                                    opticfinder1, (Pair<String, Pair<Either<List<NewEntityTree>, Unit>, Entity>>)p_145326_
                                                )
                                                .getOptional(opticfinder2)
                                                .orElseThrow(() -> new IllegalStateException("Should always have an entity tree here"));
                                            return Either.left(ImmutableList.of(newentitytree));
                                        }
                                    ),
                                    Either.right(DSL.unit())
                                );
                                optional = Optional.of(Pair.of(References.ENTITY_TREE.typeName(), Pair.of(either, pair.getSecond().getSecond())));
                                Optional<OldEntityTree> optional1 = pair.getSecond().getFirst().left();
                                if (optional1.isEmpty()) {
                                    return optional.orElseThrow(() -> new IllegalStateException("Should always have an entity tree here"));
                                }

                                pair = new Typed<>(oldEntityTreeType, p_15653_, optional1.get())
                                    .getOptional(opticfinder)
                                    .orElseThrow(() -> new IllegalStateException("Should always have an entity here"));
                            }
                        }
                ),
                this.writeAndRead("player RootVehicle injecter", type4, type5)
            );
        }
    }
}
