package net.minecraft.client.gui.screens.social;

import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.reporting.ReportPlayerScreen;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerEntry extends ContainerObjectSelectionList.Entry<PlayerEntry> {
    private static final ResourceLocation DRAFT_REPORT_SPRITE = ResourceLocation.withDefaultNamespace("icon/draft_report");
    private static final Duration TOOLTIP_DELAY = Duration.ofMillis(500L);
    private static final WidgetSprites REPORT_BUTTON_SPRITES = new WidgetSprites(
        ResourceLocation.withDefaultNamespace("social_interactions/report_button"),
        ResourceLocation.withDefaultNamespace("social_interactions/report_button_disabled"),
        ResourceLocation.withDefaultNamespace("social_interactions/report_button_highlighted")
    );
    private static final WidgetSprites MUTE_BUTTON_SPRITES = new WidgetSprites(
        ResourceLocation.withDefaultNamespace("social_interactions/mute_button"),
        ResourceLocation.withDefaultNamespace("social_interactions/mute_button_highlighted")
    );
    private static final WidgetSprites UNMUTE_BUTTON_SPRITES = new WidgetSprites(
        ResourceLocation.withDefaultNamespace("social_interactions/unmute_button"),
        ResourceLocation.withDefaultNamespace("social_interactions/unmute_button_highlighted")
    );
    private final Minecraft minecraft;
    private final List<AbstractWidget> children;
    private final UUID id;
    private final String playerName;
    private final Supplier<PlayerSkin> skinGetter;
    private boolean isRemoved;
    private boolean hasRecentMessages;
    private final boolean reportingEnabled;
    private final boolean hasDraftReport;
    private final boolean chatReportable;
    @Nullable
    private Button hideButton;
    @Nullable
    private Button showButton;
    @Nullable
    private Button reportButton;
    private float tooltipHoverTime;
    private static final Component HIDDEN = Component.translatable("gui.socialInteractions.status_hidden").withStyle(ChatFormatting.ITALIC);
    private static final Component BLOCKED = Component.translatable("gui.socialInteractions.status_blocked").withStyle(ChatFormatting.ITALIC);
    private static final Component OFFLINE = Component.translatable("gui.socialInteractions.status_offline").withStyle(ChatFormatting.ITALIC);
    private static final Component HIDDEN_OFFLINE = Component.translatable("gui.socialInteractions.status_hidden_offline").withStyle(ChatFormatting.ITALIC);
    private static final Component BLOCKED_OFFLINE = Component.translatable("gui.socialInteractions.status_blocked_offline").withStyle(ChatFormatting.ITALIC);
    private static final Component REPORT_DISABLED_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report.disabled");
    private static final Component HIDE_TEXT_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.hide");
    private static final Component SHOW_TEXT_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.show");
    private static final Component REPORT_PLAYER_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report");
    private static final int SKIN_SIZE = 24;
    private static final int PADDING = 4;
    public static final int SKIN_SHADE = FastColor.ARGB32.color(190, 0, 0, 0);
    private static final int CHAT_TOGGLE_ICON_SIZE = 20;
    public static final int BG_FILL = FastColor.ARGB32.color(255, 74, 74, 74);
    public static final int BG_FILL_REMOVED = FastColor.ARGB32.color(255, 48, 48, 48);
    public static final int PLAYERNAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);
    public static final int PLAYER_STATUS_COLOR = FastColor.ARGB32.color(140, 255, 255, 255);

    public PlayerEntry(
        Minecraft minecraft, SocialInteractionsScreen socialInteractionsScreen, UUID id, String playerName, Supplier<PlayerSkin> skinGetter, boolean playerReportable
    ) {
        this.minecraft = minecraft;
        this.id = id;
        this.playerName = playerName;
        this.skinGetter = skinGetter;
        ReportingContext reportingcontext = minecraft.getReportingContext();
        this.reportingEnabled = reportingcontext.sender().isEnabled();
        this.chatReportable = playerReportable;
        this.hasDraftReport = reportingcontext.hasDraftReportFor(id);
        Component component = Component.translatable("gui.socialInteractions.narration.hide", playerName);
        Component component1 = Component.translatable("gui.socialInteractions.narration.show", playerName);
        PlayerSocialManager playersocialmanager = minecraft.getPlayerSocialManager();
        boolean flag = minecraft.getChatStatus().isChatAllowed(minecraft.isLocalServer());
        boolean flag1 = !minecraft.player.getUUID().equals(id);
        if (flag1 && flag && !playersocialmanager.isBlocked(id)) {
            this.reportButton = new ImageButton(
                0,
                0,
                20,
                20,
                REPORT_BUTTON_SPRITES,
                p_238875_ -> reportingcontext.draftReportHandled(
                        minecraft, socialInteractionsScreen, () -> minecraft.setScreen(new ReportPlayerScreen(socialInteractionsScreen, reportingcontext, this)), false
                    ),
                Component.translatable("gui.socialInteractions.report")
            ) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };
            this.reportButton.active = this.reportingEnabled;
            this.reportButton.setTooltip(this.createReportButtonTooltip());
            this.reportButton.setTooltipDelay(TOOLTIP_DELAY);
            this.hideButton = new ImageButton(0, 0, 20, 20, MUTE_BUTTON_SPRITES, p_100612_ -> {
                playersocialmanager.hidePlayer(id);
                this.onHiddenOrShown(true, Component.translatable("gui.socialInteractions.hidden_in_chat", playerName));
            }, Component.translatable("gui.socialInteractions.hide")) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };
            this.hideButton.setTooltip(Tooltip.create(HIDE_TEXT_TOOLTIP, component));
            this.hideButton.setTooltipDelay(TOOLTIP_DELAY);
            this.showButton = new ImageButton(0, 0, 20, 20, UNMUTE_BUTTON_SPRITES, p_170074_ -> {
                playersocialmanager.showPlayer(id);
                this.onHiddenOrShown(false, Component.translatable("gui.socialInteractions.shown_in_chat", playerName));
            }, Component.translatable("gui.socialInteractions.show")) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };
            this.showButton.setTooltip(Tooltip.create(SHOW_TEXT_TOOLTIP, component1));
            this.showButton.setTooltipDelay(TOOLTIP_DELAY);
            this.children = new ArrayList<>();
            this.children.add(this.hideButton);
            this.children.add(this.reportButton);
            this.updateHideAndShowButton(playersocialmanager.isHidden(this.id));
        } else {
            this.children = ImmutableList.of();
        }
    }

    private Tooltip createReportButtonTooltip() {
        return !this.reportingEnabled
            ? Tooltip.create(REPORT_DISABLED_TOOLTIP)
            : Tooltip.create(REPORT_PLAYER_TOOLTIP, Component.translatable("gui.socialInteractions.narration.report", this.playerName));
    }

    @Override
    public void render(
        GuiGraphics guiGraphics,
        int index,
        int top,
        int left,
        int width,
        int height,
        int mouseX,
        int mouseY,
        boolean hovering,
        float partialTick
    ) {
        int i = left + 4;
        int j = top + (height - 24) / 2;
        int k = i + 24 + 4;
        Component component = this.getStatusComponent();
        int l;
        if (component == CommonComponents.EMPTY) {
            guiGraphics.fill(left, top, left + width, top + height, BG_FILL);
            l = top + (height - 9) / 2;
        } else {
            guiGraphics.fill(left, top, left + width, top + height, BG_FILL_REMOVED);
            l = top + (height - (9 + 9)) / 2;
            guiGraphics.drawString(this.minecraft.font, component, k, l + 12, PLAYER_STATUS_COLOR, false);
        }

        PlayerFaceRenderer.draw(guiGraphics, this.skinGetter.get(), i, j, 24);
        guiGraphics.drawString(this.minecraft.font, this.playerName, k, l, PLAYERNAME_COLOR, false);
        if (this.isRemoved) {
            guiGraphics.fill(i, j, i + 24, j + 24, SKIN_SHADE);
        }

        if (this.hideButton != null && this.showButton != null && this.reportButton != null) {
            float f = this.tooltipHoverTime;
            this.hideButton.setX(left + (width - this.hideButton.getWidth() - 4) - 20 - 4);
            this.hideButton.setY(top + (height - this.hideButton.getHeight()) / 2);
            this.hideButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.showButton.setX(left + (width - this.showButton.getWidth() - 4) - 20 - 4);
            this.showButton.setY(top + (height - this.showButton.getHeight()) / 2);
            this.showButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.reportButton.setX(left + (width - this.showButton.getWidth() - 4));
            this.reportButton.setY(top + (height - this.showButton.getHeight()) / 2);
            this.reportButton.render(guiGraphics, mouseX, mouseY, partialTick);
            if (f == this.tooltipHoverTime) {
                this.tooltipHoverTime = 0.0F;
            }
        }

        if (this.hasDraftReport && this.reportButton != null) {
            guiGraphics.blitSprite(DRAFT_REPORT_SPRITE, this.reportButton.getX() + 5, this.reportButton.getY() + 1, 15, 15);
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return this.children;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public UUID getPlayerId() {
        return this.id;
    }

    public Supplier<PlayerSkin> getSkinGetter() {
        return this.skinGetter;
    }

    public void setRemoved(boolean isRemoved) {
        this.isRemoved = isRemoved;
    }

    public boolean isRemoved() {
        return this.isRemoved;
    }

    public void setHasRecentMessages(boolean hasRecentMessages) {
        this.hasRecentMessages = hasRecentMessages;
    }

    public boolean hasRecentMessages() {
        return this.hasRecentMessages;
    }

    public boolean isChatReportable() {
        return this.chatReportable;
    }

    private void onHiddenOrShown(boolean visible, Component message) {
        this.updateHideAndShowButton(visible);
        this.minecraft.gui.getChat().addMessage(message);
        this.minecraft.getNarrator().sayNow(message);
    }

    private void updateHideAndShowButton(boolean visible) {
        this.showButton.visible = visible;
        this.hideButton.visible = !visible;
        this.children.set(0, visible ? this.showButton : this.hideButton);
    }

    MutableComponent getEntryNarationMessage(MutableComponent p_component) {
        Component component = this.getStatusComponent();
        return component == CommonComponents.EMPTY
            ? Component.literal(this.playerName).append(", ").append(p_component)
            : Component.literal(this.playerName).append(", ").append(component).append(", ").append(p_component);
    }

    private Component getStatusComponent() {
        boolean flag = this.minecraft.getPlayerSocialManager().isHidden(this.id);
        boolean flag1 = this.minecraft.getPlayerSocialManager().isBlocked(this.id);
        if (flag1 && this.isRemoved) {
            return BLOCKED_OFFLINE;
        } else if (flag && this.isRemoved) {
            return HIDDEN_OFFLINE;
        } else if (flag1) {
            return BLOCKED;
        } else if (flag) {
            return HIDDEN;
        } else {
            return this.isRemoved ? OFFLINE : CommonComponents.EMPTY;
        }
    }
}
