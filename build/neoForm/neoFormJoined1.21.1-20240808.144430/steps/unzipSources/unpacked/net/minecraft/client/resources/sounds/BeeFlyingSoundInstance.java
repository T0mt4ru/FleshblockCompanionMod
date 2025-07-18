package net.minecraft.client.resources.sounds;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Bee;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeFlyingSoundInstance extends BeeSoundInstance {
    public BeeFlyingSoundInstance(Bee bee) {
        super(bee, SoundEvents.BEE_LOOP, SoundSource.NEUTRAL);
    }

    @Override
    protected AbstractTickableSoundInstance getAlternativeSoundInstance() {
        return new BeeAggressiveSoundInstance(this.bee);
    }

    @Override
    protected boolean shouldSwitchSounds() {
        return this.bee.isAngry();
    }
}
