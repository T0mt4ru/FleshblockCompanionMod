package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record HiveDebugPayload(HiveDebugPayload.HiveInfo hiveInfo) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, HiveDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(HiveDebugPayload::write, HiveDebugPayload::new);
    public static final CustomPacketPayload.Type<HiveDebugPayload> TYPE = CustomPacketPayload.createType("debug/hive");

    private HiveDebugPayload(FriendlyByteBuf p_296486_) {
        this(new HiveDebugPayload.HiveInfo(p_296486_));
    }

    private void write(FriendlyByteBuf buffer) {
        this.hiveInfo.write(buffer);
    }

    @Override
    public CustomPacketPayload.Type<HiveDebugPayload> type() {
        return TYPE;
    }

    public static record HiveInfo(BlockPos pos, String hiveType, int occupantCount, int honeyLevel, boolean sedated) {
        public HiveInfo(FriendlyByteBuf p_295182_) {
            this(p_295182_.readBlockPos(), p_295182_.readUtf(), p_295182_.readInt(), p_295182_.readInt(), p_295182_.readBoolean());
        }

        public void write(FriendlyByteBuf buffer) {
            buffer.writeBlockPos(this.pos);
            buffer.writeUtf(this.hiveType);
            buffer.writeInt(this.occupantCount);
            buffer.writeInt(this.honeyLevel);
            buffer.writeBoolean(this.sedated);
        }
    }
}
