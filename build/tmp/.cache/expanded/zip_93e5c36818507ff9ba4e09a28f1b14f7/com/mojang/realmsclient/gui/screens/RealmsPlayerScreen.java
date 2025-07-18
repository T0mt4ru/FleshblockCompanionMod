package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsUtil;
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
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsPlayerScreen extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.translatable("mco.configure.world.players.title");
    static final Component QUESTION_TITLE = Component.translatable("mco.question");
    private static final int PADDING = 8;
    final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final RealmsConfigureWorldScreen lastScreen;
    final RealmsServer serverData;
    @Nullable
    private RealmsPlayerScreen.InvitedObjectSelectionList invitedList;
    boolean stateChanged;

    public RealmsPlayerScreen(RealmsConfigureWorldScreen lastScreen, RealmsServer serverData) {
        super(TITLE);
        this.lastScreen = lastScreen;
        this.serverData = serverData;
    }

    @Override
    public void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        this.invitedList = this.layout.addToContents(new RealmsPlayerScreen.InvitedObjectSelectionList());
        this.repopulateInvitedList();
        LinearLayout linearlayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        linearlayout.addChild(
            Button.builder(
                    Component.translatable("mco.configure.world.buttons.invite"),
                    p_280732_ -> this.minecraft.setScreen(new RealmsInviteScreen(this.lastScreen, this, this.serverData))
                )
                .build()
        );
        linearlayout.addChild(Button.builder(CommonComponents.GUI_BACK, p_329657_ -> this.onClose()).build());
        this.layout.visitWidgets(p_329656_ -> {
            AbstractWidget abstractwidget = this.addRenderableWidget(p_329656_);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.invitedList != null) {
            this.invitedList.updateSize(this.width, this.layout);
        }
    }

    void repopulateInvitedList() {
        if (this.invitedList != null) {
            this.invitedList.children().clear();

            for (PlayerInfo playerinfo : this.serverData.players) {
                this.invitedList.children().add(new RealmsPlayerScreen.Entry(playerinfo));
            }
        }
    }

    @Override
    public void onClose() {
        this.backButtonClicked();
    }

    private void backButtonClicked() {
        if (this.stateChanged) {
            this.minecraft.setScreen(this.lastScreen.getNewScreen());
        } else {
            this.minecraft.setScreen(this.lastScreen);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ContainerObjectSelectionList.Entry<RealmsPlayerScreen.Entry> {
        private static final Component NORMAL_USER_TEXT = Component.translatable("mco.configure.world.invites.normal.tooltip");
        private static final Component OP_TEXT = Component.translatable("mco.configure.world.invites.ops.tooltip");
        private static final Component REMOVE_TEXT = Component.translatable("mco.configure.world.invites.remove.tooltip");
        private static final ResourceLocation MAKE_OP_SPRITE = ResourceLocation.withDefaultNamespace("player_list/make_operator");
        private static final ResourceLocation REMOVE_OP_SPRITE = ResourceLocation.withDefaultNamespace("player_list/remove_operator");
        private static final ResourceLocation REMOVE_PLAYER_SPRITE = ResourceLocation.withDefaultNamespace("player_list/remove_player");
        private static final int ICON_WIDTH = 8;
        private static final int ICON_HEIGHT = 7;
        private final PlayerInfo playerInfo;
        private final Button removeButton;
        private final Button makeOpButton;
        private final Button removeOpButton;

        public Entry(PlayerInfo playerInfo) {
            this.playerInfo = playerInfo;
            int i = RealmsPlayerScreen.this.serverData.players.indexOf(this.playerInfo);
            this.makeOpButton = SpriteIconButton.builder(NORMAL_USER_TEXT, p_329669_ -> this.op(i), false)
                .sprite(MAKE_OP_SPRITE, 8, 7)
                .width(16 + RealmsPlayerScreen.this.font.width(NORMAL_USER_TEXT))
                .narration(
                    p_329661_ -> CommonComponents.joinForNarration(
                            Component.translatable("mco.invited.player.narration", playerInfo.getName()),
                            p_329661_.get(),
                            Component.translatable("narration.cycle_button.usage.focused", OP_TEXT)
                        )
                )
                .build();
            this.removeOpButton = SpriteIconButton.builder(OP_TEXT, p_329663_ -> this.deop(i), false)
                .sprite(REMOVE_OP_SPRITE, 8, 7)
                .width(16 + RealmsPlayerScreen.this.font.width(OP_TEXT))
                .narration(
                    p_329659_ -> CommonComponents.joinForNarration(
                            Component.translatable("mco.invited.player.narration", playerInfo.getName()),
                            p_329659_.get(),
                            Component.translatable("narration.cycle_button.usage.focused", NORMAL_USER_TEXT)
                        )
                )
                .build();
            this.removeButton = SpriteIconButton.builder(REMOVE_TEXT, p_329665_ -> this.uninvite(i), false)
                .sprite(REMOVE_PLAYER_SPRITE, 8, 7)
                .width(16 + RealmsPlayerScreen.this.font.width(REMOVE_TEXT))
                .narration(
                    p_329667_ -> CommonComponents.joinForNarration(Component.translatable("mco.invited.player.narration", playerInfo.getName()), p_329667_.get())
                )
                .build();
            this.updateOpButtons();
        }

        private void op(int index) {
            RealmsClient realmsclient = RealmsClient.create();
            UUID uuid = RealmsPlayerScreen.this.serverData.players.get(index).getUuid();

            try {
                this.updateOps(realmsclient.op(RealmsPlayerScreen.this.serverData.id, uuid));
            } catch (RealmsServiceException realmsserviceexception) {
                RealmsPlayerScreen.LOGGER.error("Couldn't op the user", (Throwable)realmsserviceexception);
            }

            this.updateOpButtons();
        }

        private void deop(int index) {
            RealmsClient realmsclient = RealmsClient.create();
            UUID uuid = RealmsPlayerScreen.this.serverData.players.get(index).getUuid();

            try {
                this.updateOps(realmsclient.deop(RealmsPlayerScreen.this.serverData.id, uuid));
            } catch (RealmsServiceException realmsserviceexception) {
                RealmsPlayerScreen.LOGGER.error("Couldn't deop the user", (Throwable)realmsserviceexception);
            }

            this.updateOpButtons();
        }

        private void uninvite(int index) {
            if (index >= 0 && index < RealmsPlayerScreen.this.serverData.players.size()) {
                PlayerInfo playerinfo = RealmsPlayerScreen.this.serverData.players.get(index);
                RealmsConfirmScreen realmsconfirmscreen = new RealmsConfirmScreen(p_335150_ -> {
                    if (p_335150_) {
                        RealmsClient realmsclient = RealmsClient.create();

                        try {
                            realmsclient.uninvite(RealmsPlayerScreen.this.serverData.id, playerinfo.getUuid());
                        } catch (RealmsServiceException realmsserviceexception) {
                            RealmsPlayerScreen.LOGGER.error("Couldn't uninvite user", (Throwable)realmsserviceexception);
                        }

                        RealmsPlayerScreen.this.serverData.players.remove(index);
                        RealmsPlayerScreen.this.repopulateInvitedList();
                    }

                    RealmsPlayerScreen.this.stateChanged = true;
                    RealmsPlayerScreen.this.minecraft.setScreen(RealmsPlayerScreen.this);
                }, RealmsPlayerScreen.QUESTION_TITLE, Component.translatable("mco.configure.world.uninvite.player", playerinfo.getName()));
                RealmsPlayerScreen.this.minecraft.setScreen(realmsconfirmscreen);
            }
        }

        private void updateOps(Ops ops) {
            for (PlayerInfo playerinfo : RealmsPlayerScreen.this.serverData.players) {
                playerinfo.setOperator(ops.ops.contains(playerinfo.getName()));
            }
        }

        private void updateOpButtons() {
            this.makeOpButton.visible = !this.playerInfo.isOperator();
            this.removeOpButton.visible = !this.makeOpButton.visible;
        }

        private Button activeOpButton() {
            return this.makeOpButton.visible ? this.makeOpButton : this.removeOpButton;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.activeOpButton(), this.removeButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.activeOpButton(), this.removeButton);
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
            int i;
            if (!this.playerInfo.getAccepted()) {
                i = -6250336;
            } else if (this.playerInfo.getOnline()) {
                i = 8388479;
            } else {
                i = -1;
            }

            int j = top + height / 2 - 16;
            RealmsUtil.renderPlayerFace(guiGraphics, left, j, 32, this.playerInfo.getUuid());
            int k = top + height / 2 - 9 / 2;
            guiGraphics.drawString(RealmsPlayerScreen.this.font, this.playerInfo.getName(), left + 8 + 32, k, i, false);
            int l = top + height / 2 - 10;
            int i1 = left + width - this.removeButton.getWidth();
            this.removeButton.setPosition(i1, l);
            this.removeButton.render(guiGraphics, mouseX, mouseY, partialTick);
            int j1 = i1 - this.activeOpButton().getWidth() - 8;
            this.makeOpButton.setPosition(j1, l);
            this.makeOpButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.removeOpButton.setPosition(j1, l);
            this.removeOpButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class InvitedObjectSelectionList extends ContainerObjectSelectionList<RealmsPlayerScreen.Entry> {
        private static final int ITEM_HEIGHT = 36;

        public InvitedObjectSelectionList() {
            super(
                Minecraft.getInstance(),
                RealmsPlayerScreen.this.width,
                RealmsPlayerScreen.this.layout.getContentHeight(),
                RealmsPlayerScreen.this.layout.getHeaderHeight(),
                36
            );
            this.setRenderHeader(true, (int)(9.0F * 1.5F));
        }

        @Override
        protected void renderHeader(GuiGraphics p_331651_, int p_330785_, int p_331045_) {
            String s = RealmsPlayerScreen.this.serverData.players != null ? Integer.toString(RealmsPlayerScreen.this.serverData.players.size()) : "0";
            Component component = Component.translatable("mco.configure.world.invited.number", s).withStyle(ChatFormatting.UNDERLINE);
            p_331651_.drawString(
                RealmsPlayerScreen.this.font,
                component,
                p_330785_ + this.getRowWidth() / 2 - RealmsPlayerScreen.this.font.width(component) / 2,
                p_331045_,
                -1,
                false
            );
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * this.itemHeight + this.headerHeight;
        }

        @Override
        public int getRowWidth() {
            return 300;
        }
    }
}
