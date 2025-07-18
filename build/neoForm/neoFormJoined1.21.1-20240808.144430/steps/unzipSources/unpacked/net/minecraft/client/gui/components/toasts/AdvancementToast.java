package net.minecraft.client.gui.components.toasts;

import java.util.List;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancementToast implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/advancement");
    public static final int DISPLAY_TIME = 5000;
    private final AdvancementHolder advancement;
    private boolean playedSound;

    public AdvancementToast(AdvancementHolder advancement) {
        this.advancement = advancement;
    }

    @Override
    public Toast.Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        DisplayInfo displayinfo = this.advancement.value().display().orElse(null);
        guiGraphics.blitSprite(BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
        if (displayinfo != null) {
            List<FormattedCharSequence> list = toastComponent.getMinecraft().font.split(displayinfo.getTitle(), 125);
            int i = displayinfo.getType() == AdvancementType.CHALLENGE ? 16746751 : 16776960;
            if (list.size() == 1) {
                guiGraphics.drawString(toastComponent.getMinecraft().font, displayinfo.getType().getDisplayName(), 30, 7, i | 0xFF000000, false);
                guiGraphics.drawString(toastComponent.getMinecraft().font, list.get(0), 30, 18, -1, false);
            } else {
                int j = 1500;
                float f = 300.0F;
                if (timeSinceLastVisible < 1500L) {
                    int k = Mth.floor(Mth.clamp((float)(1500L - timeSinceLastVisible) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
                    guiGraphics.drawString(toastComponent.getMinecraft().font, displayinfo.getType().getDisplayName(), 30, 11, i | k, false);
                } else {
                    int i1 = Mth.floor(Mth.clamp((float)(timeSinceLastVisible - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
                    int l = this.height() / 2 - list.size() * 9 / 2;

                    for (FormattedCharSequence formattedcharsequence : list) {
                        guiGraphics.drawString(toastComponent.getMinecraft().font, formattedcharsequence, 30, l, 16777215 | i1, false);
                        l += 9;
                    }
                }
            }

            if (!this.playedSound && timeSinceLastVisible > 0L) {
                this.playedSound = true;
                if (displayinfo.getType() == AdvancementType.CHALLENGE) {
                    toastComponent.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
                }
            }

            guiGraphics.renderFakeItem(displayinfo.getIcon(), 8, 8);
            return (double)timeSinceLastVisible >= 5000.0 * toastComponent.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
        } else {
            return Toast.Visibility.HIDE;
        }
    }
}
