package net.minecraft.world.entity.animal;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record FrogVariant(ResourceLocation texture) {
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<FrogVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.FROG_VARIANT);
    public static final ResourceKey<FrogVariant> TEMPERATE = createKey("temperate");
    public static final ResourceKey<FrogVariant> WARM = createKey("warm");
    public static final ResourceKey<FrogVariant> COLD = createKey("cold");

    private static ResourceKey<FrogVariant> createKey(String name) {
        return ResourceKey.create(Registries.FROG_VARIANT, ResourceLocation.withDefaultNamespace(name));
    }

    public static FrogVariant bootstrap(Registry<FrogVariant> registry) {
        register(registry, TEMPERATE, "textures/entity/frog/temperate_frog.png");
        register(registry, WARM, "textures/entity/frog/warm_frog.png");
        return register(registry, COLD, "textures/entity/frog/cold_frog.png");
    }

    private static FrogVariant register(Registry<FrogVariant> registry, ResourceKey<FrogVariant> key, String texture) {
        return Registry.register(registry, key, new FrogVariant(ResourceLocation.withDefaultNamespace(texture)));
    }
}
