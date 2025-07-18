package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundChunkBatchFinishedPacket(int batchSize) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundChunkBatchFinishedPacket> STREAM_CODEC = Packet.codec(
        ClientboundChunkBatchFinishedPacket::write, ClientboundChunkBatchFinishedPacket::new
    );

    private ClientboundChunkBatchFinishedPacket(FriendlyByteBuf p_296389_) {
        this(p_296389_.readVarInt());
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.batchSize);
    }

    @Override
    public PacketType<ClientboundChunkBatchFinishedPacket> type() {
        return GamePacketTypes.CLIENTBOUND_CHUNK_BATCH_FINISHED;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    public void handle(ClientGamePacketListener handler) {
        handler.handleChunkBatchFinished(this);
    }
}
