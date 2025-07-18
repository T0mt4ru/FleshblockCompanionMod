package net.minecraft.client.model;

import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SquidModel<T extends Entity> extends HierarchicalModel<T> {
    private final ModelPart[] tentacles = new ModelPart[8];
    private final ModelPart root;

    public SquidModel(ModelPart root) {
        this.root = root;
        Arrays.setAll(this.tentacles, p_170995_ -> root.getChild(createTentacleName(p_170995_)));
    }

    private static String createTentacleName(int index) {
        return "tentacle" + index;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        CubeDeformation cubedeformation = new CubeDeformation(0.02F);
        int i = -16;
        partdefinition.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -8.0F, -6.0F, 12.0F, 16.0F, 12.0F, cubedeformation), PartPose.offset(0.0F, 8.0F, 0.0F)
        );
        int j = 8;
        CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 18.0F, 2.0F);

        for (int k = 0; k < 8; k++) {
            double d0 = (double)k * Math.PI * 2.0 / 8.0;
            float f = (float)Math.cos(d0) * 5.0F;
            float f1 = 15.0F;
            float f2 = (float)Math.sin(d0) * 5.0F;
            d0 = (double)k * Math.PI * -2.0 / 8.0 + (Math.PI / 2);
            float f3 = (float)d0;
            partdefinition.addOrReplaceChild(createTentacleName(k), cubelistbuilder, PartPose.offsetAndRotation(f, 15.0F, f2, 0.0F, f3, 0.0F));
        }

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    /**
     * Sets this entity's model rotation angles
     */
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        for (ModelPart modelpart : this.tentacles) {
            modelpart.xRot = ageInTicks;
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
