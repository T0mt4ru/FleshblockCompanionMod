package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LerpingModel;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class AxolotlModel<T extends Axolotl & LerpingModel> extends AgeableListModel<T> {
    public static final float SWIMMING_LEG_XROT = 1.8849558F;
    private final ModelPart tail;
    private final ModelPart leftHindLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart topGills;
    private final ModelPart leftGills;
    private final ModelPart rightGills;

    public AxolotlModel(ModelPart root) {
        super(true, 8.0F, 3.35F);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.rightHindLeg = this.body.getChild("right_hind_leg");
        this.leftHindLeg = this.body.getChild("left_hind_leg");
        this.rightFrontLeg = this.body.getChild("right_front_leg");
        this.leftFrontLeg = this.body.getChild("left_front_leg");
        this.tail = this.body.getChild("tail");
        this.topGills = this.head.getChild("top_gills");
        this.leftGills = this.head.getChild("left_gills");
        this.rightGills = this.head.getChild("right_gills");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 11).addBox(-4.0F, -2.0F, -9.0F, 8.0F, 4.0F, 10.0F).texOffs(2, 17).addBox(0.0F, -3.0F, -8.0F, 0.0F, 5.0F, 9.0F),
            PartPose.offset(0.0F, 20.0F, 5.0F)
        );
        CubeDeformation cubedeformation = new CubeDeformation(0.001F);
        PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 1).addBox(-4.0F, -3.0F, -5.0F, 8.0F, 5.0F, 5.0F, cubedeformation), PartPose.offset(0.0F, 0.0F, -9.0F)
        );
        CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(3, 37).addBox(-4.0F, -3.0F, 0.0F, 8.0F, 3.0F, 0.0F, cubedeformation);
        CubeListBuilder cubelistbuilder1 = CubeListBuilder.create().texOffs(0, 40).addBox(-3.0F, -5.0F, 0.0F, 3.0F, 7.0F, 0.0F, cubedeformation);
        CubeListBuilder cubelistbuilder2 = CubeListBuilder.create().texOffs(11, 40).addBox(0.0F, -5.0F, 0.0F, 3.0F, 7.0F, 0.0F, cubedeformation);
        partdefinition2.addOrReplaceChild("top_gills", cubelistbuilder, PartPose.offset(0.0F, -3.0F, -1.0F));
        partdefinition2.addOrReplaceChild("left_gills", cubelistbuilder1, PartPose.offset(-4.0F, 0.0F, -1.0F));
        partdefinition2.addOrReplaceChild("right_gills", cubelistbuilder2, PartPose.offset(4.0F, 0.0F, -1.0F));
        CubeListBuilder cubelistbuilder3 = CubeListBuilder.create().texOffs(2, 13).addBox(-1.0F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, cubedeformation);
        CubeListBuilder cubelistbuilder4 = CubeListBuilder.create().texOffs(2, 13).addBox(-2.0F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, cubedeformation);
        partdefinition1.addOrReplaceChild("right_hind_leg", cubelistbuilder4, PartPose.offset(-3.5F, 1.0F, -1.0F));
        partdefinition1.addOrReplaceChild("left_hind_leg", cubelistbuilder3, PartPose.offset(3.5F, 1.0F, -1.0F));
        partdefinition1.addOrReplaceChild("right_front_leg", cubelistbuilder4, PartPose.offset(-3.5F, 1.0F, -8.0F));
        partdefinition1.addOrReplaceChild("left_front_leg", cubelistbuilder3, PartPose.offset(3.5F, 1.0F, -8.0F));
        partdefinition1.addOrReplaceChild(
            "tail", CubeListBuilder.create().texOffs(2, 19).addBox(0.0F, -3.0F, 0.0F, 0.0F, 5.0F, 12.0F), PartPose.offset(0.0F, 0.0F, 1.0F)
        );
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body);
    }

    /**
     * Sets this entity's model rotation angles
     */
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.setupInitialAnimationValues(entity, netHeadYaw, headPitch);
        if (entity.isPlayingDead()) {
            this.setupPlayDeadAnimation(netHeadYaw);
            this.saveAnimationValues(entity);
        } else {
            boolean flag = limbSwingAmount > 1.0E-5F || entity.getXRot() != entity.xRotO || entity.getYRot() != entity.yRotO;
            if (entity.isInWaterOrBubble()) {
                if (flag) {
                    this.setupSwimmingAnimation(ageInTicks, headPitch);
                } else {
                    this.setupWaterHoveringAnimation(ageInTicks);
                }

                this.saveAnimationValues(entity);
            } else {
                if (entity.onGround()) {
                    if (flag) {
                        this.setupGroundCrawlingAnimation(ageInTicks, netHeadYaw);
                    } else {
                        this.setupLayStillOnGroundAnimation(ageInTicks, netHeadYaw);
                    }
                }

                this.saveAnimationValues(entity);
            }
        }
    }

    private void saveAnimationValues(T axolotl) {
        Map<String, Vector3f> map = axolotl.getModelRotationValues();
        map.put("body", this.getRotationVector(this.body));
        map.put("head", this.getRotationVector(this.head));
        map.put("right_hind_leg", this.getRotationVector(this.rightHindLeg));
        map.put("left_hind_leg", this.getRotationVector(this.leftHindLeg));
        map.put("right_front_leg", this.getRotationVector(this.rightFrontLeg));
        map.put("left_front_leg", this.getRotationVector(this.leftFrontLeg));
        map.put("tail", this.getRotationVector(this.tail));
        map.put("top_gills", this.getRotationVector(this.topGills));
        map.put("left_gills", this.getRotationVector(this.leftGills));
        map.put("right_gills", this.getRotationVector(this.rightGills));
    }

    private Vector3f getRotationVector(ModelPart part) {
        return new Vector3f(part.xRot, part.yRot, part.zRot);
    }

    private void setRotationFromVector(ModelPart part, Vector3f rotation) {
        part.setRotation(rotation.x(), rotation.y(), rotation.z());
    }

    private void setupInitialAnimationValues(T axolotl, float netHeadYaw, float headPitch) {
        this.body.x = 0.0F;
        this.head.y = 0.0F;
        this.body.y = 20.0F;
        Map<String, Vector3f> map = axolotl.getModelRotationValues();
        if (map.isEmpty()) {
            this.body.setRotation(headPitch * (float) (Math.PI / 180.0), netHeadYaw * (float) (Math.PI / 180.0), 0.0F);
            this.head.setRotation(0.0F, 0.0F, 0.0F);
            this.leftHindLeg.setRotation(0.0F, 0.0F, 0.0F);
            this.rightHindLeg.setRotation(0.0F, 0.0F, 0.0F);
            this.leftFrontLeg.setRotation(0.0F, 0.0F, 0.0F);
            this.rightFrontLeg.setRotation(0.0F, 0.0F, 0.0F);
            this.leftGills.setRotation(0.0F, 0.0F, 0.0F);
            this.rightGills.setRotation(0.0F, 0.0F, 0.0F);
            this.topGills.setRotation(0.0F, 0.0F, 0.0F);
            this.tail.setRotation(0.0F, 0.0F, 0.0F);
        } else {
            this.setRotationFromVector(this.body, map.get("body"));
            this.setRotationFromVector(this.head, map.get("head"));
            this.setRotationFromVector(this.leftHindLeg, map.get("left_hind_leg"));
            this.setRotationFromVector(this.rightHindLeg, map.get("right_hind_leg"));
            this.setRotationFromVector(this.leftFrontLeg, map.get("left_front_leg"));
            this.setRotationFromVector(this.rightFrontLeg, map.get("right_front_leg"));
            this.setRotationFromVector(this.leftGills, map.get("left_gills"));
            this.setRotationFromVector(this.rightGills, map.get("right_gills"));
            this.setRotationFromVector(this.topGills, map.get("top_gills"));
            this.setRotationFromVector(this.tail, map.get("tail"));
        }
    }

    private float lerpTo(float start, float end) {
        return this.lerpTo(0.05F, start, end);
    }

    private float lerpTo(float delta, float start, float end) {
        return Mth.rotLerp(delta, start, end);
    }

    private void lerpPart(ModelPart part, float xDelta, float yDelta, float zDelta) {
        part.setRotation(this.lerpTo(part.xRot, xDelta), this.lerpTo(part.yRot, yDelta), this.lerpTo(part.zRot, zDelta));
    }

    private void setupLayStillOnGroundAnimation(float ageInTicks, float netHeadYaw) {
        float f = ageInTicks * 0.09F;
        float f1 = Mth.sin(f);
        float f2 = Mth.cos(f);
        float f3 = f1 * f1 - 2.0F * f1;
        float f4 = f2 * f2 - 3.0F * f1;
        this.head.xRot = this.lerpTo(this.head.xRot, -0.09F * f3);
        this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
        this.head.zRot = this.lerpTo(this.head.zRot, -0.2F);
        this.tail.yRot = this.lerpTo(this.tail.yRot, -0.1F + 0.1F * f3);
        this.topGills.xRot = this.lerpTo(this.topGills.xRot, 0.6F + 0.05F * f4);
        this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, -this.topGills.xRot);
        this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
        this.lerpPart(this.leftHindLeg, 1.1F, 1.0F, 0.0F);
        this.lerpPart(this.leftFrontLeg, 0.8F, 2.3F, -0.5F);
        this.applyMirrorLegRotations();
        this.body.xRot = this.lerpTo(0.2F, this.body.xRot, 0.0F);
        this.body.yRot = this.lerpTo(this.body.yRot, netHeadYaw * (float) (Math.PI / 180.0));
        this.body.zRot = this.lerpTo(this.body.zRot, 0.0F);
    }

    private void setupGroundCrawlingAnimation(float ageInTicks, float netHeadYaw) {
        float f = ageInTicks * 0.11F;
        float f1 = Mth.cos(f);
        float f2 = (f1 * f1 - 2.0F * f1) / 5.0F;
        float f3 = 0.7F * f1;
        this.head.xRot = this.lerpTo(this.head.xRot, 0.0F);
        this.head.yRot = this.lerpTo(this.head.yRot, 0.09F * f1);
        this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
        this.tail.yRot = this.lerpTo(this.tail.yRot, this.head.yRot);
        this.topGills.xRot = this.lerpTo(this.topGills.xRot, 0.6F - 0.08F * (f1 * f1 + 2.0F * Mth.sin(f)));
        this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, -this.topGills.xRot);
        this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
        this.lerpPart(this.leftHindLeg, 0.9424779F, 1.5F - f2, -0.1F);
        this.lerpPart(this.leftFrontLeg, 1.0995574F, (float) (Math.PI / 2) - f3, 0.0F);
        this.lerpPart(this.rightHindLeg, this.leftHindLeg.xRot, -1.0F - f2, 0.0F);
        this.lerpPart(this.rightFrontLeg, this.leftFrontLeg.xRot, (float) (-Math.PI / 2) - f3, 0.0F);
        this.body.xRot = this.lerpTo(0.2F, this.body.xRot, 0.0F);
        this.body.yRot = this.lerpTo(this.body.yRot, netHeadYaw * (float) (Math.PI / 180.0));
        this.body.zRot = this.lerpTo(this.body.zRot, 0.0F);
    }

    private void setupWaterHoveringAnimation(float ageInTicks) {
        float f = ageInTicks * 0.075F;
        float f1 = Mth.cos(f);
        float f2 = Mth.sin(f) * 0.15F;
        this.body.xRot = this.lerpTo(this.body.xRot, -0.15F + 0.075F * f1);
        this.body.y -= f2;
        this.head.xRot = this.lerpTo(this.head.xRot, -this.body.xRot);
        this.topGills.xRot = this.lerpTo(this.topGills.xRot, 0.2F * f1);
        this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, -0.3F * f1 - 0.19F);
        this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
        this.lerpPart(this.leftHindLeg, (float) (Math.PI * 3.0 / 4.0) - f1 * 0.11F, 0.47123894F, 1.7278761F);
        this.lerpPart(this.leftFrontLeg, (float) (Math.PI / 4) - f1 * 0.2F, 2.042035F, 0.0F);
        this.applyMirrorLegRotations();
        this.tail.yRot = this.lerpTo(this.tail.yRot, 0.5F * f1);
        this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
        this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
    }

    private void setupSwimmingAnimation(float ageInTicks, float headPitch) {
        float f = ageInTicks * 0.33F;
        float f1 = Mth.sin(f);
        float f2 = Mth.cos(f);
        float f3 = 0.13F * f1;
        this.body.xRot = this.lerpTo(0.1F, this.body.xRot, headPitch * (float) (Math.PI / 180.0) + f3);
        this.head.xRot = -f3 * 1.8F;
        this.body.y -= 0.45F * f2;
        this.topGills.xRot = this.lerpTo(this.topGills.xRot, -0.5F * f1 - 0.8F);
        this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, 0.3F * f1 + 0.9F);
        this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
        this.tail.yRot = this.lerpTo(this.tail.yRot, 0.3F * Mth.cos(f * 0.9F));
        this.lerpPart(this.leftHindLeg, 1.8849558F, -0.4F * f1, (float) (Math.PI / 2));
        this.lerpPart(this.leftFrontLeg, 1.8849558F, -0.2F * f2 - 0.1F, (float) (Math.PI / 2));
        this.applyMirrorLegRotations();
        this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
        this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
    }

    private void setupPlayDeadAnimation(float netHeadYaw) {
        this.lerpPart(this.leftHindLeg, 1.4137167F, 1.0995574F, (float) (Math.PI / 4));
        this.lerpPart(this.leftFrontLeg, (float) (Math.PI / 4), 2.042035F, 0.0F);
        this.body.xRot = this.lerpTo(this.body.xRot, -0.15F);
        this.body.zRot = this.lerpTo(this.body.zRot, 0.35F);
        this.applyMirrorLegRotations();
        this.body.yRot = this.lerpTo(this.body.yRot, netHeadYaw * (float) (Math.PI / 180.0));
        this.head.xRot = this.lerpTo(this.head.xRot, 0.0F);
        this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
        this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
        this.tail.yRot = this.lerpTo(this.tail.yRot, 0.0F);
        this.lerpPart(this.topGills, 0.0F, 0.0F, 0.0F);
        this.lerpPart(this.leftGills, 0.0F, 0.0F, 0.0F);
        this.lerpPart(this.rightGills, 0.0F, 0.0F, 0.0F);
    }

    private void applyMirrorLegRotations() {
        this.lerpPart(this.rightHindLeg, this.leftHindLeg.xRot, -this.leftHindLeg.yRot, -this.leftHindLeg.zRot);
        this.lerpPart(this.rightFrontLeg, this.leftFrontLeg.xRot, -this.leftFrontLeg.yRot, -this.leftFrontLeg.zRot);
    }
}
