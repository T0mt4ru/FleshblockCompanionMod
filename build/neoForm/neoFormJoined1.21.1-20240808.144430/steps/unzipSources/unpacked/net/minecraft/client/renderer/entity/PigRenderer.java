package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PigRenderer extends MobRenderer<Pig, PigModel<Pig>> {
    private static final ResourceLocation PIG_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/pig/pig.png");

    public PigRenderer(EntityRendererProvider.Context p_174340_) {
        super(p_174340_, new PigModel<>(p_174340_.bakeLayer(ModelLayers.PIG)), 0.7F);
        this.addLayer(
            new SaddleLayer<>(
                this, new PigModel<>(p_174340_.bakeLayer(ModelLayers.PIG_SADDLE)), ResourceLocation.withDefaultNamespace("textures/entity/pig/pig_saddle.png")
            )
        );
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(Pig entity) {
        return PIG_LOCATION;
    }
}
