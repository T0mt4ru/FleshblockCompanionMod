package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class CubeVoxelShape extends VoxelShape {
    protected CubeVoxelShape(DiscreteVoxelShape shape) {
        super(shape);
    }

    @Override
    public DoubleList getCoords(Direction.Axis axis) {
        return new CubePointRange(this.shape.getSize(axis));
    }

    @Override
    protected int findIndex(Direction.Axis axis, double position) {
        int i = this.shape.getSize(axis);
        return Mth.floor(Mth.clamp(position * (double)i, -1.0, (double)i));
    }
}
