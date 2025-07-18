package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.DolphinCarryingItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Dolphin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DolphinRenderer extends MobRenderer<Dolphin, DolphinModel<Dolphin>> {
    private static final ResourceLocation DOLPHIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/dolphin.png");

    public DolphinRenderer(EntityRendererProvider.Context p_173960_) {
        super(p_173960_, new DolphinModel<>(p_173960_.bakeLayer(ModelLayers.DOLPHIN)), 0.7F);
        this.addLayer(new DolphinCarryingItemLayer(this, p_173960_.getItemInHandRenderer()));
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(Dolphin entity) {
        return DOLPHIN_LOCATION;
    }
}
