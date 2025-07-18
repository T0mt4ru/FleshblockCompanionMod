package net.minecraft.client.resources.metadata.animation;

import com.google.common.collect.Lists;
import java.util.List;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnimationMetadataSection {
    public static final AnimationMetadataSectionSerializer SERIALIZER = new AnimationMetadataSectionSerializer();
    public static final String SECTION_NAME = "animation";
    public static final int DEFAULT_FRAME_TIME = 1;
    public static final int UNKNOWN_SIZE = -1;
    public static final AnimationMetadataSection EMPTY = new AnimationMetadataSection(Lists.newArrayList(), -1, -1, 1, false) {
        @Override
        public FrameSize calculateFrameSize(int p_251622_, int p_252064_) {
            return new FrameSize(p_251622_, p_252064_);
        }
    };
    private final List<AnimationFrame> frames;
    private final int frameWidth;
    private final int frameHeight;
    private final int defaultFrameTime;
    private final boolean interpolatedFrames;

    public AnimationMetadataSection(List<AnimationFrame> frames, int frameWidth, int frameHeight, int defaultFrameTime, boolean interpolatedFrames) {
        this.frames = frames;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.defaultFrameTime = defaultFrameTime;
        this.interpolatedFrames = interpolatedFrames;
    }

    public FrameSize calculateFrameSize(int width, int height) {
        if (this.frameWidth != -1) {
            return this.frameHeight != -1 ? new FrameSize(this.frameWidth, this.frameHeight) : new FrameSize(this.frameWidth, height);
        } else if (this.frameHeight != -1) {
            return new FrameSize(width, this.frameHeight);
        } else {
            int i = Math.min(width, height);
            return new FrameSize(i, i);
        }
    }

    public int getDefaultFrameTime() {
        return this.defaultFrameTime;
    }

    public boolean isInterpolatedFrames() {
        return this.interpolatedFrames;
    }

    public void forEachFrame(AnimationMetadataSection.FrameOutput output) {
        for (AnimationFrame animationframe : this.frames) {
            output.accept(animationframe.getIndex(), animationframe.getTime(this.defaultFrameTime));
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface FrameOutput {
        void accept(int index, int time);
    }
}
