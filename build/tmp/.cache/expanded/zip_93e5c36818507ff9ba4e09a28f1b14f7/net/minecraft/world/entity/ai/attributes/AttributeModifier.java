package net.minecraft.world.entity.ai.attributes;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.slf4j.Logger;

public record AttributeModifier(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<AttributeModifier> MAP_CODEC = RecordCodecBuilder.mapCodec(
        p_349989_ -> p_349989_.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(AttributeModifier::id),
                    Codec.DOUBLE.fieldOf("amount").forGetter(AttributeModifier::amount),
                    AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(AttributeModifier::operation)
                )
                .apply(p_349989_, AttributeModifier::new)
    );
    public static final Codec<AttributeModifier> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf, AttributeModifier> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        AttributeModifier::id,
        ByteBufCodecs.DOUBLE,
        AttributeModifier::amount,
        AttributeModifier.Operation.STREAM_CODEC,
        AttributeModifier::operation,
        AttributeModifier::new
    );

    public CompoundTag save() {
        DataResult<Tag> dataresult = CODEC.encode(this, NbtOps.INSTANCE, new CompoundTag());
        return (CompoundTag)dataresult.getOrThrow();
    }

    @Nullable
    public static AttributeModifier load(CompoundTag nbt) {
        DataResult<AttributeModifier> dataresult = CODEC.parse(NbtOps.INSTANCE, nbt);
        if (dataresult.isSuccess()) {
            return dataresult.getOrThrow();
        } else {
            LOGGER.warn("Unable to create attribute: {}", dataresult.error().get().message());
            return null;
        }
    }

    public boolean is(ResourceLocation id) {
        return id.equals(this.id);
    }

    public static enum Operation implements StringRepresentable {
        ADD_VALUE("add_value", 0),
        ADD_MULTIPLIED_BASE("add_multiplied_base", 1),
        ADD_MULTIPLIED_TOTAL("add_multiplied_total", 2);

        public static final IntFunction<AttributeModifier.Operation> BY_ID = ByIdMap.continuous(
            AttributeModifier.Operation::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO
        );
        public static final StreamCodec<ByteBuf, AttributeModifier.Operation> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, AttributeModifier.Operation::id);
        public static final Codec<AttributeModifier.Operation> CODEC = StringRepresentable.fromEnum(AttributeModifier.Operation::values);
        private final String name;
        private final int id;

        private Operation(String name, int value) {
            this.name = name;
            this.id = value;
        }

        public int id() {
            return this.id;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
