package net.minecraft.world.entity.animal.sniffer;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public class SnifferAi {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_LOOK_DISTANCE = 6;
    static final List<SensorType<? extends Sensor<? super Sniffer>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.NEAREST_PLAYERS, SensorType.SNIFFER_TEMPTATIONS
    );
    static final List<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.PATH,
        MemoryModuleType.IS_PANICKING,
        MemoryModuleType.SNIFFER_SNIFFING_TARGET,
        MemoryModuleType.SNIFFER_DIGGING,
        MemoryModuleType.SNIFFER_HAPPY,
        MemoryModuleType.SNIFF_COOLDOWN,
        MemoryModuleType.SNIFFER_EXPLORED_POSITIONS,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.BREED_TARGET,
        MemoryModuleType.TEMPTING_PLAYER,
        MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
        MemoryModuleType.IS_TEMPTED
    );
    private static final int SNIFFING_COOLDOWN_TICKS = 9600;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0F;
    private static final float SPEED_MULTIPLIER_WHEN_SNIFFING = 1.25F;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25F;

    public static Predicate<ItemStack> getTemptations() {
        return p_335270_ -> p_335270_.is(ItemTags.SNIFFER_FOOD);
    }

    protected static Brain<?> makeBrain(Brain<Sniffer> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initSniffingActivity(brain);
        initDigActivity(brain);
        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    static Sniffer resetSniffing(Sniffer sniffer) {
        sniffer.getBrain().eraseMemory(MemoryModuleType.SNIFFER_DIGGING);
        sniffer.getBrain().eraseMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
        return sniffer.transitionTo(Sniffer.State.IDLING);
    }

    private static void initCoreActivity(Brain<Sniffer> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim(0.8F), new AnimalPanic<Sniffer>(2.0F) {
            protected void start(ServerLevel p_326360_, Sniffer p_326298_, long p_326169_) {
                SnifferAi.resetSniffing(p_326298_);
                super.start(p_326360_, p_326298_, p_326169_);
            }
        }, new MoveToTargetSink(500, 700), new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));
    }

    private static void initSniffingActivity(Brain<Sniffer> brain) {
        brain.addActivityWithConditions(
            Activity.SNIFF,
            ImmutableList.of(Pair.of(0, new SnifferAi.Searching())),
            Set.of(
                Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.SNIFFER_SNIFFING_TARGET, MemoryStatus.VALUE_PRESENT),
                Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT)
            )
        );
    }

    private static void initDigActivity(Brain<Sniffer> brain) {
        brain.addActivityWithConditions(
            Activity.DIG,
            ImmutableList.of(Pair.of(0, new SnifferAi.Digging(160, 180)), Pair.of(0, new SnifferAi.FinishedDigging(40))),
            Set.of(
                Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                Pair.of(MemoryModuleType.SNIFFER_DIGGING, MemoryStatus.VALUE_PRESENT)
            )
        );
    }

    private static void initIdleActivity(Brain<Sniffer> brain) {
        brain.addActivityWithConditions(
            Activity.IDLE,
            ImmutableList.of(
                Pair.of(0, new AnimalMakeLove(EntityType.SNIFFER) {
                    @Override
                    protected void start(ServerLevel p_279149_, Animal p_279090_, long p_279482_) {
                        SnifferAi.resetSniffing((Sniffer)p_279090_);
                        super.start(p_279149_, p_279090_, p_279482_);
                    }
                }),
                Pair.of(1, new FollowTemptation(p_279492_ -> 1.25F, p_288909_ -> p_288909_.isBaby() ? 2.5 : 3.5) {
                    @Override
                    protected void start(ServerLevel p_279230_, PathfinderMob p_279386_, long p_279139_) {
                        SnifferAi.resetSniffing((Sniffer)p_279386_);
                        super.start(p_279230_, p_279386_, p_279139_);
                    }
                }),
                Pair.of(2, new LookAtTargetSink(45, 90)),
                Pair.of(3, new SnifferAi.FeelingHappy(40, 100)),
                Pair.of(
                    4,
                    new RunOne<>(
                        ImmutableList.of(
                            Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 2),
                            Pair.of(new SnifferAi.Scenting(40, 80), 1),
                            Pair.of(new SnifferAi.Sniffing(40, 80), 1),
                            Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 6.0F), 1),
                            Pair.of(RandomStroll.stroll(1.0F), 1),
                            Pair.of(new DoNothing(5, 20), 2)
                        )
                    )
                )
            ),
            Set.of(Pair.of(MemoryModuleType.SNIFFER_DIGGING, MemoryStatus.VALUE_ABSENT))
        );
    }

    static void updateActivity(Sniffer sniffer) {
        sniffer.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.DIG, Activity.SNIFF, Activity.IDLE));
    }

    static class Digging extends Behavior<Sniffer> {
        Digging(int minDuration, int maxDuration) {
            super(
                Map.of(
                    MemoryModuleType.IS_PANICKING,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.WALK_TARGET,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFFER_DIGGING,
                    MemoryStatus.VALUE_PRESENT,
                    MemoryModuleType.SNIFF_COOLDOWN,
                    MemoryStatus.VALUE_ABSENT
                ),
                minDuration,
                maxDuration
            );
        }

        protected boolean checkExtraStartConditions(ServerLevel level, Sniffer owner) {
            return owner.canSniff();
        }

        protected boolean canStillUse(ServerLevel level, Sniffer entity, long gameTime) {
            return entity.getBrain().getMemory(MemoryModuleType.SNIFFER_DIGGING).isPresent() && entity.canDig() && !entity.isInLove();
        }

        protected void start(ServerLevel level, Sniffer entity, long gameTime) {
            entity.transitionTo(Sniffer.State.DIGGING);
        }

        protected void stop(ServerLevel level, Sniffer entity, long gameTime) {
            boolean flag = this.timedOut(gameTime);
            if (flag) {
                entity.getBrain().setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 9600L);
            } else {
                SnifferAi.resetSniffing(entity);
            }
        }
    }

    static class FeelingHappy extends Behavior<Sniffer> {
        FeelingHappy(int minDuration, int maxDuration) {
            super(Map.of(MemoryModuleType.SNIFFER_HAPPY, MemoryStatus.VALUE_PRESENT), minDuration, maxDuration);
        }

        protected boolean canStillUse(ServerLevel level, Sniffer entity, long gameTime) {
            return true;
        }

        protected void start(ServerLevel level, Sniffer entity, long gameTime) {
            entity.transitionTo(Sniffer.State.FEELING_HAPPY);
        }

        protected void stop(ServerLevel level, Sniffer entity, long gameTime) {
            entity.transitionTo(Sniffer.State.IDLING);
            entity.getBrain().eraseMemory(MemoryModuleType.SNIFFER_HAPPY);
        }
    }

    static class FinishedDigging extends Behavior<Sniffer> {
        FinishedDigging(int duration) {
            super(
                Map.of(
                    MemoryModuleType.IS_PANICKING,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.WALK_TARGET,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFFER_DIGGING,
                    MemoryStatus.VALUE_PRESENT,
                    MemoryModuleType.SNIFF_COOLDOWN,
                    MemoryStatus.VALUE_PRESENT
                ),
                duration,
                duration
            );
        }

        protected boolean checkExtraStartConditions(ServerLevel level, Sniffer owner) {
            return true;
        }

        protected boolean canStillUse(ServerLevel level, Sniffer entity, long gameTime) {
            return entity.getBrain().getMemory(MemoryModuleType.SNIFFER_DIGGING).isPresent();
        }

        protected void start(ServerLevel level, Sniffer entity, long gameTime) {
            entity.transitionTo(Sniffer.State.RISING);
        }

        protected void stop(ServerLevel level, Sniffer entity, long gameTime) {
            boolean flag = this.timedOut(gameTime);
            entity.transitionTo(Sniffer.State.IDLING).onDiggingComplete(flag);
            entity.getBrain().eraseMemory(MemoryModuleType.SNIFFER_DIGGING);
            entity.getBrain().setMemory(MemoryModuleType.SNIFFER_HAPPY, true);
        }
    }

    static class Scenting extends Behavior<Sniffer> {
        Scenting(int minDuration, int maxDuration) {
            super(
                Map.of(
                    MemoryModuleType.IS_PANICKING,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFFER_DIGGING,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFFER_SNIFFING_TARGET,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFFER_HAPPY,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.BREED_TARGET,
                    MemoryStatus.VALUE_ABSENT
                ),
                minDuration,
                maxDuration
            );
        }

        protected boolean checkExtraStartConditions(ServerLevel level, Sniffer owner) {
            return !owner.isTempted();
        }

        protected boolean canStillUse(ServerLevel level, Sniffer entity, long gameTime) {
            return true;
        }

        protected void start(ServerLevel level, Sniffer entity, long gameTime) {
            entity.transitionTo(Sniffer.State.SCENTING);
        }

        protected void stop(ServerLevel level, Sniffer entity, long gameTime) {
            entity.transitionTo(Sniffer.State.IDLING);
        }
    }

    static class Searching extends Behavior<Sniffer> {
        Searching() {
            super(
                Map.of(
                    MemoryModuleType.WALK_TARGET,
                    MemoryStatus.VALUE_PRESENT,
                    MemoryModuleType.IS_PANICKING,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFFER_SNIFFING_TARGET,
                    MemoryStatus.VALUE_PRESENT
                ),
                600
            );
        }

        protected boolean checkExtraStartConditions(ServerLevel p_273493_, Sniffer p_272857_) {
            return p_272857_.canSniff();
        }

        protected boolean canStillUse(ServerLevel p_273196_, Sniffer p_273769_, long p_273602_) {
            if (!p_273769_.canSniff()) {
                p_273769_.transitionTo(Sniffer.State.IDLING);
                return false;
            } else {
                Optional<BlockPos> optional = p_273769_.getBrain()
                    .getMemory(MemoryModuleType.WALK_TARGET)
                    .map(WalkTarget::getTarget)
                    .map(PositionTracker::currentBlockPosition);
                Optional<BlockPos> optional1 = p_273769_.getBrain().getMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
                return !optional.isEmpty() && !optional1.isEmpty() ? optional1.get().equals(optional.get()) : false;
            }
        }

        protected void start(ServerLevel p_273563_, Sniffer p_273394_, long p_273358_) {
            p_273394_.transitionTo(Sniffer.State.SEARCHING);
        }

        protected void stop(ServerLevel p_273705_, Sniffer p_273135_, long p_272667_) {
            if (p_273135_.canDig() && p_273135_.canSniff()) {
                p_273135_.getBrain().setMemory(MemoryModuleType.SNIFFER_DIGGING, true);
            }

            p_273135_.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            p_273135_.getBrain().eraseMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
        }
    }

    static class Sniffing extends Behavior<Sniffer> {
        Sniffing(int minDuration, int maxDuration) {
            super(
                Map.of(
                    MemoryModuleType.WALK_TARGET,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFFER_SNIFFING_TARGET,
                    MemoryStatus.VALUE_ABSENT,
                    MemoryModuleType.SNIFF_COOLDOWN,
                    MemoryStatus.VALUE_ABSENT
                ),
                minDuration,
                maxDuration
            );
        }

        protected boolean checkExtraStartConditions(ServerLevel level, Sniffer owner) {
            return !owner.isBaby() && owner.canSniff();
        }

        protected boolean canStillUse(ServerLevel level, Sniffer entity, long gameTime) {
            return entity.canSniff();
        }

        protected void start(ServerLevel level, Sniffer entity, long gameTime) {
            entity.transitionTo(Sniffer.State.SNIFFING);
        }

        protected void stop(ServerLevel level, Sniffer entity, long gameTime) {
            boolean flag = this.timedOut(gameTime);
            entity.transitionTo(Sniffer.State.IDLING);
            if (flag) {
                entity.calculateDigPosition().ifPresent(p_273341_ -> {
                    entity.getBrain().setMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET, p_273341_);
                    entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(p_273341_, 1.25F, 0));
                });
            }
        }
    }
}
