package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public record WorldDataConfiguration(DataPackConfig dataPacks, FeatureFlagSet enabledFeatures) {
    public static final String ENABLED_FEATURES_ID = "enabled_features";
    public static final Codec<WorldDataConfiguration> CODEC = RecordCodecBuilder.create(
        p_337972_ -> p_337972_.group(
                    DataPackConfig.CODEC.lenientOptionalFieldOf("DataPacks", DataPackConfig.DEFAULT).forGetter(WorldDataConfiguration::dataPacks),
                    FeatureFlags.CODEC
                        .lenientOptionalFieldOf("enabled_features", FeatureFlags.DEFAULT_FLAGS)
                        .forGetter(WorldDataConfiguration::enabledFeatures)
                )
                .apply(p_337972_, WorldDataConfiguration::new)
    );
    public static final WorldDataConfiguration DEFAULT = new WorldDataConfiguration(DataPackConfig.DEFAULT, FeatureFlags.DEFAULT_FLAGS);

    public WorldDataConfiguration expandFeatures(FeatureFlagSet newFeatures) {
        return new WorldDataConfiguration(this.dataPacks, this.enabledFeatures.join(newFeatures));
    }
}
