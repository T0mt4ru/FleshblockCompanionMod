package net.minecraft.client.player;

import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Input {
    public float leftImpulse;
    public float forwardImpulse;
    public boolean up;
    public boolean down;
    public boolean left;
    public boolean right;
    public boolean jumping;
    public boolean shiftKeyDown;

    public void tick(boolean isSneaking, float sneakingSpeedMultiplier) {
    }

    public Vec2 getMoveVector() {
        return new Vec2(this.leftImpulse, this.forwardImpulse);
    }

    public boolean hasForwardImpulse() {
        return this.forwardImpulse > 1.0E-5F;
    }
}
