package net.minecraft.network.protocol.ping;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundPingRequestPacket implements Packet<ServerPingPacketListener> {
    public static final StreamCodec<ByteBuf, ServerboundPingRequestPacket> STREAM_CODEC = Packet.codec(
        ServerboundPingRequestPacket::write, ServerboundPingRequestPacket::new
    );
    private final long time;

    public ServerboundPingRequestPacket(long time) {
        this.time = time;
    }

    private ServerboundPingRequestPacket(ByteBuf buffer) {
        this.time = buffer.readLong();
    }

    private void write(ByteBuf buffer) {
        buffer.writeLong(this.time);
    }

    @Override
    public PacketType<ServerboundPingRequestPacket> type() {
        return PingPacketTypes.SERVERBOUND_PING_REQUEST;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    public void handle(ServerPingPacketListener handler) {
        handler.handlePingRequest(this);
    }

    public long getTime() {
        return this.time;
    }
}
