package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DecoratedPotRenderer implements BlockEntityRenderer<DecoratedPotBlockEntity> {
    private static final String NECK = "neck";
    private static final String FRONT = "front";
    private static final String BACK = "back";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String TOP = "top";
    private static final String BOTTOM = "bottom";
    private final ModelPart neck;
    private final ModelPart frontSide;
    private final ModelPart backSide;
    private final ModelPart leftSide;
    private final ModelPart rightSide;
    private final ModelPart top;
    private final ModelPart bottom;
    private static final float WOBBLE_AMPLITUDE = 0.125F;

    public DecoratedPotRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelpart = context.bakeLayer(ModelLayers.DECORATED_POT_BASE);
        this.neck = modelpart.getChild("neck");
        this.top = modelpart.getChild("top");
        this.bottom = modelpart.getChild("bottom");
        ModelPart modelpart1 = context.bakeLayer(ModelLayers.DECORATED_POT_SIDES);
        this.frontSide = modelpart1.getChild("front");
        this.backSide = modelpart1.getChild("back");
        this.leftSide = modelpart1.getChild("left");
        this.rightSide = modelpart1.getChild("right");
    }

    public static LayerDefinition createBaseLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        CubeDeformation cubedeformation = new CubeDeformation(0.2F);
        CubeDeformation cubedeformation1 = new CubeDeformation(-0.1F);
        partdefinition.addOrReplaceChild(
            "neck",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(4.0F, 17.0F, 4.0F, 8.0F, 3.0F, 8.0F, cubedeformation1)
                .texOffs(0, 5)
                .addBox(5.0F, 20.0F, 5.0F, 6.0F, 1.0F, 6.0F, cubedeformation),
            PartPose.offsetAndRotation(0.0F, 37.0F, 16.0F, (float) Math.PI, 0.0F, 0.0F)
        );
        CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(-14, 13).addBox(0.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F);
        partdefinition.addOrReplaceChild("top", cubelistbuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("bottom", cubelistbuilder, PartPose.offsetAndRotation(1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public static LayerDefinition createSidesLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(1, 0).addBox(0.0F, 0.0F, 0.0F, 14.0F, 16.0F, 0.0F, EnumSet.of(Direction.NORTH));
        partdefinition.addOrReplaceChild("back", cubelistbuilder, PartPose.offsetAndRotation(15.0F, 16.0F, 1.0F, 0.0F, 0.0F, (float) Math.PI));
        partdefinition.addOrReplaceChild("left", cubelistbuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, (float) (-Math.PI / 2), (float) Math.PI));
        partdefinition.addOrReplaceChild(
            "right", cubelistbuilder, PartPose.offsetAndRotation(15.0F, 16.0F, 15.0F, 0.0F, (float) (Math.PI / 2), (float) Math.PI)
        );
        partdefinition.addOrReplaceChild("front", cubelistbuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 15.0F, (float) Math.PI, 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static Material getSideMaterial(Optional<Item> item) {
        if (item.isPresent()) {
            Material material = Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.getPatternFromItem(item.get()));
            if (material != null) {
                return material;
            }
        }

        return Sheets.DECORATED_POT_SIDE;
    }

    public void render(DecoratedPotBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        Direction direction = blockEntity.getDirection();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - direction.toYRot()));
        poseStack.translate(-0.5, 0.0, -0.5);
        DecoratedPotBlockEntity.WobbleStyle decoratedpotblockentity$wobblestyle = blockEntity.lastWobbleStyle;
        if (decoratedpotblockentity$wobblestyle != null && blockEntity.getLevel() != null) {
            float f = ((float)(blockEntity.getLevel().getGameTime() - blockEntity.wobbleStartedAtTick) + partialTick)
                / (float)decoratedpotblockentity$wobblestyle.duration;
            if (f >= 0.0F && f <= 1.0F) {
                if (decoratedpotblockentity$wobblestyle == DecoratedPotBlockEntity.WobbleStyle.POSITIVE) {
                    float f1 = 0.015625F;
                    float f2 = f * (float) (Math.PI * 2);
                    float f3 = -1.5F * (Mth.cos(f2) + 0.5F) * Mth.sin(f2 / 2.0F);
                    poseStack.rotateAround(Axis.XP.rotation(f3 * 0.015625F), 0.5F, 0.0F, 0.5F);
                    float f4 = Mth.sin(f2);
                    poseStack.rotateAround(Axis.ZP.rotation(f4 * 0.015625F), 0.5F, 0.0F, 0.5F);
                } else {
                    float f5 = Mth.sin(-f * 3.0F * (float) Math.PI) * 0.125F;
                    float f6 = 1.0F - f;
                    poseStack.rotateAround(Axis.YP.rotation(f5 * f6), 0.5F, 0.0F, 0.5F);
                }
            }
        }

        VertexConsumer vertexconsumer = Sheets.DECORATED_POT_BASE.buffer(bufferSource, RenderType::entitySolid);
        this.neck.render(poseStack, vertexconsumer, packedLight, packedOverlay);
        this.top.render(poseStack, vertexconsumer, packedLight, packedOverlay);
        this.bottom.render(poseStack, vertexconsumer, packedLight, packedOverlay);
        PotDecorations potdecorations = blockEntity.getDecorations();
        this.renderSide(this.frontSide, poseStack, bufferSource, packedLight, packedOverlay, getSideMaterial(potdecorations.front()));
        this.renderSide(this.backSide, poseStack, bufferSource, packedLight, packedOverlay, getSideMaterial(potdecorations.back()));
        this.renderSide(this.leftSide, poseStack, bufferSource, packedLight, packedOverlay, getSideMaterial(potdecorations.left()));
        this.renderSide(this.rightSide, poseStack, bufferSource, packedLight, packedOverlay, getSideMaterial(potdecorations.right()));
        poseStack.popPose();
    }

    private void renderSide(ModelPart modelPart, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, Material material) {
        modelPart.render(poseStack, material.buffer(buffer, RenderType::entitySolid), packedLight, packedOverlay);
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(DecoratedPotBlockEntity blockEntity) {
        net.minecraft.core.BlockPos pos = blockEntity.getBlockPos();
        return new net.minecraft.world.phys.AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.3, pos.getZ() + 1.0);
    }
}
