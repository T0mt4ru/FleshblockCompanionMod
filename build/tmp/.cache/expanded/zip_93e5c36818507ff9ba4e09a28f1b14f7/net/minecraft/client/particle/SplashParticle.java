package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SplashParticle extends WaterDropParticle {
    protected SplashParticle(ClientLevel p_107929_, double p_107930_, double p_107931_, double p_107932_, double p_107933_, double p_107934_, double p_107935_) {
        super(p_107929_, p_107930_, p_107931_, p_107932_);
        this.gravity = 0.04F;
        if (p_107934_ == 0.0 && (p_107933_ != 0.0 || p_107935_ != 0.0)) {
            this.xd = p_107933_;
            this.yd = 0.1;
            this.zd = p_107935_;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprites) {
            this.sprite = sprites;
        }

        public Particle createParticle(
            SimpleParticleType type,
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed
        ) {
            SplashParticle splashparticle = new SplashParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            splashparticle.pickSprite(this.sprite);
            return splashparticle;
        }
    }
}
