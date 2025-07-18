package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockMarker extends TextureSheetParticle {
    protected BlockMarker(ClientLevel level, double x, double y, double z, BlockState state) {
        super(level, x, y, z);
        this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state));
        this.gravity = 0.0F;
        this.lifetime = 80;
        this.hasPhysics = false;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        return 0.5F;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<BlockParticleOption> {
        public Particle createParticle(
            BlockParticleOption p_194277_,
            ClientLevel p_194278_,
            double p_194279_,
            double p_194280_,
            double p_194281_,
            double p_194282_,
            double p_194283_,
            double p_194284_
        ) {
            return new BlockMarker(p_194278_, p_194279_, p_194280_, p_194281_, p_194277_.getState());
        }
    }
}
