package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArmorStandModel extends ArmorStandArmorModel {
    private static final String RIGHT_BODY_STICK = "right_body_stick";
    private static final String LEFT_BODY_STICK = "left_body_stick";
    private static final String SHOULDER_STICK = "shoulder_stick";
    private static final String BASE_PLATE = "base_plate";
    private final ModelPart rightBodyStick;
    private final ModelPart leftBodyStick;
    private final ModelPart shoulderStick;
    private final ModelPart basePlate;

    public ArmorStandModel(ModelPart root) {
        super(root);
        this.rightBodyStick = root.getChild("right_body_stick");
        this.leftBodyStick = root.getChild("left_body_stick");
        this.shoulderStick = root.getChild("shoulder_stick");
        this.basePlate = root.getChild("base_plate");
        this.hat.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.offset(0.0F, 1.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 26).addBox(-6.0F, 0.0F, -1.5F, 12.0F, 3.0F, 3.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild(
            "right_arm", CubeListBuilder.create().texOffs(24, 0).addBox(-2.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_arm", CubeListBuilder.create().texOffs(32, 16).mirror().addBox(0.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_leg", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F), PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_leg", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F), PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_body_stick", CubeListBuilder.create().texOffs(16, 0).addBox(-3.0F, 3.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.ZERO
        );
        partdefinition.addOrReplaceChild("left_body_stick", CubeListBuilder.create().texOffs(48, 16).addBox(1.0F, 3.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("shoulder_stick", CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, 10.0F, -1.0F, 8.0F, 2.0F, 2.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild(
            "base_plate", CubeListBuilder.create().texOffs(0, 32).addBox(-6.0F, 11.0F, -6.0F, 12.0F, 1.0F, 12.0F), PartPose.offset(0.0F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void prepareMobModel(ArmorStand entity, float limbSwing, float limbSwingAmount, float partialTick) {
        this.basePlate.xRot = 0.0F;
        this.basePlate.yRot = (float) (Math.PI / 180.0) * -Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        this.basePlate.zRot = 0.0F;
    }

    /**
     * Sets this entity's model rotation angles
     */
    @Override
    public void setupAnim(ArmorStand entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.leftArm.visible = entity.isShowArms();
        this.rightArm.visible = entity.isShowArms();
        this.basePlate.visible = !entity.isNoBasePlate();
        this.rightBodyStick.xRot = (float) (Math.PI / 180.0) * entity.getBodyPose().getX();
        this.rightBodyStick.yRot = (float) (Math.PI / 180.0) * entity.getBodyPose().getY();
        this.rightBodyStick.zRot = (float) (Math.PI / 180.0) * entity.getBodyPose().getZ();
        this.leftBodyStick.xRot = (float) (Math.PI / 180.0) * entity.getBodyPose().getX();
        this.leftBodyStick.yRot = (float) (Math.PI / 180.0) * entity.getBodyPose().getY();
        this.leftBodyStick.zRot = (float) (Math.PI / 180.0) * entity.getBodyPose().getZ();
        this.shoulderStick.xRot = (float) (Math.PI / 180.0) * entity.getBodyPose().getX();
        this.shoulderStick.yRot = (float) (Math.PI / 180.0) * entity.getBodyPose().getY();
        this.shoulderStick.zRot = (float) (Math.PI / 180.0) * entity.getBodyPose().getZ();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.rightBodyStick, this.leftBodyStick, this.shoulderStick, this.basePlate));
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        ModelPart modelpart = this.getArm(side);
        boolean flag = modelpart.visible;
        modelpart.visible = true;
        super.translateToHand(side, poseStack);
        modelpart.visible = flag;
    }
}
