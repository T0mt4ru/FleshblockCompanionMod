package net.minecraft.client.resources.sounds;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Bee;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeAggressiveSoundInstance extends BeeSoundInstance {
    public BeeAggressiveSoundInstance(Bee bee) {
        super(bee, SoundEvents.BEE_LOOP_AGGRESSIVE, SoundSource.NEUTRAL);
        this.delay = 0;
    }

    @Override
    protected AbstractTickableSoundInstance getAlternativeSoundInstance() {
        return new BeeFlyingSoundInstance(this.bee);
    }

    @Override
    protected boolean shouldSwitchSounds() {
        return !this.bee.isAngry();
    }
}
