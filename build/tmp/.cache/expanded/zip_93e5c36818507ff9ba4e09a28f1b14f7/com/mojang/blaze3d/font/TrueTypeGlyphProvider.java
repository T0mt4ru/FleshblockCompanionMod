package com.mojang.blaze3d.font;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;

@OnlyIn(Dist.CLIENT)
public class TrueTypeGlyphProvider implements GlyphProvider {
    @Nullable
    private ByteBuffer fontMemory;
    @Nullable
    private FT_Face face;
    final float oversample;
    private final IntSet skip = new IntArraySet();

    public TrueTypeGlyphProvider(ByteBuffer fontMemory, FT_Face face, float size, float oversample, float shiftX, float shiftY, String skip) {
        this.fontMemory = fontMemory;
        this.face = face;
        this.oversample = oversample;
        skip.codePoints().forEach(this.skip::add);
        int i = Math.round(size * oversample);
        FreeType.FT_Set_Pixel_Sizes(face, i, i);
        float f = shiftX * oversample;
        float f1 = -shiftY * oversample;

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            FT_Vector ft_vector = FreeTypeUtil.setVector(FT_Vector.malloc(memorystack), f, f1);
            FreeType.FT_Set_Transform(face, null, ft_vector);
        }
    }

    @Nullable
    @Override
    public GlyphInfo getGlyph(int character) {
        FT_Face ft_face = this.validateFontOpen();
        if (this.skip.contains(character)) {
            return null;
        } else {
            int i = FreeType.FT_Get_Char_Index(ft_face, (long)character);
            if (i == 0) {
                return null;
            } else {
                FreeTypeUtil.assertError(FreeType.FT_Load_Glyph(ft_face, i, 4194312), "Loading glyph");
                FT_GlyphSlot ft_glyphslot = Objects.requireNonNull(ft_face.glyph(), "Glyph not initialized");
                float f = FreeTypeUtil.x(ft_glyphslot.advance());
                FT_Bitmap ft_bitmap = ft_glyphslot.bitmap();
                int j = ft_glyphslot.bitmap_left();
                int k = ft_glyphslot.bitmap_top();
                int l = ft_bitmap.width();
                int i1 = ft_bitmap.rows();
                return (GlyphInfo)(l > 0 && i1 > 0 ? new TrueTypeGlyphProvider.Glyph((float)j, (float)k, l, i1, f, i) : (GlyphInfo.SpaceGlyphInfo)() -> f / this.oversample);
            }
        }
    }

    FT_Face validateFontOpen() {
        if (this.fontMemory != null && this.face != null) {
            return this.face;
        } else {
            throw new IllegalStateException("Provider already closed");
        }
    }

    @Override
    public void close() {
        if (this.face != null) {
            synchronized (FreeTypeUtil.LIBRARY_LOCK) {
                FreeTypeUtil.checkError(FreeType.FT_Done_Face(this.face), "Deleting face");
            }

            this.face = null;
        }

        MemoryUtil.memFree(this.fontMemory);
        this.fontMemory = null;
    }

    @Override
    public IntSet getSupportedGlyphs() {
        FT_Face ft_face = this.validateFontOpen();
        IntSet intset = new IntOpenHashSet();

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            IntBuffer intbuffer = memorystack.mallocInt(1);

            for (long i = FreeType.FT_Get_First_Char(ft_face, intbuffer); intbuffer.get(0) != 0; i = FreeType.FT_Get_Next_Char(ft_face, i, intbuffer)) {
                intset.add((int)i);
            }
        }

        intset.removeAll(this.skip);
        return intset;
    }

    @OnlyIn(Dist.CLIENT)
    class Glyph implements GlyphInfo {
        final int width;
        final int height;
        final float bearingX;
        final float bearingY;
        private final float advance;
        final int index;

        Glyph(float bearingX, float bearingY, int width, int height, float advance, int index) {
            this.width = width;
            this.height = height;
            this.advance = advance / TrueTypeGlyphProvider.this.oversample;
            this.bearingX = bearingX / TrueTypeGlyphProvider.this.oversample;
            this.bearingY = bearingY / TrueTypeGlyphProvider.this.oversample;
            this.index = index;
        }

        @Override
        public float getAdvance() {
            return this.advance;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> glyphProvider) {
            return glyphProvider.apply(new SheetGlyphInfo() {
                @Override
                public int getPixelWidth() {
                    return Glyph.this.width;
                }

                @Override
                public int getPixelHeight() {
                    return Glyph.this.height;
                }

                @Override
                public float getOversample() {
                    return TrueTypeGlyphProvider.this.oversample;
                }

                @Override
                public float getBearingLeft() {
                    return Glyph.this.bearingX;
                }

                @Override
                public float getBearingTop() {
                    return Glyph.this.bearingY;
                }

                @Override
                public void upload(int p_231126_, int p_231127_) {
                    FT_Face ft_face = TrueTypeGlyphProvider.this.validateFontOpen();
                    NativeImage nativeimage = new NativeImage(NativeImage.Format.LUMINANCE, Glyph.this.width, Glyph.this.height, false);
                    if (nativeimage.copyFromFont(ft_face, Glyph.this.index)) {
                        nativeimage.upload(0, p_231126_, p_231127_, 0, 0, Glyph.this.width, Glyph.this.height, false, true);
                    } else {
                        nativeimage.close();
                    }
                }

                @Override
                public boolean isColored() {
                    return false;
                }
            });
        }
    }
}
