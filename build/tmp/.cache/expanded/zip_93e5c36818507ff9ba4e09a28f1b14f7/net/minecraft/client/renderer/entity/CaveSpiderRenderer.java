package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.CaveSpider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CaveSpiderRenderer extends SpiderRenderer<CaveSpider> {
    private static final ResourceLocation CAVE_SPIDER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/spider/cave_spider.png");
    private static final float SCALE = 0.7F;

    public CaveSpiderRenderer(EntityRendererProvider.Context p_173946_) {
        super(p_173946_, ModelLayers.CAVE_SPIDER);
        this.shadowRadius *= 0.7F;
    }

    protected void scale(CaveSpider livingEntity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.7F, 0.7F, 0.7F);
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(CaveSpider entity) {
        return CAVE_SPIDER_LOCATION;
    }
}
