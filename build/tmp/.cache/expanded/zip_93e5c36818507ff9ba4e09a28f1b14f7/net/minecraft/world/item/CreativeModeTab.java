package net.minecraft.world.item;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ItemLike;

public class CreativeModeTab {
    private static final net.minecraft.resources.ResourceLocation SCROLLER_SPRITE = net.minecraft.resources.ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
    private static final net.minecraft.resources.ResourceLocation SCROLLER_DISABLED_SPRITE = net.minecraft.resources.ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");
    static final ResourceLocation DEFAULT_BACKGROUND = createTextureLocation("items");
    private final Component displayName;
    ResourceLocation backgroundTexture = DEFAULT_BACKGROUND;
    boolean canScroll = true;
    boolean showTitle = true;
    boolean alignedRight = false;
    private final CreativeModeTab.Row row;
    private final int column;
    private final CreativeModeTab.Type type;
    @Nullable
    private ItemStack iconItemStack;
    private Collection<ItemStack> displayItems = ItemStackLinkedSet.createTypeAndComponentsSet();
    private Set<ItemStack> displayItemsSearchTab = ItemStackLinkedSet.createTypeAndComponentsSet();
    private final Supplier<ItemStack> iconGenerator;
    private final CreativeModeTab.DisplayItemsGenerator displayItemsGenerator;
    @Nullable
    private final net.minecraft.resources.ResourceLocation scrollerSpriteLocation;
    private final boolean hasSearchBar;
    private final int searchBarWidth;
    private final net.minecraft.resources.ResourceLocation tabsImage;
    private final int labelColor;
    private final int slotColor;
    public final java.util.List<net.minecraft.resources.ResourceLocation> tabsBefore;
    public final java.util.List<net.minecraft.resources.ResourceLocation> tabsAfter;

    CreativeModeTab(
        CreativeModeTab.Row row,
        int column,
        CreativeModeTab.Type type,
        Component displayName,
        Supplier<ItemStack> iconGenerator,
        CreativeModeTab.DisplayItemsGenerator displayItemGenerator,
        net.minecraft.resources.ResourceLocation scrollerSpriteLocation,
        boolean hasSearchBar,
        int searchBarWidth,
        net.minecraft.resources.ResourceLocation tabsImage,
        int labelColor,
        int slotColor,
        java.util.List<net.minecraft.resources.ResourceLocation> tabsBefore,
        java.util.List<net.minecraft.resources.ResourceLocation> tabsAfter
    ) {
        this.row = row;
        this.column = column;
        this.displayName = displayName;
        this.iconGenerator = iconGenerator;
        this.displayItemsGenerator = displayItemGenerator;
        this.type = type;
        this.scrollerSpriteLocation = scrollerSpriteLocation;
        this.hasSearchBar = hasSearchBar;
        this.searchBarWidth = searchBarWidth;
        this.tabsImage = tabsImage;
        this.labelColor = labelColor;
        this.slotColor = slotColor;
        this.tabsBefore = java.util.List.copyOf(tabsBefore);
        this.tabsAfter = java.util.List.copyOf(tabsAfter);
    }

    protected CreativeModeTab(CreativeModeTab.Builder builder) {
        this(builder.row, builder.column, builder.type, builder.displayName, builder.iconGenerator, builder.displayItemsGenerator, builder.spriteScrollerLocation, builder.hasSearchBar, builder.searchBarWidth, builder.tabsImage, builder.labelColor, builder.slotColor, builder.tabsBefore, builder.tabsAfter);
    }

    public static CreativeModeTab.Builder builder() {
        return new CreativeModeTab.Builder(Row.TOP, 0);
    }

    public static ResourceLocation createTextureLocation(String name) {
        return ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tab_" + name + ".png");
    }

    /**
 * @deprecated Forge: use {@link #builder()}
 */ @Deprecated
    public static CreativeModeTab.Builder builder(CreativeModeTab.Row row, int column) {
        return new CreativeModeTab.Builder(row, column);
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public ItemStack getIconItem() {
        if (this.iconItemStack == null) {
            this.iconItemStack = this.iconGenerator.get();
        }

        return this.iconItemStack;
    }

    public ResourceLocation getBackgroundTexture() {
        return this.backgroundTexture;
    }

    public boolean showTitle() {
        return this.showTitle;
    }

    public boolean canScroll() {
        return this.canScroll;
    }

    public int column() {
        return this.column;
    }

    public CreativeModeTab.Row row() {
        return this.row;
    }

    public boolean hasAnyItems() {
        return !this.displayItems.isEmpty();
    }

    public boolean shouldDisplay() {
        return this.type != CreativeModeTab.Type.CATEGORY || this.hasAnyItems();
    }

    public boolean isAlignedRight() {
        return this.alignedRight;
    }

    public CreativeModeTab.Type getType() {
        return this.type;
    }

    public void buildContents(CreativeModeTab.ItemDisplayParameters parameters) {
        CreativeModeTab.ItemDisplayBuilder creativemodetab$itemdisplaybuilder = new CreativeModeTab.ItemDisplayBuilder(this, parameters.enabledFeatures);
        ResourceKey<CreativeModeTab> resourcekey = BuiltInRegistries.CREATIVE_MODE_TAB
            .getResourceKey(this)
            .orElseThrow(() -> new IllegalStateException("Unregistered creative tab: " + this));
        net.neoforged.neoforge.event.EventHooks.onCreativeModeTabBuildContents(this, resourcekey, this.displayItemsGenerator, parameters, creativemodetab$itemdisplaybuilder);
        this.displayItems = creativemodetab$itemdisplaybuilder.tabContents;
        this.displayItemsSearchTab = creativemodetab$itemdisplaybuilder.searchTabContents;
    }

    public Collection<ItemStack> getDisplayItems() {
        return this.displayItems;
    }

    public Collection<ItemStack> getSearchTabDisplayItems() {
        return this.displayItemsSearchTab;
    }

    public boolean contains(ItemStack stack) {
        return this.displayItemsSearchTab.contains(stack);
    }

    public boolean hasSearchBar() {
        return this.hasSearchBar;
    }

    public int getSearchBarWidth() {
        return searchBarWidth;
    }

    public net.minecraft.resources.ResourceLocation getTabsImage() {
        return tabsImage;
    }

    public int getLabelColor() {
        return labelColor;
    }

    public int getSlotColor() {
        return slotColor;
    }

    public net.minecraft.resources.ResourceLocation getScrollerSprite() {
         if (scrollerSpriteLocation == null)
              return this.canScroll() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
         return scrollerSpriteLocation;
    }

    public static class Builder {
        private static final CreativeModeTab.DisplayItemsGenerator EMPTY_GENERATOR = (p_270422_, p_259433_) -> {
        };
        private static final net.minecraft.resources.ResourceLocation CREATIVE_INVENTORY_TABS_IMAGE = net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tabs.png");
        private static final net.minecraft.resources.ResourceLocation CREATIVE_ITEM_SEARCH_BACKGROUND = createTextureLocation("item_search");
        private final CreativeModeTab.Row row;
        private final int column;
        private Component displayName = Component.empty();
        private Supplier<ItemStack> iconGenerator = () -> ItemStack.EMPTY;
        private CreativeModeTab.DisplayItemsGenerator displayItemsGenerator = EMPTY_GENERATOR;
        private boolean canScroll = true;
        private boolean showTitle = true;
        private boolean alignedRight = false;
        private CreativeModeTab.Type type = CreativeModeTab.Type.CATEGORY;
        private ResourceLocation backgroundTexture = CreativeModeTab.DEFAULT_BACKGROUND;
        @org.jetbrains.annotations.Nullable
        private net.minecraft.resources.ResourceLocation spriteScrollerLocation;
        private boolean hasSearchBar = false;
        private int searchBarWidth = 89;
        private net.minecraft.resources.ResourceLocation tabsImage = CREATIVE_INVENTORY_TABS_IMAGE;
        private int labelColor = 4210752;
        private int slotColor = -2130706433;
        private java.util.function.Function<CreativeModeTab.Builder, CreativeModeTab> tabFactory = CreativeModeTab::new;
        private final java.util.List<net.minecraft.resources.ResourceLocation> tabsBefore = new java.util.ArrayList<>();
        private final java.util.List<net.minecraft.resources.ResourceLocation> tabsAfter = new java.util.ArrayList<>();

        public Builder(CreativeModeTab.Row row, int column) {
            this.row = row;
            this.column = column;
        }

        public CreativeModeTab.Builder title(Component title) {
            this.displayName = title;
            return this;
        }

        public CreativeModeTab.Builder icon(Supplier<ItemStack> icon) {
            this.iconGenerator = icon;
            return this;
        }

        public CreativeModeTab.Builder displayItems(CreativeModeTab.DisplayItemsGenerator displayItemsGenerator) {
            this.displayItemsGenerator = displayItemsGenerator;
            return this;
        }

        public CreativeModeTab.Builder alignedRight() {
            this.alignedRight = true;
            return this;
        }

        public CreativeModeTab.Builder hideTitle() {
            this.showTitle = false;
            return this;
        }

        public CreativeModeTab.Builder noScrollBar() {
            this.canScroll = false;
            return this;
        }

        protected CreativeModeTab.Builder type(CreativeModeTab.Type type) {
            this.type = type;
            if (type == Type.SEARCH)
                return this.withSearchBar();
            return this;
        }

        public CreativeModeTab.Builder backgroundTexture(ResourceLocation backgroundTexture) {
            this.backgroundTexture = backgroundTexture;
            return this;
        }

        /**
         * Gives this tab a search bar.
         * <p>Note that, if using a custom {@link #backgroundTexture(net.minecraft.resources.ResourceLocation) background image}, you will need to make sure that your image contains the input box and the scroll bar.</p>
         */
        public CreativeModeTab.Builder withSearchBar() {
            this.hasSearchBar = true;
            if (this.backgroundTexture == CreativeModeTab.DEFAULT_BACKGROUND)
                return this.backgroundTexture(CREATIVE_ITEM_SEARCH_BACKGROUND);
            return this;
        }

        /**
         * Gives this tab a search bar, with a specific width.
         * @param searchBarWidth the width of the search bar
         */
        public CreativeModeTab.Builder withSearchBar(int searchBarWidth) {
            this.searchBarWidth = searchBarWidth;
            return withSearchBar();
        }

        /**
         * Sets the location of the scroll bar background.
         */
        public CreativeModeTab.Builder withScrollBarSpriteLocation(net.minecraft.resources.ResourceLocation scrollBarSpriteLocation) {
             this.spriteScrollerLocation = scrollBarSpriteLocation;
             return this;
        }

        /**
         * Sets the image of the tab to a custom resource location, instead of an item's texture.
         */
        public CreativeModeTab.Builder withTabsImage(net.minecraft.resources.ResourceLocation tabsImage) {
            this.tabsImage = tabsImage;
            return this;
        }

        /**
         * Sets the color of the tab label.
         */
        public CreativeModeTab.Builder withLabelColor(int labelColor) {
             this.labelColor = labelColor;
             return this;
        }

        /**
         * Sets the color of tab's slots.
         */
        public CreativeModeTab.Builder withSlotColor(int slotColor) {
             this.slotColor = slotColor;
             return this;
        }

        public CreativeModeTab.Builder withTabFactory(java.util.function.Function<CreativeModeTab.Builder, CreativeModeTab> tabFactory) {
            this.tabFactory = tabFactory;
            return this;
        }

        /** Define tabs that should come <i>before</i> this tab. This tab will be placed <strong>after</strong> the {@code tabs}. **/
        public CreativeModeTab.Builder withTabsBefore(net.minecraft.resources.ResourceLocation... tabs) {
            this.tabsBefore.addAll(java.util.List.of(tabs));
            return this;
        }

        /** Define tabs that should come <i>after</i> this tab. This tab will be placed <strong>before</strong> the {@code tabs}.**/
        public CreativeModeTab.Builder withTabsAfter(net.minecraft.resources.ResourceLocation... tabs) {
            this.tabsAfter.addAll(java.util.List.of(tabs));
            return this;
        }

        /** Define tabs that should come <i>before</i> this tab. This tab will be placed <strong>after</strong> the {@code tabs}. **/
        @SafeVarargs
        public final CreativeModeTab.Builder withTabsBefore(net.minecraft.resources.ResourceKey<CreativeModeTab>... tabs) {
            java.util.stream.Stream.of(tabs).map(net.minecraft.resources.ResourceKey::location).forEach(this.tabsBefore::add);
            return this;
        }

        /** Define tabs that should come <i>after</i> this tab. This tab will be placed <strong>before</strong> the {@code tabs}.**/
        @SafeVarargs
        public final CreativeModeTab.Builder withTabsAfter(net.minecraft.resources.ResourceKey<CreativeModeTab>... tabs) {
            java.util.stream.Stream.of(tabs).map(net.minecraft.resources.ResourceKey::location).forEach(this.tabsAfter::add);
            return this;
        }

        public CreativeModeTab build() {
            if ((this.type == CreativeModeTab.Type.HOTBAR || this.type == CreativeModeTab.Type.INVENTORY) && this.displayItemsGenerator != EMPTY_GENERATOR) {
                throw new IllegalStateException("Special tabs can't have display items");
            } else {
                CreativeModeTab creativemodetab = tabFactory.apply(this);
                creativemodetab.alignedRight = this.alignedRight;
                creativemodetab.showTitle = this.showTitle;
                creativemodetab.canScroll = this.canScroll;
                creativemodetab.backgroundTexture = this.backgroundTexture;
                return creativemodetab;
            }
        }
    }

    @FunctionalInterface
    public interface DisplayItemsGenerator {
        void accept(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output);
    }

    static class ItemDisplayBuilder implements CreativeModeTab.Output {
        public final Collection<ItemStack> tabContents = ItemStackLinkedSet.createTypeAndComponentsSet();
        public final Set<ItemStack> searchTabContents = ItemStackLinkedSet.createTypeAndComponentsSet();
        private final CreativeModeTab tab;
        private final FeatureFlagSet featureFlagSet;

        public ItemDisplayBuilder(CreativeModeTab tab, FeatureFlagSet featureFlagSet) {
            this.tab = tab;
            this.featureFlagSet = featureFlagSet;
        }

        @Override
        public void accept(ItemStack stack, CreativeModeTab.TabVisibility tabVisibility) {
            if (stack.getCount() != 1) {
                throw new IllegalArgumentException("Stack size must be exactly 1");
            } else {
                boolean flag = this.tabContents.contains(stack) && tabVisibility != CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY;
                if (flag) {
                    throw new IllegalStateException(
                        "Accidentally adding the same item stack twice "
                            + stack.getDisplayName().getString()
                            + " to a Creative Mode Tab: "
                            + this.tab.getDisplayName().getString()
                    );
                } else {
                    if (stack.getItem().isEnabled(this.featureFlagSet)) {
                        switch (tabVisibility) {
                            case PARENT_AND_SEARCH_TABS:
                                this.tabContents.add(stack);
                                this.searchTabContents.add(stack);
                                break;
                            case PARENT_TAB_ONLY:
                                this.tabContents.add(stack);
                                break;
                            case SEARCH_TAB_ONLY:
                                this.searchTabContents.add(stack);
                        }
                    }
                }
            }
        }
    }

    public static record ItemDisplayParameters(FeatureFlagSet enabledFeatures, boolean hasPermissions, HolderLookup.Provider holders) {
        public boolean needsUpdate(FeatureFlagSet enabledFeatures, boolean hasPermissions, HolderLookup.Provider holders) {
            return !this.enabledFeatures.equals(enabledFeatures) || this.hasPermissions != hasPermissions || this.holders != holders;
        }
    }

    public interface Output {
        void accept(ItemStack stack, CreativeModeTab.TabVisibility tabVisibility);

        default void accept(ItemStack stack) {
            this.accept(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        default void accept(ItemLike item, CreativeModeTab.TabVisibility tabVisibility) {
            this.accept(new ItemStack(item), tabVisibility);
        }

        default void accept(ItemLike item) {
            this.accept(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        default void acceptAll(Collection<ItemStack> stacks, CreativeModeTab.TabVisibility tabVisibility) {
            stacks.forEach(p_252337_ -> this.accept(p_252337_, tabVisibility));
        }

        default void acceptAll(Collection<ItemStack> stacks) {
            this.acceptAll(stacks, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    public static enum Row {
        TOP,
        BOTTOM;
    }

    public static enum TabVisibility {
        PARENT_AND_SEARCH_TABS,
        PARENT_TAB_ONLY,
        SEARCH_TAB_ONLY;
    }

    public static enum Type {
        CATEGORY,
        INVENTORY,
        HOTBAR,
        SEARCH;
    }
}
