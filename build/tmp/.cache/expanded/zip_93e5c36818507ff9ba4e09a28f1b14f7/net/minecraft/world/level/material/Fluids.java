package net.minecraft.world.level.material;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class Fluids {
    public static final Fluid EMPTY = register("empty", new EmptyFluid());
    public static final FlowingFluid FLOWING_WATER = register("flowing_water", new WaterFluid.Flowing());
    public static final FlowingFluid WATER = register("water", new WaterFluid.Source());
    public static final FlowingFluid FLOWING_LAVA = register("flowing_lava", new LavaFluid.Flowing());
    public static final FlowingFluid LAVA = register("lava", new LavaFluid.Source());

    private static <T extends Fluid> T register(String key, T fluid) {
        return Registry.register(BuiltInRegistries.FLUID, key, fluid);
    }

    static {
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            for (FluidState fluidstate : fluid.getStateDefinition().getPossibleStates()) {
                Fluid.FLUID_STATE_REGISTRY.add(fluidstate);
            }
        }
    }
}
