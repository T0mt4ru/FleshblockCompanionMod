package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class PiglinBrute extends AbstractPiglin {
    private static final int MAX_HEALTH = 50;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.35F;
    private static final int ATTACK_DAMAGE = 7;
    protected static final ImmutableList<SensorType<? extends Sensor<? super PiglinBrute>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.HURT_BY, SensorType.PIGLIN_BRUTE_SPECIFIC_SENSOR
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.DOORS_TO_CLOSE,
        MemoryModuleType.NEAREST_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,
        MemoryModuleType.NEARBY_ADULT_PIGLINS,
        MemoryModuleType.HURT_BY,
        MemoryModuleType.HURT_BY_ENTITY,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.ATTACK_TARGET,
        MemoryModuleType.ATTACK_COOLING_DOWN,
        MemoryModuleType.INTERACTION_TARGET,
        MemoryModuleType.PATH,
        MemoryModuleType.ANGRY_AT,
        MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
        MemoryModuleType.HOME
    );

    public PiglinBrute(EntityType<? extends PiglinBrute> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.MOVEMENT_SPEED, 0.35F).add(Attributes.ATTACK_DAMAGE, 7.0);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        PiglinBruteAi.initMemories(this);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
    }

    @Override
    protected Brain.Provider<PiglinBrute> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PiglinBruteAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public Brain<PiglinBrute> getBrain() {
        return (Brain<PiglinBrute>)super.getBrain();
    }

    @Override
    public boolean canHunt() {
        return false;
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return stack.is(Items.GOLDEN_AXE) ? super.wantsToPickUp(stack) : false;
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("piglinBruteBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        this.level().getProfiler().pop();
        PiglinBruteAi.updateActivity(this);
        PiglinBruteAi.maybePlayActivitySound(this);
        super.customServerAiStep();
    }

    @Override
    public PiglinArmPose getArmPose() {
        return this.isAggressive() && this.isHoldingMeleeWeapon() ? PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON : PiglinArmPose.DEFAULT;
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean flag = super.hurt(source, amount);
        if (this.level().isClientSide) {
            return false;
        } else {
            if (flag && source.getEntity() instanceof LivingEntity) {
                PiglinBruteAi.wasHurtBy(this, (LivingEntity)source.getEntity());
            }

            return flag;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIGLIN_BRUTE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PIGLIN_BRUTE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_BRUTE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.PIGLIN_BRUTE_STEP, 0.15F, 1.0F);
    }

    protected void playAngrySound() {
        this.makeSound(SoundEvents.PIGLIN_BRUTE_ANGRY);
    }

    @Override
    protected void playConvertedSound() {
        this.makeSound(SoundEvents.PIGLIN_BRUTE_CONVERTED_TO_ZOMBIFIED);
    }
}
