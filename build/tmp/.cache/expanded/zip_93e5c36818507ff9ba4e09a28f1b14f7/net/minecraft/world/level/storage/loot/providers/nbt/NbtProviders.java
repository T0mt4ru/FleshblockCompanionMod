package net.minecraft.world.level.storage.loot.providers.nbt;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Registry for {@link NbtProvider}
 */
public class NbtProviders {
    private static final Codec<NbtProvider> TYPED_CODEC = BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE
        .byNameCodec()
        .dispatch(NbtProvider::getType, LootNbtProviderType::codec);
    public static final Codec<NbtProvider> CODEC = Codec.lazyInitialized(
        () -> Codec.either(ContextNbtProvider.INLINE_CODEC, TYPED_CODEC)
                .xmap(
                    Either::unwrap,
                    p_299257_ -> p_299257_ instanceof ContextNbtProvider contextnbtprovider ? Either.left(contextnbtprovider) : Either.right(p_299257_)
                )
    );
    public static final LootNbtProviderType STORAGE = register("storage", StorageNbtProvider.CODEC);
    public static final LootNbtProviderType CONTEXT = register("context", ContextNbtProvider.CODEC);

    private static LootNbtProviderType register(String name, MapCodec<? extends NbtProvider> codec) {
        return Registry.register(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, ResourceLocation.withDefaultNamespace(name), new LootNbtProviderType(codec));
    }
}
