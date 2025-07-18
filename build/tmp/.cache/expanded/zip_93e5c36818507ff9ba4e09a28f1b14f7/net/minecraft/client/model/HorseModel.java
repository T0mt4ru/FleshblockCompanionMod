package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseModel<T extends AbstractHorse> extends AgeableListModel<T> {
    private static final float DEG_125 = 2.1816616F;
    private static final float DEG_60 = (float) (Math.PI / 3);
    private static final float DEG_45 = (float) (Math.PI / 4);
    private static final float DEG_30 = (float) (Math.PI / 6);
    private static final float DEG_15 = (float) (Math.PI / 12);
    protected static final String HEAD_PARTS = "head_parts";
    private static final String LEFT_HIND_BABY_LEG = "left_hind_baby_leg";
    private static final String RIGHT_HIND_BABY_LEG = "right_hind_baby_leg";
    private static final String LEFT_FRONT_BABY_LEG = "left_front_baby_leg";
    private static final String RIGHT_FRONT_BABY_LEG = "right_front_baby_leg";
    private static final String SADDLE = "saddle";
    private static final String LEFT_SADDLE_MOUTH = "left_saddle_mouth";
    private static final String LEFT_SADDLE_LINE = "left_saddle_line";
    private static final String RIGHT_SADDLE_MOUTH = "right_saddle_mouth";
    private static final String RIGHT_SADDLE_LINE = "right_saddle_line";
    private static final String HEAD_SADDLE = "head_saddle";
    private static final String MOUTH_SADDLE_WRAP = "mouth_saddle_wrap";
    protected final ModelPart body;
    protected final ModelPart headParts;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightHindBabyLeg;
    private final ModelPart leftHindBabyLeg;
    private final ModelPart rightFrontBabyLeg;
    private final ModelPart leftFrontBabyLeg;
    private final ModelPart tail;
    private final ModelPart[] saddleParts;
    private final ModelPart[] ridingParts;

    public HorseModel(ModelPart root) {
        super(true, 16.2F, 1.36F, 2.7272F, 2.0F, 20.0F);
        this.body = root.getChild("body");
        this.headParts = root.getChild("head_parts");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.rightHindBabyLeg = root.getChild("right_hind_baby_leg");
        this.leftHindBabyLeg = root.getChild("left_hind_baby_leg");
        this.rightFrontBabyLeg = root.getChild("right_front_baby_leg");
        this.leftFrontBabyLeg = root.getChild("left_front_baby_leg");
        this.tail = this.body.getChild("tail");
        ModelPart modelpart = this.body.getChild("saddle");
        ModelPart modelpart1 = this.headParts.getChild("left_saddle_mouth");
        ModelPart modelpart2 = this.headParts.getChild("right_saddle_mouth");
        ModelPart modelpart3 = this.headParts.getChild("left_saddle_line");
        ModelPart modelpart4 = this.headParts.getChild("right_saddle_line");
        ModelPart modelpart5 = this.headParts.getChild("head_saddle");
        ModelPart modelpart6 = this.headParts.getChild("mouth_saddle_wrap");
        this.saddleParts = new ModelPart[]{modelpart, modelpart1, modelpart2, modelpart5, modelpart6};
        this.ridingParts = new ModelPart[]{modelpart3, modelpart4};
    }

    public static MeshDefinition createBodyMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 32).addBox(-5.0F, -8.0F, -17.0F, 10.0F, 10.0F, 22.0F, new CubeDeformation(0.05F)),
            PartPose.offset(0.0F, 11.0F, 5.0F)
        );
        PartDefinition partdefinition2 = partdefinition.addOrReplaceChild(
            "head_parts",
            CubeListBuilder.create().texOffs(0, 35).addBox(-2.05F, -6.0F, -2.0F, 4.0F, 12.0F, 7.0F),
            PartPose.offsetAndRotation(0.0F, 4.0F, -12.0F, (float) (Math.PI / 6), 0.0F, 0.0F)
        );
        PartDefinition partdefinition3 = partdefinition2.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -11.0F, -2.0F, 6.0F, 5.0F, 7.0F, cubeDeformation), PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
            "mane", CubeListBuilder.create().texOffs(56, 36).addBox(-1.0F, -11.0F, 5.01F, 2.0F, 16.0F, 2.0F, cubeDeformation), PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
            "upper_mouth", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -11.0F, -7.0F, 4.0F, 5.0F, 5.0F, cubeDeformation), PartPose.ZERO
        );
        partdefinition.addOrReplaceChild(
            "left_hind_leg",
            CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, cubeDeformation),
            PartPose.offset(4.0F, 14.0F, 7.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_hind_leg",
            CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, cubeDeformation),
            PartPose.offset(-4.0F, 14.0F, 7.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_front_leg",
            CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, cubeDeformation),
            PartPose.offset(4.0F, 14.0F, -12.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_front_leg",
            CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, cubeDeformation),
            PartPose.offset(-4.0F, 14.0F, -12.0F)
        );
        CubeDeformation cubedeformation = cubeDeformation.extend(0.0F, 5.5F, 0.0F);
        partdefinition.addOrReplaceChild(
            "left_hind_baby_leg",
            CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, cubedeformation),
            PartPose.offset(4.0F, 14.0F, 7.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_hind_baby_leg",
            CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, cubedeformation),
            PartPose.offset(-4.0F, 14.0F, 7.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_front_baby_leg",
            CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, cubedeformation),
            PartPose.offset(4.0F, 14.0F, -12.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_front_baby_leg",
            CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, cubedeformation),
            PartPose.offset(-4.0F, 14.0F, -12.0F)
        );
        partdefinition1.addOrReplaceChild(
            "tail",
            CubeListBuilder.create().texOffs(42, 36).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 4.0F, cubeDeformation),
            PartPose.offsetAndRotation(0.0F, -5.0F, 2.0F, (float) (Math.PI / 6), 0.0F, 0.0F)
        );
        partdefinition1.addOrReplaceChild(
            "saddle", CubeListBuilder.create().texOffs(26, 0).addBox(-5.0F, -8.0F, -9.0F, 10.0F, 9.0F, 9.0F, new CubeDeformation(0.5F)), PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
            "left_saddle_mouth", CubeListBuilder.create().texOffs(29, 5).addBox(2.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F, cubeDeformation), PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
            "right_saddle_mouth", CubeListBuilder.create().texOffs(29, 5).addBox(-3.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F, cubeDeformation), PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
            "left_saddle_line",
            CubeListBuilder.create().texOffs(32, 2).addBox(3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F),
            PartPose.rotation((float) (-Math.PI / 6), 0.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "right_saddle_line",
            CubeListBuilder.create().texOffs(32, 2).addBox(-3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F),
            PartPose.rotation((float) (-Math.PI / 6), 0.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "head_saddle", CubeListBuilder.create().texOffs(1, 1).addBox(-3.0F, -11.0F, -1.9F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.22F)), PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
            "mouth_saddle_wrap",
            CubeListBuilder.create().texOffs(19, 0).addBox(-2.0F, -11.0F, -4.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.2F)),
            PartPose.ZERO
        );
        partdefinition3.addOrReplaceChild(
            "left_ear", CubeListBuilder.create().texOffs(19, 16).addBox(0.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.001F)), PartPose.ZERO
        );
        partdefinition3.addOrReplaceChild(
            "right_ear", CubeListBuilder.create().texOffs(19, 16).addBox(-2.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.001F)), PartPose.ZERO
        );
        return meshdefinition;
    }

    /**
     * Sets this entity's model rotation angles
     */
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean flag = entity.isSaddled();
        boolean flag1 = entity.isVehicle();

        for (ModelPart modelpart : this.saddleParts) {
            modelpart.visible = flag;
        }

        for (ModelPart modelpart1 : this.ridingParts) {
            modelpart1.visible = flag1 && flag;
        }

        this.body.y = 11.0F;
    }

    @Override
    public Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.headParts);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(
            this.body,
            this.rightHindLeg,
            this.leftHindLeg,
            this.rightFrontLeg,
            this.leftFrontLeg,
            this.rightHindBabyLeg,
            this.leftHindBabyLeg,
            this.rightFrontBabyLeg,
            this.leftFrontBabyLeg
        );
    }

    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
        super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        float f = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float f1 = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float f2 = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        float f3 = f1 - f;
        float f4 = f2 * (float) (Math.PI / 180.0);
        if (f3 > 20.0F) {
            f3 = 20.0F;
        }

        if (f3 < -20.0F) {
            f3 = -20.0F;
        }

        if (limbSwingAmount > 0.2F) {
            f4 += Mth.cos(limbSwing * 0.8F) * 0.15F * limbSwingAmount;
        }

        float f5 = entity.getEatAnim(partialTick);
        float f6 = entity.getStandAnim(partialTick);
        float f7 = 1.0F - f6;
        float f8 = entity.getMouthAnim(partialTick);
        boolean flag = entity.tailCounter != 0;
        float f9 = (float)entity.tickCount + partialTick;
        this.headParts.y = 4.0F;
        this.headParts.z = -12.0F;
        this.body.xRot = 0.0F;
        this.headParts.xRot = (float) (Math.PI / 6) + f4;
        this.headParts.yRot = f3 * (float) (Math.PI / 180.0);
        float f10 = entity.isInWater() ? 0.2F : 1.0F;
        float f11 = Mth.cos(f10 * limbSwing * 0.6662F + (float) Math.PI);
        float f12 = f11 * 0.8F * limbSwingAmount;
        float f13 = (1.0F - Math.max(f6, f5)) * ((float) (Math.PI / 6) + f4 + f8 * Mth.sin(f9) * 0.05F);
        this.headParts.xRot = f6 * ((float) (Math.PI / 12) + f4) + f5 * (2.1816616F + Mth.sin(f9) * 0.05F) + f13;
        this.headParts.yRot = f6 * f3 * (float) (Math.PI / 180.0) + (1.0F - Math.max(f6, f5)) * this.headParts.yRot;
        this.headParts.y = f6 * -4.0F + f5 * 11.0F + (1.0F - Math.max(f6, f5)) * this.headParts.y;
        this.headParts.z = f6 * -4.0F + f5 * -12.0F + (1.0F - Math.max(f6, f5)) * this.headParts.z;
        this.body.xRot = f6 * (float) (-Math.PI / 4) + f7 * this.body.xRot;
        float f14 = (float) (Math.PI / 12) * f6;
        float f15 = Mth.cos(f9 * 0.6F + (float) Math.PI);
        this.leftFrontLeg.y = 2.0F * f6 + 14.0F * f7;
        this.leftFrontLeg.z = -6.0F * f6 - 10.0F * f7;
        this.rightFrontLeg.y = this.leftFrontLeg.y;
        this.rightFrontLeg.z = this.leftFrontLeg.z;
        float f16 = ((float) (-Math.PI / 3) + f15) * f6 + f12 * f7;
        float f17 = ((float) (-Math.PI / 3) - f15) * f6 - f12 * f7;
        this.leftHindLeg.xRot = f14 - f11 * 0.5F * limbSwingAmount * f7;
        this.rightHindLeg.xRot = f14 + f11 * 0.5F * limbSwingAmount * f7;
        this.leftFrontLeg.xRot = f16;
        this.rightFrontLeg.xRot = f17;
        this.tail.xRot = (float) (Math.PI / 6) + limbSwingAmount * 0.75F;
        this.tail.y = -5.0F + limbSwingAmount;
        this.tail.z = 2.0F + limbSwingAmount * 2.0F;
        if (flag) {
            this.tail.yRot = Mth.cos(f9 * 0.7F);
        } else {
            this.tail.yRot = 0.0F;
        }

        this.rightHindBabyLeg.y = this.rightHindLeg.y;
        this.rightHindBabyLeg.z = this.rightHindLeg.z;
        this.rightHindBabyLeg.xRot = this.rightHindLeg.xRot;
        this.leftHindBabyLeg.y = this.leftHindLeg.y;
        this.leftHindBabyLeg.z = this.leftHindLeg.z;
        this.leftHindBabyLeg.xRot = this.leftHindLeg.xRot;
        this.rightFrontBabyLeg.y = this.rightFrontLeg.y;
        this.rightFrontBabyLeg.z = this.rightFrontLeg.z;
        this.rightFrontBabyLeg.xRot = this.rightFrontLeg.xRot;
        this.leftFrontBabyLeg.y = this.leftFrontLeg.y;
        this.leftFrontBabyLeg.z = this.leftFrontLeg.z;
        this.leftFrontBabyLeg.xRot = this.leftFrontLeg.xRot;
        boolean flag1 = entity.isBaby();
        this.rightHindLeg.visible = !flag1;
        this.leftHindLeg.visible = !flag1;
        this.rightFrontLeg.visible = !flag1;
        this.leftFrontLeg.visible = !flag1;
        this.rightHindBabyLeg.visible = flag1;
        this.leftHindBabyLeg.visible = flag1;
        this.rightFrontBabyLeg.visible = flag1;
        this.leftFrontBabyLeg.visible = flag1;
        this.body.y = flag1 ? 10.8F : 0.0F;
    }
}
