package net.minecraft.world.entity.monster.hoglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.BecomePassiveIfMemoryPresent;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.schedule.Activity;

public class HoglinAi {
    public static final int REPELLENT_DETECTION_RANGE_HORIZONTAL = 8;
    public static final int REPELLENT_DETECTION_RANGE_VERTICAL = 4;
    private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    private static final int ATTACK_DURATION = 200;
    private static final int DESIRED_DISTANCE_FROM_PIGLIN_WHEN_IDLING = 8;
    private static final int DESIRED_DISTANCE_FROM_PIGLIN_WHEN_RETREATING = 15;
    private static final int ATTACK_INTERVAL = 40;
    private static final int BABY_ATTACK_INTERVAL = 15;
    private static final int REPELLENT_PACIFY_TIME = 200;
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final float SPEED_MULTIPLIER_WHEN_AVOIDING_REPELLENT = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_RETREATING = 1.3F;
    private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 0.6F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.4F;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 0.6F;

    protected static Brain<?> makeBrain(Brain<Hoglin> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(brain);
        initRetreatActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Hoglin> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink()));
    }

    private static void initIdleActivity(Brain<Hoglin> brain) {
        brain.addActivity(
            Activity.IDLE,
            10,
            ImmutableList.of(
                BecomePassiveIfMemoryPresent.create(MemoryModuleType.NEAREST_REPELLENT, 200),
                new AnimalMakeLove(EntityType.HOGLIN, 0.6F, 2),
                SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, true),
                StartAttacking.create(HoglinAi::findNearestValidAttackTarget),
                BehaviorBuilder.triggerIf(Hoglin::isAdult, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, 0.4F, 8, false)),
                SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)),
                BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 0.6F),
                createIdleMovementBehaviors()
            )
        );
    }

    private static void initFightActivity(Brain<Hoglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(
            Activity.FIGHT,
            10,
            ImmutableList.of(
                BecomePassiveIfMemoryPresent.create(MemoryModuleType.NEAREST_REPELLENT, 200),
                new AnimalMakeLove(EntityType.HOGLIN, 0.6F, 2),
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                BehaviorBuilder.triggerIf(Hoglin::isAdult, MeleeAttack.create(40)),
                BehaviorBuilder.triggerIf(AgeableMob::isBaby, MeleeAttack.create(15)),
                StopAttackingIfTargetInvalid.create(),
                EraseMemoryIf.create(HoglinAi::isBreeding, MemoryModuleType.ATTACK_TARGET)
            ),
            MemoryModuleType.ATTACK_TARGET
        );
    }

    private static void initRetreatActivity(Brain<Hoglin> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(
            Activity.AVOID,
            10,
            ImmutableList.of(
                SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.3F, 15, false),
                createIdleMovementBehaviors(),
                SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)),
                EraseMemoryIf.create(HoglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)
            ),
            MemoryModuleType.AVOID_TARGET
        );
    }

    private static RunOne<Hoglin> createIdleMovementBehaviors() {
        return new RunOne<>(
            ImmutableList.of(Pair.of(RandomStroll.stroll(0.4F), 2), Pair.of(SetWalkTargetFromLookTarget.create(0.4F, 3), 2), Pair.of(new DoNothing(30, 60), 1))
        );
    }

    protected static void updateActivity(Hoglin hoglin) {
        Brain<Hoglin> brain = hoglin.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
        Activity activity1 = brain.getActiveNonCoreActivity().orElse(null);
        if (activity != activity1) {
            getSoundForCurrentActivity(hoglin).ifPresent(hoglin::makeSound);
        }

        hoglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }

    protected static void onHitTarget(Hoglin hoglin, LivingEntity target) {
        if (!hoglin.isBaby()) {
            if (target.getType() == EntityType.PIGLIN && piglinsOutnumberHoglins(hoglin)) {
                setAvoidTarget(hoglin, target);
                broadcastRetreat(hoglin, target);
            } else {
                broadcastAttackTarget(hoglin, target);
            }
        }
    }

    private static void broadcastRetreat(Hoglin hoglin, LivingEntity target) {
        getVisibleAdultHoglins(hoglin).forEach(p_34590_ -> retreatFromNearestTarget(p_34590_, target));
    }

    private static void retreatFromNearestTarget(Hoglin hoglin, LivingEntity target) {
        Brain<Hoglin> brain = hoglin.getBrain();
        LivingEntity $$2 = BehaviorUtils.getNearestTarget(hoglin, brain.getMemory(MemoryModuleType.AVOID_TARGET), target);
        $$2 = BehaviorUtils.getNearestTarget(hoglin, brain.getMemory(MemoryModuleType.ATTACK_TARGET), $$2);
        setAvoidTarget(hoglin, $$2);
    }

    private static void setAvoidTarget(Hoglin hoglin, LivingEntity target) {
        hoglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        hoglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        hoglin.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, target, (long)RETREAT_DURATION.sample(hoglin.level().random));
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Hoglin hoglin) {
        return !isPacified(hoglin) && !isBreeding(hoglin)
            ? hoglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
            : Optional.empty();
    }

    static boolean isPosNearNearestRepellent(Hoglin hoglin, BlockPos pos) {
        Optional<BlockPos> optional = hoglin.getBrain().getMemory(MemoryModuleType.NEAREST_REPELLENT);
        return optional.isPresent() && optional.get().closerThan(pos, 8.0);
    }

    private static boolean wantsToStopFleeing(Hoglin hoglin) {
        return hoglin.isAdult() && !piglinsOutnumberHoglins(hoglin);
    }

    private static boolean piglinsOutnumberHoglins(Hoglin hoglin) {
        if (hoglin.isBaby()) {
            return false;
        } else {
            int i = hoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0);
            int j = hoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0) + 1;
            return i > j;
        }
    }

    protected static void wasHurtBy(Hoglin hoglin, LivingEntity livingEntity) {
        Brain<Hoglin> brain = hoglin.getBrain();
        brain.eraseMemory(MemoryModuleType.PACIFIED);
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        if (hoglin.isBaby()) {
            retreatFromNearestTarget(hoglin, livingEntity);
        } else {
            maybeRetaliate(hoglin, livingEntity);
        }
    }

    private static void maybeRetaliate(Hoglin hoglin, LivingEntity livingEntity) {
        if (!hoglin.getBrain().isActive(Activity.AVOID) || livingEntity.getType() != EntityType.PIGLIN) {
            if (livingEntity.getType() != EntityType.HOGLIN) {
                if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(hoglin, livingEntity, 4.0)) {
                    if (Sensor.isEntityAttackable(hoglin, livingEntity)) {
                        setAttackTarget(hoglin, livingEntity);
                        broadcastAttackTarget(hoglin, livingEntity);
                    }
                }
            }
        }
    }

    private static void setAttackTarget(Hoglin hoglin, LivingEntity target) {
        Brain<Hoglin> brain = hoglin.getBrain();
        brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, target, 200L);
    }

    private static void broadcastAttackTarget(Hoglin hoglin, LivingEntity target) {
        getVisibleAdultHoglins(hoglin).forEach(p_34574_ -> setAttackTargetIfCloserThanCurrent(p_34574_, target));
    }

    private static void setAttackTargetIfCloserThanCurrent(Hoglin hoglin, LivingEntity target) {
        if (!isPacified(hoglin)) {
            Optional<LivingEntity> optional = hoglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
            LivingEntity livingentity = BehaviorUtils.getNearestTarget(hoglin, optional, target);
            setAttackTarget(hoglin, livingentity);
        }
    }

    public static Optional<SoundEvent> getSoundForCurrentActivity(Hoglin hoglin) {
        return hoglin.getBrain().getActiveNonCoreActivity().map(p_34600_ -> getSoundForActivity(hoglin, p_34600_));
    }

    private static SoundEvent getSoundForActivity(Hoglin hoglin, Activity activity) {
        if (activity == Activity.AVOID || hoglin.isConverting()) {
            return SoundEvents.HOGLIN_RETREAT;
        } else if (activity == Activity.FIGHT) {
            return SoundEvents.HOGLIN_ANGRY;
        } else {
            return isNearRepellent(hoglin) ? SoundEvents.HOGLIN_RETREAT : SoundEvents.HOGLIN_AMBIENT;
        }
    }

    private static List<Hoglin> getVisibleAdultHoglins(Hoglin hoglin) {
        return hoglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS).orElse(ImmutableList.of());
    }

    private static boolean isNearRepellent(Hoglin hoglin) {
        return hoglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
    }

    private static boolean isBreeding(Hoglin hoglin) {
        return hoglin.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
    }

    protected static boolean isPacified(Hoglin hoglin) {
        return hoglin.getBrain().hasMemoryValue(MemoryModuleType.PACIFIED);
    }
}
