package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

/**
 * A condition based on {@link LootContext}.
 *
 * @see {@link LootItemConditions}
 * @see {@link PredicateManager}
 */
public interface LootItemCondition extends LootContextUser, Predicate<LootContext> {
    Codec<LootItemCondition> TYPED_CODEC = BuiltInRegistries.LOOT_CONDITION_TYPE
        .byNameCodec()
        .dispatch("condition", LootItemCondition::getType, LootItemConditionType::codec);
    Codec<LootItemCondition> DIRECT_CODEC = Codec.lazyInitialized(() -> Codec.withAlternative(TYPED_CODEC, AllOfCondition.INLINE_CODEC));
    Codec<Holder<LootItemCondition>> CODEC = RegistryFileCodec.create(Registries.PREDICATE, DIRECT_CODEC);

    LootItemConditionType getType();

    @FunctionalInterface
    public interface Builder {
        LootItemCondition build();

        default LootItemCondition.Builder invert() {
            return InvertedLootItemCondition.invert(this);
        }

        default AnyOfCondition.Builder or(LootItemCondition.Builder condition) {
            return AnyOfCondition.anyOf(this, condition);
        }

        default AllOfCondition.Builder and(LootItemCondition.Builder condition) {
            return AllOfCondition.allOf(this, condition);
        }
    }
}
