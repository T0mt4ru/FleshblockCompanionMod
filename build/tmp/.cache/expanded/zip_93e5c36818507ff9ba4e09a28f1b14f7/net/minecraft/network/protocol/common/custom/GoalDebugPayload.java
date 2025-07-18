package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record GoalDebugPayload(int entityId, BlockPos pos, List<GoalDebugPayload.DebugGoal> goals) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, GoalDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(GoalDebugPayload::write, GoalDebugPayload::new);
    public static final CustomPacketPayload.Type<GoalDebugPayload> TYPE = CustomPacketPayload.createType("debug/goal_selector");

    private GoalDebugPayload(FriendlyByteBuf p_295257_) {
        this(p_295257_.readInt(), p_295257_.readBlockPos(), p_295257_.readList(GoalDebugPayload.DebugGoal::new));
    }

    private void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeBlockPos(this.pos);
        buffer.writeCollection(this.goals, (p_294168_, p_295766_) -> p_295766_.write(p_294168_));
    }

    @Override
    public CustomPacketPayload.Type<GoalDebugPayload> type() {
        return TYPE;
    }

    public static record DebugGoal(int priority, boolean isRunning, String name) {
        public DebugGoal(FriendlyByteBuf p_294803_) {
            this(p_294803_.readInt(), p_294803_.readBoolean(), p_294803_.readUtf(255));
        }

        public void write(FriendlyByteBuf buffer) {
            buffer.writeInt(this.priority);
            buffer.writeBoolean(this.isRunning);
            buffer.writeUtf(this.name);
        }
    }
}
