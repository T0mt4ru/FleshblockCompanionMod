package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;

public interface EnchantmentLocationBasedEffect {
    Codec<EnchantmentLocationBasedEffect> CODEC = BuiltInRegistries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE
        .byNameCodec()
        .dispatch(EnchantmentLocationBasedEffect::codec, Function.identity());

    static MapCodec<? extends EnchantmentLocationBasedEffect> bootstrap(Registry<MapCodec<? extends EnchantmentLocationBasedEffect>> registry) {
        Registry.register(registry, "all_of", AllOf.LocationBasedEffects.CODEC);
        Registry.register(registry, "apply_mob_effect", ApplyMobEffect.CODEC);
        Registry.register(registry, "attribute", EnchantmentAttributeEffect.CODEC);
        Registry.register(registry, "damage_entity", DamageEntity.CODEC);
        Registry.register(registry, "damage_item", DamageItem.CODEC);
        Registry.register(registry, "explode", ExplodeEffect.CODEC);
        Registry.register(registry, "ignite", Ignite.CODEC);
        Registry.register(registry, "play_sound", PlaySoundEffect.CODEC);
        Registry.register(registry, "replace_block", ReplaceBlock.CODEC);
        Registry.register(registry, "replace_disk", ReplaceDisk.CODEC);
        Registry.register(registry, "run_function", RunFunction.CODEC);
        Registry.register(registry, "set_block_properties", SetBlockProperties.CODEC);
        Registry.register(registry, "spawn_particles", SpawnParticlesEffect.CODEC);
        return Registry.register(registry, "summon_entity", SummonEntityEffect.CODEC);
    }

    void onChangedBlock(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 pos, boolean applyTransientEffects);

    default void onDeactivated(EnchantedItemInUse item, Entity entity, Vec3 pos, int enchantmentLevel) {
    }

    MapCodec<? extends EnchantmentLocationBasedEffect> codec();
}
