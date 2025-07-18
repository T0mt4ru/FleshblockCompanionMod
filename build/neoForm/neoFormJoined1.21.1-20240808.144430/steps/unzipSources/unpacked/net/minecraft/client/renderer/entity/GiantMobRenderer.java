package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.GiantZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Giant;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GiantMobRenderer extends MobRenderer<Giant, HumanoidModel<Giant>> {
    private static final ResourceLocation ZOMBIE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");
    private final float scale;

    public GiantMobRenderer(EntityRendererProvider.Context context, float scale) {
        super(context, new GiantZombieModel(context.bakeLayer(ModelLayers.GIANT)), 0.5F * scale);
        this.scale = scale;
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(
            new HumanoidArmorLayer<>(
                this,
                new GiantZombieModel(context.bakeLayer(ModelLayers.GIANT_INNER_ARMOR)),
                new GiantZombieModel(context.bakeLayer(ModelLayers.GIANT_OUTER_ARMOR)),
                context.getModelManager()
            )
        );
    }

    protected void scale(Giant livingEntity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(Giant entity) {
        return ZOMBIE_LOCATION;
    }
}
