package com.mojang.blaze3d.audio;

import java.nio.ByteBuffer;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.openal.AL10;

/**
 * The SoundBuffer class represents an audio buffer containing audio data in a particular format.
 *
 * The audio data can be used to create an OpenAL buffer, which can be played in a 3D audio environment.
 */
@OnlyIn(Dist.CLIENT)
public class SoundBuffer {
    @Nullable
    private ByteBuffer data;
    private final AudioFormat format;
    private boolean hasAlBuffer;
    private int alBuffer;

    public SoundBuffer(ByteBuffer data, AudioFormat format) {
        this.data = data;
        this.format = format;
    }

    OptionalInt getAlBuffer() {
        if (!this.hasAlBuffer) {
            if (this.data == null) {
                return OptionalInt.empty();
            }

            int i = OpenAlUtil.audioFormatToOpenAl(this.format);
            int[] aint = new int[1];
            AL10.alGenBuffers(aint);
            if (OpenAlUtil.checkALError("Creating buffer")) {
                return OptionalInt.empty();
            }

            AL10.alBufferData(aint[0], i, this.data, (int)this.format.getSampleRate());
            if (OpenAlUtil.checkALError("Assigning buffer data")) {
                return OptionalInt.empty();
            }

            this.alBuffer = aint[0];
            this.hasAlBuffer = true;
            this.data = null;
        }

        return OptionalInt.of(this.alBuffer);
    }

    public void discardAlBuffer() {
        if (this.hasAlBuffer) {
            AL10.alDeleteBuffers(new int[]{this.alBuffer});
            if (OpenAlUtil.checkALError("Deleting stream buffers")) {
                return;
            }
        }

        this.hasAlBuffer = false;
    }

    public OptionalInt releaseAlBuffer() {
        OptionalInt optionalint = this.getAlBuffer();
        this.hasAlBuffer = false;
        return optionalint;
    }
}
