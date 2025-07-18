package net.minecraft.client.gui.screens.options;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonLinks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AccessibilityOptionsScreen extends OptionsSubScreen {
    public static final Component TITLE = Component.translatable("options.accessibility.title");

    private static OptionInstance<?>[] options(Options options) {
        return new OptionInstance[]{
            options.narrator(),
            options.showSubtitles(),
            options.highContrast(),
            options.autoJump(),
            options.menuBackgroundBlurriness(),
            options.textBackgroundOpacity(),
            options.backgroundForChatOnly(),
            options.chatOpacity(),
            options.chatLineSpacing(),
            options.chatDelay(),
            options.notificationDisplayTime(),
            options.bobView(),
            options.toggleCrouch(),
            options.toggleSprint(),
            options.screenEffectScale(),
            options.fovEffectScale(),
            options.darknessEffectScale(),
            options.damageTiltStrength(),
            options.glintSpeed(),
            options.glintStrength(),
            options.hideLightningFlash(),
            options.darkMojangStudiosBackground(),
            options.panoramaSpeed(),
            options.hideSplashTexts(),
            options.narratorHotkey()
        };
    }

    public AccessibilityOptionsScreen(Screen lastScreen, Options options) {
        super(lastScreen, options, TITLE);
    }

    @Override
    protected void init() {
        super.init();
        AbstractWidget abstractwidget = this.list.findOption(this.options.highContrast());
        if (abstractwidget != null && !this.minecraft.getResourcePackRepository().getAvailableIds().contains("high_contrast")) {
            abstractwidget.active = false;
            abstractwidget.setTooltip(Tooltip.create(Component.translatable("options.accessibility.high_contrast.error.tooltip")));
        }
    }

    @Override
    protected void addOptions() {
        this.list.addSmall(options(this.options));
    }

    @Override
    protected void addFooter() {
        LinearLayout linearlayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        linearlayout.addChild(
            Button.builder(Component.translatable("options.accessibility.link"), ConfirmLinkScreen.confirmLink(this, CommonLinks.ACCESSIBILITY_HELP)).build()
        );
        linearlayout.addChild(Button.builder(CommonComponents.GUI_DONE, p_345508_ -> this.minecraft.setScreen(this.lastScreen)).build());
    }
}
