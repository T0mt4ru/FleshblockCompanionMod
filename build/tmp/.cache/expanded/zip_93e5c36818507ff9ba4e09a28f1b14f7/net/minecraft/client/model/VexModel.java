package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VexModel extends HierarchicalModel<Vex> implements ArmedModel {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart head;

    public VexModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightWing = this.body.getChild("right_wing");
        this.leftWing = this.body.getChild("left_wing");
        this.head = this.root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, -2.5F, 0.0F));
        partdefinition1.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 20.0F, 0.0F)
        );
        PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(0, 10)
                .addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16)
                .addBox(-1.5F, 1.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(-0.2F)),
            PartPose.offset(0.0F, 20.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "right_arm",
            CubeListBuilder.create().texOffs(23, 0).addBox(-1.25F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F)),
            PartPose.offset(-1.75F, 0.25F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "left_arm",
            CubeListBuilder.create().texOffs(23, 6).addBox(-0.75F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F)),
            PartPose.offset(1.75F, 0.25F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "left_wing",
            CubeListBuilder.create().texOffs(16, 14).mirror().addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false),
            PartPose.offset(0.5F, 1.0F, 1.0F)
        );
        partdefinition2.addOrReplaceChild(
            "right_wing",
            CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-0.5F, 1.0F, 1.0F)
        );
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    /**
     * Sets this entity's model rotation angles
     */
    public void setupAnim(Vex entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
        this.head.xRot = headPitch * (float) (Math.PI / 180.0);
        float f = Mth.cos(ageInTicks * 5.5F * (float) (Math.PI / 180.0)) * 0.1F;
        this.rightArm.zRot = (float) (Math.PI / 5) + f;
        this.leftArm.zRot = -((float) (Math.PI / 5) + f);
        if (entity.isCharging()) {
            this.body.xRot = 0.0F;
            this.setArmsCharging(entity.getMainHandItem(), entity.getOffhandItem(), f);
        } else {
            this.body.xRot = (float) (Math.PI / 20);
        }

        this.leftWing.yRot = 1.0995574F + Mth.cos(ageInTicks * 45.836624F * (float) (Math.PI / 180.0)) * (float) (Math.PI / 180.0) * 16.2F;
        this.rightWing.yRot = -this.leftWing.yRot;
        this.leftWing.xRot = 0.47123888F;
        this.leftWing.zRot = -0.47123888F;
        this.rightWing.xRot = 0.47123888F;
        this.rightWing.zRot = 0.47123888F;
    }

    private void setArmsCharging(ItemStack rightHandItem, ItemStack leftHandItem, float p_265125_) {
        if (rightHandItem.isEmpty() && leftHandItem.isEmpty()) {
            this.rightArm.xRot = -1.2217305F;
            this.rightArm.yRot = (float) (Math.PI / 12);
            this.rightArm.zRot = -0.47123888F - p_265125_;
            this.leftArm.xRot = -1.2217305F;
            this.leftArm.yRot = (float) (-Math.PI / 12);
            this.leftArm.zRot = 0.47123888F + p_265125_;
        } else {
            if (!rightHandItem.isEmpty()) {
                this.rightArm.xRot = (float) (Math.PI * 7.0 / 6.0);
                this.rightArm.yRot = (float) (Math.PI / 12);
                this.rightArm.zRot = -0.47123888F - p_265125_;
            }

            if (!leftHandItem.isEmpty()) {
                this.leftArm.xRot = (float) (Math.PI * 7.0 / 6.0);
                this.leftArm.yRot = (float) (-Math.PI / 12);
                this.leftArm.zRot = 0.47123888F + p_265125_;
            }
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        boolean flag = side == HumanoidArm.RIGHT;
        ModelPart modelpart = flag ? this.rightArm : this.leftArm;
        this.root.translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        modelpart.translateAndRotate(poseStack);
        poseStack.scale(0.55F, 0.55F, 0.55F);
        this.offsetStackPosition(poseStack, flag);
    }

    private void offsetStackPosition(PoseStack poseStack, boolean rightSide) {
        if (rightSide) {
            poseStack.translate(0.046875, -0.15625, 0.078125);
        } else {
            poseStack.translate(-0.046875, -0.15625, 0.078125);
        }
    }
}
