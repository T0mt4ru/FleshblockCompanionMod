package net.minecraft.client.model.dragon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DragonHeadModel extends SkullModelBase {
    private final ModelPart head;
    private final ModelPart jaw;

    public DragonHeadModel(ModelPart root) {
        this.head = root.getChild("head");
        this.jaw = this.head.getChild("jaw");
    }

    public static LayerDefinition createHeadLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        float f = -16.0F;
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .addBox("upper_lip", -6.0F, -1.0F, -24.0F, 12, 5, 16, 176, 44)
                .addBox("upper_head", -8.0F, -8.0F, -10.0F, 16, 16, 16, 112, 30)
                .mirror(true)
                .addBox("scale", -5.0F, -12.0F, -4.0F, 2, 4, 6, 0, 0)
                .addBox("nostril", -5.0F, -3.0F, -22.0F, 2, 2, 4, 112, 0)
                .mirror(false)
                .addBox("scale", 3.0F, -12.0F, -4.0F, 2, 4, 6, 0, 0)
                .addBox("nostril", 3.0F, -3.0F, -22.0F, 2, 2, 4, 112, 0),
            PartPose.ZERO
        );
        partdefinition1.addOrReplaceChild(
            "jaw", CubeListBuilder.create().texOffs(176, 65).addBox("jaw", -6.0F, 0.0F, -16.0F, 12.0F, 4.0F, 16.0F), PartPose.offset(0.0F, 4.0F, -8.0F)
        );
        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(float mouthAnimation, float yRot, float xRot) {
        this.jaw.xRot = (float)(Math.sin((double)(mouthAnimation * (float) Math.PI * 0.2F)) + 1.0) * 0.2F;
        this.head.yRot = yRot * (float) (Math.PI / 180.0);
        this.head.xRot = xRot * (float) (Math.PI / 180.0);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();
        poseStack.translate(0.0F, -0.374375F, 0.0F);
        poseStack.scale(0.75F, 0.75F, 0.75F);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }
}
