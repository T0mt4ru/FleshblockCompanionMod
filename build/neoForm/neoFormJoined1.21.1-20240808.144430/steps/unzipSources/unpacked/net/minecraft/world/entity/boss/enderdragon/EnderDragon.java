package net.minecraft.world.entity.boss.enderdragon;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class EnderDragon extends Mob implements Enemy {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(EnderDragon.class, EntityDataSerializers.INT);
    private static final TargetingConditions CRYSTAL_DESTROY_TARGETING = TargetingConditions.forCombat().range(64.0);
    private static final int GROWL_INTERVAL_MIN = 200;
    private static final int GROWL_INTERVAL_MAX = 400;
    private static final float SITTING_ALLOWED_DAMAGE_PERCENTAGE = 0.25F;
    private static final String DRAGON_DEATH_TIME_KEY = "DragonDeathTime";
    private static final String DRAGON_PHASE_KEY = "DragonPhase";
    public final double[][] positions = new double[64][3];
    public int posPointer = -1;
    private final EnderDragonPart[] subEntities;
    public final EnderDragonPart head;
    private final EnderDragonPart neck;
    private final EnderDragonPart body;
    private final EnderDragonPart tail1;
    private final EnderDragonPart tail2;
    private final EnderDragonPart tail3;
    private final EnderDragonPart wing1;
    private final EnderDragonPart wing2;
    public float oFlapTime;
    public float flapTime;
    public boolean inWall;
    public int dragonDeathTime;
    public float yRotA;
    @Nullable
    public EndCrystal nearestCrystal;
    @Nullable
    private EndDragonFight dragonFight;
    private BlockPos fightOrigin = BlockPos.ZERO;
    private final EnderDragonPhaseManager phaseManager;
    private int growlTime = 100;
    private float sittingDamageReceived;
    private final Node[] nodes = new Node[24];
    private final int[] nodeAdjacency = new int[24];
    private final BinaryHeap openSet = new BinaryHeap();

    public EnderDragon(EntityType<? extends EnderDragon> entityType, Level level) {
        super(EntityType.ENDER_DRAGON, level);
        this.head = new EnderDragonPart(this, "head", 1.0F, 1.0F);
        this.neck = new EnderDragonPart(this, "neck", 3.0F, 3.0F);
        this.body = new EnderDragonPart(this, "body", 5.0F, 3.0F);
        this.tail1 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
        this.tail2 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
        this.tail3 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
        this.wing1 = new EnderDragonPart(this, "wing", 4.0F, 2.0F);
        this.wing2 = new EnderDragonPart(this, "wing", 4.0F, 2.0F);
        this.subEntities = new EnderDragonPart[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.wing1, this.wing2};
        this.setHealth(this.getMaxHealth());
        this.noPhysics = true;
        this.noCulling = true;
        this.phaseManager = new EnderDragonPhaseManager(this);
        this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1); // Forge: Fix MC-158205: Make sure part ids are successors of parent mob id
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < this.subEntities.length; i++) // Forge: Fix MC-158205: Set part ids to successors of parent mob id
            this.subEntities[i].setId(id + i + 1);
    }

    public void setDragonFight(EndDragonFight dragonFight) {
        this.dragonFight = dragonFight;
    }

    public void setFightOrigin(BlockPos fightOrigin) {
        this.fightOrigin = fightOrigin;
    }

    public BlockPos getFightOrigin() {
        return this.fightOrigin;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 200.0);
    }

    @Override
    public boolean isFlapping() {
        float f = Mth.cos(this.flapTime * (float) (Math.PI * 2));
        float f1 = Mth.cos(this.oFlapTime * (float) (Math.PI * 2));
        return f1 <= -0.3F && f >= -0.3F;
    }

    @Override
    public void onFlap() {
        if (this.level().isClientSide && !this.isSilent()) {
            this.level()
                .playLocalSound(
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.ENDER_DRAGON_FLAP,
                    this.getSoundSource(),
                    5.0F,
                    0.8F + this.random.nextFloat() * 0.3F,
                    false
                );
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, EnderDragonPhase.HOVERING.getId());
    }

    /**
     * Returns a double[3] array with movement offsets, used to calculate trailing tail/neck positions. [0] = yaw offset, [1] = y offset, [2] = unused, always 0. Parameters: buffer index offset, partial ticks.
     */
    public double[] getLatencyPos(int bufferIndexOffset, float partialTicks) {
        if (this.isDeadOrDying()) {
            partialTicks = 0.0F;
        }

        partialTicks = 1.0F - partialTicks;
        int i = this.posPointer - bufferIndexOffset & 63;
        int j = this.posPointer - bufferIndexOffset - 1 & 63;
        double[] adouble = new double[3];
        double d0 = this.positions[i][0];
        double d1 = Mth.wrapDegrees(this.positions[j][0] - d0);
        adouble[0] = d0 + d1 * (double)partialTicks;
        d0 = this.positions[i][1];
        d1 = this.positions[j][1] - d0;
        adouble[1] = d0 + d1 * (double)partialTicks;
        adouble[2] = Mth.lerp((double)partialTicks, this.positions[i][2], this.positions[j][2]);
        return adouble;
    }

    @org.jetbrains.annotations.Nullable private Player unlimitedLastHurtByPlayer = null;
    @Override
    public void aiStep() {
        // lastHurtByPlayer is cleared after 100 ticks, capture it indefinitely in unlimitedLastHurtByPlayer for LivingExperienceDropEvent
        if (this.lastHurtByPlayer != null) this.unlimitedLastHurtByPlayer = lastHurtByPlayer;
        if (this.unlimitedLastHurtByPlayer != null && this.unlimitedLastHurtByPlayer.isRemoved()) this.unlimitedLastHurtByPlayer = null;
        this.processFlappingMovement();
        if (this.level().isClientSide) {
            this.setHealth(this.getHealth());
            if (!this.isSilent() && !this.phaseManager.getCurrentPhase().isSitting() && --this.growlTime < 0) {
                this.level()
                    .playLocalSound(
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        SoundEvents.ENDER_DRAGON_GROWL,
                        this.getSoundSource(),
                        2.5F,
                        0.8F + this.random.nextFloat() * 0.3F,
                        false
                    );
                this.growlTime = 200 + this.random.nextInt(200);
            }
        }

        if (this.dragonFight == null && this.level() instanceof ServerLevel serverlevel) {
            EndDragonFight enddragonfight = serverlevel.getDragonFight();
            if (enddragonfight != null && this.getUUID().equals(enddragonfight.getDragonUUID())) {
                this.dragonFight = enddragonfight;
            }
        }

        this.oFlapTime = this.flapTime;
        if (this.isDeadOrDying()) {
            float f7 = (this.random.nextFloat() - 0.5F) * 8.0F;
            float f9 = (this.random.nextFloat() - 0.5F) * 4.0F;
            float f10 = (this.random.nextFloat() - 0.5F) * 8.0F;
            this.level()
                .addParticle(ParticleTypes.EXPLOSION, this.getX() + (double)f7, this.getY() + 2.0 + (double)f9, this.getZ() + (double)f10, 0.0, 0.0, 0.0);
        } else {
            this.checkCrystals();
            Vec3 vec34 = this.getDeltaMovement();
            float f8 = 0.2F / ((float)vec34.horizontalDistance() * 10.0F + 1.0F);
            f8 *= (float)Math.pow(2.0, vec34.y);
            if (this.phaseManager.getCurrentPhase().isSitting()) {
                this.flapTime += 0.1F;
            } else if (this.inWall) {
                this.flapTime += f8 * 0.5F;
            } else {
                this.flapTime += f8;
            }

            this.setYRot(Mth.wrapDegrees(this.getYRot()));
            if (this.isNoAi()) {
                this.flapTime = 0.5F;
            } else {
                if (this.posPointer < 0) {
                    for (int i = 0; i < this.positions.length; i++) {
                        this.positions[i][0] = (double)this.getYRot();
                        this.positions[i][1] = this.getY();
                    }
                }

                if (++this.posPointer == this.positions.length) {
                    this.posPointer = 0;
                }

                this.positions[this.posPointer][0] = (double)this.getYRot();
                this.positions[this.posPointer][1] = this.getY();
                if (this.level().isClientSide) {
                    if (this.lerpSteps > 0) {
                        this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
                        this.lerpSteps--;
                    }

                    this.phaseManager.getCurrentPhase().doClientTick();
                } else {
                    DragonPhaseInstance dragonphaseinstance = this.phaseManager.getCurrentPhase();
                    dragonphaseinstance.doServerTick();
                    if (this.phaseManager.getCurrentPhase() != dragonphaseinstance) {
                        dragonphaseinstance = this.phaseManager.getCurrentPhase();
                        dragonphaseinstance.doServerTick();
                    }

                    Vec3 vec3 = dragonphaseinstance.getFlyTargetLocation();
                    if (vec3 != null) {
                        double d0 = vec3.x - this.getX();
                        double d1 = vec3.y - this.getY();
                        double d2 = vec3.z - this.getZ();
                        double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                        float f3 = dragonphaseinstance.getFlySpeed();
                        double d4 = Math.sqrt(d0 * d0 + d2 * d2);
                        if (d4 > 0.0) {
                            d1 = Mth.clamp(d1 / d4, (double)(-f3), (double)f3);
                        }

                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, d1 * 0.01, 0.0));
                        this.setYRot(Mth.wrapDegrees(this.getYRot()));
                        Vec3 vec31 = vec3.subtract(this.getX(), this.getY(), this.getZ()).normalize();
                        Vec3 vec32 = new Vec3(
                                (double)Mth.sin(this.getYRot() * (float) (Math.PI / 180.0)),
                                this.getDeltaMovement().y,
                                (double)(-Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)))
                            )
                            .normalize();
                        float f4 = Math.max(((float)vec32.dot(vec31) + 0.5F) / 1.5F, 0.0F);
                        if (Math.abs(d0) > 1.0E-5F || Math.abs(d2) > 1.0E-5F) {
                            float f5 = Mth.clamp(Mth.wrapDegrees(180.0F - (float)Mth.atan2(d0, d2) * (180.0F / (float)Math.PI) - this.getYRot()), -50.0F, 50.0F);
                            this.yRotA *= 0.8F;
                            this.yRotA = this.yRotA + f5 * dragonphaseinstance.getTurnSpeed();
                            this.setYRot(this.getYRot() + this.yRotA * 0.1F);
                        }

                        float f19 = (float)(2.0 / (d3 + 1.0));
                        float f6 = 0.06F;
                        this.moveRelative(0.06F * (f4 * f19 + (1.0F - f19)), new Vec3(0.0, 0.0, -1.0));
                        if (this.inWall) {
                            this.move(MoverType.SELF, this.getDeltaMovement().scale(0.8F));
                        } else {
                            this.move(MoverType.SELF, this.getDeltaMovement());
                        }

                        Vec3 vec33 = this.getDeltaMovement().normalize();
                        double d5 = 0.8 + 0.15 * (vec33.dot(vec32) + 1.0) / 2.0;
                        this.setDeltaMovement(this.getDeltaMovement().multiply(d5, 0.91F, d5));
                    }
                }

                this.yBodyRot = this.getYRot();
                Vec3[] avec3 = new Vec3[this.subEntities.length];

                for (int j = 0; j < this.subEntities.length; j++) {
                    avec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
                }

                float f11 = (float)(this.getLatencyPos(5, 1.0F)[1] - this.getLatencyPos(10, 1.0F)[1]) * 10.0F * (float) (Math.PI / 180.0);
                float f12 = Mth.cos(f11);
                float f = Mth.sin(f11);
                float f13 = this.getYRot() * (float) (Math.PI / 180.0);
                float f1 = Mth.sin(f13);
                float f14 = Mth.cos(f13);
                this.tickPart(this.body, (double)(f1 * 0.5F), 0.0, (double)(-f14 * 0.5F));
                this.tickPart(this.wing1, (double)(f14 * 4.5F), 2.0, (double)(f1 * 4.5F));
                this.tickPart(this.wing2, (double)(f14 * -4.5F), 2.0, (double)(f1 * -4.5F));
                if (this.level() instanceof ServerLevel serverlevel1 && this.hurtTime == 0) {
                    this.knockBack(
                        serverlevel1,
                        serverlevel1.getEntities(
                            this, this.wing1.getBoundingBox().inflate(4.0, 2.0, 4.0).move(0.0, -2.0, 0.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR
                        )
                    );
                    this.knockBack(
                        serverlevel1,
                        serverlevel1.getEntities(
                            this, this.wing2.getBoundingBox().inflate(4.0, 2.0, 4.0).move(0.0, -2.0, 0.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR
                        )
                    );
                    this.hurt(serverlevel1.getEntities(this, this.head.getBoundingBox().inflate(1.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
                    this.hurt(serverlevel1.getEntities(this, this.neck.getBoundingBox().inflate(1.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
                }

                float f15 = Mth.sin(this.getYRot() * (float) (Math.PI / 180.0) - this.yRotA * 0.01F);
                float f16 = Mth.cos(this.getYRot() * (float) (Math.PI / 180.0) - this.yRotA * 0.01F);
                float f2 = this.getHeadYOffset();
                this.tickPart(this.head, (double)(f15 * 6.5F * f12), (double)(f2 + f * 6.5F), (double)(-f16 * 6.5F * f12));
                this.tickPart(this.neck, (double)(f15 * 5.5F * f12), (double)(f2 + f * 5.5F), (double)(-f16 * 5.5F * f12));
                double[] adouble = this.getLatencyPos(5, 1.0F);

                for (int k = 0; k < 3; k++) {
                    EnderDragonPart enderdragonpart = null;
                    if (k == 0) {
                        enderdragonpart = this.tail1;
                    }

                    if (k == 1) {
                        enderdragonpart = this.tail2;
                    }

                    if (k == 2) {
                        enderdragonpart = this.tail3;
                    }

                    double[] adouble1 = this.getLatencyPos(12 + k * 2, 1.0F);
                    float f17 = this.getYRot() * (float) (Math.PI / 180.0) + this.rotWrap(adouble1[0] - adouble[0]) * (float) (Math.PI / 180.0);
                    float f18 = Mth.sin(f17);
                    float f20 = Mth.cos(f17);
                    float f21 = 1.5F;
                    float f22 = (float)(k + 1) * 2.0F;
                    this.tickPart(
                        enderdragonpart,
                        (double)(-(f1 * 1.5F + f18 * f22) * f12),
                        adouble1[1] - adouble[1] - (double)((f22 + 1.5F) * f) + 1.5,
                        (double)((f14 * 1.5F + f20 * f22) * f12)
                    );
                }

                if (!this.level().isClientSide) {
                    this.inWall = this.checkWalls(this.head.getBoundingBox())
                        | this.checkWalls(this.neck.getBoundingBox())
                        | this.checkWalls(this.body.getBoundingBox());
                    if (this.dragonFight != null) {
                        this.dragonFight.updateDragon(this);
                    }
                }

                for (int l = 0; l < this.subEntities.length; l++) {
                    this.subEntities[l].xo = avec3[l].x;
                    this.subEntities[l].yo = avec3[l].y;
                    this.subEntities[l].zo = avec3[l].z;
                    this.subEntities[l].xOld = avec3[l].x;
                    this.subEntities[l].yOld = avec3[l].y;
                    this.subEntities[l].zOld = avec3[l].z;
                }
            }
        }
    }

    private void tickPart(EnderDragonPart part, double offsetX, double offsetY, double offsetZ) {
        part.setPos(this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ);
    }

    private float getHeadYOffset() {
        if (this.phaseManager.getCurrentPhase().isSitting()) {
            return -1.0F;
        } else {
            double[] adouble = this.getLatencyPos(5, 1.0F);
            double[] adouble1 = this.getLatencyPos(0, 1.0F);
            return (float)(adouble[1] - adouble1[1]);
        }
    }

    private void checkCrystals() {
        if (this.nearestCrystal != null) {
            if (this.nearestCrystal.isRemoved()) {
                this.nearestCrystal = null;
            } else if (this.tickCount % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 1.0F);
            }
        }

        if (this.random.nextInt(10) == 0) {
            List<EndCrystal> list = this.level().getEntitiesOfClass(EndCrystal.class, this.getBoundingBox().inflate(32.0));
            EndCrystal endcrystal = null;
            double d0 = Double.MAX_VALUE;

            for (EndCrystal endcrystal1 : list) {
                double d1 = endcrystal1.distanceToSqr(this);
                if (d1 < d0) {
                    d0 = d1;
                    endcrystal = endcrystal1;
                }
            }

            this.nearestCrystal = endcrystal;
        }
    }

    private void knockBack(ServerLevel level, List<Entity> targets) {
        double d0 = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0;
        double d1 = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0;

        for (Entity entity : targets) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity)entity;
                double d2 = entity.getX() - d0;
                double d3 = entity.getZ() - d1;
                double d4 = Math.max(d2 * d2 + d3 * d3, 0.1);
                entity.push(d2 / d4 * 4.0, 0.2F, d3 / d4 * 4.0);
                if (!this.phaseManager.getCurrentPhase().isSitting() && livingentity.getLastHurtByMobTimestamp() < entity.tickCount - 2) {
                    DamageSource damagesource = this.damageSources().mobAttack(this);
                    entity.hurt(damagesource, 5.0F);
                    EnchantmentHelper.doPostAttackEffects(level, entity, damagesource);
                }
            }
        }
    }

    /**
     * Attacks all entities inside this list, dealing 5 hearts of damage.
     */
    private void hurt(List<Entity> entities) {
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity) {
                DamageSource damagesource = this.damageSources().mobAttack(this);
                entity.hurt(damagesource, 10.0F);
                if (this.level() instanceof ServerLevel serverlevel) {
                    EnchantmentHelper.doPostAttackEffects(serverlevel, entity, damagesource);
                }
            }
        }
    }

    /**
     * Simplifies the value of a number by adding/subtracting 180 to the point that the number is between -180 and 180.
     */
    private float rotWrap(double angle) {
        return (float)Mth.wrapDegrees(angle);
    }

    /**
     * Destroys all blocks that aren't associated with 'The End' inside the given bounding box.
     */
    private boolean checkWalls(AABB area) {
        int i = Mth.floor(area.minX);
        int j = Mth.floor(area.minY);
        int k = Mth.floor(area.minZ);
        int l = Mth.floor(area.maxX);
        int i1 = Mth.floor(area.maxY);
        int j1 = Mth.floor(area.maxZ);
        boolean flag = false;
        boolean flag1 = false;

        for (int k1 = i; k1 <= l; k1++) {
            for (int l1 = j; l1 <= i1; l1++) {
                for (int i2 = k; i2 <= j1; i2++) {
                    BlockPos blockpos = new BlockPos(k1, l1, i2);
                    BlockState blockstate = this.level().getBlockState(blockpos);
                    if (!blockstate.isAir() && !blockstate.is(BlockTags.DRAGON_TRANSPARENT)) {
                        if (net.neoforged.neoforge.common.CommonHooks.canEntityDestroy(this.level(), blockpos, this) && !blockstate.is(BlockTags.DRAGON_IMMUNE)) {
                            flag1 = this.level().removeBlock(blockpos, false) || flag1;
                        } else {
                            flag = true;
                        }
                    }
                }
            }
        }

        if (flag1) {
            BlockPos blockpos1 = new BlockPos(i + this.random.nextInt(l - i + 1), j + this.random.nextInt(i1 - j + 1), k + this.random.nextInt(j1 - k + 1));
            this.level().levelEvent(2008, blockpos1, 0);
        }

        return flag;
    }

    public boolean hurt(EnderDragonPart part, DamageSource source, float damage) {
        if (this.phaseManager.getCurrentPhase().getPhase() == EnderDragonPhase.DYING) {
            return false;
        } else {
            damage = this.phaseManager.getCurrentPhase().onHurt(source, damage);
            if (part != this.head) {
                damage = damage / 4.0F + Math.min(damage, 1.0F);
            }

            if (damage < 0.01F) {
                return false;
            } else {
                if (source.getEntity() instanceof Player || source.is(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS)) {
                    float f = this.getHealth();
                    this.reallyHurt(source, damage);
                    if (this.isDeadOrDying() && !this.phaseManager.getCurrentPhase().isSitting()) {
                        this.setHealth(1.0F);
                        this.phaseManager.setPhase(EnderDragonPhase.DYING);
                    }

                    if (this.phaseManager.getCurrentPhase().isSitting()) {
                        this.sittingDamageReceived = this.sittingDamageReceived + f - this.getHealth();
                        if (this.sittingDamageReceived > 0.25F * this.getMaxHealth()) {
                            this.sittingDamageReceived = 0.0F;
                            this.phaseManager.setPhase(EnderDragonPhase.TAKEOFF);
                        }
                    }
                }

                return true;
            }
        }
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !this.level().isClientSide ? this.hurt(this.body, source, amount) : false;
    }

    /**
     * Provides a way to cause damage to an ender dragon.
     */
    protected boolean reallyHurt(DamageSource damageSource, float amount) {
        return super.hurt(damageSource, amount);
    }

    @Override
    public void kill() {
        this.remove(Entity.RemovalReason.KILLED);
        this.gameEvent(GameEvent.ENTITY_DIE);
        if (this.dragonFight != null) {
            this.dragonFight.updateDragon(this);
            this.dragonFight.setDragonKilled(this);
        }
    }

    @Override
    protected void tickDeath() {
        if (this.dragonFight != null) {
            this.dragonFight.updateDragon(this);
        }

        this.dragonDeathTime++;
        if (this.dragonDeathTime >= 180 && this.dragonDeathTime <= 200) {
            float f = (this.random.nextFloat() - 0.5F) * 8.0F;
            float f1 = (this.random.nextFloat() - 0.5F) * 4.0F;
            float f2 = (this.random.nextFloat() - 0.5F) * 8.0F;
            this.level()
                .addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getX() + (double)f, this.getY() + 2.0 + (double)f1, this.getZ() + (double)f2, 0.0, 0.0, 0.0);
        }

        boolean flag = this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
        int i = 500;
        if (this.dragonFight != null && !this.dragonFight.hasPreviouslyKilledDragon()) {
            i = 12000;
        }

        if (this.level() instanceof ServerLevel) {
            if (this.dragonDeathTime > 150 && this.dragonDeathTime % 5 == 0 && flag) {
                int award = net.neoforged.neoforge.event.EventHooks.getExperienceDrop(this, this.unlimitedLastHurtByPlayer, Mth.floor((float)i * 0.08F));
                ExperienceOrb.award((ServerLevel) this.level(), this.position(), award);
            }

            if (this.dragonDeathTime == 1 && !this.isSilent()) {
                this.level().globalLevelEvent(1028, this.blockPosition(), 0);
            }
        }

        this.move(MoverType.SELF, new Vec3(0.0, 0.1F, 0.0));
        if (this.dragonDeathTime == 200 && this.level() instanceof ServerLevel) {
            if (flag) {
                int award = net.neoforged.neoforge.event.EventHooks.getExperienceDrop(this, this.unlimitedLastHurtByPlayer, Mth.floor((float)i * 0.2F));
                ExperienceOrb.award((ServerLevel) this.level(), this.position(), award);
            }

            if (this.dragonFight != null) {
                this.dragonFight.setDragonKilled(this);
            }

            this.remove(Entity.RemovalReason.KILLED);
            this.gameEvent(GameEvent.ENTITY_DIE);
        }
    }

    public int findClosestNode() {
        if (this.nodes[0] == null) {
            for (int i = 0; i < 24; i++) {
                int j = 5;
                int l;
                int i1;
                if (i < 12) {
                    l = Mth.floor(60.0F * Mth.cos(2.0F * ((float) -Math.PI + (float) (Math.PI / 12) * (float)i)));
                    i1 = Mth.floor(60.0F * Mth.sin(2.0F * ((float) -Math.PI + (float) (Math.PI / 12) * (float)i)));
                } else if (i < 20) {
                    int $$2 = i - 12;
                    l = Mth.floor(40.0F * Mth.cos(2.0F * ((float) -Math.PI + (float) (Math.PI / 8) * (float)$$2)));
                    i1 = Mth.floor(40.0F * Mth.sin(2.0F * ((float) -Math.PI + (float) (Math.PI / 8) * (float)$$2)));
                    j += 10;
                } else {
                    int k1 = i - 20;
                    l = Mth.floor(20.0F * Mth.cos(2.0F * ((float) -Math.PI + (float) (Math.PI / 4) * (float)k1)));
                    i1 = Mth.floor(20.0F * Mth.sin(2.0F * ((float) -Math.PI + (float) (Math.PI / 4) * (float)k1)));
                }

                int j1 = Math.max(
                    this.level().getSeaLevel() + 10, this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(l, 0, i1)).getY() + j
                );
                this.nodes[i] = new Node(l, j1, i1);
            }

            this.nodeAdjacency[0] = 6146;
            this.nodeAdjacency[1] = 8197;
            this.nodeAdjacency[2] = 8202;
            this.nodeAdjacency[3] = 16404;
            this.nodeAdjacency[4] = 32808;
            this.nodeAdjacency[5] = 32848;
            this.nodeAdjacency[6] = 65696;
            this.nodeAdjacency[7] = 131392;
            this.nodeAdjacency[8] = 131712;
            this.nodeAdjacency[9] = 263424;
            this.nodeAdjacency[10] = 526848;
            this.nodeAdjacency[11] = 525313;
            this.nodeAdjacency[12] = 1581057;
            this.nodeAdjacency[13] = 3166214;
            this.nodeAdjacency[14] = 2138120;
            this.nodeAdjacency[15] = 6373424;
            this.nodeAdjacency[16] = 4358208;
            this.nodeAdjacency[17] = 12910976;
            this.nodeAdjacency[18] = 9044480;
            this.nodeAdjacency[19] = 9706496;
            this.nodeAdjacency[20] = 15216640;
            this.nodeAdjacency[21] = 13688832;
            this.nodeAdjacency[22] = 11763712;
            this.nodeAdjacency[23] = 8257536;
        }

        return this.findClosestNode(this.getX(), this.getY(), this.getZ());
    }

    /**
     * Returns the index into pathPoints of the nearest PathPoint.
     */
    public int findClosestNode(double x, double y, double z) {
        float f = 10000.0F;
        int i = 0;
        Node node = new Node(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        int j = 0;
        if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
            j = 12;
        }

        for (int k = j; k < 24; k++) {
            if (this.nodes[k] != null) {
                float f1 = this.nodes[k].distanceToSqr(node);
                if (f1 < f) {
                    f = f1;
                    i = k;
                }
            }
        }

        return i;
    }

    /**
     * Find and return a path among the circles described by pathPoints, or null if the shortest path would just be directly between the start and finish with no intermediate points.
     *
     * Starting with pathPoint[startIdx], it searches the neighboring points (and their neighboring points, and so on) until it reaches pathPoint[finishIdx], at which point it calls makePath to seal the deal.
     */
    @Nullable
    public Path findPath(int startIndex, int finishIndex, @Nullable Node andThen) {
        for (int i = 0; i < 24; i++) {
            Node node = this.nodes[i];
            node.closed = false;
            node.f = 0.0F;
            node.g = 0.0F;
            node.h = 0.0F;
            node.cameFrom = null;
            node.heapIdx = -1;
        }

        Node node4 = this.nodes[startIndex];
        Node node5 = this.nodes[finishIndex];
        node4.g = 0.0F;
        node4.h = node4.distanceTo(node5);
        node4.f = node4.h;
        this.openSet.clear();
        this.openSet.insert(node4);
        Node node1 = node4;
        int j = 0;
        if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
            j = 12;
        }

        while (!this.openSet.isEmpty()) {
            Node node2 = this.openSet.pop();
            if (node2.equals(node5)) {
                if (andThen != null) {
                    andThen.cameFrom = node5;
                    node5 = andThen;
                }

                return this.reconstructPath(node4, node5);
            }

            if (node2.distanceTo(node5) < node1.distanceTo(node5)) {
                node1 = node2;
            }

            node2.closed = true;
            int k = 0;

            for (int l = 0; l < 24; l++) {
                if (this.nodes[l] == node2) {
                    k = l;
                    break;
                }
            }

            for (int i1 = j; i1 < 24; i1++) {
                if ((this.nodeAdjacency[k] & 1 << i1) > 0) {
                    Node node3 = this.nodes[i1];
                    if (!node3.closed) {
                        float f = node2.g + node2.distanceTo(node3);
                        if (!node3.inOpenSet() || f < node3.g) {
                            node3.cameFrom = node2;
                            node3.g = f;
                            node3.h = node3.distanceTo(node5);
                            if (node3.inOpenSet()) {
                                this.openSet.changeCost(node3, node3.g + node3.h);
                            } else {
                                node3.f = node3.g + node3.h;
                                this.openSet.insert(node3);
                            }
                        }
                    }
                }
            }
        }

        if (node1 == node4) {
            return null;
        } else {
            LOGGER.debug("Failed to find path from {} to {}", startIndex, finishIndex);
            if (andThen != null) {
                andThen.cameFrom = node1;
                node1 = andThen;
            }

            return this.reconstructPath(node4, node1);
        }
    }

    /**
     * Create and return a new PathEntity defining a path from the start to the finish, using the connections already made by the caller, findPath.
     */
    private Path reconstructPath(Node start, Node finish) {
        List<Node> list = Lists.newArrayList();
        Node node = finish;
        list.add(0, finish);

        while (node.cameFrom != null) {
            node = node.cameFrom;
            list.add(0, node);
        }

        return new Path(list, new BlockPos(finish.x, finish.y, finish.z), true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("DragonPhase", this.phaseManager.getCurrentPhase().getPhase().getId());
        compound.putInt("DragonDeathTime", this.dragonDeathTime);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("DragonPhase")) {
            this.phaseManager.setPhase(EnderDragonPhase.getById(compound.getInt("DragonPhase")));
        }

        if (compound.contains("DragonDeathTime")) {
            this.dragonDeathTime = compound.getInt("DragonDeathTime");
        }
    }

    @Override
    public void checkDespawn() {
    }

    public EnderDragonPart[] getSubEntities() {
        return this.subEntities;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0F;
    }

    public float getHeadPartYOffset(int partIndex, double[] spineEndOffsets, double[] headPartOffsets) {
        DragonPhaseInstance dragonphaseinstance = this.phaseManager.getCurrentPhase();
        EnderDragonPhase<? extends DragonPhaseInstance> enderdragonphase = dragonphaseinstance.getPhase();
        double d0;
        if (enderdragonphase == EnderDragonPhase.LANDING || enderdragonphase == EnderDragonPhase.TAKEOFF) {
            BlockPos blockpos = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.fightOrigin));
            double d1 = Math.max(Math.sqrt(blockpos.distToCenterSqr(this.position())) / 4.0, 1.0);
            d0 = (double)partIndex / d1;
        } else if (dragonphaseinstance.isSitting()) {
            d0 = (double)partIndex;
        } else if (partIndex == 6) {
            d0 = 0.0;
        } else {
            d0 = headPartOffsets[1] - spineEndOffsets[1];
        }

        return (float)d0;
    }

    public Vec3 getHeadLookVector(float partialTicks) {
        DragonPhaseInstance dragonphaseinstance = this.phaseManager.getCurrentPhase();
        EnderDragonPhase<? extends DragonPhaseInstance> enderdragonphase = dragonphaseinstance.getPhase();
        Vec3 vec3;
        if (enderdragonphase == EnderDragonPhase.LANDING || enderdragonphase == EnderDragonPhase.TAKEOFF) {
            BlockPos blockpos = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.fightOrigin));
            float f5 = Math.max((float)Math.sqrt(blockpos.distToCenterSqr(this.position())) / 4.0F, 1.0F);
            float f2 = 6.0F / f5;
            float f3 = this.getXRot();
            float f4 = 1.5F;
            this.setXRot(-f2 * 1.5F * 5.0F);
            vec3 = this.getViewVector(partialTicks);
            this.setXRot(f3);
        } else if (dragonphaseinstance.isSitting()) {
            float f = this.getXRot();
            float f1 = 1.5F;
            this.setXRot(-45.0F);
            vec3 = this.getViewVector(partialTicks);
            this.setXRot(f);
        } else {
            vec3 = this.getViewVector(partialTicks);
        }

        return vec3;
    }

    public void onCrystalDestroyed(EndCrystal crystal, BlockPos pos, DamageSource damageSource) {
        Player player;
        if (damageSource.getEntity() instanceof Player) {
            player = (Player)damageSource.getEntity();
        } else {
            player = this.level().getNearestPlayer(CRYSTAL_DESTROY_TARGETING, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
        }

        if (crystal == this.nearestCrystal) {
            this.hurt(this.head, this.damageSources().explosion(crystal, player), 10.0F);
        }

        this.phaseManager.getCurrentPhase().onCrystalDestroyed(crystal, pos, damageSource, player);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_PHASE.equals(key) && this.level().isClientSide) {
            this.phaseManager.setPhase(EnderDragonPhase.getById(this.getEntityData().get(DATA_PHASE)));
        }

        super.onSyncedDataUpdated(key);
    }

    public EnderDragonPhaseManager getPhaseManager() {
        return this.phaseManager;
    }

    @Nullable
    public EndDragonFight getDragonFight() {
        return this.dragonFight;
    }

    @Override
    public boolean addEffect(MobEffectInstance effectInstance, @Nullable Entity entity) {
        return false;
    }

    @Override
    protected boolean canRide(Entity entity) {
        return false;
    }

    @Override
    public boolean canUsePortal(boolean allowPassengers) {
        return false;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public net.neoforged.neoforge.entity.PartEntity<?>[] getParts() {
        return this.subEntities;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        if (true) return; // Forge: Fix MC-158205: Moved into setId()
        EnderDragonPart[] aenderdragonpart = this.getSubEntities();

        for (int i = 0; i < aenderdragonpart.length; i++) {
            aenderdragonpart[i].setId(i + packet.getId());
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target.canBeSeenAsEnemy();
    }

    @Override
    protected float sanitizeScale(float scale) {
        return 1.0F;
    }
}
