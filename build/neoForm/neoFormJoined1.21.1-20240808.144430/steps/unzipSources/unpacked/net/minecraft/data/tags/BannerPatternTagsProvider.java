package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;

public class BannerPatternTagsProvider extends TagsProvider<BannerPattern> {
    /**
 * @deprecated Forge: Use the {@linkplain #BannerPatternTagsProvider(PackOutput,
 *             CompletableFuture, String,
 *             net.neoforged.neoforge.common.data.ExistingFileHelper) mod id
 *             variant}
 */
    @Deprecated
    public BannerPatternTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, Registries.BANNER_PATTERN, provider);
    }
    public BannerPatternTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, String modId, @org.jetbrains.annotations.Nullable net.neoforged.neoforge.common.data.ExistingFileHelper existingFileHelper) {
        super(output, Registries.BANNER_PATTERN, provider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BannerPatternTags.NO_ITEM_REQUIRED)
            .add(
                BannerPatterns.SQUARE_BOTTOM_LEFT,
                BannerPatterns.SQUARE_BOTTOM_RIGHT,
                BannerPatterns.SQUARE_TOP_LEFT,
                BannerPatterns.SQUARE_TOP_RIGHT,
                BannerPatterns.STRIPE_BOTTOM,
                BannerPatterns.STRIPE_TOP,
                BannerPatterns.STRIPE_LEFT,
                BannerPatterns.STRIPE_RIGHT,
                BannerPatterns.STRIPE_CENTER,
                BannerPatterns.STRIPE_MIDDLE,
                BannerPatterns.STRIPE_DOWNRIGHT,
                BannerPatterns.STRIPE_DOWNLEFT,
                BannerPatterns.STRIPE_SMALL,
                BannerPatterns.CROSS,
                BannerPatterns.STRAIGHT_CROSS,
                BannerPatterns.TRIANGLE_BOTTOM,
                BannerPatterns.TRIANGLE_TOP,
                BannerPatterns.TRIANGLES_BOTTOM,
                BannerPatterns.TRIANGLES_TOP,
                BannerPatterns.DIAGONAL_LEFT,
                BannerPatterns.DIAGONAL_RIGHT,
                BannerPatterns.DIAGONAL_LEFT_MIRROR,
                BannerPatterns.DIAGONAL_RIGHT_MIRROR,
                BannerPatterns.CIRCLE_MIDDLE,
                BannerPatterns.RHOMBUS_MIDDLE,
                BannerPatterns.HALF_VERTICAL,
                BannerPatterns.HALF_HORIZONTAL,
                BannerPatterns.HALF_VERTICAL_MIRROR,
                BannerPatterns.HALF_HORIZONTAL_MIRROR,
                BannerPatterns.BORDER,
                BannerPatterns.CURLY_BORDER,
                BannerPatterns.GRADIENT,
                BannerPatterns.GRADIENT_UP,
                BannerPatterns.BRICKS
            );
        this.tag(BannerPatternTags.PATTERN_ITEM_FLOWER).add(BannerPatterns.FLOWER);
        this.tag(BannerPatternTags.PATTERN_ITEM_CREEPER).add(BannerPatterns.CREEPER);
        this.tag(BannerPatternTags.PATTERN_ITEM_SKULL).add(BannerPatterns.SKULL);
        this.tag(BannerPatternTags.PATTERN_ITEM_MOJANG).add(BannerPatterns.MOJANG);
        this.tag(BannerPatternTags.PATTERN_ITEM_GLOBE).add(BannerPatterns.GLOBE);
        this.tag(BannerPatternTags.PATTERN_ITEM_PIGLIN).add(BannerPatterns.PIGLIN);
        this.tag(BannerPatternTags.PATTERN_ITEM_FLOW).add(BannerPatterns.FLOW);
        this.tag(BannerPatternTags.PATTERN_ITEM_GUSTER).add(BannerPatterns.GUSTER);
    }
}
