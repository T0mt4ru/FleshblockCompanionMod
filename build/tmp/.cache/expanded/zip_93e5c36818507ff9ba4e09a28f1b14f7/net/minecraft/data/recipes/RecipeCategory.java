package net.minecraft.data.recipes;

public enum RecipeCategory {
    BUILDING_BLOCKS("building_blocks"),
    DECORATIONS("decorations"),
    REDSTONE("redstone"),
    TRANSPORTATION("transportation"),
    TOOLS("tools"),
    COMBAT("combat"),
    FOOD("food"),
    BREWING("brewing"),
    MISC("misc");

    private final String recipeFolderName;

    private RecipeCategory(String recipeFolderName) {
        this.recipeFolderName = recipeFolderName;
    }

    public String getFolderName() {
        return this.recipeFolderName;
    }
}
