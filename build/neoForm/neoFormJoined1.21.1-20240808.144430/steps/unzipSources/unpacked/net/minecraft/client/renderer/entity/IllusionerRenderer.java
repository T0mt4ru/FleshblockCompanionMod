package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IllusionerRenderer extends IllagerRenderer<Illusioner> {
    private static final ResourceLocation ILLUSIONER = ResourceLocation.withDefaultNamespace("textures/entity/illager/illusioner.png");

    public IllusionerRenderer(EntityRendererProvider.Context p_174186_) {
        super(p_174186_, new IllagerModel<>(p_174186_.bakeLayer(ModelLayers.ILLUSIONER)), 0.5F);
        this.addLayer(
            new ItemInHandLayer<Illusioner, IllagerModel<Illusioner>>(this, p_174186_.getItemInHandRenderer()) {
                public void render(
                    PoseStack p_114989_,
                    MultiBufferSource p_114990_,
                    int p_114991_,
                    Illusioner p_114992_,
                    float p_114993_,
                    float p_114994_,
                    float p_114995_,
                    float p_114996_,
                    float p_114997_,
                    float p_114998_
                ) {
                    if (p_114992_.isCastingSpell() || p_114992_.isAggressive()) {
                        super.render(p_114989_, p_114990_, p_114991_, p_114992_, p_114993_, p_114994_, p_114995_, p_114996_, p_114997_, p_114998_);
                    }
                }
            }
        );
        this.model.getHat().visible = true;
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(Illusioner entity) {
        return ILLUSIONER;
    }

    public void render(Illusioner entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.isInvisible()) {
            Vec3[] avec3 = entity.getIllusionOffsets(partialTicks);
            float f = this.getBob(entity, partialTicks);

            for (int i = 0; i < avec3.length; i++) {
                poseStack.pushPose();
                poseStack.translate(
                    avec3[i].x + (double)Mth.cos((float)i + f * 0.5F) * 0.025,
                    avec3[i].y + (double)Mth.cos((float)i + f * 0.75F) * 0.0125,
                    avec3[i].z + (double)Mth.cos((float)i + f * 0.7F) * 0.025
                );
                super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
                poseStack.popPose();
            }
        } else {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }
    }

    protected boolean isBodyVisible(Illusioner livingEntity) {
        return true;
    }
}
