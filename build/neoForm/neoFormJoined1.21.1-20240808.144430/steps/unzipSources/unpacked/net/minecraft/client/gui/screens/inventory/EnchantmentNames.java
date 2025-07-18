package net.minecraft.client.gui.screens.inventory;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchantmentNames {
    private static final ResourceLocation ALT_FONT = ResourceLocation.withDefaultNamespace("alt");
    private static final Style ROOT_STYLE = Style.EMPTY.withFont(ALT_FONT);
    private static final EnchantmentNames INSTANCE = new EnchantmentNames();
    private final RandomSource random = RandomSource.create();
    private final String[] words = new String[]{
        "the",
        "elder",
        "scrolls",
        "klaatu",
        "berata",
        "niktu",
        "xyzzy",
        "bless",
        "curse",
        "light",
        "darkness",
        "fire",
        "air",
        "earth",
        "water",
        "hot",
        "dry",
        "cold",
        "wet",
        "ignite",
        "snuff",
        "embiggen",
        "twist",
        "shorten",
        "stretch",
        "fiddle",
        "destroy",
        "imbue",
        "galvanize",
        "enchant",
        "free",
        "limited",
        "range",
        "of",
        "towards",
        "inside",
        "sphere",
        "cube",
        "self",
        "other",
        "ball",
        "mental",
        "physical",
        "grow",
        "shrink",
        "demon",
        "elemental",
        "spirit",
        "animal",
        "creature",
        "beast",
        "humanoid",
        "undead",
        "fresh",
        "stale",
        "phnglui",
        "mglwnafh",
        "cthulhu",
        "rlyeh",
        "wgahnagl",
        "fhtagn",
        "baguette"
    };

    private EnchantmentNames() {
    }

    public static EnchantmentNames getInstance() {
        return INSTANCE;
    }

    public FormattedText getRandomName(Font fontRenderer, int maxWidth) {
        StringBuilder stringbuilder = new StringBuilder();
        int i = this.random.nextInt(2) + 3;

        for (int j = 0; j < i; j++) {
            if (j != 0) {
                stringbuilder.append(" ");
            }

            stringbuilder.append(Util.getRandom(this.words, this.random));
        }

        return fontRenderer.getSplitter().headByWidth(Component.literal(stringbuilder.toString()).withStyle(ROOT_STYLE), maxWidth, Style.EMPTY);
    }

    /**
     * Resets the underlying random number generator using a given seed.
     */
    public void initSeed(long seed) {
        this.random.setSeed(seed);
    }
}
