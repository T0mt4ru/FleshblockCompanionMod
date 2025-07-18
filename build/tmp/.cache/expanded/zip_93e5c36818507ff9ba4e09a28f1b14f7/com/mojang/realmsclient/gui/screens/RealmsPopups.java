package com.mojang.realmsclient.gui.screens;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsPopups {
    private static final int COLOR_INFO = 8226750;
    private static final Component INFO = Component.translatable("mco.info").withColor(8226750);
    private static final Component WARNING = Component.translatable("mco.warning").withColor(-65536);

    public static PopupScreen infoPopupScreen(Screen backgroundScreen, Component message, Consumer<PopupScreen> onContinue) {
        return new PopupScreen.Builder(backgroundScreen, INFO)
            .setMessage(message)
            .addButton(CommonComponents.GUI_CONTINUE, onContinue)
            .addButton(CommonComponents.GUI_CANCEL, PopupScreen::onClose)
            .build();
    }

    public static PopupScreen warningPopupScreen(Screen backgroundScreen, Component message, Consumer<PopupScreen> onContinue) {
        return new PopupScreen.Builder(backgroundScreen, WARNING)
            .setMessage(message)
            .addButton(CommonComponents.GUI_CONTINUE, onContinue)
            .addButton(CommonComponents.GUI_CANCEL, PopupScreen::onClose)
            .build();
    }

    public static PopupScreen warningAcknowledgePopupScreen(Screen backgroundScreen, Component message, Consumer<PopupScreen> onContinue) {
        return new PopupScreen.Builder(backgroundScreen, WARNING).setMessage(message).addButton(CommonComponents.GUI_OK, onContinue).build();
    }
}
