package net.minecraft.client.model;

import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlazeModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart[] upperBodyParts;
    private final ModelPart head;

    public BlazeModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.upperBodyParts = new ModelPart[12];
        Arrays.setAll(this.upperBodyParts, p_170449_ -> root.getChild(getPartName(p_170449_)));
    }

    private static String getPartName(int index) {
        return "part" + index;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        float f = 0.0F;
        CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F);

        for (int i = 0; i < 4; i++) {
            float f1 = Mth.cos(f) * 9.0F;
            float f2 = -2.0F + Mth.cos((float)(i * 2) * 0.25F);
            float f3 = Mth.sin(f) * 9.0F;
            partdefinition.addOrReplaceChild(getPartName(i), cubelistbuilder, PartPose.offset(f1, f2, f3));
            f++;
        }

        f = (float) (Math.PI / 4);

        for (int j = 4; j < 8; j++) {
            float f4 = Mth.cos(f) * 7.0F;
            float f6 = 2.0F + Mth.cos((float)(j * 2) * 0.25F);
            float f8 = Mth.sin(f) * 7.0F;
            partdefinition.addOrReplaceChild(getPartName(j), cubelistbuilder, PartPose.offset(f4, f6, f8));
            f++;
        }

        f = 0.47123894F;

        for (int k = 8; k < 12; k++) {
            float f5 = Mth.cos(f) * 5.0F;
            float f7 = 11.0F + Mth.cos((float)k * 1.5F * 0.5F);
            float f9 = Mth.sin(f) * 5.0F;
            partdefinition.addOrReplaceChild(getPartName(k), cubelistbuilder, PartPose.offset(f5, f7, f9));
            f++;
        }

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    /**
     * Sets this entity's model rotation angles
     */
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float f = ageInTicks * (float) Math.PI * -0.1F;

        for (int i = 0; i < 4; i++) {
            this.upperBodyParts[i].y = -2.0F + Mth.cos(((float)(i * 2) + ageInTicks) * 0.25F);
            this.upperBodyParts[i].x = Mth.cos(f) * 9.0F;
            this.upperBodyParts[i].z = Mth.sin(f) * 9.0F;
            f++;
        }

        f = (float) (Math.PI / 4) + ageInTicks * (float) Math.PI * 0.03F;

        for (int j = 4; j < 8; j++) {
            this.upperBodyParts[j].y = 2.0F + Mth.cos(((float)(j * 2) + ageInTicks) * 0.25F);
            this.upperBodyParts[j].x = Mth.cos(f) * 7.0F;
            this.upperBodyParts[j].z = Mth.sin(f) * 7.0F;
            f++;
        }

        f = 0.47123894F + ageInTicks * (float) Math.PI * -0.05F;

        for (int k = 8; k < 12; k++) {
            this.upperBodyParts[k].y = 11.0F + Mth.cos(((float)k * 1.5F + ageInTicks) * 0.5F);
            this.upperBodyParts[k].x = Mth.cos(f) * 5.0F;
            this.upperBodyParts[k].z = Mth.sin(f) * 5.0F;
            f++;
        }

        this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
        this.head.xRot = headPitch * (float) (Math.PI / 180.0);
    }
}
