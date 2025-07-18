package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FastColor;

public class ColorParticleOption implements ParticleOptions {
    private final ParticleType<ColorParticleOption> type;
    private final int color;

    public static MapCodec<ColorParticleOption> codec(ParticleType<ColorParticleOption> particleType) {
        return ExtraCodecs.ARGB_COLOR_CODEC.xmap(p_333828_ -> new ColorParticleOption(particleType, p_333828_), p_333908_ -> p_333908_.color).fieldOf("color");
    }

    public static StreamCodec<? super ByteBuf, ColorParticleOption> streamCodec(ParticleType<ColorParticleOption> type) {
        return ByteBufCodecs.INT.map(p_333912_ -> new ColorParticleOption(type, p_333912_), p_334072_ -> p_334072_.color);
    }

    private ColorParticleOption(ParticleType<ColorParticleOption> type, int color) {
        this.type = type;
        this.color = color;
    }

    @Override
    public ParticleType<ColorParticleOption> getType() {
        return this.type;
    }

    public float getRed() {
        return (float)FastColor.ARGB32.red(this.color) / 255.0F;
    }

    public float getGreen() {
        return (float)FastColor.ARGB32.green(this.color) / 255.0F;
    }

    public float getBlue() {
        return (float)FastColor.ARGB32.blue(this.color) / 255.0F;
    }

    public float getAlpha() {
        return (float)FastColor.ARGB32.alpha(this.color) / 255.0F;
    }

    public static ColorParticleOption create(ParticleType<ColorParticleOption> type, int color) {
        return new ColorParticleOption(type, color);
    }

    public static ColorParticleOption create(ParticleType<ColorParticleOption> type, float red, float green, float blue) {
        return create(type, FastColor.ARGB32.colorFromFloat(1.0F, red, green, blue));
    }
}
