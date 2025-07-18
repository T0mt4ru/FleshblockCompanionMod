package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public class WorldPresetTagsProvider extends TagsProvider<WorldPreset> {
    /**
 * @deprecated Forge: Use the {@linkplain #WorldPresetTagsProvider(PackOutput,
 *             CompletableFuture, String,
 *             net.neoforged.neoforge.common.data.ExistingFileHelper) mod id
 *             variant}
 */
    @Deprecated
    public WorldPresetTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, Registries.WORLD_PRESET, provider);
    }
    public WorldPresetTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, String modId, @org.jetbrains.annotations.Nullable net.neoforged.neoforge.common.data.ExistingFileHelper existingFileHelper) {
        super(output, Registries.WORLD_PRESET, provider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(WorldPresetTags.NORMAL)
            .add(WorldPresets.NORMAL)
            .add(WorldPresets.FLAT)
            .add(WorldPresets.LARGE_BIOMES)
            .add(WorldPresets.AMPLIFIED)
            .add(WorldPresets.SINGLE_BIOME_SURFACE);
        this.tag(WorldPresetTags.EXTENDED).addTag(WorldPresetTags.NORMAL).add(WorldPresets.DEBUG);
    }
}
