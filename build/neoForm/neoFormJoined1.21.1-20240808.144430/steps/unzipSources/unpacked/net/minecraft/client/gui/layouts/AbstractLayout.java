package net.minecraft.client.gui.layouts;

import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractLayout implements Layout {
    private int x;
    private int y;
    protected int width;
    protected int height;

    public AbstractLayout(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void setX(int x) {
        this.visitChildren(p_265043_ -> {
            int i = p_265043_.getX() + (x - this.getX());
            p_265043_.setX(i);
        });
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.visitChildren(p_265586_ -> {
            int i = p_265586_.getY() + (y - this.getY());
            p_265586_.setY(i);
        });
        this.y = y;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @OnlyIn(Dist.CLIENT)
    protected abstract static class AbstractChildWrapper {
        public final LayoutElement child;
        public final LayoutSettings.LayoutSettingsImpl layoutSettings;

        protected AbstractChildWrapper(LayoutElement child, LayoutSettings layoutSettings) {
            this.child = child;
            this.layoutSettings = layoutSettings.getExposed();
        }

        public int getHeight() {
            return this.child.getHeight() + this.layoutSettings.paddingTop + this.layoutSettings.paddingBottom;
        }

        public int getWidth() {
            return this.child.getWidth() + this.layoutSettings.paddingLeft + this.layoutSettings.paddingRight;
        }

        public void setX(int x, int width) {
            float f = (float)this.layoutSettings.paddingLeft;
            float f1 = (float)(width - this.child.getWidth() - this.layoutSettings.paddingRight);
            int i = (int)Mth.lerp(this.layoutSettings.xAlignment, f, f1);
            this.child.setX(i + x);
        }

        public void setY(int y, int height) {
            float f = (float)this.layoutSettings.paddingTop;
            float f1 = (float)(height - this.child.getHeight() - this.layoutSettings.paddingBottom);
            int i = Math.round(Mth.lerp(this.layoutSettings.yAlignment, f, f1));
            this.child.setY(i + y);
        }
    }
}
