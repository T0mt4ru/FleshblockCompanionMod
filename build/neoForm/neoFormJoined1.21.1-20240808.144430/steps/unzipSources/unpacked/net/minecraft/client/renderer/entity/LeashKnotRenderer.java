package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LeashKnotRenderer extends EntityRenderer<LeashFenceKnotEntity> {
    private static final ResourceLocation KNOT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/lead_knot.png");
    private final LeashKnotModel<LeashFenceKnotEntity> model;

    public LeashKnotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new LeashKnotModel<>(context.bakeLayer(ModelLayers.LEASH_KNOT));
    }

    public void render(LeashFenceKnotEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        VertexConsumer vertexconsumer = buffer.getBuffer(this.model.renderType(KNOT_LOCATION));
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(LeashFenceKnotEntity entity) {
        return KNOT_LOCATION;
    }
}
