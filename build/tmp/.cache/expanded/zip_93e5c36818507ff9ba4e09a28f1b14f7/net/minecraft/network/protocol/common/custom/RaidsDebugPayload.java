package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record RaidsDebugPayload(List<BlockPos> raidCenters) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, RaidsDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
        RaidsDebugPayload::write, RaidsDebugPayload::new
    );
    public static final CustomPacketPayload.Type<RaidsDebugPayload> TYPE = CustomPacketPayload.createType("debug/raids");

    private RaidsDebugPayload(FriendlyByteBuf p_295468_) {
        this(p_295468_.readList(BlockPos.STREAM_CODEC));
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeCollection(this.raidCenters, BlockPos.STREAM_CODEC);
    }

    @Override
    public CustomPacketPayload.Type<RaidsDebugPayload> type() {
        return TYPE;
    }
}
