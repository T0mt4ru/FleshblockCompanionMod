package net.minecraft.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ValidateNearbyPoi {
    private static final int MAX_DISTANCE = 16;

    public static BehaviorControl<LivingEntity> create(Predicate<Holder<PoiType>> poiValidator, MemoryModuleType<GlobalPos> poiPosMemory) {
        return BehaviorBuilder.create(
            p_259215_ -> p_259215_.group(p_259215_.present(poiPosMemory)).apply(p_259215_, p_259498_ -> (p_259843_, p_259259_, p_260036_) -> {
                        GlobalPos globalpos = p_259215_.get(p_259498_);
                        BlockPos blockpos = globalpos.pos();
                        if (p_259843_.dimension() == globalpos.dimension() && blockpos.closerToCenterThan(p_259259_.position(), 16.0)) {
                            ServerLevel serverlevel = p_259843_.getServer().getLevel(globalpos.dimension());
                            if (serverlevel == null || !serverlevel.getPoiManager().exists(blockpos, poiValidator)) {
                                p_259498_.erase();
                            } else if (bedIsOccupied(serverlevel, blockpos, p_259259_)) {
                                p_259498_.erase();
                                p_259843_.getPoiManager().release(blockpos);
                                DebugPackets.sendPoiTicketCountPacket(p_259843_, blockpos);
                            }

                            return true;
                        } else {
                            return false;
                        }
                    })
        );
    }

    private static boolean bedIsOccupied(ServerLevel level, BlockPos pos, LivingEntity entity) {
        BlockState blockstate = level.getBlockState(pos);
        return blockstate.is(BlockTags.BEDS) && blockstate.getValue(BedBlock.OCCUPIED) && !entity.isSleeping();
    }
}
