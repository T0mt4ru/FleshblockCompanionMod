package net.minecraft.network.protocol.status;

import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.ping.ClientPongPacketListener;

/**
 * PacketListener for the client side of the STATUS protocol.
 */
public interface ClientStatusPacketListener extends ClientPongPacketListener, ClientboundPacketListener {
    @Override
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.STATUS;
    }

    void handleStatusResponse(ClientboundStatusResponsePacket packet);
}
