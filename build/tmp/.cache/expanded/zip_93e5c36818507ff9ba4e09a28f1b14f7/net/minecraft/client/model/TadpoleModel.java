package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TadpoleModel<T extends Tadpole> extends AgeableListModel<T> {
    private final ModelPart root;
    private final ModelPart tail;

    public TadpoleModel(ModelPart root) {
        super(true, 8.0F, 3.35F);
        this.root = root;
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        float f = 0.0F;
        float f1 = 22.0F;
        float f2 = -3.0F;
        partdefinition.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 3.0F), PartPose.offset(0.0F, 22.0F, -3.0F)
        );
        partdefinition.addOrReplaceChild(
            "tail", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.0F, 0.0F, 0.0F, 2.0F, 7.0F), PartPose.offset(0.0F, 22.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.root);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.tail);
    }

    /**
     * Sets this entity's model rotation angles
     */
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float f = entity.isInWater() ? 1.0F : 1.5F;
        this.tail.yRot = -f * 0.25F * Mth.sin(0.3F * ageInTicks);
    }
}
