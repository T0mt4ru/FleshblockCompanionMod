package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AlertScreen extends Screen {
    private static final int LABEL_Y = 90;
    private final Component messageText;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private final Runnable callback;
    private final Component okButton;
    private final boolean shouldCloseOnEsc;

    public AlertScreen(Runnable callback, Component title, Component text) {
        this(callback, title, text, CommonComponents.GUI_BACK, true);
    }

    public AlertScreen(Runnable callback, Component title, Component messageText, Component okButton, boolean shouldCloseOnEsc) {
        super(title);
        this.callback = callback;
        this.messageText = messageText;
        this.okButton = okButton;
        this.shouldCloseOnEsc = shouldCloseOnEsc;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), this.messageText);
    }

    @Override
    protected void init() {
        super.init();
        this.message = MultiLineLabel.create(this.font, this.messageText, this.width - 50);
        int i = this.message.getLineCount() * 9;
        int j = Mth.clamp(90 + i + 12, this.height / 6 + 96, this.height - 24);
        int k = 150;
        this.addRenderableWidget(Button.builder(this.okButton, p_95533_ -> this.callback.run()).bounds((this.width - 150) / 2, j, 150, 20).build());
    }

    /**
     * Renders the graphical user interface (GUI) element.
     *
     * @param guiGraphics the GuiGraphics object used for rendering.
     * @param mouseX      the x-coordinate of the mouse cursor.
     * @param mouseY      the y-coordinate of the mouse cursor.
     * @param partialTick the partial tick time.
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 70, 16777215);
        this.message.renderCentered(guiGraphics, this.width / 2, 90);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.shouldCloseOnEsc;
    }
}
