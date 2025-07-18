package net.minecraft.client.gui.components.toasts;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface Toast {
    Object NO_TOKEN = new Object();
    int SLOT_HEIGHT = 32;

    Toast.Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible);

    default Object getToken() {
        return NO_TOKEN;
    }

    default int width() {
        return 160;
    }

    default int height() {
        return 32;
    }

    default int slotCount() {
        return Mth.positiveCeilDiv(this.height(), 32);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Visibility {
        SHOW(SoundEvents.UI_TOAST_IN),
        HIDE(SoundEvents.UI_TOAST_OUT);

        private final SoundEvent soundEvent;

        private Visibility(SoundEvent soundEvent) {
            this.soundEvent = soundEvent;
        }

        public void playSound(SoundManager handler) {
            handler.play(SimpleSoundInstance.forUI(this.soundEvent, 1.0F, 1.0F));
        }
    }
}
