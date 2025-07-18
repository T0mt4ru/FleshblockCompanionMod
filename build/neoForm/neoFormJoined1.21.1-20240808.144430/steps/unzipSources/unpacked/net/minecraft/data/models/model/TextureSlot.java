package net.minecraft.data.models.model;

import javax.annotation.Nullable;

public final class TextureSlot {
    public static final TextureSlot ALL = create("all");
    public static final TextureSlot TEXTURE = create("texture", ALL);
    public static final TextureSlot PARTICLE = create("particle", TEXTURE);
    public static final TextureSlot END = create("end", ALL);
    public static final TextureSlot BOTTOM = create("bottom", END);
    public static final TextureSlot TOP = create("top", END);
    public static final TextureSlot FRONT = create("front", ALL);
    public static final TextureSlot BACK = create("back", ALL);
    public static final TextureSlot SIDE = create("side", ALL);
    public static final TextureSlot NORTH = create("north", SIDE);
    public static final TextureSlot SOUTH = create("south", SIDE);
    public static final TextureSlot EAST = create("east", SIDE);
    public static final TextureSlot WEST = create("west", SIDE);
    public static final TextureSlot UP = create("up");
    public static final TextureSlot DOWN = create("down");
    public static final TextureSlot CROSS = create("cross");
    public static final TextureSlot PLANT = create("plant");
    public static final TextureSlot WALL = create("wall", ALL);
    public static final TextureSlot RAIL = create("rail");
    public static final TextureSlot WOOL = create("wool");
    public static final TextureSlot PATTERN = create("pattern");
    public static final TextureSlot PANE = create("pane");
    public static final TextureSlot EDGE = create("edge");
    public static final TextureSlot FAN = create("fan");
    public static final TextureSlot STEM = create("stem");
    public static final TextureSlot UPPER_STEM = create("upperstem");
    public static final TextureSlot CROP = create("crop");
    public static final TextureSlot DIRT = create("dirt");
    public static final TextureSlot FIRE = create("fire");
    public static final TextureSlot LANTERN = create("lantern");
    public static final TextureSlot PLATFORM = create("platform");
    public static final TextureSlot UNSTICKY = create("unsticky");
    public static final TextureSlot TORCH = create("torch");
    public static final TextureSlot LAYER0 = create("layer0");
    public static final TextureSlot LAYER1 = create("layer1");
    public static final TextureSlot LAYER2 = create("layer2");
    public static final TextureSlot LIT_LOG = create("lit_log");
    public static final TextureSlot CANDLE = create("candle");
    public static final TextureSlot INSIDE = create("inside");
    public static final TextureSlot CONTENT = create("content");
    public static final TextureSlot INNER_TOP = create("inner_top");
    public static final TextureSlot FLOWERBED = create("flowerbed");
    private final String id;
    @Nullable
    private final TextureSlot parent;

    private static TextureSlot create(String id) {
        return new TextureSlot(id, null);
    }

    private static TextureSlot create(String id, TextureSlot parent) {
        return new TextureSlot(id, parent);
    }

    private TextureSlot(String id, @Nullable TextureSlot parent) {
        this.id = id;
        this.parent = parent;
    }

    public String getId() {
        return this.id;
    }

    @Nullable
    public TextureSlot getParent() {
        return this.parent;
    }

    @Override
    public String toString() {
        return "#" + this.id;
    }
}
