package net.minecraft.world.level.levelgen;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurfaceRules {
    public static final SurfaceRules.ConditionSource ON_FLOOR = stoneDepthCheck(0, false, CaveSurface.FLOOR);
    public static final SurfaceRules.ConditionSource UNDER_FLOOR = stoneDepthCheck(0, true, CaveSurface.FLOOR);
    public static final SurfaceRules.ConditionSource DEEP_UNDER_FLOOR = stoneDepthCheck(0, true, 6, CaveSurface.FLOOR);
    public static final SurfaceRules.ConditionSource VERY_DEEP_UNDER_FLOOR = stoneDepthCheck(0, true, 30, CaveSurface.FLOOR);
    public static final SurfaceRules.ConditionSource ON_CEILING = stoneDepthCheck(0, false, CaveSurface.CEILING);
    public static final SurfaceRules.ConditionSource UNDER_CEILING = stoneDepthCheck(0, true, CaveSurface.CEILING);

    public static SurfaceRules.ConditionSource stoneDepthCheck(int offset, boolean addSurfaceDepth, CaveSurface surfaceType) {
        return new SurfaceRules.StoneDepthCheck(offset, addSurfaceDepth, 0, surfaceType);
    }

    public static SurfaceRules.ConditionSource stoneDepthCheck(int offset, boolean addSurfaceDepth, int secondaryDepthRange, CaveSurface surfaceType) {
        return new SurfaceRules.StoneDepthCheck(offset, addSurfaceDepth, secondaryDepthRange, surfaceType);
    }

    public static SurfaceRules.ConditionSource not(SurfaceRules.ConditionSource target) {
        return new SurfaceRules.NotConditionSource(target);
    }

    public static SurfaceRules.ConditionSource yBlockCheck(VerticalAnchor anchor, int surfaceDepthMultiplier) {
        return new SurfaceRules.YConditionSource(anchor, surfaceDepthMultiplier, false);
    }

    public static SurfaceRules.ConditionSource yStartCheck(VerticalAnchor anchor, int surfaceDepthMultiplier) {
        return new SurfaceRules.YConditionSource(anchor, surfaceDepthMultiplier, true);
    }

    public static SurfaceRules.ConditionSource waterBlockCheck(int offset, int surfaceDepthMultiplier) {
        return new SurfaceRules.WaterConditionSource(offset, surfaceDepthMultiplier, false);
    }

    public static SurfaceRules.ConditionSource waterStartCheck(int offset, int surfaceDepthMultiplier) {
        return new SurfaceRules.WaterConditionSource(offset, surfaceDepthMultiplier, true);
    }

    @SafeVarargs
    public static SurfaceRules.ConditionSource isBiome(ResourceKey<Biome>... biomes) {
        return isBiome(List.of(biomes));
    }

    private static SurfaceRules.BiomeConditionSource isBiome(List<ResourceKey<Biome>> biomes) {
        return new SurfaceRules.BiomeConditionSource(biomes);
    }

    public static SurfaceRules.ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> noise, double minThreshold) {
        return noiseCondition(noise, minThreshold, Double.MAX_VALUE);
    }

    public static SurfaceRules.ConditionSource noiseCondition(ResourceKey<NormalNoise.NoiseParameters> noise, double minThreshold, double maxThreshold) {
        return new SurfaceRules.NoiseThresholdConditionSource(noise, minThreshold, maxThreshold);
    }

    public static SurfaceRules.ConditionSource verticalGradient(String randomName, VerticalAnchor trueAtAndBelow, VerticalAnchor falseAtAndAbove) {
        return new SurfaceRules.VerticalGradientConditionSource(ResourceLocation.parse(randomName), trueAtAndBelow, falseAtAndAbove);
    }

    public static SurfaceRules.ConditionSource steep() {
        return SurfaceRules.Steep.INSTANCE;
    }

    public static SurfaceRules.ConditionSource hole() {
        return SurfaceRules.Hole.INSTANCE;
    }

    public static SurfaceRules.ConditionSource abovePreliminarySurface() {
        return SurfaceRules.AbovePreliminarySurface.INSTANCE;
    }

    public static SurfaceRules.ConditionSource temperature() {
        return SurfaceRules.Temperature.INSTANCE;
    }

    public static SurfaceRules.RuleSource ifTrue(SurfaceRules.ConditionSource ifTrue, SurfaceRules.RuleSource thenRun) {
        return new SurfaceRules.TestRuleSource(ifTrue, thenRun);
    }

    public static SurfaceRules.RuleSource sequence(SurfaceRules.RuleSource... rules) {
        if (rules.length == 0) {
            throw new IllegalArgumentException("Need at least 1 rule for a sequence");
        } else {
            return new SurfaceRules.SequenceRuleSource(Arrays.asList(rules));
        }
    }

    public static SurfaceRules.RuleSource state(BlockState resultState) {
        return new SurfaceRules.BlockRuleSource(resultState);
    }

    public static SurfaceRules.RuleSource bandlands() {
        return SurfaceRules.Bandlands.INSTANCE;
    }

    static <A> MapCodec<? extends A> register(Registry<MapCodec<? extends A>> registry, String name, KeyDispatchDataCodec<? extends A> codec) {
        return Registry.register(registry, name, codec.codec());
    }

    static enum AbovePreliminarySurface implements SurfaceRules.ConditionSource {
        INSTANCE;

        static final KeyDispatchDataCodec<SurfaceRules.AbovePreliminarySurface> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context context) {
            return context.abovePreliminarySurface;
        }
    }

    static enum Bandlands implements SurfaceRules.RuleSource {
        INSTANCE;

        static final KeyDispatchDataCodec<SurfaceRules.Bandlands> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
            return CODEC;
        }

        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
            return context.system::getBand;
        }
    }

    static final class BiomeConditionSource implements SurfaceRules.ConditionSource {
        static final KeyDispatchDataCodec<SurfaceRules.BiomeConditionSource> CODEC = KeyDispatchDataCodec.of(
            ResourceKey.codec(Registries.BIOME).listOf().fieldOf("biome_is").xmap(SurfaceRules::isBiome, p_204620_ -> p_204620_.biomes)
        );
        private final List<ResourceKey<Biome>> biomes;
        final Predicate<ResourceKey<Biome>> biomeNameTest;

        BiomeConditionSource(List<ResourceKey<Biome>> biomes) {
            this.biomes = biomes;
            this.biomeNameTest = Set.copyOf(biomes)::contains;
        }

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(final SurfaceRules.Context p_context) {
            class BiomeCondition extends SurfaceRules.LazyYCondition {
                BiomeCondition() {
                    super(p_context);
                }

                @Override
                protected boolean compute() {
                    return this.context.biome.get().is(BiomeConditionSource.this.biomeNameTest);
                }
            }

            return new BiomeCondition();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            } else {
                return other instanceof SurfaceRules.BiomeConditionSource surfacerules$biomeconditionsource
                    ? this.biomes.equals(surfacerules$biomeconditionsource.biomes)
                    : false;
            }
        }

        @Override
        public int hashCode() {
            return this.biomes.hashCode();
        }

        @Override
        public String toString() {
            return "BiomeConditionSource[biomes=" + this.biomes + "]";
        }
    }

    static record BlockRuleSource(BlockState resultState, SurfaceRules.StateRule rule) implements SurfaceRules.RuleSource {
        static final KeyDispatchDataCodec<SurfaceRules.BlockRuleSource> CODEC = KeyDispatchDataCodec.of(
            BlockState.CODEC.xmap(SurfaceRules.BlockRuleSource::new, SurfaceRules.BlockRuleSource::resultState).fieldOf("result_state")
        );

        BlockRuleSource(BlockState p_189517_) {
            this(p_189517_, new SurfaceRules.StateRule(p_189517_));
        }

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
            return CODEC;
        }

        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
            return this.rule;
        }
    }

    interface Condition {
        boolean test();
    }

    public interface ConditionSource extends Function<SurfaceRules.Context, SurfaceRules.Condition> {
        Codec<SurfaceRules.ConditionSource> CODEC = BuiltInRegistries.MATERIAL_CONDITION
            .byNameCodec()
            .dispatch(p_338094_ -> p_338094_.codec().codec(), Function.identity());

        static MapCodec<? extends SurfaceRules.ConditionSource> bootstrap(Registry<MapCodec<? extends SurfaceRules.ConditionSource>> registry) {
            SurfaceRules.register(registry, "biome", SurfaceRules.BiomeConditionSource.CODEC);
            SurfaceRules.register(registry, "noise_threshold", SurfaceRules.NoiseThresholdConditionSource.CODEC);
            SurfaceRules.register(registry, "vertical_gradient", SurfaceRules.VerticalGradientConditionSource.CODEC);
            SurfaceRules.register(registry, "y_above", SurfaceRules.YConditionSource.CODEC);
            SurfaceRules.register(registry, "water", SurfaceRules.WaterConditionSource.CODEC);
            SurfaceRules.register(registry, "temperature", SurfaceRules.Temperature.CODEC);
            SurfaceRules.register(registry, "steep", SurfaceRules.Steep.CODEC);
            SurfaceRules.register(registry, "not", SurfaceRules.NotConditionSource.CODEC);
            SurfaceRules.register(registry, "hole", SurfaceRules.Hole.CODEC);
            SurfaceRules.register(registry, "above_preliminary_surface", SurfaceRules.AbovePreliminarySurface.CODEC);
            return SurfaceRules.register(registry, "stone_depth", SurfaceRules.StoneDepthCheck.CODEC);
        }

        KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec();
    }

    protected static final class Context {
        private static final int HOW_FAR_BELOW_PRELIMINARY_SURFACE_LEVEL_TO_BUILD_SURFACE = 8;
        private static final int SURFACE_CELL_BITS = 4;
        private static final int SURFACE_CELL_SIZE = 16;
        private static final int SURFACE_CELL_MASK = 15;
        final SurfaceSystem system;
        final SurfaceRules.Condition temperature = new SurfaceRules.Context.TemperatureHelperCondition(this);
        final SurfaceRules.Condition steep = new SurfaceRules.Context.SteepMaterialCondition(this);
        final SurfaceRules.Condition hole = new SurfaceRules.Context.HoleCondition(this);
        final SurfaceRules.Condition abovePreliminarySurface = new SurfaceRules.Context.AbovePreliminarySurfaceCondition();
        final RandomState randomState;
        final ChunkAccess chunk;
        private final NoiseChunk noiseChunk;
        private final Function<BlockPos, Holder<Biome>> biomeGetter;
        final WorldGenerationContext context;
        private long lastPreliminarySurfaceCellOrigin = Long.MAX_VALUE;
        private final int[] preliminarySurfaceCache = new int[4];
        long lastUpdateXZ = -9223372036854775807L;
        int blockX;
        int blockZ;
        int surfaceDepth;
        private long lastSurfaceDepth2Update = this.lastUpdateXZ - 1L;
        private double surfaceSecondary;
        private long lastMinSurfaceLevelUpdate = this.lastUpdateXZ - 1L;
        private int minSurfaceLevel;
        long lastUpdateY = -9223372036854775807L;
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Supplier<Holder<Biome>> biome;
        int blockY;
        int waterHeight;
        int stoneDepthBelow;
        int stoneDepthAbove;

        protected Context(
            SurfaceSystem system,
            RandomState randomState,
            ChunkAccess chunk,
            NoiseChunk noiseChunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            Registry<Biome> p_224621_,
            WorldGenerationContext context
        ) {
            this.system = system;
            this.randomState = randomState;
            this.chunk = chunk;
            this.noiseChunk = noiseChunk;
            this.biomeGetter = biomeGetter;
            this.context = context;
        }

        protected void updateXZ(int blockX, int blockZ) {
            this.lastUpdateXZ++;
            this.lastUpdateY++;
            this.blockX = blockX;
            this.blockZ = blockZ;
            this.surfaceDepth = this.system.getSurfaceDepth(blockX, blockZ);
        }

        protected void updateY(int stoneDepthAbove, int stoneDepthBelow, int waterHeight, int blockX, int blockY, int blockZ) {
            this.lastUpdateY++;
            this.biome = Suppliers.memoize(() -> this.biomeGetter.apply(this.pos.set(blockX, blockY, blockZ)));
            this.blockY = blockY;
            this.waterHeight = waterHeight;
            this.stoneDepthBelow = stoneDepthBelow;
            this.stoneDepthAbove = stoneDepthAbove;
        }

        protected double getSurfaceSecondary() {
            if (this.lastSurfaceDepth2Update != this.lastUpdateXZ) {
                this.lastSurfaceDepth2Update = this.lastUpdateXZ;
                this.surfaceSecondary = this.system.getSurfaceSecondary(this.blockX, this.blockZ);
            }

            return this.surfaceSecondary;
        }

        private static int blockCoordToSurfaceCell(int blockCoord) {
            return blockCoord >> 4;
        }

        private static int surfaceCellToBlockCoord(int surfaceCell) {
            return surfaceCell << 4;
        }

        protected int getMinSurfaceLevel() {
            if (this.lastMinSurfaceLevelUpdate != this.lastUpdateXZ) {
                this.lastMinSurfaceLevelUpdate = this.lastUpdateXZ;
                int i = blockCoordToSurfaceCell(this.blockX);
                int j = blockCoordToSurfaceCell(this.blockZ);
                long k = ChunkPos.asLong(i, j);
                if (this.lastPreliminarySurfaceCellOrigin != k) {
                    this.lastPreliminarySurfaceCellOrigin = k;
                    this.preliminarySurfaceCache[0] = this.noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord(i), surfaceCellToBlockCoord(j));
                    this.preliminarySurfaceCache[1] = this.noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord(i + 1), surfaceCellToBlockCoord(j));
                    this.preliminarySurfaceCache[2] = this.noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord(i), surfaceCellToBlockCoord(j + 1));
                    this.preliminarySurfaceCache[3] = this.noiseChunk.preliminarySurfaceLevel(surfaceCellToBlockCoord(i + 1), surfaceCellToBlockCoord(j + 1));
                }

                int l = Mth.floor(
                    Mth.lerp2(
                        (double)((float)(this.blockX & 15) / 16.0F),
                        (double)((float)(this.blockZ & 15) / 16.0F),
                        (double)this.preliminarySurfaceCache[0],
                        (double)this.preliminarySurfaceCache[1],
                        (double)this.preliminarySurfaceCache[2],
                        (double)this.preliminarySurfaceCache[3]
                    )
                );
                this.minSurfaceLevel = l + this.surfaceDepth - 8;
            }

            return this.minSurfaceLevel;
        }

        final class AbovePreliminarySurfaceCondition implements SurfaceRules.Condition {
            @Override
            public boolean test() {
                return Context.this.blockY >= Context.this.getMinSurfaceLevel();
            }
        }

        static final class HoleCondition extends SurfaceRules.LazyXZCondition {
            HoleCondition(SurfaceRules.Context p_189591_) {
                super(p_189591_);
            }

            @Override
            protected boolean compute() {
                return this.context.surfaceDepth <= 0;
            }
        }

        static class SteepMaterialCondition extends SurfaceRules.LazyXZCondition {
            SteepMaterialCondition(SurfaceRules.Context p_189594_) {
                super(p_189594_);
            }

            @Override
            protected boolean compute() {
                int i = this.context.blockX & 15;
                int j = this.context.blockZ & 15;
                int k = Math.max(j - 1, 0);
                int l = Math.min(j + 1, 15);
                ChunkAccess chunkaccess = this.context.chunk;
                int i1 = chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, k);
                int j1 = chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, l);
                if (j1 >= i1 + 4) {
                    return true;
                } else {
                    int k1 = Math.max(i - 1, 0);
                    int l1 = Math.min(i + 1, 15);
                    int i2 = chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, k1, j);
                    int j2 = chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, l1, j);
                    return i2 >= j2 + 4;
                }
            }
        }

        static class TemperatureHelperCondition extends SurfaceRules.LazyYCondition {
            TemperatureHelperCondition(SurfaceRules.Context p_189597_) {
                super(p_189597_);
            }

            @Override
            protected boolean compute() {
                return this.context.biome.get().value().coldEnoughToSnow(this.context.pos.set(this.context.blockX, this.context.blockY, this.context.blockZ));
            }
        }
    }

    static enum Hole implements SurfaceRules.ConditionSource {
        INSTANCE;

        static final KeyDispatchDataCodec<SurfaceRules.Hole> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context context) {
            return context.hole;
        }
    }

    abstract static class LazyCondition implements SurfaceRules.Condition {
        protected final SurfaceRules.Context context;
        private long lastUpdate;
        @Nullable
        Boolean result;

        protected LazyCondition(SurfaceRules.Context context) {
            this.context = context;
            this.lastUpdate = this.getContextLastUpdate() - 1L;
        }

        @Override
        public boolean test() {
            long i = this.getContextLastUpdate();
            if (i == this.lastUpdate) {
                if (this.result == null) {
                    throw new IllegalStateException("Update triggered but the result is null");
                } else {
                    return this.result;
                }
            } else {
                this.lastUpdate = i;
                this.result = this.compute();
                return this.result;
            }
        }

        protected abstract long getContextLastUpdate();

        protected abstract boolean compute();
    }

    abstract static class LazyXZCondition extends SurfaceRules.LazyCondition {
        protected LazyXZCondition(SurfaceRules.Context p_189622_) {
            super(p_189622_);
        }

        @Override
        protected long getContextLastUpdate() {
            return this.context.lastUpdateXZ;
        }
    }

    abstract static class LazyYCondition extends SurfaceRules.LazyCondition {
        protected LazyYCondition(SurfaceRules.Context p_189625_) {
            super(p_189625_);
        }

        @Override
        protected long getContextLastUpdate() {
            return this.context.lastUpdateY;
        }
    }

    static record NoiseThresholdConditionSource(ResourceKey<NormalNoise.NoiseParameters> noise, double minThreshold, double maxThreshold)
        implements SurfaceRules.ConditionSource {
        static final KeyDispatchDataCodec<SurfaceRules.NoiseThresholdConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                p_258995_ -> p_258995_.group(
                            ResourceKey.codec(Registries.NOISE).fieldOf("noise").forGetter(SurfaceRules.NoiseThresholdConditionSource::noise),
                            Codec.DOUBLE.fieldOf("min_threshold").forGetter(SurfaceRules.NoiseThresholdConditionSource::minThreshold),
                            Codec.DOUBLE.fieldOf("max_threshold").forGetter(SurfaceRules.NoiseThresholdConditionSource::maxThreshold)
                        )
                        .apply(p_258995_, SurfaceRules.NoiseThresholdConditionSource::new)
            )
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(final SurfaceRules.Context p_context) {
            final NormalNoise normalnoise = p_context.randomState.getOrCreateNoise(this.noise);

            class NoiseThresholdCondition extends SurfaceRules.LazyXZCondition {
                NoiseThresholdCondition() {
                    super(p_context);
                }

                @Override
                protected boolean compute() {
                    double d0 = normalnoise.getValue((double)this.context.blockX, 0.0, (double)this.context.blockZ);
                    return d0 >= NoiseThresholdConditionSource.this.minThreshold && d0 <= NoiseThresholdConditionSource.this.maxThreshold;
                }
            }

            return new NoiseThresholdCondition();
        }
    }

    static record NotCondition(SurfaceRules.Condition target) implements SurfaceRules.Condition {
        @Override
        public boolean test() {
            return !this.target.test();
        }
    }

    static record NotConditionSource(SurfaceRules.ConditionSource target) implements SurfaceRules.ConditionSource {
        static final KeyDispatchDataCodec<SurfaceRules.NotConditionSource> CODEC = KeyDispatchDataCodec.of(
            SurfaceRules.ConditionSource.CODEC.xmap(SurfaceRules.NotConditionSource::new, SurfaceRules.NotConditionSource::target).fieldOf("invert")
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context context) {
            return new SurfaceRules.NotCondition(this.target.apply(context));
        }
    }

    public interface RuleSource extends Function<SurfaceRules.Context, SurfaceRules.SurfaceRule> {
        Codec<SurfaceRules.RuleSource> CODEC = BuiltInRegistries.MATERIAL_RULE
            .byNameCodec()
            .dispatch(p_338095_ -> p_338095_.codec().codec(), Function.identity());

        static MapCodec<? extends SurfaceRules.RuleSource> bootstrap(Registry<MapCodec<? extends SurfaceRules.RuleSource>> registry) {
            SurfaceRules.register(registry, "bandlands", SurfaceRules.Bandlands.CODEC);
            SurfaceRules.register(registry, "block", SurfaceRules.BlockRuleSource.CODEC);
            SurfaceRules.register(registry, "sequence", SurfaceRules.SequenceRuleSource.CODEC);
            return SurfaceRules.register(registry, "condition", SurfaceRules.TestRuleSource.CODEC);
        }

        KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec();
    }

    static record SequenceRule(List<SurfaceRules.SurfaceRule> rules) implements SurfaceRules.SurfaceRule {
        @Nullable
        @Override
        public BlockState tryApply(int p_189694_, int p_189695_, int p_189696_) {
            for (SurfaceRules.SurfaceRule surfacerules$surfacerule : this.rules) {
                BlockState blockstate = surfacerules$surfacerule.tryApply(p_189694_, p_189695_, p_189696_);
                if (blockstate != null) {
                    return blockstate;
                }
            }

            return null;
        }
    }

    static record SequenceRuleSource(List<SurfaceRules.RuleSource> sequence) implements SurfaceRules.RuleSource {
        static final KeyDispatchDataCodec<SurfaceRules.SequenceRuleSource> CODEC = KeyDispatchDataCodec.of(
            SurfaceRules.RuleSource.CODEC.listOf().xmap(SurfaceRules.SequenceRuleSource::new, SurfaceRules.SequenceRuleSource::sequence).fieldOf("sequence")
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
            return CODEC;
        }

        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
            if (this.sequence.size() == 1) {
                return this.sequence.get(0).apply(context);
            } else {
                Builder<SurfaceRules.SurfaceRule> builder = ImmutableList.builder();

                for (SurfaceRules.RuleSource surfacerules$rulesource : this.sequence) {
                    builder.add(surfacerules$rulesource.apply(context));
                }

                return new SurfaceRules.SequenceRule(builder.build());
            }
        }
    }

    static record StateRule(BlockState state) implements SurfaceRules.SurfaceRule {
        @Override
        public BlockState tryApply(int p_189721_, int p_189722_, int p_189723_) {
            return this.state;
        }
    }

    static enum Steep implements SurfaceRules.ConditionSource {
        INSTANCE;

        static final KeyDispatchDataCodec<SurfaceRules.Steep> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context context) {
            return context.steep;
        }
    }

    static record StoneDepthCheck(int offset, boolean addSurfaceDepth, int secondaryDepthRange, CaveSurface surfaceType)
        implements SurfaceRules.ConditionSource {
        static final KeyDispatchDataCodec<SurfaceRules.StoneDepthCheck> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                p_189753_ -> p_189753_.group(
                            Codec.INT.fieldOf("offset").forGetter(SurfaceRules.StoneDepthCheck::offset),
                            Codec.BOOL.fieldOf("add_surface_depth").forGetter(SurfaceRules.StoneDepthCheck::addSurfaceDepth),
                            Codec.INT.fieldOf("secondary_depth_range").forGetter(SurfaceRules.StoneDepthCheck::secondaryDepthRange),
                            CaveSurface.CODEC.fieldOf("surface_type").forGetter(SurfaceRules.StoneDepthCheck::surfaceType)
                        )
                        .apply(p_189753_, SurfaceRules.StoneDepthCheck::new)
            )
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(final SurfaceRules.Context p_context) {
            final boolean flag = this.surfaceType == CaveSurface.CEILING;

            class StoneDepthCondition extends SurfaceRules.LazyYCondition {
                StoneDepthCondition() {
                    super(p_context);
                }

                @Override
                protected boolean compute() {
                    int i = flag ? this.context.stoneDepthBelow : this.context.stoneDepthAbove;
                    int j = StoneDepthCheck.this.addSurfaceDepth ? this.context.surfaceDepth : 0;
                    int k = StoneDepthCheck.this.secondaryDepthRange == 0
                        ? 0
                        : (int)Mth.map(this.context.getSurfaceSecondary(), -1.0, 1.0, 0.0, (double)StoneDepthCheck.this.secondaryDepthRange);
                    return i <= 1 + StoneDepthCheck.this.offset + j + k;
                }
            }

            return new StoneDepthCondition();
        }
    }

    protected interface SurfaceRule {
        @Nullable
        BlockState tryApply(int x, int y, int z);
    }

    static enum Temperature implements SurfaceRules.ConditionSource {
        INSTANCE;

        static final KeyDispatchDataCodec<SurfaceRules.Temperature> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(SurfaceRules.Context context) {
            return context.temperature;
        }
    }

    static record TestRule(SurfaceRules.Condition condition, SurfaceRules.SurfaceRule followup) implements SurfaceRules.SurfaceRule {
        @Nullable
        @Override
        public BlockState tryApply(int p_189805_, int p_189806_, int p_189807_) {
            return !this.condition.test() ? null : this.followup.tryApply(p_189805_, p_189806_, p_189807_);
        }
    }

    static record TestRuleSource(SurfaceRules.ConditionSource ifTrue, SurfaceRules.RuleSource thenRun) implements SurfaceRules.RuleSource {
        static final KeyDispatchDataCodec<SurfaceRules.TestRuleSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                p_189817_ -> p_189817_.group(
                            SurfaceRules.ConditionSource.CODEC.fieldOf("if_true").forGetter(SurfaceRules.TestRuleSource::ifTrue),
                            SurfaceRules.RuleSource.CODEC.fieldOf("then_run").forGetter(SurfaceRules.TestRuleSource::thenRun)
                        )
                        .apply(p_189817_, SurfaceRules.TestRuleSource::new)
            )
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
            return CODEC;
        }

        public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
            return new SurfaceRules.TestRule(this.ifTrue.apply(context), this.thenRun.apply(context));
        }
    }

    static record VerticalGradientConditionSource(ResourceLocation randomName, VerticalAnchor trueAtAndBelow, VerticalAnchor falseAtAndAbove)
        implements SurfaceRules.ConditionSource {
        static final KeyDispatchDataCodec<SurfaceRules.VerticalGradientConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                p_189839_ -> p_189839_.group(
                            ResourceLocation.CODEC.fieldOf("random_name").forGetter(SurfaceRules.VerticalGradientConditionSource::randomName),
                            VerticalAnchor.CODEC.fieldOf("true_at_and_below").forGetter(SurfaceRules.VerticalGradientConditionSource::trueAtAndBelow),
                            VerticalAnchor.CODEC.fieldOf("false_at_and_above").forGetter(SurfaceRules.VerticalGradientConditionSource::falseAtAndAbove)
                        )
                        .apply(p_189839_, SurfaceRules.VerticalGradientConditionSource::new)
            )
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(final SurfaceRules.Context p_context) {
            final int i = this.trueAtAndBelow().resolveY(p_context.context);
            final int j = this.falseAtAndAbove().resolveY(p_context.context);
            final PositionalRandomFactory positionalrandomfactory = p_context.randomState.getOrCreateRandomFactory(this.randomName());

            class VerticalGradientCondition extends SurfaceRules.LazyYCondition {
                VerticalGradientCondition() {
                    super(p_context);
                }

                @Override
                protected boolean compute() {
                    int k = this.context.blockY;
                    if (k <= i) {
                        return true;
                    } else if (k >= j) {
                        return false;
                    } else {
                        double d0 = Mth.map((double)k, (double)i, (double)j, 1.0, 0.0);
                        RandomSource randomsource = positionalrandomfactory.at(this.context.blockX, k, this.context.blockZ);
                        return (double)randomsource.nextFloat() < d0;
                    }
                }
            }

            return new VerticalGradientCondition();
        }
    }

    static record WaterConditionSource(int offset, int surfaceDepthMultiplier, boolean addStoneDepth) implements SurfaceRules.ConditionSource {
        static final KeyDispatchDataCodec<SurfaceRules.WaterConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                p_189874_ -> p_189874_.group(
                            Codec.INT.fieldOf("offset").forGetter(SurfaceRules.WaterConditionSource::offset),
                            Codec.intRange(-20, 20).fieldOf("surface_depth_multiplier").forGetter(SurfaceRules.WaterConditionSource::surfaceDepthMultiplier),
                            Codec.BOOL.fieldOf("add_stone_depth").forGetter(SurfaceRules.WaterConditionSource::addStoneDepth)
                        )
                        .apply(p_189874_, SurfaceRules.WaterConditionSource::new)
            )
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(final SurfaceRules.Context p_context) {
            class WaterCondition extends SurfaceRules.LazyYCondition {
                WaterCondition() {
                    super(p_context);
                }

                @Override
                protected boolean compute() {
                    return this.context.waterHeight == Integer.MIN_VALUE
                        || this.context.blockY + (WaterConditionSource.this.addStoneDepth ? this.context.stoneDepthAbove : 0)
                            >= this.context.waterHeight
                                + WaterConditionSource.this.offset
                                + this.context.surfaceDepth * WaterConditionSource.this.surfaceDepthMultiplier;
                }
            }

            return new WaterCondition();
        }
    }

    static record YConditionSource(VerticalAnchor anchor, int surfaceDepthMultiplier, boolean addStoneDepth) implements SurfaceRules.ConditionSource {
        static final KeyDispatchDataCodec<SurfaceRules.YConditionSource> CODEC = KeyDispatchDataCodec.of(
            RecordCodecBuilder.mapCodec(
                p_189455_ -> p_189455_.group(
                            VerticalAnchor.CODEC.fieldOf("anchor").forGetter(SurfaceRules.YConditionSource::anchor),
                            Codec.intRange(-20, 20).fieldOf("surface_depth_multiplier").forGetter(SurfaceRules.YConditionSource::surfaceDepthMultiplier),
                            Codec.BOOL.fieldOf("add_stone_depth").forGetter(SurfaceRules.YConditionSource::addStoneDepth)
                        )
                        .apply(p_189455_, SurfaceRules.YConditionSource::new)
            )
        );

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }

        public SurfaceRules.Condition apply(final SurfaceRules.Context p_context) {
            class YCondition extends SurfaceRules.LazyYCondition {
                YCondition() {
                    super(p_context);
                }

                @Override
                protected boolean compute() {
                    return this.context.blockY + (YConditionSource.this.addStoneDepth ? this.context.stoneDepthAbove : 0)
                        >= YConditionSource.this.anchor.resolveY(this.context.context)
                            + this.context.surfaceDepth * YConditionSource.this.surfaceDepthMultiplier;
                }
            }

            return new YCondition();
        }
    }
}
