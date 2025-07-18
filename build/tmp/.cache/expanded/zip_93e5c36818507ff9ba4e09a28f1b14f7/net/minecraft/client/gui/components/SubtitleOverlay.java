package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.audio.ListenerTransform;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SubtitleOverlay implements SoundEventListener {
    private static final long DISPLAY_TIME = 3000L;
    private final Minecraft minecraft;
    private final List<SubtitleOverlay.Subtitle> subtitles = Lists.newArrayList();
    private boolean isListening;
    private final List<SubtitleOverlay.Subtitle> audibleSubtitles = new ArrayList<>();

    public SubtitleOverlay(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(GuiGraphics guiGraphics) {
        SoundManager soundmanager = this.minecraft.getSoundManager();
        if (!this.isListening && this.minecraft.options.showSubtitles().get()) {
            soundmanager.addListener(this);
            this.isListening = true;
        } else if (this.isListening && !this.minecraft.options.showSubtitles().get()) {
            soundmanager.removeListener(this);
            this.isListening = false;
        }

        if (this.isListening) {
            ListenerTransform listenertransform = soundmanager.getListenerTransform();
            Vec3 vec3 = listenertransform.position();
            Vec3 vec31 = listenertransform.forward();
            Vec3 vec32 = listenertransform.right();
            this.audibleSubtitles.clear();

            for (SubtitleOverlay.Subtitle subtitleoverlay$subtitle : this.subtitles) {
                if (subtitleoverlay$subtitle.isAudibleFrom(vec3)) {
                    this.audibleSubtitles.add(subtitleoverlay$subtitle);
                }
            }

            if (!this.audibleSubtitles.isEmpty()) {
                int i2 = 0;
                int j2 = 0;
                double d0 = this.minecraft.options.notificationDisplayTime().get();
                Iterator<SubtitleOverlay.Subtitle> iterator = this.audibleSubtitles.iterator();

                while (iterator.hasNext()) {
                    SubtitleOverlay.Subtitle subtitleoverlay$subtitle1 = iterator.next();
                    subtitleoverlay$subtitle1.purgeOldInstances(3000.0 * d0);
                    if (!subtitleoverlay$subtitle1.isStillActive()) {
                        iterator.remove();
                    } else {
                        j2 = Math.max(j2, this.minecraft.font.width(subtitleoverlay$subtitle1.getText()));
                    }
                }

                j2 += this.minecraft.font.width("<") + this.minecraft.font.width(" ") + this.minecraft.font.width(">") + this.minecraft.font.width(" ");

                for (SubtitleOverlay.Subtitle subtitleoverlay$subtitle2 : this.audibleSubtitles) {
                    int i = 255;
                    Component component = subtitleoverlay$subtitle2.getText();
                    SubtitleOverlay.SoundPlayedAt subtitleoverlay$soundplayedat = subtitleoverlay$subtitle2.getClosest(vec3);
                    if (subtitleoverlay$soundplayedat != null) {
                        Vec3 vec33 = subtitleoverlay$soundplayedat.location.subtract(vec3).normalize();
                        double d1 = vec32.dot(vec33);
                        double d2 = vec31.dot(vec33);
                        boolean flag = d2 > 0.5;
                        int j = j2 / 2;
                        int k = 9;
                        int l = k / 2;
                        float f = 1.0F;
                        int i1 = this.minecraft.font.width(component);
                        int j1 = Mth.floor(
                            Mth.clampedLerp(255.0F, 75.0F, (float)(Util.getMillis() - subtitleoverlay$soundplayedat.time) / (float)(3000.0 * d0))
                        );
                        int k1 = j1 << 16 | j1 << 8 | j1;
                        guiGraphics.pose().pushPose();
                        guiGraphics.pose()
                            .translate(
                                (float)guiGraphics.guiWidth() - (float)j * 1.0F - 2.0F, (float)(guiGraphics.guiHeight() - 35) - (float)(i2 * (k + 1)) * 1.0F, 0.0F
                            );
                        guiGraphics.pose().scale(1.0F, 1.0F, 1.0F);
                        guiGraphics.fill(-j - 1, -l - 1, j + 1, l + 1, this.minecraft.options.getBackgroundColor(0.8F));
                        int l1 = k1 + -16777216;
                        if (!flag) {
                            if (d1 > 0.0) {
                                guiGraphics.drawString(this.minecraft.font, ">", j - this.minecraft.font.width(">"), -l, l1);
                            } else if (d1 < 0.0) {
                                guiGraphics.drawString(this.minecraft.font, "<", -j, -l, l1);
                            }
                        }

                        guiGraphics.drawString(this.minecraft.font, component, -i1 / 2, -l, l1);
                        guiGraphics.pose().popPose();
                        i2++;
                    }
                }
            }
        }
    }

    @Override
    public void onPlaySound(SoundInstance sound, WeighedSoundEvents accessor, float range) {
        if (accessor.getSubtitle() != null) {
            Component component = accessor.getSubtitle();
            if (!this.subtitles.isEmpty()) {
                for (SubtitleOverlay.Subtitle subtitleoverlay$subtitle : this.subtitles) {
                    if (subtitleoverlay$subtitle.getText().equals(component)) {
                        subtitleoverlay$subtitle.refresh(new Vec3(sound.getX(), sound.getY(), sound.getZ()));
                        return;
                    }
                }
            }

            this.subtitles.add(new SubtitleOverlay.Subtitle(component, range, new Vec3(sound.getX(), sound.getY(), sound.getZ())));
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record SoundPlayedAt(Vec3 location, long time) {
    }

    @OnlyIn(Dist.CLIENT)
    static class Subtitle {
        private final Component text;
        private final float range;
        private final List<SubtitleOverlay.SoundPlayedAt> playedAt = new ArrayList<>();

        public Subtitle(Component text, float range, Vec3 location) {
            this.text = text;
            this.range = range;
            this.playedAt.add(new SubtitleOverlay.SoundPlayedAt(location, Util.getMillis()));
        }

        public Component getText() {
            return this.text;
        }

        @Nullable
        public SubtitleOverlay.SoundPlayedAt getClosest(Vec3 location) {
            if (this.playedAt.isEmpty()) {
                return null;
            } else {
                return this.playedAt.size() == 1
                    ? this.playedAt.getFirst()
                    : this.playedAt.stream().min(Comparator.comparingDouble(p_347541_ -> p_347541_.location().distanceTo(location))).orElse(null);
            }
        }

        public void refresh(Vec3 location) {
            this.playedAt.removeIf(p_347631_ -> location.equals(p_347631_.location()));
            this.playedAt.add(new SubtitleOverlay.SoundPlayedAt(location, Util.getMillis()));
        }

        public boolean isAudibleFrom(Vec3 location) {
            if (Float.isInfinite(this.range)) {
                return true;
            } else if (this.playedAt.isEmpty()) {
                return false;
            } else {
                SubtitleOverlay.SoundPlayedAt subtitleoverlay$soundplayedat = this.getClosest(location);
                return subtitleoverlay$soundplayedat == null ? false : location.closerThan(subtitleoverlay$soundplayedat.location, (double)this.range);
            }
        }

        public void purgeOldInstances(double displayTime) {
            long i = Util.getMillis();
            this.playedAt.removeIf(p_347590_ -> (double)(i - p_347590_.time()) > displayTime);
        }

        public boolean isStillActive() {
            return !this.playedAt.isEmpty();
        }
    }
}
