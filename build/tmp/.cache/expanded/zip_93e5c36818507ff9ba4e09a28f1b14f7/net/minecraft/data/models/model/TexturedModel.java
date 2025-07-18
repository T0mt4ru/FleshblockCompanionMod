package net.minecraft.data.models.model;

import com.google.gson.JsonElement;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class TexturedModel {
    public static final TexturedModel.Provider CUBE = createDefault(TextureMapping::cube, ModelTemplates.CUBE_ALL);
    public static final TexturedModel.Provider CUBE_INNER_FACES = createDefault(TextureMapping::cube, ModelTemplates.CUBE_ALL_INNER_FACES);
    public static final TexturedModel.Provider CUBE_MIRRORED = createDefault(TextureMapping::cube, ModelTemplates.CUBE_MIRRORED_ALL);
    public static final TexturedModel.Provider COLUMN = createDefault(TextureMapping::column, ModelTemplates.CUBE_COLUMN);
    public static final TexturedModel.Provider COLUMN_HORIZONTAL = createDefault(TextureMapping::column, ModelTemplates.CUBE_COLUMN_HORIZONTAL);
    public static final TexturedModel.Provider CUBE_TOP_BOTTOM = createDefault(TextureMapping::cubeBottomTop, ModelTemplates.CUBE_BOTTOM_TOP);
    public static final TexturedModel.Provider CUBE_TOP = createDefault(TextureMapping::cubeTop, ModelTemplates.CUBE_TOP);
    public static final TexturedModel.Provider ORIENTABLE_ONLY_TOP = createDefault(TextureMapping::orientableCubeOnlyTop, ModelTemplates.CUBE_ORIENTABLE);
    public static final TexturedModel.Provider ORIENTABLE = createDefault(TextureMapping::orientableCube, ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM);
    public static final TexturedModel.Provider CARPET = createDefault(TextureMapping::wool, ModelTemplates.CARPET);
    public static final TexturedModel.Provider FLOWERBED_1 = createDefault(TextureMapping::flowerbed, ModelTemplates.FLOWERBED_1);
    public static final TexturedModel.Provider FLOWERBED_2 = createDefault(TextureMapping::flowerbed, ModelTemplates.FLOWERBED_2);
    public static final TexturedModel.Provider FLOWERBED_3 = createDefault(TextureMapping::flowerbed, ModelTemplates.FLOWERBED_3);
    public static final TexturedModel.Provider FLOWERBED_4 = createDefault(TextureMapping::flowerbed, ModelTemplates.FLOWERBED_4);
    public static final TexturedModel.Provider GLAZED_TERRACOTTA = createDefault(TextureMapping::pattern, ModelTemplates.GLAZED_TERRACOTTA);
    public static final TexturedModel.Provider CORAL_FAN = createDefault(TextureMapping::fan, ModelTemplates.CORAL_FAN);
    public static final TexturedModel.Provider PARTICLE_ONLY = createDefault(TextureMapping::particle, ModelTemplates.PARTICLE_ONLY);
    public static final TexturedModel.Provider ANVIL = createDefault(TextureMapping::top, ModelTemplates.ANVIL);
    public static final TexturedModel.Provider LEAVES = createDefault(TextureMapping::cube, ModelTemplates.LEAVES);
    public static final TexturedModel.Provider LANTERN = createDefault(TextureMapping::lantern, ModelTemplates.LANTERN);
    public static final TexturedModel.Provider HANGING_LANTERN = createDefault(TextureMapping::lantern, ModelTemplates.HANGING_LANTERN);
    public static final TexturedModel.Provider SEAGRASS = createDefault(TextureMapping::defaultTexture, ModelTemplates.SEAGRASS);
    public static final TexturedModel.Provider COLUMN_ALT = createDefault(TextureMapping::logColumn, ModelTemplates.CUBE_COLUMN);
    public static final TexturedModel.Provider COLUMN_HORIZONTAL_ALT = createDefault(TextureMapping::logColumn, ModelTemplates.CUBE_COLUMN_HORIZONTAL);
    public static final TexturedModel.Provider TOP_BOTTOM_WITH_WALL = createDefault(TextureMapping::cubeBottomTopWithWall, ModelTemplates.CUBE_BOTTOM_TOP);
    public static final TexturedModel.Provider COLUMN_WITH_WALL = createDefault(TextureMapping::columnWithWall, ModelTemplates.CUBE_COLUMN);
    private final TextureMapping mapping;
    private final ModelTemplate template;

    private TexturedModel(TextureMapping mapping, ModelTemplate template) {
        this.mapping = mapping;
        this.template = template;
    }

    public ModelTemplate getTemplate() {
        return this.template;
    }

    public TextureMapping getMapping() {
        return this.mapping;
    }

    public TexturedModel updateTextures(Consumer<TextureMapping> textureMappingConsumer) {
        textureMappingConsumer.accept(this.mapping);
        return this;
    }

    public ResourceLocation create(Block modelBlock, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput) {
        return this.template.create(modelBlock, this.mapping, modelOutput);
    }

    public ResourceLocation createWithSuffix(Block modelBlock, String modelLocationSuffix, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput) {
        return this.template.createWithSuffix(modelBlock, modelLocationSuffix, this.mapping, modelOutput);
    }

    private static TexturedModel.Provider createDefault(Function<Block, TextureMapping> blockToTextureMapping, ModelTemplate modelTemplate) {
        return p_125948_ -> new TexturedModel(blockToTextureMapping.apply(p_125948_), modelTemplate);
    }

    public static TexturedModel createAllSame(ResourceLocation allTextureLocation) {
        return new TexturedModel(TextureMapping.cube(allTextureLocation), ModelTemplates.CUBE_ALL);
    }

    @FunctionalInterface
    public interface Provider {
        TexturedModel get(Block block);

        default ResourceLocation create(Block modelBlock, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput) {
            return this.get(modelBlock).create(modelBlock, modelOutput);
        }

        default ResourceLocation createWithSuffix(Block modelBlock, String modelLocationSuffix, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput) {
            return this.get(modelBlock).createWithSuffix(modelBlock, modelLocationSuffix, modelOutput);
        }

        default TexturedModel.Provider updateTexture(Consumer<TextureMapping> textureMappingConsumer) {
            return p_125963_ -> this.get(p_125963_).updateTextures(textureMappingConsumer);
        }
    }
}
