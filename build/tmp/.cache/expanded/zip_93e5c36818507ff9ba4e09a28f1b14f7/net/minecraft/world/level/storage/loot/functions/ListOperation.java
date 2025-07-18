package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.slf4j.Logger;

public interface ListOperation {
    MapCodec<ListOperation> UNLIMITED_CODEC = codec(Integer.MAX_VALUE);

    static MapCodec<ListOperation> codec(int maxSize) {
        return ListOperation.Type.CODEC.<ListOperation>dispatchMap("mode", ListOperation::mode, p_338141_ -> p_338141_.mapCodec).validate(p_335630_ -> {
            if (p_335630_ instanceof ListOperation.ReplaceSection listoperation$replacesection && listoperation$replacesection.size().isPresent()) {
                int i = listoperation$replacesection.size().get();
                if (i > maxSize) {
                    return DataResult.error(() -> "Size value too large: " + i + ", max size is " + maxSize);
                }
            }

            return DataResult.success(p_335630_);
        });
    }

    ListOperation.Type mode();

    default <T> List<T> apply(List<T> currentValue, List<T> operand) {
        return this.apply(currentValue, operand, Integer.MAX_VALUE);
    }

    <T> List<T> apply(List<T> currentValue, List<T> operand, int maxSize);

    public static class Append implements ListOperation {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final ListOperation.Append INSTANCE = new ListOperation.Append();
        public static final MapCodec<ListOperation.Append> MAP_CODEC = MapCodec.unit(() -> INSTANCE);

        private Append() {
        }

        @Override
        public ListOperation.Type mode() {
            return ListOperation.Type.APPEND;
        }

        @Override
        public <T> List<T> apply(List<T> p_333898_, List<T> p_333849_, int p_333915_) {
            if (p_333898_.size() + p_333849_.size() > p_333915_) {
                LOGGER.error("Contents overflow in section append");
                return p_333898_;
            } else {
                return Stream.concat(p_333898_.stream(), p_333849_.stream()).toList();
            }
        }
    }

    public static record Insert(int offset) implements ListOperation {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<ListOperation.Insert> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_338142_ -> p_338142_.group(ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("offset", 0).forGetter(ListOperation.Insert::offset))
                    .apply(p_338142_, ListOperation.Insert::new)
        );

        @Override
        public ListOperation.Type mode() {
            return ListOperation.Type.INSERT;
        }

        @Override
        public <T> List<T> apply(List<T> currentValue, List<T> operand, int maxSize) {
            int i = currentValue.size();
            if (this.offset > i) {
                LOGGER.error("Cannot insert when offset is out of bounds");
                return currentValue;
            } else if (i + operand.size() > maxSize) {
                LOGGER.error("Contents overflow in section insertion");
                return currentValue;
            } else {
                Builder<T> builder = ImmutableList.builder();
                builder.addAll(currentValue.subList(0, this.offset));
                builder.addAll(operand);
                builder.addAll(currentValue.subList(this.offset, i));
                return builder.build();
            }
        }
    }

    public static class ReplaceAll implements ListOperation {
        public static final ListOperation.ReplaceAll INSTANCE = new ListOperation.ReplaceAll();
        public static final MapCodec<ListOperation.ReplaceAll> MAP_CODEC = MapCodec.unit(() -> INSTANCE);

        private ReplaceAll() {
        }

        @Override
        public ListOperation.Type mode() {
            return ListOperation.Type.REPLACE_ALL;
        }

        @Override
        public <T> List<T> apply(List<T> p_333855_, List<T> p_333756_, int p_333945_) {
            return p_333756_;
        }
    }

    public static record ReplaceSection(int offset, Optional<Integer> size) implements ListOperation {
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final MapCodec<ListOperation.ReplaceSection> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_338143_ -> p_338143_.group(
                        ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("offset", 0).forGetter(ListOperation.ReplaceSection::offset),
                        ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("size").forGetter(ListOperation.ReplaceSection::size)
                    )
                    .apply(p_338143_, ListOperation.ReplaceSection::new)
        );

        public ReplaceSection(int p_333961_) {
            this(p_333961_, Optional.empty());
        }

        @Override
        public ListOperation.Type mode() {
            return ListOperation.Type.REPLACE_SECTION;
        }

        @Override
        public <T> List<T> apply(List<T> currentValue, List<T> operand, int maxSize) {
            int i = currentValue.size();
            if (this.offset > i) {
                LOGGER.error("Cannot replace when offset is out of bounds");
                return currentValue;
            } else {
                Builder<T> builder = ImmutableList.builder();
                builder.addAll(currentValue.subList(0, this.offset));
                builder.addAll(operand);
                int j = this.offset + this.size.orElse(operand.size());
                if (j < i) {
                    builder.addAll(currentValue.subList(j, i));
                }

                List<T> list = builder.build();
                if (list.size() > maxSize) {
                    LOGGER.error("Contents overflow in section replacement");
                    return currentValue;
                } else {
                    return list;
                }
            }
        }
    }

    public static record StandAlone<T>(List<T> value, ListOperation operation) {
        public static <T> Codec<ListOperation.StandAlone<T>> codec(Codec<T> elementCodec, int maxSize) {
            return RecordCodecBuilder.create(
                p_341682_ -> p_341682_.group(
                            elementCodec.sizeLimitedListOf(maxSize).fieldOf("values").forGetter(p_341601_ -> p_341601_.value),
                            ListOperation.codec(maxSize).forGetter(p_341647_ -> p_341647_.operation)
                        )
                        .apply(p_341682_, ListOperation.StandAlone::new)
            );
        }

        public List<T> apply(List<T> list) {
            return this.operation.apply(list, this.value);
        }
    }

    public static enum Type implements StringRepresentable {
        REPLACE_ALL("replace_all", ListOperation.ReplaceAll.MAP_CODEC),
        REPLACE_SECTION("replace_section", ListOperation.ReplaceSection.MAP_CODEC),
        INSERT("insert", ListOperation.Insert.MAP_CODEC),
        APPEND("append", ListOperation.Append.MAP_CODEC);

        public static final Codec<ListOperation.Type> CODEC = StringRepresentable.fromEnum(ListOperation.Type::values);
        private final String id;
        final MapCodec<? extends ListOperation> mapCodec;

        private Type(String id, MapCodec<? extends ListOperation> mapCodec) {
            this.id = id;
            this.mapCodec = mapCodec;
        }

        public MapCodec<? extends ListOperation> mapCodec() {
            return this.mapCodec;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }
}
