package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class ScreenEffectRenderer {
    private static final ResourceLocation UNDERWATER_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");

    public static void renderScreenEffect(Minecraft minecraft, PoseStack poseStack) {
        Player player = minecraft.player;
        if (!player.noPhysics) {
            org.apache.commons.lang3.tuple.Pair<BlockState, BlockPos> overlay = getOverlayBlock(player);
            if (overlay != null) {
                if (!net.neoforged.neoforge.client.ClientHooks.renderBlockOverlay(player, poseStack, net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent.OverlayType.BLOCK, overlay.getLeft(), overlay.getRight()))
                    renderTex(minecraft.getBlockRenderer().getBlockModelShaper().getTexture(overlay.getLeft(), minecraft.level, overlay.getRight()), poseStack);
            }
        }

        if (!minecraft.player.isSpectator()) {
            if (minecraft.player.isEyeInFluid(FluidTags.WATER)) {
                if (!net.neoforged.neoforge.client.ClientHooks.renderWaterOverlay(player, poseStack))
                renderWater(minecraft, poseStack);
            }
            else if (!player.getEyeInFluidType().isAir()) net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(player.getEyeInFluidType()).renderOverlay(minecraft, poseStack);

            if (minecraft.player.isOnFire()) {
                if (!net.neoforged.neoforge.client.ClientHooks.renderFireOverlay(player, poseStack))
                renderFire(minecraft, poseStack);
            }
        }
    }

    @Nullable
    private static BlockState getViewBlockingState(Player player) {
        return getOverlayBlock(player).getLeft();
    }

    @Nullable
    private static org.apache.commons.lang3.tuple.Pair<BlockState, BlockPos> getOverlayBlock(Player p_110717_) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 8; i++) {
            double d0 = p_110717_.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * p_110717_.getBbWidth() * 0.8F);
            double d1 = p_110717_.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F * p_110717_.getScale());
            double d2 = p_110717_.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * p_110717_.getBbWidth() * 0.8F);
            blockpos$mutableblockpos.set(d0, d1, d2);
            BlockState blockstate = p_110717_.level().getBlockState(blockpos$mutableblockpos);
            if (blockstate.getRenderShape() != RenderShape.INVISIBLE && blockstate.isViewBlocking(p_110717_.level(), blockpos$mutableblockpos)) {
                return org.apache.commons.lang3.tuple.Pair.of(blockstate, blockpos$mutableblockpos.immutable());
            }
        }

        return null;
    }

    private static void renderTex(TextureAtlasSprite texture, PoseStack poseStack) {
        RenderSystem.setShaderTexture(0, texture.atlasLocation());
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        float f = 0.1F;
        float f1 = -1.0F;
        float f2 = 1.0F;
        float f3 = -1.0F;
        float f4 = 1.0F;
        float f5 = -0.5F;
        float f6 = texture.getU0();
        float f7 = texture.getU1();
        float f8 = texture.getV0();
        float f9 = texture.getV1();
        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix4f, -1.0F, -1.0F, -0.5F).setUv(f7, f9).setColor(0.1F, 0.1F, 0.1F, 1.0F);
        bufferbuilder.addVertex(matrix4f, 1.0F, -1.0F, -0.5F).setUv(f6, f9).setColor(0.1F, 0.1F, 0.1F, 1.0F);
        bufferbuilder.addVertex(matrix4f, 1.0F, 1.0F, -0.5F).setUv(f6, f8).setColor(0.1F, 0.1F, 0.1F, 1.0F);
        bufferbuilder.addVertex(matrix4f, -1.0F, 1.0F, -0.5F).setUv(f7, f8).setColor(0.1F, 0.1F, 0.1F, 1.0F);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    private static void renderWater(Minecraft minecraft, PoseStack poseStack) {
        renderFluid(minecraft, poseStack, UNDERWATER_LOCATION);
    }

    public static void renderFluid(Minecraft p_110726_, PoseStack p_110727_, ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        BlockPos blockpos = BlockPos.containing(p_110726_.player.getX(), p_110726_.player.getEyeY(), p_110726_.player.getZ());
        float f = LightTexture.getBrightness(p_110726_.player.level().dimensionType(), p_110726_.player.level().getMaxLocalRawBrightness(blockpos));
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(f, f, f, 0.1F);
        float f1 = 4.0F;
        float f2 = -1.0F;
        float f3 = 1.0F;
        float f4 = -1.0F;
        float f5 = 1.0F;
        float f6 = -0.5F;
        float f7 = -p_110726_.player.getYRot() / 64.0F;
        float f8 = p_110726_.player.getXRot() / 64.0F;
        Matrix4f matrix4f = p_110727_.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(matrix4f, -1.0F, -1.0F, -0.5F).setUv(4.0F + f7, 4.0F + f8);
        bufferbuilder.addVertex(matrix4f, 1.0F, -1.0F, -0.5F).setUv(0.0F + f7, 4.0F + f8);
        bufferbuilder.addVertex(matrix4f, 1.0F, 1.0F, -0.5F).setUv(0.0F + f7, 0.0F + f8);
        bufferbuilder.addVertex(matrix4f, -1.0F, 1.0F, -0.5F).setUv(4.0F + f7, 0.0F + f8);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static void renderFire(Minecraft minecraft, PoseStack poseStack) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.depthFunc(519);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        TextureAtlasSprite textureatlassprite = ModelBakery.FIRE_1.sprite();
        RenderSystem.setShaderTexture(0, textureatlassprite.atlasLocation());
        float f = textureatlassprite.getU0();
        float f1 = textureatlassprite.getU1();
        float f2 = (f + f1) / 2.0F;
        float f3 = textureatlassprite.getV0();
        float f4 = textureatlassprite.getV1();
        float f5 = (f3 + f4) / 2.0F;
        float f6 = textureatlassprite.uvShrinkRatio();
        float f7 = Mth.lerp(f6, f, f2);
        float f8 = Mth.lerp(f6, f1, f2);
        float f9 = Mth.lerp(f6, f3, f5);
        float f10 = Mth.lerp(f6, f4, f5);
        float f11 = 1.0F;

        for (int i = 0; i < 2; i++) {
            poseStack.pushPose();
            float f12 = -0.5F;
            float f13 = 0.5F;
            float f14 = -0.5F;
            float f15 = 0.5F;
            float f16 = -0.5F;
            poseStack.translate((float)(-(i * 2 - 1)) * 0.24F, -0.3F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees((float)(i * 2 - 1) * 10.0F));
            Matrix4f matrix4f = poseStack.last().pose();
            BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.addVertex(matrix4f, -0.5F, -0.5F, -0.5F).setUv(f8, f10).setColor(1.0F, 1.0F, 1.0F, 0.9F);
            bufferbuilder.addVertex(matrix4f, 0.5F, -0.5F, -0.5F).setUv(f7, f10).setColor(1.0F, 1.0F, 1.0F, 0.9F);
            bufferbuilder.addVertex(matrix4f, 0.5F, 0.5F, -0.5F).setUv(f7, f9).setColor(1.0F, 1.0F, 1.0F, 0.9F);
            bufferbuilder.addVertex(matrix4f, -0.5F, 0.5F, -0.5F).setUv(f8, f9).setColor(1.0F, 1.0F, 1.0F, 0.9F);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
            poseStack.popPose();
        }

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
    }
}
