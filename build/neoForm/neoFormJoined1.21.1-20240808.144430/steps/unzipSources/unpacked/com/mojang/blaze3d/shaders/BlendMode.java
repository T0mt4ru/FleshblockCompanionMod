package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Locale;
import javax.annotation.Nullable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlendMode {
    @Nullable
    private static BlendMode lastApplied;
    private final int srcColorFactor;
    private final int srcAlphaFactor;
    private final int dstColorFactor;
    private final int dstAlphaFactor;
    private final int blendFunc;
    private final boolean separateBlend;
    private final boolean opaque;

    private BlendMode(boolean separateBlend, boolean opaque, int srcColorFactor, int dstColorFactor, int srcAlphaFactor, int dstAlphaFactor, int blendFunc) {
        this.separateBlend = separateBlend;
        this.srcColorFactor = srcColorFactor;
        this.dstColorFactor = dstColorFactor;
        this.srcAlphaFactor = srcAlphaFactor;
        this.dstAlphaFactor = dstAlphaFactor;
        this.opaque = opaque;
        this.blendFunc = blendFunc;
    }

    public BlendMode() {
        this(false, true, 1, 0, 1, 0, 32774);
    }

    public BlendMode(int srcFactor, int dstFactor, int blendFunc) {
        this(false, false, srcFactor, dstFactor, srcFactor, dstFactor, blendFunc);
    }

    public BlendMode(int srcColorFactor, int dstColorFactor, int srcAlphaFactor, int dstAlphaFactor, int blendFunc) {
        this(true, false, srcColorFactor, dstColorFactor, srcAlphaFactor, dstAlphaFactor, blendFunc);
    }

    public void apply() {
        if (!this.equals(lastApplied)) {
            if (lastApplied == null || this.opaque != lastApplied.isOpaque()) {
                lastApplied = this;
                if (this.opaque) {
                    RenderSystem.disableBlend();
                    return;
                }

                RenderSystem.enableBlend();
            }

            RenderSystem.blendEquation(this.blendFunc);
            if (this.separateBlend) {
                RenderSystem.blendFuncSeparate(this.srcColorFactor, this.dstColorFactor, this.srcAlphaFactor, this.dstAlphaFactor);
            } else {
                RenderSystem.blendFunc(this.srcColorFactor, this.dstColorFactor);
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof BlendMode blendmode)) {
            return false;
        } else if (this.blendFunc != blendmode.blendFunc) {
            return false;
        } else if (this.dstAlphaFactor != blendmode.dstAlphaFactor) {
            return false;
        } else if (this.dstColorFactor != blendmode.dstColorFactor) {
            return false;
        } else if (this.opaque != blendmode.opaque) {
            return false;
        } else if (this.separateBlend != blendmode.separateBlend) {
            return false;
        } else {
            return this.srcAlphaFactor != blendmode.srcAlphaFactor ? false : this.srcColorFactor == blendmode.srcColorFactor;
        }
    }

    @Override
    public int hashCode() {
        int i = this.srcColorFactor;
        i = 31 * i + this.srcAlphaFactor;
        i = 31 * i + this.dstColorFactor;
        i = 31 * i + this.dstAlphaFactor;
        i = 31 * i + this.blendFunc;
        i = 31 * i + (this.separateBlend ? 1 : 0);
        return 31 * i + (this.opaque ? 1 : 0);
    }

    public boolean isOpaque() {
        return this.opaque;
    }

    /**
     * Converts a blend function name to an id, returning add (32774) if not recognized.
     */
    public static int stringToBlendFunc(String funcName) {
        String s = funcName.trim().toLowerCase(Locale.ROOT);
        if ("add".equals(s)) {
            return 32774;
        } else if ("subtract".equals(s)) {
            return 32778;
        } else if ("reversesubtract".equals(s)) {
            return 32779;
        } else if ("reverse_subtract".equals(s)) {
            return 32779;
        } else if ("min".equals(s)) {
            return 32775;
        } else {
            return "max".equals(s) ? 32776 : 32774;
        }
    }

    public static int stringToBlendFactor(String factorName) {
        String s = factorName.trim().toLowerCase(Locale.ROOT);
        s = s.replaceAll("_", "");
        s = s.replaceAll("one", "1");
        s = s.replaceAll("zero", "0");
        s = s.replaceAll("minus", "-");
        if ("0".equals(s)) {
            return 0;
        } else if ("1".equals(s)) {
            return 1;
        } else if ("srccolor".equals(s)) {
            return 768;
        } else if ("1-srccolor".equals(s)) {
            return 769;
        } else if ("dstcolor".equals(s)) {
            return 774;
        } else if ("1-dstcolor".equals(s)) {
            return 775;
        } else if ("srcalpha".equals(s)) {
            return 770;
        } else if ("1-srcalpha".equals(s)) {
            return 771;
        } else if ("dstalpha".equals(s)) {
            return 772;
        } else {
            return "1-dstalpha".equals(s) ? 773 : -1;
        }
    }
}
