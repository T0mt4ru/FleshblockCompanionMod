package net.minecraft.client.gui.screens.options.controls;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.MouseSettingsScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ControlsScreen extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("controls.title");

    private static OptionInstance<?>[] options(Options options) {
        return new OptionInstance[]{options.toggleCrouch(), options.toggleSprint(), options.autoJump(), options.operatorItemsTab()};
    }

    public ControlsScreen(Screen lastScreen, Options options) {
        super(lastScreen, options, TITLE);
    }

    @Override
    protected void addOptions() {
        this.list
            .addSmall(
                Button.builder(
                        Component.translatable("options.mouse_settings"), p_345025_ -> this.minecraft.setScreen(new MouseSettingsScreen(this, this.options))
                    )
                    .build(),
                Button.builder(Component.translatable("controls.keybinds"), p_346358_ -> this.minecraft.setScreen(new KeyBindsScreen(this, this.options)))
                    .build()
            );
        this.list.addSmall(options(this.options));
    }
}
