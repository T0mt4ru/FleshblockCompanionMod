package net.minecraft.world.entity.animal;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Ocelot extends Animal {
    public static final double CROUCH_SPEED_MOD = 0.6;
    public static final double WALK_SPEED_MOD = 0.8;
    public static final double SPRINT_SPEED_MOD = 1.33;
    private static final EntityDataAccessor<Boolean> DATA_TRUSTING = SynchedEntityData.defineId(Ocelot.class, EntityDataSerializers.BOOLEAN);
    @Nullable
    private Ocelot.OcelotAvoidEntityGoal<Player> ocelotAvoidPlayersGoal;
    @Nullable
    private Ocelot.OcelotTemptGoal temptGoal;

    public Ocelot(EntityType<? extends Ocelot> entityType, Level level) {
        super(entityType, level);
        this.reassessTrustingGoals();
    }

    boolean isTrusting() {
        return this.entityData.get(DATA_TRUSTING);
    }

    private void setTrusting(boolean trusting) {
        this.entityData.set(DATA_TRUSTING, trusting);
        this.reassessTrustingGoals();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Trusting", this.isTrusting());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setTrusting(compound.getBoolean("Trusting"));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TRUSTING, false);
    }

    @Override
    protected void registerGoals() {
        this.temptGoal = new Ocelot.OcelotTemptGoal(this, 0.6, p_335596_ -> p_335596_.is(ItemTags.OCELOT_FOOD), true);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, this.temptGoal);
        this.goalSelector.addGoal(7, new LeapAtTargetGoal(this, 0.3F));
        this.goalSelector.addGoal(8, new OcelotAttackGoal(this));
        this.goalSelector.addGoal(9, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 0.8, 1.0000001E-5F));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Chicken.class, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, false, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    @Override
    public void customServerAiStep() {
        if (this.getMoveControl().hasWanted()) {
            double d0 = this.getMoveControl().getSpeedModifier();
            if (d0 == 0.6) {
                this.setPose(Pose.CROUCHING);
                this.setSprinting(false);
            } else if (d0 == 1.33) {
                this.setPose(Pose.STANDING);
                this.setSprinting(true);
            } else {
                this.setPose(Pose.STANDING);
                this.setSprinting(false);
            }
        } else {
            this.setPose(Pose.STANDING);
            this.setSprinting(false);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isTrusting() && this.tickCount > 2400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.OCELOT_AMBIENT;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 900;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.OCELOT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.OCELOT_DEATH;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if ((this.temptGoal == null || this.temptGoal.isRunning()) && !this.isTrusting() && this.isFood(itemstack) && player.distanceToSqr(this) < 9.0) {
            this.usePlayerItem(player, hand, itemstack);
            if (!this.level().isClientSide) {
                if (this.random.nextInt(3) == 0 && !net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player)) {
                    this.setTrusting(true);
                    this.spawnTrustingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte)41);
                } else {
                    this.spawnTrustingParticles(false);
                    this.level().broadcastEntityEvent(this, (byte)40);
                }
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            return super.mobInteract(player, hand);
        }
    }

    /**
     * Handler for {@link World#setEntityState}
     */
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 41) {
            this.spawnTrustingParticles(true);
        } else if (id == 40) {
            this.spawnTrustingParticles(false);
        } else {
            super.handleEntityEvent(id);
        }
    }

    private void spawnTrustingParticles(boolean isTrusted) {
        ParticleOptions particleoptions = ParticleTypes.HEART;
        if (!isTrusted) {
            particleoptions = ParticleTypes.SMOKE;
        }

        for (int i = 0; i < 7; i++) {
            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(particleoptions, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d0, d1, d2);
        }
    }

    protected void reassessTrustingGoals() {
        if (this.ocelotAvoidPlayersGoal == null) {
            this.ocelotAvoidPlayersGoal = new Ocelot.OcelotAvoidEntityGoal<>(this, Player.class, 16.0F, 0.8, 1.33);
        }

        this.goalSelector.removeGoal(this.ocelotAvoidPlayersGoal);
        if (!this.isTrusting()) {
            this.goalSelector.addGoal(4, this.ocelotAvoidPlayersGoal);
        }
    }

    @Nullable
    public Ocelot getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return EntityType.OCELOT.create(level);
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on the animal type)
     */
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.OCELOT_FOOD);
    }

    public static boolean checkOcelotSpawnRules(
        EntityType<Ocelot> ocelot, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
    ) {
        return random.nextInt(3) != 0;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        if (level.isUnobstructed(this) && !level.containsAnyLiquid(this.getBoundingBox())) {
            BlockPos blockpos = this.blockPosition();
            if (blockpos.getY() < level.getSeaLevel()) {
                return false;
            }

            BlockState blockstate = level.getBlockState(blockpos.below());
            if (blockstate.is(Blocks.GRASS_BLOCK) || blockstate.is(BlockTags.LEAVES)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnGroupData == null) {
            spawnGroupData = new AgeableMob.AgeableMobGroupData(1.0F);
        }

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
    }

    @Override
    public boolean isSteppingCarefully() {
        return this.isCrouching() || super.isSteppingCarefully();
    }

    static class OcelotAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final Ocelot ocelot;

        public OcelotAvoidEntityGoal(Ocelot ocelot, Class<T> entityClassToAvoid, float maxDist, double walkSpeedModifier, double sprintSpeedModifier) {
            super(ocelot, entityClassToAvoid, maxDist, walkSpeedModifier, sprintSpeedModifier, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
            this.ocelot = ocelot;
        }

        @Override
        public boolean canUse() {
            return !this.ocelot.isTrusting() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.ocelot.isTrusting() && super.canContinueToUse();
        }
    }

    static class OcelotTemptGoal extends TemptGoal {
        private final Ocelot ocelot;

        public OcelotTemptGoal(Ocelot ocelot, double speedModifier, Predicate<ItemStack> items, boolean canScare) {
            super(ocelot, speedModifier, items, canScare);
            this.ocelot = ocelot;
        }

        @Override
        protected boolean canScare() {
            return super.canScare() && !this.ocelot.isTrusting();
        }
    }
}
