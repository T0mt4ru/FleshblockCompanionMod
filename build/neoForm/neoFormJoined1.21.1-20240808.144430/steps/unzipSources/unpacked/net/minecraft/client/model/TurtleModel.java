package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Turtle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TurtleModel<T extends Turtle> extends QuadrupedModel<T> {
    private static final String EGG_BELLY = "egg_belly";
    private final ModelPart eggBelly;

    public TurtleModel(ModelPart root) {
        super(root, true, 120.0F, 0.0F, 9.0F, 6.0F, 120);
        this.eggBelly = root.getChild("egg_belly");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(3, 0).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 5.0F, 6.0F), PartPose.offset(0.0F, 19.0F, -10.0F)
        );
        partdefinition.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(7, 37)
                .addBox("shell", -9.5F, 3.0F, -10.0F, 19.0F, 20.0F, 6.0F)
                .texOffs(31, 1)
                .addBox("belly", -5.5F, 3.0F, -13.0F, 11.0F, 18.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, 11.0F, -10.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "egg_belly",
            CubeListBuilder.create().texOffs(70, 33).addBox(-4.5F, 3.0F, -14.0F, 9.0F, 18.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 11.0F, -10.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        int i = 1;
        partdefinition.addOrReplaceChild(
            "right_hind_leg", CubeListBuilder.create().texOffs(1, 23).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F), PartPose.offset(-3.5F, 22.0F, 11.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_hind_leg", CubeListBuilder.create().texOffs(1, 12).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F), PartPose.offset(3.5F, 22.0F, 11.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_front_leg", CubeListBuilder.create().texOffs(27, 30).addBox(-13.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F), PartPose.offset(-5.0F, 21.0F, -4.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_front_leg", CubeListBuilder.create().texOffs(27, 24).addBox(0.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F), PartPose.offset(5.0F, 21.0F, -4.0F)
        );
        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.eggBelly));
    }

    /**
     * Sets this entity's model rotation angles
     */
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F * 0.6F) * 0.5F * limbSwingAmount;
        this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F * 0.6F + (float) Math.PI) * 0.5F * limbSwingAmount;
        this.rightFrontLeg.zRot = Mth.cos(limbSwing * 0.6662F * 0.6F + (float) Math.PI) * 0.5F * limbSwingAmount;
        this.leftFrontLeg.zRot = Mth.cos(limbSwing * 0.6662F * 0.6F) * 0.5F * limbSwingAmount;
        this.rightFrontLeg.xRot = 0.0F;
        this.leftFrontLeg.xRot = 0.0F;
        this.rightFrontLeg.yRot = 0.0F;
        this.leftFrontLeg.yRot = 0.0F;
        this.rightHindLeg.yRot = 0.0F;
        this.leftHindLeg.yRot = 0.0F;
        if (!entity.isInWater() && entity.onGround()) {
            float f = entity.isLayingEgg() ? 4.0F : 1.0F;
            float f1 = entity.isLayingEgg() ? 2.0F : 1.0F;
            float f2 = 5.0F;
            this.rightFrontLeg.yRot = Mth.cos(f * limbSwing * 5.0F + (float) Math.PI) * 8.0F * limbSwingAmount * f1;
            this.rightFrontLeg.zRot = 0.0F;
            this.leftFrontLeg.yRot = Mth.cos(f * limbSwing * 5.0F) * 8.0F * limbSwingAmount * f1;
            this.leftFrontLeg.zRot = 0.0F;
            this.rightHindLeg.yRot = Mth.cos(limbSwing * 5.0F + (float) Math.PI) * 3.0F * limbSwingAmount;
            this.rightHindLeg.xRot = 0.0F;
            this.leftHindLeg.yRot = Mth.cos(limbSwing * 5.0F) * 3.0F * limbSwingAmount;
            this.leftHindLeg.xRot = 0.0F;
        }

        this.eggBelly.visible = !this.young && entity.hasEgg();
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        boolean flag = this.eggBelly.visible;
        if (flag) {
            poseStack.pushPose();
            poseStack.translate(0.0F, -0.08F, 0.0F);
        }

        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
        if (flag) {
            poseStack.popPose();
        }
    }
}
