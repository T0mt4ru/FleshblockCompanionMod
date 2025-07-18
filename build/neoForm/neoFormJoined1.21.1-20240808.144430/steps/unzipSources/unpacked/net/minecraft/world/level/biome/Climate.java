package net.minecraft.world.level.biome;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public class Climate {
    private static final boolean DEBUG_SLOW_BIOME_SEARCH = false;
    private static final float QUANTIZATION_FACTOR = 10000.0F;
    @VisibleForTesting
    protected static final int PARAMETER_COUNT = 7;

    public static Climate.TargetPoint target(float temperature, float humidity, float continentalness, float erosion, float depth, float weirdness) {
        return new Climate.TargetPoint(
            quantizeCoord(temperature),
            quantizeCoord(humidity),
            quantizeCoord(continentalness),
            quantizeCoord(erosion),
            quantizeCoord(depth),
            quantizeCoord(weirdness)
        );
    }

    public static Climate.ParameterPoint parameters(
        float temperature, float humidity, float continentalness, float erosion, float depth, float weirdness, float offset
    ) {
        return new Climate.ParameterPoint(
            Climate.Parameter.point(temperature),
            Climate.Parameter.point(humidity),
            Climate.Parameter.point(continentalness),
            Climate.Parameter.point(erosion),
            Climate.Parameter.point(depth),
            Climate.Parameter.point(weirdness),
            quantizeCoord(offset)
        );
    }

    public static Climate.ParameterPoint parameters(
        Climate.Parameter temperature,
        Climate.Parameter humidity,
        Climate.Parameter continentalness,
        Climate.Parameter erosion,
        Climate.Parameter depth,
        Climate.Parameter weirdness,
        float offset
    ) {
        return new Climate.ParameterPoint(temperature, humidity, continentalness, erosion, depth, weirdness, quantizeCoord(offset));
    }

    public static long quantizeCoord(float coord) {
        return (long)(coord * 10000.0F);
    }

    public static float unquantizeCoord(long coord) {
        return (float)coord / 10000.0F;
    }

    public static Climate.Sampler empty() {
        DensityFunction densityfunction = DensityFunctions.zero();
        return new Climate.Sampler(densityfunction, densityfunction, densityfunction, densityfunction, densityfunction, densityfunction, List.of());
    }

    public static BlockPos findSpawnPosition(List<Climate.ParameterPoint> points, Climate.Sampler sampler) {
        return (new Climate.SpawnFinder(points, sampler)).result.location();
    }

    interface DistanceMetric<T> {
        long distance(Climate.RTree.Node<T> node, long[] searchedValues);
    }

    public static record Parameter(long min, long max) {
        public static final Codec<Climate.Parameter> CODEC = ExtraCodecs.intervalCodec(
            Codec.floatRange(-2.0F, 2.0F),
            "min",
            "max",
            (p_275164_, p_275165_) -> p_275164_.compareTo(p_275165_) > 0
                    ? DataResult.error(() -> "Cannon construct interval, min > max (" + p_275164_ + " > " + p_275165_ + ")")
                    : DataResult.success(new Climate.Parameter(Climate.quantizeCoord(p_275164_), Climate.quantizeCoord(p_275165_))),
            p_186841_ -> Climate.unquantizeCoord(p_186841_.min()),
            p_186839_ -> Climate.unquantizeCoord(p_186839_.max())
        );

        public static Climate.Parameter point(float value) {
            return span(value, value);
        }

        public static Climate.Parameter span(float min, float max) {
            if (min > max) {
                throw new IllegalArgumentException("min > max: " + min + " " + max);
            } else {
                return new Climate.Parameter(Climate.quantizeCoord(min), Climate.quantizeCoord(max));
            }
        }

        public static Climate.Parameter span(Climate.Parameter min, Climate.Parameter max) {
            if (min.min() > max.max()) {
                throw new IllegalArgumentException("min > max: " + min + " " + max);
            } else {
                return new Climate.Parameter(min.min(), max.max());
            }
        }

        @Override
        public String toString() {
            return this.min == this.max ? String.format(Locale.ROOT, "%d", this.min) : String.format(Locale.ROOT, "[%d-%d]", this.min, this.max);
        }

        public long distance(long pointValue) {
            long i = pointValue - this.max;
            long j = this.min - pointValue;
            return i > 0L ? i : Math.max(j, 0L);
        }

        public long distance(Climate.Parameter parameter) {
            long i = parameter.min() - this.max;
            long j = this.min - parameter.max();
            return i > 0L ? i : Math.max(j, 0L);
        }

        public Climate.Parameter span(@Nullable Climate.Parameter param) {
            return param == null ? this : new Climate.Parameter(Math.min(this.min, param.min()), Math.max(this.max, param.max()));
        }
    }

    public static class ParameterList<T> {
        private final List<Pair<Climate.ParameterPoint, T>> values;
        private final Climate.RTree<T> index;

        public static <T> Codec<Climate.ParameterList<T>> codec(MapCodec<T> codec) {
            return ExtraCodecs.nonEmptyList(
                    RecordCodecBuilder.<Pair<Climate.ParameterPoint, T>>create(
                            p_275233_ -> p_275233_.group(
                                        Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), codec.forGetter(Pair::getSecond)
                                    )
                                    .apply(p_275233_, Pair::of)
                        )
                        .listOf()
                )
                .xmap(Climate.ParameterList::new, Climate.ParameterList::values);
        }

        public ParameterList(List<Pair<Climate.ParameterPoint, T>> values) {
            this.values = values;
            this.index = Climate.RTree.create(values);
        }

        public List<Pair<Climate.ParameterPoint, T>> values() {
            return this.values;
        }

        public T findValue(Climate.TargetPoint targetPoint) {
            return this.findValueIndex(targetPoint);
        }

        @VisibleForTesting
        public T findValueBruteForce(Climate.TargetPoint targetPoint) {
            Iterator<Pair<Climate.ParameterPoint, T>> iterator = this.values().iterator();
            Pair<Climate.ParameterPoint, T> pair = iterator.next();
            long i = pair.getFirst().fitness(targetPoint);
            T t = pair.getSecond();

            while (iterator.hasNext()) {
                Pair<Climate.ParameterPoint, T> pair1 = iterator.next();
                long j = pair1.getFirst().fitness(targetPoint);
                if (j < i) {
                    i = j;
                    t = pair1.getSecond();
                }
            }

            return t;
        }

        public T findValueIndex(Climate.TargetPoint targetPoint) {
            return this.findValueIndex(targetPoint, Climate.RTree.Node::distance);
        }

        protected T findValueIndex(Climate.TargetPoint targetPoint, Climate.DistanceMetric<T> distanceMetric) {
            return this.index.search(targetPoint, distanceMetric);
        }
    }

    public static record ParameterPoint(
        Climate.Parameter temperature,
        Climate.Parameter humidity,
        Climate.Parameter continentalness,
        Climate.Parameter erosion,
        Climate.Parameter depth,
        Climate.Parameter weirdness,
        long offset
    ) {
        public static final Codec<Climate.ParameterPoint> CODEC = RecordCodecBuilder.create(
            p_186885_ -> p_186885_.group(
                        Climate.Parameter.CODEC.fieldOf("temperature").forGetter(p_186905_ -> p_186905_.temperature),
                        Climate.Parameter.CODEC.fieldOf("humidity").forGetter(p_186902_ -> p_186902_.humidity),
                        Climate.Parameter.CODEC.fieldOf("continentalness").forGetter(p_186897_ -> p_186897_.continentalness),
                        Climate.Parameter.CODEC.fieldOf("erosion").forGetter(p_186894_ -> p_186894_.erosion),
                        Climate.Parameter.CODEC.fieldOf("depth").forGetter(p_186891_ -> p_186891_.depth),
                        Climate.Parameter.CODEC.fieldOf("weirdness").forGetter(p_186888_ -> p_186888_.weirdness),
                        Codec.floatRange(0.0F, 1.0F)
                            .fieldOf("offset")
                            .xmap(Climate::quantizeCoord, Climate::unquantizeCoord)
                            .forGetter(p_186881_ -> p_186881_.offset)
                    )
                    .apply(p_186885_, Climate.ParameterPoint::new)
        );

        long fitness(Climate.TargetPoint point) {
            return Mth.square(this.temperature.distance(point.temperature))
                + Mth.square(this.humidity.distance(point.humidity))
                + Mth.square(this.continentalness.distance(point.continentalness))
                + Mth.square(this.erosion.distance(point.erosion))
                + Mth.square(this.depth.distance(point.depth))
                + Mth.square(this.weirdness.distance(point.weirdness))
                + Mth.square(this.offset);
        }

        protected List<Climate.Parameter> parameterSpace() {
            return ImmutableList.of(
                this.temperature,
                this.humidity,
                this.continentalness,
                this.erosion,
                this.depth,
                this.weirdness,
                new Climate.Parameter(this.offset, this.offset)
            );
        }
    }

    protected static final class RTree<T> {
        private static final int CHILDREN_PER_NODE = 6;
        private final Climate.RTree.Node<T> root;
        private final ThreadLocal<Climate.RTree.Leaf<T>> lastResult = new ThreadLocal<>();

        private RTree(Climate.RTree.Node<T> root) {
            this.root = root;
        }

        public static <T> Climate.RTree<T> create(List<Pair<Climate.ParameterPoint, T>> nodes) {
            if (nodes.isEmpty()) {
                throw new IllegalArgumentException("Need at least one value to build the search tree.");
            } else {
                int i = nodes.get(0).getFirst().parameterSpace().size();
                if (i != 7) {
                    throw new IllegalStateException("Expecting parameter space to be 7, got " + i);
                } else {
                    List<Climate.RTree.Leaf<T>> list = nodes.stream()
                        .map(p_186934_ -> new Climate.RTree.Leaf<T>(p_186934_.getFirst(), p_186934_.getSecond()))
                        .collect(Collectors.toCollection(ArrayList::new));
                    return new Climate.RTree<>(build(i, list));
                }
            }
        }

        private static <T> Climate.RTree.Node<T> build(int paramSpaceSize, List<? extends Climate.RTree.Node<T>> children) {
            if (children.isEmpty()) {
                throw new IllegalStateException("Need at least one child to build a node");
            } else if (children.size() == 1) {
                return (Climate.RTree.Node<T>)children.get(0);
            } else if (children.size() <= 6) {
                children.sort(Comparator.comparingLong(p_186916_ -> {
                    long i1 = 0L;

                    for (int j1 = 0; j1 < paramSpaceSize; j1++) {
                        Climate.Parameter climate$parameter = p_186916_.parameterSpace[j1];
                        i1 += Math.abs((climate$parameter.min() + climate$parameter.max()) / 2L);
                    }

                    return i1;
                }));
                return new Climate.RTree.SubTree<>(children);
            } else {
                long i = Long.MAX_VALUE;
                int j = -1;
                List<Climate.RTree.SubTree<T>> list = null;

                for (int k = 0; k < paramSpaceSize; k++) {
                    sort(children, paramSpaceSize, k, false);
                    List<Climate.RTree.SubTree<T>> list1 = bucketize(children);
                    long l = 0L;

                    for (Climate.RTree.SubTree<T> subtree : list1) {
                        l += cost(subtree.parameterSpace);
                    }

                    if (i > l) {
                        i = l;
                        j = k;
                        list = list1;
                    }
                }

                sort(list, paramSpaceSize, j, true);
                return new Climate.RTree.SubTree<>(
                    list.stream().map(p_186919_ -> build(paramSpaceSize, Arrays.asList(p_186919_.children))).collect(Collectors.toList())
                );
            }
        }

        private static <T> void sort(List<? extends Climate.RTree.Node<T>> children, int paramSpaceSize, int size, boolean absolute) {
            Comparator<Climate.RTree.Node<T>> comparator = comparator(size, absolute);

            for (int i = 1; i < paramSpaceSize; i++) {
                comparator = comparator.thenComparing(comparator((size + i) % paramSpaceSize, absolute));
            }

            children.sort(comparator);
        }

        private static <T> Comparator<Climate.RTree.Node<T>> comparator(int size, boolean absolute) {
            return Comparator.comparingLong(p_186929_ -> {
                Climate.Parameter climate$parameter = p_186929_.parameterSpace[size];
                long i = (climate$parameter.min() + climate$parameter.max()) / 2L;
                return absolute ? Math.abs(i) : i;
            });
        }

        private static <T> List<Climate.RTree.SubTree<T>> bucketize(List<? extends Climate.RTree.Node<T>> nodes) {
            List<Climate.RTree.SubTree<T>> list = Lists.newArrayList();
            List<Climate.RTree.Node<T>> list1 = Lists.newArrayList();
            int i = (int)Math.pow(6.0, Math.floor(Math.log((double)nodes.size() - 0.01) / Math.log(6.0)));

            for (Climate.RTree.Node<T> node : nodes) {
                list1.add(node);
                if (list1.size() >= i) {
                    list.add(new Climate.RTree.SubTree<>(list1));
                    list1 = Lists.newArrayList();
                }
            }

            if (!list1.isEmpty()) {
                list.add(new Climate.RTree.SubTree<>(list1));
            }

            return list;
        }

        private static long cost(Climate.Parameter[] parameters) {
            long i = 0L;

            for (Climate.Parameter climate$parameter : parameters) {
                i += Math.abs(climate$parameter.max() - climate$parameter.min());
            }

            return i;
        }

        static <T> List<Climate.Parameter> buildParameterSpace(List<? extends Climate.RTree.Node<T>> children) {
            if (children.isEmpty()) {
                throw new IllegalArgumentException("SubTree needs at least one child");
            } else {
                int i = 7;
                List<Climate.Parameter> list = Lists.newArrayList();

                for (int j = 0; j < 7; j++) {
                    list.add(null);
                }

                for (Climate.RTree.Node<T> node : children) {
                    for (int k = 0; k < 7; k++) {
                        list.set(k, node.parameterSpace[k].span(list.get(k)));
                    }
                }

                return list;
            }
        }

        public T search(Climate.TargetPoint targetPoint, Climate.DistanceMetric<T> distanceMetric) {
            long[] along = targetPoint.toParameterArray();
            Climate.RTree.Leaf<T> leaf = this.root.search(along, this.lastResult.get(), distanceMetric);
            this.lastResult.set(leaf);
            return leaf.value;
        }

        static final class Leaf<T> extends Climate.RTree.Node<T> {
            final T value;

            Leaf(Climate.ParameterPoint point, T value) {
                super(point.parameterSpace());
                this.value = value;
            }

            @Override
            protected Climate.RTree.Leaf<T> search(long[] searchedValues, @Nullable Climate.RTree.Leaf<T> leaf, Climate.DistanceMetric<T> metric) {
                return this;
            }
        }

        abstract static class Node<T> {
            protected final Climate.Parameter[] parameterSpace;

            protected Node(List<Climate.Parameter> parameters) {
                this.parameterSpace = parameters.toArray(new Climate.Parameter[0]);
            }

            protected abstract Climate.RTree.Leaf<T> search(long[] searchedValues, @Nullable Climate.RTree.Leaf<T> leaf, Climate.DistanceMetric<T> metric);

            protected long distance(long[] values) {
                long i = 0L;

                for (int j = 0; j < 7; j++) {
                    i += Mth.square(this.parameterSpace[j].distance(values[j]));
                }

                return i;
            }

            @Override
            public String toString() {
                return Arrays.toString((Object[])this.parameterSpace);
            }
        }

        static final class SubTree<T> extends Climate.RTree.Node<T> {
            final Climate.RTree.Node<T>[] children;

            protected SubTree(List<? extends Climate.RTree.Node<T>> parameters) {
                this(Climate.RTree.buildParameterSpace(parameters), parameters);
            }

            protected SubTree(List<Climate.Parameter> parameters, List<? extends Climate.RTree.Node<T>> children) {
                super(parameters);
                this.children = children.toArray(new Climate.RTree.Node[0]);
            }

            @Override
            protected Climate.RTree.Leaf<T> search(long[] searchedValues, @Nullable Climate.RTree.Leaf<T> p_leaf, Climate.DistanceMetric<T> metric) {
                long i = p_leaf == null ? Long.MAX_VALUE : metric.distance(p_leaf, searchedValues);
                Climate.RTree.Leaf<T> leaf = p_leaf;

                for (Climate.RTree.Node<T> node : this.children) {
                    long j = metric.distance(node, searchedValues);
                    if (i > j) {
                        Climate.RTree.Leaf<T> leaf1 = node.search(searchedValues, leaf, metric);
                        long k = node == leaf1 ? j : metric.distance(leaf1, searchedValues);
                        if (i > k) {
                            i = k;
                            leaf = leaf1;
                        }
                    }
                }

                return leaf;
            }
        }
    }

    public static record Sampler(
        DensityFunction temperature,
        DensityFunction humidity,
        DensityFunction continentalness,
        DensityFunction erosion,
        DensityFunction depth,
        DensityFunction weirdness,
        List<Climate.ParameterPoint> spawnTarget
    ) {
        public Climate.TargetPoint sample(int x, int y, int z) {
            int i = QuartPos.toBlock(x);
            int j = QuartPos.toBlock(y);
            int k = QuartPos.toBlock(z);
            DensityFunction.SinglePointContext densityfunction$singlepointcontext = new DensityFunction.SinglePointContext(i, j, k);
            return Climate.target(
                (float)this.temperature.compute(densityfunction$singlepointcontext),
                (float)this.humidity.compute(densityfunction$singlepointcontext),
                (float)this.continentalness.compute(densityfunction$singlepointcontext),
                (float)this.erosion.compute(densityfunction$singlepointcontext),
                (float)this.depth.compute(densityfunction$singlepointcontext),
                (float)this.weirdness.compute(densityfunction$singlepointcontext)
            );
        }

        public BlockPos findSpawnPosition() {
            return this.spawnTarget.isEmpty() ? BlockPos.ZERO : Climate.findSpawnPosition(this.spawnTarget, this);
        }
    }

    static class SpawnFinder {
        Climate.SpawnFinder.Result result;

        SpawnFinder(List<Climate.ParameterPoint> points, Climate.Sampler sampler) {
            this.result = getSpawnPositionAndFitness(points, sampler, 0, 0);
            this.radialSearch(points, sampler, 2048.0F, 512.0F);
            this.radialSearch(points, sampler, 512.0F, 32.0F);
        }

        private void radialSearch(List<Climate.ParameterPoint> point, Climate.Sampler sampler, float max, float min) {
            float f = 0.0F;
            float f1 = min;
            BlockPos blockpos = this.result.location();

            while (f1 <= max) {
                int i = blockpos.getX() + (int)(Math.sin((double)f) * (double)f1);
                int j = blockpos.getZ() + (int)(Math.cos((double)f) * (double)f1);
                Climate.SpawnFinder.Result climate$spawnfinder$result = getSpawnPositionAndFitness(point, sampler, i, j);
                if (climate$spawnfinder$result.fitness() < this.result.fitness()) {
                    this.result = climate$spawnfinder$result;
                }

                f += min / f1;
                if ((double)f > Math.PI * 2) {
                    f = 0.0F;
                    f1 += min;
                }
            }
        }

        private static Climate.SpawnFinder.Result getSpawnPositionAndFitness(
            List<Climate.ParameterPoint> points, Climate.Sampler sampler, int x, int z
        ) {
            double d0 = Mth.square(2500.0);
            int i = 2;
            long j = (long)((double)Mth.square(10000.0F) * Math.pow((double)(Mth.square((long)x) + Mth.square((long)z)) / d0, 2.0));
            Climate.TargetPoint climate$targetpoint = sampler.sample(QuartPos.fromBlock(x), 0, QuartPos.fromBlock(z));
            Climate.TargetPoint climate$targetpoint1 = new Climate.TargetPoint(
                climate$targetpoint.temperature(),
                climate$targetpoint.humidity(),
                climate$targetpoint.continentalness(),
                climate$targetpoint.erosion(),
                0L,
                climate$targetpoint.weirdness()
            );
            long k = Long.MAX_VALUE;

            for (Climate.ParameterPoint climate$parameterpoint : points) {
                k = Math.min(k, climate$parameterpoint.fitness(climate$targetpoint1));
            }

            return new Climate.SpawnFinder.Result(new BlockPos(x, 0, z), j + k);
        }

        static record Result(BlockPos location, long fitness) {
        }
    }

    public static record TargetPoint(long temperature, long humidity, long continentalness, long erosion, long depth, long weirdness) {
        @VisibleForTesting
        protected long[] toParameterArray() {
            return new long[]{this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, 0L};
        }
    }
}
