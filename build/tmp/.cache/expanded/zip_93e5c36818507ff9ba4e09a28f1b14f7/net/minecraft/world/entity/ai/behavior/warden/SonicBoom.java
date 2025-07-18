package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

public class SonicBoom extends Behavior<Warden> {
    private static final int DISTANCE_XZ = 15;
    private static final int DISTANCE_Y = 20;
    private static final double KNOCKBACK_VERTICAL = 0.5;
    private static final double KNOCKBACK_HORIZONTAL = 2.5;
    public static final int COOLDOWN = 40;
    private static final int TICKS_BEFORE_PLAYING_SOUND = Mth.ceil(34.0);
    private static final int DURATION = Mth.ceil(60.0F);

    public SonicBoom() {
        super(
            ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.SONIC_BOOM_COOLDOWN,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN,
                MemoryStatus.REGISTERED,
                MemoryModuleType.SONIC_BOOM_SOUND_DELAY,
                MemoryStatus.REGISTERED
            ),
            DURATION
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel level, Warden owner) {
        return owner.closerThan(owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get(), 15.0, 20.0);
    }

    protected boolean canStillUse(ServerLevel level, Warden entity, long gameTime) {
        return true;
    }

    protected void start(ServerLevel level, Warden entity, long gameTime) {
        entity.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long)DURATION);
        entity.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_SOUND_DELAY, Unit.INSTANCE, (long)TICKS_BEFORE_PLAYING_SOUND);
        level.broadcastEntityEvent(entity, (byte)62);
        entity.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 3.0F, 1.0F);
    }

    protected void tick(ServerLevel level, Warden owner, long gameTime) {
        owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(p_352767_ -> owner.getLookControl().setLookAt(p_352767_.position()));
        if (!owner.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_DELAY)
            && !owner.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN)) {
            owner.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, Unit.INSTANCE, (long)(DURATION - TICKS_BEFORE_PLAYING_SOUND));
            owner.getBrain()
                .getMemory(MemoryModuleType.ATTACK_TARGET)
                .filter(owner::canTargetEntity)
                .filter(p_217707_ -> owner.closerThan(p_217707_, 15.0, 20.0))
                .ifPresent(p_340767_ -> {
                    Vec3 vec3 = owner.position().add(owner.getAttachments().get(EntityAttachment.WARDEN_CHEST, 0, owner.getYRot()));
                    Vec3 vec31 = p_340767_.getEyePosition().subtract(vec3);
                    Vec3 vec32 = vec31.normalize();
                    int i = Mth.floor(vec31.length()) + 7;

                    for (int j = 1; j < i; j++) {
                        Vec3 vec33 = vec3.add(vec32.scale((double)j));
                        level.sendParticles(ParticleTypes.SONIC_BOOM, vec33.x, vec33.y, vec33.z, 1, 0.0, 0.0, 0.0, 0.0);
                    }

                    owner.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
                    if (p_340767_.hurt(level.damageSources().sonicBoom(owner), 10.0F)) {
                        double d1 = 0.5 * (1.0 - p_340767_.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                        double d0 = 2.5 * (1.0 - p_340767_.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                        p_340767_.push(vec32.x() * d0, vec32.y() * d1, vec32.z() * d0);
                    }
                });
        }
    }

    protected void stop(ServerLevel level, Warden entity, long gameTime) {
        setCooldown(entity, 40);
    }

    public static void setCooldown(LivingEntity entity, int cooldown) {
        entity.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_COOLDOWN, Unit.INSTANCE, (long)cooldown);
    }
}
