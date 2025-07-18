package com.mojang.blaze3d.font;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface SheetGlyphInfo {
    int getPixelWidth();

    int getPixelHeight();

    void upload(int xOffset, int yOffset);

    boolean isColored();

    float getOversample();

    default float getLeft() {
        return this.getBearingLeft();
    }

    default float getRight() {
        return this.getLeft() + (float)this.getPixelWidth() / this.getOversample();
    }

    default float getTop() {
        return 7.0F - this.getBearingTop();
    }

    default float getBottom() {
        return this.getTop() + (float)this.getPixelHeight() / this.getOversample();
    }

    default float getBearingLeft() {
        return 0.0F;
    }

    default float getBearingTop() {
        return 7.0F;
    }
}
