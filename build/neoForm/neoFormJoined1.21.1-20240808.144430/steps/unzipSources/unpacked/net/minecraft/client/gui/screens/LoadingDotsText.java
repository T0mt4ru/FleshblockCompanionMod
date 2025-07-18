package net.minecraft.client.gui.screens;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoadingDotsText {
    private static final String[] FRAMES = new String[]{"O o o", "o O o", "o o O", "o O o"};
    private static final long INTERVAL_MS = 300L;

    public static String get(long millis) {
        int i = (int)(millis / 300L % (long)FRAMES.length);
        return FRAMES[i];
    }
}
