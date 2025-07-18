package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundContainerButtonClickPacket(int containerId, int buttonId) implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundContainerButtonClickPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        ServerboundContainerButtonClickPacket::containerId,
        ByteBufCodecs.VAR_INT,
        ServerboundContainerButtonClickPacket::buttonId,
        ServerboundContainerButtonClickPacket::new
    );

    @Override
    public PacketType<ServerboundContainerButtonClickPacket> type() {
        return GamePacketTypes.SERVERBOUND_CONTAINER_BUTTON_CLICK;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void handle(ServerGamePacketListener handler) {
        handler.handleContainerButtonClick(this);
    }
}
