package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class DragonLandingApproachPhase extends AbstractDragonPhaseInstance {
    private static final TargetingConditions NEAR_EGG_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight();
    @Nullable
    private Path currentPath;
    @Nullable
    private Vec3 targetLocation;

    public DragonLandingApproachPhase(EnderDragon dragon) {
        super(dragon);
    }

    @Override
    public EnderDragonPhase<DragonLandingApproachPhase> getPhase() {
        return EnderDragonPhase.LANDING_APPROACH;
    }

    @Override
    public void begin() {
        this.currentPath = null;
        this.targetLocation = null;
    }

    @Override
    public void doServerTick() {
        double d0 = this.targetLocation == null ? 0.0 : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (d0 < 100.0 || d0 > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            this.findNewTarget();
        }
    }

    @Nullable
    @Override
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    private void findNewTarget() {
        if (this.currentPath == null || this.currentPath.isDone()) {
            int i = this.dragon.findClosestNode();
            BlockPos blockpos = this.dragon
                .level()
                .getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.dragon.getFightOrigin()));
            Player player = this.dragon
                .level()
                .getNearestPlayer(NEAR_EGG_TARGETING, this.dragon, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
            int j;
            if (player != null) {
                Vec3 vec3 = new Vec3(player.getX(), 0.0, player.getZ()).normalize();
                j = this.dragon.findClosestNode(-vec3.x * 40.0, 105.0, -vec3.z * 40.0);
            } else {
                j = this.dragon.findClosestNode(40.0, (double)blockpos.getY(), 0.0);
            }

            Node node = new Node(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            this.currentPath = this.dragon.findPath(i, j, node);
            if (this.currentPath != null) {
                this.currentPath.advance();
            }
        }

        this.navigateToNextPathNode();
        if (this.currentPath != null && this.currentPath.isDone()) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING);
        }
    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null && !this.currentPath.isDone()) {
            Vec3i vec3i = this.currentPath.getNextNodePos();
            this.currentPath.advance();
            double d0 = (double)vec3i.getX();
            double d1 = (double)vec3i.getZ();

            double d2;
            do {
                d2 = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
            } while (d2 < (double)vec3i.getY());

            this.targetLocation = new Vec3(d0, d2, d1);
        }
    }
}
