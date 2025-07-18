package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class SimpleRandomFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<SimpleRandomFeatureConfiguration> CODEC = ExtraCodecs.nonEmptyHolderSet(PlacedFeature.LIST_CODEC)
        .fieldOf("features")
        .xmap(SimpleRandomFeatureConfiguration::new, p_204844_ -> p_204844_.features)
        .codec();
    public final HolderSet<PlacedFeature> features;

    public SimpleRandomFeatureConfiguration(HolderSet<PlacedFeature> features) {
        this.features = features;
    }

    @Override
    public Stream<ConfiguredFeature<?, ?>> getFeatures() {
        return this.features.stream().flatMap(p_204846_ -> p_204846_.value().getFeatures());
    }
}
