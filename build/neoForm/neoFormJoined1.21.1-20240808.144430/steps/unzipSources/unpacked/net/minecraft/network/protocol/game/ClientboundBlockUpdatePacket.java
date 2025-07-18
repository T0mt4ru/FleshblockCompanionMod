package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ClientboundBlockUpdatePacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBlockUpdatePacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ClientboundBlockUpdatePacket::getPos,
        ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY),
        ClientboundBlockUpdatePacket::getBlockState,
        ClientboundBlockUpdatePacket::new
    );
    private final BlockPos pos;
    private final BlockState blockState;

    public ClientboundBlockUpdatePacket(BlockPos pos, BlockState blockState) {
        this.pos = pos;
        this.blockState = blockState;
    }

    public ClientboundBlockUpdatePacket(BlockGetter blockGetter, BlockPos pos) {
        this(pos, blockGetter.getBlockState(pos));
    }

    @Override
    public PacketType<ClientboundBlockUpdatePacket> type() {
        return GamePacketTypes.CLIENTBOUND_BLOCK_UPDATE;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void handle(ClientGamePacketListener handler) {
        handler.handleBlockUpdate(this);
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public BlockPos getPos() {
        return this.pos;
    }
}
