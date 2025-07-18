package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class Parrot extends ShoulderRidingEntity implements VariantHolder<Parrot.Variant>, FlyingAnimal {
    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Parrot.class, EntityDataSerializers.INT);
    private static final Predicate<Mob> NOT_PARROT_PREDICATE = new Predicate<Mob>() {
        public boolean test(@Nullable Mob mob) {
            return mob != null && (Parrot.MOB_SOUND_MAP.containsKey(mob.getType()) || mob.getType().builtInRegistryHolder().getData(net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps.PARROT_IMITATIONS) != null);
        }
    };
    /** @deprecated Neo: use the {@link net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps#PARROT_IMITATIONS data map} instead */
    @Deprecated
    static final Map<EntityType<?>, SoundEvent> MOB_SOUND_MAP = Util.make(Maps.newHashMap(), p_326764_ -> {
        p_326764_.put(EntityType.BLAZE, SoundEvents.PARROT_IMITATE_BLAZE);
        p_326764_.put(EntityType.BOGGED, SoundEvents.PARROT_IMITATE_BOGGED);
        p_326764_.put(EntityType.BREEZE, SoundEvents.PARROT_IMITATE_BREEZE);
        p_326764_.put(EntityType.CAVE_SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
        p_326764_.put(EntityType.CREEPER, SoundEvents.PARROT_IMITATE_CREEPER);
        p_326764_.put(EntityType.DROWNED, SoundEvents.PARROT_IMITATE_DROWNED);
        p_326764_.put(EntityType.ELDER_GUARDIAN, SoundEvents.PARROT_IMITATE_ELDER_GUARDIAN);
        p_326764_.put(EntityType.ENDER_DRAGON, SoundEvents.PARROT_IMITATE_ENDER_DRAGON);
        p_326764_.put(EntityType.ENDERMITE, SoundEvents.PARROT_IMITATE_ENDERMITE);
        p_326764_.put(EntityType.EVOKER, SoundEvents.PARROT_IMITATE_EVOKER);
        p_326764_.put(EntityType.GHAST, SoundEvents.PARROT_IMITATE_GHAST);
        p_326764_.put(EntityType.GUARDIAN, SoundEvents.PARROT_IMITATE_GUARDIAN);
        p_326764_.put(EntityType.HOGLIN, SoundEvents.PARROT_IMITATE_HOGLIN);
        p_326764_.put(EntityType.HUSK, SoundEvents.PARROT_IMITATE_HUSK);
        p_326764_.put(EntityType.ILLUSIONER, SoundEvents.PARROT_IMITATE_ILLUSIONER);
        p_326764_.put(EntityType.MAGMA_CUBE, SoundEvents.PARROT_IMITATE_MAGMA_CUBE);
        p_326764_.put(EntityType.PHANTOM, SoundEvents.PARROT_IMITATE_PHANTOM);
        p_326764_.put(EntityType.PIGLIN, SoundEvents.PARROT_IMITATE_PIGLIN);
        p_326764_.put(EntityType.PIGLIN_BRUTE, SoundEvents.PARROT_IMITATE_PIGLIN_BRUTE);
        p_326764_.put(EntityType.PILLAGER, SoundEvents.PARROT_IMITATE_PILLAGER);
        p_326764_.put(EntityType.RAVAGER, SoundEvents.PARROT_IMITATE_RAVAGER);
        p_326764_.put(EntityType.SHULKER, SoundEvents.PARROT_IMITATE_SHULKER);
        p_326764_.put(EntityType.SILVERFISH, SoundEvents.PARROT_IMITATE_SILVERFISH);
        p_326764_.put(EntityType.SKELETON, SoundEvents.PARROT_IMITATE_SKELETON);
        p_326764_.put(EntityType.SLIME, SoundEvents.PARROT_IMITATE_SLIME);
        p_326764_.put(EntityType.SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
        p_326764_.put(EntityType.STRAY, SoundEvents.PARROT_IMITATE_STRAY);
        p_326764_.put(EntityType.VEX, SoundEvents.PARROT_IMITATE_VEX);
        p_326764_.put(EntityType.VINDICATOR, SoundEvents.PARROT_IMITATE_VINDICATOR);
        p_326764_.put(EntityType.WARDEN, SoundEvents.PARROT_IMITATE_WARDEN);
        p_326764_.put(EntityType.WITCH, SoundEvents.PARROT_IMITATE_WITCH);
        p_326764_.put(EntityType.WITHER, SoundEvents.PARROT_IMITATE_WITHER);
        p_326764_.put(EntityType.WITHER_SKELETON, SoundEvents.PARROT_IMITATE_WITHER_SKELETON);
        p_326764_.put(EntityType.ZOGLIN, SoundEvents.PARROT_IMITATE_ZOGLIN);
        p_326764_.put(EntityType.ZOMBIE, SoundEvents.PARROT_IMITATE_ZOMBIE);
        p_326764_.put(EntityType.ZOMBIE_VILLAGER, SoundEvents.PARROT_IMITATE_ZOMBIE_VILLAGER);
    });
    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    private float flapping = 1.0F;
    private float nextFlap = 1.0F;
    private boolean partyParrot;
    @Nullable
    private BlockPos jukebox;

    public Parrot(EntityType<? extends Parrot> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.COCOA, -1.0F);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(Util.getRandom(Parrot.Variant.values(), level.getRandom()));
        if (spawnGroupData == null) {
            spawnGroupData = new AgeableMob.AgeableMobGroupData(false);
        }

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new TamableAnimal.TamableAnimalPanicGoal(1.25));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0, 5.0F, 1.0F));
        this.goalSelector.addGoal(2, new Parrot.ParrotWanderGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LandOnOwnersShoulderGoal(this));
        this.goalSelector.addGoal(3, new FollowMobGoal(this, 1.0, 3.0F, 7.0F));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 6.0)
            .add(Attributes.FLYING_SPEED, 0.4F)
            .add(Attributes.MOVEMENT_SPEED, 0.2F)
            .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    @Override
    public void aiStep() {
        if (this.jukebox == null || !this.jukebox.closerToCenterThan(this.position(), 3.46) || !this.level().getBlockState(this.jukebox).is(Blocks.JUKEBOX)) {
            this.partyParrot = false;
            this.jukebox = null;
        }

        if (this.level().random.nextInt(400) == 0) {
            imitateNearbyMobs(this.level(), this);
        }

        super.aiStep();
        this.calculateFlapping();
    }

    /**
     * Called when a record starts or stops playing. Used to make parrots start or stop partying.
     */
    @Override
    public void setRecordPlayingNearby(BlockPos pos, boolean isPartying) {
        this.jukebox = pos;
        this.partyParrot = isPartying;
    }

    public boolean isPartyParrot() {
        return this.partyParrot;
    }

    private void calculateFlapping() {
        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        this.flapSpeed = this.flapSpeed + (float)(!this.onGround() && !this.isPassenger() ? 4 : -1) * 0.3F;
        this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0F, 1.0F);
        if (!this.onGround() && this.flapping < 1.0F) {
            this.flapping = 1.0F;
        }

        this.flapping *= 0.9F;
        Vec3 vec3 = this.getDeltaMovement();
        if (!this.onGround() && vec3.y < 0.0) {
            this.setDeltaMovement(vec3.multiply(1.0, 0.6, 1.0));
        }

        this.flap = this.flap + this.flapping * 2.0F;
    }

    public static boolean imitateNearbyMobs(Level level, Entity parrot) {
        if (parrot.isAlive() && !parrot.isSilent() && level.random.nextInt(2) == 0) {
            List<Mob> list = level.getEntitiesOfClass(Mob.class, parrot.getBoundingBox().inflate(20.0), NOT_PARROT_PREDICATE);
            if (!list.isEmpty()) {
                Mob mob = list.get(level.random.nextInt(list.size()));
                if (!mob.isSilent()) {
                    SoundEvent soundevent = getImitatedSound(mob.getType());
                    level.playSound(
                        null, parrot.getX(), parrot.getY(), parrot.getZ(), soundevent, parrot.getSoundSource(), 0.7F, getPitch(level.random)
                    );
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!this.isTame() && itemstack.is(ItemTags.PARROT_FOOD)) {
            itemstack.consume(1, player);
            if (!this.isSilent()) {
                this.level()
                    .playSound(
                        null,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        SoundEvents.PARROT_EAT,
                        this.getSoundSource(),
                        1.0F,
                        1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
                    );
            }

            if (!this.level().isClientSide) {
                if (this.random.nextInt(10) == 0 && !net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player)) {
                    this.tame(player);
                    this.level().broadcastEntityEvent(this, (byte)7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte)6);
                }
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else if (!itemstack.is(ItemTags.PARROT_POISONOUS_FOOD)) {
            if (!this.isFlying() && this.isTame() && this.isOwnedBy(player)) {
                if (!this.level().isClientSide) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                }

                return InteractionResult.sidedSuccess(this.level().isClientSide);
            } else {
                return super.mobInteract(player, hand);
            }
        } else {
            itemstack.consume(1, player);
            this.addEffect(new MobEffectInstance(MobEffects.POISON, 900));
            if (player.isCreative() || !this.isInvulnerable()) {
                this.hurt(this.damageSources().playerAttack(player), Float.MAX_VALUE);
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on the animal type)
     */
    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    public static boolean checkParrotSpawnRules(
        EntityType<Parrot> parrot, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
    ) {
        return level.getBlockState(pos.below()).is(BlockTags.PARROTS_SPAWNABLE_ON) && isBrightEnoughToSpawn(level, pos);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    /**
     * Returns {@code true} if the mob is currently able to mate with the specified mob.
     */
    @Override
    public boolean canMate(Animal otherAnimal) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound() {
        return getAmbient(this.level(), this.level().random);
    }

    public static SoundEvent getAmbient(Level level, RandomSource random) {
        if (level.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(1000) == 0) {
            final var entities = new java.util.HashSet<>(MOB_SOUND_MAP.keySet());
            final var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENTITY_TYPE);
            registry.getDataMap(net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps.PARROT_IMITATIONS).keySet().forEach(key -> entities.add(registry.get(key)));
            List<EntityType<?>> list = Lists.newArrayList(entities);
            return getImitatedSound(list.get(random.nextInt(list.size())));
        } else {
            return SoundEvents.PARROT_AMBIENT;
        }
    }

    private static SoundEvent getImitatedSound(EntityType<?> type) {
        var imitation = type.builtInRegistryHolder().getData(net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps.PARROT_IMITATIONS);
        if (imitation != null) return imitation.sound();
        return SoundEvents.PARROT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.PARROT_STEP, 0.15F, 1.0F);
    }

    @Override
    protected boolean isFlapping() {
        return this.flyDist > this.nextFlap;
    }

    @Override
    protected void onFlap() {
        this.playSound(SoundEvents.PARROT_FLY, 0.15F, 1.0F);
        this.nextFlap = this.flyDist + this.flapSpeed / 2.0F;
    }

    @Override
    public float getVoicePitch() {
        return getPitch(this.random);
    }

    public static float getPitch(RandomSource random) {
        return (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void doPush(Entity entity) {
        if (!(entity instanceof Player)) {
            super.doPush(entity);
        }
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            if (!this.level().isClientSide) {
                this.setOrderedToSit(false);
            }

            return super.hurt(source, amount);
        }
    }

    public Parrot.Variant getVariant() {
        return Parrot.Variant.byId(this.entityData.get(DATA_VARIANT_ID));
    }

    public void setVariant(Parrot.Variant variant) {
        this.entityData.set(DATA_VARIANT_ID, variant.id);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT_ID, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", this.getVariant().id);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setVariant(Parrot.Variant.byId(compound.getInt("Variant")));
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    protected boolean canFlyToOwner() {
        return true;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
    }

    static class ParrotWanderGoal extends WaterAvoidingRandomFlyingGoal {
        public ParrotWanderGoal(PathfinderMob p_186224_, double p_186225_) {
            super(p_186224_, p_186225_);
        }

        @Nullable
        @Override
        protected Vec3 getPosition() {
            Vec3 vec3 = null;
            if (this.mob.isInWater()) {
                vec3 = LandRandomPos.getPos(this.mob, 15, 15);
            }

            if (this.mob.getRandom().nextFloat() >= this.probability) {
                vec3 = this.getTreePos();
            }

            return vec3 == null ? super.getPosition() : vec3;
        }

        @Nullable
        private Vec3 getTreePos() {
            BlockPos blockpos = this.mob.blockPosition();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos blockpos$mutableblockpos1 = new BlockPos.MutableBlockPos();

            for (BlockPos blockpos1 : BlockPos.betweenClosed(
                Mth.floor(this.mob.getX() - 3.0),
                Mth.floor(this.mob.getY() - 6.0),
                Mth.floor(this.mob.getZ() - 3.0),
                Mth.floor(this.mob.getX() + 3.0),
                Mth.floor(this.mob.getY() + 6.0),
                Mth.floor(this.mob.getZ() + 3.0)
            )) {
                if (!blockpos.equals(blockpos1)) {
                    BlockState blockstate = this.mob.level().getBlockState(blockpos$mutableblockpos1.setWithOffset(blockpos1, Direction.DOWN));
                    boolean flag = blockstate.getBlock() instanceof LeavesBlock || blockstate.is(BlockTags.LOGS);
                    if (flag
                        && this.mob.level().isEmptyBlock(blockpos1)
                        && this.mob.level().isEmptyBlock(blockpos$mutableblockpos.setWithOffset(blockpos1, Direction.UP))) {
                        return Vec3.atBottomCenterOf(blockpos1);
                    }
                }
            }

            return null;
        }
    }

    public static enum Variant implements StringRepresentable {
        RED_BLUE(0, "red_blue"),
        BLUE(1, "blue"),
        GREEN(2, "green"),
        YELLOW_BLUE(3, "yellow_blue"),
        GRAY(4, "gray");

        public static final Codec<Parrot.Variant> CODEC = StringRepresentable.fromEnum(Parrot.Variant::values);
        private static final IntFunction<Parrot.Variant> BY_ID = ByIdMap.continuous(Parrot.Variant::getId, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
        final int id;
        private final String name;

        private Variant(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return this.id;
        }

        public static Parrot.Variant byId(int id) {
            return BY_ID.apply(id);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
