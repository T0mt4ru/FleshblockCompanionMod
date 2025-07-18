package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HoglinRenderer extends MobRenderer<Hoglin, HoglinModel<Hoglin>> {
    private static final ResourceLocation HOGLIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/hoglin/hoglin.png");

    public HoglinRenderer(EntityRendererProvider.Context p_174165_) {
        super(p_174165_, new HoglinModel<>(p_174165_.bakeLayer(ModelLayers.HOGLIN)), 0.7F);
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(Hoglin entity) {
        return HOGLIN_LOCATION;
    }

    protected boolean isShaking(Hoglin entity) {
        return super.isShaking(entity) || entity.isConverting();
    }
}
