package net.minecraft.client.gui.screens.reporting;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.report.ChatReportContextBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageLink;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatSelectionLogFiller {
    private final ChatLog log;
    private final ChatReportContextBuilder contextBuilder;
    private final Predicate<LoggedChatMessage.Player> canReport;
    @Nullable
    private SignedMessageLink previousLink = null;
    private int eventId;
    private int missedCount;
    @Nullable
    private PlayerChatMessage lastMessage;

    public ChatSelectionLogFiller(ReportingContext reportingContext, Predicate<LoggedChatMessage.Player> canReport) {
        this.log = reportingContext.chatLog();
        this.contextBuilder = new ChatReportContextBuilder(reportingContext.sender().reportLimits().leadingContextMessageCount());
        this.canReport = canReport;
        this.eventId = this.log.end();
    }

    public void fillNextPage(int maxVisibleEntries, ChatSelectionLogFiller.Output output) {
        int i = 0;

        while (i < maxVisibleEntries) {
            LoggedChatEvent loggedchatevent = this.log.lookup(this.eventId);
            if (loggedchatevent == null) {
                break;
            }

            int j = this.eventId--;
            if (loggedchatevent instanceof LoggedChatMessage.Player loggedchatmessage$player && !loggedchatmessage$player.message().equals(this.lastMessage)) {
                if (this.acceptMessage(output, loggedchatmessage$player)) {
                    if (this.missedCount > 0) {
                        output.acceptDivider(Component.translatable("gui.chatSelection.fold", this.missedCount));
                        this.missedCount = 0;
                    }

                    output.acceptMessage(j, loggedchatmessage$player);
                    i++;
                } else {
                    this.missedCount++;
                }

                this.lastMessage = loggedchatmessage$player.message();
            }
        }
    }

    private boolean acceptMessage(ChatSelectionLogFiller.Output output, LoggedChatMessage.Player player) {
        PlayerChatMessage playerchatmessage = player.message();
        boolean flag = this.contextBuilder.acceptContext(playerchatmessage);
        if (this.canReport.test(player)) {
            this.contextBuilder.trackContext(playerchatmessage);
            if (this.previousLink != null && !this.previousLink.isDescendantOf(playerchatmessage.link())) {
                output.acceptDivider(Component.translatable("gui.chatSelection.join", player.profile().getName()).withStyle(ChatFormatting.YELLOW));
            }

            this.previousLink = playerchatmessage.link();
            return true;
        } else {
            return flag;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface Output {
        void acceptMessage(int chatId, LoggedChatMessage.Player playerMessage);

        void acceptDivider(Component text);
    }
}
