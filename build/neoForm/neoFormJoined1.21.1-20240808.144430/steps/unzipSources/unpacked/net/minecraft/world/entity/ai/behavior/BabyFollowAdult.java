package net.minecraft.world.entity.ai.behavior;

import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class BabyFollowAdult {
    public static OneShot<AgeableMob> create(UniformInt followRange, float speedModifier) {
        return create(followRange, p_147421_ -> speedModifier);
    }

    public static OneShot<AgeableMob> create(UniformInt followRange, Function<LivingEntity, Float> speedModifier) {
        return BehaviorBuilder.create(
            p_258331_ -> p_258331_.group(
                        p_258331_.present(MemoryModuleType.NEAREST_VISIBLE_ADULT),
                        p_258331_.registered(MemoryModuleType.LOOK_TARGET),
                        p_258331_.absent(MemoryModuleType.WALK_TARGET)
                    )
                    .apply(
                        p_258331_,
                        (p_258317_, p_258318_, p_258319_) -> (p_258326_, p_258327_, p_258328_) -> {
                                if (!p_258327_.isBaby()) {
                                    return false;
                                } else {
                                    AgeableMob ageablemob = p_258331_.get(p_258317_);
                                    if (p_258327_.closerThan(ageablemob, (double)(followRange.getMaxValue() + 1))
                                        && !p_258327_.closerThan(ageablemob, (double)followRange.getMinValue())) {
                                        WalkTarget walktarget = new WalkTarget(
                                            new EntityTracker(ageablemob, false), speedModifier.apply(p_258327_), followRange.getMinValue() - 1
                                        );
                                        p_258318_.set(new EntityTracker(ageablemob, true));
                                        p_258319_.set(walktarget);
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            }
                    )
        );
    }
}
