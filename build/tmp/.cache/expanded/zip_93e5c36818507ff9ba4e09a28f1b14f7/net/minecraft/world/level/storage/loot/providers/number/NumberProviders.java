package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Registration for {@link LootNumberProviderType}.
 *
 * @see LootNumberProviderType
 * @see NumberProvider
 */
public class NumberProviders {
    private static final Codec<NumberProvider> TYPED_CODEC = BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE
        .byNameCodec()
        .dispatch(NumberProvider::getType, LootNumberProviderType::codec);
    public static final Codec<NumberProvider> CODEC = Codec.lazyInitialized(
        () -> {
            Codec<NumberProvider> codec = Codec.withAlternative(TYPED_CODEC, UniformGenerator.CODEC.codec());
            return Codec.either(ConstantValue.INLINE_CODEC, codec)
                .xmap(Either::unwrap, p_299325_ -> p_299325_ instanceof ConstantValue constantvalue ? Either.left(constantvalue) : Either.right(p_299325_));
        }
    );
    public static final LootNumberProviderType CONSTANT = register("constant", ConstantValue.CODEC);
    public static final LootNumberProviderType UNIFORM = register("uniform", UniformGenerator.CODEC);
    public static final LootNumberProviderType BINOMIAL = register("binomial", BinomialDistributionGenerator.CODEC);
    public static final LootNumberProviderType SCORE = register("score", ScoreboardValue.CODEC);
    public static final LootNumberProviderType STORAGE = register("storage", StorageValue.CODEC);
    public static final LootNumberProviderType ENCHANTMENT_LEVEL = register("enchantment_level", EnchantmentLevelProvider.CODEC);

    private static LootNumberProviderType register(String name, MapCodec<? extends NumberProvider> codec) {
        return Registry.register(
            BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, ResourceLocation.withDefaultNamespace(name), new LootNumberProviderType(codec)
        );
    }
}
