package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitchModel<T extends Entity> extends VillagerModel<T> {
    private boolean holdingItem;

    public WitchModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = VillagerModel.createBodyModel();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), PartPose.ZERO
        );
        PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild(
            "hat", CubeListBuilder.create().texOffs(0, 64).addBox(0.0F, 0.0F, 0.0F, 10.0F, 2.0F, 10.0F), PartPose.offset(-5.0F, -10.03125F, -5.0F)
        );
        PartDefinition partdefinition3 = partdefinition2.addOrReplaceChild(
            "hat2",
            CubeListBuilder.create().texOffs(0, 76).addBox(0.0F, 0.0F, 0.0F, 7.0F, 4.0F, 7.0F),
            PartPose.offsetAndRotation(1.75F, -4.0F, 2.0F, -0.05235988F, 0.0F, 0.02617994F)
        );
        PartDefinition partdefinition4 = partdefinition3.addOrReplaceChild(
            "hat3",
            CubeListBuilder.create().texOffs(0, 87).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F),
            PartPose.offsetAndRotation(1.75F, -4.0F, 2.0F, -0.10471976F, 0.0F, 0.05235988F)
        );
        partdefinition4.addOrReplaceChild(
            "hat4",
            CubeListBuilder.create().texOffs(0, 95).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)),
            PartPose.offsetAndRotation(1.75F, -2.0F, 2.0F, (float) (-Math.PI / 15), 0.0F, 0.10471976F)
        );
        PartDefinition partdefinition5 = partdefinition1.getChild("nose");
        partdefinition5.addOrReplaceChild(
            "mole",
            CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 3.0F, -6.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)),
            PartPose.offset(0.0F, -2.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 64, 128);
    }

    /**
     * Sets this entity's model rotation angles
     */
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.nose.setPos(0.0F, -2.0F, 0.0F);
        float f = 0.01F * (float)(entity.getId() % 10);
        this.nose.xRot = Mth.sin((float)entity.tickCount * f) * 4.5F * (float) (Math.PI / 180.0);
        this.nose.yRot = 0.0F;
        this.nose.zRot = Mth.cos((float)entity.tickCount * f) * 2.5F * (float) (Math.PI / 180.0);
        if (this.holdingItem) {
            this.nose.setPos(0.0F, 1.0F, -1.5F);
            this.nose.xRot = -0.9F;
        }
    }

    public ModelPart getNose() {
        return this.nose;
    }

    public void setHoldingItem(boolean holdingItem) {
        this.holdingItem = holdingItem;
    }
}
