package net.minecraft.client;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record GuiMessageTag(int indicatorColor, @Nullable GuiMessageTag.Icon icon, @Nullable Component text, @Nullable String logTag) {
    private static final Component SYSTEM_TEXT = Component.translatable("chat.tag.system");
    private static final Component SYSTEM_TEXT_SINGLE_PLAYER = Component.translatable("chat.tag.system_single_player");
    private static final Component CHAT_NOT_SECURE_TEXT = Component.translatable("chat.tag.not_secure");
    private static final Component CHAT_MODIFIED_TEXT = Component.translatable("chat.tag.modified");
    private static final Component CHAT_ERROR_TEXT = Component.translatable("chat.tag.error");
    private static final int CHAT_NOT_SECURE_INDICATOR_COLOR = 13684944;
    private static final int CHAT_MODIFIED_INDICATOR_COLOR = 6316128;
    private static final GuiMessageTag SYSTEM = new GuiMessageTag(13684944, null, SYSTEM_TEXT, "System");
    private static final GuiMessageTag SYSTEM_SINGLE_PLAYER = new GuiMessageTag(13684944, null, SYSTEM_TEXT_SINGLE_PLAYER, "System");
    private static final GuiMessageTag CHAT_NOT_SECURE = new GuiMessageTag(13684944, null, CHAT_NOT_SECURE_TEXT, "Not Secure");
    private static final GuiMessageTag CHAT_ERROR = new GuiMessageTag(16733525, null, CHAT_ERROR_TEXT, "Chat Error");

    public static GuiMessageTag system() {
        return SYSTEM;
    }

    public static GuiMessageTag systemSinglePlayer() {
        return SYSTEM_SINGLE_PLAYER;
    }

    public static GuiMessageTag chatNotSecure() {
        return CHAT_NOT_SECURE;
    }

    public static GuiMessageTag chatModified(String originalText) {
        Component component = Component.literal(originalText).withStyle(ChatFormatting.GRAY);
        Component component1 = Component.empty().append(CHAT_MODIFIED_TEXT).append(CommonComponents.NEW_LINE).append(component);
        return new GuiMessageTag(6316128, GuiMessageTag.Icon.CHAT_MODIFIED, component1, "Modified");
    }

    public static GuiMessageTag chatError() {
        return CHAT_ERROR;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Icon {
        CHAT_MODIFIED(ResourceLocation.withDefaultNamespace("icon/chat_modified"), 9, 9);

        public final ResourceLocation sprite;
        public final int width;
        public final int height;

        private Icon(ResourceLocation sprite, int width, int height) {
            this.sprite = sprite;
            this.width = width;
            this.height = height;
        }

        public void draw(GuiGraphics guiGraphics, int x, int y) {
            guiGraphics.blitSprite(this.sprite, x, y, this.width, this.height);
        }
    }
}
