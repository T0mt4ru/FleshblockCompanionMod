package net.minecraft.world.level.gameevent;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityPositionSource implements PositionSource {
    public static final MapCodec<EntityPositionSource> CODEC = RecordCodecBuilder.mapCodec(
        p_253607_ -> p_253607_.group(
                    UUIDUtil.CODEC.fieldOf("source_entity").forGetter(EntityPositionSource::getUuid),
                    Codec.FLOAT.fieldOf("y_offset").orElse(0.0F).forGetter(p_223666_ -> p_223666_.yOffset)
                )
                .apply(p_253607_, (p_223672_, p_223673_) -> new EntityPositionSource(Either.right(Either.left(p_223672_)), p_223673_))
    );
    public static final StreamCodec<ByteBuf, EntityPositionSource> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        EntityPositionSource::getId,
        ByteBufCodecs.FLOAT,
        p_319761_ -> p_319761_.yOffset,
        (p_319759_, p_319760_) -> new EntityPositionSource(Either.right(Either.right(p_319759_)), p_319760_)
    );
    private Either<Entity, Either<UUID, Integer>> entityOrUuidOrId;
    private final float yOffset;

    public EntityPositionSource(Entity entity, float yOffset) {
        this(Either.left(entity), yOffset);
    }

    private EntityPositionSource(Either<Entity, Either<UUID, Integer>> entityOrUuidOrId, float yOffset) {
        this.entityOrUuidOrId = entityOrUuidOrId;
        this.yOffset = yOffset;
    }

    @Override
    public Optional<Vec3> getPosition(Level level) {
        if (this.entityOrUuidOrId.left().isEmpty()) {
            this.resolveEntity(level);
        }

        return this.entityOrUuidOrId.left().map(p_223676_ -> p_223676_.position().add(0.0, (double)this.yOffset, 0.0));
    }

    private void resolveEntity(Level level) {
        this.entityOrUuidOrId
            .map(
                Optional::of,
                p_223657_ -> Optional.ofNullable(
                        p_223657_.map(p_223660_ -> level instanceof ServerLevel serverlevel ? serverlevel.getEntity(p_223660_) : null, level::getEntity)
                    )
            )
            .ifPresent(p_223654_ -> this.entityOrUuidOrId = Either.left(p_223654_));
    }

    private UUID getUuid() {
        return this.entityOrUuidOrId.map(Entity::getUUID, p_223680_ -> p_223680_.map(Function.identity(), p_223668_ -> {
                throw new RuntimeException("Unable to get entityId from uuid");
            }));
    }

    private int getId() {
        return this.entityOrUuidOrId.map(Entity::getId, p_223662_ -> p_223662_.map(p_223670_ -> {
                throw new IllegalStateException("Unable to get entityId from uuid");
            }, Function.identity()));
    }

    @Override
    public PositionSourceType<EntityPositionSource> getType() {
        return PositionSourceType.ENTITY;
    }

    public static class Type implements PositionSourceType<EntityPositionSource> {
        @Override
        public MapCodec<EntityPositionSource> codec() {
            return EntityPositionSource.CODEC;
        }

        @Override
        public StreamCodec<ByteBuf, EntityPositionSource> streamCodec() {
            return EntityPositionSource.STREAM_CODEC;
        }
    }
}
