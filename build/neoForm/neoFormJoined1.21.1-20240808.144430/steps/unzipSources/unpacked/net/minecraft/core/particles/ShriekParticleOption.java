package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ShriekParticleOption implements ParticleOptions {
    public static final MapCodec<ShriekParticleOption> CODEC = RecordCodecBuilder.mapCodec(
        p_235952_ -> p_235952_.group(Codec.INT.fieldOf("delay").forGetter(p_235954_ -> p_235954_.delay)).apply(p_235952_, ShriekParticleOption::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ShriekParticleOption> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, p_319440_ -> p_319440_.delay, ShriekParticleOption::new
    );
    private final int delay;

    public ShriekParticleOption(int delay) {
        this.delay = delay;
    }

    @Override
    public ParticleType<ShriekParticleOption> getType() {
        return ParticleTypes.SHRIEK;
    }

    public int getDelay() {
        return this.delay;
    }
}
