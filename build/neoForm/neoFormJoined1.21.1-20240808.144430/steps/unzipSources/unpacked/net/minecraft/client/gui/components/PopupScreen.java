package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PopupScreen extends Screen {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("popup/background");
    private static final int SPACING = 12;
    private static final int BG_BORDER_WITH_SPACING = 18;
    private static final int BUTTON_SPACING = 6;
    private static final int IMAGE_SIZE_X = 130;
    private static final int IMAGE_SIZE_Y = 64;
    private static final int POPUP_DEFAULT_WIDTH = 250;
    private final Screen backgroundScreen;
    @Nullable
    private final ResourceLocation image;
    private final Component message;
    private final List<PopupScreen.ButtonOption> buttons;
    @Nullable
    private final Runnable onClose;
    private final int contentWidth;
    private final LinearLayout layout = LinearLayout.vertical();

    PopupScreen(
        Screen backgroundScreen,
        int width,
        @Nullable ResourceLocation image,
        Component title,
        Component message,
        List<PopupScreen.ButtonOption> buttons,
        @Nullable Runnable onClose
    ) {
        super(title);
        this.backgroundScreen = backgroundScreen;
        this.image = image;
        this.message = message;
        this.buttons = buttons;
        this.onClose = onClose;
        this.contentWidth = width - 36;
    }

    @Override
    public void added() {
        super.added();
        this.backgroundScreen.clearFocus();
    }

    @Override
    protected void init() {
        this.backgroundScreen.init(this.minecraft, this.width, this.height);
        this.layout.spacing(12).defaultCellSetting().alignHorizontallyCenter();
        this.layout
            .addChild(new MultiLineTextWidget(this.title.copy().withStyle(ChatFormatting.BOLD), this.font).setMaxWidth(this.contentWidth).setCentered(true));
        if (this.image != null) {
            this.layout.addChild(ImageWidget.texture(130, 64, this.image, 130, 64));
        }

        this.layout.addChild(new MultiLineTextWidget(this.message, this.font).setMaxWidth(this.contentWidth).setCentered(true));
        this.layout.addChild(this.buildButtonRow());
        this.layout.visitWidgets(p_321353_ -> {
            AbstractWidget abstractwidget = this.addRenderableWidget(p_321353_);
        });
        this.repositionElements();
    }

    private LinearLayout buildButtonRow() {
        int i = 6 * (this.buttons.size() - 1);
        int j = Math.min((this.contentWidth - i) / this.buttons.size(), 150);
        LinearLayout linearlayout = LinearLayout.horizontal();
        linearlayout.spacing(6);

        for (PopupScreen.ButtonOption popupscreen$buttonoption : this.buttons) {
            linearlayout.addChild(
                Button.builder(popupscreen$buttonoption.message(), p_304831_ -> popupscreen$buttonoption.action().accept(this)).width(j).build()
            );
        }

        return linearlayout;
    }

    @Override
    protected void repositionElements() {
        this.backgroundScreen.resize(this.minecraft, this.width, this.height);
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.backgroundScreen.render(guiGraphics, -1, -1, partialTick);
        guiGraphics.flush();
        RenderSystem.clear(256, Minecraft.ON_OSX);
        this.renderTransparentBackground(guiGraphics);
        guiGraphics.blitSprite(BACKGROUND_SPRITE, this.layout.getX() - 18, this.layout.getY() - 18, this.layout.getWidth() + 36, this.layout.getHeight() + 36);
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(this.title, this.message);
    }

    @Override
    public void onClose() {
        if (this.onClose != null) {
            this.onClose.run();
        }

        this.minecraft.setScreen(this.backgroundScreen);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Screen backgroundScreen;
        private final Component title;
        private Component message = CommonComponents.EMPTY;
        private int width = 250;
        @Nullable
        private ResourceLocation image;
        private final List<PopupScreen.ButtonOption> buttons = new ArrayList<>();
        @Nullable
        private Runnable onClose = null;

        public Builder(Screen backgroundScreen, Component title) {
            this.backgroundScreen = backgroundScreen;
            this.title = title;
        }

        public PopupScreen.Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public PopupScreen.Builder setImage(ResourceLocation image) {
            this.image = image;
            return this;
        }

        public PopupScreen.Builder setMessage(Component message) {
            this.message = message;
            return this;
        }

        public PopupScreen.Builder addButton(Component message, Consumer<PopupScreen> action) {
            this.buttons.add(new PopupScreen.ButtonOption(message, action));
            return this;
        }

        public PopupScreen.Builder onClose(Runnable onClose) {
            this.onClose = onClose;
            return this;
        }

        public PopupScreen build() {
            if (this.buttons.isEmpty()) {
                throw new IllegalStateException("Popup must have at least one button");
            } else {
                return new PopupScreen(this.backgroundScreen, this.width, this.image, this.title, this.message, List.copyOf(this.buttons), this.onClose);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record ButtonOption(Component message, Consumer<PopupScreen> action) {
    }
}
