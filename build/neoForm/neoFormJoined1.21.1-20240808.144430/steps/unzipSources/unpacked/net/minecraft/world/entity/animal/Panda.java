package net.minecraft.world.entity.animal;

import java.util.EnumSet;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class Panda extends Animal {
    private static final EntityDataAccessor<Integer> UNHAPPY_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SNEEZE_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EAT_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> MAIN_GENE_ID = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> HIDDEN_GENE_ID = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
    static final TargetingConditions BREED_TARGETING = TargetingConditions.forNonCombat().range(8.0);
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.PANDA
        .getDimensions()
        .scale(0.5F)
        .withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0F, 0.40625F, 0.0F));
    private static final int FLAG_SNEEZE = 2;
    private static final int FLAG_ROLL = 4;
    private static final int FLAG_SIT = 8;
    private static final int FLAG_ON_BACK = 16;
    private static final int EAT_TICK_INTERVAL = 5;
    public static final int TOTAL_ROLL_STEPS = 32;
    private static final int TOTAL_UNHAPPY_TIME = 32;
    boolean gotBamboo;
    boolean didBite;
    public int rollCounter;
    private Vec3 rollDelta;
    private float sitAmount;
    private float sitAmountO;
    private float onBackAmount;
    private float onBackAmountO;
    private float rollAmount;
    private float rollAmountO;
    Panda.PandaLookAtPlayerGoal lookAtPlayerGoal;
    static final Predicate<ItemEntity> PANDA_ITEMS = p_350087_ -> {
        ItemStack itemstack = p_350087_.getItem();
        return (itemstack.is(Blocks.BAMBOO.asItem()) || itemstack.is(Blocks.CAKE.asItem())) && p_350087_.isAlive() && !p_350087_.hasPickUpDelay();
    };

    public Panda(EntityType<? extends Panda> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new Panda.PandaMoveControl(this);
        if (!this.isBaby()) {
            this.setCanPickUpLoot(true);
        }
    }

    @Override
    public boolean canTakeItem(ItemStack itemstack) {
        EquipmentSlot equipmentslot = this.getEquipmentSlotForItem(itemstack);
        return !this.getItemBySlot(equipmentslot).isEmpty() ? false : equipmentslot == EquipmentSlot.MAINHAND && super.canTakeItem(itemstack);
    }

    public int getUnhappyCounter() {
        return this.entityData.get(UNHAPPY_COUNTER);
    }

    public void setUnhappyCounter(int unhappyCounter) {
        this.entityData.set(UNHAPPY_COUNTER, unhappyCounter);
    }

    public boolean isSneezing() {
        return this.getFlag(2);
    }

    public boolean isSitting() {
        return this.getFlag(8);
    }

    public void sit(boolean sitting) {
        this.setFlag(8, sitting);
    }

    public boolean isOnBack() {
        return this.getFlag(16);
    }

    public void setOnBack(boolean onBack) {
        this.setFlag(16, onBack);
    }

    public boolean isEating() {
        return this.entityData.get(EAT_COUNTER) > 0;
    }

    public void eat(boolean eating) {
        this.entityData.set(EAT_COUNTER, eating ? 1 : 0);
    }

    private int getEatCounter() {
        return this.entityData.get(EAT_COUNTER);
    }

    private void setEatCounter(int eatCounter) {
        this.entityData.set(EAT_COUNTER, eatCounter);
    }

    public void sneeze(boolean sneezing) {
        this.setFlag(2, sneezing);
        if (!sneezing) {
            this.setSneezeCounter(0);
        }
    }

    public int getSneezeCounter() {
        return this.entityData.get(SNEEZE_COUNTER);
    }

    public void setSneezeCounter(int sneezeCounter) {
        this.entityData.set(SNEEZE_COUNTER, sneezeCounter);
    }

    public Panda.Gene getMainGene() {
        return Panda.Gene.byId(this.entityData.get(MAIN_GENE_ID));
    }

    public void setMainGene(Panda.Gene pandaType) {
        if (pandaType.getId() > 6) {
            pandaType = Panda.Gene.getRandom(this.random);
        }

        this.entityData.set(MAIN_GENE_ID, (byte)pandaType.getId());
    }

    public Panda.Gene getHiddenGene() {
        return Panda.Gene.byId(this.entityData.get(HIDDEN_GENE_ID));
    }

    public void setHiddenGene(Panda.Gene pandaType) {
        if (pandaType.getId() > 6) {
            pandaType = Panda.Gene.getRandom(this.random);
        }

        this.entityData.set(HIDDEN_GENE_ID, (byte)pandaType.getId());
    }

    public boolean isRolling() {
        return this.getFlag(4);
    }

    public void roll(boolean rolling) {
        this.setFlag(4, rolling);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(UNHAPPY_COUNTER, 0);
        builder.define(SNEEZE_COUNTER, 0);
        builder.define(MAIN_GENE_ID, (byte)0);
        builder.define(HIDDEN_GENE_ID, (byte)0);
        builder.define(DATA_ID_FLAGS, (byte)0);
        builder.define(EAT_COUNTER, 0);
    }

    private boolean getFlag(int flag) {
        return (this.entityData.get(DATA_ID_FLAGS) & flag) != 0;
    }

    private void setFlag(int flagId, boolean value) {
        byte b0 = this.entityData.get(DATA_ID_FLAGS);
        if (value) {
            this.entityData.set(DATA_ID_FLAGS, (byte)(b0 | flagId));
        } else {
            this.entityData.set(DATA_ID_FLAGS, (byte)(b0 & ~flagId));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("MainGene", this.getMainGene().getSerializedName());
        compound.putString("HiddenGene", this.getHiddenGene().getSerializedName());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setMainGene(Panda.Gene.byName(compound.getString("MainGene")));
        this.setHiddenGene(Panda.Gene.byName(compound.getString("HiddenGene")));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        Panda panda = EntityType.PANDA.create(level);
        if (panda != null) {
            if (otherParent instanceof Panda panda1) {
                panda.setGeneFromParents(this, panda1);
            }

            panda.setAttributes();
        }

        return panda;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new Panda.PandaPanicGoal(this, 2.0));
        this.goalSelector.addGoal(2, new Panda.PandaBreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new Panda.PandaAttackGoal(this, 1.2F, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, p_335256_ -> p_335256_.is(ItemTags.PANDA_FOOD), false));
        this.goalSelector.addGoal(6, new Panda.PandaAvoidGoal<>(this, Player.class, 8.0F, 2.0, 2.0));
        this.goalSelector.addGoal(6, new Panda.PandaAvoidGoal<>(this, Monster.class, 4.0F, 2.0, 2.0));
        this.goalSelector.addGoal(7, new Panda.PandaSitGoal());
        this.goalSelector.addGoal(8, new Panda.PandaLieOnBackGoal(this));
        this.goalSelector.addGoal(8, new Panda.PandaSneezeGoal(this));
        this.lookAtPlayerGoal = new Panda.PandaLookAtPlayerGoal(this, Player.class, 6.0F);
        this.goalSelector.addGoal(9, this.lookAtPlayerGoal);
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(12, new Panda.PandaRollGoal(this));
        this.goalSelector.addGoal(13, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(14, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new Panda.PandaHurtByTargetGoal(this).setAlertOthers(new Class[0]));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.15F).add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    public Panda.Gene getVariant() {
        return Panda.Gene.getVariantFromGenes(this.getMainGene(), this.getHiddenGene());
    }

    public boolean isLazy() {
        return this.getVariant() == Panda.Gene.LAZY;
    }

    public boolean isWorried() {
        return this.getVariant() == Panda.Gene.WORRIED;
    }

    public boolean isPlayful() {
        return this.getVariant() == Panda.Gene.PLAYFUL;
    }

    public boolean isBrown() {
        return this.getVariant() == Panda.Gene.BROWN;
    }

    public boolean isWeak() {
        return this.getVariant() == Panda.Gene.WEAK;
    }

    @Override
    public boolean isAggressive() {
        return this.getVariant() == Panda.Gene.AGGRESSIVE;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (!this.isAggressive()) {
            this.didBite = true;
        }

        return super.doHurtTarget(entity);
    }

    @Override
    public void playAttackSound() {
        this.playSound(SoundEvents.PANDA_BITE, 1.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isWorried()) {
            if (this.level().isThundering() && !this.isInWater()) {
                this.sit(true);
                this.eat(false);
            } else if (!this.isEating()) {
                this.sit(false);
            }
        }

        LivingEntity livingentity = this.getTarget();
        if (livingentity == null) {
            this.gotBamboo = false;
            this.didBite = false;
        }

        if (this.getUnhappyCounter() > 0) {
            if (livingentity != null) {
                this.lookAt(livingentity, 90.0F, 90.0F);
            }

            if (this.getUnhappyCounter() == 29 || this.getUnhappyCounter() == 14) {
                this.playSound(SoundEvents.PANDA_CANT_BREED, 1.0F, 1.0F);
            }

            this.setUnhappyCounter(this.getUnhappyCounter() - 1);
        }

        if (this.isSneezing()) {
            this.setSneezeCounter(this.getSneezeCounter() + 1);
            if (this.getSneezeCounter() > 20) {
                this.sneeze(false);
                this.afterSneeze();
            } else if (this.getSneezeCounter() == 1) {
                this.playSound(SoundEvents.PANDA_PRE_SNEEZE, 1.0F, 1.0F);
            }
        }

        if (this.isRolling()) {
            this.handleRoll();
        } else {
            this.rollCounter = 0;
        }

        if (this.isSitting()) {
            this.setXRot(0.0F);
        }

        this.updateSitAmount();
        this.handleEating();
        this.updateOnBackAnimation();
        this.updateRollAmount();
    }

    public boolean isScared() {
        return this.isWorried() && this.level().isThundering();
    }

    private void handleEating() {
        if (!this.isEating() && this.isSitting() && !this.isScared() && !this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && this.random.nextInt(80) == 1) {
            this.eat(true);
        } else if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() || !this.isSitting()) {
            this.eat(false);
        }

        if (this.isEating()) {
            this.addEatingParticles();
            if (!this.level().isClientSide && this.getEatCounter() > 80 && this.random.nextInt(20) == 1) {
                if (this.getEatCounter() > 100 && this.isFoodOrCake(this.getItemBySlot(EquipmentSlot.MAINHAND))) {
                    if (!this.level().isClientSide) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        this.gameEvent(GameEvent.EAT);
                    }

                    this.sit(false);
                }

                this.eat(false);
                return;
            }

            this.setEatCounter(this.getEatCounter() + 1);
        }
    }

    private void addEatingParticles() {
        if (this.getEatCounter() % 5 == 0) {
            this.playSound(
                SoundEvents.PANDA_EAT, 0.5F + 0.5F * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F
            );

            for (int i = 0; i < 6; i++) {
                Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, ((double)this.random.nextFloat() - 0.5) * 0.1);
                vec3 = vec3.xRot(-this.getXRot() * (float) (Math.PI / 180.0));
                vec3 = vec3.yRot(-this.getYRot() * (float) (Math.PI / 180.0));
                double d0 = (double)(-this.random.nextFloat()) * 0.6 - 0.3;
                Vec3 vec31 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.8, d0, 1.0 + ((double)this.random.nextFloat() - 0.5) * 0.4);
                vec31 = vec31.yRot(-this.yBodyRot * (float) (Math.PI / 180.0));
                vec31 = vec31.add(this.getX(), this.getEyeY() + 1.0, this.getZ());
                this.level()
                    .addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, this.getItemBySlot(EquipmentSlot.MAINHAND)),
                        vec31.x,
                        vec31.y,
                        vec31.z,
                        vec3.x,
                        vec3.y + 0.05,
                        vec3.z
                    );
            }
        }
    }

    private void updateSitAmount() {
        this.sitAmountO = this.sitAmount;
        if (this.isSitting()) {
            this.sitAmount = Math.min(1.0F, this.sitAmount + 0.15F);
        } else {
            this.sitAmount = Math.max(0.0F, this.sitAmount - 0.19F);
        }
    }

    private void updateOnBackAnimation() {
        this.onBackAmountO = this.onBackAmount;
        if (this.isOnBack()) {
            this.onBackAmount = Math.min(1.0F, this.onBackAmount + 0.15F);
        } else {
            this.onBackAmount = Math.max(0.0F, this.onBackAmount - 0.19F);
        }
    }

    private void updateRollAmount() {
        this.rollAmountO = this.rollAmount;
        if (this.isRolling()) {
            this.rollAmount = Math.min(1.0F, this.rollAmount + 0.15F);
        } else {
            this.rollAmount = Math.max(0.0F, this.rollAmount - 0.19F);
        }
    }

    public float getSitAmount(float partialTick) {
        return Mth.lerp(partialTick, this.sitAmountO, this.sitAmount);
    }

    public float getLieOnBackAmount(float partialTick) {
        return Mth.lerp(partialTick, this.onBackAmountO, this.onBackAmount);
    }

    public float getRollAmount(float partialTick) {
        return Mth.lerp(partialTick, this.rollAmountO, this.rollAmount);
    }

    private void handleRoll() {
        this.rollCounter++;
        if (this.rollCounter > 32) {
            this.roll(false);
        } else {
            if (!this.level().isClientSide) {
                Vec3 vec3 = this.getDeltaMovement();
                if (this.rollCounter == 1) {
                    float f = this.getYRot() * (float) (Math.PI / 180.0);
                    float f1 = this.isBaby() ? 0.1F : 0.2F;
                    this.rollDelta = new Vec3(vec3.x + (double)(-Mth.sin(f) * f1), 0.0, vec3.z + (double)(Mth.cos(f) * f1));
                    this.setDeltaMovement(this.rollDelta.add(0.0, 0.27, 0.0));
                } else if ((float)this.rollCounter != 7.0F && (float)this.rollCounter != 15.0F && (float)this.rollCounter != 23.0F) {
                    this.setDeltaMovement(this.rollDelta.x, vec3.y, this.rollDelta.z);
                } else {
                    this.setDeltaMovement(0.0, this.onGround() ? 0.27 : vec3.y, 0.0);
                }
            }
        }
    }

    private void afterSneeze() {
        Vec3 vec3 = this.getDeltaMovement();
        Level level = this.level();
        level.addParticle(
            ParticleTypes.SNEEZE,
            this.getX() - (double)(this.getBbWidth() + 1.0F) * 0.5 * (double)Mth.sin(this.yBodyRot * (float) (Math.PI / 180.0)),
            this.getEyeY() - 0.1F,
            this.getZ() + (double)(this.getBbWidth() + 1.0F) * 0.5 * (double)Mth.cos(this.yBodyRot * (float) (Math.PI / 180.0)),
            vec3.x,
            0.0,
            vec3.z
        );
        this.playSound(SoundEvents.PANDA_SNEEZE, 1.0F, 1.0F);

        for (Panda panda : level.getEntitiesOfClass(Panda.class, this.getBoundingBox().inflate(10.0))) {
            if (!panda.isBaby() && panda.onGround() && !panda.isInWater() && panda.canPerformAction()) {
                panda.jumpFromGround();
            }
        }

        if (!level.isClientSide() && level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            ServerLevel serverlevel = (ServerLevel)level;
            LootTable loottable = serverlevel.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.PANDA_SNEEZE);
            LootParams lootparams = new LootParams.Builder(serverlevel)
                .withParameter(LootContextParams.ORIGIN, this.position())
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .create(LootContextParamSets.GIFT);

            for (ItemStack itemstack : loottable.getRandomItems(lootparams)) {
                this.spawnAtLocation(itemstack);
            }
        }
    }

    /**
     * Tests if this entity should pick up a weapon or an armor piece. Entity drops current weapon or armor if the new one is better.
     */
    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && PANDA_ITEMS.test(itemEntity)) {
            this.onItemPickup(itemEntity);
            ItemStack itemstack = itemEntity.getItem();
            this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            this.take(itemEntity, itemstack.getCount());
            itemEntity.discard();
        }
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide) {
            this.sit(false);
        }

        return super.hurt(source, amount);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource randomsource = level.getRandom();
        this.setMainGene(Panda.Gene.getRandom(randomsource));
        this.setHiddenGene(Panda.Gene.getRandom(randomsource));
        this.setAttributes();
        if (spawnGroupData == null) {
            spawnGroupData = new AgeableMob.AgeableMobGroupData(0.2F);
        }

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    public void setGeneFromParents(Panda father, @Nullable Panda mother) {
        if (mother == null) {
            if (this.random.nextBoolean()) {
                this.setMainGene(father.getOneOfGenesRandomly());
                this.setHiddenGene(Panda.Gene.getRandom(this.random));
            } else {
                this.setMainGene(Panda.Gene.getRandom(this.random));
                this.setHiddenGene(father.getOneOfGenesRandomly());
            }
        } else if (this.random.nextBoolean()) {
            this.setMainGene(father.getOneOfGenesRandomly());
            this.setHiddenGene(mother.getOneOfGenesRandomly());
        } else {
            this.setMainGene(mother.getOneOfGenesRandomly());
            this.setHiddenGene(father.getOneOfGenesRandomly());
        }

        if (this.random.nextInt(32) == 0) {
            this.setMainGene(Panda.Gene.getRandom(this.random));
        }

        if (this.random.nextInt(32) == 0) {
            this.setHiddenGene(Panda.Gene.getRandom(this.random));
        }
    }

    private Panda.Gene getOneOfGenesRandomly() {
        return this.random.nextBoolean() ? this.getMainGene() : this.getHiddenGene();
    }

    public void setAttributes() {
        if (this.isWeak()) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10.0);
        }

        if (this.isLazy()) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.07F);
        }
    }

    void tryToSit() {
        if (!this.isInWater()) {
            this.setZza(0.0F);
            this.getNavigation().stop();
            this.sit(true);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.isScared()) {
            return InteractionResult.PASS;
        } else if (this.isOnBack()) {
            this.setOnBack(false);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else if (this.isFood(itemstack)) {
            if (this.getTarget() != null) {
                this.gotBamboo = true;
            }

            if (this.isBaby()) {
                this.usePlayerItem(player, hand, itemstack);
                this.ageUp((int)((float)(-this.getAge() / 20) * 0.1F), true);
            } else if (!this.level().isClientSide && this.getAge() == 0 && this.canFallInLove()) {
                this.usePlayerItem(player, hand, itemstack);
                this.setInLove(player);
            } else {
                if (this.level().isClientSide || this.isSitting() || this.isInWater()) {
                    return InteractionResult.PASS;
                }

                this.tryToSit();
                this.eat(true);
                ItemStack itemstack1 = this.getItemBySlot(EquipmentSlot.MAINHAND);
                if (!itemstack1.isEmpty() && !player.hasInfiniteMaterials()) {
                    this.spawnAtLocation(itemstack1);
                }

                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(itemstack.getItem(), 1));
                this.usePlayerItem(player, hand, itemstack);
            }

            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isAggressive()) {
            return SoundEvents.PANDA_AGGRESSIVE_AMBIENT;
        } else {
            return this.isWorried() ? SoundEvents.PANDA_WORRIED_AMBIENT : SoundEvents.PANDA_AMBIENT;
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.PANDA_STEP, 0.15F, 1.0F);
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on the animal type)
     */
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.PANDA_FOOD);
    }

    private boolean isFoodOrCake(ItemStack stack) {
        return this.isFood(stack) || stack.is(Blocks.CAKE.asItem());
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PANDA_DEATH;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PANDA_HURT;
    }

    public boolean canPerformAction() {
        return !this.isOnBack() && !this.isScared() && !this.isEating() && !this.isRolling() && !this.isSitting();
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
    }

    public static enum Gene implements StringRepresentable {
        NORMAL(0, "normal", false),
        LAZY(1, "lazy", false),
        WORRIED(2, "worried", false),
        PLAYFUL(3, "playful", false),
        BROWN(4, "brown", true),
        WEAK(5, "weak", true),
        AGGRESSIVE(6, "aggressive", false);

        public static final StringRepresentable.EnumCodec<Panda.Gene> CODEC = StringRepresentable.fromEnum(Panda.Gene::values);
        private static final IntFunction<Panda.Gene> BY_ID = ByIdMap.continuous(Panda.Gene::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        private static final int MAX_GENE = 6;
        private final int id;
        private final String name;
        private final boolean isRecessive;

        private Gene(int id, String name, boolean isRecessive) {
            this.id = id;
            this.name = name;
            this.isRecessive = isRecessive;
        }

        public int getId() {
            return this.id;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public boolean isRecessive() {
            return this.isRecessive;
        }

        static Panda.Gene getVariantFromGenes(Panda.Gene mainGene, Panda.Gene hiddenGene) {
            if (mainGene.isRecessive()) {
                return mainGene == hiddenGene ? mainGene : NORMAL;
            } else {
                return mainGene;
            }
        }

        public static Panda.Gene byId(int index) {
            return BY_ID.apply(index);
        }

        public static Panda.Gene byName(String name) {
            return CODEC.byName(name, NORMAL);
        }

        public static Panda.Gene getRandom(RandomSource random) {
            int i = random.nextInt(16);
            if (i == 0) {
                return LAZY;
            } else if (i == 1) {
                return WORRIED;
            } else if (i == 2) {
                return PLAYFUL;
            } else if (i == 4) {
                return AGGRESSIVE;
            } else if (i < 9) {
                return WEAK;
            } else {
                return i < 11 ? BROWN : NORMAL;
            }
        }
    }

    static class PandaAttackGoal extends MeleeAttackGoal {
        private final Panda panda;

        public PandaAttackGoal(Panda panda, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(panda, speedModifier, followingTargetEvenIfNotSeen);
            this.panda = panda;
        }

        @Override
        public boolean canUse() {
            return this.panda.canPerformAction() && super.canUse();
        }
    }

    static class PandaAvoidGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final Panda panda;

        public PandaAvoidGoal(Panda panda, Class<T> entityClassToAvoid, float maxDist, double walkSpeedModifier, double sprintSpeedModifier) {
            super(panda, entityClassToAvoid, maxDist, walkSpeedModifier, sprintSpeedModifier, EntitySelector.NO_SPECTATORS::test);
            this.panda = panda;
        }

        @Override
        public boolean canUse() {
            return this.panda.isWorried() && this.panda.canPerformAction() && super.canUse();
        }
    }

    static class PandaBreedGoal extends BreedGoal {
        private final Panda panda;
        private int unhappyCooldown;

        public PandaBreedGoal(Panda panda, double speedModifier) {
            super(panda, speedModifier);
            this.panda = panda;
        }

        @Override
        public boolean canUse() {
            if (!super.canUse() || this.panda.getUnhappyCounter() != 0) {
                return false;
            } else if (!this.canFindBamboo()) {
                if (this.unhappyCooldown <= this.panda.tickCount) {
                    this.panda.setUnhappyCounter(32);
                    this.unhappyCooldown = this.panda.tickCount + 600;
                    if (this.panda.isEffectiveAi()) {
                        Player player = this.level.getNearestPlayer(Panda.BREED_TARGETING, this.panda);
                        this.panda.lookAtPlayerGoal.setTarget(player);
                    }
                }

                return false;
            } else {
                return true;
            }
        }

        private boolean canFindBamboo() {
            BlockPos blockpos = this.panda.blockPosition();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 8; j++) {
                    for (int k = 0; k <= j; k = k > 0 ? -k : 1 - k) {
                        for (int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l) {
                            blockpos$mutableblockpos.setWithOffset(blockpos, k, i, l);
                            if (this.level.getBlockState(blockpos$mutableblockpos).is(Blocks.BAMBOO)) {
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }
    }

    static class PandaHurtByTargetGoal extends HurtByTargetGoal {
        private final Panda panda;

        public PandaHurtByTargetGoal(Panda panda, Class<?>... entityClassToIgnoreDamage) {
            super(panda, entityClassToIgnoreDamage);
            this.panda = panda;
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.panda.gotBamboo && !this.panda.didBite) {
                return super.canContinueToUse();
            } else {
                this.panda.setTarget(null);
                return false;
            }
        }

        @Override
        protected void alertOther(Mob mob, LivingEntity target) {
            if (mob instanceof Panda && mob.isAggressive()) {
                mob.setTarget(target);
            }
        }
    }

    static class PandaLieOnBackGoal extends Goal {
        private final Panda panda;
        private int cooldown;

        public PandaLieOnBackGoal(Panda panda) {
            this.panda = panda;
        }

        @Override
        public boolean canUse() {
            return this.cooldown < this.panda.tickCount
                && this.panda.isLazy()
                && this.panda.canPerformAction()
                && this.panda.random.nextInt(reducedTickDelay(400)) == 1;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.panda.isInWater() && (this.panda.isLazy() || this.panda.random.nextInt(reducedTickDelay(600)) != 1)
                ? this.panda.random.nextInt(reducedTickDelay(2000)) != 1
                : false;
        }

        @Override
        public void start() {
            this.panda.setOnBack(true);
            this.cooldown = 0;
        }

        @Override
        public void stop() {
            this.panda.setOnBack(false);
            this.cooldown = this.panda.tickCount + 200;
        }
    }

    static class PandaLookAtPlayerGoal extends LookAtPlayerGoal {
        private final Panda panda;

        public PandaLookAtPlayerGoal(Panda panda, Class<? extends LivingEntity> lookAtType, float lookDistance) {
            super(panda, lookAtType, lookDistance);
            this.panda = panda;
        }

        public void setTarget(LivingEntity lookAt) {
            this.lookAt = lookAt;
        }

        @Override
        public boolean canContinueToUse() {
            return this.lookAt != null && super.canContinueToUse();
        }

        @Override
        public boolean canUse() {
            if (this.mob.getRandom().nextFloat() >= this.probability) {
                return false;
            } else {
                if (this.lookAt == null) {
                    if (this.lookAtType == Player.class) {
                        this.lookAt = this.mob.level().getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
                    } else {
                        this.lookAt = this.mob
                            .level()
                            .getNearestEntity(
                                this.mob
                                    .level()
                                    .getEntitiesOfClass(
                                        this.lookAtType,
                                        this.mob.getBoundingBox().inflate((double)this.lookDistance, 3.0, (double)this.lookDistance),
                                        p_148985_ -> true
                                    ),
                                this.lookAtContext,
                                this.mob,
                                this.mob.getX(),
                                this.mob.getEyeY(),
                                this.mob.getZ()
                            );
                    }
                }

                return this.panda.canPerformAction() && this.lookAt != null;
            }
        }

        @Override
        public void tick() {
            if (this.lookAt != null) {
                super.tick();
            }
        }
    }

    static class PandaMoveControl extends MoveControl {
        private final Panda panda;

        public PandaMoveControl(Panda panda) {
            super(panda);
            this.panda = panda;
        }

        @Override
        public void tick() {
            if (this.panda.canPerformAction()) {
                super.tick();
            }
        }
    }

    static class PandaPanicGoal extends PanicGoal {
        private final Panda panda;

        public PandaPanicGoal(Panda panda, double speedModifier) {
            super(panda, speedModifier, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES);
            this.panda = panda;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.panda.isSitting()) {
                this.panda.getNavigation().stop();
                return false;
            } else {
                return super.canContinueToUse();
            }
        }
    }

    static class PandaRollGoal extends Goal {
        private final Panda panda;

        public PandaRollGoal(Panda panda) {
            this.panda = panda;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if ((this.panda.isBaby() || this.panda.isPlayful()) && this.panda.onGround()) {
                if (!this.panda.canPerformAction()) {
                    return false;
                } else {
                    float f = this.panda.getYRot() * (float) (Math.PI / 180.0);
                    float f1 = -Mth.sin(f);
                    float f2 = Mth.cos(f);
                    int i = (double)Math.abs(f1) > 0.5 ? Mth.sign((double)f1) : 0;
                    int j = (double)Math.abs(f2) > 0.5 ? Mth.sign((double)f2) : 0;
                    if (this.panda.level().getBlockState(this.panda.blockPosition().offset(i, -1, j)).isAir()) {
                        return true;
                    } else {
                        return this.panda.isPlayful() && this.panda.random.nextInt(reducedTickDelay(60)) == 1
                            ? true
                            : this.panda.random.nextInt(reducedTickDelay(500)) == 1;
                    }
                }
            } else {
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            this.panda.roll(true);
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }
    }

    class PandaSitGoal extends Goal {
        private int cooldown;

        public PandaSitGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown <= Panda.this.tickCount
                && !Panda.this.isBaby()
                && !Panda.this.isInWater()
                && Panda.this.canPerformAction()
                && Panda.this.getUnhappyCounter() <= 0) {
                List<ItemEntity> list = Panda.this.level()
                    .getEntitiesOfClass(ItemEntity.class, Panda.this.getBoundingBox().inflate(6.0, 6.0, 6.0), Panda.PANDA_ITEMS);
                return !list.isEmpty() || !Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
            } else {
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return !Panda.this.isInWater() && (Panda.this.isLazy() || Panda.this.random.nextInt(reducedTickDelay(600)) != 1)
                ? Panda.this.random.nextInt(reducedTickDelay(2000)) != 1
                : false;
        }

        @Override
        public void tick() {
            if (!Panda.this.isSitting() && !Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                Panda.this.tryToSit();
            }
        }

        @Override
        public void start() {
            List<ItemEntity> list = Panda.this.level()
                .getEntitiesOfClass(ItemEntity.class, Panda.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Panda.PANDA_ITEMS);
            if (!list.isEmpty() && Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                Panda.this.getNavigation().moveTo(list.get(0), 1.2F);
            } else if (!Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                Panda.this.tryToSit();
            }

            this.cooldown = 0;
        }

        @Override
        public void stop() {
            ItemStack itemstack = Panda.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!itemstack.isEmpty()) {
                Panda.this.spawnAtLocation(itemstack);
                Panda.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                int i = Panda.this.isLazy() ? Panda.this.random.nextInt(50) + 10 : Panda.this.random.nextInt(150) + 10;
                this.cooldown = Panda.this.tickCount + i * 20;
            }

            Panda.this.sit(false);
        }
    }

    static class PandaSneezeGoal extends Goal {
        private final Panda panda;

        public PandaSneezeGoal(Panda panda) {
            this.panda = panda;
        }

        @Override
        public boolean canUse() {
            if (this.panda.isBaby() && this.panda.canPerformAction()) {
                return this.panda.isWeak() && this.panda.random.nextInt(reducedTickDelay(500)) == 1
                    ? true
                    : this.panda.random.nextInt(reducedTickDelay(6000)) == 1;
            } else {
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            this.panda.sneeze(true);
        }
    }
}
