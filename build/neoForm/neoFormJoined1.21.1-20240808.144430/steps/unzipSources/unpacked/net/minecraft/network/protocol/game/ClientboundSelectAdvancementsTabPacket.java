package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public class ClientboundSelectAdvancementsTabPacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSelectAdvancementsTabPacket> STREAM_CODEC = Packet.codec(
        ClientboundSelectAdvancementsTabPacket::write, ClientboundSelectAdvancementsTabPacket::new
    );
    @Nullable
    private final ResourceLocation tab;

    public ClientboundSelectAdvancementsTabPacket(@Nullable ResourceLocation tab) {
        this.tab = tab;
    }

    private ClientboundSelectAdvancementsTabPacket(FriendlyByteBuf buffer) {
        this.tab = buffer.readNullable(FriendlyByteBuf::readResourceLocation);
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    private void write(FriendlyByteBuf buffer) {
        buffer.writeNullable(this.tab, FriendlyByteBuf::writeResourceLocation);
    }

    @Override
    public PacketType<ClientboundSelectAdvancementsTabPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SELECT_ADVANCEMENTS_TAB;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void handle(ClientGamePacketListener handler) {
        handler.handleSelectAdvancementsTab(this);
    }

    @Nullable
    public ResourceLocation getTab() {
        return this.tab;
    }
}
