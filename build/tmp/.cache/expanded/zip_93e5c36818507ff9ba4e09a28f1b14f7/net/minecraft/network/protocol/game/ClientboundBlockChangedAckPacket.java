package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundBlockChangedAckPacket(int sequence) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundBlockChangedAckPacket> STREAM_CODEC = Packet.codec(
        ClientboundBlockChangedAckPacket::write, ClientboundBlockChangedAckPacket::new
    );

    private ClientboundBlockChangedAckPacket(FriendlyByteBuf p_237582_) {
        this(p_237582_.readVarInt());
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.sequence);
    }

    @Override
    public PacketType<ClientboundBlockChangedAckPacket> type() {
        return GamePacketTypes.CLIENTBOUND_BLOCK_CHANGED_ACK;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    public void handle(ClientGamePacketListener handler) {
        handler.handleBlockChangedAck(this);
    }
}
