package net.minecraft.client.resources.sounds;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UnderwaterAmbientSoundInstances {
    @OnlyIn(Dist.CLIENT)
    public static class SubSound extends AbstractTickableSoundInstance {
        private final LocalPlayer player;

        protected SubSound(LocalPlayer player, SoundEvent soundEvent) {
            super(soundEvent, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
            this.player = player;
            this.looping = false;
            this.delay = 0;
            this.volume = 1.0F;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (this.player.isRemoved() || !this.player.isUnderWater()) {
                this.stop();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class UnderwaterAmbientSoundInstance extends AbstractTickableSoundInstance {
        public static final int FADE_DURATION = 40;
        private final LocalPlayer player;
        private int fade;

        public UnderwaterAmbientSoundInstance(LocalPlayer player) {
            super(SoundEvents.AMBIENT_UNDERWATER_LOOP, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
            this.player = player;
            this.looping = true;
            this.delay = 0;
            this.volume = 1.0F;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (!this.player.isRemoved() && this.fade >= 0) {
                if (this.player.isUnderWater()) {
                    this.fade++;
                } else {
                    this.fade -= 2;
                }

                this.fade = Math.min(this.fade, 40);
                this.volume = Math.max(0.0F, Math.min((float)this.fade / 40.0F, 1.0F));
            } else {
                this.stop();
            }
        }
    }
}
