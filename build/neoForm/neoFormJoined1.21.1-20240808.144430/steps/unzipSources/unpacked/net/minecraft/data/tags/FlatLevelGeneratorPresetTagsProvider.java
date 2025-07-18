package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPresets;

public class FlatLevelGeneratorPresetTagsProvider extends TagsProvider<FlatLevelGeneratorPreset> {
    /**
 * @deprecated Forge: Use the {@linkplain #FlatLevelGeneratorPresetTagsProvider(
 *             PackOutput, CompletableFuture, String,
 *             net.neoforged.neoforge.common.data.ExistingFileHelper) mod id
 *             variant}
 */
    @Deprecated
    public FlatLevelGeneratorPresetTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, Registries.FLAT_LEVEL_GENERATOR_PRESET, provider);
    }
    public FlatLevelGeneratorPresetTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, String modId, @org.jetbrains.annotations.Nullable net.neoforged.neoforge.common.data.ExistingFileHelper existingFileHelper) {
        super(output, Registries.FLAT_LEVEL_GENERATOR_PRESET, provider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(FlatLevelGeneratorPresetTags.VISIBLE)
            .add(FlatLevelGeneratorPresets.CLASSIC_FLAT)
            .add(FlatLevelGeneratorPresets.TUNNELERS_DREAM)
            .add(FlatLevelGeneratorPresets.WATER_WORLD)
            .add(FlatLevelGeneratorPresets.OVERWORLD)
            .add(FlatLevelGeneratorPresets.SNOWY_KINGDOM)
            .add(FlatLevelGeneratorPresets.BOTTOMLESS_PIT)
            .add(FlatLevelGeneratorPresets.DESERT)
            .add(FlatLevelGeneratorPresets.REDSTONE_READY)
            .add(FlatLevelGeneratorPresets.THE_VOID);
    }
}
