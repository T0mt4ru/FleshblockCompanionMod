package net.minecraft.world.entity.boss.enderdragon.phases;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class DragonChargePlayerPhase extends AbstractDragonPhaseInstance {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHARGE_RECOVERY_TIME = 10;
    @Nullable
    private Vec3 targetLocation;
    private int timeSinceCharge;

    public DragonChargePlayerPhase(EnderDragon dragon) {
        super(dragon);
    }

    @Override
    public void doServerTick() {
        if (this.targetLocation == null) {
            LOGGER.warn("Aborting charge player as no target was set.");
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        } else if (this.timeSinceCharge > 0 && this.timeSinceCharge++ >= 10) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        } else {
            double d0 = this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
            if (d0 < 100.0 || d0 > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
                this.timeSinceCharge++;
            }
        }
    }

    @Override
    public void begin() {
        this.targetLocation = null;
        this.timeSinceCharge = 0;
    }

    public void setTarget(Vec3 targetLocation) {
        this.targetLocation = targetLocation;
    }

    @Override
    public float getFlySpeed() {
        return 3.0F;
    }

    @Nullable
    @Override
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    @Override
    public EnderDragonPhase<DragonChargePlayerPhase> getPhase() {
        return EnderDragonPhase.CHARGING_PLAYER;
    }
}
