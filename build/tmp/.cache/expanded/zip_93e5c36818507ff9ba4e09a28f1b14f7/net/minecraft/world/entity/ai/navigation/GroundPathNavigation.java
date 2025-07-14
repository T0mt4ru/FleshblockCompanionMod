package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class GroundPathNavigation extends PathNavigation {
    private boolean avoidSun;

    public GroundPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.mob.onGround() || this.mob.isInLiquid() || this.mob.isPassenger();
    }

    @Override
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), (double)this.getSurfaceY(), this.mob.getZ());
    }

    /**
     * Returns path to given BlockPos
     */
    @Override
    public Path createPath(BlockPos pos, int accuracy) {
        LevelChunk levelchunk = this.level
            .getChunkSource()
            .getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        if (levelchunk == null) {
            return null;
        } else {
            if (levelchunk.getBlockState(pos).isAir()) {
                BlockPos blockpos = pos.below();

                while (blockpos.getY() > this.level.getMinBuildHeight() && levelchunk.getBlockState(blockpos).isAir()) {
                    blockpos = blockpos.below();
                }

                if (blockpos.getY() > this.level.getMinBuildHeight()) {
                    return super.createPath(blockpos.above(), accuracy);
                }

                while (blockpos.getY() < this.level.getMaxBuildHeight() && levelchunk.getBlockState(blockpos).isAir()) {
                    blockpos = blockpos.above();
                }

                pos = blockpos;
            }

            if (!levelchunk.getBlockState(pos).isSolid()) {
                return super.createPath(pos, accuracy);
            } else {
                BlockPos blockpos1 = pos.above();

                while (blockpos1.getY() < this.level.getMaxBuildHeight() && levelchunk.getBlockState(blockpos1).isSolid()) {
                    blockpos1 = blockpos1.above();
                }

                return super.createPath(blockpos1, accuracy);
            }
        }
    }

    /**
     * Returns a path to the given entity or null
     */
    @Override
    public Path createPath(Entity entity, int accuracy) {
        return this.createPath(entity.blockPosition(), accuracy);
    }

    private int getSurfaceY() {
        if (this.mob.isInWater() && this.canFloat()) {
            int i = this.mob.getBlockY();
            BlockState blockstate = this.level.getBlockState(BlockPos.containing(this.mob.getX(), (double)i, this.mob.getZ()));
            int j = 0;

            while (blockstate.is(Blocks.WATER)) {
                blockstate = this.level.getBlockState(BlockPos.containing(this.mob.getX(), (double)(++i), this.mob.getZ()));
                if (++j > 16) {
                    return this.mob.getBlockY();
                }
            }

            return i;
        } else {
            return Mth.floor(this.mob.getY() + 0.5);
        }
    }

    @Override
    protected void trimPath() {
        super.trimPath();
        if (this.avoidSun) {
            if (this.level.canSeeSky(BlockPos.containing(this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ()))) {
                return;
            }

            for (int i = 0; i < this.path.getNodeCount(); i++) {
                Node node = this.path.getNode(i);
                if (this.level.canSeeSky(new BlockPos(node.x, node.y, node.z))) {
                    this.path.truncateNodes(i);
                    return;
                }
            }
        }
    }

    protected boolean hasValidPathType(PathType pathType) {
        if (pathType == PathType.WATER) {
            return false;
        } else {
            return pathType == PathType.LAVA ? false : pathType != PathType.OPEN;
        }
    }

    public void setCanOpenDoors(boolean canOpenDoors) {
        this.nodeEvaluator.setCanOpenDoors(canOpenDoors);
    }

    public boolean canPassDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setCanPassDoors(boolean canPassDoors) {
        this.nodeEvaluator.setCanPassDoors(canPassDoors);
    }

    public boolean canOpenDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setAvoidSun(boolean avoidSun) {
        this.avoidSun = avoidSun;
    }

    public void setCanWalkOverFences(boolean canWalkOverFences) {
        this.nodeEvaluator.setCanWalkOverFences(canWalkOverFences);
    }
}
