package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;

/**
 * This samples the sum of two individual samplers of perlin noise octaves.
 * The input coordinates are scaled by {@link #INPUT_FACTOR}, and the result is scaled by {@link #valueFactor}.
 */
public class NormalNoise {
    private static final double INPUT_FACTOR = 1.0181268882175227;
    private static final double TARGET_DEVIATION = 0.3333333333333333;
    private final double valueFactor;
    private final PerlinNoise first;
    private final PerlinNoise second;
    private final double maxValue;
    private final NormalNoise.NoiseParameters parameters;

    @Deprecated
    public static NormalNoise createLegacyNetherBiome(RandomSource random, NormalNoise.NoiseParameters parameters) {
        return new NormalNoise(random, parameters, false);
    }

    public static NormalNoise create(RandomSource random, int firstOctave, double... amplitudes) {
        return create(random, new NormalNoise.NoiseParameters(firstOctave, new DoubleArrayList(amplitudes)));
    }

    public static NormalNoise create(RandomSource random, NormalNoise.NoiseParameters parameters) {
        return new NormalNoise(random, parameters, true);
    }

    private NormalNoise(RandomSource random, NormalNoise.NoiseParameters parameters, boolean useLegacyNetherBiome) {
        int i = parameters.firstOctave;
        DoubleList doublelist = parameters.amplitudes;
        this.parameters = parameters;
        if (useLegacyNetherBiome) {
            this.first = PerlinNoise.create(random, i, doublelist);
            this.second = PerlinNoise.create(random, i, doublelist);
        } else {
            this.first = PerlinNoise.createLegacyForLegacyNetherBiome(random, i, doublelist);
            this.second = PerlinNoise.createLegacyForLegacyNetherBiome(random, i, doublelist);
        }

        int j = Integer.MAX_VALUE;
        int k = Integer.MIN_VALUE;
        DoubleListIterator doublelistiterator = doublelist.iterator();

        while (doublelistiterator.hasNext()) {
            int l = doublelistiterator.nextIndex();
            double d0 = doublelistiterator.nextDouble();
            if (d0 != 0.0) {
                j = Math.min(j, l);
                k = Math.max(k, l);
            }
        }

        this.valueFactor = 0.16666666666666666 / expectedDeviation(k - j);
        this.maxValue = (this.first.maxValue() + this.second.maxValue()) * this.valueFactor;
    }

    public double maxValue() {
        return this.maxValue;
    }

    private static double expectedDeviation(int octaves) {
        return 0.1 * (1.0 + 1.0 / (double)(octaves + 1));
    }

    public double getValue(double x, double y, double z) {
        double d0 = x * 1.0181268882175227;
        double d1 = y * 1.0181268882175227;
        double d2 = z * 1.0181268882175227;
        return (this.first.getValue(x, y, z) + this.second.getValue(d0, d1, d2)) * this.valueFactor;
    }

    public NormalNoise.NoiseParameters parameters() {
        return this.parameters;
    }

    @VisibleForTesting
    public void parityConfigString(StringBuilder builder) {
        builder.append("NormalNoise {");
        builder.append("first: ");
        this.first.parityConfigString(builder);
        builder.append(", second: ");
        this.second.parityConfigString(builder);
        builder.append("}");
    }

    public static record NoiseParameters(int firstOctave, DoubleList amplitudes) {
        public static final Codec<NormalNoise.NoiseParameters> DIRECT_CODEC = RecordCodecBuilder.create(
            p_192865_ -> p_192865_.group(
                        Codec.INT.fieldOf("firstOctave").forGetter(NormalNoise.NoiseParameters::firstOctave),
                        Codec.DOUBLE.listOf().fieldOf("amplitudes").forGetter(NormalNoise.NoiseParameters::amplitudes)
                    )
                    .apply(p_192865_, NormalNoise.NoiseParameters::new)
        );
        public static final Codec<Holder<NormalNoise.NoiseParameters>> CODEC = RegistryFileCodec.create(Registries.NOISE, DIRECT_CODEC);

        public NoiseParameters(int p_192861_, List<Double> p_192862_) {
            this(p_192861_, new DoubleArrayList(p_192862_));
        }

        public NoiseParameters(int p_192857_, double p_192858_, double... p_192859_) {
            this(p_192857_, Util.make(new DoubleArrayList(p_192859_), p_210636_ -> p_210636_.add(0, p_192858_)));
        }
    }
}
