package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderWarningDelayPacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetBorderWarningDelayPacket> STREAM_CODEC = Packet.codec(
        ClientboundSetBorderWarningDelayPacket::write, ClientboundSetBorderWarningDelayPacket::new
    );
    private final int warningDelay;

    public ClientboundSetBorderWarningDelayPacket(WorldBorder worldBorder) {
        this.warningDelay = worldBorder.getWarningTime();
    }

    private ClientboundSetBorderWarningDelayPacket(FriendlyByteBuf buffer) {
        this.warningDelay = buffer.readVarInt();
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.warningDelay);
    }

    @Override
    public PacketType<ClientboundSetBorderWarningDelayPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_BORDER_WARNING_DELAY;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    public void handle(ClientGamePacketListener handler) {
        handler.handleSetBorderWarningDelay(this);
    }

    public int getWarningDelay() {
        return this.warningDelay;
    }
}
