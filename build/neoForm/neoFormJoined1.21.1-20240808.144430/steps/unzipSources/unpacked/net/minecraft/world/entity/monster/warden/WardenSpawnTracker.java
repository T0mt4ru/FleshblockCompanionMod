package net.minecraft.world.entity.monster.warden;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WardenSpawnTracker {
    public static final Codec<WardenSpawnTracker> CODEC = RecordCodecBuilder.create(
        p_219589_ -> p_219589_.group(
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("ticks_since_last_warning").orElse(0).forGetter(p_219607_ -> p_219607_.ticksSinceLastWarning),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("warning_level").orElse(0).forGetter(p_219604_ -> p_219604_.warningLevel),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("cooldown_ticks").orElse(0).forGetter(p_219601_ -> p_219601_.cooldownTicks)
                )
                .apply(p_219589_, WardenSpawnTracker::new)
    );
    public static final int MAX_WARNING_LEVEL = 4;
    private static final double PLAYER_SEARCH_RADIUS = 16.0;
    private static final int WARNING_CHECK_DIAMETER = 48;
    private static final int DECREASE_WARNING_LEVEL_EVERY_INTERVAL = 12000;
    private static final int WARNING_LEVEL_INCREASE_COOLDOWN = 200;
    private int ticksSinceLastWarning;
    private int warningLevel;
    private int cooldownTicks;

    public WardenSpawnTracker(int ticksSinceLastWarning, int warningLevel, int cooldownTicks) {
        this.ticksSinceLastWarning = ticksSinceLastWarning;
        this.warningLevel = warningLevel;
        this.cooldownTicks = cooldownTicks;
    }

    public void tick() {
        if (this.ticksSinceLastWarning >= 12000) {
            this.decreaseWarningLevel();
            this.ticksSinceLastWarning = 0;
        } else {
            this.ticksSinceLastWarning++;
        }

        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
        }
    }

    public void reset() {
        this.ticksSinceLastWarning = 0;
        this.warningLevel = 0;
        this.cooldownTicks = 0;
    }

    public static OptionalInt tryWarn(ServerLevel level, BlockPos pos, ServerPlayer player) {
        if (hasNearbyWarden(level, pos)) {
            return OptionalInt.empty();
        } else {
            List<ServerPlayer> list = getNearbyPlayers(level, pos);
            if (!list.contains(player)) {
                list.add(player);
            }

            if (list.stream().anyMatch(p_248397_ -> p_248397_.getWardenSpawnTracker().map(WardenSpawnTracker::onCooldown).orElse(false))) {
                return OptionalInt.empty();
            } else {
                Optional<WardenSpawnTracker> optional = list.stream()
                    .flatMap(p_248394_ -> p_248394_.getWardenSpawnTracker().stream())
                    .max(Comparator.comparingInt(WardenSpawnTracker::getWarningLevel));
                if (optional.isPresent()) {
                    WardenSpawnTracker wardenspawntracker = optional.get();
                    wardenspawntracker.increaseWarningLevel();
                    list.forEach(p_248396_ -> p_248396_.getWardenSpawnTracker().ifPresent(p_248401_ -> p_248401_.copyData(wardenspawntracker)));
                    return OptionalInt.of(wardenspawntracker.warningLevel);
                } else {
                    return OptionalInt.empty();
                }
            }
        }
    }

    private boolean onCooldown() {
        return this.cooldownTicks > 0;
    }

    private static boolean hasNearbyWarden(ServerLevel level, BlockPos pos) {
        AABB aabb = AABB.ofSize(Vec3.atCenterOf(pos), 48.0, 48.0, 48.0);
        return !level.getEntitiesOfClass(Warden.class, aabb).isEmpty();
    }

    private static List<ServerPlayer> getNearbyPlayers(ServerLevel level, BlockPos pos) {
        Vec3 vec3 = Vec3.atCenterOf(pos);
        Predicate<ServerPlayer> predicate = p_352830_ -> p_352830_.position().closerThan(vec3, 16.0);
        return level.getPlayers(predicate.and(LivingEntity::isAlive).and(EntitySelector.NO_SPECTATORS));
    }

    private void increaseWarningLevel() {
        if (!this.onCooldown()) {
            this.ticksSinceLastWarning = 0;
            this.cooldownTicks = 200;
            this.setWarningLevel(this.getWarningLevel() + 1);
        }
    }

    private void decreaseWarningLevel() {
        this.setWarningLevel(this.getWarningLevel() - 1);
    }

    public void setWarningLevel(int warningLevel) {
        this.warningLevel = Mth.clamp(warningLevel, 0, 4);
    }

    public int getWarningLevel() {
        return this.warningLevel;
    }

    private void copyData(WardenSpawnTracker other) {
        this.warningLevel = other.warningLevel;
        this.cooldownTicks = other.cooldownTicks;
        this.ticksSinceLastWarning = other.ticksSinceLastWarning;
    }
}
