package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HumanoidModel<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {
    public static final float OVERLAY_SCALE = 0.25F;
    public static final float HAT_OVERLAY_SCALE = 0.5F;
    public static final float LEGGINGS_OVERLAY_SCALE = -0.1F;
    private static final float DUCK_WALK_ROTATION = 0.005F;
    private static final float SPYGLASS_ARM_ROT_Y = (float) (Math.PI / 12);
    private static final float SPYGLASS_ARM_ROT_X = 1.9198622F;
    private static final float SPYGLASS_ARM_CROUCH_ROT_X = (float) (Math.PI / 12);
    private static final float HIGHEST_SHIELD_BLOCKING_ANGLE = (float) (-Math.PI * 4.0 / 9.0);
    private static final float LOWEST_SHIELD_BLOCKING_ANGLE = 0.43633232F;
    private static final float HORIZONTAL_SHIELD_MOVEMENT_LIMIT = (float) (Math.PI / 6);
    public static final float TOOT_HORN_XROT_BASE = 1.4835298F;
    public static final float TOOT_HORN_YROT_BASE = (float) (Math.PI / 6);
    public final ModelPart head;
    /**
     * The Biped's Headwear. Used for the outer layer of player skins.
     */
    public final ModelPart hat;
    public final ModelPart body;
    /**
     * The Biped's Right Arm
     */
    public final ModelPart rightArm;
    /**
     * The Biped's Left Arm
     */
    public final ModelPart leftArm;
    /**
     * The Biped's Right Leg
     */
    public final ModelPart rightLeg;
    /**
     * The Biped's Left Leg
     */
    public final ModelPart leftLeg;
    public HumanoidModel.ArmPose leftArmPose = HumanoidModel.ArmPose.EMPTY;
    public HumanoidModel.ArmPose rightArmPose = HumanoidModel.ArmPose.EMPTY;
    public boolean crouching;
    public float swimAmount;

    public HumanoidModel(ModelPart root) {
        this(root, RenderType::entityCutoutNoCull);
    }

    public HumanoidModel(ModelPart root, Function<ResourceLocation, RenderType> renderType) {
        super(renderType, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
        this.head = root.getChild("head");
        this.hat = root.getChild("hat");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation, float yOffset) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation),
            PartPose.offset(0.0F, 0.0F + yOffset, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "hat",
            CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation.extend(0.5F)),
            PartPose.offset(0.0F, 0.0F + yOffset, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubeDeformation),
            PartPose.offset(0.0F, 0.0F + yOffset, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_arm",
            CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation),
            PartPose.offset(-5.0F, 2.0F + yOffset, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_arm",
            CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation),
            PartPose.offset(5.0F, 2.0F + yOffset, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation),
            PartPose.offset(-1.9F, 12.0F + yOffset, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation),
            PartPose.offset(1.9F, 12.0F + yOffset, 0.0F)
        );
        return meshdefinition;
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
    }

    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
        this.swimAmount = entity.getSwimAmount(partialTick);
        super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
    }

    /**
     * Sets this entity's model rotation angles
     */
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean flag = entity.getFallFlyingTicks() > 4;
        boolean flag1 = entity.isVisuallySwimming();
        this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
        if (flag) {
            this.head.xRot = (float) (-Math.PI / 4);
        } else if (this.swimAmount > 0.0F) {
            if (flag1) {
                this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, (float) (-Math.PI / 4));
            } else {
                this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, headPitch * (float) (Math.PI / 180.0));
            }
        } else {
            this.head.xRot = headPitch * (float) (Math.PI / 180.0);
        }

        this.body.yRot = 0.0F;
        this.rightArm.z = 0.0F;
        this.rightArm.x = -5.0F;
        this.leftArm.z = 0.0F;
        this.leftArm.x = 5.0F;
        float f = 1.0F;
        if (flag) {
            f = (float)entity.getDeltaMovement().lengthSqr();
            f /= 0.2F;
            f *= f * f;
        }

        if (f < 1.0F) {
            f = 1.0F;
        }

        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F / f;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount / f;
        this.rightLeg.yRot = 0.005F;
        this.leftLeg.yRot = -0.005F;
        this.rightLeg.zRot = 0.005F;
        this.leftLeg.zRot = -0.005F;
        if (this.riding) {
            this.rightArm.xRot += (float) (-Math.PI / 5);
            this.leftArm.xRot += (float) (-Math.PI / 5);
            this.rightLeg.xRot = -1.4137167F;
            this.rightLeg.yRot = (float) (Math.PI / 10);
            this.rightLeg.zRot = 0.07853982F;
            this.leftLeg.xRot = -1.4137167F;
            this.leftLeg.yRot = (float) (-Math.PI / 10);
            this.leftLeg.zRot = -0.07853982F;
        }

        this.rightArm.yRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        boolean flag2 = entity.getMainArm() == HumanoidArm.RIGHT;
        if (entity.isUsingItem()) {
            boolean flag3 = entity.getUsedItemHand() == InteractionHand.MAIN_HAND;
            if (flag3 == flag2) {
                this.poseRightArm(entity);
            } else {
                this.poseLeftArm(entity);
            }
        } else {
            boolean flag4 = flag2 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
            if (flag2 != flag4) {
                this.poseLeftArm(entity);
                this.poseRightArm(entity);
            } else {
                this.poseRightArm(entity);
                this.poseLeftArm(entity);
            }
        }

        this.setupAttackAnimation(entity, ageInTicks);
        if (this.crouching) {
            this.body.xRot = 0.5F;
            this.rightArm.xRot += 0.4F;
            this.leftArm.xRot += 0.4F;
            this.rightLeg.z = 4.0F;
            this.leftLeg.z = 4.0F;
            this.rightLeg.y = 12.2F;
            this.leftLeg.y = 12.2F;
            this.head.y = 4.2F;
            this.body.y = 3.2F;
            this.leftArm.y = 5.2F;
            this.rightArm.y = 5.2F;
        } else {
            this.body.xRot = 0.0F;
            this.rightLeg.z = 0.0F;
            this.leftLeg.z = 0.0F;
            this.rightLeg.y = 12.0F;
            this.leftLeg.y = 12.0F;
            this.head.y = 0.0F;
            this.body.y = 0.0F;
            this.leftArm.y = 2.0F;
            this.rightArm.y = 2.0F;
        }

        if (this.rightArmPose != HumanoidModel.ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(this.rightArm, ageInTicks, 1.0F);
        }

        if (this.leftArmPose != HumanoidModel.ArmPose.SPYGLASS) {
            AnimationUtils.bobModelPart(this.leftArm, ageInTicks, -1.0F);
        }

        if (this.swimAmount > 0.0F) {
            float f5 = limbSwing % 26.0F;
            HumanoidArm humanoidarm = this.getAttackArm(entity);
            float f1 = humanoidarm == HumanoidArm.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
            float f2 = humanoidarm == HumanoidArm.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
            if (!entity.isUsingItem()) {
                if (f5 < 14.0F) {
                    this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, 0.0F);
                    this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, 0.0F);
                    this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float) Math.PI);
                    this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float) Math.PI);
                    this.leftArm.zRot = this.rotlerpRad(
                        f2, this.leftArm.zRot, (float) Math.PI + 1.8707964F * this.quadraticArmUpdate(f5) / this.quadraticArmUpdate(14.0F)
                    );
                    this.rightArm.zRot = Mth.lerp(
                        f1, this.rightArm.zRot, (float) Math.PI - 1.8707964F * this.quadraticArmUpdate(f5) / this.quadraticArmUpdate(14.0F)
                    );
                } else if (f5 >= 14.0F && f5 < 22.0F) {
                    float f6 = (f5 - 14.0F) / 8.0F;
                    this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, (float) (Math.PI / 2) * f6);
                    this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, (float) (Math.PI / 2) * f6);
                    this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float) Math.PI);
                    this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float) Math.PI);
                    this.leftArm.zRot = this.rotlerpRad(f2, this.leftArm.zRot, 5.012389F - 1.8707964F * f6);
                    this.rightArm.zRot = Mth.lerp(f1, this.rightArm.zRot, 1.2707963F + 1.8707964F * f6);
                } else if (f5 >= 22.0F && f5 < 26.0F) {
                    float f3 = (f5 - 22.0F) / 4.0F;
                    this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, (float) (Math.PI / 2) - (float) (Math.PI / 2) * f3);
                    this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, (float) (Math.PI / 2) - (float) (Math.PI / 2) * f3);
                    this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float) Math.PI);
                    this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float) Math.PI);
                    this.leftArm.zRot = this.rotlerpRad(f2, this.leftArm.zRot, (float) Math.PI);
                    this.rightArm.zRot = Mth.lerp(f1, this.rightArm.zRot, (float) Math.PI);
                }
            }

            float f7 = 0.3F;
            float f4 = 0.33333334F;
            this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * Mth.cos(limbSwing * 0.33333334F + (float) Math.PI));
            this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * Mth.cos(limbSwing * 0.33333334F));
        }

        this.hat.copyFrom(this.head);
    }

    private void poseRightArm(T livingEntity) {
        switch (this.rightArmPose) {
            case EMPTY:
                this.rightArm.yRot = 0.0F;
                break;
            case ITEM:
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) (Math.PI / 10);
                this.rightArm.yRot = 0.0F;
                break;
            case BLOCK:
                this.poseBlockingArm(this.rightArm, true);
                break;
            case BOW_AND_ARROW:
                this.rightArm.yRot = -0.1F + this.head.yRot;
                this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
                this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
                this.leftArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
                break;
            case THROW_SPEAR:
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) Math.PI;
                this.rightArm.yRot = 0.0F;
                break;
            case CROSSBOW_CHARGE:
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, livingEntity, true);
                break;
            case CROSSBOW_HOLD:
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
                break;
            case SPYGLASS:
                this.rightArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (livingEntity.isCrouching() ? (float) (Math.PI / 12) : 0.0F), -2.4F, 3.3F);
                this.rightArm.yRot = this.head.yRot - (float) (Math.PI / 12);
                break;
            case TOOT_HORN:
                this.rightArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
                this.rightArm.yRot = this.head.yRot - (float) (Math.PI / 6);
                break;
            case BRUSH:
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) (Math.PI / 5);
                this.rightArm.yRot = 0.0F;
            default:
                this.rightArmPose.applyTransform(this, livingEntity, net.minecraft.world.entity.HumanoidArm.RIGHT);
        }
    }

    private void poseLeftArm(T livingEntity) {
        switch (this.leftArmPose) {
            case EMPTY:
                this.leftArm.yRot = 0.0F;
                break;
            case ITEM:
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) (Math.PI / 10);
                this.leftArm.yRot = 0.0F;
                break;
            case BLOCK:
                this.poseBlockingArm(this.leftArm, false);
                break;
            case BOW_AND_ARROW:
                this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
                this.leftArm.yRot = 0.1F + this.head.yRot;
                this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
                this.leftArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
                break;
            case THROW_SPEAR:
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) Math.PI;
                this.leftArm.yRot = 0.0F;
                break;
            case CROSSBOW_CHARGE:
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, livingEntity, false);
                break;
            case CROSSBOW_HOLD:
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
                break;
            case SPYGLASS:
                this.leftArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (livingEntity.isCrouching() ? (float) (Math.PI / 12) : 0.0F), -2.4F, 3.3F);
                this.leftArm.yRot = this.head.yRot + (float) (Math.PI / 12);
                break;
            case TOOT_HORN:
                this.leftArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
                this.leftArm.yRot = this.head.yRot + (float) (Math.PI / 6);
                break;
            case BRUSH:
                this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) (Math.PI / 5);
                this.leftArm.yRot = 0.0F;
            default:
                this.leftArmPose.applyTransform(this, livingEntity, net.minecraft.world.entity.HumanoidArm.LEFT);
        }
    }

    private void poseBlockingArm(ModelPart arm, boolean isRightArm) {
        arm.xRot = arm.xRot * 0.5F - 0.9424779F + Mth.clamp(this.head.xRot, (float) (-Math.PI * 4.0 / 9.0), 0.43633232F);
        arm.yRot = (isRightArm ? -30.0F : 30.0F) * (float) (Math.PI / 180.0) + Mth.clamp(this.head.yRot, (float) (-Math.PI / 6), (float) (Math.PI / 6));
    }

    protected void setupAttackAnimation(T livingEntity, float ageInTicks) {
        if (!(this.attackTime <= 0.0F)) {
            HumanoidArm humanoidarm = this.getAttackArm(livingEntity);
            ModelPart modelpart = this.getArm(humanoidarm);
            float f = this.attackTime;
            this.body.yRot = Mth.sin(Mth.sqrt(f) * (float) (Math.PI * 2)) * 0.2F;
            if (humanoidarm == HumanoidArm.LEFT) {
                this.body.yRot *= -1.0F;
            }

            this.rightArm.z = Mth.sin(this.body.yRot) * 5.0F;
            this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0F;
            this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0F;
            this.leftArm.x = Mth.cos(this.body.yRot) * 5.0F;
            this.rightArm.yRot = this.rightArm.yRot + this.body.yRot;
            this.leftArm.yRot = this.leftArm.yRot + this.body.yRot;
            this.leftArm.xRot = this.leftArm.xRot + this.body.yRot;
            f = 1.0F - this.attackTime;
            f *= f;
            f *= f;
            f = 1.0F - f;
            float f1 = Mth.sin(f * (float) Math.PI);
            float f2 = Mth.sin(this.attackTime * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
            modelpart.xRot -= f1 * 1.2F + f2;
            modelpart.yRot = modelpart.yRot + this.body.yRot * 2.0F;
            modelpart.zRot = modelpart.zRot + Mth.sin(this.attackTime * (float) Math.PI) * -0.4F;
        }
    }

    protected float rotlerpRad(float angle, float maxAngle, float mul) {
        float f = (mul - maxAngle) % (float) (Math.PI * 2);
        if (f < (float) -Math.PI) {
            f += (float) (Math.PI * 2);
        }

        if (f >= (float) Math.PI) {
            f -= (float) (Math.PI * 2);
        }

        return maxAngle + angle * f;
    }

    private float quadraticArmUpdate(float limbSwing) {
        return -65.0F * limbSwing + limbSwing * limbSwing;
    }

    public void copyPropertiesTo(HumanoidModel<T> model) {
        super.copyPropertiesTo(model);
        model.leftArmPose = this.leftArmPose;
        model.rightArmPose = this.rightArmPose;
        model.crouching = this.crouching;
        model.head.copyFrom(this.head);
        model.hat.copyFrom(this.hat);
        model.body.copyFrom(this.body);
        model.rightArm.copyFrom(this.rightArm);
        model.leftArm.copyFrom(this.leftArm);
        model.rightLeg.copyFrom(this.rightLeg);
        model.leftLeg.copyFrom(this.leftLeg);
    }

    public void setAllVisible(boolean visible) {
        this.head.visible = visible;
        this.hat.visible = visible;
        this.body.visible = visible;
        this.rightArm.visible = visible;
        this.leftArm.visible = visible;
        this.rightLeg.visible = visible;
        this.leftLeg.visible = visible;
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        this.getArm(side).translateAndRotate(poseStack);
    }

    protected ModelPart getArm(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    private HumanoidArm getAttackArm(T entity) {
        HumanoidArm humanoidarm = entity.getMainArm();
        return entity.swingingArm == InteractionHand.MAIN_HAND ? humanoidarm : humanoidarm.getOpposite();
    }

    @OnlyIn(Dist.CLIENT)
    public static enum ArmPose implements net.neoforged.fml.common.asm.enumextension.IExtensibleEnum {
        EMPTY(false),
        ITEM(false),
        BLOCK(false),
        BOW_AND_ARROW(true),
        THROW_SPEAR(false),
        CROSSBOW_CHARGE(true),
        CROSSBOW_HOLD(true),
        SPYGLASS(false),
        TOOT_HORN(false),
        BRUSH(false);

        private final boolean twoHanded;
        @org.jetbrains.annotations.Nullable
        private final net.neoforged.neoforge.client.IArmPoseTransformer forgeArmPose;

        @net.neoforged.fml.common.asm.enumextension.ReservedConstructor
        private ArmPose(boolean twoHanded) {
            this.twoHanded = twoHanded;
            this.forgeArmPose = null;
        }

        private ArmPose(boolean p_twoHanded, net.neoforged.neoforge.client.IArmPoseTransformer forgeArmPose) {
            this.twoHanded = p_twoHanded;
            com.google.common.base.Preconditions.checkNotNull(forgeArmPose, "Cannot create new ArmPose with null transformer!");
            this.forgeArmPose = forgeArmPose;
        }

        public boolean isTwoHanded() {
            return this.twoHanded;
        }

        public <T extends LivingEntity> void applyTransform(HumanoidModel<T> model, T entity, net.minecraft.world.entity.HumanoidArm arm) {
            if (this.forgeArmPose != null) this.forgeArmPose.applyTransform(model, entity, arm);
        }

        public static net.neoforged.fml.common.asm.enumextension.ExtensionInfo getExtensionInfo() {
            return net.neoforged.fml.common.asm.enumextension.ExtensionInfo.nonExtended(HumanoidModel.ArmPose.class);
        }
    }
}
