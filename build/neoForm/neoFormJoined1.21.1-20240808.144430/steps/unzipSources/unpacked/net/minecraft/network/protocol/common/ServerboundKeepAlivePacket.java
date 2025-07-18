package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundKeepAlivePacket implements Packet<ServerCommonPacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundKeepAlivePacket> STREAM_CODEC = Packet.codec(
        ServerboundKeepAlivePacket::write, ServerboundKeepAlivePacket::new
    );
    private final long id;

    public ServerboundKeepAlivePacket(long id) {
        this.id = id;
    }

    private ServerboundKeepAlivePacket(FriendlyByteBuf buffer) {
        this.id = buffer.readLong();
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeLong(this.id);
    }

    @Override
    public PacketType<ServerboundKeepAlivePacket> type() {
        return CommonPacketTypes.SERVERBOUND_KEEP_ALIVE;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    public void handle(ServerCommonPacketListener handler) {
        handler.handleKeepAlive(this);
    }

    public long getId() {
        return this.id;
    }
}
