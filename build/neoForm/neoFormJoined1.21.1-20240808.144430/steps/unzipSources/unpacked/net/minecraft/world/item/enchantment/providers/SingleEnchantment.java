package net.minecraft.world.item.enchantment.providers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record SingleEnchantment(Holder<Enchantment> enchantment, IntProvider level) implements EnchantmentProvider {
    public static final MapCodec<SingleEnchantment> CODEC = RecordCodecBuilder.mapCodec(
        p_346042_ -> p_346042_.group(
                    Enchantment.CODEC.fieldOf("enchantment").forGetter(SingleEnchantment::enchantment),
                    IntProvider.CODEC.fieldOf("level").forGetter(SingleEnchantment::level)
                )
                .apply(p_346042_, SingleEnchantment::new)
    );

    @Override
    public void enchant(ItemStack stack, ItemEnchantments.Mutable enchantments, RandomSource random, DifficultyInstance difficulty) {
        enchantments.upgrade(
            this.enchantment, Mth.clamp(this.level.sample(random), this.enchantment.value().getMinLevel(), this.enchantment.value().getMaxLevel())
        );
    }

    @Override
    public MapCodec<SingleEnchantment> codec() {
        return CODEC;
    }
}
