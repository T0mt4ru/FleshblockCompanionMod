package net.minecraft.world.level.levelgen;

import net.minecraft.util.RandomSource;

public class SingleThreadedRandomSource implements BitRandomSource {
    private static final int MODULUS_BITS = 48;
    private static final long MODULUS_MASK = 281474976710655L;
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private long seed;
    private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

    public SingleThreadedRandomSource(long seed) {
        this.setSeed(seed);
    }

    @Override
    public RandomSource fork() {
        return new SingleThreadedRandomSource(this.nextLong());
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return new LegacyRandomSource.LegacyPositionalRandomFactory(this.nextLong());
    }

    @Override
    public void setSeed(long seed) {
        this.seed = (seed ^ 25214903917L) & 281474976710655L;
        this.gaussianSource.reset();
    }

    @Override
    public int next(int size) {
        long i = this.seed * 25214903917L + 11L & 281474976710655L;
        this.seed = i;
        return (int)(i >> 48 - size);
    }

    @Override
    public double nextGaussian() {
        return this.gaussianSource.nextGaussian();
    }
}
