package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.allay.Allay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AllayModel extends HierarchicalModel<Allay> implements ArmedModel {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_wing;
    private final ModelPart left_wing;
    private static final float FLYING_ANIMATION_X_ROT = (float) (Math.PI / 4);
    private static final float MAX_HAND_HOLDING_ITEM_X_ROT_RAD = -1.134464F;
    private static final float MIN_HAND_HOLDING_ITEM_X_ROT_RAD = (float) (-Math.PI / 3);

    public AllayModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root.getChild("root");
        this.head = this.root.getChild("head");
        this.body = this.root.getChild("body");
        this.right_arm = this.body.getChild("right_arm");
        this.left_arm = this.body.getChild("left_arm");
        this.right_wing = this.body.getChild("right_wing");
        this.left_wing = this.body.getChild("left_wing");
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 23.5F, 0.0F));
        partdefinition1.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, -3.99F, 0.0F)
        );
        PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(0, 10)
                .addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16)
                .addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(-0.2F)),
            PartPose.offset(0.0F, -4.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "right_arm",
            CubeListBuilder.create().texOffs(23, 0).addBox(-0.75F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)),
            PartPose.offset(-1.75F, 0.5F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "left_arm",
            CubeListBuilder.create().texOffs(23, 6).addBox(-0.25F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)),
            PartPose.offset(1.75F, 0.5F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "right_wing",
            CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-0.5F, 0.0F, 0.6F)
        );
        partdefinition2.addOrReplaceChild(
            "left_wing",
            CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.5F, 0.0F, 0.6F)
        );
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    /**
     * Sets this entity's model rotation angles
     */
    public void setupAnim(Allay entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float f = ageInTicks * 20.0F * (float) (Math.PI / 180.0) + limbSwing;
        float f1 = Mth.cos(f) * (float) Math.PI * 0.15F + limbSwingAmount;
        float f2 = ageInTicks - (float)entity.tickCount;
        float f3 = ageInTicks * 9.0F * (float) (Math.PI / 180.0);
        float f4 = Math.min(limbSwingAmount / 0.3F, 1.0F);
        float f5 = 1.0F - f4;
        float f6 = entity.getHoldingItemAnimationProgress(f2);
        if (entity.isDancing()) {
            float f7 = ageInTicks * 8.0F * (float) (Math.PI / 180.0) + limbSwingAmount;
            float f8 = Mth.cos(f7) * 16.0F * (float) (Math.PI / 180.0);
            float f9 = entity.getSpinningProgress(f2);
            float f10 = Mth.cos(f7) * 14.0F * (float) (Math.PI / 180.0);
            float f11 = Mth.cos(f7) * 30.0F * (float) (Math.PI / 180.0);
            this.root.yRot = entity.isSpinning() ? (float) (Math.PI * 4) * f9 : this.root.yRot;
            this.root.zRot = f8 * (1.0F - f9);
            this.head.yRot = f11 * (1.0F - f9);
            this.head.zRot = f10 * (1.0F - f9);
        } else {
            this.head.xRot = headPitch * (float) (Math.PI / 180.0);
            this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
        }

        this.right_wing.xRot = 0.43633232F * (1.0F - f4);
        this.right_wing.yRot = (float) (-Math.PI / 4) + f1;
        this.left_wing.xRot = 0.43633232F * (1.0F - f4);
        this.left_wing.yRot = (float) (Math.PI / 4) - f1;
        this.body.xRot = f4 * (float) (Math.PI / 4);
        float f12 = f6 * Mth.lerp(f4, (float) (-Math.PI / 3), -1.134464F);
        this.root.y = this.root.y + (float)Math.cos((double)f3) * 0.25F * f5;
        this.right_arm.xRot = f12;
        this.left_arm.xRot = f12;
        float f13 = f5 * (1.0F - f6);
        float f14 = 0.43633232F - Mth.cos(f3 + (float) (Math.PI * 3.0 / 2.0)) * (float) Math.PI * 0.075F * f13;
        this.left_arm.zRot = -f14;
        this.right_arm.zRot = f14;
        this.right_arm.yRot = 0.27925268F * f6;
        this.left_arm.yRot = -0.27925268F * f6;
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        float f = 1.0F;
        float f1 = 3.0F;
        this.root.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        poseStack.translate(0.0F, 0.0625F, 0.1875F);
        poseStack.mulPose(Axis.XP.rotation(this.right_arm.xRot));
        poseStack.scale(0.7F, 0.7F, 0.7F);
        poseStack.translate(0.0625F, 0.0F, 0.0F);
    }
}
