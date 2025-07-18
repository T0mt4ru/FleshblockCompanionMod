package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundSetSimulationDistancePacket(int simulationDistance) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetSimulationDistancePacket> STREAM_CODEC = Packet.codec(
        ClientboundSetSimulationDistancePacket::write, ClientboundSetSimulationDistancePacket::new
    );

    private ClientboundSetSimulationDistancePacket(FriendlyByteBuf p_195800_) {
        this(p_195800_.readVarInt());
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.simulationDistance);
    }

    @Override
    public PacketType<ClientboundSetSimulationDistancePacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_SIMULATION_DISTANCE;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    public void handle(ClientGamePacketListener handler) {
        handler.handleSetSimulationDistance(this);
    }
}
