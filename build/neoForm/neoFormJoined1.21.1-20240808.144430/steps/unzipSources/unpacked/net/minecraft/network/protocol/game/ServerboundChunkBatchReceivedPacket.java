package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundChunkBatchReceivedPacket(float desiredChunksPerTick) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundChunkBatchReceivedPacket> STREAM_CODEC = Packet.codec(
        ServerboundChunkBatchReceivedPacket::write, ServerboundChunkBatchReceivedPacket::new
    );

    private ServerboundChunkBatchReceivedPacket(FriendlyByteBuf p_294171_) {
        this(p_294171_.readFloat());
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.desiredChunksPerTick);
    }

    @Override
    public PacketType<ServerboundChunkBatchReceivedPacket> type() {
        return GamePacketTypes.SERVERBOUND_CHUNK_BATCH_RECEIVED;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    public void handle(ServerGamePacketListener handler) {
        handler.handleChunkBatchReceived(this);
    }
}
