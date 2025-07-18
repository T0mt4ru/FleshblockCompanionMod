package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.warden.Warden;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WardenEmissiveLayer<T extends Warden, M extends WardenModel<T>> extends RenderLayer<T, M> {
    private final ResourceLocation texture;
    private final WardenEmissiveLayer.AlphaFunction<T> alphaFunction;
    private final WardenEmissiveLayer.DrawSelector<T, M> drawSelector;

    public WardenEmissiveLayer(
        RenderLayerParent<T, M> renderer,
        ResourceLocation texture,
        WardenEmissiveLayer.AlphaFunction<T> alphaFunction,
        WardenEmissiveLayer.DrawSelector<T, M> drawSelector
    ) {
        super(renderer);
        this.texture = texture;
        this.alphaFunction = alphaFunction;
        this.drawSelector = drawSelector;
    }

    public void render(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        int packedLight,
        T livingEntity,
        float limbSwing,
        float limbSwingAmount,
        float partialTick,
        float ageInTicks,
        float netHeadYaw,
        float headPitch
    ) {
        if (!livingEntity.isInvisible()) {
            this.onlyDrawSelectedParts();
            VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(this.texture));
            float f = this.alphaFunction.apply(livingEntity, partialTick, ageInTicks);
            int i = FastColor.ARGB32.color(Mth.floor(f * 255.0F), 255, 255, 255);
            this.getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0F), i);
            this.resetDrawForAllParts();
        }
    }

    private void onlyDrawSelectedParts() {
        List<ModelPart> list = this.drawSelector.getPartsToDraw(this.getParentModel());
        this.getParentModel().root().getAllParts().forEach(p_234918_ -> p_234918_.skipDraw = true);
        list.forEach(p_234916_ -> p_234916_.skipDraw = false);
    }

    private void resetDrawForAllParts() {
        this.getParentModel().root().getAllParts().forEach(p_234913_ -> p_234913_.skipDraw = false);
    }

    @OnlyIn(Dist.CLIENT)
    public interface AlphaFunction<T extends Warden> {
        float apply(T livingEntity, float partialTick, float ageInTicks);
    }

    @OnlyIn(Dist.CLIENT)
    public interface DrawSelector<T extends Warden, M extends EntityModel<T>> {
        List<ModelPart> getPartsToDraw(M parentModel);
    }
}
