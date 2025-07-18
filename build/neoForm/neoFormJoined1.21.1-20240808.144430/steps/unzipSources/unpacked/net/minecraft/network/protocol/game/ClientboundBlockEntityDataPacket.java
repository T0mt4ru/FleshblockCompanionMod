package net.minecraft.network.protocol.game;

import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ClientboundBlockEntityDataPacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBlockEntityDataPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ClientboundBlockEntityDataPacket::getPos,
        ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE),
        ClientboundBlockEntityDataPacket::getType,
        ByteBufCodecs.TRUSTED_COMPOUND_TAG,
        ClientboundBlockEntityDataPacket::getTag,
        ClientboundBlockEntityDataPacket::new
    );
    private final BlockPos pos;
    /**
     * Used only for vanilla block entities
     */
    private final BlockEntityType<?> type;
    private final CompoundTag tag;

    public static ClientboundBlockEntityDataPacket create(BlockEntity blockEntity, BiFunction<BlockEntity, RegistryAccess, CompoundTag> dataGetter) {
        RegistryAccess registryaccess = blockEntity.getLevel().registryAccess();
        return new ClientboundBlockEntityDataPacket(blockEntity.getBlockPos(), blockEntity.getType(), dataGetter.apply(blockEntity, registryaccess));
    }

    public static ClientboundBlockEntityDataPacket create(BlockEntity blockEntity) {
        return create(blockEntity, BlockEntity::getUpdateTag);
    }

    private ClientboundBlockEntityDataPacket(BlockPos pos, BlockEntityType<?> type, CompoundTag tag) {
        this.pos = pos;
        this.type = type;
        this.tag = tag;
    }

    @Override
    public PacketType<ClientboundBlockEntityDataPacket> type() {
        return GamePacketTypes.CLIENTBOUND_BLOCK_ENTITY_DATA;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void handle(ClientGamePacketListener handler) {
        handler.handleBlockEntityData(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public BlockEntityType<?> getType() {
        return this.type;
    }

    public CompoundTag getTag() {
        return this.tag;
    }
}
