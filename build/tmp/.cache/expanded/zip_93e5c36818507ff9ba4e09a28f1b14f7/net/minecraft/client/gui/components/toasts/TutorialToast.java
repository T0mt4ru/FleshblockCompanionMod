package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TutorialToast implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/tutorial");
    public static final int PROGRESS_BAR_WIDTH = 154;
    public static final int PROGRESS_BAR_HEIGHT = 1;
    public static final int PROGRESS_BAR_X = 3;
    public static final int PROGRESS_BAR_Y = 28;
    private final TutorialToast.Icons icon;
    private final Component title;
    @Nullable
    private final Component message;
    private Toast.Visibility visibility = Toast.Visibility.SHOW;
    private long lastProgressTime;
    private float lastProgress;
    private float progress;
    private final boolean progressable;

    public TutorialToast(TutorialToast.Icons icon, Component title, @Nullable Component message, boolean progressable) {
        this.icon = icon;
        this.title = title;
        this.message = message;
        this.progressable = progressable;
    }

    @Override
    public Toast.Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        guiGraphics.blitSprite(BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
        this.icon.render(guiGraphics, 6, 6);
        if (this.message == null) {
            guiGraphics.drawString(toastComponent.getMinecraft().font, this.title, 30, 12, -11534256, false);
        } else {
            guiGraphics.drawString(toastComponent.getMinecraft().font, this.title, 30, 7, -11534256, false);
            guiGraphics.drawString(toastComponent.getMinecraft().font, this.message, 30, 18, -16777216, false);
        }

        if (this.progressable) {
            guiGraphics.fill(3, 28, 157, 29, -1);
            float f = Mth.clampedLerp(this.lastProgress, this.progress, (float)(timeSinceLastVisible - this.lastProgressTime) / 100.0F);
            int i;
            if (this.progress >= this.lastProgress) {
                i = -16755456;
            } else {
                i = -11206656;
            }

            guiGraphics.fill(3, 28, (int)(3.0F + 154.0F * f), 29, i);
            this.lastProgress = f;
            this.lastProgressTime = timeSinceLastVisible;
        }

        return this.visibility;
    }

    public void hide() {
        this.visibility = Toast.Visibility.HIDE;
    }

    public void updateProgress(float progress) {
        this.progress = progress;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Icons {
        MOVEMENT_KEYS(ResourceLocation.withDefaultNamespace("toast/movement_keys")),
        MOUSE(ResourceLocation.withDefaultNamespace("toast/mouse")),
        TREE(ResourceLocation.withDefaultNamespace("toast/tree")),
        RECIPE_BOOK(ResourceLocation.withDefaultNamespace("toast/recipe_book")),
        WOODEN_PLANKS(ResourceLocation.withDefaultNamespace("toast/wooden_planks")),
        SOCIAL_INTERACTIONS(ResourceLocation.withDefaultNamespace("toast/social_interactions")),
        RIGHT_CLICK(ResourceLocation.withDefaultNamespace("toast/right_click"));

        private final ResourceLocation sprite;

        private Icons(ResourceLocation sprite) {
            this.sprite = sprite;
        }

        public void render(GuiGraphics guiGraphics, int x, int y) {
            RenderSystem.enableBlend();
            guiGraphics.blitSprite(this.sprite, x, y, 20, 20);
        }
    }
}
