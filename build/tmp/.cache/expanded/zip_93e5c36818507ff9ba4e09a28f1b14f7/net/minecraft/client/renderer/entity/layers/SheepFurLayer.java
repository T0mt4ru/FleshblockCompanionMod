package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SheepFurLayer extends RenderLayer<Sheep, SheepModel<Sheep>> {
    private static final ResourceLocation SHEEP_FUR_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/sheep/sheep_fur.png");
    private final SheepFurModel<Sheep> model;

    public SheepFurLayer(RenderLayerParent<Sheep, SheepModel<Sheep>> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new SheepFurModel<>(modelSet.bakeLayer(ModelLayers.SHEEP_FUR));
    }

    public void render(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        Sheep livingEntity,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch
    ) {
        if (!livingEntity.isSheared()) {
            if (livingEntity.isInvisible()) {
                Minecraft minecraft = Minecraft.getInstance();
                boolean flag = minecraft.shouldEntityAppearGlowing(livingEntity);
                if (flag) {
                    this.getParentModel().copyPropertiesTo(this.model);
                    this.model.prepareMobModel(livingEntity, limbSwing, limbSwingAmount, partialTicks);
                    this.model.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                    VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.outline(SHEEP_FUR_LOCATION));
                    this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0F), -16777216);
                }
            } else {
                int i;
                if (livingEntity.hasCustomName() && "jeb_".equals(livingEntity.getName().getString())) {
                    int j = 25;
                    int k = livingEntity.tickCount / 25 + livingEntity.getId();
                    int l = DyeColor.values().length;
                    int i1 = k % l;
                    int j1 = (k + 1) % l;
                    float f = ((float)(livingEntity.tickCount % 25) + partialTicks) / 25.0F;
                    int k1 = Sheep.getColor(DyeColor.byId(i1));
                    int l1 = Sheep.getColor(DyeColor.byId(j1));
                    i = FastColor.ARGB32.lerp(f, k1, l1);
                } else {
                    i = Sheep.getColor(livingEntity.getColor());
                }

                coloredCutoutModelCopyLayerRender(
                    this.getParentModel(),
                    this.model,
                    SHEEP_FUR_LOCATION,
                    poseStack,
                    buffer,
                    packedLight,
                    livingEntity,
                    limbSwing,
                    limbSwingAmount,
                    ageInTicks,
                    netHeadYaw,
                    headPitch,
                    partialTicks,
                    i
                );
            }
        }
    }
}
