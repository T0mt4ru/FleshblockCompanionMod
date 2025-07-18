package net.minecraft.network.protocol.game;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundPlayerInfoRemovePacket(List<UUID> profileIds) implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundPlayerInfoRemovePacket> STREAM_CODEC = Packet.codec(
        ClientboundPlayerInfoRemovePacket::write, ClientboundPlayerInfoRemovePacket::new
    );

    private ClientboundPlayerInfoRemovePacket(FriendlyByteBuf p_248744_) {
        this(p_248744_.readList(UUIDUtil.STREAM_CODEC));
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeCollection(this.profileIds, UUIDUtil.STREAM_CODEC);
    }

    @Override
    public PacketType<ClientboundPlayerInfoRemovePacket> type() {
        return GamePacketTypes.CLIENTBOUND_PLAYER_INFO_REMOVE;
    }

    /**
     * Passes this Packet on to the PacketListener for processing.
     */
    public void handle(ClientGamePacketListener handler) {
        handler.handlePlayerInfoRemove(this);
    }
}
