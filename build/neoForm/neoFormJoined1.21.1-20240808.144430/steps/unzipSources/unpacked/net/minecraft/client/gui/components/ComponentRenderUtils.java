package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ComponentRenderUtils {
    private static final FormattedCharSequence INDENT = FormattedCharSequence.codepoint(32, Style.EMPTY);

    private static String stripColor(String text) {
        return Minecraft.getInstance().options.chatColors().get() ? text : ChatFormatting.stripFormatting(text);
    }

    public static List<FormattedCharSequence> wrapComponents(FormattedText component, int maxWidth, Font font) {
        ComponentCollector componentcollector = new ComponentCollector();
        component.visit((p_93997_, p_93998_) -> {
            componentcollector.append(FormattedText.of(stripColor(p_93998_), p_93997_));
            return Optional.empty();
        }, Style.EMPTY);
        List<FormattedCharSequence> list = Lists.newArrayList();
        font.getSplitter().splitLines(componentcollector.getResultOrEmpty(), maxWidth, Style.EMPTY, (p_94003_, p_94004_) -> {
            FormattedCharSequence formattedcharsequence = Language.getInstance().getVisualOrder(p_94003_);
            list.add(p_94004_ ? FormattedCharSequence.composite(INDENT, formattedcharsequence) : formattedcharsequence);
        });
        return (List<FormattedCharSequence>)(list.isEmpty() ? Lists.newArrayList(FormattedCharSequence.EMPTY) : list);
    }
}
